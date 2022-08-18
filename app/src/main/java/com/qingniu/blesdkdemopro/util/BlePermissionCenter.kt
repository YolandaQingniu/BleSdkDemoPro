package com.qingniu.blesdkdemopro.util

import android.Manifest
import android.app.Activity
import android.os.Build
import android.util.Log
import com.tbruyelle.rxpermissions2.RxPermissions

/**
 *@author: hyr
 *@date: 2022/3/22 10:45
 *@desc:
 */
object BlePermissionCenter {

    private val PERMISSIONS_LIST = arrayOf(
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_FINE_LOCATION"
    )

    //安卓12需要申请的权限列表
    private val ANDROID_S_PERMISSIONS_LIST = arrayOf(
        "android.permission.BLUETOOTH_SCAN",
        "android.permission.BLUETOOTH_ADVERTISE",
        "android.permission.BLUETOOTH_CONNECT"
    )

    private fun getNeedPermissionList(activity: Activity): Array<String> {
        return if (activity.applicationInfo.targetSdkVersion > 30 && Build.VERSION.SDK_INT > 30) {
            ANDROID_S_PERMISSIONS_LIST
        } else {
            PERMISSIONS_LIST
        }
    }

    fun verifyPermissions(activity: Activity) {
        val rxPermissions = RxPermissions(activity)

        //需要相关申请权限
        val coarseLocationGranted =
            rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val coarseFindGranted = rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)

        val isRunOnAndroidS =
            activity.applicationInfo.targetSdkVersion > 30 && Build.VERSION.SDK_INT > 30

        if (isRunOnAndroidS) {
            val bleScanGranted = rxPermissions.isGranted("android.permission.BLUETOOTH_SCAN")
            val bleConnectGranted = rxPermissions.isGranted("android.permission.BLUETOOTH_CONNECT")
            val bleAdvertiseGranted =
                rxPermissions.isGranted("android.permission.BLUETOOTH_ADVERTISE")
            if (coarseFindGranted && coarseLocationGranted && bleScanGranted && bleConnectGranted && bleAdvertiseGranted) {
                return
            }
        } else {
            if (coarseFindGranted && coarseLocationGranted) {
                return
            }
        }
        val permissionArray = getNeedPermissionList(activity)
        rxPermissions.request(*permissionArray).subscribe {
            Log.e("qzx", "request result: $it")
        }
    }
}