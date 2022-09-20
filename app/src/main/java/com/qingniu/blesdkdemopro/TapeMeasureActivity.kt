package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.model.QNLengthUnit
import com.qingniu.qnrulerplugin.QNRulerPlugin
import com.qingniu.qnrulerplugin.listener.QNRulerDataListener
import com.qingniu.qnrulerplugin.listener.QNRulerDeviceListener
import com.qingniu.qnrulerplugin.model.QNRulerData
import com.qingniu.qnrulerplugin.model.QNRulerDevice

class TapeMeasureActivity : ComponentActivity() {

    companion object {
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, TapeMeasureActivity::class.java)
        }
    }

    lateinit var mViewModel: TapeViewModel

    val mHandler = Handler(Looper.getMainLooper())

    var tapeDevice: QNRulerDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleSdkDemoProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    mViewModel = viewModel()
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BgGrey)
                    ) {
                        TitleBar("Ruler", false)
                        TapeStatusBar()
                        TapeMeasureBoard()
                    }
                }
            }
        }
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        QNPlugin.getInstance(this).stopScan()
        tapeDevice?.let {
            QNRulerPlugin.cancelConnectDevice(it)
        }
    }

    private fun init() {
        QNPlugin.getInstance(this).startScan()
        QNRulerPlugin.setRulerPlugin(QNPlugin.getInstance(this))
        QNRulerPlugin.setDeviceListener(object : QNRulerDeviceListener{
            override fun onDiscoverRulerDevice(device: QNRulerDevice?) {
                Log.e("tape", "发现围度尺设备")
                this@TapeMeasureActivity.tapeDevice.let {
                    QNRulerPlugin.cancelConnectDevice(it)
                }
                this@TapeMeasureActivity.tapeDevice = device
                this@TapeMeasureActivity.tapeDevice.let {
                    QNRulerPlugin.connectDevice(it)
                }
            }

            override fun onRulerConnectedSuccess(device: QNRulerDevice?) {
                Log.e("tape", "连接成功, 停止扫描")
                QNPlugin.getInstance(this@TapeMeasureActivity).stopScan()
                mViewModel.vState.value = TapeViewModel.MeasureState.CONNECT
                mViewModel.mac.value = device?.mac.toString()
            }

            override fun onRulerConnectFail(device: QNRulerDevice?) {
                Log.e("tape", "连接失败")
                mViewModel.vState.value = TapeViewModel.MeasureState.DISCONNECT
            }

            override fun onRulerReadyInteract(code: Int, device: QNRulerDevice?) {
                Log.e("tape", "设备允许交互, code = $code")
            }

            override fun onRulerDisconnected(device: QNRulerDevice?) {
                Log.e("tape", "断开连接")
                mViewModel.vState.value = TapeViewModel.MeasureState.DISCONNECT
            }

        })

        QNRulerPlugin.setDataListener(object : QNRulerDataListener{
            override fun onRulerRealTimeData(data: QNRulerData?, device: QNRulerDevice?) {
                Log.e("tape", "实时测量数据：  ${data?.value}")
                mViewModel.vState.value = TapeViewModel.MeasureState.MEASURE_LENGTH
                mViewModel.heightStr.value = createTapeLengthStr(data?.value!!, data?.unit!!)
            }

            override fun onRulerReceiveMeasureResult(data: QNRulerData?, device: QNRulerDevice?) {
                Log.e("tape", "测量结束： data = $data")
                mViewModel.vState.value = TapeViewModel.MeasureState.MEASURE_END
                mViewModel.heightStr.value = createTapeLengthStr(data?.value!!, data?.unit!!)
            }

        })
    }
}

fun createTapeLengthStr(length: Double, unit: QNLengthUnit): String{
    when(unit){
        QNLengthUnit.UNIT_CM -> {
            return "$length cm"
        }
        QNLengthUnit.UNIT_IN -> {
            return "$length inch"
        }
        else ->{
            return "$length cm"
        }
    }
}

@Composable
fun TapeStatusBar() {
    val ctx = LocalContext.current
    val hsvm: TapeViewModel = viewModel()
    val status = if (!DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Bluetooth turn off"
    } else if (!DemoBleUtils.isRunOnAndroid12Mode(ctx) && !DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Location turn off"
    } else if (!DemoBleUtils.hasBlePermission(ctx)) {
        "Need bluetooth permission"
    } else {
        when (hsvm.vState.value) {
            TapeViewModel.MeasureState.CONNECT -> "Connected"
            TapeViewModel.MeasureState.DISCONNECT -> "Disconnected"
            TapeViewModel.MeasureState.MEASURE_LENGTH -> "Measure length"
            TapeViewModel.MeasureState.MEASURE_END -> "Measure end"
            TapeViewModel.MeasureState.MEASURE_FAIL -> "Measure fail"
        }
    }
    Box(
        Modifier
            .padding(top = 50.dp)
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

@Composable
fun TapeMeasureBoard() {
    val ctx = LocalContext.current
    val hsvm: TapeViewModel = viewModel()
    Column(
        Modifier
            .padding(top = 100.dp)
            .fillMaxSize()
    ) {
        val cMac = hsvm.mac.value
        val cHeightStr = hsvm.heightStr.value
        val cvState = hsvm.vState.value

        Box(
            Modifier
                .fillMaxWidth()
                .height(30.dp)
        ) {
            Text(
                text = cMac,
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp)
            )
            Text(
                text = "AppId: " + QNPlugin.getInstance(ctx).appId,
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            )
        }
        Text(
            text = if (cvState == TapeViewModel.MeasureState.MEASURE_LENGTH ||
                cvState == TapeViewModel.MeasureState.MEASURE_END ||
                cvState == TapeViewModel.MeasureState.MEASURE_FAIL
            ) {
                cHeightStr
            } else {
                ""
            },

            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp, bottom = 10.dp)
        )
        if (cvState == TapeViewModel.MeasureState.MEASURE_END) {
            Column {
                TapeIndicator("length", cHeightStr, false)
            }
        }
    }
}

@Composable
fun TapeIndicator(
    indicatorName: String,
    indicatorValue: String,
    showBottomDivider: Boolean = false,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
    ) {
        Divider(
            color = DividerGrey,
            modifier = Modifier
                .align(Alignment.TopStart)
                .height(1.dp)
                .fillMaxWidth()
        )
        Text(
            text = indicatorName,
            Modifier
                .padding(30.dp, 10.dp, 0.dp, 10.dp)
                .align(Alignment.CenterStart),
            fontSize = 16.sp
        )
        Text(
            text = indicatorValue,
            Modifier
                .padding(0.dp, 10.dp, 30.dp, 10.dp)
                .align(Alignment.CenterEnd),
            fontSize = 16.sp
        )
        if (showBottomDivider) {
            Divider(
                color = DividerGrey,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .height(1.dp)
                    .fillMaxWidth()
            )
        }
    }
}

class TapeViewModel : ViewModel() {

    enum class MeasureState {
        CONNECT, DISCONNECT, MEASURE_LENGTH, MEASURE_END, MEASURE_FAIL
    }

    var mac: MutableState<String> = mutableStateOf("")
    var heightStr: MutableState<String> = mutableStateOf("--")
    var vState: MutableState<MeasureState> = mutableStateOf(MeasureState.DISCONNECT)
}