package com.qingniu.blesdkdemopro.db.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author: hyr
 * @Date: 2022/8/14 20:31
 * @Description:
 */
@Entity(tableName = "UNIT_SETTING")
class UnitSetting {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "WEIGHT_UNIT", defaultValue = "KG")
    lateinit var weightUnit: String

    @ColumnInfo(name = "LENGTH_UNIT", defaultValue = "CM")
    lateinit var lengthUnit: String
}