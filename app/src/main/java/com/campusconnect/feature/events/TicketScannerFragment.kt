package com.campusconnect.feature.events

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.campusconnect.core.common.Constants
import com.campusconnect.databinding.FragmentTicketScannerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class TicketScannerFragment : Fragment() {

    private var _binding: FragmentTicketScannerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firestore: FirebaseFirestore

    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = true
    private var cameraProvider: ProcessCameraProvider? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Camera Permission Required")
                .setMessage("We need access to the camera to scan event tickets.")
                .setPositiveButton("OK") { _, _ -> findNavController().navigateUp() }
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QRCodeAnalyzer { text ->
                        if (isScanning) {
                            isScanning = false
                            viewLifecycleOwner.lifecycleScope.launch {
                                handleScannedTicket(text)
                            }
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private suspend fun handleScannedTicket(ticketText: String) {
        // Expected format: TICKET_<eventId>_<userUidPrefixOrUid>
        // Let's parse it
        val parts = ticketText.split("_")
        if (parts.size < 3 || parts[0] != "TICKET") {
            showResultDialog("Invalid Ticket", "This QR Code is not a valid CampusConnect ticket pass.", false)
            return
        }

        val eventId = parts[1]
        val studentUid = parts[2] // Since student details contain the UID, let's query all students or registration list

        try {
            val eventDoc = withContext(Dispatchers.IO) {
                firestore.collection(Constants.COLLECTION_EVENTS).document(eventId).get().await()
            }

            if (!eventDoc.exists()) {
                showResultDialog("Event Not Found", "The event associated with this ticket does not exist.", false)
                return
            }

            val eventName = eventDoc.getString("title") ?: "Event"
            val registeredUsers = eventDoc.get("registeredUsers") as? List<String> ?: emptyList()

            // Find matching UID from registered users (matching by prefix or full UID)
            val matchedUid = registeredUsers.find { it.startsWith(studentUid) || studentUid.startsWith(it) }

            if (matchedUid != null) {
                // Get student's details
                val studentDoc = withContext(Dispatchers.IO) {
                    firestore.collection(Constants.COLLECTION_USERS).document(matchedUid).get().await()
                }
                val studentName = studentDoc.getString("name") ?: "Student"

                // Add to checkedInUsers set on event doc
                withContext(Dispatchers.IO) {
                    firestore.collection(Constants.COLLECTION_EVENTS).document(eventId)
                        .update("checkedInUsers", FieldValue.arrayUnion(matchedUid))
                        .await()
                }

                showResultDialog(
                    "Admission Successful",
                    "Welcome, $studentName!\nTicket verified successfully for:\n$eventName",
                    true
                )
            } else {
                showResultDialog(
                    "Access Denied",
                    "This student is not registered for $eventName.",
                    false
                )
            }
        } catch (e: Exception) {
            showResultDialog("Verification Error", "Failed to verify ticket: ${e.message}", false)
        }
    }

    private fun showResultDialog(title: String, message: String, isSuccess: Boolean) {
        activity?.runOnUiThread {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(if (isSuccess) android.R.drawable.presence_online else android.R.drawable.presence_busy)
                .setCancelable(false)
                .setPositiveButton("Scan Next") { _, _ ->
                    isScanning = true
                }
                .setNegativeButton("Done") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        _binding = null
    }

    private class QRCodeAnalyzer(private val onQrCodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {

        private val reader = MultiFormatReader().apply {
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
            )
            setHints(hints)
        }

        override fun analyze(image: ImageProxy) {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val rowStride = plane.rowStride
            val width = image.width
            val height = image.height
            
            // Extract contiguous luminance bytes from the Y plane (avoiding row stride offsets)
            val size = width * height
            val nv21 = ByteArray(size)
            for (y in 0 until height) {
                buffer.position(y * rowStride)
                buffer.get(nv21, y * width, width)
            }

            val source = PlanarYUVLuminanceSource(
                nv21, width, height,
                0, 0, width, height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decode(binaryBitmap)
                onQrCodeDetected(result.text)
            } catch (e: Exception) {
                // Ignore decoding failures
            } finally {
                image.close()
            }
        }
    }
}
