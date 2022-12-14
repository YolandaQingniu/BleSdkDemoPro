package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.widget.TitleBar

class WifiSettingActivity : ComponentActivity() {

    companion object {
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, WifiSettingActivity::class.java)
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
                    val dao = DemoDataBase.getInstance(ctx).wifiInfoDao()

                    val ssid = remember {
                        mutableStateOf(dao.getWifiInfo().ssid)
                    }
                    val password = remember {
                        mutableStateOf(dao.getWifiInfo().password)
                    }
                    val serverUrl = remember {
                        mutableStateOf(dao.getWifiInfo().serverUrl)
                    }
                    Column(
                        Modifier
                            .background(BgGrey)
                            .fillMaxSize()
                    ) {
                        TitleBar(title = "Wifi Setting")
                        Column(
                            Modifier.padding(top = 20.dp)
                        ) {
                            Text(
                                text = "ssid", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            TextField(value = ssid.value.toString(), onValueChange = {
                                if (!TextUtils.isEmpty(it)) {
                                    ssid.value = it

                                } else {
                                    ssid.value = ""
                                }
                                val wifiInfo = dao.getWifiInfo().apply {
                                    this.ssid = ssid.value
                                    this.password = password.value
                                    this.serverUrl = serverUrl.value
                                }
                                dao.update(wifiInfo)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                            )
                        }
                        Column(Modifier.padding(top = 20.dp)) {
                            Text(
                                text = "password", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            TextField(value = password.value.toString(), onValueChange = {
                                if (!TextUtils.isEmpty(it)) {
                                    password.value = it

                                } else {
                                    password.value = ""
                                }
                                val wifiInfo = dao.getWifiInfo().apply {
                                    this.ssid = ssid.value
                                    this.password = password.value
                                    this.serverUrl = serverUrl.value
                                }
                                dao.update(wifiInfo)
                            },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                        }
                        Column(Modifier.padding(top = 20.dp)) {
                            Text(
                                text = "serverUrl", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            TextField(value = serverUrl.value.toString(), onValueChange = {
                                if (!TextUtils.isEmpty(it)) {
                                    serverUrl.value = it

                                } else {
                                    serverUrl.value = ""
                                }
                                val wifiInfo = dao.getWifiInfo().apply {
                                    this.ssid = ssid.value
                                    this.password = password.value
                                    this.serverUrl = serverUrl.value
                                }
                                dao.update(wifiInfo)
                            },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                            )
                        }
                    }
                }
            }
        }
    }
}