package com.nahohoon.golfgps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.wear.widget.SwipeDismissFrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.abs
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnRefresh: Button
    private lateinit var tvStatusLine: TextView
    private lateinit var tvCurrentHole: TextView
    private lateinit var tvDistanceNum: TextView
    private lateinit var tvDistanceUnit: TextView
    private lateinit var tvLastUpdated: TextView

    private var currentHole = HOLE_MIN
    private var lastLocation: Location? = null
    private var isUpdating = false
    private var isFetching = false
    private var locationToken: CancellationTokenSource? = null
    private var swipeStartX = 0f
    private var swipeStartY = 0f
    private var horizontalSwipeInProgress = false

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L
    )
        .setMinUpdateIntervalMillis(2000L)
        .setMaxUpdates(1)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            stopLocationUpdates()
            showLocation(location)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            fetchLocation(isRefresh = false)
        } else {
            setFetching(false)
            setGpsStatus(R.string.gps_denied, StatusColor.ERROR)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnRefresh = findViewById(R.id.btn_refresh)
        tvStatusLine = findViewById(R.id.tv_status_line)
        tvCurrentHole = findViewById(R.id.tv_current_hole)
        tvDistanceNum = findViewById(R.id.tv_distance_num)
        tvDistanceUnit = findViewById(R.id.tv_distance_unit)
        tvLastUpdated = findViewById(R.id.tv_last_updated)

        updateHoleDisplay()

        findViewById<Button>(R.id.btn_prev_hole).setOnClickListener {
            changeHole(-1)
        }
        findViewById<Button>(R.id.btn_next_hole).setOnClickListener {
            changeHole(1)
        }
        btnRefresh.setOnClickListener {
            if (isFetching) return@setOnClickListener
            Log.d("GolfGPS", "Refresh clicked")
            fetchLocation(isRefresh = true)
        }

        disableWearSwipeDismiss()

        requestLocationPermissionAndFetch()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                swipeStartX = ev.x
                swipeStartY = ev.y
                horizontalSwipeInProgress = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = ev.x - swipeStartX
                val deltaY = ev.y - swipeStartY
                if (abs(deltaX) > SWIPE_DIRECTION_LOCK_PX &&
                    abs(deltaX) > abs(deltaY) * SWIPE_HORIZONTAL_RATIO
                ) {
                    horizontalSwipeInProgress = true
                    window.decorView.parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (horizontalSwipeInProgress) {
                    val deltaX = ev.x - swipeStartX
                    val deltaY = ev.y - swipeStartY
                    if (abs(deltaX) >= SWIPE_THRESHOLD_PX && abs(deltaX) > abs(deltaY)) {
                        changeHole(if (deltaX > 0) -1 else 1)
                    }
                    horizontalSwipeInProgress = false
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun disableWearSwipeDismiss() {
        var parent: View? = window.decorView
        while (parent != null) {
            if (parent is SwipeDismissFrameLayout) {
                parent.isSwipeable = false
                return
            }
            parent = parent.parent as? View
        }
    }

    override fun onDestroy() {
        cancelLocationRequest()
        stopLocationUpdates()
        super.onDestroy()
    }

    private fun setupSwipeHoleChange() {
        val panel = findViewById<View>(R.id.distance_panel)
        panel.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    swipeStartX = event.x
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.x - swipeStartX
                    if (abs(deltaX) >= SWIPE_THRESHOLD_PX) {
                        changeHole(if (deltaX > 0) -1 else 1)
                        view.performClick()
                    }
                    true
                }
                else -> true
            }
        }
    }

    private fun requestLocationPermissionAndFetch() {
        if (hasLocationPermission()) {
            fetchLocation(isRefresh = false)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun fetchLocation(isRefresh: Boolean) {
        if (!hasLocationPermission()) {
            requestLocationPermissionAndFetch()
            return
        }
        if (isFetching) return

        setFetching(true)
        val statusText = if (isRefresh) R.string.gps_refreshing else R.string.gps_searching
        setGpsStatus(statusText, StatusColor.SEARCHING)
        cancelLocationRequest()
        stopLocationUpdates()

        if (isRefresh) {
            requestFreshLocation()
        } else {
            requestCachedOrFreshLocation()
        }
    }

    private fun requestCachedOrFreshLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        showLocation(location)
                    } else {
                        requestFreshLocation()
                    }
                }
                .addOnFailureListener {
                    setFetching(false)
                    setGpsStatus(R.string.gps_error, StatusColor.ERROR)
                }
        } catch (e: SecurityException) {
            setFetching(false)
            setGpsStatus(R.string.gps_denied, StatusColor.ERROR)
        }
    }

    private fun requestFreshLocation() {
        try {
            val token = CancellationTokenSource()
            locationToken = token
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                token.token
            )
                .addOnSuccessListener { location ->
                    locationToken = null
                    if (location != null) {
                        showLocation(location)
                    } else {
                        startSingleLocationUpdate()
                    }
                }
                .addOnFailureListener {
                    locationToken = null
                    setFetching(false)
                    setGpsStatus(R.string.gps_error, StatusColor.ERROR)
                }
        } catch (e: SecurityException) {
            locationToken = null
            setFetching(false)
            setGpsStatus(R.string.gps_denied, StatusColor.ERROR)
        }
    }

    private fun startSingleLocationUpdate() {
        if (!hasLocationPermission() || isUpdating) return

        try {
            isUpdating = true
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            isUpdating = false
            setFetching(false)
            setGpsStatus(R.string.gps_denied, StatusColor.ERROR)
        }
    }

    private fun cancelLocationRequest() {
        locationToken?.cancel()
        locationToken = null
    }

    private fun stopLocationUpdates() {
        if (!isUpdating) return
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: SecurityException) {
            // ignore on cleanup
        }
        isUpdating = false
    }

    private fun showLocation(location: Location) {
        lastLocation = location
        setFetching(false)
        setGpsStatus(R.string.gps_ready, StatusColor.SUCCESS, location)
        tvLastUpdated.text = getString(R.string.last_updated_format, timeFormat.format(Date()))
        updateGreenDistance(location)
    }

    private fun changeHole(delta: Int) {
        currentHole = when {
            delta > 0 -> if (currentHole >= HOLE_MAX) HOLE_MIN else currentHole + 1
            delta < 0 -> if (currentHole <= HOLE_MIN) HOLE_MAX else currentHole - 1
            else -> currentHole
        }
        updateHoleDisplay()
        findViewById<View>(R.id.main_content)
            .performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val location = lastLocation
        if (location != null) {
            updateGreenDistance(location)
        } else {
            clearDistanceDisplay()
        }
    }

    private fun updateHoleDisplay() {
        tvCurrentHole.text = getString(R.string.hole_format, currentHole)
    }

    private fun clearDistanceDisplay() {
        tvDistanceNum.setText(R.string.distance_num_placeholder)
        tvDistanceUnit.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
    }

    private fun updateGreenDistance(location: Location) {
        val green = GREENS.first { it.hole == currentHole }
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            green.lat,
            green.lng,
            results
        )
        val meters = results[0].roundToInt()
        tvDistanceNum.text = meters.toString()
        tvDistanceUnit.setTextColor(ContextCompat.getColor(this, R.color.text_bright))
    }

    private fun setFetching(fetching: Boolean) {
        isFetching = fetching
        btnRefresh.isEnabled = !fetching
    }

    private fun setGpsStatus(
        textRes: Int,
        color: StatusColor,
        location: Location? = lastLocation
    ) {
        if (textRes == R.string.gps_ready && location != null) {
            val status = getString(R.string.gps_ready)
            val accuracy = location.accuracy
            if (accuracy >= ACCURACY_WARN_METERS) {
                tvStatusLine.text = getString(R.string.status_line_warn, status, accuracy)
                tvStatusLine.setTextColor(ContextCompat.getColor(this, R.color.status_warn))
            } else {
                tvStatusLine.text = getString(R.string.status_line_ok, status, accuracy)
                tvStatusLine.setTextColor(ContextCompat.getColor(this, R.color.status_success))
            }
            return
        }

        tvStatusLine.setText(textRes)
        tvStatusLine.setTextColor(
            ContextCompat.getColor(
                this,
                when (color) {
                    StatusColor.SEARCHING -> R.color.status_searching
                    StatusColor.SUCCESS -> R.color.status_success
                    StatusColor.ERROR -> R.color.status_error
                    StatusColor.NEUTRAL -> R.color.text_bright
                }
            )
        )
    }

    private enum class StatusColor {
        SEARCHING, SUCCESS, ERROR, NEUTRAL
    }

    private data class GreenCenter(val hole: Int, val lat: Double, val lng: Double)

    companion object {
        private const val HOLE_MIN = 1
        private const val HOLE_MAX = 18
        private const val ACCURACY_WARN_METERS = 30f
        private const val SWIPE_THRESHOLD_PX = 48f
        private const val SWIPE_DIRECTION_LOCK_PX = 18f
        private const val SWIPE_HORIZONTAL_RATIO = 1.2f

        private val GREENS = listOf(
            GreenCenter(1, 35.902494, 128.663617),
            GreenCenter(2, 35.903894, 128.662169),
            GreenCenter(3, 35.900303, 128.661111),
            GreenCenter(4, 35.903389, 128.662028),
            GreenCenter(5, 35.905872, 128.657864),
            GreenCenter(6, 35.904975, 128.656803),
            GreenCenter(7, 35.903142, 128.660486),
            GreenCenter(8, 35.901367, 128.659561),
            GreenCenter(9, 35.899169, 128.661744),
            GreenCenter(10, 35.901589, 128.664028),
            GreenCenter(11, 35.904119, 128.662375),
            GreenCenter(12, 35.900428, 128.660836),
            GreenCenter(13, 35.903408, 128.662303),
            GreenCenter(14, 35.906192, 128.658089),
            GreenCenter(15, 35.905117, 128.656419),
            GreenCenter(16, 35.902775, 128.660492),
            GreenCenter(17, 35.901711, 128.659156),
            GreenCenter(18, 35.899167, 128.661739)
        )
    }
}
