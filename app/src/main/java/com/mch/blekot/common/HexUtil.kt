package com.mch.blekot.common

import android.util.Log
import kotlin.jvm.JvmOverloads
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.util.*
import kotlin.experimental.and
import kotlin.math.roundToInt

object HexUtil {
    private val DIGITS_LOWER = charArrayOf(
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    )
    private val DIGITS_UPPER = charArrayOf(
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    )

    @JvmOverloads
    fun encodeHex(data: ByteArray?, toLowerCase: Boolean = true): CharArray? {
        return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
    }

    @ExperimentalUnsignedTypes
    fun ByteArray.toHexString() =
        asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

    private fun encodeHex(data: ByteArray?, toDigits: CharArray): CharArray? {
        if (data == null) return null
        val l = data.size
        val out = CharArray(l shl 1)
        var i = 0
        var j = 0
        while (i < l) {
            out[j++] = toDigits[0xF0 and data[i].toInt() ushr 4]
            out[j++] = toDigits[0x0F and data[i].toInt()]
            i++
        }
        return out
    }

    @JvmOverloads
    fun encodeHexStr(data: ByteArray?, toLowerCase: Boolean = true): String {
        return encodeHexStr(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
    }

    private fun encodeHexStr(data: ByteArray?, toDigits: CharArray): String {
        return String(encodeHex(data, toDigits)!!)
    }

    @JvmOverloads
    fun formatHexString(data: ByteArray?, addSpace: Boolean = false): String? {
        if (data == null || data.isEmpty()) return null
        val sb = StringBuilder()
        for (i in data.indices) {
            var hex = Integer.toHexString((data[i] and 0xFF.toByte()).toInt())
            if (hex.length == 1) {
                hex = "0$hex"
            }
            sb.append(hex)
            if (addSpace) sb.append(" ")
        }
        return sb.toString().trim { it <= ' ' }
    }

    fun decodeHex(data: CharArray): ByteArray {
        val len = data.size
        if (len and 0x01 != 0) {
            throw RuntimeException("Odd number of characters.")
        }
        val out = ByteArray(len shr 1)

        // two characters form the hex value.
        var i = 0
        var j = 0
        while (j < len) {
            var f = toDigit(data[j], j) shl 4
            j++
            f = f or toDigit(data[j], j)
            j++
            out[i] = (f and 0xFF).toByte()
            i++
        }
        return out
    }

    private fun toDigit(ch: Char, index: Int): Int {
        val digit = ch.digitToIntOrNull(16) ?: -1
        if (digit == -1) {
            throw RuntimeException(
                "Illegal hexadecimal character " + ch
                        + " at index " + index
            )
        }
        return digit
    }

    fun hexStringToBytes(hexString: String?): ByteArray? {
        var hexString = hexString
        if (hexString == null || hexString == "") {
            return null
        }
        hexString = hexString.trim { it <= ' ' }
        hexString = hexString.uppercase(Locale.getDefault())
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            d[i] = ( charToByte(hexChars[pos]) shl 4 or charToByte(hexChars[pos + 1]).toInt()).toByte()
        }
        return d
    }

    private fun charToByte(c: Char): Byte {
        return "0123456789ABCDEF".indexOf(c).toByte()
    }

    fun extractData(data: ByteArray, position: Int): String? {
        return formatHexString(byteArrayOf(data[position]))
    }

    fun splitByte(data: ByteArray, count: Int): Queue<ByteArray> {
        if (count > 20) {
            // BleLog.w("Be careful: split count beyond 20! Ensure MTU higher than 23!");
            Log.i("HexUtil", "Be careful: split count beyond 20! Ensure MTU higher than 23!")
        }
        val byteQueue: Queue<ByteArray> = LinkedList()
        val pkgCount = if (data.size % count == 0) {
            data.size / count
        } else {
            (data.size / count + 1).toFloat().roundToInt()
        }
        if (pkgCount > 0) {
            for (i in 0 until pkgCount) {
                var dataPkg: ByteArray
                var j: Int
                if (pkgCount == 1 || i == pkgCount - 1) {
                    j = if (data.size % count == 0) count else data.size % count
                    System.arraycopy(data, i * count, ByteArray(j).also { dataPkg = it }, 0, j)
                } else {
                    System.arraycopy(
                        data,
                        i * count,
                        ByteArray(count).also { dataPkg = it },
                        0,
                        count
                    )
                }
                byteQueue.offer(dataPkg)
            }
        }
        return byteQueue
    }
}

private infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)