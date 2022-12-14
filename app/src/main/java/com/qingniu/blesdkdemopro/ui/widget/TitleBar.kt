package com.qingniu.blesdkdemopro.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingniu.blesdkdemopro.SettingActivity

/**
 *@author: hyr
 *@date: 2022/8/11 16:12
 *@desc:
 */

@Composable
fun TitleBar(title: String, showSetting: Boolean = false) {
    Box(
        Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Text(text = title, Modifier.align(Alignment.Center), fontSize = 16.sp)
        if (showSetting) {
            val ctx = LocalContext.current
            Text(
                text = "Setting",
                Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(
                        indication = null,
                        interactionSource = MutableInteractionSource()
                    ) { ctx.startActivity(SettingActivity.getCallIntent(ctx)) }
                    .padding(end = 20.dp),
                fontSize = 16.sp
            )
        }
    }
}


enum class SettingAction {
    FULL, DISCONNECT, MEASURE_END, MEASURE_FAIL
}

@Preview(showSystemUi = false, showBackground = false)
@Composable
fun PreviewTitleBar() {
    TitleBar("测试", true)
}