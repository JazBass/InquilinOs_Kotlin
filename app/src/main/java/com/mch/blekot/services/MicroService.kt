package com.mch.blekot.services

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.CountDownTimer
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.mch.blekot.common.Constants
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*
import kotlin.math.log10
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.days


class MicroService : Service() {

    private var continueRecording: Boolean = true
    private var mRecorder: MediaRecorder? = null
    private var decibels = 0.0
    private var decibelsHistory: Stack<Double> = Stack()
    private val TAG = "MicroService"
    private var mEMA = 0.0
    private var runner: Thread? = null

    private lateinit var file: File

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        Log.d(TAG, "Servicio creado...")

        setUpRecorder()

        //launchDecibelsMeasure()

    }

    private fun startRecorder() {

        if (file.exists()) {
            file.delete()
        }

        try {
            file.createNewFile()
        } catch (e: IOException) {
            throw IllegalStateException("Fail to create $file")
        }

        try {
            mRecorder?.start()
            Log.i(TAG, "Start Recorder")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: " + Log.getStackTraceString(e))
        }

        while (continueRecording) {
            object : CountDownTimer(5000, 1000) {
                var second = 1
                override fun onTick(p0: Long) {
                    Log.i(TAG, "$second")
                    second++
                }

                override fun onFinish() {
                    continueRecording = false
                }
            }.start()
        }

        mRecorder?.stop()
        mRecorder?.release()
        Log.i(TAG, "Stop Recorder")


    }

    private fun launchDecibelsMeasure() {
        if (runner == null) {
            runner = Thread {
                while (runner != null) {
                    try {
                        Thread.sleep(Constants.INTERVAL_GET_DECIBEL)
                        measureDecibels()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
            runner!!.start()
            Log.d("Noise", "start runner()")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Servicio iniciado...")

        startRecorder()

        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpRecorder() {
        if (mRecorder == null) {
            mRecorder = MediaRecorder() // TODO: Get media recorder
            with(mRecorder!!) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis().days}")
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOCUMENTS}/mch/"
                )
            }

            val resolver = applicationContext.contentResolver
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

            val myURI = URI(uri.toString())

            file = File(myURI)

            mRecorder!!.setOutputFile(file)
        }
        try {
            mRecorder?.prepare()
        } catch (e: IOException) {
            Log.e(
                TAG, "IOException: " +
                        Log.getStackTraceString(e)
            )
        } catch (e: SecurityException) {
            Log.e(
                "[Monkey]", "IOException: " +
                        Log.getStackTraceString(e)
            )
        }
    }


    /*----------------------------function for take ambient decibels----------------------------*/
    private fun convertDb(amplitude: Double): Double {
        /*
         *Los teléfonos celulares pueden alcanzar hasta 90 db + -
         *getMaxAmplitude devuelve un valor entre 0 y 32767 (en la mayoría de los teléfonos). eso
         *significa que si el db máximo es 90, la presión en el micrófono es 0.6325 Pascal. hace una
         *comparación con el valor anterior de getMaxAmplitude. necesitamos dividir maxAmplitude con
         *(32767/0.6325) 51805.5336 o si 100db entonces 46676.6381
         */
        val EMA_FILTER = 0.6

        /*---------------Decibels measure---------------*/
        //static final private double EMA_FILTER = 0.6;

        mEMA = EMA_FILTER * amplitude + (1.0 - EMA_FILTER) * mEMA
        //Asumiendo que la presión de referencia mínima es 0.000085 Pascal
        // (en la mayoría de los teléfonos) es igual a 0 db
        // TODO: Find out the minimum reference in Redmi
        // return 20 * (float) Math.log10((mEMAValue / 51805.5336) / 0.000028251);
        return (20 * log10(mEMA / 51805.5336 / 0.000028251) * 100).roundToLong() / 100.0
    }

    private fun measureDecibels() {
        val amplitude: Double? = mRecorder?.maxAmplitude?.toDouble()
        //Log.i("ron11", "ron11: " + String.format(Locale.US,"%.2f", amplitude));
        if (amplitude!! > 0 && amplitude < 1000000) {
            decibels = convertDb(amplitude)
            Log.i(TAG, "Decibels: $decibels")

            /*
            if (true) { // decibels > Constants.RUIDO_MIN
                if (decibelsHistory.size >= Constants.DECIBEL_DATA_LENGTH) {
                    //count = 0;
                    if (decibelMedia(decibelsHistory) > Constants.RUIDO_MIN) {
                        Ewelink.turnOffLight()
                        Log.i("dec", "turnOffLight: Aqui se apago la luz de sus ojos")
                        //decibelsHistory.clear();
                    }
                }
                //count++;
                //Log.i("dec", "push-"+count+": " + decibelsHistory.push(decibels));
            }

             */
        }
    }

    private fun decibelMedia(decHist: Stack<Double>): Double {
        var med = 0.00
        val sizeTmp: Int = decHist.size
        while (!decHist.isEmpty()) {
            med += decHist.pop()
        }
        return (med / sizeTmp * 100.0 / 100.0).roundToLong().toDouble()
    }

}