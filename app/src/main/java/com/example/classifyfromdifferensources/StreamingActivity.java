package com.example.classifyfromdifferensources;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StreamingActivity extends AppCompatActivity {
    EditText etIPAddress;
    EditText etPortNum;
    Button btnConnect;
    TextView tvRecTag0;
    TextView tvRecConf0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
        etIPAddress = findViewById(R.id.et_ip_adress_2);
        etPortNum = findViewById(R.id.et_port_num_2);
        btnConnect = findViewById(R.id.btn_connect_2);
        tvRecTag0 = findViewById(R.id.feed_recTag0);
        tvRecConf0 = findViewById(R.id.feed_recConf0);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ip = etIPAddress.getText().toString();
                String portNum = etPortNum.getText().toString();
                if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(portNum)) {
                    Toast.makeText(StreamingActivity.this, "Enter proper data.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent clientLiveFeed = new Intent(StreamingActivity.this, com.example.classifyfromdifferensources.ClientFeed.class);
                clientLiveFeed.putExtra("ip", ip);
                clientLiveFeed.putExtra("port", portNum);
                startActivity(clientLiveFeed);
            }
        });
    }
}
