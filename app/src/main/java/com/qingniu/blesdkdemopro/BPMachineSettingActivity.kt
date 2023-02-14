package com.qingniu.blesdkdemopro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingniu.blesdkdemopro.db.DemoDataBase
import com.qingniu.blesdkdemopro.ui.theme.BgGrey
import com.qingniu.blesdkdemopro.ui.theme.BleSdkDemoProTheme
import com.qingniu.blesdkdemopro.ui.widget.SelectConfigItem
import com.qingniu.blesdkdemopro.ui.widget.TitleBar
import com.qingniu.qnbpmachineplugin.QNBPMachineLanguage
import com.qingniu.qnbpmachineplugin.QNBPMachineStandard
import com.qingniu.qnbpmachineplugin.QNBPMachineUnit
import com.qingniu.qnbpmachineplugin.QNBPMachineVolume

class BPMachineSettingActivity : ComponentActivity() {

    companion object {
        fun getCallIntent(ctx: Context): Intent {
            return Intent(ctx, BPMachineSettingActivity::class.java)
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
                    Column(
                        Modifier
                            .background(BgGrey)
                    ) {
                        TitleBar(title = "BPMachine Setting")

                        val ctx = LocalContext.current
                        val dao = DemoDataBase.getInstance(ctx).bpMachineSettingDao()
                        val unit = remember {
                            mutableStateOf(dao.getBPMachineSetting().unit)
                        }
                        val volume = remember {
                            mutableStateOf(dao.getBPMachineSetting().volume)
                        }
                        val standard = remember {
                            mutableStateOf(dao.getBPMachineSetting().standard)
                        }
                        val language = remember {
                            mutableStateOf(dao.getBPMachineSetting().language)
                        }

                        SelectConfigList(
                            "Unit",
                            arrayListOf<QNBPMachineUnit>(
                                QNBPMachineUnit.MMHG,
                                QNBPMachineUnit.KPA
                            ),
                            unit
                        ) {
                            val setting = dao.getBPMachineSetting().apply {
                                this.unit = it.toString()
                            }
                            dao.update(setting)
                            unit.value = setting.unit
                        }

                        SelectConfigList(
                            "Volume",
                            arrayListOf<QNBPMachineVolume>(
                                QNBPMachineVolume.MUTE,
                                QNBPMachineVolume.FIRST_LEVEL,
                                QNBPMachineVolume.SECOND_LEVEL,
                                QNBPMachineVolume.THIRD_LEVEL,
                                QNBPMachineVolume.FOURTH_LEVEL,
                                QNBPMachineVolume.FIFTH_LEVEL
                            ),
                            volume
                        ) {
                            val setting = dao.getBPMachineSetting().apply {
                                this.volume = it.toString()
                            }
                            dao.update(setting)
                            volume.value = setting.volume
                        }


                        SelectConfigList(
                            "Standard",
                            arrayListOf<QNBPMachineStandard>(
                                QNBPMachineStandard.CHINA,
                                QNBPMachineStandard.USA,
                                QNBPMachineStandard.EUROPE,
                            ),
                            standard
                        ) {
                            val setting = dao.getBPMachineSetting().apply {
                                this.standard = it.toString()
                            }
                            dao.update(setting)
                            standard.value = setting.standard
                        }

                        SelectConfigList(
                            "Language",
                            arrayListOf<QNBPMachineLanguage>(
                                QNBPMachineLanguage.CHINESE,
                                QNBPMachineLanguage.ENGLISH
                            ),
                            language
                        ) {
                            val setting = dao.getBPMachineSetting().apply {
                                this.language = it.toString()
                            }
                            dao.update(setting)
                            language.value = setting.language
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun <T> SelectConfigList(
    configName: String,
    configList: List<T>,
    checkObject: MutableState<out Any>,
    clickAction: (updateItem: T) -> Unit
) {
    Column(Modifier.padding(top = 14.dp)) {
        Text(
            text = configName, fontSize = 16.sp,
            modifier = Modifier.padding(start = 10.dp, bottom = 6.dp)
        )
        LazyColumn {
            item(configList) {
                repeat(configList.size) {
                    val itemString = configList[it].toString()
                    SelectConfigItem(
                        itemString, true,
                        { checkObject.value == itemString }) {
                        clickAction.invoke(configList[it])
                    }
                }
            }
        }
    }
}