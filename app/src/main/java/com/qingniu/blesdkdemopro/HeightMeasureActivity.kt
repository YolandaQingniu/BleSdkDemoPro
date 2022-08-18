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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        TitleBar("Height Scale", true)
                        MeasureBoard(weight = 56.5, unit = "kg")
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
                weight: String?,
                device: QNHeightWeightScaleDevice?
            ) {
                Log.e("qzx", "onHeightWeightScaleRealTimeWeight, weight = $weight")
            }

            override fun onHeightWeightScaleRealTimeHeight(
                height: String?,
                device: QNHeightWeightScaleDevice?
            ) {
                Log.e("qzx", "onHeightWeightScaleRealTimeHeight, height = $height")
            }

            override fun onHeightWeightScaleReceiveMeasureResult(
                scaleData: QNHeightWeightScaleData?,
                device: QNHeightWeightScaleDevice?
            ) {
                Log.e("qzx", "onHeightWeightScaleReceiveMeasureResult, result = $scaleData")
            }

            override fun onHeightWeightScaleReceiveMeasureFailed(device: QNHeightWeightScaleDevice?) {
                Log.e("qzx", "onHeightWeightScaleReceiveMeasureFailed")
            }

        })


        QNPlugin.getInstance(this).startScan(object : QNResultCallback {
            override fun onResult(p0: Int, p1: String?) {
            }
        })
    }
}

@Composable
fun MeasureBoard(weight: Double, unit: String) {
    val showText = if (weight > 0) {
        "$weight $unit"
    } else {
        "- - $unit"
    }

    Column(
        Modifier
            .padding(top = 160.dp)
            .fillMaxSize()
    ) {
        Text(
            text = showText, fontSize = 32.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}