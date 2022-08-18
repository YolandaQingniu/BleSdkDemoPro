package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
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

    val mHeightScaleViewModel = HeightScaleViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleSdkDemoProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BgGrey)
                    ) {
                        val weightS = remember {
                            mHeightScaleViewModel.weight
                        }
                        val heightS = remember {
                            mHeightScaleViewModel.height
                        }
                        val weightUnitS = remember {
                            mHeightScaleViewModel.weightUnit
                        }
                        val heightUnitS = remember {
                            mHeightScaleViewModel.heightUnit
                        }
                        TitleBar("Height Scale", true)
                        MeasureBoard(weightS, heightS, weightUnitS, heightUnitS)
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
            override fun onHeightWeightScaleConnectedSuccess(device: QNHeightWeightScaleDevice?) {
                Log.e("qzx", "onHeightWeightScaleConnectedSuccess")
            }

            override fun onHeightWeightScaleConnectFail(
                code: Int,
                device: QNHeightWeightScaleDevice?
            ) {
                Log.e("qzx", "onHeightWeightScaleConnectFail")
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
                }
            }

            override fun onHeightWeightScaleRealTimeHeight(
                height: String,
                device: QNHeightWeightScaleDevice
            ) {
                Log.e("qzx", "onHeightWeightScaleRealTimeHeight, height = $height")
                mHeightScaleViewModel.apply {
                    this.height.value = height
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
                }
            }

            override fun onHeightWeightScaleReceiveMeasureFailed(device: QNHeightWeightScaleDevice?) {
                Log.e("qzx", "onHeightWeightScaleReceiveMeasureFailed")
                mHeightScaleViewModel.apply {
                    this.weight.value = "--"
                    this.height.value = "--"
                }
            }

        })


        QNPlugin.getInstance(this).startScan(object : QNResultCallback {
            override fun onResult(p0: Int, p1: String?) {
            }
        })
    }
}

@Composable
fun MeasureBoard(
    weight: MutableState<String>, height: MutableState<String>,
    weightUnit: MutableState<String>, heightUnit: MutableState<String>
) {
    Column(
        Modifier
            .padding(top = 160.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "${weight.value} ${weightUnit.value} ${height.value} ${heightUnit.value}", fontSize = 32.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

class HeightScaleViewModel:ViewModel(){
    var weight: MutableState<String> = mutableStateOf("--")
    var height: MutableState<String> = mutableStateOf("--")
    var weightUnit: MutableState<String> = mutableStateOf("KG")
    var heightUnit: MutableState<String> = mutableStateOf("CM")
}