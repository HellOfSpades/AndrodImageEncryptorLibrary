package com.example.androdimageencryptorlibrary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.androdimageencryptorlibrary.databinding.ActivityMainBinding
import com.example.imageencryptorlibrary.encryption.PPKeyImageEncryptor
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainActivityViewModel
    lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)

        var encryptor = PPKeyImageEncryptor()
        encryptor.makeKeyPair(2048)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.activity = this
        viewModel.imageEncryptor = PPKeyImageEncryptor();
        viewModel.imageEncryptor.makeKeyPair(2048)

        binding.button.setOnClickListener(){
            Timber.i("button pressed")
            onClickChooseImage()
        }
        setContentView(binding.root)
    }

    /**
     * Launcher to retrieve image from library
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private var retrieveImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data?.data != null) {
                val data: Uri? = result.data!!.data
                if (data != null) {
                    viewModel.setPicture(data)
                    binding.previewImageView.setImageURI(viewModel.getPicture())
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun onClickChooseImage() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        retrieveImageResultLauncher.launch(intent)
    }

}