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
import com.qingniu.blecenter.utils.BleUtils
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.qnheightweightscaleplugin.QNHeightWeightScalePlugin
import com.qingniu.qnheightweightscaleplugin.listener.QNHeightWeightScaleDataListener
import com.qingniu.qnheightweightscaleplugin.listener.QNHeightWeightScaleStatusListener
import com.qingniu.qnheightweightscaleplugin.model.*
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.inter.QNResultCallback

class HeightMeasureActivity : ComponentActivity() {

    companion object {
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, HeightMeasureActivity::class.java)
        }
    }

    lateinit var mHeightScaleViewModel: HeightScaleViewModel

    val mHandler = Handler(Looper.getMainLooper())

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
                        StatusBar()
                        MeasureBoard()
                    }
                }
            }
        }
        initQNHeightWeightScale()
    }

    private fun initQNHeightWeightScale() {
        QNHeightWeightScalePlugin.setScalePlugin(
            (application as BaseApplication).getQNPlugin(),
            object :
                QNResultCallback {
                override fun onResult(code: Int, msg: String?) {
                    Log.e("qzx", "code: $code, msg: $msg")
                }
            })
        QNHeightWeightScalePlugin.setDeviceListener {
            QNPlugin.getInstance(this).stopScan()
            Log.e("qzx", it.toString())
            val operate = QNHeightWeightScaleOperate()
            operate.heightUnit = QNHeightUnit.QNHeightUnitCm
            operate.weightUnit = QNWeightUnit.QNWeightUnitKg
            QNHeightWeightScalePlugin.connectHeightWeightScaleDevice(
                it,
                operate,
                object : QNResultCallback {
                    override fun onResult(code: Int, msg: String?) {
                        Log.e("qzx", "code: $code, msg: $msg")
                    }
                })
        }
        QNHeightWeightScalePlugin.setStatusListener(object : QNHeightWeightScaleStatusListener {
            override fun onHeightWeightScaleConnectedSuccess(device: QNHeightWeightScaleDevice) {
                Log.e("qzx", "onHeightWeightScaleConnectedSuccess")
                mHeightScaleViewModel.mac.value = device.mac
                mHeightScaleViewModel.vState.value = HeightScaleViewModel.MeasureState.CONNECT
            }

            override fun onHeightWeightScaleConnectFail(
                code: Int,
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
                Log.e("qzx", "onHeightWeightScaleReadyInteractResult")
            }

            override fun onHeightWeightScaleDisconnected(device: QNHeightWeightScaleDevice?) {
                Log.e("qzx", "onHeightWeightScaleDisconnected")
            }

        })
        QNHeightWeightScalePlugin.setDataListener(object : QNHeightWeightScaleDataListener {
            override fun onHeightWeightScaleRealTimeWeight(
                weight: String,
                device: QNHeightWeightScaleDevice
            ) {
                Log.e("qzx", "onHeightWeightScaleRealTimeWeight, weight = $weight")
                mHeightScaleViewModel.apply {
                    this.weight.value = weight
                    this.vState.value = HeightScaleViewModel.MeasureState.MEASURE_WEIGHT
                }
            }

            override fun onHeightWeightScaleRealTimeHeight(
                height: String,
                device: QNHeightWeightScaleDevice
            ) {
                Log.e("qzx", "onHeightWeightScaleRealTimeHeight, height = $height")
                mHeightScaleViewModel.apply {
                    this.height.value = height
                    this.vState.value = HeightScaleViewModel.MeasureState.MEASURE_HEIGHT
                }
            }

            override fun onHeightWeightScaleReceiveMeasureResult(
                scaleData: QNHeightWeightScaleData,
                device: QNHeightWeightScaleDevice
            ) {
                Log.e("qzx", "onHeightWeightScaleReceiveMeasureResult, result = $scaleData")
                mHeightScaleViewModel.apply {
                    this.weight.value = scaleData.weight
                    this.height.value = scaleData.height
                    this.vState.value = HeightScaleViewModel.MeasureState.MEASURE_END
                }
            }

            override fun onHeightWeightScaleReceiveMeasureFailed(device: QNHeightWeightScaleDevice?) {
                Log.e("qzx", "onHeightWeightScaleReceiveMeasureFailed")
                mHeightScaleViewModel.apply {
                    this.weight.value = "--"
                    this.height.value = "--"
                    this.vState.value = HeightScaleViewModel.MeasureState.MEASURE_END
                }
            }

        })


        QNPlugin.getInstance(this).startScan(object : QNResultCallback {
            override fun onResult(p0: Int, p1: String?) {
                Log.e("hyrrr", "$p0 $p1")
            }
        })
    }
}

@Composable
fun StatusBar() {
    val ctx = LocalContext.current
    val hsvm:HeightScaleViewModel = viewModel()
    val status = if (!BleUtils.isBlueToothSwitchOn(ctx)){
        "Bluetooth switch is disabled"
    }else if (!BleUtils.isRunOnAndroid12Mode(ctx) && !BleUtils.isBlueToothSwitchOn(ctx)){
        "Location switch is disabled"
    }else if(!BleUtils.hasBlePermission(ctx)){
        "Need bluetooth permission"
    }else{
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
    val hsvm:HeightScaleViewModel = viewModel()
    Column(
        Modifier
            .padding(top = 100.dp)
            .fillMaxSize()
    ) {
        val cMac = hsvm.mac.value
        val cWeight = hsvm.weight.value
        val cWeightUnit = DemoDataBase.getInstance(ctx).unitSettingDao().getUnitSetting().weightUnit
        val cHeight = hsvm.height.value
        val cLengthUnit = DemoDataBase.getInstance(ctx).unitSettingDao().getUnitSetting().lengthUnit
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
                text = "AppId: "+QNPlugin.getInstance(ctx).appId,
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            )
        }
        Text(
            text = if (cvState == HeightScaleViewModel.MeasureState.MEASURE_WEIGHT) {
                "$cWeight $cWeightUnit"
            } else if (cvState == HeightScaleViewModel.MeasureState.MEASURE_HEIGHT ||
                cvState == HeightScaleViewModel.MeasureState.MEASURE_END ||
                cvState == HeightScaleViewModel.MeasureState.MEASURE_FAIL
            ) {
                "$cHeight $cLengthUnit"
            } else {
                ""
            },

            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp, bottom = 10.dp)
        )
        if (cvState == HeightScaleViewModel.MeasureState.MEASURE_END) {
            Column {
                Indicator("weight", "$cWeight $cWeightUnit", false)
                Indicator("height", "$cHeight $cLengthUnit", false)
                Indicator("bmi", "", true)
            }
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
        CONNECT,DISCONNECT, MEASURE_WEIGHT, MEASURE_HEIGHT, MEASURE_END,MEASURE_FAIL
    }

    var mac: MutableState<String> = mutableStateOf("")
    var weight: MutableState<String> = mutableStateOf("--")
    var height: MutableState<String> = mutableStateOf("--")
    var vState:MutableState<MeasureState> = mutableStateOf(MeasureState.DISCONNECT)
}