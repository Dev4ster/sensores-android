package com.example.projetofinalsensores;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


/**
 *
 *
 * Nome: Marcus Vinicius Teixeira Salgado RA: 1772940
 * Eduardo Justino da Silva                  8902553
 * Victor Menezes Barreto                    1692977
 *
 */
public class MainActivity extends AppCompatActivity {

    SensorManager mSensorManager;
    ListView listView;
    TextView dadosAtuais;
    EditText identificador;
    List<Sensor> deviceSensor = null;

    EditText device;

    //Localização
    Geocoder geocoder;
    Double latPoint;
    Double longPoint;
    Map<String,String> dadosDoEndereco = new HashMap<String, String>();

    //Luminosidade
    String Luminosidade ="";

    //Proximidade
    String Proximidade = "";

    //Umidade
    String Umidade = "";

    //Temperatura

    String Temperatura = "";

    Timer timer;


    String deviceName;

    Button startbutton;
    Button stopbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startbutton = findViewById(R.id.start);
        setContentView(R.layout.activity_main);
        identificador =  findViewById(R.id.device);
        dadosAtuais = findViewById(R.id.dadossensor);
        pedirPermissao(); // inicia serviço de localização
        geocoder = new Geocoder(this, Locale.getDefault());
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        LUMINOSIDADE();
        PROXIMIDADE();
        UMIDADE();
        TEMPERATURA();
        timer = new Timer();


        deviceName = new Date().getTime() + "";
    }

    public void listarsensores(View view) {

        deviceSensor = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        listView.setAdapter(new ArrayAdapter<Sensor>(this, android.R.layout.simple_list_item_1, deviceSensor));
        LUMINOSIDADE();
    }

    private boolean isEmpty(EditText editText){
        return editText.getText().toString().trim().length() == 0;
    }


    public void StartSeed(View view) {


       if(isEmpty(identificador)){
           Toast.makeText(MainActivity.this,
                   "Erro Campos vazios!", Toast.LENGTH_LONG).show();
           return;
       }

        dadosAtuais.setText("" +
                "---- DADOS ATUAIS --- \n" +
                "Sensor de Localização: Ativo  \n" +
                "Sensor Humidade:" + Umidade + "\n" +
                "Sensor Luz:"+ Luminosidade + "\n" +
                "Sensor Proximidade" + Proximidade + "\n" +
                "Sensor de Temperatura"+ Temperatura );

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    JSONObject jsonEnderecos = new JSONObject(dadosDoEndereco);

                    Map<String, Object> dadosJson =  new HashMap<String, Object>();
                    dadosJson.put("SensorLocation", jsonEnderecos);
                    dadosJson.put("SensorLight",Luminosidade);
                    dadosJson.put("SensorProximity",Proximidade);
                    dadosJson.put("SensorHumidity",Umidade);
                    dadosJson.put("SensorTemperature",Temperatura);
                    dadosJson.put("Device", deviceName + "-"+ identificador.getText().toString());
                    dadosJson.put("Date", new Date().toString());



                    JSONObject dadosFinais = new JSONObject(dadosJson);
                    Log.i("Post", "Enviando Dados");

                    post(dadosFinais);


                }
            }, 0, 5000);

    }



    public String post(final JSONObject data) {
        try {
            final URL url = new URL("https://www.vetor.tech/fmu/android/service.php");
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-type", "application/json");

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            final OutputStream outputStream = connection.getOutputStream();
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            writer.write(data.toString());
            writer.flush();
            writer.close();
            outputStream.close();

            connection.connect();

            final InputStream stream = connection.getInputStream();
            return "foooi";
        } catch (Exception e) {
            Log.e("Your tag", "Error", e);
        }

        return null;
    }




    public void StopSeed(View view){

        timer.cancel();
    }
    //sensores

    class LuzSensor implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float valor = event.values[0];
            Luminosidade = "" + valor;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public  void LUMINOSIDADE(){
        Sensor mluz;
        mluz = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(new LuzSensor(), mluz, SensorManager.SENSOR_DELAY_NORMAL);

    }

    class ProxSensor implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float valor = event.values[0];
            Proximidade = "" + valor;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public  void PROXIMIDADE(){
        Sensor mProx;
        mProx = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(new ProxSensor(), mProx, SensorManager.SENSOR_DELAY_NORMAL);

    }

    class UmiSensor implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float valor = event.values[0];
            Umidade =  "" + valor ;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public  void UMIDADE(){
        Sensor mUmi;
        mUmi = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mSensorManager.registerListener(new UmiSensor(), mUmi, SensorManager.SENSOR_DELAY_NORMAL);

    }


    class TempSensor implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float valor = event.values[0];
            Temperatura =  "" + valor ;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public  void TEMPERATURA(){
        Sensor mTemp;
        mTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mSensorManager.registerListener(new TempSensor(), mTemp, SensorManager.SENSOR_DELAY_NORMAL);

    }

    //fim sensores

    private void pedirPermissao(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 1);
        }
        else
            configurarServico();

    }

    public void configurarServico(){
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    atualizar(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {  }

                @Override
                public void onProviderDisabled(String provider) {  }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }catch(SecurityException ex){
        }
    }

    public void atualizar(Location location){
      latPoint = location.getLatitude();
      longPoint = location.getLongitude();

        //Coletar endereço completo pela lat e long
        try {

            List<Address> addresses = null;

            addresses = geocoder.getFromLocation(latPoint, longPoint,1);
            if(addresses != null && addresses.size() > 0 ){
                dadosDoEndereco.put("endereco", addresses.get(0).getAddressLine(0));
                dadosDoEndereco.put("cidade", addresses.get(0).getLocality());
                dadosDoEndereco.put("estado", addresses.get(0).getAdminArea());
                dadosDoEndereco.put("pais", addresses.get(0).getCountryName());
                dadosDoEndereco.put("cep", addresses.get(0).getPostalCode());

            }

        }catch (IOException e){
            Toast.makeText(this, "Erro ao capturar sua localização", Toast.LENGTH_LONG);
        }

//        Log.i("Sensores", "lat "+latPoint);
//        Log.i("Sensores", "lon "+longPoint);

    }

}
