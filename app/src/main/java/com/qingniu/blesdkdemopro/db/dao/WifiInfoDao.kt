package com.qingniu.blesdkdemopro.db.dao

import androidx.room.*
import com.qingniu.blesdkdemopro.db.table.User
import com.qingniu.blesdkdemopro.db.table.WifiInfo

/**
 * @Author: hyr
 * @Date: 2022/8/14 20:40
 * @Description:
 */

@Dao
interface WifiInfoDao {
    @Insert
    fun insert(wifiInfo: WifiInfo)

    @Delete
    fun delete(wifiInfo: WifiInfo)

    @Update
    fun update(wifiInfo: WifiInfo)

    @Query("SELECT * FROM WIFI_INFO WHERE id = 1")
    fun getWifiInfo(): WifiInfo
}
