package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingniu.blesdkdemopro.constant.DemoUnit
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.widget.SelectConfigItem
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
                    val height = remember {
                        mutableStateOf(dao.getUser().height)
                    }
                    val users = remember {
                        mutableStateOf(dao.getAllUser())
                    }
                    Column(
                        Modifier
                            .background(BgGrey)
                            .fillMaxSize()
                    ) {
                        TitleBar(title = "User Setting")
                        Column(
                            Modifier.padding(top = 20.dp)
                        ) {
                            Text(
                                text = "Age", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            TextField(value = age.value.toString(), onValueChange = {
                                if (!TextUtils.isEmpty(it)) {
                                    age.value = it.toInt()

                                } else {
                                    age.value = 0
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Column(
                            Modifier.padding(top = 20.dp)
                        ) {
                            Text(
                                text = "Height", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            TextField(value = height.value.toString(), onValueChange = {
                                if (!TextUtils.isEmpty(it)) {
                                    height.value = it.toInt()
                                } else {
                                    height.value = 0
                                }
                            },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Column(Modifier.padding(top = 20.dp)) {
                            Text(
                                text = "Gender", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            SelectConfigItem(unit = DemoUnit.MALE.showName, checkState = { gender.value == DemoUnit.MALE.showName }) {
                                val user = dao.getUser().apply {
                                    this.gender = DemoUnit.MALE.showName
                                }
                                gender.value = user.gender
                            }
                            SelectConfigItem(unit = DemoUnit.FEMALE.showName, checkState = { gender.value == DemoUnit.FEMALE.showName }) {
                                val user = dao.getUser().apply {
                                    this.gender = DemoUnit.FEMALE.showName
                                }
                                gender.value = user.gender
                            }
                        }

                        Column(
                            Modifier.padding(top = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {
                                val user = dao.getUser().apply {
                                    var tempId = 0L
                                    dao.getAllUser().forEach{
                                        if(it.id >= tempId){
                                            tempId = it.id + 1
                                        }
                                    }
                                    this.id = tempId
                                    this.age = age.value
                                    this.gender = gender.value
                                    this.height = height.value
                                    this.isCurrent = false
                                    this.userId = "user${System.currentTimeMillis()}"
                                }
                                dao.insert(user)
                                users.value = dao.getAllUser()
                            }) {
                                Text(
                                    text = "Create New", fontSize = 16.sp,
                                )
                            }
                        }
                        Column(
                            Modifier.padding(top = 20.dp, bottom = 20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "User List", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
//                            UserList(this@UserSettingActivity, users = users.value)
                            LazyColumn(){
                                itemsIndexed(items = users.value){_, item ->
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
                                        SelectConfigItem(unit = "  ${item.id}          ${item.gender}     ${item.age}years    ${item.height}cm", checkState = { item.isCurrent }) {
                                            users.value.forEach {
                                                it.isCurrent = false
                                                if(it.id == item.id){
                                                    it.isCurrent = true
                                                }
                                                DemoDataBase.getInstance(ctx).userDao().update(it)
                                                users.value = dao.getAllUser()
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
}