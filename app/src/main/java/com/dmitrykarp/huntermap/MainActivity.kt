package com.dmitrykarp.huntermap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.dmitrykarp.huntermap.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.mapsforge.core.graphics.Bitmap
import org.mapsforge.core.graphics.GraphicFactory
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidBitmap
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.layers.MyLocationOverlay
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.layer.overlay.Polygon
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme
import org.mapsforge.map.rendertheme.XmlUtils
import java.io.FileInputStream


class MainActivity : AppCompatActivity() {

    companion object {
        //For emulation
        var CURRENT_LOCATION = LatLong(51.3200, 46.0000)
    }

    private lateinit var b: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private var zoneStatus = false
    private var buttonFlag = false
    private lateinit var marker: Marker
    private lateinit var locationLayer: MyLocationOverlay
    private lateinit var gf: GraphicFactory
    private lateinit var polygonList: Collection<Polygon>

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidGraphicFactory.createInstance(application)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val bitmapBalloonSN: Bitmap = AndroidBitmap(
            BitmapFactory.decodeResource(
                resources, R.drawable.ic_maps_indicator_current_position
            )
        )
        b.switchTrack.isChecked = false
        polygonList = JavaUtils.initPolygons(resources);

        marker = Marker(CURRENT_LOCATION, bitmapBalloonSN, 1, 1)
        locationLayer = MyLocationOverlay(marker)
        gf = AndroidGraphicFactory.INSTANCE


        val contract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result?.data?.data?.let { uri ->
                openMap(uri)
            }
        }

        b.onOff.setOnClickListener {
            if (buttonFlag) {
                buttonFlag = false
                b.onOff.text = "OFF"
                b.onOff.setBackgroundColor(XmlUtils.getColor(gf, "#FF5902D3"))
                b.switchTrack.isChecked = false
            } else {
                buttonFlag = true
                b.onOff.setBackgroundColor(XmlUtils.getColor(gf, "#FFDB1833"))
                b.onOff.text = "ON"
                b.switchTrack.isChecked = true
            }
        }


        b.openMap.setOnClickListener {
            contract.launch(
                Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                ).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            )
            b.showZone.isVisible = true
            b.openMap.isVisible = false
            b.onOff.isVisible = true
        }



        b.showZone.setOnClickListener {
            if (!zoneStatus) {
                for (pl in polygonList) {
                    b.map.addLayer(pl)
                }
                zoneStatus = true

            } else {
                for (pl in polygonList) {
                    if (b.map.layerManager.layers.indexOf(pl) > 0) {
                        b.map.layerManager.layers.remove(b.map.layerManager.layers.indexOf(pl))
                    }
                }
                zoneStatus = false
            }
        }

        updateGPS()

        // End onCreate method
    }

    fun openMap(uri: Uri) {
        b.map.mapScaleBar.isVisible = true
        b.map.setBuiltInZoomControls(true)
        b.map.setZoomLevelMax(16)
        b.map.setZoomLevelMin(10)
        val cache = AndroidUtil.createTileCache(
            this,
            "mycache",
            b.map.model.displayModel.tileSize,
            1f,
            b.map.model.frameBufferModel.overdrawFactor
        )
        val stream = contentResolver.openInputStream(uri) as FileInputStream
        val mapStore = MapFile(stream)

        val renderLayer = TileRendererLayer(
            cache,
            mapStore,
            b.map.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        )

        renderLayer.setXmlRenderTheme(
            InternalRenderTheme.DEFAULT
        )

        b.map.layerManager.layers.add(renderLayer)
        b.map.setCenter(CURRENT_LOCATION)
        b.map.setZoomLevel(13)

    }

    fun updateGPS() {
        println(" DMKA UpdateGPS")
        locationRequest = com.google.android.gms.location.LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.QUALITY_HIGH_ACCURACY
        val permission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                99
            )
        }

        if (permission == PackageManager.PERMISSION_GRANTED) {

            /*fusedLocationClient.lastLocation.addOnSuccessListener(
                this,
                OnSuccessListener<Location>() {
                    fun onSuccess(location: Location) {
                        if (b.switchTrack.isChecked) {
                            updateMarker(location)
                        }
                    }
                })*/

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location: Location? = locationResult.lastLocation
                        if (location != null && b.switchTrack.isChecked) {
                            updateMarker(location)
                        }
                    }
                },
                null
            )
        }
    }

    fun updateMarker(location: Location) {
        println(" DMKA UpdateMarker")
        CURRENT_LOCATION = LatLong(location.latitude, location.longitude)
        marker.latLong = CURRENT_LOCATION
        if (b.map.layerManager.layers.indexOf(locationLayer) >= 0) {
            b.map.layerManager.layers.remove(b.map.layerManager.layers.indexOf(locationLayer))
            b.map.addLayer(locationLayer)
        } else {
            b.map.addLayer(locationLayer)
        }
        b.map.setCenter(CURRENT_LOCATION)
    }
}
