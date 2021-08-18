package dev.daryl.takepicturesample.ui

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.daryl.takepicturesample.R
import dev.daryl.takepicturesample.data.FileListModel
import dev.daryl.takepicturesample.databinding.ActivityMainBinding
import dev.daryl.takepicturesample.utils.FileUtils.copyToFile
import dev.daryl.takepicturesample.utils.FileUtils.getFileName
import dev.daryl.takepicturesample.utils.FileUtils.getUri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding: ActivityMainBinding by viewBinding()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fileListAdapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        removeCacheAndFiles()
        setupClickListeners()
        setupRecyclerView()
    }

    private fun removeCacheAndFiles() {
        cacheDir.deleteRecursively()
        filesDir.deleteRecursively()
    }

    private fun setupRecyclerView() {
        fileListAdapter = FileListAdapter(
            onItemClicked = { onRecyclerViewItemClicked(it) },
            onDeletePressed = { viewModel.removeFromFileList(it) }
        ).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            viewModel.fileList.observe(this@MainActivity) {
                submitList(it)
            }
            binding.recyclerFileList.adapter = this
        }
    }

    private fun setupClickListeners() {
        binding.btnOpenCameraPicture.setOnClickListener {
            openCameraPicture()
        }
        binding.btnOpenCameraVideo.setOnClickListener {
            openCameraVideo()
        }
        binding.btnSingleFilePicker.setOnClickListener {
            singleFilePicker.launch(arrayOf("image/*"))
        }
        binding.btnMultipleFilePicker.setOnClickListener {
            multipleFilePicker.launch(arrayOf("image/*"))
        }
    }

    private fun openCameraPicture() {
        val file = File(filesDir, "${System.currentTimeMillis()}.jpg")
        viewModel.filePath = file.path
        val fileUri = file.getUri(this)
        imageCapture.launch(fileUri)
    }

    private val imageCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (it) {
                    viewModel.filePath?.let { filePath ->
                        val file = Compressor.compress(this@MainActivity, File(filePath)) {
                            resolution(500, 500)
                        }
                        withContext(Dispatchers.Main) {
                            val fileModel = FileListModel(
                                file.name,
                                file
                            )
                            viewModel.addToFileList(fileModel)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun openCameraVideo() {
        val file = File(filesDir, "${System.currentTimeMillis()}.mp4")
        viewModel.filePath = file.path
        val fileUri = file.getUri(this)
        videoCapture.launch(fileUri)
    }

    private val videoCapture = registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (it) {
                    viewModel.filePath?.let { filePath ->
                        val file = File(filePath)
                        withContext(Dispatchers.Main) {
                            val fileModel = FileListModel(
                                file.name,
                                file
                            )
                            viewModel.addToFileList(fileModel)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private val singleFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    it?.let { uri ->
                        uri.getFileName(this@MainActivity) { fileName ->
                            val file = File(filesDir, fileName)
                            uri.copyToFile(this@MainActivity, file)
                            Compressor.compress(this@MainActivity, file) {
                                resolution(500, 500)
                            }.let {
                                withContext(Dispatchers.Main) {
                                    val fileModel = FileListModel(
                                        it.name,
                                        it
                                    )
                                    viewModel.addToFileList(fileModel)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

    private val multipleFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    it.forEach { uri ->
                        uri?.let { fileUri ->
                            fileUri.getFileName(this@MainActivity) { fileName ->
                                val file = File(filesDir, fileName)
                                fileUri.copyToFile(this@MainActivity, file)
                                Compressor.compress(this@MainActivity, file) {
                                    resolution(500, 500)
                                }.let {
                                    withContext(Dispatchers.Main) {
                                        val fileModel = FileListModel(
                                            it.name,
                                            it
                                        )
                                        viewModel.addToFileList(fileModel)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

    private fun onRecyclerViewItemClicked(item: FileListModel) {
        try {
            val fileUri = item.file.getUri(this)
            Intent(Intent.ACTION_VIEW).apply {
                data = fileUri
                clipData = ClipData.newRawUri("", fileUri)
                flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                startActivity(Intent.createChooser(this, "Please choose an app"))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

}