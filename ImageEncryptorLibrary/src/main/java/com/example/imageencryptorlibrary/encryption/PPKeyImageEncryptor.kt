package com.example.imageencryptorlibrary.encryption

import android.graphics.Bitmap
import com.example.imageencryptorlibrary.encryption.imageencoder.ImageEncoder
import com.example.imageencryptorlibrary.encryption.imageencoder.PerColourEncoder
import com.example.imageencryptorlibrary.encryption.imageencoder.TooManyBitsException
import java.lang.Exception
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.jvm.Throws


class PPKeyImageEncryptor: ImageEncryptor {

    private var privateKey: RSAPrivateKey? = null
    private var publicKey: RSAPublicKey? = null

    fun getPrivateKey(): RSAPrivateKey?{
        return privateKey
    }
    fun getPublicKey(): RSAPublicKey?{
        return publicKey
    }


    fun makeKeyPair(keySize: Int) {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(keySize, SecureRandom())
            val keyPair = keyPairGenerator.generateKeyPair()
            privateKey = keyPair.private as RSAPrivateKey
            publicKey = keyPair.public as RSAPublicKey
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    fun setPublicKey(modulus: BigInteger, publicExponent: BigInteger) {
        try {
            val factory = KeyFactory.getInstance("RSA")
            publicKey =
                factory.generatePublic(RSAPublicKeySpec(modulus, publicExponent)) as RSAPublicKey
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    fun setPublicPrivateKey(
        modulus: BigInteger,
        publicExponent: BigInteger,
        privateExponent: BigInteger) {
        try {
            val factory = KeyFactory.getInstance("RSA")
            publicKey =
                factory.generatePublic(RSAPublicKeySpec(modulus, publicExponent)) as RSAPublicKey
            privateKey = factory.generatePrivate(
                RSAPrivateKeySpec(
                    modulus,
                    privateExponent
                )
            ) as RSAPrivateKey
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    override fun encrypt(message: ByteArray, image: Bitmap): Bitmap? {
        val encoder = createImageEncoder(image)
        if (message.size > getSymbolCapacity(encoder)) throw TooManyBitsException()

        try {
            val aesEncryptor = AesEncryptor()
            //the message encrypted with the AES algorithm
            val encryptedMessage: ByteArray = aesEncryptor.encrypt(message)
            val messageLength =
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(encryptedMessage.size)
                    .array()
            val aesParameters: ByteArray = aesEncryptor.parameters

            //putting the message length and aes parameters into a single byte array so they can be encoded together
            val encodedWithRsa = ByteArray(aesParameters.size + messageLength.size)
            for (i in aesParameters.indices) {
                encodedWithRsa[i] = aesParameters[i]
            }
            for (i in messageLength.indices) {
                encodedWithRsa[i + aesParameters.size] = messageLength[i]
            }
            val rsaCipher = Cipher.getInstance("RSA")
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            //encrypted AES cipher parameters with the RSA algorithm
            val encryptedAesParameters = rsaCipher.doFinal(encodedWithRsa)
            //combined message that will be encoded into the image
            //first comes the encrypted AES parameters (48 bytes)
            //then comes the encrypted message
            val combinedMessage = ByteArray(encryptedMessage.size + encryptedAesParameters.size)
            for (i in encryptedAesParameters.indices) {
                combinedMessage[i] = encryptedAesParameters[i]
            }
            for (i in encryptedMessage.indices) {
                combinedMessage[i + encryptedAesParameters.size] = encryptedMessage[i]
            }
            encoder.encryptBytes(combinedMessage)
            return encoder.image
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(CantDecryptImageException::class)
    override fun decrypt(image: Bitmap): ByteArray{
        if (privateKey == null) throw UnsupportedOperationException()
        try {
            val imageEncoder = createImageEncoder(image)
            val encryptedAesParametersAndMessageLength =
                ByteArray(publicKey!!.modulus.toByteArray().size - 1)

            val imageByteIterator = imageEncoder.decryptToByteIterator()
            //changed to iterator
            for (i in encryptedAesParametersAndMessageLength.indices) {
                encryptedAesParametersAndMessageLength[i] = imageByteIterator.next()
            }

            val rsaCipher = Cipher.getInstance("RSA")
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val rsaDecoded = rsaCipher.doFinal(encryptedAesParametersAndMessageLength)
            val aesParameters = ByteArray(48)
            val messageLength = ByteArray(4)
            for (i in aesParameters.indices) {
                aesParameters[i] = rsaDecoded[i]
            }
            for (i in messageLength.indices) {
                messageLength[i] = rsaDecoded[i + aesParameters.size]
            }

            val length = ByteBuffer.wrap(messageLength).order(ByteOrder.LITTLE_ENDIAN).int
            val aesEncryptor = AesEncryptor(aesParameters)
            //check if the device can handle creating a byte array fo this size
            val runtime = Runtime.getRuntime()
            if(runtime.freeMemory()<=length)throw CantDecryptImageException()
            //bytes of the encryptedMessage
            val encryptedMessage = ByteArray(length)
            //changed to iterator
            for (i in 0 until length) {
                encryptedMessage[i] = imageByteIterator.next()
            }
            return aesEncryptor.decrypt(encryptedMessage)
        }catch (e: Exception){
            //.printStackTrace()
            throw CantDecryptImageException()
        }
    }
    /**
     *
     * @param image
     * @return returns the maximum number of symbols that can be encoded in the given image
     */
    override fun getSymbolCapacity(image: Bitmap): Int {
        val imageEncoder: ImageEncoder = createImageEncoder(image)
        return getSymbolCapacity(imageEncoder)
    }
    /**
     *
     * @param imageEncoder
     * @return returns the symbol capacity based on the ImageEncoder
     */
    private fun getSymbolCapacity(imageEncoder: ImageEncoder): Int {
        var byteCopacity = imageEncoder.getBitCapacity() / 8
        //subtract the space needed for the encrypted AES key and message length
        byteCopacity -= publicKey!!.modulus.toByteArray().size
        //AES Encryption has an output length in multiples of 16, so we round down
        byteCopacity /= 16
        byteCopacity *= 16
        return byteCopacity
    }
    /**
     *
     * @param image
     * @return an ImageEncoder that was created using the image
     */
    private fun createImageEncoder(image: Bitmap): ImageEncoder {
        return PerColourEncoder(image)
    }


    /**
     * This class is used to code the message itself
     * then the generated iv and key are encrypted with the rsa algorithm
     */
    private class AesEncryptor {
        private var iv: IvParameterSpec
        private var key: SecretKey?

        constructor(iv: IvParameterSpec, key: SecretKey?) {
            this.iv = iv
            this.key = key
        }

        constructor(parameters: ByteArray) {
            val keyBytes = ByteArray(32)
            val ivBytes = ByteArray(16)
            for (i in keyBytes.indices) {
                keyBytes[i] = parameters[i]
            }
            for (i in keyBytes.size until parameters.size) {
                ivBytes[i - keyBytes.size] = parameters[i]
            }
            key = SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")
            iv = IvParameterSpec(ivBytes)
        }

        val parameters: ByteArray
            get() {
                val bytes = ByteArray(48)
                val keyBytes = key!!.encoded
                val ivBytes = iv.iv
                for (i in keyBytes.indices) {
                    bytes[i] = keyBytes[i]
                }
                for (i in ivBytes.indices) {
                    bytes[i + keyBytes.size] = ivBytes[i]
                }
                return bytes
            }

        constructor() {
            iv = generateAesIv()
            key = generateAesKey()
        }

        fun decrypt(bytes: ByteArray): ByteArray {
            try {
                val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                aesCipher.init(Cipher.DECRYPT_MODE, key, iv)
                return aesCipher.doFinal(bytes)
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            } catch (e: BadPaddingException) {
                e.printStackTrace()
            }
            return bytes
        }

        fun encrypt(bytes: ByteArray): ByteArray {
            try {
                val aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                aesCipher.init(Cipher.ENCRYPT_MODE, key, iv)
                return aesCipher.doFinal(bytes)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            } catch (e: BadPaddingException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            }
            return ByteArray(0)
        }

        private fun generateAesKey(): SecretKey? {
            return try {
                val keyGenerator = KeyGenerator.getInstance("AES")
                keyGenerator.init(256)
                keyGenerator.generateKey()
            } catch (e: NoSuchAlgorithmException) {
                null
            }
        }

        private fun generateAesIv(): IvParameterSpec {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return IvParameterSpec(bytes)
        }
    }
}