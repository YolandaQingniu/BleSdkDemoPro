# BleSdkDemoPro
组件化的蓝牙SDK Demo


ATTENTION: the following runtime permissions are required if you run this sdk on Android 12
android.permission.BLUETOOTH_ADVERTISE
android.permission.BLUETOOTH_SCAN
android.permission.BLUETOOTH_CONNECT

and below android12 we need
android.permission.BLUETOOTH
android.permission.BLUETOOTH_ADMIN
android.permission.ACCESS_COARSE_LOCATION
android.permission.ACCESS_FINE_LOCATION

Google's official documentation:
https://developer.android.com/guide/topics/connectivity/bluetooth/permissions


	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Core Plugin (necessary):
implementation "com.github.YolandaQingniu.BleSdkDemoPro:qnpluginX:1.0.0"

HeightWeightScale Plugin:
implementation "com.github.YolandaQingniu.BleSdkDemoPro:qnheightweightscalepluginX:1.0.0"
