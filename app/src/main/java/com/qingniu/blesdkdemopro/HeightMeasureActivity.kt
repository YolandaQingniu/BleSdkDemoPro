package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
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
import com.qingniu.blesdkdemopro.constant.DemoUnit
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.qnheightweightscaleplugin.QNHeightWeightScalePlugin
import com.qingniu.qnheightweightscaleplugin.listener.QNHeightWeightScaleDataListener
import com.qingniu.qnheightweightscaleplugin.listener.QNHeightWeightScaleStatusListener
import com.qingniu.qnheightweightscaleplugin.QNHeightWeightScaleData
import com.qingniu.qnheightweightscaleplugin.QNHeightWeightScaleDevice
import com.qingniu.qnheightweightscaleplugin.QNHeightWeightScaleOperate
import com.qingniu.qnheightweightscaleplugin.QNHeightWeightUser
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.inter.QNScanListener
import com.qingniu.qnplugin.model.QNGender
import com.qingniu.qnplugin.model.QNLengthUnit
import com.qingniu.qnplugin.model.QNWeightUnit

class HeightMeasureActivity : ComponentActivity() {

    companion object {
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, HeightMeasureActivity::class.java)
        }
    }

    lateinit var mHeightScaleViewModel: HeightScaleViewModel

    val mHandler = Handler(Looper.getMainLooper())

    var qnHeightWeightScaleDevice: QNHeightWeightScaleDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleSdkDemoProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    mHeightScaleViewModel = viewModel()
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BgGrey)
                    ) {
                        TitleBar("Height Scale", true)
                        HeightStatusBar()
                        MeasureBoard()
                    }
                }
            }
        }
        initQNHeightWeightScale()
    }

    override fun onDestroy() {
        super.onDestroy()
        QNPlugin.getInstance(this).stopScan()
        qnHeightWeightScaleDevice?.let {
            QNHeightWeightScalePlugin.cancelConnectHeightWeightScaleDevice(it)
        }
    }

    private fun createPercentageIndicatorStr(rate: String?): String {
        return if (TextUtils.isEmpty(rate)) {
            ""
        } else {
            "$rate %"
        }
    }

    private fun createWeightIndicatorStr(weight: String?):String{
        if (TextUtils.isEmpty(weight)) {
            return ""
        }
        val curWeightUnit = DemoDataBase.getInstance(this@HeightMeasureActivity)
            .unitSettingDao().getUnitSetting().weightUnit
        return when (curWeightUnit) {
            DemoUnit.KG.showName -> "$weight ${DemoUnit.KG.showName}"
            DemoUnit.LB.showName -> "${QNHeightWeightScalePlugin.getWeightLb(weight)} ${DemoUnit.LB.showName}"
            DemoUnit.ST_LB.showName -> {
                val valueArray = QNHeightWeightScalePlugin.getWeightStLb(weight)
                "${valueArray[0]} ST : ${valueArray[1]} LB"
            }
            DemoUnit.ST.showName -> "${QNHeightWeightScalePlugin.getWeightSt(weight)} ${DemoUnit.ST.showName}"
            DemoUnit.JIN.showName -> "${QNHeightWeightScalePlugin.getWeightJin(weight)} ${DemoUnit.JIN.showName}"
            else -> "$weight ${DemoUnit.KG.showName}"
        }
    }

    private fun createHeightStr(height: String):String{
        val curLengthUnit = DemoDataBase.getInstance(this@HeightMeasureActivity)
            .unitSettingDao().getUnitSetting().lengthUnit
        return when(curLengthUnit){
            DemoUnit.CM.showName -> "$height ${DemoUnit.CM.showName}"
            DemoUnit.FT_IN.showName ->  {
                val valueArray = QNHeightWeightScalePlugin.getHeightFtIn(height)
                "${valueArray[0]}' ${valueArray[1]}\""
            }
            else -> "$height ${DemoUnit.CM.showName}"
        }
    }

    private fun initQNHeightWeightScale() {
        QNHeightWeightScalePlugin.setScalePlugin(QNPlugin.getInstance(this))

        QNHeightWeightScalePlugin.setStatusListener(object : QNHeightWeightScaleStatusListener {
            override fun onDiscoverScaleDevice(device: QNHeightWeightScaleDevice) {
                qnHeightWeightScaleDevice = device
                QNPlugin.getInstance(this@HeightMeasureActivity).stopScan()
            }

            override fun onHeightWeightScaleConnectedSuccess(device: QNHeightWeightScaleDevice) {
                Log.e("qzx", "onHeightWeightScaleConnectedSuccess")
                mHeightScaleViewModel.mac.value = device.mac
                mHeightScaleViewModel.vState.value = HeightScaleViewModel.MeasureState.CONNECT
            }

            override fun onHeightWeightScaleConnectFail(
                device: QNHeightWeightScaleDevice?
            ) {
                Log.e("qzx", "onHeightWeightScaleConnectFail")
                mHeightScaleViewModel.mac.value = ""
                mHeightScaleViewModel.vState.value = HeightScaleViewModel.MeasureState.DISCONNECT
            }

            override fun onHeightWeightScaleReadyInteractResult(
                code: Int,
                device: QNHeightWeightScaleDevice?
            ) {
                Log.e("qzx", "onHeightWeightScaleReadyInteractResult, code = $code")
            }

            override fun onHeightWeightScaleDisconnected(device: QNHeightWeightScaleDevice?) {
                Log.e("qzx", "onHeightWeightScaleDisconnected")
                mHeightScaleViewModel.vState.value = HeightScaleViewModel.MeasureState.DISCONNECT
            }
        })
        QNHeightWeightScalePlugin.setDataListener(object : QNHeightWeightScaleDataListener {
            override fun onHeightWeightScaleRealTimeWeight(
                weight: String,
                device: QNHeightWeightScaleDevice
            ) {
                Log.e("qzx", "onHeightWeightScaleRealTimeWeight, weight = $weight")
                mHeightScaleViewModel.apply {
                    this.weightStr.value = createWeightIndicatorStr(weight)
                    this.vState.value = HeightScaleViewModel.MeasureState.MEASURE_WEIGHT
                }
            }

            override fun onHeightWeightScaleRealTimeHeight(
                height: String,
                device: QNHeightWeightScaleDevice
            ) {
                Log.e("qzx", "onHeightWeightScaleRealTimeHeight, height = $height")
                mHeightScaleViewModel.apply {
                    this.heightStr.value = createHeightStr(height)
                    this.vState.value = HeightScaleViewModel.MeasureState.MEASURE_HEIGHT
                }
            }

            override fun onHeightWeightScaleReceiveMeasureResult(
                scaleData: QNHeightWeightScaleData,
                device: QNHeightWeightScaleDevice
            ) {
                Log.e("qzx", "onHeightWeightScaleReceiveMeasureResult, result = $scaleData")


                val gender = if (DemoDataBase.getInstance(this@HeightMeasureActivity)
                        .userDao().getUser().gender == "MALE"
                ) {
                    QNGender.MALE
                } else {
                    QNGender.FEMALE
                }

                val age = DemoDataBase.getInstance(this@HeightMeasureActivity)
                    .userDao().getUser().age

                scaleData.makeDataComplete(QNHeightWeightUser("001", gender, age))

                mHeightScaleViewModel.apply {
                    this.weightStr.value = createWeightIndicatorStr(scaleData.weight)
                    this.heightStr.value = createHeightStr(scaleData.height)
                    this.bmi.value = scaleData.bmi ?: ""
                    this.bodyFatRate.value = createPercentageIndicatorStr(scaleData.bodyFatRate)
                    this.subcutaneousFatRate.value = createPercentageIndicatorStr(scaleData.subcutaneousFatRate)
                    this.visceralFatLevel.value = scaleData.visceralFatLevel ?: ""
                    this.bodyWaterRate.value = createPercentageIndicatorStr(scaleData.bodyWaterRate)
                    this.skeletalMuscleRate.value = createPercentageIndicatorStr(scaleData.skeletalMuscleRate)
                    this.boneMass.value = createWeightIndicatorStr(scaleData.boneMass)
                    this.bmr.value = scaleData.bmr ?: ""
                    this.bodyType.value = scaleData.bodyType ?: ""
                    this.proteinRate.value = createPercentageIndicatorStr(scaleData.proteinRate)
                    this.leanBodyMass.value = createWeightIndicatorStr(scaleData.leanBodyMass)
                    this.muscleMass.value = createWeightIndicatorStr(scaleData.muscleMass)
                    this.bodyAge.value = scaleData.bodyAge ?: ""
                    this.healthScore.value = scaleData.healthScore ?: ""
                    this.fattyLiverRiskLevel.value = scaleData.fattyLiverRiskLevel ?: ""
                    this.obesity.value = createPercentageIndicatorStr(scaleData.obesity)
                    this.bodyWaterMass.value = createWeightIndicatorStr(scaleData.bodyWaterMass)
                    this.proteinMass.value = createWeightIndicatorStr(scaleData.proteinMass)
                    this.mineralLevel.value = scaleData.mineralLevel ?: ""
                    this.dreamWeight.value = createWeightIndicatorStr(scaleData.dreamWeight)
                    this.standWeight.value = createWeightIndicatorStr(scaleData.standWeight)
                    this.weightControl.value = createWeightIndicatorStr(scaleData.weightControl)
                    this.bodyFatControl.value = createWeightIndicatorStr(scaleData.bodyFatControl)
                    this.muscleMassControl.value = createWeightIndicatorStr(scaleData.muscleMassControl)
                    this.muscleRate.value = createPercentageIndicatorStr(scaleData.muscleRate)
                    this.vState.value = HeightScaleViewModel.MeasureState.MEASURE_END
                }
            }

            override fun onHeightWeightScaleReceiveMeasureFailed(device: QNHeightWeightScaleDevice?) {
                Log.e("qzx", "onHeightWeightScaleReceiveMeasureFailed")
                mHeightScaleViewModel.apply {
                    this.weightStr.value = "--"
                    this.heightStr.value = "--"
                    this.vState.value = HeightScaleViewModel.MeasureState.MEASURE_END
                }
            }

            override fun onHeightWeightScaleReceiveStorageData(
                list: MutableList<QNHeightWeightScaleData>,
                device: QNHeightWeightScaleDevice
            ) {

            }
        })

        QNPlugin.getInstance(this).setQnScanListener(object : QNScanListener {
            override fun onStartScan() {
                qnHeightWeightScaleDevice = null
            }

            override fun onStopScan() {
                qnHeightWeightScaleDevice?.let {

                    val operate = QNHeightWeightScaleOperate()

                    val curWeightUnit = DemoDataBase.getInstance(this@HeightMeasureActivity)
                        .unitSettingDao().getUnitSetting().weightUnit

                    val curLengthUnit = DemoDataBase.getInstance(this@HeightMeasureActivity)
                        .unitSettingDao().getUnitSetting().lengthUnit

                    operate.heightUnit = when (curLengthUnit) {
                        DemoUnit.CM.showName -> QNLengthUnit.UNIT_CM
                        DemoUnit.FT_IN.showName -> QNLengthUnit.UNIT_FT_IN
                        else -> QNLengthUnit.UNIT_CM
                    }
                    operate.weightUnit = when (curWeightUnit) {
                        DemoUnit.KG.showName -> QNWeightUnit.UNIT_KG
                        DemoUnit.LB.showName -> QNWeightUnit.UNIT_LB
                        DemoUnit.ST_LB.showName -> QNWeightUnit.UNIT_ST_LB
                        DemoUnit.ST.showName -> QNWeightUnit.UNIT_ST
                        DemoUnit.JIN.showName -> QNWeightUnit.UNIT_JIN
                        else -> QNWeightUnit.UNIT_KG
                    }
                    QNHeightWeightScalePlugin.connectHeightWeightScaleDevice(it, operate)
                }
            }
        })

        QNPlugin.getInstance(this).startScan()
    }
}

