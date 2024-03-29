### Permissions we need

#### On Android 12 or higher we need

> android.permission.BLUETOOTH_ADVERTISE
>
> android.permission.BLUETOOTH_SCAN
>
> android.permission.BLUETOOTH_CONNECT

#### Below android12 we need

> android.permission.BLUETOOTH
>
> android.permission.BLUETOOTH_ADMIN
>
> android.permission.ACCESS_COARSE_LOCATION
>
> android.permission.ACCESS_FINE_LOCATION

### Google's official documentation:

https://developer.android.com/guide/topics/connectivity/bluetooth/permissions

### ProGuard

```
-keep class com.qingniu.** {*;}

-keep classmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
```

### Download

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Core Plugin (necessary):

> implementation "com.github.YolandaQingniu.BleSdkDemoPro:QNPluginX:1.4.2"

Algorithm Plugin (scale need):

> implementation "com.github.YolandaQingniu.BleSdkDemoPro:QNAlgorithmPluginX:1.4.2"

HeightWeightScale Plugin:

> implementation "com.github.YolandaQingniu.BleSdkDemoPro:QNHeightWeightScalePluginX:1.4.2"

Ruler Plugin:

> implementation "com.github.YolandaQingniu.BleSdkDemoPro:QNRulerPluginX:1.4.2"

Scale Plugin:

> implementation "com.github.YolandaQingniu.BleSdkDemoPro:QNScalePluginX:1.4.2"

KitchenScale Plugin:

> implementation "com.github.YolandaQingniu.BleSdkDemoPro:QNKitchenScalePluginX:1.4.2"

BPMachine Plugin:

> implementation "com.github.YolandaQingniu.BleSdkDemoPro:QNBPMachinePluginX:1.4.2"
