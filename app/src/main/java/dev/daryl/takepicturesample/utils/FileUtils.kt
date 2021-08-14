package dev.daryl.takepicturesample.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    inline fun Uri.getFileName(context: Context, fileName: (String) -> Unit) {
        context.contentResolver?.let { contentResolver ->
            contentResolver.query(
                this,
                null,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        ?.let {
                            fileName(it)
                        }
                }
            }
        }
    }

    fun Uri.copyToFile(context: Context, file: File) {
        context.contentResolver?.let { contentResolver ->
            contentResolver.openInputStream(this)?.buffered()?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }


}