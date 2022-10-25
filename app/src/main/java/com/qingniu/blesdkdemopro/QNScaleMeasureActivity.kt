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
                if(mIsConecting || (
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

                // 设置用户信息
                val user = DemoDataBase.getInstance(this@QNScaleMeasureActivity)
                    .userDao().getUser()
                val qnUser = QNUser.build(
                    "user123456789",
                    if(user.gender == "MALE") QNGender.MALE else QNGender.FEMALE,
                    user.age,
                    user.height,
                    false
                )
                QNScalePlugin.setMeasureUser(mDevice, qnUser)
                if(mDevice?.supportScaleUser == true){
                    /** 设置测量用户 start **/
                    val index = SpUtils.getIntValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_INDEX_KEY)
                    val key = SpUtils.getIntValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_SECRET_KEY)
                    val isVisitorMode = SpUtils.getBooleanValue(mDevice?.mac!!, this@QNScaleMeasureActivity, SpUtils.USER_IS_VISITOR_KEY)
                    val qnScaleUser = QNScaleUser.build(
                        qnUser,
                        index,
                        if(key <= 0) 10086 else key,
                        isVisitorMode
                    )
                    Log.e(TAG, "设置测量用户，user = $qnScaleUser")
                    QNUserScaleMp.setMeasureUserToUserDevice(mDevice, qnScaleUser)
                } else {
                    Log.e(TAG, "设备不支持设置测量用户")
                }
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
                    if(device?.scaleUserFull == true){
                        Log.e(TAG, "注册用户未成功， 秤端用户数已满")
                    }
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
                Log.e(TAG, if(code == 0) "配网成功" else "配网失败，状态返回： code = $code")
            }

        })

        QNScalePlugin.setDataListener(object : QNScaleDataListener {
            override fun onRealTimeWeight(weight: String?, device: QNScaleDevice?) {
                Log.e(TAG, "实时测量： weight = $weight")
                mViewModel.vState.value = QNScaleViewModel.MeasureState.MEASURE_ING
                mViewModel.weight.value = createQNScaleWeightStr(weight?.toDouble()?: 0.0, QNWeightUnit.UNIT_KG)
            }

            override fun onReceiveMeasureResult(scaleData: QNScaleData?, device: QNScaleDevice?) {
                Log.e(TAG, "测量结果： data = $scaleData")
                mViewModel.vState.value = QNScaleViewModel.MeasureState.MEASURE_END
                mViewModel.height.value = createQNScaleHeightStr(if(scaleData?.height == null) 0 else (scaleData?.height!!.toInt()), QNLengthUnit.UNIT_CM)
                mViewModel.weight.value = createQNScaleWeightStr(if(scaleData?.weight == null) 0.0 else (scaleData?.weight!!.toDouble()), QNWeightUnit.UNIT_KG)
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

fun createQNScaleHeightStr(height: Int, unit: QNLengthUnit): String{
    when(unit){
        QNLengthUnit.UNIT_CM -> {
            return "$height CM"
        }
        QNLengthUnit.UNIT_IN -> {
            return "$height IN"
        }
        QNLengthUnit.UNIT_FT_IN -> {
            return "$height FT_IN"
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
        CONNECT, DISCONNECT, MEASURE_ING, MEASURE_END, MEASURE_FAIL
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