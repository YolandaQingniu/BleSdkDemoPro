package com.qingniu.blesdkdemopro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.format.DateUtils
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
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.model.QNGender
import com.qingniu.qnplugin.model.QNLengthUnit
import com.qingniu.qnplugin.model.QNWeightUnit
import com.qingniu.qnscaleplugin.QNScalePlugin
import com.qingniu.qnscaleplugin.QNScaleWiFiMp
import com.qingniu.qnscaleplugin.QNUserScaleMp
import com.qingniu.qnscaleplugin.listener.*
import com.qingniu.qnscaleplugin.*
import java.text.DecimalFormat

class QNScaleMeasureActivity : ComponentActivity() {
    // 是否已经连接设备
    private var mIsConnected = false

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
                    val indexs = i.getIntegerArrayListExtra(UserConstant.DELETE_USER_INDEX)
                    if(mDevice != null  && indexs != null && !indexs.isEmpty()){
                        Log.e(TAG, "删除指定坑位的用户, index = ${indexs.size}")
                        QNUserScaleMp.deleteUserList(mDevice, indexs.toList())
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
                if(mIsConnected || mViewModel.vState.value == QNScaleViewModel.MeasureState.CONNECTING){
                    return
                }
                QNPlugin.getInstance(this@QNScaleMeasureActivity).stopScan()
                device.let {
                    val op = QNScaleOperate()
                    op.unit = mCurWeightUnit
                    Log.e(TAG, "连接设备")
                    mViewModel.vState.value = QNScaleViewModel.MeasureState.CONNECTING
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
                mIsConnected = true
                mViewModel.vState.value = QNScaleViewModel.MeasureState.CONNECT
            }

            override fun onConnectFail(code: Int, device: QNScaleDevice?) {
                Log.e(TAG, "设备连接失败")
                mIsConnected = false
                mViewModel.vState.value = QNScaleViewModel.MeasureState.DISCONNECT
            }

            override fun onReadyInteractResult(code: Int, device: QNScaleDevice?) {
                if(code == 0){
                    Log.e(TAG, "设备允许交互")
                    mDevice = device
                    mViewModel.mac.value = device?.mac ?: ""
                    setUser()
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
                    Log.e(TAG, "注册用户成功   user = $user    device = $device")
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
                Log.e(TAG, "同步用户结果，code = $code   user = $user    device = $device")
                if(code == 0){
                    insertOrUpdateDeviceUser(user, device)
                }else if(code == 2003){
                    // 用户密钥错误，说明秤可能把我们本地存的用户坑位给删了
                    // 设置用户信息
                    val user = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
                        .userDao().getUser()
                    val deviceUsers = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
                        .deviceUserDao().getDeviceUser(user?.userId?: "")
                    if(deviceUsers != null){
                        deviceUsers.forEach {
                            if(it.mac == mDevice?.mac){
                                mDeviceUserDao.delete(it)
                                setUser()
                                return@forEach
                            }
                        }
                    }
                }else {
                    mViewModel.vState.value = QNScaleViewModel.MeasureState.USER_REGISTER_OR_VISIT_FAIL
                }
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
                storedDataList: MutableList<QNScaleData>,
                device: QNScaleDevice?
            ) {
                storedDataList?.forEach {
                    Log.e(TAG, "历史测量数据： data = $it")
                }
            }

            override fun onGetLastDataHmac(user: QNScaleUser?, device: QNScaleDevice?): String? {
                Log.e(TAG, "onGetLastDataHmac： user = $user")
                return null
            }

        })
    }

    private fun setUser(){
        if(mDevice == null){
            return
        }
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
            ){ code, msg ->
                Log.e(TAG, "QNScaleUser，$code  $msg")
            }
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
            ) { code, msg ->
                Log.e(TAG, "QNUser构建失败，$code  $msg")
            }
            QNScalePlugin.setMeasureUser(mDevice, qnUser)
        }
    }

    private fun insertOrUpdateDeviceUser(user: QNScaleUser?, device: QNScaleDevice?){
        if (user != null) {
            // 设置用户信息
            val dao = DemoDataBase.getInstance(this@QNScaleMeasureActivity).deviceUserDao()
            val deviceUsers = dao.getAllDeviceUser()
            if(deviceUsers != null && deviceUsers.isNotEmpty()){
                val filterUsers = deviceUsers.filter { it.mac == device?.mac && it.userId == user.userId}
                if(filterUsers != null && filterUsers.isNotEmpty()){
                    val updateUser = filterUsers.get(0)
                    updateUser.index = user.index
                    updateUser.isVisitorMode = user.isVisitorMode
                    if (device != null) {
                        updateUser.isSupportUser = device.supportScaleUser
                        updateUser.isSupportWifi = device.supportWiFi
                    }
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
                insertUser.userId = user.userId
                insertUser.isVisitorMode = user.isVisitorMode
                if (it != null) {
                    insertUser.isSupportUser = it.supportScaleUser
                    insertUser.isSupportWifi = it.supportWiFi
                }
                Log.e(TAG, "insert device user, $insertUser")
                dao.insert(insertUser)
            }
        }
    }

}

