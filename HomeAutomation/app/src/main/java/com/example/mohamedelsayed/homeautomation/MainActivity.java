package com.example.mohamedelsayed.homeautomation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String IPAddress = "IP_ADDRESS";
    public final static String PortNumber = "PORT_NUMBER";
    public final static String Values = "Reading";

    Button btnRED, btnGreen, btnYellow, btnRef;
    TextView SRED, SGreen, SYellow;
    EditText txtIPAddress, txtPortNumber;

    TextView txtTempC, txtTempF, txtHum, txtLight;

    SharedPreferences.Editor editor, SetValues;
    SharedPreferences sharesPref, ValuesPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharesPref = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharesPref.edit();

        ValuesPrefs = getSharedPreferences(Values, Context.MODE_PRIVATE);
        SetValues = ValuesPrefs.edit();

        btnRED = (Button) findViewById(R.id.btnLEDRED);
        btnGreen = (Button) findViewById(R.id.btnLEDGreen);
        btnYellow = (Button) findViewById(R.id.btnLEDYellow);
        btnRef = (Button)findViewById(R.id.btnRefresh);

        btnRED.setOnClickListener(this);
        btnGreen.setOnClickListener(this);
        btnYellow.setOnClickListener(this);
        btnRef.setOnClickListener(this);

        txtIPAddress = (EditText) findViewById(R.id.txtIpAddress);
        txtPortNumber = (EditText) findViewById(R.id.txtPortNumber);

        txtIPAddress.setText(sharesPref.getString(IPAddress, ""));
        txtPortNumber.setText(sharesPref.getString(PortNumber, ""));

        SRED = (TextView) findViewById(R.id.SRED);
        SGreen = (TextView) findViewById(R.id.SGreen);
        SYellow = (TextView) findViewById(R.id.SYellow);

        txtTempC = (TextView)findViewById(R.id.txtTempC);
        txtHum = (TextView)findViewById(R.id.txtHumi);
        txtTempF = (TextView)findViewById(R.id.txtTempF);
        txtLight = (TextView)findViewById(R.id.txtLight);

        String IP = "", Port = "";

        Toast.makeText(this, "Hello !", Toast.LENGTH_SHORT).show();

        IP = txtIPAddress.getText().toString().trim();
        Port = txtPortNumber.getText().toString().trim();

        if (IP.length() > 0 && Port.length() > 0) {
            new HttpRequestAsyncTask(this, IP, Port, "GETALL").execute();
        }
