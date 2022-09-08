package com.example.imageencryptorlibrary.encryption

import android.graphics.Bitmap
import com.example.imageencryptorlibrary.encryption.imageencoder.TooManyBitsException


interface ImageEncryptor {
    @Throws(TooManyBitsException::class)
    //returns null if there was a problem when encrypting message
    fun encrypt(message: ByteArray, image: Bitmap): Bitmap?
    //returns null if there was a problem when decrypting message
    fun decrypt(image: Bitmap): ByteArray?
    fun getSymbolCapacity(image: Bitmap): Int
}