fun createQNScaleWeightStr(weight: Double, unit: QNWeightUnit): String{
    val format = DecimalFormat(weight.toString())
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
    val format = DecimalFormat(height.toString())
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
            QNScaleViewModel.MeasureState.CONNECTING -> "Connecting"
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
                            QNScaleIndicator("timeStamp", DateUtils.formatDateTime(ctx,hsvm.timestamp.value.toLong()*1000L,DateUtils.FORMAT_SHOW_TIME), false)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyFatRate.value)) {
                            QNScaleIndicator("bodyFatRate", hsvm.bodyFatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.subcutaneousFatRate.value)) {
                            QNScaleIndicator("subcutaneousFatRate", hsvm.subcutaneousFatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.visceralFatLevel.value)) {
                            QNScaleIndicator("visceralFatLevel", hsvm.visceralFatLevel.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyWaterRate.value)) {
                            QNScaleIndicator("bodyWaterRate", hsvm.bodyWaterRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.skeletalMuscleRate.value)) {
                            QNScaleIndicator("skeletalMuscleRate", hsvm.skeletalMuscleRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.boneMass.value)) {
                            QNScaleIndicator("boneMass", hsvm.boneMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.BMR.value)) {
                            QNScaleIndicator("BMR", hsvm.BMR.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyType.value)) {
                            QNScaleIndicator("bodyType", hsvm.bodyType.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.proteinRate.value)) {
                            QNScaleIndicator("proteinRate", hsvm.proteinRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leanBodyMass.value)) {
                            QNScaleIndicator("leanBodyMass", hsvm.leanBodyMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.muscleMass.value)) {
                            QNScaleIndicator("muscleMass", hsvm.muscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyAge.value)) {
                            QNScaleIndicator("bodyAge", hsvm.bodyAge.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.healthScore.value)) {
                            QNScaleIndicator("healthScore", hsvm.healthScore.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.heartRate.value)) {
                            QNScaleIndicator("heartRate", hsvm.heartRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.heartIndex.value)) {
                            QNScaleIndicator("heartIndex", hsvm.heartIndex.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.fattyLiverRiskLevel.value)) {
                            QNScaleIndicator("fattyLiverRiskLevel", hsvm.fattyLiverRiskLevel.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.res50KHZ.value)) {
                            QNScaleIndicator("res50KHZ", hsvm.res50KHZ.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.res500KHZ.value)) {
                            QNScaleIndicator("res500KHZ", hsvm.res500KHZ.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyFatMass.value)) {
                            QNScaleIndicator("bodyFatMass", hsvm.bodyFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.obesity.value)) {
                            QNScaleIndicator("obesity", hsvm.obesity.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyWaterMass.value)) {
                            QNScaleIndicator("bodyWaterMass", hsvm.bodyWaterMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.proteinMass.value)) {
                            QNScaleIndicator("proteinMass", hsvm.proteinMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.mineralLevel.value)) {
                            QNScaleIndicator("mineralLevel", hsvm.mineralLevel.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.dreamWeight.value)) {
                            QNScaleIndicator("dreamWeight", hsvm.dreamWeight.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.standWeight.value)) {
                            QNScaleIndicator("standWeight", hsvm.standWeight.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.weightControl.value)) {
                            QNScaleIndicator("weightControl", hsvm.weightControl.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.bodyFatControl.value)) {
                            QNScaleIndicator("bodyFatControl", hsvm.bodyFatControl.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.muscleMassControl.value)) {
                            QNScaleIndicator("muscleMassControl", hsvm.muscleMassControl.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.muscleRate.value)) {
                            QNScaleIndicator("muscleRate", hsvm.muscleRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftArmBodyfatRate.value)) {
                            QNScaleIndicator("leftArmBodyfatRate", hsvm.leftArmBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftLegBodyfatRate.value)) {
                            QNScaleIndicator("leftLegBodyfatRate", hsvm.leftLegBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightArmBodyfatRate.value)) {
                            QNScaleIndicator("rightArmBodyfatRate", hsvm.rightArmBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightLegBodyfatRate.value)) {
                            QNScaleIndicator("rightLegBodyfatRate", hsvm.rightLegBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.trunkBodyfatRate.value)) {
                            QNScaleIndicator("trunkBodyfatRate", hsvm.trunkBodyfatRate.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftArmFatMass.value)) {
                            QNScaleIndicator("leftArmFatMass", hsvm.leftArmFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftLegFatMass.value)) {
                            QNScaleIndicator("leftLegFatMass", hsvm.leftLegFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightArmFatMass.value)) {
                            QNScaleIndicator("rightArmFatMass", hsvm.rightArmFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightLegFatMass.value)) {
                            QNScaleIndicator("rightLegFatMass", hsvm.rightLegFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.trunkFatMass.value)) {
                            QNScaleIndicator("trunkFatMass", hsvm.trunkFatMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftArmMuscleMass.value)) {
                            QNScaleIndicator("leftArmMuscleMass", hsvm.leftArmMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.leftLegMuscleMass.value)) {
                            QNScaleIndicator("leftLegMuscleMass", hsvm.leftLegMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightArmMuscleMass.value)) {
                            QNScaleIndicator("rightArmMuscleMass", hsvm.rightArmMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.rightLegMuscleMass.value)) {
                            QNScaleIndicator("rightLegMuscleMass", hsvm.rightLegMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.trunkMuscleMass.value)) {
                            QNScaleIndicator("trunkMuscleMass", hsvm.trunkMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.skeletalMuscleMass.value)) {
                            QNScaleIndicator("skeletalMuscleMass", hsvm.skeletalMuscleMass.value, true)
                        }
                        if (!TextUtils.isEmpty(hsvm.mineralSaltRate.value)) {
                            QNScaleIndicator("mineralSaltRate", hsvm.mineralSaltRate.value, true)
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
        CONNECT, CONNECTING, DISCONNECT, USER_REGISTER_OR_VISIT_FAIL, MEASURE_ING, MEASURE_END, MEASURE_FAIL
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