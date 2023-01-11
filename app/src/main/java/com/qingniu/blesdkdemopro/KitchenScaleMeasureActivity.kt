package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.TipGrey
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.qnkitchenplugin.NumberType
import com.qingniu.qnkitchenplugin.QNKitchenScaleDevice
import com.qingniu.qnkitchenplugin.QNKitchenScalePlugin
import com.qingniu.qnkitchenplugin.listener.QNKitchenScaleDeviceListener
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.model.QNWeightUnit

/**
 * create by qzx
 * 2022/12/6: 9:39 上午
 * desc:
 */
class KitchenScaleMeasureActivity : ComponentActivity() {

    private var mIsConnected = false

    private var mDevice: MutableState<QNKitchenScaleDevice?> = mutableStateOf(null)

    companion object {
        const val TAG = "KitchenScale"
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, KitchenScaleMeasureActivity::class.java)
        }
    }

    lateinit var mViewModel: KitchenScaleViewModel

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
                        TitleBar("QNKitchenScale", false)
                        KitchenScaleStatusBar()
                        KitchenScaleMeasureBoard(mViewModel, mDevice)
                    }
                }
            }
        }
        init()
    }

    fun init() {
        QNPlugin.getInstance(this).startScan()
        QNKitchenScalePlugin.setKitchenScalePlugin(QNPlugin.getInstance(this))
        QNKitchenScalePlugin.setDeviceListener(object : QNKitchenScaleDeviceListener {
            override fun onDiscoverKitchenScaleDevice(device: QNKitchenScaleDevice?) {
                Log.e(QNScaleMeasureActivity.TAG, "发现设备，mac = ${device?.mac} ")
                QNPlugin.getInstance(this@KitchenScaleMeasureActivity).stopScan()
                device.let {
                    Log.e(TAG, "连接设备")
                    mViewModel.vState.value = KitchenScaleViewModel.MeasureState.CONNECTING
                    QNKitchenScalePlugin.connectDevice(device)
                }
            }

            override fun onSetKitchenScaleUnitResult(code: Int, device: QNKitchenScaleDevice?) {
                if (code == 0) {
                    Log.e(TAG, "设置单位成功")
                } else {
                    Log.e(TAG, "设置单位失败，code = $code")
                }
            }

            override fun onSetKitchenScaleShellingResult(code: Int, device: QNKitchenScaleDevice?) {
                if (code == 0) {
                    Log.e(TAG, "去皮成功")
                } else {
                    Log.e(TAG, "去皮失败，code = $code")
                }
            }

            override fun onSetKitchenScaleStandTimeResult(
                code: Int,
                device: QNKitchenScaleDevice?
            ) {
                if (code == 0) {
                    Log.e(TAG, "设置待机时间成功")
                } else {
                    Log.e(TAG, "设置待机时间失败，code = $code")
                }
            }

            override fun onKitchenScaleConnectedSuccess(device: QNKitchenScaleDevice?) {
                Log.e(TAG, "设备连接成功")
                mViewModel.vState.value = KitchenScaleViewModel.MeasureState.CONNECT
                mIsConnected = true
            }

            override fun onKitchenScaleConnectFail(device: QNKitchenScaleDevice?) {
                Log.e(TAG, "设备连接失败")
                mViewModel.vState.value = KitchenScaleViewModel.MeasureState.DISCONNECT
                mIsConnected
                mDevice.value = null
            }

            override fun onKitchenScaleReadyInteract(code: Int, device: QNKitchenScaleDevice?) {
                if (code == 0) {
                    Log.e(TAG, "设备允许交互")
                    mDevice.value = device
                    mViewModel.mac.value = mDevice.value?.mac ?: ""
                    mViewModel.supportUnitList.value.clear()
                    mDevice.value?.deviceUnitSupportedList?.forEach {
                        mViewModel.supportUnitList.value.add(it)
                        Log.e(TAG, "设备支持的单位 $it")
                    }
                }
            }

            override fun onKitchenScaleDisconnected(device: QNKitchenScaleDevice?) {
                Log.e(TAG, "设备断开连接")
                mViewModel.vState.value = KitchenScaleViewModel.MeasureState.DISCONNECT
                mIsConnected = false
                mDevice.value = null
            }

        })

        QNKitchenScalePlugin.setDataListener { data, device ->
            Log.e(TAG, "测量数据： data = $data ")
            mViewModel.curUnit.value = data.unit
            mViewModel.timestamp.value = data.timeStamp
            mViewModel.weight.value = if(data.isReverseWeightFlag) "-${data.weight}" else data.weight
            mViewModel.curNumberType.value = device.deviceNumberType
            mViewModel.isPeel.value = data.isShellingFlag
            mViewModel.isOverWeight.value = data.isOverWeightFlag
            mViewModel.isSteady.value = data.isStableFlag
//            mViewModel.supportUnitList.value = device.deviceUnitSupportedList
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDevice.value != null) {
            QNKitchenScalePlugin.cancelConnectDevice(mDevice.value)
        }
    }
}


fun createKitchenScaleUnitStr(unit: QNWeightUnit): String {
    when (unit) {
        QNWeightUnit.UNIT_G -> {
            return "g"
        }
        QNWeightUnit.UNIT_ML -> {
            return "ml"
        }
        QNWeightUnit.UNIT_LB_OZ -> {
            return "lb.oz"
        }
        QNWeightUnit.UNIT_FL_OZ -> {
            return "fl.oz"
        }
        QNWeightUnit.UNIT_ML_MILK -> {
            return "ml(milk)"
        }
        else -> {
            return "g"
        }
    }
}

