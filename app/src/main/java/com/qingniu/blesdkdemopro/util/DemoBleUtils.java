package com.qingniu.blesdkdemopro.util;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

public final class DemoBleUtils {

    public static final String PERMISSION_ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public static final String PERMISSION_ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String PERMISSION_BLUETOOTH = "android.permission.BLUETOOTH";
    public static final String PERMISSION_BLUETOOTH_ADMIN = "android.permission.BLUETOOTH_ADMIN";
    public static final String PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";
    public static final String PERMISSION_BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE";
    public static final String PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";


    public static final int STATUS_BLE_ENABLED = 0;
    public static final int STATUS_BLUETOOTH_NOT_AVAILABLE = 1;
    public static final int STATUS_BLE_NOT_AVAILABLE = 2;
    public static final int STATUS_BLUETOOTH_DISABLED = 3;

    private static boolean IGNORE_ADVERTISE = false;

    private DemoBleUtils() {
    }

    public static boolean isIgnoreAdvertise() {
        return IGNORE_ADVERTISE;
    }

    public static void setIgnoreAdvertise(boolean ignoreAdvertise) {
        IGNORE_ADVERTISE = ignoreAdvertise;
    }

    public static int getBleStatus(Context context) {
        if (!isSupportBLE(context)) {
            return STATUS_BLE_NOT_AVAILABLE;
        } else {
            BluetoothAdapter adapter = getBluetoothAdapter(context);
            return adapter == null ? STATUS_BLUETOOTH_NOT_AVAILABLE : (!adapter.isEnabled() ? STATUS_BLUETOOTH_DISABLED : STATUS_BLE_ENABLED);
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????BLE
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public static boolean isSupportBLE(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                && getBluetoothAdapter(context) != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2);
    }

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            return bluetoothManager == null ? null : bluetoothManager.getAdapter();
        } else {
            return BluetoothAdapter.getDefaultAdapter();
        }
    }

    /**
     * ????????????????????????????????????
     */
    public static boolean isBlueToothSwitchOn(Context context) {
        boolean isTurnON = false;
        BluetoothAdapter mBluetoothAdapter = getBluetoothAdapter(context);
        if (mBluetoothAdapter != null && (mBluetoothAdapter.isEnabled()
                || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON)) {
            isTurnON = true;
        }
        return isTurnON;
    }

    /**
     * ??????????????????????????????
     *
     * @param context
     * @return
     */
    public static boolean isSupportAdviser(Context context) {
        if (!isSupportBLE(context)) {
            return false;
        } else {
            if (null == getBluetoothAdapter(context)) {
                return false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return getBluetoothAdapter(context).getBluetoothLeAdvertiser() != null;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * ????????????getSystemService??????BluetoothManager???
     * ?????????BluetoothManager??????BluetoothAdapter???
     * BluetoothManager???Android4.3????????????(API level 18)???
     */
    public static boolean isEnable(Context context) {
        boolean isEnable = false;
        BluetoothAdapter mBluetoothAdapter = getBluetoothAdapter(context);
        if (mBluetoothAdapter != null
                && (mBluetoothAdapter.isEnabled() || mBluetoothAdapter
                .getState() == BluetoothAdapter.STATE_TURNING_ON))
            isEnable = true;
        return isEnable;
    }


    /**
     * ??????????????????????????????
     *
     * @param context
     * @return
     */
    public static boolean isLocationOpen(Context context) {
        int locationMode;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


    /**
     * ???????????????????????????
     *
     * @param context
     * @return
     */
    public static boolean hasLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    && hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            return true;
        }
    }


    /**
     * ??????????????????????????????
     *
     * @param context
     * @param permission ???????????????
     * @return
     */
    public static boolean hasPermission(Context context, String permission) {
        boolean hasPermission = ContextCompat.checkSelfPermission(context,
                permission) == PackageManager.PERMISSION_GRANTED;
        return hasPermission;
    }

    /**
     * ??????????????????????????????????????????????????????????????????Android12
     *
     * @param context
     * @return
     */
    public static boolean hasBlePermission(Context context) {
        boolean hasBlePermission;
        //???????????????12
        if (isRunOnAndroid12Mode(context)) {
            hasBlePermission = DemoBleUtils.hasPermission(context, DemoBleUtils.PERMISSION_BLUETOOTH_SCAN)
                    && DemoBleUtils.hasPermission(context, DemoBleUtils.PERMISSION_BLUETOOTH_CONNECT);
            //??????????????????????????????
            if (!IGNORE_ADVERTISE) {
                hasBlePermission = hasBlePermission && DemoBleUtils.hasPermission(context, DemoBleUtils.PERMISSION_BLUETOOTH_ADVERTISE);
            }
        } else {
            hasBlePermission = DemoBleUtils.hasPermission(context, DemoBleUtils.PERMISSION_BLUETOOTH)
                    && DemoBleUtils.hasPermission(context, DemoBleUtils.PERMISSION_BLUETOOTH_ADMIN);
        }
        return hasBlePermission;
    }

    /**
     * ???????????????12???????????????
     * ???targetSdkVersion ??? SDK_INT ?????????30
     */
    public static boolean isRunOnAndroid12Mode(Context context) {
        return Build.VERSION.SDK_INT > 30 && context.getApplicationInfo().targetSdkVersion > 30;
    }

}

