package dev.daryl.takepicturesample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import dev.daryl.takepicturesample.databinding.ActivityMainBinding
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding: ActivityMainBinding by viewBinding()
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setupClickListeners()
        setupObservers()
    }

    private fun setupObservers() {
        // Observes the viewModel variable for the captured file and then loads into the imageView
        viewModel.image.observe(this) {
            binding.imageView.load(it)
        }
    }

    private fun setupClickListeners() {
        binding.btn.setOnClickListener {
            openCamera()
        }
    }

    private var fileName: String? = null
    private var filePath: String? = null
    private fun openCamera() {
        fileName = "IMG_${System.currentTimeMillis()}.jpg"

        // If you want a persistent file which you can use later, use this.
        // It will be stored under the app's 'files' directory
        val persistFile = File(
            filesDir,
            fileName
        ).apply {
            filePath = this.path
        }

        // Getting the Uri of the aforementioned file
        val fileUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            persistFile
        )

        // Launches the intent
        openCameraActivity.launch(fileUri)
    }

    private val openCameraActivity =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            // If the captured image (uncompressed) has been successfully written to the file TRUE is returned
            if (it) {

                // Launching a coroutine to compress the file in a background thread
                lifecycleScope.launch(Dispatchers.IO) {

                    // Uses the Zelory Compressor
                    File(filePath).let { file ->
                        val compressedImageFile =
                            Compressor.compress(this@MainActivity, file) {
                                quality(50)
                            }

                        // The compressed file is retrieved and then passed onto
                        // the viewModel where you can use it for sending the image via retrofit
                        viewModel.setImage(compressedImageFile)
                    }
                }
            } else {
                Toast.makeText(this, "User cancelled", Toast.LENGTH_LONG).show()
            }
        }


}