package com.example.serverb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 1;
	//private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	private static final UUID MY_UUID= UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	String NAME="bluetoothAudio";
	//private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	public BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
			Context context = getApplicationContext();
			CharSequence text = "Device does not support bluetooth";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		else
		{
			Context context = getApplicationContext();
			CharSequence text = "Device supports bluetooth";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			if (!mBluetoothAdapter.isEnabled()) 
			{
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			else
			{
				text = "bluetooth is already enabled";
				//int duration = Toast.LENGTH_LONG;
				Toast toast1 = Toast.makeText(context, text, duration);
				toast1.show();
				start();
			}
			
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == REQUEST_ENABLE_BT) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	            // The user picked a contact.
	            // The Intent's data Uri identifies which contact was selected.
	        	Context context = getApplicationContext();
				CharSequence text = "Bluetooth is enabled";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
	            // Do something with the contact here (bigger example below)
				//searchForDevices();
				start();
				
	        }
	    }
	}

	public synchronized void start()
	{
		AcceptThread acThread=new AcceptThread();
		acThread.start();
		try {
			acThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("BluetoothServer","Thread join exception");
			e.printStackTrace();
			
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private class AcceptThread extends Thread {
	    private final BluetoothServerSocket mmServerSocket;
	   // InputStream mmInstream = null;
     

	   
	    public AcceptThread() {
	    	 Log.d("BluetoothServer","Inside accept thread constructor");
				
				
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
	            
	        } catch (IOException e) {
	        	Log.e("BluetoothServer","inside catch block AcceptThread constructor");
				
	        }
	        mmServerSocket = tmp;
	        
	    }
	 
	    public void run() {
	    	
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        Log.d("BluetoothServer","Inside run");
	        while (true) {
	            try {
	                socket = mmServerSocket.accept();
	            } catch (IOException e) {
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
	               // manageConnectedSocket(socket);
	            	
	    			 Log.d("BluetoothServer","Socket created");
	    			 try 
	    			 {
	    				  
	    				manageConnectedThread(socket);
	    		     }
	    			 catch (Exception e) 
	    		        {
	    				 	e.printStackTrace();
	    		        	Log.e("BluetoothServer", "Error in calling the manageConnectedThread");
	    		        }

	                try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                break;
	            }
	        }
	    }
	 
	   

		/** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	            mmServerSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	 class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private  InputStream mmInStream;
	   int totalbytes;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        totalbytes=0;
	       
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	        Log.d("BluetoothServer", "beforesdcard root");
	         File SDCardRoot = Environment.getExternalStorageDirectory();
	         //create a new file, to save the downloaded file
	         Log.d("BluetoothServer", "before file creation");
	         File file = new File(SDCardRoot,"downloaded_file.mp3");
	         Log.d("BluetoothServer", "after file creation");
			 FileOutputStream fos;
			 BufferedOutputStream bos=null;
			try {
				fos = new FileOutputStream(file);
				 bos = new BufferedOutputStream(fos);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			    Log.d("BluetoothServer", "after file oututstream");
			
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	        	
	            try {
	            	
	            	
    				 Log.d("BluetoothServer", "Inside try block of Reading audio file");


    				// Log.e("BServer", "connected ->" + mmSocket.isConnected());
    				 //bytes = mmInStream.read(buffer);
			            while ((bytes = mmInStream.read(buffer))>0){
			            	
			            	Log.d("BluetoothServer", "bytes read :"+bytes);
			            	Log.d("BluetoothServer","Total bytes read :"+totalbytes);
			            	//bytes is a integer and 'in' is InputStream
				            bos.write(buffer, 0, bytes); 
				            totalbytes+=bytes;
				            bos.flush(); 
			            }
			            Log.d("BluetoothServer","Total bytes read :"+totalbytes);
			            
			            bos.close();
	            } catch (IOException e) {
	            	e.printStackTrace();
	            	//Log.e("BluetoothServer","IOException in BluetoothServer");
	            	//Log.e("BluetoothServer","Total bytes read :"+totalbytes);
	                break;
	            }
	        }
	    }
	 
	   
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	 
	 private void manageConnectedThread(BluetoothSocket socket) {
			// TODO Auto-generated method stub
			ConnectedThread connectedThread=new ConnectedThread(socket);
			connectedThread.start();
		}
	 
}
	

