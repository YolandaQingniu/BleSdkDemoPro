package com.qingniu.blesdkdemopro.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.qingniu.blesdkdemopro.db.dao.UnitSettingDao
import com.qingniu.blesdkdemopro.db.table.UnitSetting

/**
 * @Author: hyr
 * @Date: 2022/8/14 20:42
 * @Description:
 */
@Database(entities = [UnitSetting::class], version = 1)
abstract class DemoDataBase : RoomDatabase() {
    abstract fun unitSettingDao(): UnitSettingDao

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
                            sInstance!!.unitSettingDao().insert(defaultUnit)
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
                .build()
        }
    }
}