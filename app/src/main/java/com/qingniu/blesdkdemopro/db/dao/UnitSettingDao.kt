package com.qingniu.blesdkdemopro.db.dao

import androidx.room.*
import com.qingniu.blesdkdemopro.db.table.UnitSetting

/**
 * @Author: hyr
 * @Date: 2022/8/14 20:40
 * @Description:
 */


@Dao
interface UnitSettingDao {
    @Insert
    fun insert(unitSetting: UnitSetting)

    @Delete
    fun delete(unitSetting: UnitSetting)

    @Update
    fun update(unitSetting: UnitSetting)

    @Query("SELECT * FROM UNIT_SETTING WHERE id = 1")
    fun getUnitSetting(): UnitSetting
}
