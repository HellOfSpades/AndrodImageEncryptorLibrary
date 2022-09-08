package com.example.imageencryptorlibrary.encryption.imageencoder

import android.graphics.Bitmap
import com.example.imageencryptorlibrary.encryption.adaptors.BitByteAdaptor


abstract class ImageEncoder(ogImage: Bitmap) {
    var image: Bitmap = ogImage.copy(ogImage.config, true)

    @Throws(TooManyBitsException::class)
    open fun encryptString(string: String) {
        encryptBytes(string.toByteArray())
    }

    @Throws(TooManyBitsException::class)
    open fun encryptBytes(bytes: ByteArray) {
        encryptBits(BitByteAdaptor.byteArrayToBitArray(bytes))
    }

    @Throws(TooManyBitsException::class)
    abstract fun encryptBits(bits: BooleanArray)


    open fun decryptToString(): String {
        return String(decryptToBytes())
    }

    open fun decryptToBytes(): ByteArray {
        return BitByteAdaptor.bitArrayToByteArray(decryptToBits())
    }

    abstract fun decryptToBits(): BooleanArray

    abstract fun getBitCapacity(): Int
}