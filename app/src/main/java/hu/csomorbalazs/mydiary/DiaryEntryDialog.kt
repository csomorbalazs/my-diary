package hu.csomorbalazs.mydiary

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import hu.csomorbalazs.mydiary.data.DiaryEntry
import kotlinx.android.synthetic.main.diary_entry_dialog.view.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class DiaryEntryDialog : DialogFragment() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1
        private const val CAMERA_PERMISSION_REQUEST = 2
        private const val LOCATION_SETTINGS_REQUEST = 1441
    }

    interface DiaryEntryHandler {
        fun diaryEntryCreated(diaryEntry: DiaryEntry)
    }

    lateinit var client: FusedLocationProviderClient
    lateinit var easyImage: EasyImage
    lateinit var diaryEntryHandler: DiaryEntryHandler

    private val dateFormat: DateFormat = SimpleDateFormat.getDateInstance()
    private var selectedDate: Calendar = Calendar.getInstance()

    private var imagePath: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is DiaryEntryHandler) {
            diaryEntryHandler = context
        } else {
            throw RuntimeException(
                "The Activity doesn't implement the DiaryEntryHandler interface"
            )
        }
    }

    private lateinit var rbPersonal: RadioButton
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var etPlace: EditText
    private lateinit var btnLocation: ImageButton
    private lateinit var ivAddedImage: ImageView
    private lateinit var btnAddImage: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())

        val dialogView = requireActivity().layoutInflater.inflate(
            R.layout.diary_entry_dialog, null
        )

        rbPersonal = dialogView.rbPersonal
        etTitle = dialogView.etTitle
        etDescription = dialogView.etDescription
        etDate = dialogView.etDate
        etPlace = dialogView.etPlace
        btnLocation = dialogView.btnLocation
        ivAddedImage = dialogView.ivAddedImage
        btnAddImage = dialogView.btnAddImage

        etDate.setText(dateFormat.format(selectedDate.time))

        dialogView.etDate.setOnClickListener {
            val picker = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    selectedDate = Calendar.getInstance()
                        .apply {
                            set(year, month, dayOfMonth)
                        }

                    etDate.setText(
                        dateFormat.format(selectedDate.time)
                    )
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )

            picker.show()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setPositiveButton("Create") { _, _ -> }

        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        btnLocation.setOnClickListener { tryToGetLocation() }

        easyImage = EasyImage.Builder(requireContext()).build()
        btnAddImage.setOnClickListener { tryToGetImage() }

        return dialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()

        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
            .setOnClickListener {
                if (etTitle.text.isNotEmpty() && etDescription.text.isNotEmpty()) {
                    handleDiaryEntryCreate()
                    (dialog as AlertDialog).dismiss()
                } else {
                    if (etTitle.text.isNullOrEmpty()) {
                        etTitle.error =
                            "The title can not be empty"
                    }

                    if (etDescription.text.isNullOrEmpty()) {
                        etDescription.error =
                            "The description can not be empty"
                    }
                }
            }
    }

    private fun handleDiaryEntryCreate() {
        diaryEntryHandler.diaryEntryCreated(
            DiaryEntry(
                null,
                selectedDate.timeInMillis,
                etTitle.text.toString(),
                etDescription.text.toString(),
                rbPersonal.isChecked,
                etPlace.text.toString(),
                imagePath
            )
        )
    }

    private fun tryToGetLocation() {
        if (!hasLocationPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        } else if (!isGpsEnabled() && !isNetworkEnabled()) {
            askToTurnLocationServicesOn()
        } else {
            getLocation()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
                || checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    private fun getLocation() {
        client.lastLocation.addOnSuccessListener { location ->
            etPlace.setText("${location.latitude}, ${location.longitude}")
        }
    }

    private fun isGpsEnabled(): Boolean {
        val lm: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        return gpsEnabled
    }

    private fun isNetworkEnabled(): Boolean {
        val lm: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var networkEnabled = false

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        return networkEnabled
    }

    private fun askToTurnLocationServicesOn() {
        if (!isGpsEnabled() && !isNetworkEnabled()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Enable location")
                .setMessage("Location services should be enabled to use this feature")
                .setPositiveButton(
                    "Open location settings"
                ) { _, _ ->
                    requireActivity().startActivityForResult(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        ), LOCATION_SETTINGS_REQUEST
                    )
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }
    }

    private fun tryToGetImage() {
        if (!hasCameraPermissions()) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), CAMERA_PERMISSION_REQUEST
            )
        } else {
            getImage()
        }
    }

    private fun hasCameraPermissions(): Boolean {
        return checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PermissionChecker.PERMISSION_GRANTED
                && checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    private fun getImage() {
        easyImage.openChooser(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.all { result -> result == PermissionChecker.PERMISSION_GRANTED }) {
                    getLocation()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Location permission is required for this feature",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.all { result -> result == PermissionChecker.PERMISSION_GRANTED }) {
                    getImage()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Camera and storage permissions are required for this feature",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        easyImage.handleActivityResult(
            requestCode,
            resultCode,
            data,
            requireActivity(),
            object : DefaultCallback() {
                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    imagePath = imageFiles[0].file.absolutePath

                    ivAddedImage.setImageBitmap(BitmapFactory.decodeFile(imagePath))
                    ivAddedImage.visibility = View.VISIBLE
                }
            })
    }
}