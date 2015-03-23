package com.scanner.qb0x;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.scanner.qb0x.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
    private NetworkTask networktask;
    private TextView tv; 
    private String hostname;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv = new TextView(this);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user selects the Send button */
    public void sendMessage(View view) {
 
    	EditText editText = (EditText) findViewById(R.id.edit_message);
        hostname = editText.getText().toString(); 
        networktask = new NetworkTask(); //New instance of NetworkTask
        networktask.execute();
    }
    
    
    public class NetworkTask extends AsyncTask<Void, byte[], Boolean> {
    	  	
        Socket nsocket; //Network Socket
        InputStream nis; //Network Input Stream
        OutputStream nos; //Network Output Stream
        String portList = "";
        
    	private String[][] portMap = {
    			{"21", ""},
                {"22", ""},
                {"23", ""},
                {"25", ""},
                {"110", ""},
                {"80", "HEAD / HTTP/1.1\r\n\r\n"},
                {"443", "HEAD / HTTP/1.1\r\n\r\n"},
                {"8080", "HEAD / HTTP/1.1\r\n\r\n"}
            };

        @Override
        protected void onPreExecute() {
            Log.i("AsyncTask", "onPreExecute");
        }

        @Override
        protected Boolean doInBackground(Void... params) { //This runs on a different thread
       
        	boolean result = false;
			for (int i = 0; i < portMap.length; i++) {
				int portNumber = Integer.parseInt(portMap[i][0]);
				String fingerprint = portMap[i][1];

				try {

					Log.i("AsyncTask", "doInBackground: Creating socket");
					SocketAddress sockaddr = new InetSocketAddress(hostname,
							portNumber);
					nsocket = new Socket();
					nsocket.connect(sockaddr, 1000);

					if (nsocket.isConnected()) {
						nos = nsocket.getOutputStream();

						nos.write(fingerprint.getBytes());
						nis = nsocket.getInputStream();
						Log.i("AsyncTask",
								"doInBackground: Socket created, streams assigned");
						Log.i("AsyncTask",
								"doInBackground: Waiting for inital data...");
						byte[] buffer = new byte[4096];
						int read = nis.read(buffer, 0, 4096);
						portList += portMap[i][0] + " open " + new String(buffer)
								+ "\r\n";
					}

				} catch (IOException e) {
					e.printStackTrace();
					Log.i("AsyncTask", "doInBackground: IOException");
					result = true;
				} catch (Exception e) {
					e.printStackTrace();
					Log.i("AsyncTask", "doInBackground: Exception");
					result = true;
				} finally {
					try {
						nis.close();
						nsocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Log.i("AsyncTask", "doInBackground: Finished");
				}

			}
            return result;
        }
        
        
        

        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {
                Log.i("AsyncTask", "onProgressUpdate: " + values[0].length + " bytes received.");     
            }
        }
        @Override
        protected void onCancelled() {
            Log.i("AsyncTask", "Cancelled.");
        }
        @Override
        protected void onPostExecute(Boolean result) {
        	tv.setText(portList);
            setContentView(tv);
            if (result) {
                Log.i("AsyncTask", "onPostExecute: Completed with an Error.");
            } else {
                Log.i("AsyncTask", "onPostExecute: Completed.");
            }
        }
    }
    
}
