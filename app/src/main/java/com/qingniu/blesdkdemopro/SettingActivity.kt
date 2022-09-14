package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.CT4
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.widget.TitleBar

class SettingActivity : ComponentActivity() {

    companion object {
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, SettingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleSdkDemoProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background,
                ) {
                    Column(
                        Modifier
                            .background(BgGrey)
                            .fillMaxSize()
                    ) {
                        TitleBar(title = "Setting")
                        Settings()
                    }
                }
            }
        }
    }
}

@Composable
fun Settings() {
    Column(
        Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .background(Color.White)
    ) {
        val ctx = LocalContext.current
        SettingItem(text = "Unit Setting", true) {
            ctx.startActivity(UnitSelectActivity.getCallIntent(ctx))
        }

        SettingItem(text = "User Setting", false) {
            ctx.startActivity(UserSettingActivity.getCallIntent(ctx))
        }
    }
}

@Composable
fun SettingItem(text: String, showDivider: Boolean = true, action: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable {
                action.invoke()
            }
    ) {
        Text(
            text = text,
            Modifier
                .padding(20.dp, 10.dp, 0.dp, 10.dp)
                .align(Alignment.CenterStart),
            fontSize = 16.sp
        )
        Text(
            text = ">",
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
            fontSize = 14.sp,
            color = CT4
        )
        if (showDivider) {
            Divider(
                color = DividerGrey,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .height(1.dp)
                    .fillMaxWidth()
                    .padding(start = 10.dp)
            )
        }
    }
}