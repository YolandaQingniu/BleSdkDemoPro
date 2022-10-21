package com.qingniu.blesdkdemopro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.qingniu.blesdkdemopro.constant.DemoUnit
import com.qingniu.blesdkdemopro.constant.UserConstant
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.blesdkdemopro.util.SpUtils
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.model.QNGender
import com.qingniu.qnplugin.model.QNWeightUnit
import com.qingniu.qnscaleplugin.QNScalePlugin
import com.qingniu.qnscaleplugin.QNScaleWiFiMp
import com.qingniu.qnscaleplugin.QNUserScaleMp
import com.qingniu.qnscaleplugin.listener.*
import com.qingniu.qnscaleplugin.model.*

class QNScaleMeasureActivity : ComponentActivity() {
    // 是否正在连接设备
    private var mIsConecting = false

    companion object {
        const val TAG = "QNScale"
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, QNScaleMeasureActivity::class.java)
        }
    }

    lateinit var mViewModel: QNScaleViewModel

    val mHandler = Handler(Looper.getMainLooper())

    var mDevice: QNScaleDevice? = null

    private val mReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, i: Intent?) {
            when(i?.action){
                UserConstant.ACTION_DELETE_ALL_USERS -> {
                    // 删除全部用户
                    if(mDevice != null){
                        Log.e(TAG, "删除全部用户")
                        if(mDevice != null) SpUtils.cleanUserData(mDevice!!.mac, this@QNScaleMeasureActivity)
                        QNUserScaleMp.deleteUserList(mDevice, listOf(1, 2, 3, 4, 5, 6, 7, 8))
                    }
                }
                UserConstant.ACTION_DELETE_INDEX_USER -> {
                    // 删除指定坑位的用户
                    val index = i.getIntExtra(UserConstant.DELETE_USER_INDEX, 0)
                    if(mDevice != null && index > 0){
                        Log.e(TAG, "删除指定坑位的用户, index = $index")
                        QNUserScaleMp.deleteUserList(mDevice, listOf(index))
                    }
                }
            }
        }

    }

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
                        TitleBar("QNScale", true)
                        QNScaleStatusBar()
                        QNScaleMeasureBoard()
                    }
                }
            }
        }
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        QNPlugin.getInstance(this).stopScan()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
        mDevice?.let {
            QNScalePlugin.cancelConnectDevice(it)
        }
    }

    private fun init() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(UserConstant.ACTION_DELETE_ALL_USERS)
        intentFilter.addAction(UserConstant.ACTION_DELETE_INDEX_USER)
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter)
        QNPlugin.getInstance(this).startScan()
        QNScalePlugin.setScalePlugin(QNPlugin.getInstance(this))
        QNScalePlugin.setDeviceListener(object : QNScaleDeviceListener {
            override fun onDiscoverScaleDevice(device: QNScaleDevice?) {
                Log.e(TAG, "发现设备，mac = ${device?.mac} ")
                if(mIsConecting ||
                    (device?.mac != "5C:D6:1F:EB:68:50"
                            && device?.mac != "F0:FE:6B:CB:8A:C8"
                            && device?.mac != "FF:01:00:00:18:08"
                            && device?.mac != "ED:67:37:11:B3:AC"
                            && device?.mac != "ED:67:37:27:F0:4D"
                            && device?.mac != "A1:7C:08:A6:A8:5F"
                            && device?.mac != "F0:08:D1:B2:F3:CA"
                            && device?.mac != "C4:DD:57:EC:2F:9A"
                            && device?.mac != "C7:C7:63:DF:FF:78")
                ){
                    return
                }
                QNPlugin.getInstance(this@QNScaleMeasureActivity).stopScan()
                device.let {
                    val op = QNScaleOperate()
                    val curWeightUnit = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
                        .unitSettingDao().getUnitSetting().weightUnit
                    op.unit = when(curWeightUnit) {
                        DemoUnit.KG.showName -> QNWeightUnit.UNIT_KG
                        DemoUnit.LB.showName -> QNWeightUnit.UNIT_LB
                        DemoUnit.ST_LB.showName -> QNWeightUnit.UNIT_ST_LB
                        DemoUnit.ST.showName -> QNWeightUnit.UNIT_ST
                        DemoUnit.JIN.showName -> QNWeightUnit.UNIT_JIN
                        else -> QNWeightUnit.UNIT_KG
                    }

                    Log.e(TAG, "连接设备")
                    mIsConecting = true
                    QNScalePlugin.connectDevice(device, op)
                }
            }

            override fun onSetUnitResult(code: Int, device: QNScaleDevice?) {
                Log.e(TAG, "设置设备单位成功")
            }

        })

        QNScalePlugin.setStatusListener(object : QNScaleStatusListener {
            override fun onConnectedSuccess(device: QNScaleDevice?) {
                Log.e(TAG, "设备连接成功")
                mIsConecting = false
                mViewModel.vState.value = QNScaleViewModel.MeasureState.CONNECT
            }

            override fun onConnectFail(code: Int, device: QNScaleDevice?) {
                Log.e(TAG, "设备连接失败")
                mIsConecting = false
                mViewModel.vState.value = QNScaleViewModel.MeasureState.DISCONNECT
            }

            override fun onReadyInteractResult(device: QNScaleDevice?) {
                Log.e(TAG, "设备允许交互")
                mDevice = device
                mViewModel.mac.value = mDevice?.mac ?: ""

                /** 设置测量用户 start **/
                val user = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
                    .userDao().getUser()
                val qnUser = QNUser.build(
                    "user123456789",
                    if(user.gender == "1") QNGender.MALE else QNGender.FEMALE,
                    user.age,
                    180,
                    false
                )
                QNScalePlugin.setMeasureUser(mDevice, qnUser)

                val index = SpUtils.getIntValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_INDEX_KEY)
                val key = SpUtils.getIntValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_SECRET_KEY)
                val isVisitorMode = SpUtils.getBooleanValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_IS_VISITOR_KEY)
                val qnScaleUser = QNScaleUser.build(
                    qnUser,
                    index,
                    if(key <= 0) 10086 else key,
                    isVisitorMode
                )
                Log.e(TAG, "如果设备支持设置测量用户则去设置，user = $qnScaleUser")
                QNUserScaleMp.setMeasureUserToUserDevice(mDevice, qnScaleUser)
                /** 设置测量用户 end **/

                /** 配置wifi start **/
                val wifiInfo = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
                    .wifiInfoDao().getWifiInfo()
                val qnWiFiInfo = QNWiFiInfo()
                qnWiFiInfo.ssid = wifiInfo.ssid
                qnWiFiInfo.pwd = wifiInfo.password
                qnWiFiInfo.serverUrl = wifiInfo.serverUrl
                Log.e(TAG, "如果设备支持配网则去配网，wifi = $qnWiFiInfo")
                QNScaleWiFiMp.startConnectWiFi(mDevice, qnWiFiInfo)
                /** 配置wifi end **/
            }

        })

        QNUserScaleMp.setUserScaleEventListener(object : QNScaleUserEventListener {
            override fun onRegisterUserResult(
                code: Int,
                user: QNScaleUser?,
                device: QNScaleDevice?
            ) {
                if(code == 0){
                    Log.e(TAG, "注册用户成功   user = $user")
                    // 保存坑位和key到sp
                    if (user != null) {
                        SpUtils.saveValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_SECRET_KEY, user.key)
                        SpUtils.saveValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_INDEX_KEY, user.index)
                        SpUtils.saveValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_IS_VISITOR_KEY, user.isVisitorMode)
                    }

                }else {
                    Log.e(TAG, "注册用户未成功， code = $code")
                }
            }

            override fun onSyncUserInfoResult(
                code: Int,
                user: QNScaleUser?,
                device: QNScaleDevice?
            ) {
                Log.e(TAG, "同步用户结果，code = $code   user = $user")
            }

            override fun onDeleteUsersResult(code: Int, device: QNScaleDevice?) {
                val msg = if(code == 0) "删除用户成功" else "删除用户失败，code = $code"
                Log.e(TAG, msg)
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }

        })

        QNScaleWiFiMp.setWiFiStatusListener(object : QNScaleWiFiListener{
            override fun onStartWiFiConnect(device: QNScaleDevice?) {
                Log.e(TAG, "开始配网")
            }

            override fun onConnectWiFiStatus(code: Int, device: QNScaleDevice?) {
                Log.e(TAG, if(code == 0) "配网成功" else "配网状态返回： code = $code")
            }

        })

        QNScalePlugin.setDataListener(object : QNScaleDataListener {
            override fun onRealTimeWeight(weight: String?, device: QNScaleDevice?) {
                Log.e(TAG, "实时测量： weight = $weight")
                mViewModel.vState.value = QNScaleViewModel.MeasureState.MEASURE_ING
                mViewModel.weightStr.value = createQNScaleWeightStr(weight?.toDouble()?: 0.0, QNWeightUnit.UNIT_KG)
            }

            override fun onReceiveMeasureResult(scaleData: QNScaleData?, device: QNScaleDevice?) {
                Log.e(TAG, "测量结果： data = $scaleData")
                mViewModel.vState.value = QNScaleViewModel.MeasureState.MEASURE_END
                mViewModel.weightStr.value = createQNScaleWeightStr(if(scaleData?.weight == null) 0.0 else (scaleData?.weight!!.toDouble()), QNWeightUnit.UNIT_KG)
            }

            override fun onReceiveStoredData(
                storedDataList: MutableList<QNScaleStoredData>?,
                device: QNScaleDevice?
            ) {
                storedDataList?.forEach {
                    Log.e(TAG, "历史测量数据： data = $it")
                }
            }

            override fun onGetLastDataHmac(user: QNScaleUser?, device: QNScaleDevice?) {
                Log.e(TAG, "onGetLastDataHmac： user = $user")
            }

        })
    }

}

