package com.example.androdimageencryptorlibrary

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.AndroidViewModel
import com.example.imageencryptorlibrary.encryption.PPKeyImageEncryptor
import com.example.imageencryptorlibrary.encryption.imageencoder.CantDecryptImageException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.InputStream
import java.net.URL

/**
 * this is used for testing the library
 */
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
                var message = "Lizards typically have rounded torsos, elevated heads on short necks, four limbs and long tails, although some are legless.[4] Lizards and snakes share a movable quadrate bone, distinguishing them from the rhynchocephalians, which have more rigid diapsid skulls.[5] Some lizards such as chameleons have prehensile tails, assisting them in climbing among vegetation.[6]\n" +
                        "\n" +
                        "As in other reptiles, the skin of lizards is covered in overlapping scales made of keratin. This provides protection from the environment and reduces water loss through evaporation. This adaptation enables lizards to thrive in some of the driest deserts on earth. The skin is tough and leathery, and is shed (sloughed) as the animal grows. Unlike snakes which shed the skin in a single piece, lizards slough their skin in several pieces. The scales may be modified into spines for display or protection, and some species have bone osteoderms underneath the scales.[6][7]\n" +
                        "\n" +
                        "\n" +
                        "Red tegu (Tupinambis rufescens) skull, showing teeth of differing types\n" +
                        "The dentitions of lizards reflect their wide range of diets, including carnivorous, insectivorous, omnivorous, herbivorous, nectivorous, and molluscivorous. Species typically have uniform teeth suited to their diet, but several species have variable teeth, such as cutting teeth in the front of the jaws and crushing teeth in the rear. Most species are pleurodont, though agamids and chameleons are acrodont.[8][6]\n" +
                        "\n" +
                        "The tongue can be extended outside the mouth, and is often long. In the beaded lizards, whiptails and monitor lizards, the tongue is forked and used mainly or exclusively to sense the environment, continually flicking out to sample the environment, and back to transfer molecules to the vomeronasal organ responsible for chemosensation, analogous to but different from smell or taste. In geckos, the tongue is used to lick the eyes clean: they have no eyelids. Chameleons have very long sticky tongues which can be extended rapidly to catch their insect prey.[6]\n" +
                        "\n" +
                        "Three lineages, the geckos, anoles, and chameleons, have modified the scales under their toes to form adhesive pads, highly prominent in the first two groups. The pads are composed of millions of tiny setae (hair-like structures) which fit closely to the substrate to adhere using van der Waals forces; no liquid adhesive is needed.[9] In addition, the toes of chameleons are divided into two opposed groups on each foot (zygodactyly), enabling them to perch on branches as birds do.[a][6]"

                var encryptedBitmap = imageEncryptor.encrypt(message.toByteArray(), imageBitmap)!!
                try {
                    var decryptedMessage = String(imageEncryptor.decrypt(imageBitmap)!!)
                    Timber.i(decryptedMessage)
                }catch (e: CantDecryptImageException){
                    Timber.i("image could not be decrypted")
                }

            }
        }
    }

    private fun compareBitmaps(imageBitmap: Bitmap, encryptedBitmap: Bitmap) {
        var sameDimen = imageBitmap.height==encryptedBitmap.height && imageBitmap.width==encryptedBitmap.width
        Timber.i(if(sameDimen) "Bitmaps have the same dimensions" else "Bitmaps don't have the same dimensions")

        if(!sameDimen)return

        for(i in 0..imageBitmap.height-1){
            for(n in 0..imageBitmap.width-1){
                val imagePixel = imageBitmap.getPixel(n, i)
                val encryptedPixel = encryptedBitmap.getPixel(n, i)

                if(imagePixel!=encryptedPixel){
                    Timber.i("y: $i x: $n | difference = "+(imagePixel-encryptedPixel))
                    Timber.i("original: $imagePixel red: "+imagePixel.red+" green: "+imagePixel.green+" blue: "+imagePixel.blue)
                    Timber.i("encrypted: $encryptedPixel red: "+encryptedPixel.red+" green: "+encryptedPixel.green+" blue: "+encryptedPixel.blue)
                }
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