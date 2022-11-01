### Permissions we need

#### On Android 12 or higher we need

> android.permission.BLUETOOTH_ADVERTISE
>
> android.permission.BLUETOOTH_SCAN
>
> android.permission.BLUETOOTH_CONNECT

####  Below android12 we need

>android.permission.BLUETOOTH
>
>android.permission.BLUETOOTH_ADMIN
>
>android.permission.ACCESS_COARSE_LOCATION
>
>android.permission.ACCESS_FINE_LOCATION
>
>

### Google's official documentation:

https://developer.android.com/guide/topics/connectivity/bluetooth/permissions


### ProGuard


```
-keep class com.qingniu.scale.model.BleScaleData{*;}
```


### Download


```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```


Core Plugin (necessary):

>implementation "com.github.YolandaQingniu.BleSdkDemoPro:qnpluginX:1.2.0"

Algorithm Plugin (scale need):

>implementation "com.github.YolandaQingniu.BleSdkDemoPro:qnalgorithmpluginX:1.2.0"

HeightWeightScale Plugin:

>implementation "com.github.YolandaQingniu.BleSdkDemoPro:qnheightweightscalepluginX:1.2.0"

Ruler Plugin:

>implementation "com.github.YolandaQingniu.BleSdkDemoPro:qnrulerpluginX:1.2.0"

Scale Plugin:

>implementation "com.github.YolandaQingniu.BleSdkDemoPro:qnscalepluginX:1.2.0"
