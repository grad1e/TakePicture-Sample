package dev.daryl.takepicturesample.ui

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.daryl.takepicturesample.BuildConfig
import dev.daryl.takepicturesample.R
import dev.daryl.takepicturesample.data.FileListModel
import dev.daryl.takepicturesample.databinding.ActivityMainBinding
import dev.daryl.takepicturesample.utils.FileUtils.copyToFile
import dev.daryl.takepicturesample.utils.FileUtils.getFileName
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding: ActivityMainBinding by viewBinding()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fileListAdapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setupClickListeners()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        fileListAdapter = FileListAdapter {
            viewModel.removeFromFileList(it)
        }.apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            viewModel.fileList.observe(this@MainActivity) {
                submitList(it)
            }
            binding.recyclerFileList.adapter = this
        }
    }

    private fun setupClickListeners() {
        binding.btnOpenCamera.setOnClickListener {
            openCamera()
        }
        binding.btnSingleFilePicker.setOnClickListener {
            singleFilePicker.launch(arrayOf("image/*"))
        }
        binding.btnMultipleFilePicker.setOnClickListener {
            multipleFilePicker.launch(arrayOf("image/*"))
        }
    }

    private fun openCamera() {
        val file = File(filesDir, "${System.currentTimeMillis()}.jpg")
        viewModel.filePath = file.path
        FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)?.let {
            imageCapture.launch(it)
        }
    }

    private val imageCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
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
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private val singleFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            try {
                lifecycleScope.launch(Dispatchers.IO) {
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
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

    private val multipleFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
            try {
                lifecycleScope.launch(Dispatchers.IO) {
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
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

}