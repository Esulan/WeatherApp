package com.example.user.sensormotion

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_humidity.*

class HumidityActivity : AppCompatActivity() , SensorEventListener, SurfaceHolder.Callback{

    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0
    private var h: Float = 1700.0f    //0.0が100%、1700.0が0%
    private var x: Float = 0.0f     //加速度
    private var hu: Int = 0     //湿度

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        val intent = getIntent()
        hu = intent.extras.getInt("hum_data")

        h *= (1.0f - hu / 100.0f)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) return
        when(event.sensor.type){

            Sensor.TYPE_ACCELEROMETER -> {
                x = -event.values[0]
                drawCanvas(x)
            }

            else -> {
                return
            }

        }
    }

    private fun drawCanvas(x: Float){

        var f: Float =
                if(h < 170.0f){
                    h * 0.17f
                } else if(h > 1530.0f){
                    (1700.0f - h) * 0.17f
                } else {
                    30.0f
                }

        val canvas = surfaceView.holder.lockCanvas()
        canvas.drawColor(Color.argb(255, 170, 170, 200))

        canvas.drawLine(-500.0f, surfaceHeight / 1.65f + x * f + h,
                surfaceWidth.toFloat() + 500.0f, surfaceHeight / 1.65f - x * f + h,
                Paint().apply{
            color = Color.argb(250, 100, 100, 200)
            strokeWidth = 2000.0f
        })

        h_text.text = "$hu%"

        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT     //画面固定
        setContentView(R.layout.activity_humidity)
        val holder = surfaceView.holder
        holder.addCallback(this)
    }
}
