package com.example.gps

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gps.ui.theme.GPSTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.util.Log
import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay




object R {
    object id {
        const val map = 1
    }
}

class MainActivity : ComponentActivity() {

    //creaza o constanta pentru codul permiksiunii "access fine  location"
    private val  locationPermissionRequestCode = 1001
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var mapView : MapView

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("GPS", "Permisiunea a fost acordată.")
                startLocationUpdates()
            } else {
                Log.d("GPS", "Permisiunea a fost REFUZATĂ.")
            }
        }

    //functia care porneste obtinerea pozitiei GPS in timp real
    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, //prioritate mare (precizie ridicata)
            10000L //intervalul dorit intre actualizari (in  milisecunde)
        ).apply {
            setMinUpdateIntervalMillis(5000L) //intervalul minim intre actualizari (mai rapide de atat nu cere)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for (location in p0.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.d("GPS", "Locatie noua: $lat, $lon")
                    //aici pot actualiza harta sau sa  salvez in baza  de date

                    val geoPoint = GeoPoint(lat, lon)
                    //mutam harta la noua locatie
                    mapView.controller.setCenter(geoPoint)

                    //stergem marker-ele anterioare (daca dorim doar un singur marker)
                    mapView.overlays.clear()

                    //adaugam un nou marker
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Locatia ta"

                    mapView.overlays.add(marker)
                    mapView.invalidate() //redeseneaza harta
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED //adica granted= permisiun ea a fost acordata
        ) {
            Log.d("GPS", "Permisiunea este deja acordată.")
            startLocationUpdates()
        } else {
            Log.d("GPS", "Cerem permisiunea cu launcher modern.")
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission() //verifica permisiunea

        setContent {
            GPSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AndroidView(
                        modifier = Modifier.padding(innerPadding),
                        factory = { context ->
                            MapView(context).apply {
                                id = R.id.map
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(18.0)
                                controller.setCenter(GeoPoint(45.7489, 21.2087))
                                mapView = this
                            }
                        }
                    )
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GPSTheme {
        Greeting("denisa")
    }
}