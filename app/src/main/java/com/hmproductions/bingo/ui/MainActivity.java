package com.hmproductions.bingo.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.actions.GetGridSize.*;
import com.hmproductions.bingo.dagger.ChannelModule;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;

import javax.inject.Inject;

import static com.hmproductions.bingo.utils.IPAddress.getIPAddress;

public class MainActivity extends AppCompatActivity {

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    EditText hostIP_editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hostIP_editText = findViewById(R.id.hostIP_editText);

        findViewById(R.id.host_button).setOnClickListener(view -> {

                String hostIPAddress = getIPAddress(false);
                hostIP_editText.setText(hostIPAddress);

                DaggerBingoApplicationComponent.builder()
                        .contextModule(new ContextModule(this))
                        .channelModule(new ChannelModule(hostIPAddress))
                        .build().inject(this);

                Toast.makeText(this, "Server Started", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.join_button).setOnClickListener(view -> {
            if (!hostIP_editText.getText().toString().isEmpty()) {
                String hostIPAddress = hostIP_editText.getText().toString();

                DaggerBingoApplicationComponent.builder()
                        .contextModule(new ContextModule(this))
                        .channelModule(new ChannelModule(hostIPAddress))
                        .build().inject(this);

                GetGridSizeRequest gridSizeRequest = GetGridSizeRequest.newBuilder().setPlayerId(101).build();
                GetGridSizeResponse response = actionServiceBlockingStub.getGridSize(gridSizeRequest);

                Toast.makeText(this, "Total size - " + response.getSize(), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Enter host IP Address", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
