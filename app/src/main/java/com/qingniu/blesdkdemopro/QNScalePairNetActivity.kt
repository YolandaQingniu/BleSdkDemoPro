package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.model.QNWeightUnit
import com.qingniu.qnscaleplugin.QNScalePlugin
import com.qingniu.qnscaleplugin.QNScaleWiFiMp
import com.qingniu.qnscaleplugin.listener.*
import com.qingniu.qnscaleplugin.model.*

class QNScalePairNetActivity : ComponentActivity() {
    // Is connect deivce
    private var mIsConnecting = false

    companion object {
        const val TAG = "QNScalePairNet"
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, QNScalePairNetActivity::class.java)
        }
    }

    lateinit var mViewModel: QNScalePairNetViewModel

    val mHandler = Handler(Looper.getMainLooper())

    var mDevice: QNScaleDevice? = null

    var mSsid = ""
    var mPwd = ""
    var mServerUrl = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        mutableStateOf(dao.getWifiInfo().serverUrl)
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
                                    modifier = Modifier.padding(start = 10.dp, bottom = 10.dp).fillMaxWidth()
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
                                    modifier = Modifier.padding(start = 10.dp, bottom = 10.dp).fillMaxWidth()
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
                            Column(Modifier.padding(top = 20.dp)) {
                                Text(
                                    text = "serverUrl", fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 10.dp, bottom = 10.dp).fillMaxWidth()
                                )
                                TextField(value = serverUrl.value.toString(), onValueChange = {
                                    if (!TextUtils.isEmpty(it)) {
                                        serverUrl.value = it

                                    } else {
                                        serverUrl.value = ""
                                    }
                                    mServerUrl = serverUrl.value
                                },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                                )
                            }
                            Column(
                                Modifier.padding(top = 40.dp, bottom = 80.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Button(
                                    onClick = {pairNet()},
                                    Modifier.width(200.dp).height(56.dp)
                                ) {
                                    Text(text = "Pair Net", fontSize = 18.sp)
                                }
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
        QNPlugin.getInstance(this).startScan()
        QNScalePlugin.setScalePlugin(QNPlugin.getInstance(this))
        QNScalePlugin.setDeviceListener(object : QNScaleDeviceListener {
            override fun onDiscoverScaleDevice(device: QNScaleDevice?) {
                Log.e(TAG, "Discover scale，mac = ${device?.mac} ")
                if (mIsConnecting || (
                            device?.mac != "5C:D6:1F:EB:68:50"
                                    && device?.mac != "F0:FE:6B:CB:8A:C8"
                                    && device?.mac != "FF:01:00:00:18:08"
                                    && device?.mac != "ED:67:37:11:B3:AC"
                                    && device?.mac != "ED:67:37:27:F0:4D"
                                    && device?.mac != "A1:7C:08:A6:A8:5F"
                                    && device?.mac != "F0:08:D1:B2:F3:CA"
                                    && device?.mac != "C4:5B:BE:B8:D0:1A"
                                    && device?.mac != "C4:DD:57:EC:2F:9A"
                                    && device?.mac != "C7:C7:63:DF:FF:78")
                ) {
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
            }

            override fun onConnectFail(code: Int, device: QNScaleDevice?) {
                Log.e(TAG, "Connect scale failed!")
                mIsConnecting = false
                mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.DISCONNECT
            }

            override fun onReadyInteractResult(device: QNScaleDevice?) {
                Log.e(TAG, "Device is ready interact!")
                mDevice = device
                mViewModel.mac.value = mDevice?.mac ?: ""

            }
        })

        QNScaleWiFiMp.setWiFiStatusListener(object : QNScaleWiFiListener {
            override fun onStartWiFiConnect(device: QNScaleDevice?) {
                Log.e(TAG, "Start pair net!")
                mViewModel.pairNetState.value = QNScalePairNetViewModel.PairNetState.PAIR_NET_ING
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

            }

        })
    }

    private fun pairNet(){
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
