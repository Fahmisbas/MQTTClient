package com.acsl.mqttclient;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Tambahkan baris dibawah ini
    public String color;
    public MqttAndroidClient client;
    public TextView textVoltageRed;
    public TextView textVoltageGreen;
    public TextView textVoltageWhite;
    public String val;
    public String[] vals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button ledSwitchRed = (Button) findViewById(R.id.ledSwitchRed);
        Button ledSwitchGreen = (Button) findViewById(R.id.ledSwitchGreen);
        Button ledSwitchWhite = (Button) findViewById(R.id.ledSwitchWhite);

        textVoltageRed = (TextView) findViewById(R.id.textViewRed);
        textVoltageGreen = (TextView) findViewById(R.id.textViewGreen);
        textVoltageWhite = (TextView) findViewById(R.id.textViewWhite);

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        final MemoryPersistence memPer = new MemoryPersistence();
        final String clientId = MqttClient.generateClientId();

        client = new MqttAndroidClient(
                this.getApplicationContext(),
                "tcp://192.168.1.143:1883",
                clientId,
                memPer);
        try {

            client.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    client.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }

         /*
          Jika menerima inputan click pada tombol ledSwitchRed,
          maka akan memerintahkan perangkat NodeMCU dengan ID "1"
          untuk menyalakan/mematikan LED nya. (LED Warna Merah)
        */
        ledSwitchRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color = "1";
                sendCommand(client, clientId, memPer);
            }
        });

        /*
          Jika menerima inputan click pada tombol ledSwitchGreen,
          maka akan memerintahkan perangkat NodeMCU dengan ID "2"
          untuk menyalakan/mematikan LED nya. (LED Warna Hijau)
        */
        ledSwitchGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color = "2";
                sendCommand(client, clientId, memPer);
            }
        });


        /*
          Jika menerima inputan click pada tombol ledSwitchWhite,
          maka akan memerintahkan perangkat NodeMCU dengan ID "3"
          untuk menyalakan/mematikan LED nya. (LED Warna Putih)
        */
        ledSwitchWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color = "3";
                sendCommand(client, clientId, memPer);
            }
        });

    }

    public void subscribeToTopic() {
        try {
            client.subscribe("data", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("dari app", "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("dari app", "Failed to subscribe");
                }
            });


            client.subscribe("data", 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    val = new String(message.getPayload());
                    vals = val.split(" ");
                    if (vals[0].equals("1")) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                textVoltageRed.setText(vals[1]);
                            }
                        });
                    } else if (vals[0].equals("2")) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                textVoltageGreen.setText(vals[1]);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                textVoltageWhite.setText(vals[1]);
                            }
                        });
                    }
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private void sendCommand (
            final MqttAndroidClient client,
            String clientId,
            MemoryPersistence memPer){

        Log.i("MSSG", "SMG");
        try {
            client.connect(null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken mqttToken) {
                    String messageToSend;

                    messageToSend = color + " a";

                    MqttMessage message = new MqttMessage(messageToSend.getBytes());
                    message.setQos(2);
                    message.setRetained(false);

                    try {
                        client.publish("command", message);
                        Log.i("MSSG", "Message published");
                    } catch (MqttPersistenceException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (MqttException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
                    // TODO Auto-generated method stub
                    Log.i("MSSG", "Client connection failed: "+arg1.getMessage());

                }
            });
        }
        catch (MqttException e){
            e.printStackTrace();
        }
    }
}
