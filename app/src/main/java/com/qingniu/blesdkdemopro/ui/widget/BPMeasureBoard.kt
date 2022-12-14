package com.qingniu.blesdkdemopro.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qingniu.blesdkdemopro.BPViewModel
import com.qingniu.blesdkdemopro.ui.theme.BgError
import com.qingniu.qnbpmachineplugin.QNBPMachineData
import com.qingniu.qnbpmachineplugin.QNBPMachineMeasureResult
import java.text.SimpleDateFormat

/**
 *@author: hyr
 *@date: 2022/12/14 10:45
 *@desc:
 */

@Composable
fun BPMeasureBoard() {
    val hsvm: BPViewModel = viewModel()
    Column {
        if (null != hsvm.measureError.value && hsvm.measureError.value != QNBPMachineMeasureResult.SUCCESS){
            Box(
                Modifier
                    .height(40.dp)
                    .fillMaxWidth()
                    .background(BgError),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hsvm.measureError.value.toString(),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
        LazyColumn {
            item(hsvm.datalist.value) {
                repeat(hsvm.datalist.value.size) {
                    BPMeasureItem(hsvm.datalist.value[it])
                }
            }
        }
    }
}

@Composable
fun BPMeasureItem(data: QNBPMachineData) {
    val ctx = LocalContext.current
    Card(Modifier.padding(top = 20.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        ) {
            Column {

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "timeStamp",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )
                    Text(
                        text = SimpleDateFormat().format(data.timeStamp.toLong() * 1000L),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp)
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "userIndex",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )
                    Text(
                        text = data.userIndex.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp)
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "unit",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )
                    Text(
                        text = data.unit.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp)
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "systolicBP",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )
                    Text(
                        text = data.systolicBP.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp)
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "diastolicBP",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )
                    Text(
                        text = data.diastolicBP.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp)
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "heartRate",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )
                    Text(
                        text = data.heartRate.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp)
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "resultType",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 20.dp)
                    )
                    Text(
                        text = data.resultType.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = false, showBackground = false)
@Composable
fun PreviewBPMeasureBoard() {
    //BPMeasureBoard()
}