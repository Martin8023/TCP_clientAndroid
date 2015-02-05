package com.fedex.testapp;

import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutionException;


public class Home extends ActionBarActivity {
    private Connessione connessione;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connetti(View v) { //Funzione richiamata dal pulsante "Connetti"
        EditText iptxt = (EditText) findViewById(R.id.txtIp);
        EditText portatxt = (EditText) findViewById(R.id.txtPorta);
        TextView output = (TextView) findViewById(R.id.txtConnessione);
        Button disconnetti = (Button) findViewById(R.id.disconnettiBtn);
        Button invio = (Button) findViewById(R.id.inviaBtn); //Inizializzazione da interfaccia

        try {
            output.setText("Connessione..."); //Segnalo il tentativo in corso
            connessione = new Connessione(iptxt.getText().toString(), Integer.parseInt(portatxt.getText().toString())); //Creo la connessione
            String cod = connessione.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "connetti").get(); //Avvio la connessione
            //Perché su classe diversa: necessario thread a parte per operazioni di rete (perché Android vuole così, specifica di sicurezza)

            switch (Integer.parseInt(cod)) { //A seconda del codice ritornato, stampo il risultato della connessione
                case -1:
                    output.setText("ERRORE [UnknownHost]");
                    break;

                case -2:
                    output.setText("ERRORE [IO]");
                    break;

                case -3:
                    output.setText("ERRORE [NumberFormat]");
                    break;

                case 0:
                    output.setText("Connesso");
                    output.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    invio.setEnabled(true);
                    disconnetti.setEnabled(true);
                    v.setEnabled(false); //Se connesso, segnalo successo, attivo pulsanti e barra per invio e disconnessione
                    break;
            }
        } catch (Exception ex) {} //TODO correggere
    }

    public void invia(View v) { //Funzione richiamata dal pulsante "Invia"
        EditText outstring = (EditText) findViewById(R.id.txtStringa);
        TextView outputview = (TextView) findViewById(R.id.txtOutput); //Inizializzazione da interfaccia
        try {
            DataOutputStream outstream = new DataOutputStream(connessione.socket.getOutputStream());
            outstream.writeBytes(outstring.getText().toString() + "\n");
            Invio invio = new Invio(connessione.socket); //Inizializzo connessione, stesso discorso di prima
            String output = invio.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            outputview.setText(output);
        } catch (IOException e) {
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }

    public void disconnetti(View v) {
        TextView output = (TextView) findViewById(R.id.txtConnessione);
        Button disconnetti = (Button) findViewById(R.id.disconnettiBtn);
        Button connetti = (Button) findViewById(R.id.connettiBtn);
        Button invio = (Button) findViewById(R.id.inviaBtn);
        TextView outputview = (TextView) findViewById(R.id.txtOutput);
        EditText outstring = (EditText) findViewById(R.id.txtStringa);

        try {
            connessione.socket.close();
            connessione = null;
            invio.setEnabled(false);
            disconnetti.setEnabled(false);
            connetti.setEnabled(true);
            outputview.setText("");
            outstring.setText("");
            output.setText("Disconnesso");
            output.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } catch (IOException e) {
            output.setText("ERRORE [IO]");
        }
    }
}