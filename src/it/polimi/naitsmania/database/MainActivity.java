package it.polimi.naitsmania.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// List of all places
	private ArrayList<String> allPlaces = new ArrayList<String>();
	
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	private Button blueButton;
	private Button redButton;
	private Button greenButton;
	private Button yellowButton;
	private int numb = 0;

	// Buttons
	private String readMessage;
	private ArrayList<String> readMessageArray = new ArrayList<String>(
			Arrays.asList("Blue", "Red", "Green", "Yellow"));

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	// Database
	private Uri placeUri;

	// structure
	public enum gender {
		M, F
	};

	private int[] minAge = { 13, 18, 25, 35, 50, 65 };
	private int[] maxAge = { 17, 24, 34, 49, 64, 100 };
	private int[] vote = { 1, 2, 3, 4, 5 };
	// data chosen
	private gender gender;
	private int min;
	private int max;
	private int num;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);    
		
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		//setContentView(R.layout.activity_main);

		SQLiteDatabaseHelper db = new SQLiteDatabaseHelper(getBaseContext(),
				"studenti", null, 1);

		for (int i = 1; i <= 10; i++) {
			String place = "Duomo";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		for (int i = 11; i <= 20; i++) {
			String place = "Parco Sempione";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		for (int i = 21; i <= 30; i++) {
			String place = "Navigli";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		for (int i = 31; i <= 40; i++) {
			String place = "Museo Scienza e della Tecnica";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		for (int i = 41; i <= 50; i++) {
			String place = "Cenacolo di Leonardo";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}

		// compute the query for finding the places
		db.showResults();
		// allPlaces contais all the places
		allPlaces = db.getAllPlacesList();
		for (int i = 0; i < allPlaces.size(); i++) {
			System.out.println(allPlaces.get(i));
		}
		
		for(int i=0;i<4;i++) {
			readMessageArray.add(allPlaces.get(i));
		}
		

		// BLUETOOTH ONCREATE
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");


		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	private void generateRandomValues() {
		int pick = new Random().nextInt(gender.values().length);
		gender = gender.values()[pick];
		pick = new Random().nextInt(minAge.length);
		min = minAge[pick];
		max = maxAge[pick];
		pick = new Random().nextInt(vote.length);
		num = vote[pick];
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// BLUETOOTH AFTER THIS POINT:

	@Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);
			}
		});

		// Initialize the buttons
		blueButton = (Button) findViewById(R.id.bluebutton);
		blueButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Send button item to the mobilephones
				sendMessage(blueButton.getText().toString());
			}
		});

		redButton = (Button) findViewById(R.id.redbutton);
		redButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Send button item to myplaces.
				sendMessage(redButton.getText().toString());

			}
		});

		greenButton = (Button) findViewById(R.id.greenbutton);
		greenButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage(greenButton.getText().toString());
			}
		});

		yellowButton = (Button) findViewById(R.id.yellowbutton);
		yellowButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage(yellowButton.getText().toString());
			}
		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivity(discoverableIntent);
		}
	}

/**
 * Sends a message.
 * @param message  A string of text to send.
 */
	/**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

 // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

// The Handler that gets information back from the BluetoothChatService
private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_STATE_CHANGE:
            if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
            switch (msg.arg1) {
            case BluetoothChatService.STATE_CONNECTED:
                mTitle.setText(R.string.title_connected_to);
                mTitle.append(mConnectedDeviceName);
                mConversationArrayAdapter.clear();
                break;
            case BluetoothChatService.STATE_CONNECTING:
                mTitle.setText(R.string.title_connecting);
                break;
            case BluetoothChatService.STATE_LISTEN:
            case BluetoothChatService.STATE_NONE:
                mTitle.setText(R.string.title_not_connected);
                break;
            }
            break;
        case MESSAGE_WRITE:
            byte[] writeBuf = (byte[]) msg.obj;
            // construct a string from the buffer
            String writeMessage = new String(writeBuf);
            mConversationArrayAdapter.add("Me:  " + writeMessage);
            break;
        case MESSAGE_READ:
        	if (numb>=4) {
        		Log.e(TAG, "numb is exceeds its limit. numb: " + numb);
        		return;
        	}
        	if (D) Log.e(TAG, "numb is: " + numb);
            byte[] readBuf = (byte[]) msg.obj;
            // construct a string from the valid bytes in the buffer
            readMessage = new String(readBuf, 0, msg.arg1);
            mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
            
            if (D) Log.e(TAG, "Is about to add element to array.");
            //Add to readMessageArray
            Log.e(TAG, "numb: " + numb + " msg " + readMessage);
            readMessageArray.add(numb, readMessage.toString());
            setButtonText(numb, readMessage.toString());
            if (D) Log.e(TAG, "Added element to array.");
            if (numb<=3) {
            	numb ++;
            }
            
            break;
        case MESSAGE_DEVICE_NAME:
            // save the connected device's name
            mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
            Toast.makeText(getApplicationContext(), "Connected to "
                           + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            break;
        case MESSAGE_TOAST:
            Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                           Toast.LENGTH_SHORT).show();
            break;
        }
    }
};

public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(D) Log.d(TAG, "onActivityResult " + resultCode);
    switch (requestCode) {
    case REQUEST_CONNECT_DEVICE:
        // When DeviceListActivity returns with a device to connect
        if (resultCode == Activity.RESULT_OK) {
            // Get the device MAC address
            String address = data.getExtras()
                                 .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            // Get the BLuetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            // Attempt to connect to the device
            mChatService.connect(device);
        }
        break;
    case REQUEST_ENABLE_BT:
        // When the request to enable Bluetooth returns
        if (resultCode == Activity.RESULT_OK) {
            // Bluetooth is now enabled, so set up a chat session
            setupChat();
        } else {
            // User did not enable Bluetooth or an error occured
            Log.d(TAG, "BT not enabled");
            Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.scan:
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        return true;
    case R.id.discoverable:
        // Ensure this device is discoverable by others
        ensureDiscoverable();
        return true;
    }
    return false;
}


private void setButtonText(int number, String theMessage) {
    if (number == 0) {
		//Input to buttons
        if(D) Log.e(TAG, theMessage);
        blueButton = (Button) findViewById(R.id.bluebutton);
        blueButton.setText(theMessage);
    } else if (number == 1) {
        if(D) Log.e(TAG, theMessage);
        redButton = (Button) findViewById(R.id.redbutton);
        redButton.setText(theMessage);
    	
    } else if (number == 2) {
        if(D) Log.e(TAG, theMessage);
        greenButton = (Button) findViewById(R.id.greenbutton);
        greenButton.setText(theMessage);
    } else if (number == 3) {
        if(D) Log.e(TAG, theMessage);
        yellowButton = (Button) findViewById(R.id.yellowbutton);
        yellowButton.setText(theMessage);
    }
}

}
