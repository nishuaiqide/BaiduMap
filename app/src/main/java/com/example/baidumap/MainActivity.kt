package com.example.baidumap

import android.Manifest
import android.content.pm.PackageManager

import android.os.Bundle

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng


class MainActivity : AppCompatActivity() {

    var mLocationClient: LocationClient? = null
    var positionText: TextView? = null
    var mapView: MapView? = null
    var baiduMap: BaiduMap? = null
    var isFirstLocate = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLocationClient = LocationClient(applicationContext)
        mLocationClient?.registerLocationListener(MyLocationListener())
        SDKInitializer.initialize(applicationContext)

        setContentView(R.layout.activity_main)

        positionText = findViewById<TextView>(R.id.position_text_view)
        mapView = findViewById<MapView>(R.id.bmapView)
        baiduMap = mapView?.map
        baiduMap?.isMyLocationEnabled = true

        var permissionList = ArrayList<String>()
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE)
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(!permissionList.isEmpty()) {
            var permissions = permissionList.toArray(arrayOfNulls<String>(permissionList.size))
            ActivityCompat.requestPermissions(this,permissions,1)
        }else {
            requestLocation()
        }
    }


    fun initLocation() {
        var option = LocationClientOption()
//        option.locationMode = LocationClientOption.LocationMode.Device_Sensors
        option.scanSpan = 5000
        option.setIsNeedAddress(true)
        mLocationClient?.locOption = option
    }


    fun navigateTo(location: BDLocation) {
        if(isFirstLocate) {
            var ll = LatLng(location.latitude,location.longitude)
            var update = MapStatusUpdateFactory.newLatLng(ll)
            baiduMap?.animateMapStatus(update)
            update = MapStatusUpdateFactory.zoomTo(16f)
            baiduMap?.animateMapStatus(update)
            isFirstLocate = false
        }

        var locationBuilder = MyLocationData.Builder()
        locationBuilder.latitude(location.latitude)
        locationBuilder.longitude(location.longitude)
        var locationData = locationBuilder.build()
        baiduMap?.setMyLocationData(locationData)
    }


    fun requestLocation() {
        initLocation()
        mLocationClient?.start()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            1 -> {
                if(grantResults.isNotEmpty()) {
                    for(result in grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show()
                            finish()
                            return
                        }
                    }

                    requestLocation()
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            else -> {

            }
        }
    }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }


    override fun onDestroy() {
        super.onDestroy()
        mLocationClient?.stop()
        mapView?.onDestroy()
        baiduMap?.isMyLocationEnabled = false
    }


    inner class MyLocationListener: BDLocationListener{
        override fun onReceiveLocation(location: BDLocation?) {
//            runOnUiThread(object: Runnable {
//                override fun run() {
//
//                    var currentPosition = StringBuilder()
//                    currentPosition.append("维度: ").append(location?.latitude).append("\n")
//                    currentPosition.append("经度: ").append(location?.longitude).append("\n")
//                    currentPosition.append("定位方式: ")
//
//                    currentPosition.append("国家: ").append(location?.country).append("\n")
//                    currentPosition.append("省: ").append(location?.province).append("\n")
//                    currentPosition.append("市: ").append(location?.city).append("\n")
//                    currentPosition.append("区: ").append(location?.district).append("\n")
//                    currentPosition.append("街道: ").append(location?.street).append("\n")
//
//
//                    if(location?.locType == BDLocation.TypeGpsLocation) {
//                        currentPosition.append("GPS")
//                    }else if(location?.locType == BDLocation.TypeNetWorkLocation) {
//                        currentPosition.append("网络")
//                    }
//
//                    positionText?.setText(currentPosition)
//                }
//            })


            if(location?.locType == BDLocation.TypeGpsLocation || location?.locType == BDLocation.TypeNetWorkLocation) {
                navigateTo(location!!)
            }
        }
    }
}