package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.widget.TitleBar

class UnitSelectActivity : ComponentActivity() {

    companion object {
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, UnitSelectActivity::class.java)
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
                    val dao = DemoDataBase.getInstance(ctx).unitSettingDao()
                    val weightUnit = remember {
                        mutableStateOf(dao.getUnitSetting().weightUnit)
                    }
                    val lengthUnit = remember {
                        mutableStateOf(dao.getUnitSetting().lengthUnit)
                    }
                    Column(
                        Modifier
                            .background(BgGrey)
                            .fillMaxSize()
                    ) {
                        TitleBar(title = "Select Unit")
                        Column(
                            Modifier.padding(top = 20.dp)
                        ) {
                            Text(
                                text = "Weight Unit", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            UnitItem(unit = "KG", checkState = { weightUnit.value == "KG" }) {
                                val unit = dao.getUnitSetting().apply {
                                    this.weightUnit = "KG"
                                }
                                dao.update(unit)
                                weightUnit.value = unit.weightUnit
                            }
                            UnitItem(unit = "LB", checkState = { weightUnit.value == "LB" }) {
                                val unit = dao.getUnitSetting().apply {
                                    this.weightUnit = "LB"
                                }
                                dao.update(unit)
                                weightUnit.value = unit.weightUnit
                            }
                            UnitItem(unit = "ST+LB", checkState = { weightUnit.value == "ST+LB" }) {
                                val unit = dao.getUnitSetting().apply {
                                    this.weightUnit = "ST+LB"
                                }
                                dao.update(unit)
                                weightUnit.value = unit.weightUnit
                            }
                            UnitItem(unit = "ST", checkState = { weightUnit.value == "ST" }) {
                                val unit = dao.getUnitSetting().apply {
                                    this.weightUnit = "ST"
                                }
                                dao.update(unit)
                                weightUnit.value = unit.weightUnit
                            }
                            UnitItem(unit = "斤", checkState = { weightUnit.value == "斤" }) {
                                val unit = dao.getUnitSetting().apply {
                                    this.weightUnit = "斤"
                                }
                                dao.update(unit)
                                weightUnit.value = unit.weightUnit
                            }
                        }
                        Column(Modifier.padding(top = 20.dp)) {
                            Text(
                                text = "Length Unit", fontSize = 16.sp,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )
                            UnitItem(unit = "CM", checkState = { lengthUnit.value == "CM" }) {
                                val unit = dao.getUnitSetting().apply {
                                    this.lengthUnit = "CM"
                                }
                                dao.update(unit)
                                lengthUnit.value = unit.lengthUnit
                            }
                            UnitItem(unit = "FT", checkState = { lengthUnit.value == "FT" }) {
                                val unit = dao.getUnitSetting().apply {
                                    this.lengthUnit = "FT"
                                }
                                dao.update(unit)
                                lengthUnit.value = unit.lengthUnit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnitItem(
    unit: String,
    showDivider: Boolean = true,
    checkState: () -> Boolean,
    clickAction: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
            .clickable {
                clickAction.invoke()
            }
    ) {
        if (showDivider) {
            Divider(
                color = DividerGrey,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .height(1.dp)
                    .fillMaxWidth()
            )
        }
        Text(
            text = unit,
            Modifier
                .padding(10.dp, 10.dp, 0.dp, 10.dp)
                .align(Alignment.CenterStart),
            fontSize = 16.sp
        )
        if (checkState.invoke()) {
            Icon(
                imageVector = Icons.Sharp.Check,
                contentDescription = "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
            )
        }
    }
}
