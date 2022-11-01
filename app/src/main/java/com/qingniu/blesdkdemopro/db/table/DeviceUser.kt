package com.qingniu.blesdkdemopro.db.table

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * create by qzx
 * 2022/10/26: 11:12 am
 * desc:
 */
@Entity(tableName = "DEVICE_USER")
class DeviceUser() : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "mac", defaultValue = "")
    var mac: String = ""

    @ColumnInfo(name = "userId", defaultValue = "")
    var userId: String = ""

    @ColumnInfo(name = "index", defaultValue = "0")
    var index: Int = 0

    @ColumnInfo(name = "key", defaultValue = "0")
    var key: Int = 0

    @ColumnInfo(name = "isVisitorMode", defaultValue = "false")
    var isVisitorMode: Boolean = false

    @ColumnInfo(name = "isSupportUser", defaultValue = "false")
    var isSupportUser: Boolean = false

    @ColumnInfo(name = "isSupportWifi", defaultValue = "false")
    var isSupportWifi: Boolean = false

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        mac = parcel.readString().toString()
        userId = parcel.readString().toString()
        index = parcel.readInt()
        key = parcel.readInt()
        isVisitorMode = parcel.readByte() != 0.toByte()
        isSupportUser = parcel.readByte() != 0.toByte()
        isSupportWifi = parcel.readByte() != 0.toByte()
    }

    override fun toString(): String {
        return "DeviceUser{id = $id, mac = $mac, userId = $userId, index = $index, key = $key, isVisitorMode = $isVisitorMode, isSupportUser = $isSupportUser, isSupportWifi = $isSupportWifi}"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(mac)
        parcel.writeString(userId)
        parcel.writeInt(index)
        parcel.writeInt(key)
        parcel.writeByte(if (isVisitorMode) 1 else 0)
        parcel.writeByte(if (isSupportUser) 1 else 0)
        parcel.writeByte(if (isSupportWifi) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeviceUser> {
        override fun createFromParcel(parcel: Parcel): DeviceUser {
            return DeviceUser(parcel)
        }

        override fun newArray(size: Int): Array<DeviceUser?> {
            return arrayOfNulls(size)
        }
    }
}