package com.qingniu.blesdkdemopro.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.qingniu.blesdkdemopro.db.dao.*
import com.qingniu.blesdkdemopro.db.table.*
import com.qingniu.qnbpmachineplugin.QNBPMachineLanguage
import com.qingniu.qnbpmachineplugin.QNBPMachineStandard
import com.qingniu.qnbpmachineplugin.QNBPMachineUnit
import com.qingniu.qnbpmachineplugin.QNBPMachineVolume

/**
 * @Author: hyr
 * @Date: 2022/8/14 20:42
 * @Description:
 */
@Database(entities = [UnitSetting::class, User::class, WifiInfo::class, DeviceUser::class,BPMachineSetting::class], version = 1)
abstract class DemoDataBase : RoomDatabase() {
    abstract fun unitSettingDao(): UnitSettingDao
    abstract fun userDao(): UserDao
    abstract fun wifiInfoDao(): WifiInfoDao
    abstract fun deviceUserDao(): DeviceUserDao
    abstract fun bpMachineSettingDao(): BPMachineSettingDao

    companion object {
        @Volatile
        private var sInstance: DemoDataBase? = null
        private const val DATA_BASE_NAME = "demo.db"

        @JvmStatic
        fun getInstance(context: Context): DemoDataBase {
            if (sInstance == null) {
                synchronized(DemoDataBase::class.java) {
                    if (sInstance == null) {
                        sInstance = createInstance(context)
                        if (null != sInstance){
                            val defaultUnit = UnitSetting().apply {
                                weightUnit = "KG"
                                lengthUnit = "CM"
                            }

                            val defaultUser =User().apply {
                                gender = "MALE"
                                age = 30
                                height = 180
                                isCurrent = true
                                userId = "user${System.currentTimeMillis()}"
                            }

                            val defaultWifiInfo = WifiInfo().apply {
                                ssid = "K30P"
                                password = "22223333"
                                serverUrl = "http://wifi.yolanda.hk:80/wifi_api/wsps?device_type=7&code="
                            }

                            if(sInstance!!.unitSettingDao().getUnitSetting() == null) sInstance!!.unitSettingDao().insert(defaultUnit)
                            if(sInstance!!.userDao().getUser() == null) sInstance!!.userDao().insert(defaultUser)
                            if(sInstance!!.wifiInfoDao().getWifiInfo() == null) sInstance!!.wifiInfoDao().insert(defaultWifiInfo)

                            if (null == sInstance!!.bpMachineSettingDao().getBPMachineSetting()){
                                sInstance!!.bpMachineSettingDao().insert(BPMachineSetting().apply {
                                    unit = QNBPMachineUnit.MMHG.toString()
                                    volume = QNBPMachineVolume.THIRD_LEVEL.toString()
                                    standard = QNBPMachineStandard.CHINA.toString()
                                    language = QNBPMachineLanguage.CHINESE.toString()
                                })
                            }
                        }
                    }
                }
            }
            return sInstance!!
        }

        private fun createInstance(context: Context): DemoDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                DemoDataBase::class.java,
                DATA_BASE_NAME)
                .allowMainThreadQueries()//todo hyr 后续需移除主线程操作数据库 目前临时调试
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}