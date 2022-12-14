package com.qingniu.blesdkdemopro.db.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author: hyr
 * @Date: 2022/8/14 20:31
 * @Description:
 */
@Entity(tableName = "BPMACHINE_SETTING")
class BPMachineSetting {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "UNIT")
    lateinit var unit: String

    @ColumnInfo(name = "VOLUME")
    lateinit var volume: String

    @ColumnInfo(name = "STANDARD")
    lateinit var standard: String

    @ColumnInfo(name = "LANGUAGE")
    lateinit var language: String

    override fun toString(): String {
        return "BPMachineSetting(id=$id, unit='$unit', volume='$volume', standard='$standard', language='$language')"
    }
}