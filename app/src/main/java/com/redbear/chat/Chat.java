package com.redbear.chat;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Chat extends Activity {
	private final static String TAG = Chat.class.getSimpleName();
	public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
	private TextView tv = null;
	private EditText et = null;
	private Button btn = null;
	private String mDeviceName;
	private String mDeviceAddress;
	private RBLService mBluetoothLeService;
    private static String completeSensorCode=null;
    private static int extendedCode=0;
	private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((RBLService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
            if(mDeviceAddress.equalsIgnoreCase("Neil Nodes")) {
                mBluetoothLeService.connect(mDeviceAddress);
            }
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
			} else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				getGattService(mBluetoothLeService.getSupportedGattService());
			} else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {

                MyDBHandler db = new MyDBHandler(getApplicationContext());
                try {
                    String sensorCode =new String(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
                   // String encryptedString = encryptString("AES/ECB/PKCS5padding", "123", "987654321", "567");
                    Log.e(TAG, "sensorCode" + sensorCode +" length:"+sensorCode.length());
                    if(sensorCode!=null&&(sensorCode.length()==20||sensorCode.length()==4)) {
                        if(sensorCode.length()==20) {
                            completeSensorCode = sensorCode;
                            extendedCode=1;
                        }
                        else if (sensorCode.length()==4 && extendedCode==1 && sensorCode.charAt(3)=='='){
                            completeSensorCode=completeSensorCode+sensorCode;
                            String decryptedString = decryptString("AES/ECB/PKCS5padding", "123", completeSensorCode, "567");
                            if(!db.isExists("neil")) {
                                db.addBLESensorValue(new BLESensorValues("neil", new String(decryptedString)));
                                displayData(decryptedString.getBytes());
                                Log.e(TAG, "CompleteSensorCode" + completeSensorCode+"actual decrypted code"+decryptedString);
                            }
                            completeSensorCode=null;
                        }
                        else if(extendedCode==1){
                            extendedCode=0;
                        }
                        //if(!db.isExists("neil")) {
                        //}
                            List<BLESensorValues> list = db.getAllSensorValues("neil");

                        for (int i = 0; i < list.size(); i++) {
                            Log.e(TAG, "id:" + i + "list value: " + list.toString());
                        }
                    }

                }
                catch(Exception e){
                    Log.e(TAG, "Inside main exception"+e.getMessage());
                }

            }
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second);

		tv = (TextView) findViewById(R.id.textView);
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
		et = (EditText) findViewById(R.id.editText);
		btn = (Button) findViewById(R.id.send);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BluetoothGattCharacteristic characteristic = map
						.get(RBLService.UUID_BLE_SHIELD_TX);

				String str = et.getText().toString();
				byte b = 0x00;
				byte[] tmp = str.getBytes();
				byte[] tx = new byte[tmp.length + 1];
				tx[0] = b;
				for (int i = 1; i < tmp.length + 1; i++) {
					tx[i] = tmp[i - 1];
				}

				characteristic.setValue(tx);
				mBluetoothLeService.writeCharacteristic(characteristic);

				et.setText("");
			}
		});

		Intent intent = getIntent();

		mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
		mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

		getActionBar().setTitle(mDeviceName);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent gattServiceIntent = new Intent(this, RBLService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();

			System.exit(0);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();

		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mBluetoothLeService.disconnect();
		mBluetoothLeService.close();

		System.exit(0);
	}


    // decryption method
    public static String decryptString(String cypher, String key, String textToDecrypt, String salt) {
        byte[] rawKey = new byte[32];
        java.util.Arrays.fill(rawKey, (byte) 0);
        byte[] keyOk = hmacSha1(salt, key);
        for (int i = 0; i < keyOk.length; i++) {
            rawKey[i] = keyOk[i];
        }
        SecretKeySpec skeySpec = new SecretKeySpec(hmacSha1(salt, key), "AES");
        try {
            Cipher cipher = Cipher.getInstance(cypher);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encryptedData = cipher.doFinal(Base64.decode(textToDecrypt, Base64.NO_CLOSE));
            if (encryptedData == null) return null;
            return new String(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // encryption method
    public static String encryptString(String cypher, String key, String clearText, String salt) {
        byte[] rawKey = new byte[32];
        java.util.Arrays.fill(rawKey, (byte) 0);
        byte[] keyOk = hmacSha1(salt, key);
        for (int i = 0; i < keyOk.length; i++) {
            rawKey[i] = keyOk[i];
        }
        SecretKeySpec skeySpec = new SecretKeySpec(hmacSha1(salt, key), "AES");
        try {
            Cipher cipher = Cipher.getInstance(cypher);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encryptedData = cipher.doFinal(clearText.getBytes());
            if (encryptedData == null) return null;
            return Base64.encodeToString(encryptedData, Base64.NO_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // key generator method
    public static byte[] hmacSha1(String salt, String key) {
        SecretKeyFactory factory = null;
        SecretKey keyByte = null;
        try {
            //PBKDF2WithHmacSHA1
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keyspec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 512, 256);
            keyByte = factory.generateSecret(keyspec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return keyByte.getEncoded();
    }
    public static void main(String args[]){

    }
	private void displayData(byte[] byteArray) {
		if (byteArray != null) {
			String data = new String(byteArray);
            Log.e(TAG,data);
			tv.append(data);
			// find the amount we need to scroll. This works by
			// asking the TextView's internal layout for the position
			// of the final line and then subtracting the TextView's height
			final int scrollAmount = tv.getLayout().getLineTop(
					tv.getLineCount())
					- tv.getHeight();
			// if there is no need to scroll, scrollAmount will be <=0
			if (scrollAmount > 0)
				tv.scrollTo(0, scrollAmount);
			else
				tv.scrollTo(0, 0);
		}
	}

	private void getGattService(BluetoothGattService gattService) {
		if (gattService == null)
			return;

		BluetoothGattCharacteristic characteristic = gattService
				.getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
		map.put(characteristic.getUuid(), characteristic);

		BluetoothGattCharacteristic characteristicRx = gattService
				.getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
		mBluetoothLeService.setCharacteristicNotification(characteristicRx,
				true);
		mBluetoothLeService.readCharacteristic(characteristicRx);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);

		return intentFilter;
	}
}
