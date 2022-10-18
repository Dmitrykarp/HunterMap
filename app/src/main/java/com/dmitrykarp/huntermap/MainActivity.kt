package com.dmitrykarp.huntermap

import android.Manifest
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
import org.mapsforge.core.graphics.*
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidBitmap
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.layers.MyLocationOverlay
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.layer.overlay.Polyline
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme
import org.mapsforge.map.rendertheme.XmlUtils
import java.io.FileInputStream


class MainActivity : AppCompatActivity() {

    companion object{
        //For emulation
        var CURRENT_LOCATION = LatLong(51.3200, 46.0000)
    }

    private lateinit var b: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var zoneStatus = false

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
        var marker = Marker(CURRENT_LOCATION,bitmapBalloonSN,1,1)
        var locationLayer = MyLocationOverlay(marker)
        val gf: GraphicFactory = AndroidGraphicFactory.INSTANCE
        val paintZone: Paint = gf.createPaint()
        paintZone.setStyle(Style.FILL)
        paintZone.strokeWidth = 7F
        paintZone.color = XmlUtils.getColor(gf, "#73ecec35")
        val pl = Polyline(paintZone, gf)

        val paintZoneStroke: Paint = gf.createPaint()
        paintZoneStroke.setStyle(Style.STROKE)
        paintZoneStroke.strokeWidth = 7F
        paintZoneStroke.color = XmlUtils.getColor(gf, "#FFed3438")
        val plStroke = Polyline(paintZoneStroke, gf)


        val contract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result->
            result?.data?.data?.let{uri->
                openMap(uri)
            }
        }

        b.openMap.setOnClickListener{
            contract.launch(
                Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                ).apply{
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            )
            b.goToPosition.isVisible = true
            b.showZone.isVisible = true
        }

        b.goToPosition.setOnClickListener{
            println("Start go to")
            val request = com.google.android.gms.location.LocationRequest()
            request.interval = 10000
            request.fastestInterval = 5000
            request.priority = LocationRequest.QUALITY_HIGH_ACCURACY
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            )
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION),1)
            }

            if (permission == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location: Location? = locationResult.lastLocation
                        if (location != null) {
                            CURRENT_LOCATION = LatLong(location.latitude, location.longitude)
                        }
                    }
                }, null)
            }


            marker.latLong = CURRENT_LOCATION
            if (b.map.layerManager.layers.indexOf(locationLayer) > 0){
                b.map.layerManager.layers.remove(b.map.layerManager.layers.indexOf(locationLayer))
                b.map.addLayer(locationLayer)
            }else{
                b.map.addLayer(locationLayer)
            }
            b.map.setCenter(CURRENT_LOCATION)
            b.map.setZoomLevel(13)
            println("Stop go to")
        }

        b.showZone.setOnClickListener {
            if (!zoneStatus){
                //show zone
                val latLongs: MutableList<LatLong> = pl.latLongs
                latLongs.addAll(JavaUtils.getPontList())
                b.map.addLayer(pl)
                val latLongsStroke: MutableList<LatLong> = plStroke.latLongs
                latLongsStroke.addAll(JavaUtils.getPontList())
                b.map.addLayer(plStroke)
                zoneStatus = true

            }else{
                //hide zone
                if (b.map.layerManager.layers.indexOf(pl) > 0){
                    b.map.layerManager.layers.remove(b.map.layerManager.layers.indexOf(pl))
                    b.map.layerManager.layers.remove(b.map.layerManager.layers.indexOf(plStroke))
                }
                zoneStatus = false
            }
        }
    }


    fun openMap(uri: Uri){
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
}