package com.iraimjanov.taxometr.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.taxometr.R
import com.iraimjanov.taxometr.databinding.FragmentHomeBinding
import com.iraimjanov.taxometr.models.History
import com.iraimjanov.taxometr.models.LatLong
import com.iraimjanov.taxometr.models.Users
import com.iraimjanov.taxometr.utils.Constants
import com.iraimjanov.taxometr.utils.PermissionsService
import com.orhanobut.hawk.Hawk
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import java.text.DecimalFormat
import java.util.*
import kotlin.math.*


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var placeMark: PlacemarkMapObject
    private lateinit var realReference: DatabaseReference
    private var user: Users = Hawk.get("user", Users())
    private var booleanAntiBagDialog = true
    private var placeMarkBoolean = false
    private var kmSumma = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        loadData()

        checkForLocationAccess()

        binding.imageMenu.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }

        binding.imageSettings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        return binding.root
    }

    private fun stop() {
        binding.swipeRefreshLayout.isRefreshing = true
        realReference.child(user.username).child(realReference.push().key.toString())
            .setValue(History(realReference.push().key.toString(),
                binding.tvKmSumma.text.toString(),
                binding.tvKm.text.toString(),
                binding.tvSumma.text.toString(),
                binding.tvTime.text.toString(),
                listLatLong))
            .addOnSuccessListener {
                active = false
                pause = false
                binding.btnStart.isEnabled = true
                binding.btnStop.isEnabled = false
                binding.tvMode.text = "Not active"
                timerTask.cancel()
                time = 0.0
                listLatLong.clear()
                binding.tvKm.text = "Kilometer:"
                binding.tvSumma.text = "Summa:"
                binding.tvTime.text = "Time:"
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun pause(initializedTimerTask: Boolean) {
        active = false
        pause = true
        binding.btnStart.isEnabled = true
        binding.btnPause.isEnabled = false
        binding.tvMode.text = "Paused"
        if (initializedTimerTask) {
            timerTask.cancel()
        }
    }

    private fun start() {
        active = true
        pause = false
        binding.btnStart.isEnabled = false
        binding.btnPause.isEnabled = true
        binding.btnStop.isEnabled = true
        binding.tvMode.text = "Started"
        startTimer()
    }


    private fun buildKmAndSumma(): List<String> {
        var index = 0
        var meter = 0.0
        if (listLatLong.size != 1) {
            for (i in 0 until listLatLong.size) {
                if (i != listLatLong.size - 1) {
                    index++
                    meter += measure(listLatLong[i].lat,
                        listLatLong[i].long,
                        listLatLong[index].lat,
                        listLatLong[index].long)
                }
            }
        }
        return listOf("${DecimalFormat("0.00").format(meter / 1000)} km",
            "${DecimalFormat("###,###,###,###").format((meter / 1000) * kmSumma.toInt())} so'm")
    }

    private fun measure(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radiusEarth = 6378.137
        val dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180
        val dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * Math.PI / 180) * cos(lat2 * Math.PI / 180) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val d = radiusEarth * c
        return d * 1000
    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {

        binding.cardLocation.setOnClickListener {
            checkForLocationAccess()
        }

        binding.btnStart.setOnClickListener {
            start()
        }

        binding.btnPause.setOnClickListener {
            pause(true)
        }

        binding.btnStop.setOnClickListener {
            stop()
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                buildMap(it, true)
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()!!)
    }

    private fun buildMap(location: Location, boolean: Boolean) {
        if (active) {
            listLatLong.add(LatLong(location.latitude, location.longitude))
        }
        if (boolean) {
            binding.mapView.map.move(
                CameraPosition(Point(location.latitude, location.longitude),
                    16.0f,
                    0.0f,
                    0.0f),
                Animation(Animation.Type.LINEAR, 1F),
                null)
        }

        if (placeMarkBoolean) {
            placeMark.geometry = Point(location.latitude, location.longitude)
        } else {
            placeMarkBoolean = true
            placeMark = binding.mapView.map.mapObjects.addPlacemark(Point(location.latitude,
                location.longitude),
                ImageProvider.fromBitmap(getBitmapFromVectorDrawable(R.drawable.ic_location)))
        }

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val result = p0.lastLocation
            buildMap(result, false)
        }
    }

    private fun loadData() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create()
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        binding.tvKmSumma.text = "1 km = ${Hawk.get("km/summa", "3000")} so'm"
        binding.swipeRefreshLayout.isEnabled = false
        kmSumma = Hawk.get("km/summa", "3000")
        placeMarkBoolean = false
        realReference = Firebase.database.getReference("history")
        if (active) {
            start()
        }
        if (pause) {
            pause(false)
            binding.tvTime.text = "Time: ${getTimerText()}"
            val list = buildKmAndSumma()
            binding.tvKm.text = "Kilometer: ${list[0]}"
            binding.tvSumma.text = "Summa: ${list[1]}"
        }
    }

    private fun checkForLocationAccess() {
        if (PermissionsService().checkPermission(requireActivity())) {
            checkLocationEnabled()
        } else {
            requestPermissions()
        }
    }

    private fun checkLocationEnabled() {
        if (PermissionsService().isLocationEnabled(requireActivity())) {
            getMyLocation()
        } else {
            if (booleanAntiBagDialog) {
                buildAlertMessageNoGps()
                booleanAntiBagDialog = false
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun requestPermissions() {
        val mPermissionResult = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result) {
                checkLocationEnabled()
            } else {
                buildAlertMessageNoPermissions()
            }
        }
        mPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun buildAlertMessageNoPermissions() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Allow the app to access location information for the app to work properly.\n" +
                "Go to Permission>Location to open it")
            .setCancelable(false)
            .setPositiveButton("To Open It") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") {
                    _, _,
                ->
                launchLocationEnabled.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        builder.setOnDismissListener {
            booleanAntiBagDialog = true
        }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(requireActivity(), drawableId)

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private val launchLocationEnabled =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            checkLocationEnabled()
        }

    private fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                if (active && isVisible) {
                    requireActivity().runOnUiThread {
                        time++
                        binding.tvTime.text = "Time: ${getTimerText()}"
                        val list = buildKmAndSumma()
                        binding.tvKm.text = "Kilometer: ${list[0]}"
                        binding.tvSumma.text = "Summa: ${list[1]}"
                    }
                }
            }
        }
        initializedTimerTask = true
        timer.scheduleAtFixedRate(timerTask, 0, 1000)
    }


    private fun getTimerText(): String {
        val rounded = time.roundToInt()
        val seconds = rounded % 86400 % 3600 % 60
        val minutes = rounded % 86400 % 3600 / 60
        val hours = rounded % 86400 / 3600
        return formatTime(seconds, minutes, hours)
    }

    private fun formatTime(seconds: Int, minutes: Int, hours: Int): String {
        return String.format("%02d", hours) + " : " + String.format("%02d",
            minutes) + " : " + String.format("%02d", seconds)
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onDestroyView() {
        if (initializedTimerTask) {
            timerTask.cancel()
        }
        super.onDestroyView()
    }

    private fun initialize() {
        if (initialized) {
            return
        }
        MapKitFactory.setApiKey(Constants.API_KEY_MAP_KIT)
        MapKitFactory.initialize(requireActivity())
        initialized = true
    }

    companion object {
        private lateinit var timer: Timer
        private lateinit var timerTask: TimerTask
        private var initialized = false
        private var initializedTimerTask = false
        private var listLatLong: ArrayList<LatLong> = ArrayList()
        private var active = false
        private var pause = false
        private var time = 0.0
    }
}