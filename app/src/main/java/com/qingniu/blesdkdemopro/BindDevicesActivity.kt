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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.input.KeyboardType
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
import com.qingniu.blesdkdemopro.ui.widget.SelectUnitItem
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.DemoBleUtils
import com.qingniu.qnplugin.QNPlugin
import com.qingniu.qnplugin.model.QNWeightUnit
import com.qingniu.qnscaleplugin.QNScalePlugin
import com.qingniu.qnscaleplugin.QNScaleWiFiMp
import com.qingniu.qnscaleplugin.QNUserScaleMp
import com.qingniu.qnscaleplugin.listener.*
import com.qingniu.qnscaleplugin.model.*

class BindDevicesActivity : ComponentActivity() {
    // Is connect deivce
    private var mIsConnecting = false
    // 是否已经连接设备
    private var mIsConnected = false

    companion object {
        const val TAG = "BindDevices"
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, BindDevicesActivity::class.java)
        }
    }
    val dao by lazy {
        DemoDataBase.getInstance(this).deviceUserDao()
    }

    val mDeviceUsers by lazy {
        mutableStateOf(dao.getAllDeviceUser())
    }

    lateinit var mViewModel: BindDeviceViewModel

    private val mReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, i: Intent?) {
            when(i?.action){
                "update_bind_devices" -> {
                    mDeviceUsers.value = dao.getAllDeviceUser()
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
                    val ctx = LocalContext.current

                    val deviceUsers = remember {
                        mutableStateOf(dao.getAllDeviceUser())
                    }
                    mViewModel = viewModel()
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BgGrey)
                    ) {
                        TitleBar("Bind Devices List", false)
                        Column(
                            Modifier
                                .padding(top = 60.dp)
                                .fillMaxSize()
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                            ) {
                                Text(
                                    text = "AppId: " + QNPlugin.getInstance(this@BindDevicesActivity).appId,
                                    Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 20.dp)
                                )
                            }

                            Column(
                                Modifier.padding(top = 20.dp, bottom = 20.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
//                            Text(
//                                text = " List", fontSize = 16.sp,
//                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
//                            )
//                            UserList(this@UserSettingActivity, users = users.value)
                                LazyColumn(){
                                    itemsIndexed(items = mDeviceUsers.value){_, item ->
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
                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight()
                                                    .background(Color.White)
                                            ) {
                                                Text(
                                                    text = "mac: " + item.mac,
                                                    Modifier
                                                        .align(Alignment.CenterStart)
                                                        .padding(start = 10.dp, end = 10.dp)
                                                )
                                                if(item.isSupportUser){
                                                    Image(
                                                        //设置图片资源文件
                                                        painter = painterResource(id = R.drawable.person),
                                                        contentDescription = null,
                                                        Modifier
                                                            .size(160.dp, 20.dp)
                                                            .align(Alignment.CenterEnd)
                                                            .padding(end = 140.dp),
                                                        //设置图片颜色过滤
//                                                    colorFilter = ColorFilter.tint(color = Color.Red, BlendMode.Color),
                                                        //设置图片裁剪方式
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }

                                                if(item.isSupportWifi){
                                                    Image(
                                                        //设置图片资源文件
                                                        painter = painterResource(id = R.drawable.wifi),
                                                        contentDescription = null,
                                                        Modifier
                                                            .size(130.dp, 20.dp)
                                                            .align(Alignment.CenterEnd)
                                                            .padding(end = 110.dp),
                                                        //设置图片颜色过滤
//                                                    colorFilter = ColorFilter.tint(color = Color.Red, BlendMode.Color),
                                                        //设置图片裁剪方式
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }

                                                Button({
                                                    val intent = Intent()
                                                    intent.action = UserConstant.ACTION_DELETE_INDEX_USER
                                                    intent.putExtra(UserConstant.DELETE_USER, item)
                                                    LocalBroadcastManager.getInstance(this@BindDevicesActivity).sendBroadcast(intent)

                                                },
                                                    Modifier
                                                        .align(Alignment.CenterEnd)
                                                        .padding(end = 10.dp)
                                                ){
                                                    Text(
                                                        text = "Unbind", fontSize = 14.sp,
                                                    )
                                                }

                                            }
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
                            }

                        }
                    }
                }
            }
        }
        init()
    }

    private fun init(){
        val filter = IntentFilter("update_bind_devices")
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
    }
}

class BindDeviceViewModel : ViewModel() {
    var userId: MutableState<String> = mutableStateOf("")
    var mac: MutableState<String> = mutableStateOf("")
    var isSupprtUser: MutableState<String> = mutableStateOf("")
    var isSupprtWifi: MutableState<String> = mutableStateOf("")
}
