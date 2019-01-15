package com.example.user.sensormotion

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : LocationListener, AppCompatActivity() {

    private val key = "&appid=40b682d3076b48ca264e26259b0cbc77"
    private val url1 = "https://samples.openweathermap.org/data/2.5/weather?"
//    private val url2 = "&cnt=1"

    var id = 0
    var temp = 0.0f
    var hum = 0

    private lateinit var provider: String

    private var lon: String = "&lon=0"
    private var lat: String = "lat=0"

    private var millis: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT     //画面固定
        setContentView(R.layout.activity_main)


        if(ContextCompat.checkSelfPermission(application, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(application, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            val str = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

            ActivityCompat.requestPermissions(this, str, 1)
        } else {


            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                    provider = LocationManager.GPS_PROVIDER
                }

                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                    provider = LocationManager.NETWORK_PROVIDER
                }

                else -> {
                    val setting = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(setting)
                }
            }

            locationManager.requestLocationUpdates(provider, millis, 10.0f, this)
        }

        Log.d("url", url1 + lat + lon + key)

        RequestURL().execute(url1 + lat + lon + key).toString()

        button.setOnClickListener{
            val intent = Intent(this, HumidityActivity::class.java)
            intent.putExtra("hum_data", hum)
            startActivity(intent)
        }
    }

    override fun onLocationChanged(location: Location?) {
        if(location != null) {
            lat = "lat=${location.latitude}"
            lon = "&lon=${location.longitude}"

            millis = 1000 * 60 * 60
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

    inner class RequestURL: AsyncTask<String, String, String>(){
        override fun doInBackground(vararg params: String?): String {
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null
            val buffer: StringBuffer

            try {
                //上のexecute()の中にURLを入れたため配列の最初に入れられる
                val url = URL(params[0])
                connection = url.openConnection() as HttpURLConnection
                connection.connect()

                //取得した文字を代入
                val stream = connection.inputStream
                reader = BufferedReader(InputStreamReader(stream))
                buffer = StringBuffer()

                var line: String?
                while(true){
                    line = reader.readLine()
                    if(line == null) {
                        break
                    }
                    buffer.append(line)
                }

                return buffer.toString()

            } catch (e: MalformedURLException){
                return e.toString()
            } catch (e: IOException){
                return e.toString()
            }

            finally {
                connection?.disconnect()
                try {
                    reader?.close()
                } catch (e: IOException){
                    return e.toString()
                }
            }

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result == null) return
            Log.d("response", result)

            try {

                val jObj = JSONObject(result)

//                //listというキーを参照
//                val listJson = jObj.getJSONArray("list")

//                val obj = listJson.getJSONObject(0)
//                val obj = jObj.getJSONObject("weather")
                //市のid
                id = jObj.getString("id").toInt()    //取れてる


                //市の名前（ずれてる）
                val cityName = jObj.getString("name") //取れてる

                citynameText.text = "場所：$cityName"


                //mainというキーを参照
                val mObj = jObj.getJSONObject("main")

                //気温
                temp = mObj.getString("temp").toFloat() - 273.15f   //取れてる

                tempText.text = "現在温度：" + "%.1f".format(temp) + "℃"

                //湿度
                if(mObj.has("humidity")){
                    hum = mObj.getString("humidity").toInt()    //取れてる
                }

                //humText.text = "湿度：" + hum.toString() + "%"


                val weatherJSON = jObj.getJSONArray("weather")
                val iObj = weatherJSON.getJSONObject(0)

                val weather = iObj.getString("icon")


                when(weather){
                    "01d" -> weatherImage.setImageResource(R.drawable.d01)
                    "02d" -> weatherImage.setImageResource(R.drawable.d02)
                    "03d" -> weatherImage.setImageResource(R.drawable.d03)
                    "04d" -> weatherImage.setImageResource(R.drawable.d04)
                    "09d" -> weatherImage.setImageResource(R.drawable.d09)
                    "10d" -> weatherImage.setImageResource(R.drawable.d10)
                    "11d" -> weatherImage.setImageResource(R.drawable.d11)
                    "13d" -> weatherImage.setImageResource(R.drawable.d13)
                    "50d" -> weatherImage.setImageResource(R.drawable.d50)

                    "01n" -> weatherImage.setImageResource(R.drawable.n01)
                    "02n" -> weatherImage.setImageResource(R.drawable.n02)
                    "03n" -> weatherImage.setImageResource(R.drawable.d03)
                    "04n" -> weatherImage.setImageResource(R.drawable.d04)
                    "09n" -> weatherImage.setImageResource(R.drawable.d09)
                    "10n" -> weatherImage.setImageResource(R.drawable.n10)
                    "11n" -> weatherImage.setImageResource(R.drawable.d11)
                    "13n" -> weatherImage.setImageResource(R.drawable.d13)
                    "50n" -> weatherImage.setImageResource(R.drawable.d50)
                }

                val wid = iObj.getInt("id")
                val wt = "天気："

                when(wid){
                    200	-> weatherText.text = wt + "thunderstorm with light rain"
                    201	-> weatherText.text = wt + "thunderstorm with rain"
                    202	-> weatherText.text = wt + "thunderstorm with heavy rain"
                    210	-> weatherText.text = wt + "light thunderstorm"
                    211	-> weatherText.text = wt + "thunderstorm"
                    212	-> weatherText.text = wt + "heavy thunderstorm"
                    221	-> weatherText.text = wt + "ragged thunderstorm"
                    230	-> weatherText.text = wt + "thunderstorm with light drizzle"
                    231	-> weatherText.text = wt + "thunderstorm with drizzle"
                    232	-> weatherText.text = wt + "thunderstorm with heavy drizzle"

                    300	-> weatherText.text = wt + "light intensity drizzle"
                    301	-> weatherText.text = wt + "drizzle"
                    302	-> weatherText.text = wt + "heavy intensity drizzle"
                    310	-> weatherText.text = wt + "light intensity drizzle rain"
                    311	-> weatherText.text = wt + "drizzle rain"
                    312	-> weatherText.text = wt + "heavy intensity drizzle rain"
                    313	-> weatherText.text = wt + "shower rain and drizzle"
                    314	-> weatherText.text = wt + "heavy shower rain and drizzle"
                    321	-> weatherText.text = wt + "shower drizzle"

                    500	-> weatherText.text = wt + "light rain"
                    501	-> weatherText.text = wt + "moderate rain"
                    502	-> weatherText.text = wt + "heavy intensity rain"
                    503	-> weatherText.text = wt + "very heavy rain"
                    504	-> weatherText.text = wt + "extreme rain"
                    511	-> weatherText.text = wt + "freezing rain"
                    520	-> weatherText.text = wt + "light intensity shower rain"
                    521	-> weatherText.text = wt + "shower rain"
                    522	-> weatherText.text = wt + "heavy intensity shower rain"
                    531	-> weatherText.text = wt + "ragged shower rain"

                    600	-> weatherText.text = wt + "light snow"
                    601	-> weatherText.text = wt + "snow"
                    602	-> weatherText.text = wt + "heavy snow"
                    611	-> weatherText.text = wt + "sleet"
                    612	-> weatherText.text = wt + "shower sleet"
                    615	-> weatherText.text = wt + "light rain and snow"
                    616	-> weatherText.text = wt + "rain and snow"
                    620	-> weatherText.text = wt + "light shower snow"
                    621	-> weatherText.text = wt + "shower snow"
                    622	-> weatherText.text = wt + "heavy shower snow"

                    701	-> weatherText.text = wt + "mist"
                    711	-> weatherText.text = wt + "smoke"
                    721	-> weatherText.text = wt + "haze"
                    731	-> weatherText.text = wt + "sand, dust whirls"
                    741	-> weatherText.text = wt + "fog"
                    751	-> weatherText.text = wt + "sand"
                    761	-> weatherText.text = wt + "dust"
                    762	-> weatherText.text = wt + "volcanic ash"
                    771	-> weatherText.text = wt + "squalls"
                    781	-> weatherText.text = wt + "tornado"

                    800	-> weatherText.text = wt + "clear sky"

                    801	-> weatherText.text = wt + "few clouds"
                    802	-> weatherText.text = wt + "scattered clouds"
                    803	-> weatherText.text = wt + "broken clouds"
                    804	-> weatherText.text = wt + "overcast clouds"
                }

            }catch (e: JSONException){
                Log.d("MainActivity", "(error)${e}")
            }

        }

    }
}
