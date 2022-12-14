package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qingniu.blesdkdemopro.constant.DemoUnit
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.qnbpmachineplugin.*
import com.qingniu.qnbpmachineplugin.listener.QNBPMachineDeviceListener
import com.qingniu.qnbpmachineplugin.listener.QNBPMachineWiFiListener
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.model.QNWeightUnit
import com.qingniu.qnscaleplugin.QNScalePlugin
import com.qingniu.qnscaleplugin.QNScaleWiFiMp
import com.qingniu.qnscaleplugin.listener.QNScaleDeviceListener
import com.qingniu.qnscaleplugin.listener.QNScaleStatusListener
import com.qingniu.qnscaleplugin.listener.QNScaleWiFiListener
import com.qingniu.qnscaleplugin.model.QNScaleDevice
import com.qingniu.qnscaleplugin.model.QNScaleOperate
import com.qingniu.qnscaleplugin.model.QNWiFiInfo

class QNScalePairNetActivity : ComponentActivity() {
    // Is connect deivce
    private var mIsConnecting = false
    // 是否已经连接设备
    private var mIsConnected = false

    //指定的mac
    private var specifiedMac:String = ""

    companion object {
        const val TAG = "QNPairNet"
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, QNScalePairNetActivity::class.java)
        }

        fun getCallIntent(ctx: Context ,mac: String): Intent {
            return Intent(ctx, QNScalePairNetActivity::class.java).putExtra("mac", mac)
        }
    }

    lateinit var mViewModel: QNScalePairNetViewModel

    val mHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                0 -> {
                    mProgress.value += 0.025F
                }
            }
        }
    }

    var mProgress: MutableState<Float> = mutableStateOf(0F)

    var mDevice: QNScaleDevice? = null

    var mBPMachine:QNBPMachineDevice? = null

    var mSsid = ""
    var mPwd = ""
    var mServerUrl = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        specifiedMac = intent.getStringExtra("mac")?:""
        setContent {
            BleSdkDemoProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val ctx = LocalContext.current
                    val dao = DemoDataBase.getInstance(ctx).wifiInfoDao()

                    val ssid = remember {
                        mutableStateOf(dao.getWifiInfo().ssid)
                    }
                    val password = remember {
                        mutableStateOf(dao.getWifiInfo().password)
                    }
                    val serverUrl = remember {
                        val url = dao.getWifiInfo().serverUrl
                        mutableStateOf(if(TextUtils.isEmpty(url)) url else "http://wifi.yolanda.hk:80/wifi_api/wsps?device_type=7&code=")
                    }
                    mSsid = ssid.value
                    mPwd = password.value
                    mServerUrl = serverUrl.value
                    mViewModel = viewModel()
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BgGrey)
                    ) {
                        TitleBar("QNScalePairNet", false)
                        QNScalePairNetStatusBar()
                        Column(
                            Modifier
                                .padding(top = 100.dp)
                                .fillMaxSize()
                        ) {
                            val cMac = mViewModel.mac.value

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
                                    text = "AppId: " + QNPlugin.getInstance(this@QNScalePairNetActivity).appId,
                                    Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 20.dp)
                                )
                            }
                            Column(
                                Modifier.padding(top = 20.dp)
                            ) {
                                Text(
                                    text = "ssid", fontSize = 16.sp,
                                    modifier = Modifier
                                        .padding(start = 10.dp, bottom = 10.dp)
                                        .fillMaxWidth()
                                )
                                TextField(value = ssid.value.toString(), onValueChange = {
                                    if (!TextUtils.isEmpty(it)) {
                                        ssid.value = it

                                    } else {
                                        ssid.value = ""
                                    }
                                    mSsid = ssid.value
                                },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                                )
                            }
                            Column(Modifier.padding(top = 20.dp)) {
                                Text(
                                    text = "password", fontSize = 16.sp,
                                    modifier = Modifier
                                        .padding(start = 10.dp, bottom = 10.dp)
                                        .fillMaxWidth()
                                )
                                TextField(value = password.value.toString(), onValueChange = {
                                    if (!TextUtils.isEmpty(it)) {
                                        password.value = it

                                    } else {
                                        password.value = ""
                                    }
                                    mPwd = password.value
                                },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                )
                            }
                            Column(
                                Modifier
                                    .padding(top = 40.dp, bottom = 80.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Button(
                                    onClick = {pairNet()},
                                    Modifier
                                        .width(200.dp)
                                        .height(56.dp)
                                ) {
                                    Text(text = "Pair Net", fontSize = 18.sp)
                                }
                            }

                            Column(
                                Modifier
                                    .padding(top = 60.dp, bottom = 40.dp)
                                    .fillMaxWidth(),
                            ) {
                                LinearProgressIndicator(
                                    // 设置水平进度条当前进度颜色
                                    color = Color.Black,
                                    // 设置水平进度条总长度颜色
                                    backgroundColor = Color.Gray,
                                    // 设置水平进度条当前进度
                                    progress = mProgress.value,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        QNPlugin.getInstance(this).stopScan()
        mDevice?.let {
            QNScalePlugin.cancelConnectDevice(it)
        }
    }

    private fun init() {
        if (!TextUtils.isEmpty(specifiedMac)){
            QNBPMachinePlugin.setBPMachinePlugin(QNPlugin.getInstance(this))

            QNBPMachinePlugin.setDeviceListener(object :QNBPMachineDeviceListener{
                override fun onDiscoverBPMachineDevice(device: QNBPMachineDevice) {
                    Log.e(TAG,"发现 $device")
                    if (specifiedMac.toUpperCase().equals(device.mac.toUpperCase())){
                        QNBPMachinePlugin.connectDevice(device)
                        QNPlugin.getInstance(this@QNScalePairNetActivity).stopScan()
                    }
                }

                override fun onBPMachineConnectedSuccess(device: QNBPMachineDevice) {
                    mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.CONNECT
                    mBPMachine = device
                }

                override fun onBPMachineConnectFail(code: Int, device: QNBPMachineDevice) {
                    mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.DISCONNECT
                    mBPMachine = null
                }

                override fun onBPMachineReadyInteractResult(code: Int, device: QNBPMachineDevice) {
                    val dao = DemoDataBase.getInstance(this@QNScalePairNetActivity).bpMachineSettingDao()
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
                        } else if (this.standard == QNBPMachineStandard.JAPAN.toString()) {
                            QNBPMachineStandard.JAPAN
                        } else {
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
                            language,
                            QNBPMachineTimeZone.E8
                        )

                        QNBPMachinePlugin.setDeviceFunction(device, config)
                    }
                }

                override fun onSetBPMachineFunctionResult(code: Int, device: QNBPMachineDevice) {
                }

                override fun onBPMachineDisconnected(device: QNBPMachineDevice) {
                    mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.DISCONNECT
                    mBPMachine = null
                }
            })

            QNBPMachineWiFiMp.setWiFiStatusListener(object :QNBPMachineWiFiListener{
                override fun onDiscoveryNearbyWiFi(
                    ssid: String,
                    rssi: Int,
                    device: QNBPMachineDevice
                ) {

                }

                override fun onStartWiFiConnect(device: QNBPMachineDevice?) {
                    mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.PAIR_NET_ING
                }

                override fun onConnectWiFiStatus(code: Int, device: QNBPMachineDevice?) {
                    if (code == 0){
                        mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.PAIR_NET_SUCCESS
                    }else{
                        mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.PAIR_NET_FAIL
                    }
                }
            })

            QNPlugin.getInstance(this).startScan()

        }else{
            QNPlugin.getInstance(this).startScan()
            QNScalePlugin.setScalePlugin(QNPlugin.getInstance(this))
            QNScalePlugin.setDeviceListener(object : QNScaleDeviceListener {
                override fun onDiscoverScaleDevice(device: QNScaleDevice?) {
                    Log.e(TAG, "Discover scale，mac = ${device?.mac} ")
                    if(mIsConnected){
                        return
                    }
                    QNPlugin.getInstance(this@QNScalePairNetActivity).stopScan()
                    device.let {
                        val op = QNScaleOperate()
                        val curWeightUnit = DemoDataBase.getInstance(this@QNScalePairNetActivity)
                            .unitSettingDao().getUnitSetting().weightUnit
                        op.unit = when (curWeightUnit) {
                            DemoUnit.KG.showName -> QNWeightUnit.UNIT_KG
                            DemoUnit.LB.showName -> QNWeightUnit.UNIT_LB
                            DemoUnit.ST_LB.showName -> QNWeightUnit.UNIT_ST_LB
                            DemoUnit.ST.showName -> QNWeightUnit.UNIT_ST
                            DemoUnit.JIN.showName -> QNWeightUnit.UNIT_JIN
                            else -> QNWeightUnit.UNIT_KG
                        }

                        Log.e(TAG, "Connect scale")
                        mIsConnecting = true
                        QNScalePlugin.connectDevice(device, op)
                    }
                }

                override fun onSetUnitResult(code: Int, device: QNScaleDevice?) {
                    Log.e(TAG, "Set up scale unit success!")
                }

            })

            QNScalePlugin.setStatusListener(object : QNScaleStatusListener {
                override fun onConnectedSuccess(device: QNScaleDevice?) {
                    Log.e(TAG, "Connect scale success!")
                    mIsConnecting = false
                    mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.CONNECT
                    mIsConnected = true
                }

                override fun onConnectFail(code: Int, device: QNScaleDevice?) {
                    Log.e(TAG, "Connect scale failed!")
                    mIsConnecting = false
                    mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.DISCONNECT
                    mIsConnected = false
                    mHandler.removeMessages(0)
                }

                override fun onReadyInteractResult(device: QNScaleDevice?) {
                    Log.e(TAG, "Device is ready interact!")
                    mDevice = device
                    mViewModel.mac.value = mDevice?.mac ?: ""

                }

                override fun onDisconnected(device: QNScaleDevice?) {
                    Log.e(TAG, "Device is disconnected!")
                    mIsConnected = false
                    mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.DISCONNECT
                    mHandler.removeMessages(0)
                }
            })

            QNScaleWiFiMp.setWiFiStatusListener(object : QNScaleWiFiListener {
                override fun onStartWiFiConnect(device: QNScaleDevice?) {
                    Log.e(TAG, "Start pair net!")
                    mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.PAIR_NET_ING
                    mProgress.value = 0F
                    mHandler.removeMessages(0)
                    mHandler.sendEmptyMessageDelayed(0, 1000)
                }

                override fun onConnectWiFiStatus(code: Int, device: QNScaleDevice?) {
                    Log.e(TAG, if (code == 0) "Pair net success!" else "Pair net failed，status code return： code = $code")
                    if (code == 0) {
                        mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.PAIR_NET_SUCCESS
                        Toast.makeText(this@QNScalePairNetActivity, "Pair net success！", Toast.LENGTH_SHORT).show()
                        val dao = DemoDataBase.getInstance(this@QNScalePairNetActivity).wifiInfoDao()
                        val wifiInfo = dao.getWifiInfo().apply {
                            this.ssid = mSsid
                            this.password = mPwd
                            this.serverUrl = mServerUrl
                        }
                        dao.update(wifiInfo)
                    } else {
                        mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.PAIR_NET_FAIL
                        Toast.makeText(this@QNScalePairNetActivity, "Pair net failed！", Toast.LENGTH_SHORT).show()
                    }
                    mProgress.value = 1F
                    mHandler.removeMessages(0)
                }

            })
        }
    }

    private fun pairNet(){
        if (!TextUtils.isEmpty(specifiedMac)){
            if (null == mBPMachine){
                Toast.makeText(this@QNScalePairNetActivity, "Device is not connected!", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Device is not connected!")
                return
            }else{
                val code = QNBPMachineWiFiMp.startConnectWiFi(mBPMachine,QNBPMachineWiFi().apply {
                    ssid = mSsid
                    pwd = mPwd
                    serverUrl = "https://wsp-lite.yolanda.hk:443/yolanda/bps?code="
                })
                if (code != 0){
                    Log.e(TAG, "Error code $code")
                }else{
                    Log.e(TAG, "Go pair net")
                    Toast.makeText(this@QNScalePairNetActivity, "Pair net ing...", Toast.LENGTH_LONG).show()
                }
            }
        }else{
            if(mDevice == null){
                Toast.makeText(this@QNScalePairNetActivity, "Device is not connected!", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Device is not connected!")
                return
            }
            if (mDevice?.supportWiFi == true) {
                /** 配置wifi **/
                val wifiInfo = DemoDataBase.getInstance(this@QNScalePairNetActivity)
                    .wifiInfoDao().getWifiInfo()
                val qnWiFiInfo = QNWiFiInfo()
                qnWiFiInfo.ssid = wifiInfo.ssid
                qnWiFiInfo.pwd = wifiInfo.password
                qnWiFiInfo.serverUrl = wifiInfo.serverUrl
                Log.e(TAG, "Pair net，wifi = $qnWiFiInfo")
                Toast.makeText(this@QNScalePairNetActivity, "Pair net ing...", Toast.LENGTH_LONG).show()
                QNScaleWiFiMp.startConnectWiFi(mDevice, qnWiFiInfo)
            } else {
                Toast.makeText(this@QNScalePairNetActivity, "The device is not support pair net!", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "The device is not support pair net!")
            }
        }
    }
}

@Composable
fun QNScalePairNetStatusBar() {
    val ctx = LocalContext.current
    val hsvm: QNScalePairNetViewModel = viewModel()
    val status = if (!DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Bluetooth turn off"
    } else if (!DemoBleUtils.isRunOnAndroid12Mode(ctx) && !DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Location turn off"
    } else if (!DemoBleUtils.hasBlePermission(ctx)) {
        "Need bluetooth permission"
    } else {
        when (hsvm.pairNetState.value) {
            QNScalePairNetViewModel.PairNetState.CONNECT -> "Connected"
            QNScalePairNetViewModel.PairNetState.DISCONNECT -> "Disconnected"
            QNScalePairNetViewModel.PairNetState.PAIR_NET_ING -> "Pair net ing"
            QNScalePairNetViewModel.PairNetState.PAIR_NET_SUCCESS -> "Pair net success"
            QNScalePairNetViewModel.PairNetState.PAIR_NET_FAIL -> "Pair net fail"
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

class QNScalePairNetViewModel : ViewModel() {

    enum class PairNetState {
        CONNECT, DISCONNECT, PAIR_NET_ING, PAIR_NET_SUCCESS, PAIR_NET_FAIL
    }
    var pairNetState: MutableState<PairNetState>
            = mutableStateOf(QNScalePairNetViewModel.PairNetState.DISCONNECT)
    var mac: MutableState<String> = mutableStateOf("")
}