fun createQNScaleWeightStr(weight: Double, unit: QNWeightUnit): String{
    when(unit){
        QNWeightUnit.UNIT_KG -> {
            return "$weight KG"
        }
        QNWeightUnit.UNIT_LB -> {
            return "$weight LB"
        }
        QNWeightUnit.UNIT_JIN -> {
            return "$weight 斤"
        }
        QNWeightUnit.UNIT_ST -> {
            return "$weight ST"
        }
        QNWeightUnit.UNIT_ST_LB -> {
            return "$weight ST_LB"
        }
        else ->{
            return "$weight KG"
        }
    }
}

@Composable
fun QNScaleStatusBar() {
    val ctx = LocalContext.current
    val hsvm: QNScaleViewModel = viewModel()
    val status = if (!DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Bluetooth turn off"
    } else if (!DemoBleUtils.isRunOnAndroid12Mode(ctx) && !DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Location turn off"
    } else if (!DemoBleUtils.hasBlePermission(ctx)) {
        "Need bluetooth permission"
    } else {
        when (hsvm.vState.value) {
            QNScaleViewModel.MeasureState.CONNECT -> "Connected"
            QNScaleViewModel.MeasureState.DISCONNECT -> "Disconnected"
            QNScaleViewModel.MeasureState.MEASURE_ING -> "Measure ing"
            QNScaleViewModel.MeasureState.MEASURE_END -> "Measure end"
            QNScaleViewModel.MeasureState.MEASURE_FAIL -> "Measure fail"
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
fun QNScaleMeasureBoard() {
    val ctx = LocalContext.current
    val hsvm: QNScaleViewModel = viewModel()
    Column(
        Modifier
            .padding(top = 100.dp)
            .fillMaxSize()
    ) {
        val cMac = hsvm.mac.value
        val cWeightStr = hsvm.weightStr.value
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
            text = if (cvState == QNScaleViewModel.MeasureState.MEASURE_ING ||
                cvState == QNScaleViewModel.MeasureState.MEASURE_END ||
                cvState == QNScaleViewModel.MeasureState.MEASURE_FAIL
            ) {
                cWeightStr
            } else {
                ""
            },

            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp, bottom = 10.dp)
        )
        if (cvState == QNScaleViewModel.MeasureState.MEASURE_END) {
            Column {
                QNScaleIndicator("weight", cWeightStr, false)
            }
        }
    }
}

@Composable
fun QNScaleIndicator(
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

class QNScaleViewModel : ViewModel() {

    enum class MeasureState {
        CONNECT, DISCONNECT, MEASURE_ING, MEASURE_END, MEASURE_FAIL
    }

    var mac: MutableState<String> = mutableStateOf("")
    var weightStr: MutableState<String> = mutableStateOf("--")
    var vState: MutableState<MeasureState> = mutableStateOf(MeasureState.DISCONNECT)
}