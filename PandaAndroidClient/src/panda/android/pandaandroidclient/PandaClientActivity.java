package panda.android.pandaandroidclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PandaClientActivity extends Activity {
	
	private EditText textField;
    private Button button;
    private TextView textView;
    private Socket client;
    private PrintWriter printwriter;
    private BufferedReader bufferedReader;
    
    public EditText IPEdit;
    public EditText PORTAEdit;

  //Following is the IP address of the chat server. You can change this IP address according to your configuration.
    // I have localhost IP address for Android emulator.
    private String CHAT_SERVER_IP = "172.16.100.91";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panda_client);
        textField = (EditText) findViewById(R.id.editText1);
        button = (Button) findViewById(R.id.button1);
        textView = (TextView) findViewById(R.id.textView1);
        IPEdit = (EditText) findViewById(R.id.IP);
        PORTAEdit = (EditText) findViewById(R.id.PORTA);

    }
    
    public void Connect(View w){
    	ChatOperator chatOperator = new ChatOperator();
        chatOperator.execute();
    }

    /**
     * This AsyncTask create the connection with the server and initialize the
     * chat senders and receivers.
     */
    private class ChatOperator extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                client = new Socket(IPEdit.getText().toString(), Integer.parseInt(PORTAEdit.getText().toString())); // Creating the server socket.

                if (client != null) {
                    printwriter = new PrintWriter(client.getOutputStream(), true);
                    InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);
                } else {
                    Log.d("Server has not bean started on port.", "Server has not bean started on port.");
                }
            } catch (UnknownHostException e) {
            	Log.e("Failed to connect server " + CHAT_SERVER_IP,"Failed to connect server " + CHAT_SERVER_IP + ": " + e.toString());
                e.printStackTrace();
            } catch (IOException e) {
            	Log.e("Faild to connect server " + CHAT_SERVER_IP,"Faild to connect server " + CHAT_SERVER_IP + ": " + e.toString());
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Following method is executed at the end of doInBackground method.
         */
        @Override
        protected void onPostExecute(Void result) {
            button.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
				public void onClick(View v) {
                    final Sender messageSender = new Sender(); // Initialize chat sender AsyncTask.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        messageSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        messageSender.execute();
                    }
                }
            });

            Receiver receiver = new Receiver(); // Initialize chat receiver AsyncTask.
            receiver.execute();

        }

    }

    /**
     * This AsyncTask continuously reads the input buffer and show the chat
     * message if a message is availble.
     */
    private class Receiver extends AsyncTask<Void, Void, Void> {

        private String message;

        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                try {

                    if (bufferedReader.ready()) {
                        message = bufferedReader.readLine();
                        publishProgress(null);
                    }
                } catch (UnknownHostException e) {
                	Log.e("ERRORE", "ERRORE in BackGround: " + e.toString());
                    e.printStackTrace();
                } catch (IOException e) {
                	Log.e("ERRORE", "ERRORE in BackGround: " + e.toString());
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            textView.append("Server: " + message + "\n");
        }

    }

    /**
     * This AsyncTask sends the chat message through the output stream.
     */
    private class Sender extends AsyncTask<Void, Void, Void> {

        private String message;

        @Override
        protected Void doInBackground(Void... params) {
            message = textField.getText().toString();
            printwriter.write(message + "\n");
            printwriter.flush();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textField.setText(""); // Clear the chat box
            textView.append("Client: " + message + "\n");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.panda_client, menu);
        return true;
    }

}
