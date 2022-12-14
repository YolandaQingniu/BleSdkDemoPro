package com.qingniu.blesdkdemopro.db.dao

import androidx.room.*
import com.qingniu.blesdkdemopro.db.table.DeviceUser
import com.qingniu.blesdkdemopro.db.table.User

/**
 * @Author: qzx
 * @Date: 2022/10/26 11:29
 * @Description:
 */

@Dao
interface DeviceUserDao {
    @Insert
    fun insert(deviceUser: DeviceUser)

    @Delete
    fun delete(deviceUser: DeviceUser)

    @Update
    fun update(deviceUser: DeviceUser)

    @Query("SELECT * FROM DEVICE_USER WHERE userId = :selectUserId")
    fun getDeviceUser(selectUserId: String): List<DeviceUser>

    @Query("SELECT * FROM DEVICE_USER WHERE mac = :mac")
    fun getMacDevice(mac: String): List<DeviceUser>

    @Query("SELECT * FROM DEVICE_USER")
    fun getAllDeviceUser(): List<DeviceUser>
}
