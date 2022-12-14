package com.qingniu.blesdkdemopro.db.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author: hyr
 * @Date: 2022/8/14 20:31
 * @Description:
 */
@Entity(tableName = "USER")
class User {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "USERID", defaultValue = "")
    var userId: String = ""

    @ColumnInfo(name = "GENDER", defaultValue = "MALE")
    lateinit var gender: String

    @ColumnInfo(name = "AGE", defaultValue = "30")
    var age: Int = 30

    @ColumnInfo(name = "HEIGHT", defaultValue = "180")
    var height: Int = 180

    @ColumnInfo(name = "IS_CURRENT", defaultValue = "false")
    var isCurrent: Boolean = false

}