/*
        if(txtLight.getText().equals(null) || txtLight.getText().equals("")){
            ValuesPrefs = getSharedPreferences(Values, MODE_PRIVATE);
            SGreen.setText(ValuesPrefs.getString("Green", ""));
            SRED.setText(ValuesPrefs.getString("Red", ""));
            SYellow.setText(ValuesPrefs.getString("Yellow", ""));

            txtTempC.setText(ValuesPrefs.getString("TempC", ""));
            txtTempF.setText(ValuesPrefs.getString("TempF", ""));
            txtHum.setText(ValuesPrefs.getString("Hum", ""));
            txtLight.setText(ValuesPrefs.getString("Light", ""));
        }
        */
    }

    @Override
    public void onClick(View v) {
        String PinNumber = "";

        String IP = txtIPAddress.getText().toString().trim();
        String Port = txtPortNumber.getText().toString().trim();

        editor.putString(IPAddress, IP);
        editor.putString(PortNumber, Port);
        editor.commit();

        if(v.getId() == btnRED.getId()){
            PinNumber = "3";
        }
        else if(v.getId() == btnYellow.getId()){
            PinNumber = "4";
        }
        else if(v.getId() == btnGreen.getId()){
            PinNumber = "5";
        }

        if(IP.length() > 0 && Port.length() > 0) {
            if (v.getId() != btnRef.getId())
                new HttpRequestAsyncTask(v.getContext(), PinNumber, IP, Port, "pin", (Button) v).execute();
            else
                new HttpRequestAsyncTask(this, IP, Port, "GETALL").execute();
        }
    }

    public String sendRequest(String PinNumber, String IPAddress, String PortNumber, String Param){
        String ServerResponse = "ERROR";

        try{
            HttpClient Client = new DefaultHttpClient();
            URI url;
            if(PinNumber != null)
                url = new URI("http://" + IPAddress + ":" + PortNumber+"/?" + Param + "=" + PinNumber);
            else
                url = new URI("http://" + IPAddress + ":" + PortNumber+"/?" + Param);

            HttpGet getRequest = new HttpGet();
            getRequest.setURI(url);

            HttpResponse response = Client.execute(getRequest);
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));

            ServerResponse = reader.readLine();
            content.close();
        } catch (URISyntaxException e) {
            ServerResponse = e.getMessage();
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            ServerResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            ServerResponse = e.getMessage();
            e.printStackTrace();
        }
        return  ServerResponse;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {

        String requestReply, IPAddress, PortNumber;
        Context context;
        AlertDialog alertDialog;
        String Param;
        String PinNumber;
        Button S;

        public HttpRequestAsyncTask(Context context ,String PinNumber, String IPAddress, String PortNumber, String Param, Button S){
            this.context = context;
            this.PinNumber = PinNumber;
            this.IPAddress = IPAddress;
            this.PortNumber = PortNumber;
            this.Param = Param;
            this.S = S;
            alertDialog = new AlertDialog.Builder(this.context).setTitle("Home Automation").setCancelable(true).create();
        }

        public HttpRequestAsyncTask(Context context, String IPAddress, String PortNumber, String Param){
            this.IPAddress = IPAddress;
            this.PortNumber = PortNumber;
            this.Param = Param;
            this.context = context;
            alertDialog = new AlertDialog.Builder(this.context).setTitle("Home Automation").setCancelable(true).create();
        }


        @Override
        protected Void doInBackground(Void... params) {
            ShowMessage("Data Sent, please wait !");

            if (Param.equals("pin"))
                requestReply = sendRequest(PinNumber, IPAddress, PortNumber, Param);
            else {
                requestReply = sendRequest(null, IPAddress, PortNumber, Param);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(requestReply.contains("-49")) {
                ShowMessage("Error Please try Again !");
            }
            else
            {
                if(requestReply.contains("ALL")){
                    String MSG = "";
                    requestReply = requestReply.replace("ALL", "");
                    String[] Replies = requestReply.split(new String("&"));

                    SGreen.setText((Replies[0].substring(Replies[0].indexOf('=')+1, Replies[0].indexOf('=')+2).equals("0")?"OFF":"ON"));
                    SYellow.setText((Replies[1].substring(Replies[1].indexOf('=')+1, Replies[1].indexOf('=')+2).equals("0")?"OFF":"ON"));
                    SRED.setText((Replies[2].substring(Replies[2].indexOf('=')+1, Replies[2].indexOf('=')+2).equals("0")?"OFF":"ON"));

                    txtTempC.setText("TempC = "+ Replies[3].substring(Replies[3].indexOf("=")+1));
                    txtTempF.setText("TempF = "+ Replies[4].substring(Replies[4].indexOf("=")+1));
                    txtHum.setText("Humidity = "+ Replies[5].substring(Replies[5].indexOf("=")+1));
                    txtLight.setText("Light = " +Replies[6].substring(Replies[6].indexOf("=")+1));

                    SetValues.putString("Green", SGreen.getText().toString());
                    SetValues.putString("Red", SRED.getText().toString());
                    SetValues.putString("Yellow", SYellow.getText().toString());

                    SetValues.putString("TempC", txtTempC.getText().toString());
                    SetValues.putString("TempF", txtTempF.getText().toString());
                    SetValues.putString("Hum", txtHum.getText().toString());
                    SetValues.putString("Light", txtLight.getText().toString());

                    SetValues.commit();

                    for(int i = 0 ; i < Replies.length ; i++){
                        if(Replies[i].contains("=")){
                            MSG+=Replies[i] + "\n";
                        }
                        requestReply = MSG;
                    }
                }
                else {
                    if (requestReply.contains("ON")) {
                        SetText("ON");
                    } else if(requestReply.contains("OFF")){
                        SetText("OFF");
                    }
                }
            }
            ShowMessage(requestReply);
            if(requestReply.contains("Error")){
                ValuesPrefs = getSharedPreferences(Values, MODE_PRIVATE);
                SGreen.setText(ValuesPrefs.getString("Green", ""));
                SRED.setText(ValuesPrefs.getString("Red", ""));
                SYellow.setText(ValuesPrefs.getString("Yellow", ""));

                txtTempC.setText(ValuesPrefs.getString("TempC", ""));
                txtTempF.setText(ValuesPrefs.getString("TempF", ""));
                txtHum.setText(ValuesPrefs.getString("Hum", ""));
                txtLight.setText(ValuesPrefs.getString("Light", ""));
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            btnYellow.setEnabled(true);
            btnGreen.setEnabled(true);
            btnRED.setEnabled(true);
            btnRef.setEnabled(true);
        }

        @Override
        protected void onPreExecute() {
            ShowMessage("Sending Data !");
            btnYellow.setEnabled(false);
            btnGreen.setEnabled(false);
            btnRED.setEnabled(false);
            btnRef.setEnabled(false);
        }


        private  void  SetText(String Value){
                switch (S.getId()) {
                    case R.id.btnLEDGreen:
                        SGreen.setText(Value);
                        break;
                    case R.id.btnLEDYellow:
                        SYellow.setText(Value);
                        break;
                    case R.id.btnLEDRED:
                        SRED.setText(Value);
                        break;
            }
        }

        private void ShowMessage(String Msg){
            alertDialog.setMessage(Msg);
            if(!alertDialog.isShowing()){
                alertDialog.show();
            }
        }

    }
}