@Composable
fun KitchenScaleStatusBar() {
    val ctx = LocalContext.current
    val hsvm: KitchenScaleViewModel = viewModel()
    val status = if (!DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Bluetooth turn off"
    } else if (!DemoBleUtils.isRunOnAndroid12Mode(ctx) && !DemoBleUtils.isBlueToothSwitchOn(ctx)) {
        "Location turn off"
    } else if (!DemoBleUtils.hasBlePermission(ctx)) {
        "Need bluetooth permission"
    } else {
        when (hsvm.vState.value) {
            KitchenScaleViewModel.MeasureState.CONNECT -> "Connected"
            KitchenScaleViewModel.MeasureState.CONNECTING -> "Connecting"
            KitchenScaleViewModel.MeasureState.DISCONNECT -> "Disconnected"
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
fun KitchenScaleMeasureBoard(
    vm: KitchenScaleViewModel,
    device: MutableState<QNKitchenScaleDevice?>
) {
    val ctx = LocalContext.current

    Column(
        Modifier
            .padding(top = 100.dp)
            .fillMaxSize()
    ) {
        val cMac = vm.mac.value
        val cWeightStr = vm.weight.value
        val cvState = vm.vState.value

        Box(
            Modifier
                .fillMaxWidth()
                .height(30.dp)
        ) {
            Text(
                text = "Mac: $cMac",
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
        Row(
            Modifier.padding(top = 10.dp, bottom = 10.dp)
        ) {
            Text(
                text = "单位:",
                Modifier
                    .align(CenterVertically)
                    .padding(start = 20.dp, end = 10.dp),
                textAlign = TextAlign.Center
            )
            LazyRow(content = {
                item {
                    Row() {
                        vm.supportUnitList.value.forEach {
                            Button(
                                onClick = {
                                    if (device.value != null) QNKitchenScalePlugin.setDeviceUnit(
                                        device.value,
                                        it
                                    )
                                },
                                Modifier
                                    .width(120.dp)
                                    .height(40.dp),
                                shape = RectangleShape,
                                border = BorderStroke(
                                    1.dp,
                                    if (it == vm.curUnit.value) Color.Blue else Color.LightGray
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    Color.White,
                                    Color.Gray,
                                    Color.White,
                                    Color.Gray
                                )
                            ) {
                                Text(
                                    text = createKitchenScaleUnitStr(it),
                                    Modifier
                                        .padding(start = 10.dp, end = 10.dp),
                                    color = if (it == vm.curUnit.value) Color.Blue else Color.Gray,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            })
        }

        Box(
            Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier
                    .height(200.dp)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = if (TextUtils.isEmpty(vm.weight.value)) "--" else QNKitchenScalePlugin.getWeightWithUnit(
                        vm.curUnit.value,
                        vm.weight.value,
                        vm.curNumberType.value
                    ),
                    Modifier
                        .align(Alignment.Top)
                        .padding(top = 50.dp),
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center

                )
                Text(
                    text = createKitchenScaleUnitStr(vm.curUnit.value),
                    Modifier
                        .align(Alignment.Top)
                        .padding(start = 5.dp, top = 70.dp)
                )
            }
            Column(
                Modifier
                    .padding(top = 10.dp)
                    .align(Alignment.TopEnd)
            ) {
                if (vm.isOverWeight.value) {
                    Text(
                        text = "超载",
                        Modifier
                            .height(40.dp)
                            .padding(end = 10.dp)
                    )
                }
                if (vm.isPeel.value) {
                    Text(
                        text = "已去皮",
                        Modifier
                            .height(40.dp)
                            .padding(end = 10.dp)
                    )
                }
                Text(
                    text = if (vm.isSteady.value) "稳定" else "不稳定",
                    Modifier
                        .height(40.dp)
                        .padding(end = 10.dp)
                )
            }
        }
        Button(
            onClick = {
                if (device.value != null && device.value!!.deviceSupportShelling) {
                    QNKitchenScalePlugin.setDeviceShelling(device.value)
                }
            },
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(3.dp),
            border = BorderStroke(1.dp, Color.Gray),
            colors = ButtonDefaults.buttonColors(
                Color.White,
                Color.Gray,
                Color.LightGray,
                Color.Gray
            ),
        ) {
            Text(text = "去皮")
        }
    }
}

class KitchenScaleViewModel : ViewModel() {

    enum class MeasureState {
        CONNECT, CONNECTING, DISCONNECT
    }

    var mac: MutableState<String> = mutableStateOf("")
    var timestamp: MutableState<String> = mutableStateOf("")
    var weight: MutableState<String> = mutableStateOf("")
    var supportUnitList: MutableState<MutableList<QNWeightUnit>> =
        mutableStateOf(mutableListOf(QNWeightUnit.UNIT_G))
    var curUnit: MutableState<QNWeightUnit> = mutableStateOf(QNWeightUnit.UNIT_G)
    var curNumberType: MutableState<NumberType> = mutableStateOf(NumberType.TYPE_ONE_DECIMAL)
    var isPeel: MutableState<Boolean> = mutableStateOf(false)
    var isOverWeight: MutableState<Boolean> = mutableStateOf(false)
    var isSteady: MutableState<Boolean> = mutableStateOf(false)

    var vState: MutableState<MeasureState> = mutableStateOf(MeasureState.DISCONNECT)
}