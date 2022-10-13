package com.example.imageencryptorlibrary.encryption.imageencoder

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi

class PerColourEncoder(image: Bitmap) : ImageEncoder(image) {


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun colorToIntArray(color: Color): IntArray{
        var rgb = IntArray(4)
        rgb[0] = color.red().toInt()
        rgb[1] = color.green().toInt()
        rgb[2] = color.blue().toInt()
        rgb[3] = color.alpha().toInt()
        return rgb
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun intArrayToColor(rgb: IntArray): Color{
        if(rgb.size<4)throw IllegalArgumentException()
        var color = Color.valueOf(rgb[0].toFloat(),rgb[1].toFloat(),rgb[2].toFloat(),rgb[3].toFloat())
        return color
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun encryptBits(bits: BooleanArray) {
        //throw an exception if there are too many bits in the message

        if (bits.size > getBitCapacity()) throw TooManyBitsException(
            String.format(
                "There are %d bits in your message, however the image can only hold %d",
                bits.size,
                getBitCapacity()
            )
        )

        var bitsIndex = 0

        outer_loop@ for (i in 0 until image.height) {
            for (j in 0 until image.width) {

                val rgb: IntArray = colorToIntArray(Color.valueOf(image.getPixel(j,i)))
                var k = 0
                while (k < rgb.size - 1 && bitsIndex < bits.size) {
                    rgb[k] = newColour(rgb[k], bits[bitsIndex])
                    bitsIndex++
                    k++
                }
                image.setPixel(j, i, intArrayToColor(rgb).toArgb());
                if (bitsIndex >= bits.size) break@outer_loop
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun decryptToBits(): BooleanArray {
        //the image that will be decoded
        val bits = BooleanArray(image.width * image.height * 3)
        var bitsIndex = 0
        for (i in 0 until image.getHeight()) {
            for (j in 0 until image.getWidth()) {
                val rgb: IntArray = colorToIntArray(Color.valueOf(image.getPixel(j,i)))
                for (k in 0 until rgb.size - 1) {
                    bits[bitsIndex] = getColourBit(rgb[k])
                    bitsIndex++
                }
            }
        }
        return bits
    }

    override fun decryptToBitsIterator(): Iterator<Boolean> {
        return object :Iterator<Boolean>{

            var x = 0
            var y = 0
            var colourIndex = 0
            @RequiresApi(Build.VERSION_CODES.Q)
            var rgb: IntArray = colorToIntArray(Color.valueOf(image.getPixel(x,y)))

            override fun hasNext(): Boolean {
                return !(x==image.width-1 && y==image.height-1 && colourIndex==2)
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun next(): Boolean {

                var output = getColourBit(rgb[colourIndex])

                //increment the colour index, or if it reached the end, change to the next pixels rgb
                if(colourIndex==2){
                    x++
                    if(x==image.width){
                        x = 0;
                        y++;
                    }
                    rgb = colorToIntArray(Color.valueOf(image.getPixel(x,y)))
                    colourIndex = 0
                }else{
                    colourIndex++
                }

                return output
            }

        }
    }

    /**
     * returns the max number of bits that the provided image can hold
     */
    override fun getBitCapacity(): Int {
        return image.height * image.width * 3;
    }

    private fun newColour(oldColour: Int, bit: Boolean): Int {
        return if (bit == getColourBit(oldColour)) {
            oldColour
        } else changeColourByOne(oldColour)
    }

    /**
     * returns the bit that corresponds to the colour
     * even = 0/false
     * odd = 1/true
     * @param colour
     * @return
     */
    private fun getColourBit(colour: Int): Boolean {
        return colour % 2 != 0
    }

    /**
     * changes the colour provided by 1
     * odd numbers become even
     * even become odd
     * @param oldColour
     * @return
     */
    private fun changeColourByOne(oldColour: Int): Int {
        val newColour: Int
        newColour = if (oldColour > 0) {
            oldColour - 1
        } else {
            oldColour + 1
        }
        return newColour
    }
}