package dev.daryl.takepicturesample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        val fileNamePrefix = "IMG_${System.currentTimeMillis()}"
        val fileNameSuffix = ".png"
        fileName = fileNamePrefix + fileNameSuffix

        // Creating a temporary file in order to avoid using up the space
        val tempFile = File.createTempFile(
            fileNamePrefix,
            fileNameSuffix,
            filesDir
        ).apply {
            filePath = absolutePath
        }

        // If you want a persistent file which you can use later, use this.
        // It will be stored under the app's 'files' directory
        val persistFile = File(
            filesDir,
            fileName
        )

        // Getting the Uri of the aforementioned file
        val fileUri = FileProvider.getUriForFile(
            this,
            "dev.daryl.takepicturesample.fileprovider",
            tempFile
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

                    // Decodes the file into a bitmap and uses the compress method and writes into a file
                    val bmp = BitmapFactory.decodeFile(filePath)
                    this@MainActivity.openFileOutput(fileName, MODE_PRIVATE)
                        .use { fos ->
                            bmp.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                        }

                    // The compressed file is retrieved and then passed onto
                    // the viewModel where you can use it for sending the image via retrofit
                    val fileCompressed = File(filePath)
                    viewModel.setImage(fileCompressed)
                }
            } else {
                Toast.makeText(this, "User cancelled", Toast.LENGTH_LONG).show()
            }
        }


}