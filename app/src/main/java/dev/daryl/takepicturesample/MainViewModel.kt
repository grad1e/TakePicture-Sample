package dev.daryl.takepicturesample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel: ViewModel() {

    private val _image = MutableLiveData<File>()
    val image: LiveData<File> = _image

    fun setImage(image: File) {
        _image.postValue(image)
    }

}