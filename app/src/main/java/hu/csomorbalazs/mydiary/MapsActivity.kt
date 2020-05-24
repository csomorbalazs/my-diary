package hu.csomorbalazs.mydiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import hu.csomorbalazs.mydiary.data.AppDatabase


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Thread {
            mMap = googleMap

            val diaryEntriesWithLocation = AppDatabase
                .getInstance(this)
                .diaryEntryDao()
                .getAllDiaryEntries()
                .filter { entry ->
                    entry.place.isValidCoordinate()
                }

            if (diaryEntriesWithLocation.isEmpty()) {
                return@Thread
            }

            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val padding = (height * 0.20).toInt()

            val builder: LatLngBounds.Builder = LatLngBounds.builder()

            runOnUiThread {
                for (entry in diaryEntriesWithLocation) {
                    val location = entry.place!!.toLocation()

                    builder.include(location)

                    mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(entry.title)
                    )
                }

                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        width,
                        height,
                        padding
                    )
                )
            }
        }.start()
    }

    private fun String?.isValidCoordinate(): Boolean {
        return this?.matches(
            Regex("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)\$")
        ) ?: false
    }

    private fun String.toLocation(): LatLng {
        val locationStrList: List<String> = this.split(',')
        val latStr = locationStrList[0].replace("\\s".toRegex(), "")
        val lngStr = locationStrList[1].replace("\\s".toRegex(), "")

        return LatLng(latStr.toDouble(), lngStr.toDouble())
    }
}
