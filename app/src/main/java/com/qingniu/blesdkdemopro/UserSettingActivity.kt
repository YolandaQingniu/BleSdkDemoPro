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
import com.qingniu.blesdkdemopro.constant.DemoUnit
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.widget.SelectUnitItem
import com.qingniu.blesdkdemopro.ui.widget.TitleBar

class UserSettingActivity : ComponentActivity() {

    companion object {
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, UserSettingActivity::class.java)
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
                    val dao = DemoDataBase.getInstance(ctx).userDao()

                    val age = remember {
                        mutableStateOf(dao.getUser().age)
                    }
                    val gender = remember {
                        mutableStateOf(dao.getUser().gender)
                    }
                    Column(
                        Modifier
                            .background(BgGrey)
                            .fillMaxSize()
                    ) {
                        TitleBar(title = "Age")
                        Column(
                            Modifier.padding(top = 20.dp)
                        ) {
                            Text(
                                text = "Weight Unit", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            TextField(value = age.value.toString(), onValueChange = {
                                if (!TextUtils.isEmpty(it)) {
                                    age.value = it.toInt()

                                } else {
                                    age.value = 0
                                }
                                val user = dao.getUser().apply {
                                    this.age = age.value
                                }
                                dao.update(user)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Column(Modifier.padding(top = 20.dp)) {
                            Text(
                                text = "Gender", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            SelectUnitItem(unit = DemoUnit.MALE.showName, checkState = { gender.value == DemoUnit.MALE.showName }) {
                                val user = dao.getUser().apply {
                                    this.gender = DemoUnit.MALE.showName
                                }
                                dao.update(user)
                                gender.value = user.gender
                            }
                            SelectUnitItem(unit = DemoUnit.FEMALE.showName, checkState = { gender.value == DemoUnit.FEMALE.showName }) {
                                val user = dao.getUser().apply {
                                    this.gender = DemoUnit.FEMALE.showName
                                }
                                dao.update(user)
                                gender.value = user.gender
                            }
                        }
                    }
                }
            }
        }
    }
}