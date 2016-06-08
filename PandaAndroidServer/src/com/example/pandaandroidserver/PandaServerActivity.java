package com.example.pandaandroidserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PandaServerActivity extends Activity {
	
	public Button AvviaServerButton;
	public Button InviaButton;
	public EditText PortaEdit;
	public EditText MsgEdit;
	public TextView IpView;
	public TextView ChatView;
	
	ServerSocket serverSocket = null;
    Socket clientSocket = null;
    
    Receiver receiver;
    Sender sender;
    
    public int PORTA;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panda_server);
		AvviaServerButton = (Button) findViewById(R.id.AvvioServer);
		InviaButton = (Button) findViewById(R.id.Invia);
		PortaEdit = (EditText) findViewById(R.id.Porta);
		MsgEdit = (EditText) findViewById(R.id.ToBeSent);
		IpView =  (TextView) findViewById(R.id.IPAddress);
		ChatView = (TextView) findViewById(R.id.chatmsg);
		
		IpView.setText(getIpAddress());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.panda_server, menu);
		return true;
	}
	
	//BOTTONE DI START THREAD DEL SERVER
	public void AvvioServer(View v){
		try{
			PORTA = Integer.parseInt(PortaEdit.getText().toString());
			serverSocket = new ServerSocket(PORTA);
            Log.d("Server started. Listening to the port. Waiting for the client.", "Server started. Listening to the port. Waiting for the client.");
            ApriSocket as = new ApriSocket();
            as.start();
            
            //===============================================================
		}catch(Exception e) {Log.e("ERRORE AVVIO SERVER", "ERRORE: " + e.toString());}
		
	}
	
	//BOTTONE DI INVIO MESSAGGIO DEL SERVER
	public void Invia(View v){
		sender.sendMessage(MsgEdit.getText().toString());				 // |
		MsgEdit.setText(""); //Clear the chat box	
	}
	
	
	//======================================================================================
	public class Sender {

	    private PrintWriter out;
	    
	    /**
	     * Public constructor.
	     * 
	     * @param clientSocket
	     *            Socket which has been opened for chat
	     * @param chatViewParam
	     *            Chat history text area of chat window
	     */
	    public Sender(Socket clientSocket) {
	        
	        try {
	            out = new PrintWriter(clientSocket.getOutputStream(), true);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    public void sendMessage(String message) {
	        out.println(message); // Print the message on output stream.
	        ChatView.append("Server: " + message + "\n"); // Print the message on chat window.
	    }

	}

	//======================================================================================
	
	public class Receiver implements Runnable {

	    private BufferedReader bufferedReader;
	    
	    /**
	     * Public constructor.
	     * 
	     * @param clientSocket
	     *            Socket which has been opened for chat
	     * @param chatViewParam
	     *            Chat history text area of chat window
	     */
	    public Receiver(Socket clientSocket) {
	        
	        try {
	            InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
	            bufferedReader = new BufferedReader(inputStreamReader);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	    }

	    @Override
	    public void run() {

	        String message;

	        while (true) {
	            try {
	                if (bufferedReader.ready()) {
	                    message = bufferedReader.readLine(); // Read the chat message.
	                    final String msg = message;
	                    PandaServerActivity.this.runOnUiThread(new Runnable() {
	                    	@Override
	                        public void run() {
	                    		ChatView.append("Client: " + msg + "\n"); // Print the chat message on chat window.
	    	                    
	                        }
	                    });
	                    Log.e("MESSAGGIO CORRETTAMENTE RICEVUTO", "MESSAGGIO: " + message);
	                }
	            } catch (IOException ex) {
	                Log.e("Problem in message reading","ERRORE: " + ex.toString());
	                ex.printStackTrace();
	            }

	            try {
	                Thread.sleep(500);
	            } catch (InterruptedException ie) {
	            }
	        }

	    }

	}
	
	//======================================================================================
	
	 private String getIpAddress() {
		  String ip = "";
		  try {
		   Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
		     .getNetworkInterfaces();
		   while (enumNetworkInterfaces.hasMoreElements()) {
		    NetworkInterface networkInterface = enumNetworkInterfaces
		      .nextElement();
		    Enumeration<InetAddress> enumInetAddress = networkInterface
		      .getInetAddresses();
		    while (enumInetAddress.hasMoreElements()) {
		     InetAddress inetAddress = enumInetAddress.nextElement();

		     if (inetAddress.isSiteLocalAddress()) {
		      ip += "SiteLocalAddress: "
		        + inetAddress.getHostAddress() + "\n";
		     }

		    }

		   }

		  } catch (SocketException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
		   ip += "Something Wrong! " + e.toString() + "\n";
		  }

		  return ip;
		 }
	
	
	 
	 public class ApriSocket extends Thread
	 {
		 
		 public void run(){
			 try{
			    clientSocket = serverSocket.accept();
	            Log.d("Client connected on port.", "Client connected on port");
	            
	            receiver = new Receiver(clientSocket);    
	            sender = new Sender(clientSocket);    
	            															
															 
	            															 
	            Thread receiverThread = new Thread(receiver);                
	            receiverThread.run();  
			 }catch(Exception e){Log.e("ERRORE: nel run di AvviaSocket", "ERRORE: " + e.toString());}
		 }
	 
	 }
	

}
