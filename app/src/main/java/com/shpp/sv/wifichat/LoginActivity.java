package com.shpp.sv.wifichat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.BassBoost;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

/**
 * Created by SV on 13.06.2016.
 */
public class LoginActivity extends AppCompatActivity {
    private int selectedColor;
    private EditText edtUserName;
    private Button btnSelectColor;
    private ColorPickerDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        selectedColor = ContextCompat.getColor(this, R.color.sage);;
        initViews();
        initColorPickerDialog();
    }

    private void initViews() {
        edtUserName = (EditText) findViewById(R.id.edtUserName);
        btnSelectColor = (Button) findViewById(R.id.btnSelectColor);
        if (btnSelectColor != null) {
            btnSelectColor.setBackgroundColor(selectedColor);
        }
    }

    private void initColorPickerDialog(){
        int[] colorsArray = getResources().getIntArray(R.array.default_rainbow);
        dialog = ColorPickerDialog.newInstance(R.string.ColorPickerDialogLabel,
                colorsArray,
                selectedColor,
                5,
                ColorPickerDialog.SIZE_SMALL);

        dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                selectedColor = color;
                btnSelectColor.setBackgroundColor(selectedColor);
            }
        });
    }

    public void onBtnLoginClick(View view) {
        String userName = edtUserName.getText().toString();
        if (!TextUtils.isEmpty(userName)) {
            if (wifiIsConnected()) {
                openMainActivity(userName);
            } else {
                openWifiSettings();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.msgEnterYourName),
                    Toast.LENGTH_SHORT).show();
        }

    }

    private boolean wifiIsConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifiInfo.isConnected();
    }

    private void openWifiSettings() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    private void openMainActivity(String userName) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_USER_NAME, userName);
        intent.putExtra(MainActivity.EXTRA_MESSAGE_COLOR, selectedColor);
        startActivity(intent);
    }


    public void onBtnSelectColorClick(View view) {
        dialog.show(getFragmentManager(), "color_dialog_test");
    }
}
