package com.qingniu.blesdkdemopro.db.dao

import androidx.room.*
import com.qingniu.blesdkdemopro.db.table.User

/**
 * @Author: hyr
 * @Date: 2022/8/14 20:40
 * @Description:
 */

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Delete
    fun delete(user: User)

    @Update
    fun update(user: User)

    @Query("SELECT * FROM USER WHERE isCurrent")
    fun getUser(): User

    @Query("SELECT * FROM USER")
    fun getAllUser(): List<User>
}
