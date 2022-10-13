package com.example.imageencryptorlibrary.encryption.adaptors

import java.lang.IllegalArgumentException

/**
 * author: HellOfSpades
 * adapter between bit and byte arrays
 */
class BitByteAdaptor {
    companion object {
        /**
         * converts an array of bytes into an array of bits
         * @param bytes
         * @return bits
         */
        public fun byteArrayToBitArray(bytes: ByteArray): BooleanArray {
            val bits = BooleanArray(bytes.size * 8)
            for (i in bytes.indices) {
                var b : Int = bytes[i] + 128
                for (j in 7 downTo 0) {
                    bits[(i + 1) * 8 - j - 1] = (b / Math.pow(2.0, j.toDouble())).toInt() >= 1
                    b -= if (bits[(i + 1) * 8 - j - 1]) Math.pow(2.0, j.toDouble()).toInt() else 0
                }
            }
            return bits
        }

        /**
         * converts an array of bits, into an array of bytes
         * @param bits
         * @return bytes
         */
        public fun bitArrayToByteArray(bits: BooleanArray): ByteArray {
            val bytes = ByteArray(bits.size / 8)
            for (i in bytes.indices) {
                var b: Int = -128
                for (j in 7 downTo 0) {
                    b += if (bits[8 * (i + 1) - j - 1]) Math.pow(2.0, j.toDouble()).toInt() else 0
                }
                bytes[i] = b.toByte()
            }
            return bytes
        }

        /**
         * converts an array of bits, into a single byte
         * @param bits
         * @return byte
         */
        public fun bitArrayToByte(bits: BooleanArray): Byte {
            if(bits.size!=8)throw IllegalArgumentException()

            var b: Int = -128
            for (j in 7 downTo 0) {
                b += if (bits[8 - j - 1]) Math.pow(2.0, j.toDouble()).toInt() else 0
            }
            return b.toByte()
        }
    }
}