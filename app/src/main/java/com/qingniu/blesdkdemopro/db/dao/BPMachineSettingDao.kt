package com.qingniu.blesdkdemopro.db.dao

import androidx.room.*
import com.qingniu.blesdkdemopro.db.table.BPMachineSetting


@Dao
interface BPMachineSettingDao {
    @Insert
    fun insert(bpMachineSetting: BPMachineSetting)

    @Delete
    fun delete(bpMachineSetting: BPMachineSetting)

    @Update
    fun update(bpMachineSetting: BPMachineSetting)

    @Query("SELECT * FROM BPMACHINE_SETTING WHERE id = 1")
    fun getBPMachineSetting(): BPMachineSetting
}
