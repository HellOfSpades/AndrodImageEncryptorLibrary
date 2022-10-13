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

    open fun decryptToByteIterator(): Iterator<Byte> {
        return object :Iterator<Byte>{
            var bitsIterator = decryptToBitsIterator()
            var nextByte: Byte?
            init {
                nextByte = getNextByteFromIterator()
            }

            override fun hasNext(): Boolean {
                return nextByte!=null
            }

            override fun next(): Byte {
                var output = nextByte!!
                nextByte = getNextByteFromIterator()
                return output
            }

            private fun getNextByteFromIterator(): Byte?{
                var bits: BooleanArray = BooleanArray(8)
                var index = 0
                while (bitsIterator.hasNext() && index<bits.size){
                    bits[index] = bitsIterator.next()
                    index++
                }

                if(index<7)return null
                else return BitByteAdaptor.bitArrayToByte(bits)
            }

        }
    }

    abstract fun decryptToBitsIterator(): Iterator<Boolean>

    abstract fun getBitCapacity(): Int
}