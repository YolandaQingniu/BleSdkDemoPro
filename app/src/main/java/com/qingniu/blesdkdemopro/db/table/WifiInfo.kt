package com.qingniu.blesdkdemopro.db.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * create by qzx
 * 2022/10/9: 5:43 下午
 * desc:
 */
@Entity(tableName = "WIFI_INFO")
class WifiInfo {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "SSID", defaultValue = "")
    var ssid: String = ""

    @ColumnInfo(name = "PASSWORD", defaultValue = "")
    var password: String = ""

    @ColumnInfo(name = "SERVER_URL", defaultValue = "")
    var serverUrl: String = ""
}