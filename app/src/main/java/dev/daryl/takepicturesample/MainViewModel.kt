package dev.daryl.takepicturesample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {

    private val _image = MutableLiveData<File>()
    val image: LiveData<File> = _image

    fun setImage(image: File) {
        _image.postValue(image)
    }

}