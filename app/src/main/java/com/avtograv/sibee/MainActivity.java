package com.avtograv.sibee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.R.layout.simple_list_item_1;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    ToggleButton tb1, tb2, tb3, tb4;
    private TextView textTemp, textMQ135;
    private Group groupToggleButton;
    private static final int REQUEST_ENABLE_BT = 1;
    public TextView textInfo;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> pairedDeviceArrayList;
    ListView listViewPairedDevice;
    ArrayAdapter<String> pairedDeviceAdapter;
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    private UUID myUUID;
    private final StringBuilder sb = new StringBuilder();
    private String sbPrint;
    private boolean go_or_not = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTemp = findViewById(R.id.textTemperature);
        textMQ135 = findViewById(R.id.textMQ_135);

        tb1 = findViewById(R.id.toggle_button_1);
        tb2 = findViewById(R.id.toggle_button_2);
        tb3 = findViewById(R.id.toggle_button_3);
        tb4 = findViewById(R.id.toggle_button_4);

        tb1.setOnCheckedChangeListener(this);
        tb2.setOnCheckedChangeListener(this);
        tb3.setOnCheckedChangeListener(this);
        tb4.setOnCheckedChangeListener(this);

        final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
        textInfo = findViewById(R.id.textInfo);
        listViewPairedDevice = findViewById(R.id.pair_id_list);

        groupToggleButton = findViewById(R.id.group_toggle_button);


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "BLUETOOTH NOT support", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this hardware platform", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        @SuppressLint("HardwareIds")
        String stInfo = bluetoothAdapter.getName() + " : " + bluetoothAdapter.getAddress();
        textInfo.setText(String.format("Имя и IMEI вашего устройства:\n%s", stInfo));
    }

    // Запрос на включение Bluetooth
    @Override
    protected void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        setup();
    }


    // Создание списка сопряжённых Bluetooth-устройств
    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // Если есть сопряжённые устройства
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<>();

            // Добавляем сопряжённые устройства - Имя + MAC-адресс
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device.getName() + "\n" + device.getAddress());
            }
            pairedDeviceAdapter = new ArrayAdapter<>(this, simple_list_item_1, pairedDeviceArrayList);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);
            // Клик по нужному устройству
            listViewPairedDevice.setOnItemClickListener((parent, view, position, id) -> { //тут пробел после скобки !!!!

                // После клика скрываем список
                listViewPairedDevice.setVisibility(View.GONE);
                String itemValue = (String) listViewPairedDevice.getItemAtPosition(position);

                // Вычленяем MAC-адрес
                String MAC = itemValue.substring(itemValue.length() - 17);
                BluetoothDevice device2 = bluetoothAdapter.getRemoteDevice(MAC);
                myThreadConnectBTdevice = new ThreadConnectBTdevice(device2);

                // Запускаем поток для подключения Bluetooth
                myThreadConnectBTdevice.start();
            });
        }
    }

    // Закрытие приложения
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myThreadConnectBTdevice != null) myThreadConnectBTdevice.cancel();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Если разрешили включить Bluetooth, тогда void setup()
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setup();
                // Если не разрешили, тогда закрываем приложение
            } else {
                Toast.makeText(this, "BlueTooth не включён", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // click on button
    public void goGroundFloor(View view) {
        Intent intent = new Intent(MainActivity.this, GroundFloor.class);
        startActivity(intent);
        go_or_not = true;
    }


    // Поток для коннекта с Bluetooth
    private class ThreadConnectBTdevice extends Thread {
        private BluetoothSocket bluetoothSocket = null;

        private ThreadConnectBTdevice(BluetoothDevice device) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Коннект
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Нет подключения," +
                            " проверьте Bluetooth-устройство с которым хотите соединиться!", Toast.LENGTH_LONG).show();
                    listViewPairedDevice.setVisibility(View.VISIBLE);
                });
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            // Если законнектились, тогда открываем панель с кнопками и запускаем поток приёма и отправки данных
            if (success) {
                runOnUiThread(() -> {

                    // открываем панель с кнопками
                    groupToggleButton.setVisibility(View.VISIBLE);
                    textTemp.setText("Измерение" + "\n" + "температуры");
                    textMQ135.setText("Измерение" + "\n" + "содержания СO2");

                    if (myThreadConnected != null) {
                        if (textTemp.getText() == "Измерение" + "\n" + "температуры"
                                || textMQ135.getText() == "Измерение" + "\n" + "содержания СO2") {
                            // отправить запрос на измерение температуры
                            byte[] bytesToSend = "REQUEST\r\n".getBytes();
                            myThreadConnected.write(bytesToSend);
                        }
                    }
                });

                myThreadConnected = new ThreadConnected(bluetoothSocket);

                // запуск потока приёма и отправки данных
                myThreadConnected.start();
            }
        }

        public void cancel() {
            Toast.makeText(getApplicationContext(), "Close - BluetoothSocket", Toast.LENGTH_LONG).show();
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }       // END ThreadConnectBTdevice


    // Поток - приём и отправка данных
    private class ThreadConnected extends Thread {

        // InputStream - абстрактный класс, описывающий поток ввода
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;


        public ThreadConnected(BluetoothSocket socket) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectedInputStream = in;
            connectedOutputStream = out;
        }

        // Приём данных
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            while (true) {
                try {
                    byte[] buffer = new byte[1];

                    // int read(byte[] buffer) - читает байты в буфер,
                    // возвращая количество прочитанных байтов.
                    // По достижении конца файла возвращает значение -1
                    int bytes = connectedInputStream.read(buffer);
                    // задаём диапазон символьного массива
                    // указываем сам массив байтов, начало диапазона и
                    // количество символов для записи в строку.
                    String strInCom = new String(buffer, 0, bytes);

                    // собираем символы в строку
                    sb.append(strInCom);
                    // определяем конец строки
                    int endOfLineIndex = sb.indexOf("\r\n");

                    if (endOfLineIndex > 0) {
                        sbPrint = sb.substring(0, endOfLineIndex);
                        sb.delete(0, sb.length());

                        // Вывод данных
                        runOnUiThread(() -> {
                            if (sbPrint.contains("temp")) {
                                textTemp.setText(sbPrint + "\u00B0");
                                if (go_or_not) {
                                    Intent intent = new Intent(MainActivity.this, GroundFloor.class);
                                    intent.putExtra("temp_go", sbPrint + "\u00B0");
                                    startActivity(intent);
                                }
                            } else if (sbPrint.contains("MQ135")) {
                                textMQ135.setText(sbPrint);
                            }
                        });
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // отправляем данные
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.toggle_button_1:
                if (isChecked) {
                    if (myThreadConnected != null) {
                        byte[] bytesToSend = "OPEN\r\n".getBytes();
                        myThreadConnected.write(bytesToSend);
                    }
                    Toast.makeText(MainActivity.this, "KEY OPEN", Toast.LENGTH_SHORT).show();
                } else {
                    if (myThreadConnected != null) {
                        byte[] bytesToSend = "CLOSE\r\n".getBytes();
                        myThreadConnected.write(bytesToSend);
                    }
                    Toast.makeText(MainActivity.this, "KEY CLOSE", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.toggle_button_2:
                if (isChecked) {
                    if (myThreadConnected != null) {
                        byte[] bytesToSend = "b".getBytes();
                        myThreadConnected.write(bytesToSend);
                    }

//                    Toast.makeText(MainActivity.this, "D11 ON", Toast.LENGTH_SHORT).show();
                } else {
                    if (myThreadConnected != null) {

                        byte[] bytesToSend = "B".getBytes();
                        myThreadConnected.write(bytesToSend);
                    }
//                    Toast.makeText(MainActivity.this, "D11 OFF", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}