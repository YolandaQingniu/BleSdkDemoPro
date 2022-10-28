package com.qingniu.blesdkdemopro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.blesdkdemopro.util.BlePermissionCenter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleSdkDemoProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BgGrey)
                    ) {
                        TitleBar("Device Type", false)
                        Column(Modifier.align(Alignment.Center)) {
                            val ctx = LocalContext.current
                            Card(Modifier.clickable {
                                ctx.startActivity(HeightMeasureActivity.getCallIntent(ctx))
                            }) {
                                Box(
                                    Modifier
                                        .width(200.dp)
                                        .height(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Height Scale",
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                            Card(Modifier.padding(top = 20.dp).clickable {
                                ctx.startActivity(TapeMeasureActivity.getCallIntent(ctx))
                            }) {
                                Box(
                                    Modifier
                                        .width(200.dp)
                                        .height(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Ruler",
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            Card(Modifier.padding(top = 20.dp).clickable {
                                ctx.startActivity(QNScaleMeasureActivity.getCallIntent(ctx))
                            }) {
                                Box(
                                    Modifier
                                        .width(200.dp)
                                        .height(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "QNScale",
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        testBle()
    }

    fun testBle() {
        BlePermissionCenter.verifyPermissions(this)
    }
}