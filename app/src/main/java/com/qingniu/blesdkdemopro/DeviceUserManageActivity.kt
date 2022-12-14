package com.qingniu.blesdkdemopro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.qingniu.blesdkdemopro.constant.UserConstant
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.qnplugin.QNPlugin

class DeviceUserManageActivity : ComponentActivity() {

    companion object {
        const val TAG = "DeviceUserManage"
        fun getCallIntent(ctx: Context, mac: String): Intent {
            return Intent(ctx, DeviceUserManageActivity::class.java).putExtra("mac", mac)
        }
    }
    val dao by lazy {
        DemoDataBase.getInstance(this).deviceUserDao()
    }

    val mViewModels: ArrayList<DeviceIndexViewModel> by lazy {
        val list = arrayListOf<DeviceIndexViewModel>()
        for(index in 0 until 8){
            val model = DeviceIndexViewModel()
            model.index.value = index + 1
            list.add(model)
        }
        list
    }

    val mIsSelectedAll = mutableStateOf(false)

    private val mReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, i: Intent?) {
            when(i?.action){
                "update_bind_devices" -> {

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
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BgGrey)
                    ) {
                        TitleBar("Device User Manage", false)
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
                                    text = "mac: " + if(intent != null && !TextUtils.isEmpty(intent.getStringExtra("mac"))) intent.getStringExtra("mac")!! else "",
                                    Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 10.dp)
                                )
                                Text(
                                    text = "AppId: " + QNPlugin.getInstance(this@DeviceUserManageActivity).appId,
                                    Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 10.dp)
                                )
                            }

                            Column(
                                Modifier.padding(top = 20.dp, bottom = 20.dp),
                                horizontalAlignment = Alignment.Start
                            ) {

                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Clear All Scale User",
                                        Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(start = 10.dp, end = 20.dp)
                                    )

                                    Checkbox(
                                        checked = mIsSelectedAll.value,
                                        onCheckedChange = { selected ->
                                            mIsSelectedAll.value = !mIsSelectedAll.value
                                            mViewModels.forEach {
                                                it.isSelected.value = selected
                                            }
                                        },
                                        Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(start = 10.dp, end = 10.dp)
                                    )
                                }

                                Row(
                                    Modifier.align(Alignment.CenterHorizontally).padding(start = 10.dp, end = 5.dp)
                                ) {
                                    Text(
                                        text = "User index:",
                                        Modifier
                                            .align(Alignment.CenterVertically)
                                    )
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 60.dp)
                                    ){
                                        items(mViewModels) { item ->
                                            Box(
                                                Modifier
                                                    .padding(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 5.dp)
                                                    .align(Alignment.CenterVertically)
                                            ) {
                                                Text(
                                                    text = "${item.index.value}",
                                                    Modifier
                                                        .width(60.dp)
                                                        .height(60.dp)
                                                        .background(if (item.isSelected.value) Color(0xFFAAFFAA) else Color.LightGray)
                                                        .clickable {
                                                            item.isSelected.value = !item.isSelected.value
                                                            checkIsSelectAll()
                                                        },
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp)
                                ){
                                    Button({
                                        val list = arrayListOf<Int>()
                                        mViewModels.forEach {
                                            if(it.isSelected.value){
                                                list.add(it.index.value)
                                            }
                                        }
                                        val i = Intent(UserConstant.ACTION_DELETE_INDEX_USER)
                                        i.putExtra(UserConstant.DELETE_USER_INDEX, list)
                                        LocalBroadcastManager.getInstance(ctx).sendBroadcast(i)
                                    },
                                        Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .padding(start = 20.dp, end = 20.dp)
                                    ){
                                        Text(
                                            text = "Delete Scale User", fontSize = 14.sp,
                                        )
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

    private fun checkIsSelectAll(){
        mViewModels.forEach {
            if(!it.isSelected.value) {
                mIsSelectedAll.value = false
                return
            }
        }
        mIsSelectedAll.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
    }
}

class DeviceIndexViewModel : ViewModel() {
    var isSelected: MutableState<Boolean> = mutableStateOf(false)
    var index: MutableState<Int> = mutableStateOf(0)
}

