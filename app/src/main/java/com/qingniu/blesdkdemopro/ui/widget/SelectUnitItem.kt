package com.qingniu.blesdkdemopro.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingniu.blesdkdemopro.ui.theme.DividerGrey

/**
 *@author: hyr
 *@date: 2022/9/14 20:40
 *@desc:
 */

@Composable
fun SelectUnitItem(
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