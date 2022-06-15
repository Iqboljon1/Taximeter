package com.iraimjanov.taxometr.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity

class PermissionsService {

    fun checkPermission(activity: FragmentActivity):Boolean{
        return ActivityCompat.checkSelfPermission(activity , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(activity: FragmentActivity):Boolean{
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

}