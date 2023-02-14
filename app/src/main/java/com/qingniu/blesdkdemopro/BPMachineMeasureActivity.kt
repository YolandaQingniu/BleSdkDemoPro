package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.db.table.DeviceUser
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.BPMeasureBoard
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.blesdkdemopro.util.ui.theme.BleSdkDemoProTheme
import com.qingniu.qnbpmachineplugin.*
import com.qingniu.qnbpmachineplugin.listener.QNBPMachineDataListener
import com.qingniu.qnbpmachineplugin.listener.QNBPMachineDeviceListener
import com.qingniu.qnbpmachineplugin.listener.QNBPMachineWiFiListener
import com.qingniu.qnplugin.QNPlugin

class BPMachineMeasureActivity : ComponentActivity() {

    companion object {
        const val TAG = "BPMachineMeasure"
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, BPMachineMeasureActivity::class.java)
        }
    }

    lateinit var bpViewModel: BPViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleSdkDemoProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    bpViewModel = viewModel()
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BgGrey)
                    ) {
                        Column{
                            TitleBar("BPMachine", true)
                            BPStatusBar()
                            BPMeasureBoard()
                        }

                        testBle()
                    }
                }
            }
        }
    }

    private fun testBle(){
        QNBPMachinePlugin.setBPMachinePlugin(QNPlugin.getInstance(this))

        QNBPMachinePlugin.setDataListener(object :QNBPMachineDataListener{
            override fun onReceiveRealTimeData(
                data: QNBPMachineData,
                device: QNBPMachineDevice,
                measureResult: QNBPMachineMeasureResult
            ) {
                if (measureResult != QNBPMachineMeasureResult.SUCCESS){
                    bpViewModel.measureError.value = measureResult
                    bpViewModel.vState.value = BPViewModel.MeasureState.MEASURE_FAIL
                }else{
                    bpViewModel.measureError.value = null
                    bpViewModel.vState.value = BPViewModel.MeasureState.MEASURE_END

                    val tempList = bpViewModel.datalist.value.toMutableList()
                    tempList.add(data)
                    bpViewModel.datalist.value = tempList
                }
            }

            override fun onReceiveStoredData(data: QNBPMachineData, device: QNBPMachineDevice) {
                val tempList = bpViewModel.datalist.value.toMutableList()
                tempList.add(data)
                bpViewModel.datalist.value = tempList
            }
        })

        QNBPMachinePlugin.setDeviceListener(object :QNBPMachineDeviceListener{
            override fun onDiscoverDevice(device: QNBPMachineDevice) {
                QNPlugin.getInstance(this@BPMachineMeasureActivity).stopScan()
                Log.e(TAG,"发现 $device")
                QNBPMachinePlugin.connectDevice(device)
                val dao = DemoDataBase.getInstance(this@BPMachineMeasureActivity).deviceUserDao()
                val bindeDevice = dao.getMacDevice(device.mac)
                if (null == bindeDevice || bindeDevice.isEmpty()){
                    dao.insert(DeviceUser().apply {
                        mac = device.mac
                        userId = ""
                        key = 0
                        isVisitorMode = false
                        isSupportUser = false
                        isSupportWifi = true
                    })
                }
            }

            override fun onConnectedSuccess(device: QNBPMachineDevice) {
                bpViewModel.vState.value = BPViewModel.MeasureState.CONNECT
            }

            override fun onConnectFail(code: Int, device: QNBPMachineDevice) {
                bpViewModel.vState.value = BPViewModel.MeasureState.DISCONNECT
            }

            override fun onReadyInteractResult(code: Int, device: QNBPMachineDevice) {
                Toast.makeText(this@BPMachineMeasureActivity, "有${device.currentStorageCount}条存储数据", Toast.LENGTH_SHORT).show()

                val dao = DemoDataBase.getInstance(this@BPMachineMeasureActivity).bpMachineSettingDao()
                dao.getBPMachineSetting().apply {

                    val unit = if (this.unit == QNBPMachineUnit.KPA.toString()){
                        QNBPMachineUnit.KPA
                    }else{
                        QNBPMachineUnit.MMHG
                    }

                    val volume = if (this.volume == QNBPMachineVolume.FIRST_LEVEL.toString()) {
                        QNBPMachineVolume.FIRST_LEVEL
                    } else if (this.volume == QNBPMachineVolume.SECOND_LEVEL.toString()) {
                        QNBPMachineVolume.SECOND_LEVEL
                    } else if (this.volume == QNBPMachineVolume.THIRD_LEVEL.toString()) {
                        QNBPMachineVolume.THIRD_LEVEL
                    } else if (this.volume == QNBPMachineVolume.THIRD_LEVEL.toString()) {
                        QNBPMachineVolume.THIRD_LEVEL
                    } else if (this.volume == QNBPMachineVolume.FOURTH_LEVEL.toString()) {
                        QNBPMachineVolume.FOURTH_LEVEL
                    } else if (this.volume == QNBPMachineVolume.FIFTH_LEVEL.toString()) {
                        QNBPMachineVolume.FIFTH_LEVEL
                    } else {
                        QNBPMachineVolume.MUTE
                    }

                    val standard = if (this.standard == QNBPMachineStandard.USA.toString()) {
                        QNBPMachineStandard.USA
                    } else if (this.standard == QNBPMachineStandard.EUROPE.toString()) {
                        QNBPMachineStandard.EUROPE
                    }else {
                        QNBPMachineStandard.CHINA
                    }

                    val language = if (this.language == QNBPMachineLanguage.ENGLISH.toString()){
                        QNBPMachineLanguage.ENGLISH
                    }else{
                        QNBPMachineLanguage.CHINESE
                    }

                    val config = QNBPMachineDeploy.buildDeploy(
                        unit,
                        volume,
                        standard,
                        language)

                    QNBPMachinePlugin.setDeviceFunction(device, config)
                }
            }

            override fun onSetFunctionResult(code: Int, device: QNBPMachineDevice) {
                if (code == 0){
                    QNBPMachinePlugin.readStoredData(device)
                }
            }

            override fun onDisconnected(device: QNBPMachineDevice?) {
                bpViewModel.vState.value = BPViewModel.MeasureState.DISCONNECT
            }
        })

        QNPlugin.getInstance(this).startScan()
    }
}

@Composable
fun BPStatusBar() {
    val ctx = LocalContext.current
    val hsvm: BPViewModel = viewModel()
    val status = if (!DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Bluetooth turn off"
    } else if (!DemoBleUtils.isRunOnAndroid12Mode(ctx) && !DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Location turn off"
    } else if (!DemoBleUtils.hasBlePermission(ctx)) {
        "Need bluetooth permission"
    } else {
        when (hsvm.vState.value) {
            BPViewModel.MeasureState.CONNECT -> "Connected"
            BPViewModel.MeasureState.DISCONNECT -> "Disconnected"
            BPViewModel.MeasureState.MEASURE_END -> "Measure end"
            BPViewModel.MeasureState.MEASURE_FAIL -> "Measure fail"
        }
    }
    Box(
        Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(TipGrey)
    ) {
        Text(
            text = status,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

class BPViewModel : ViewModel() {

    enum class MeasureState {
        CONNECT, DISCONNECT, MEASURE_END, MEASURE_FAIL
    }

    var measureError: MutableState<QNBPMachineMeasureResult?> = mutableStateOf(null)
    var vState: MutableState<MeasureState> = mutableStateOf(MeasureState.DISCONNECT)
    var datalist:MutableState<List<QNBPMachineData>> = mutableStateOf(emptyList())
}