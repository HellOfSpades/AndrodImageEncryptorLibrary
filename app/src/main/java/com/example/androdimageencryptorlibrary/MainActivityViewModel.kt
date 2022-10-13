package com.example.androdimageencryptorlibrary

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.example.imageencryptorlibrary.encryption.PPKeyImageEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.InputStream
import java.net.URL

class MainActivityViewModel(application: Application) : AndroidViewModel(application){
    private var picture: Uri? = null
    private lateinit var imageBitmap: Bitmap
    lateinit var imageEncryptor: PPKeyImageEncryptor
    var activity: Activity? = null

    //job for using the encryptor
    private var encryptOperationJob = Job()
    //encryption scope
    private var encryptOperationScope = CoroutineScope(Dispatchers.Main+encryptOperationJob)

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setPicture(data: Uri?){
        picture = data
        if(picture!=null) {
            imageBitmap = getBitmapFromUri(picture!!)
            Timber.i("extracted bitmap")

            encryptOperationScope.launch {
                var message = "hello"
                var encryptedBitmap = imageEncryptor.encrypt(message.toByteArray(), imageBitmap)!!
                Timber.i("encrypted: "+message)
                Timber.i(String(imageEncryptor.decrypt(encryptedBitmap)!!))
            }
        }
    }

    fun getPicture(): Uri?{
        return picture
    }

    fun getBitmapFromUri(uri: Uri): Bitmap {

        val input: InputStream? = activity!!.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(input)
        if (input != null) {
            input.close()
        }
        return bitmap
    }
}