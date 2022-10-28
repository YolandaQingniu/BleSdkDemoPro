package com.qingniu.blesdkdemopro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.foundation.lazy.LazyColumn
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
import com.qingniu.blesdkdemopro.db.table.DeviceUser
import com.qingniu.blesdkdemopro.db.table.User
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.blesdkdemopro.util.SpUtils
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.model.QNGender
import com.qingniu.qnplugin.model.QNLengthUnit
import com.qingniu.qnplugin.model.QNWeightUnit
import com.qingniu.qnscaleplugin.QNScalePlugin
import com.qingniu.qnscaleplugin.QNScaleWiFiMp
import com.qingniu.qnscaleplugin.QNUserScaleMp
import com.qingniu.qnscaleplugin.listener.*
import com.qingniu.qnscaleplugin.model.*
import java.text.DecimalFormat

class QNScaleMeasureActivity : ComponentActivity() {
    // 是否正在连接设备
    private var mIsConnecting = false
    // 是否已经连接设备
    private var mIsConnected = false

    private var mWillDeleteDeviceUSer: DeviceUser? = null

    private val mDeviceUserDao by lazy {
        DemoDataBase.getInstance(this@QNScaleMeasureActivity).deviceUserDao()
    }

    companion object {
        const val TAG = "QNScale"
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, QNScaleMeasureActivity::class.java)
        }
    }

    val mCurWeightUnit by lazy {
        val weightUnit = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
            .unitSettingDao().getUnitSetting().weightUnit
        when(weightUnit) {
            DemoUnit.KG.showName -> QNWeightUnit.UNIT_KG
            DemoUnit.LB.showName -> QNWeightUnit.UNIT_LB
            DemoUnit.ST_LB.showName -> QNWeightUnit.UNIT_ST_LB
            DemoUnit.ST.showName -> QNWeightUnit.UNIT_ST
            DemoUnit.JIN.showName -> QNWeightUnit.UNIT_JIN
            else -> QNWeightUnit.UNIT_KG
        }
    }

    val mCurLengthUnit by lazy {
        val lengthUnit = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
            .unitSettingDao().getUnitSetting().lengthUnit
        when(lengthUnit) {
            DemoUnit.CM.showName -> QNLengthUnit.UNIT_CM
            DemoUnit.IN.showName -> QNLengthUnit.UNIT_IN
            DemoUnit.FT_IN.showName -> QNLengthUnit.UNIT_FT_IN
            else -> QNLengthUnit.UNIT_CM
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
                        if(mDevice != null) mDeviceUserDao.getAllDeviceUser().filter { it.mac == mDevice?.mac }.forEach { mDeviceUserDao.delete(it) }
                        QNUserScaleMp.deleteUserList(mDevice, listOf(1, 2, 3, 4, 5, 6, 7, 8))
                    }
                }
                UserConstant.ACTION_DELETE_INDEX_USER -> {
                    // 删除指定坑位的用户
                    val mWillDeleteDeviceUSer = i.getParcelableExtra<DeviceUser>(UserConstant.DELETE_USER)
                    if(mDevice != null && mWillDeleteDeviceUSer != null && mWillDeleteDeviceUSer.index > 0){
                        Log.e(TAG, "删除指定坑位的用户, index = ${mWillDeleteDeviceUSer.index}")
                        QNUserScaleMp.deleteUserList(mDevice, listOf(mWillDeleteDeviceUSer.index))
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
//                if(mIsConecting || (
//                            device?.mac != "5C:D6:1F:EB:68:50"
//                            && device?.mac != "F0:FE:6B:CB:8A:C8"
//                            && device?.mac != "FF:01:00:00:18:08"
//                            && device?.mac != "ED:67:37:11:B3:AC"
//                            && device?.mac != "ED:67:37:27:F0:4D"
//                            && device?.mac != "A1:7C:08:A6:A8:5F"
//                            && device?.mac != "F0:08:D1:B2:F3:CA"
//                            && device?.mac != "C4:5B:BE:B8:D0:1A"
//                            && device?.mac != "C4:DD:57:EC:2F:9A"
//                            && device?.mac != "C7:C7:63:DF:FF:78")
//                ){
//                    return
//                }
                if(mIsConnected) return
                QNPlugin.getInstance(this@QNScaleMeasureActivity).stopScan()
                device.let {
                    val op = QNScaleOperate()
                    op.unit = mCurWeightUnit
                    Log.e(TAG, "连接设备")
                    mIsConnecting = true
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
                mIsConnecting = false
                mIsConnected = true
                mViewModel.vState.value = QNScaleViewModel.MeasureState.CONNECT
            }

            override fun onConnectFail(code: Int, device: QNScaleDevice?) {
                Log.e(TAG, "设备连接失败")
                mIsConnecting = false
                mIsConnected = false
                mViewModel.vState.value = QNScaleViewModel.MeasureState.DISCONNECT
            }

            override fun onReadyInteractResult(device: QNScaleDevice?) {
                Log.e(TAG, "设备允许交互")
                mDevice = device
                mViewModel.mac.value = mDevice?.mac ?: ""

                // 设置用户信息
                val user = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
                    .userDao().getUser()
                val deviceUsers = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
                    .deviceUserDao().getDeviceUser(user?.userId?: "")
                var deviceUser: DeviceUser? = null
                if(deviceUsers != null){
                    deviceUsers.forEach {
                        if(it.mac == mDevice?.mac){
                            deviceUser = it
                        }
                    }
                }
                val userId = user.userId
                val gender = if(user.gender == "MALE") QNGender.MALE else QNGender.FEMALE
                val age = user.age
                val height = user.height

                if(mDevice?.supportScaleUser == true){
                    /** 设置测量用户 start **/
                    val index = deviceUser?.index ?: 0
                    val key =  deviceUser?.key ?: 1000
                    val isVisitorMode = deviceUser?.isVisitorMode == true
                    val qnScaleUser = QNScaleUser.build(
                        userId,
                        gender,
                        age,
                        height,
                        false,
                        index,
                        key,
                        isVisitorMode
                    )
                    Log.e(TAG, "设置测量用户，user = $qnScaleUser")
                    QNUserScaleMp.setMeasureUserToUserDevice(mDevice, qnScaleUser)
                } else {
                    Log.e(TAG, "设备不支持设置测量用户")
                    val qnUser = QNUser.build(
                        userId,
                        gender,
                        age,
                        height,
                        false
                    )
                    QNScalePlugin.setMeasureUser(mDevice, qnUser)
                }
            }

            override fun onDisconnected(device: QNScaleDevice?) {
                Log.e(TAG, "设备已断开")
                mIsConnected = false
                mViewModel.vState.value = QNScaleViewModel.MeasureState.DISCONNECT
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
                    insertOrUpdateDeviceUser(user, device)
                }else {
                    Log.e(TAG, "注册用户未成功， code = $code")
                    if(device?.scaleUserFull == true){
                        Log.e(TAG, "注册用户未成功， 秤端用户数已满")
                    }
                    mViewModel.vState.value = QNScaleViewModel.MeasureState.USER_REGISTER_OR_VISIT_FAIL
                }
            }

            override fun onSyncUserInfoResult(
                code: Int,
                user: QNScaleUser?,
                device: QNScaleDevice?
            ) {
                Log.e(TAG, "同步用户结果，code = $code   user = $user")
                if(code == 0){
                    insertOrUpdateDeviceUser(user, device)
                }else {
                    mViewModel.vState.value = QNScaleViewModel.MeasureState.USER_REGISTER_OR_VISIT_FAIL
                }
            }

            override fun onDeleteUsersResult(code: Int, device: QNScaleDevice?) {
                val msg = if(code == 0) "删除用户成功" else "删除用户失败，code = $code"
                Log.e(TAG, msg)
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                if(code == 0){
                    val dao = DemoDataBase.getInstance(this@QNScaleMeasureActivity).deviceUserDao()
                    if(mWillDeleteDeviceUSer != null) dao.delete(mWillDeleteDeviceUSer!!)
                }
                mWillDeleteDeviceUSer = null
                LocalBroadcastManager.getInstance(this@QNScaleMeasureActivity).sendBroadcast(Intent("update_bind_devices"))
            }

        })

        QNScaleWiFiMp.setWiFiStatusListener(object : QNScaleWiFiListener{
            override fun onStartWiFiConnect(device: QNScaleDevice?) {
                Log.e(TAG, "开始配网")
            }

            override fun onConnectWiFiStatus(code: Int, device: QNScaleDevice?) {
                Log.e(TAG, if(code == 0) "配网成功" else "配网失败，状态返回： code = $code")
            }

        })

        QNScalePlugin.setDataListener(object : QNScaleDataListener {
            override fun onRealTimeWeight(weight: String?, device: QNScaleDevice?) {
                Log.e(TAG, "实时测量： weight = $weight")
                mViewModel.vState.value = QNScaleViewModel.MeasureState.MEASURE_ING
                mViewModel.weight.value = createQNScaleWeightStr(weight?.toDouble()?: 0.0, mCurWeightUnit)
            }

            override fun onReceiveMeasureResult(scaleData: QNScaleData?, device: QNScaleDevice?) {
                Log.e(TAG, "测量结果： data = $scaleData")
                mViewModel.vState.value = QNScaleViewModel.MeasureState.MEASURE_END
                mViewModel.height.value = createQNScaleHeightStr(if(scaleData?.height == null) 0 else (scaleData?.height!!.toInt()), mCurLengthUnit)
                mViewModel.weight.value = createQNScaleWeightStr(if(scaleData?.weight == null) 0.0 else (scaleData?.weight!!.toDouble()), mCurWeightUnit)
                mViewModel.mac.value = device?.mac ?: ""
                mViewModel.timestamp.value = scaleData?.timeStamp ?: ""
                mViewModel.bmi.value = scaleData?.bmi ?: ""
                mViewModel.bodyFatRate.value = scaleData?.bodyFatRate ?: ""
                mViewModel.subcutaneousFatRate.value = scaleData?.subcutaneousFatRate ?: ""
                mViewModel.visceralFatLevel.value = scaleData?.visceralFatLevel ?: ""
                mViewModel.bodyWaterRate.value = scaleData?.bodyWaterRate ?: ""
                mViewModel.skeletalMuscleRate.value = scaleData?.skeletalMuscleRate ?: ""
                mViewModel.boneMass.value = scaleData?.boneMass ?: ""
                mViewModel.BMR.value = scaleData?.bmr ?: ""
                mViewModel.bodyType.value = scaleData?.bodyType ?: ""
                mViewModel.proteinRate.value = scaleData?.proteinRate ?: ""
                mViewModel.leanBodyMass.value = scaleData?.leanBodyMass ?: ""
                mViewModel.muscleMass.value = scaleData?.muscleMass ?: ""
                mViewModel.bodyAge.value = scaleData?.bodyAge ?: ""
                mViewModel.healthScore.value = scaleData?.healthScore ?: ""
                mViewModel.heartRate.value = scaleData?.heartRate ?: ""
                mViewModel.heartIndex.value = scaleData?.heartIndex ?: ""
                mViewModel.fattyLiverRiskLevel.value = scaleData?.fattyLiverRiskLevel ?: ""
                mViewModel.res50KHZ.value = scaleData?.res50KHZ ?: ""
                mViewModel.res500KHZ.value = scaleData?.res500KHZ ?: ""
                mViewModel.bodyFatMass.value = scaleData?.bodyFatMass ?: ""
                mViewModel.obesity.value = scaleData?.obesity ?: ""
                mViewModel.bodyWaterMass.value = scaleData?.bodyWaterMass ?: ""
                mViewModel.proteinMass.value = scaleData?.proteinMass ?: ""
                mViewModel.mineralLevel.value = scaleData?.mineralLevel ?: ""
                mViewModel.dreamWeight.value = scaleData?.dreamWeight ?: ""
                mViewModel.standWeight.value = scaleData?.standWeight ?: ""
                mViewModel.weightControl.value = scaleData?.weightControl ?: ""
                mViewModel.bodyFatControl.value = scaleData?.bodyFatControl ?: ""
                mViewModel.muscleMassControl.value = scaleData?.muscleMassControl ?: ""
                mViewModel.muscleRate.value = scaleData?.muscleRate ?: ""
                mViewModel.leftArmBodyfatRate.value = scaleData?.leftArmBodyfatRate ?: ""
                mViewModel.leftLegBodyfatRate.value = scaleData?.leftLegBodyfatRate ?: ""
                mViewModel.rightArmBodyfatRate.value = scaleData?.rightArmBodyfatRate ?: ""
                mViewModel.rightLegBodyfatRate.value = scaleData?.rightLegBodyfatRate ?: ""
                mViewModel.trunkBodyfatRate.value = scaleData?.trunkBodyfatRate ?: ""
                mViewModel.leftArmFatMass.value = scaleData?.leftArmFatMass ?: ""
                mViewModel.leftLegFatMass.value = scaleData?.leftLegFatMass ?: ""
                mViewModel.rightArmFatMass.value = scaleData?.rightArmFatMass ?: ""
                mViewModel.rightLegFatMass.value = scaleData?.rightLegFatMass ?: ""
                mViewModel.trunkFatMass.value = scaleData?.trunkFatMass ?: ""
                mViewModel.leftArmMuscleMass.value = scaleData?.leftArmMuscleMass ?: ""
                mViewModel.leftLegMuscleMass.value = scaleData?.leftLegMuscleMass ?: ""
                mViewModel.rightArmMuscleMass.value = scaleData?.rightArmMuscleMass ?: ""
                mViewModel.rightLegMuscleMass.value = scaleData?.rightLegMuscleMass ?: ""
                mViewModel.trunkMuscleMass.value = scaleData?.trunkMuscleMass ?: ""
                mViewModel.skeletalMuscleMass.value = scaleData?.skeletalMuscleMass ?: ""
                mViewModel.mineralSaltRate.value = scaleData?.mineralSaltRate ?: ""
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

    private fun insertOrUpdateDeviceUser(user: QNScaleUser?, device: QNScaleDevice?){
        if (user != null) {
            // 设置用户信息
            val dao = DemoDataBase.getInstance(this@QNScaleMeasureActivity).deviceUserDao()
            val deviceUsers = dao.getDeviceUser(user.userid)
            if(deviceUsers != null && deviceUsers.isNotEmpty()){
                val filterUsers = deviceUsers.filter { it.mac == device?.mac }
                if(filterUsers != null && filterUsers.isNotEmpty()){
                    val updateUser = filterUsers.get(0)
                    updateUser.index = user.index
                    updateUser.isVisitorMode = user.isVisitorMode
                    updateUser.isSupportUser = device?.supportScaleUser == true
                    updateUser.isSupportWifi = device?.supportWiFi == true
                    Log.e(TAG, "update device user, $updateUser")
                    dao.update(updateUser)
                    return
                }
            }
            device?.let {
                var temp = 0L
                deviceUsers.forEach {
                    if(it.id >= temp){
                        temp = it.id + 1
                    }
                }
                val insertUser = DeviceUser()
                insertUser.id = temp
                insertUser.index = user.index
                insertUser.mac = device!!.mac
                insertUser.key = user.key
                insertUser.userId = user.userid
                insertUser.isVisitorMode = user.isVisitorMode
                insertUser.isSupportUser = it.supportScaleUser
                insertUser.isSupportWifi = it.supportWiFi
                Log.e(TAG, "insert device user, $insertUser")
                dao.insert(insertUser)
            }
        }
    }

}

fun createQNScaleWeightStr(weight: Double, unit: QNWeightUnit): String{
    val format = DecimalFormat("0.0")
    when(unit){
        QNWeightUnit.UNIT_KG -> {
            return "${format.format(weight)} KG"
        }
        QNWeightUnit.UNIT_LB -> {
            return "${format.format(weight * 2.2046226)} LB"
        }
        QNWeightUnit.UNIT_JIN -> {
            return "${format.format(weight * 2)} 斤"
        }
        QNWeightUnit.UNIT_ST -> {
            return "${format.format(weight * 0.157473)} ST"
        }
        QNWeightUnit.UNIT_ST_LB -> {
            return "${format.format(weight * 0.157473)}:${format.format(weight * 2.2046226)} ST:LB"
        }
        else ->{
            return "${format.format(weight)} KG"
        }
    }
}

fun createQNScaleHeightStr(height: Int, unit: QNLengthUnit): String{
    val format = DecimalFormat("0.0")
    when(unit){
        QNLengthUnit.UNIT_CM -> {
            return "${format.format(height)} CM"
        }
        QNLengthUnit.UNIT_IN -> {
            return "${format.format(height * 0.3937008)} IN"
        }
        QNLengthUnit.UNIT_FT_IN -> {
            return "${format.format(height * 0.0328084)}:${format.format(height * 0.3937008)}  FT:IN"
        }
        else ->{
            return "$height CM"
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
            QNScaleViewModel.MeasureState.USER_REGISTER_OR_VISIT_FAIL -> "User register or visit fail"
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
        val cWeightStr = hsvm.weight.value
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
            LazyColumn(content = {
                item {
                    Column {
                        Indicator("weight", hsvm.weight.value, false)
                        Indicator("height", hsvm.height.value, false)
                        Indicator("bmi", hsvm.bmi.value, true)
                        if (!TextUtils.isEmpty(hsvm.timestamp.value)) {
                            QNScaleIndicator("timeStamp", hsvm.timestamp.value, false)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyFatRate.value)) {
                            Indicator("bodyFatRate", hsvm.bodyFatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.subcutaneousFatRate.value)) {
                            Indicator("subcutaneousFatRate", hsvm.subcutaneousFatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.visceralFatLevel.value)) {
                            Indicator("visceralFatLevel", hsvm.visceralFatLevel.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyWaterRate.value)) {
                            Indicator("bodyWaterRate", hsvm.bodyWaterRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.skeletalMuscleRate.value)) {
                            Indicator("skeletalMuscleRate", hsvm.skeletalMuscleRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.boneMass.value)) {
                            Indicator("boneMass", hsvm.boneMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.BMR.value)) {
                            Indicator("BMR", hsvm.BMR.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyType.value)) {
                            Indicator("bodyType", hsvm.bodyType.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.proteinRate.value)) {
                            Indicator("proteinRate", hsvm.proteinRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leanBodyMass.value)) {
                            Indicator("leanBodyMass", hsvm.leanBodyMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.muscleMass.value)) {
                            Indicator("muscleMass", hsvm.muscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyAge.value)) {
                            Indicator("bodyAge", hsvm.bodyAge.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.healthScore.value)) {
                            Indicator("healthScore", hsvm.healthScore.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.heartRate.value)) {
                            Indicator("heartRate", hsvm.heartRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.heartIndex.value)) {
                            Indicator("heartIndex", hsvm.heartIndex.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.fattyLiverRiskLevel.value)) {
                            Indicator("fattyLiverRiskLevel", hsvm.fattyLiverRiskLevel.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.res50KHZ.value)) {
                            Indicator("res50KHZ", hsvm.res50KHZ.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.res500KHZ.value)) {
                            Indicator("res500KHZ", hsvm.res500KHZ.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyFatMass.value)) {
                            Indicator("bodyFatMass", hsvm.bodyFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.obesity.value)) {
                            Indicator("obesity", hsvm.obesity.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyWaterMass.value)) {
                            Indicator("bodyWaterMass", hsvm.bodyWaterMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.proteinMass.value)) {
                            Indicator("proteinMass", hsvm.proteinMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.mineralLevel.value)) {
                            Indicator("mineralLevel", hsvm.mineralLevel.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.dreamWeight.value)) {
                            Indicator("dreamWeight", hsvm.dreamWeight.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.standWeight.value)) {
                            Indicator("standWeight", hsvm.standWeight.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.weightControl.value)) {
                            Indicator("weightControl", hsvm.weightControl.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyFatControl.value)) {
                            Indicator("bodyFatControl", hsvm.bodyFatControl.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.muscleMassControl.value)) {
                            Indicator("muscleMassControl", hsvm.muscleMassControl.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.muscleRate.value)) {
                            Indicator("muscleRate", hsvm.muscleRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftArmBodyfatRate.value)) {
                            Indicator("leftArmBodyfatRate", hsvm.leftArmBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftLegBodyfatRate.value)) {
                            Indicator("leftLegBodyfatRate", hsvm.leftLegBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightArmBodyfatRate.value)) {
                            Indicator("rightArmBodyfatRate", hsvm.rightArmBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightLegBodyfatRate.value)) {
                            Indicator("rightLegBodyfatRate", hsvm.rightLegBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.trunkBodyfatRate.value)) {
                            Indicator("trunkBodyfatRate", hsvm.trunkBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftArmFatMass.value)) {
                            Indicator("leftArmFatMass", hsvm.leftArmFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftLegFatMass.value)) {
                            Indicator("leftLegFatMass", hsvm.leftLegFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightArmFatMass.value)) {
                            Indicator("rightArmFatMass", hsvm.rightArmFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightLegFatMass.value)) {
                            Indicator("rightLegFatMass", hsvm.rightLegFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.trunkFatMass.value)) {
                            Indicator("trunkFatMass", hsvm.trunkFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftArmMuscleMass.value)) {
                            Indicator("leftArmMuscleMass", hsvm.leftArmMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftLegMuscleMass.value)) {
                            Indicator("leftLegMuscleMass", hsvm.leftLegMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightArmMuscleMass.value)) {
                            Indicator("rightArmMuscleMass", hsvm.rightArmMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightLegMuscleMass.value)) {
                            Indicator("rightLegMuscleMass", hsvm.rightLegMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.trunkMuscleMass.value)) {
                            Indicator("trunkMuscleMass", hsvm.trunkMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.skeletalMuscleMass.value)) {
                            Indicator("skeletalMuscleMass", hsvm.skeletalMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.mineralSaltRate.value)) {
                            Indicator("mineralSaltRate", hsvm.mineralSaltRate.value, true)
                        }
                    }
                }
            })
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
        CONNECT, DISCONNECT, USER_REGISTER_OR_VISIT_FAIL, MEASURE_ING, MEASURE_END, MEASURE_FAIL
    }

    var mac: MutableState<String> = mutableStateOf("")
    var timestamp: MutableState<String> = mutableStateOf("")
    var weight: MutableState<String> = mutableStateOf("")
    var height: MutableState<String> = mutableStateOf("")
    var bmi: MutableState<String> = mutableStateOf("")
    var bodyFatRate: MutableState<String> = mutableStateOf("")
    var subcutaneousFatRate: MutableState<String> = mutableStateOf("")
    var visceralFatLevel: MutableState<String> = mutableStateOf("")
    var bodyWaterRate: MutableState<String> = mutableStateOf("")
    var skeletalMuscleRate: MutableState<String> = mutableStateOf("")
    var boneMass: MutableState<String> = mutableStateOf("")
    var BMR: MutableState<String> = mutableStateOf("")
    var bodyType: MutableState<String> = mutableStateOf("")
    var proteinRate: MutableState<String> = mutableStateOf("")
    var leanBodyMass: MutableState<String> = mutableStateOf("")
    var muscleMass: MutableState<String> = mutableStateOf("")
    var bodyAge: MutableState<String> = mutableStateOf("")
    var healthScore: MutableState<String> = mutableStateOf("")
    var heartRate: MutableState<String> = mutableStateOf("")
    var heartIndex: MutableState<String> = mutableStateOf("")
    var fattyLiverRiskLevel: MutableState<String> = mutableStateOf("")
    var res50KHZ: MutableState<String> = mutableStateOf("")
    var res500KHZ: MutableState<String> = mutableStateOf("")
    var bodyFatMass: MutableState<String> = mutableStateOf("")
    var obesity: MutableState<String> = mutableStateOf("")
    var bodyWaterMass: MutableState<String> = mutableStateOf("")
    var proteinMass: MutableState<String> = mutableStateOf("")
    var mineralLevel: MutableState<String> = mutableStateOf("")
    var dreamWeight: MutableState<String> = mutableStateOf("")
    var standWeight: MutableState<String> = mutableStateOf("")
    var weightControl: MutableState<String> = mutableStateOf("")
    var bodyFatControl: MutableState<String> = mutableStateOf("")
    var muscleMassControl: MutableState<String> = mutableStateOf("")
    var muscleRate: MutableState<String> = mutableStateOf("")
    var leftArmBodyfatRate: MutableState<String> = mutableStateOf("")
    var leftLegBodyfatRate: MutableState<String> = mutableStateOf("")
    var rightArmBodyfatRate: MutableState<String> = mutableStateOf("")
    var rightLegBodyfatRate: MutableState<String> = mutableStateOf("")
    var trunkBodyfatRate: MutableState<String> = mutableStateOf("")
    var leftArmFatMass: MutableState<String> = mutableStateOf("")
    var leftLegFatMass: MutableState<String> = mutableStateOf("")
    var rightArmFatMass: MutableState<String> = mutableStateOf("")
    var rightLegFatMass: MutableState<String> = mutableStateOf("")
    var trunkFatMass: MutableState<String> = mutableStateOf("")
    var leftArmMuscleMass: MutableState<String> = mutableStateOf("")
    var leftLegMuscleMass: MutableState<String> = mutableStateOf("")
    var rightArmMuscleMass: MutableState<String> = mutableStateOf("")
    var rightLegMuscleMass: MutableState<String> = mutableStateOf("")
    var trunkMuscleMass: MutableState<String> = mutableStateOf("")
    var skeletalMuscleMass: MutableState<String> = mutableStateOf("")
    var mineralSaltRate: MutableState<String> = mutableStateOf("")

    var vState: MutableState<MeasureState> = mutableStateOf(MeasureState.DISCONNECT)
}