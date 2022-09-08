package com.example.androdimageencryptorlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.imageencryptorlibrary.encryption.PPKeyImageEncryptor

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var encryptor = PPKeyImageEncryptor()
        encryptor.makeKeyPair(2048)

    }
}