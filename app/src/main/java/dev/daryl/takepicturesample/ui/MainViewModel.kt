package dev.daryl.takepicturesample.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.daryl.takepicturesample.data.FileListModel

class MainViewModel : ViewModel() {

    var filePath: String? = null
    private val _fileList = MutableLiveData<List<FileListModel?>?>(emptyList())
    val fileList: LiveData<List<FileListModel?>?> get() = _fileList

    fun addToFileList(item: FileListModel) {
        val list = _fileList.value?.toMutableList()
        list?.add(item)
        _fileList.value = list
    }

    fun removeFromFileList(item: FileListModel) {
        val list = _fileList.value?.toMutableList()
        list?.remove(item)
        _fileList.value = list
    }


}