@Composable
fun HeightStatusBar() {
    val ctx = LocalContext.current
    val hsvm: HeightScaleViewModel = viewModel()
    val status = if (!DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Bluetooth turn off"
    } else if (!DemoBleUtils.isRunOnAndroid12Mode(ctx) && !DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Location turn off"
    } else if (!DemoBleUtils.hasBlePermission(ctx)) {
        "Need bluetooth permission"
    } else {
        when (hsvm.vState.value) {
            HeightScaleViewModel.MeasureState.CONNECT -> "Connected"
            HeightScaleViewModel.MeasureState.DISCONNECT -> "Disconnected"
            HeightScaleViewModel.MeasureState.MEASURE_WEIGHT -> "Measure weight"
            HeightScaleViewModel.MeasureState.MEASURE_HEIGHT -> "Measure height"
            HeightScaleViewModel.MeasureState.MEASURE_END -> "Measure end"
            HeightScaleViewModel.MeasureState.MEASURE_FAIL -> "Measure fail"
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
fun MeasureBoard() {
    val ctx = LocalContext.current
    val hsvm: HeightScaleViewModel = viewModel()
    Column(
        Modifier
            .padding(top = 100.dp)
            .fillMaxSize()
    ) {
        val cMac = hsvm.mac.value
        val cWeightStr = hsvm.weightStr.value
        val cHeightStr = hsvm.heightStr.value
        val cvState = hsvm.vState.value
        val cBmi = hsvm.bmi.value

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
            text = if (cvState == HeightScaleViewModel.MeasureState.MEASURE_WEIGHT) {
                cWeightStr
            } else if (cvState == HeightScaleViewModel.MeasureState.MEASURE_HEIGHT ||
                cvState == HeightScaleViewModel.MeasureState.MEASURE_END ||
                cvState == HeightScaleViewModel.MeasureState.MEASURE_FAIL ||
                !(TextUtils.isEmpty(cBmi))
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
        if (cvState == HeightScaleViewModel.MeasureState.MEASURE_END || !(TextUtils.isEmpty(cBmi))) {
            LazyColumn(content = {
                item {
                    Column {
                        Indicator("weight", cWeightStr, false)
                        Indicator("height", cHeightStr, false)
                        Indicator("bmi", cBmi, true)
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
                        if (!TextUtils.isEmpty(hsvm.bmr.value)) {
                            Indicator("bmr", hsvm.bmr.value, true)
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
                        if (!TextUtils.isEmpty(hsvm.fattyLiverRiskLevel.value)) {
                            Indicator("fattyLiverRiskLevel", hsvm.fattyLiverRiskLevel.value, true)
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
                        if (!TextUtils.isEmpty(hsvm.bodyFatMass.value)) {
                            Indicator("bodyFatMass", hsvm.bodyFatMass.value, true)
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
                    }
                }
            })
        }
    }
}

@Composable
fun Indicator(
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

class HeightScaleViewModel : ViewModel() {

    enum class MeasureState {
        CONNECT, DISCONNECT, MEASURE_WEIGHT, MEASURE_HEIGHT, MEASURE_END, MEASURE_FAIL
    }

    var mac: MutableState<String> = mutableStateOf("")
    var weightStr: MutableState<String> = mutableStateOf("--")
    var heightStr: MutableState<String> = mutableStateOf("--")
    var bmi: MutableState<String> = mutableStateOf("")
    var bodyFatRate: MutableState<String> = mutableStateOf("")
    var subcutaneousFatRate: MutableState<String> = mutableStateOf("")
    var visceralFatLevel: MutableState<String> = mutableStateOf("")
    var bodyWaterRate: MutableState<String> = mutableStateOf("")
    var skeletalMuscleRate: MutableState<String> = mutableStateOf("")
    var boneMass: MutableState<String> = mutableStateOf("")
    var bmr: MutableState<String> = mutableStateOf("")
    var bodyType: MutableState<String> = mutableStateOf("")
    var proteinRate: MutableState<String> = mutableStateOf("")
    var leanBodyMass: MutableState<String> = mutableStateOf("")
    var muscleMass: MutableState<String> = mutableStateOf("")
    var bodyAge: MutableState<String> = mutableStateOf("")
    var healthScore: MutableState<String> = mutableStateOf("")
    var fattyLiverRiskLevel: MutableState<String> = mutableStateOf("")
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
    var vState: MutableState<MeasureState> = mutableStateOf(MeasureState.DISCONNECT)
}