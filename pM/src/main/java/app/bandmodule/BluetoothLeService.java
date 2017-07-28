/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.bandmodule;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.pm.R;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.BandInfo.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.BandInfo.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.BandInfo.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.BandInfo.ACTION_DATA_AVAILABLE";
    public final static String GPS_DATA =
            "com.BandInfo.GPS_DATA";
    public final static String HEART_RATE_DATA =
            "com.BandInfo.HEART_RATE_DATA";
    public final static String STORE_COUNT = "" +
            "com.BandInfo.STORE_COUNT";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server, start discovery service.");
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	Log.d(TAG, "Service discovered");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
        	Log.d(TAG, "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        	Log.d(TAG, "onCharacteristicChanged");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    
    private NtfContext mNtfContext;
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        
        Log.d(TAG, "Characteristic changed(UUID = " + characteristic.getUuid() + ")"); 
        if(UUID.fromString(SampleGattAttributes.TRUSTED_DATA_NOTIFY).equals(characteristic.getUuid())){
            // For all other profiles, writes the data formatted in HEX.
            byte[] data = characteristic.getValue();
            
            Log.e("BandInfo", "Raw data is:" + Arrays.toString(data));
            if(data != null) {
//            	if(mNtfContext == null || mNtfContext.isComplete())
//            	{
//            		mNtfContext = new NtfContext(data);
//            		if(mNtfContext.getPkt() == null)
//            		{
//            			mNtfContext = null;
//            			return;
//            		}
//            	}
//            	else
//            	{
//            		mNtfContext.addPktSeg(data);
//            	}

//            	if(mNtfContext.isComplete())
                {
//            		byte[] msg = mNtfContext.getPkt();
//
//            		/*Crc verification*/
//            		if(mNtfContext.checkCrc() == false){
//            			Toast.makeText(this, "Crc check failed", Toast.LENGTH_SHORT).show();
//            		}


//            		if(msg != null)
                    {
                        int accum = 0;
                        int store_count = 1;
                        if((data[4] == (byte)0xA5) && (data[5] == (byte)0xA5))store_count = 0;
                        else if(data[0] == (byte) 0xAB) store_count = 2;
//            			  accum = accum|(msg[0] & 0xff) << 0;
//            			  accum = accum|(msg[1] & 0xff) << 8;
//            			  accum = accum|(msg[2] & 0xff) << 16;
//            			  accum = accum|(msg[3] & 0xff) << 24;///
                        accum = accum | (data[4 + 0] & 0xff) << 0;
                        accum = accum | (data[4 + 1] & 0xff) << 8;
                        accum = accum | (data[4 + 2] & 0xff) << 16;
                        accum = accum | (data[4 + 3] & 0xff) << 24;
                        float latitude = Float.intBitsToFloat(accum);

                        accum = 0;
                        accum = accum | (data[4 + 4] & 0xff) << 0;
                        accum = accum | (data[4 + 5] & 0xff) << 8;
                        accum = accum | (data[4 + 6] & 0xff) << 16;
                        accum = accum | (data[4 + 7] & 0xff) << 24;
                        float longitude = Float.intBitsToFloat(accum);

//                          int latitude_H = ((data[4+1] & 0xff) << 8) | (data[4+0] & 0xff);
//                          int latitude_L = ((data[4+3] & 0xff) << 8) | (data[4+2] & 0xff);
//                          int longitude_H = ((data[4+5] & 0xff) << 8) | (data[4+4] & 0xff);
//                          int longitude_L = ((data[4+7] & 0xff) << 8) | (data[4+6] & 0xff);

                        int direction = data[4 + 8];

                        String locationStr = null;
                        Log.e("BandInfo", "longitude = " + longitude + "    latitude = " + latitude);
//                          Log.e("BandInfo", "longitude = " + longitude_H + "." +  longitude_L  + "    latitude = " + latitude_H + "." + latitude_L);
                        if ((latitude > -0.000001 && latitude < 0.000001) || (longitude > -0.000001 && longitude < 0.000001))
                        //if((longitude_H == 0) || (latitude_H == 0))
                        {
                            Log.e("BandInfo", "no data!!!");
                            locationStr = getResources().getString(R.string.no_loaction_info);
                        } else {
                            if ((direction & 0x2) == 0x2) {
                                locationStr = getResources().getString(R.string.east_longitude);
                            } else {
                                locationStr = getResources().getString(R.string.east_longitude);
                            }

                            locationStr += " " + longitude + "        ";
                            //locationStr += " " + longitude_H + "."+longitude_L+"        ";
                            if ((direction & 0x1) == 0x1) {
                                locationStr += getResources().getString(R.string.north_latitude);
                            } else {
                                locationStr += getResources().getString(R.string.south_latitude);
                            }

                            locationStr += " " + latitude;
                            //locationStr += " "+latitude_H + "." + latitude_L;
                        }


                        int heart_rate = 0x00FF & data[4 + 9];
                        Log.e("BandInfo", locationStr + "  " + heart_rate);

                        intent.putExtra(STORE_COUNT, store_count);
                        intent.putExtra(GPS_DATA, locationStr);
                        intent.putExtra(HEART_RATE_DATA, heart_rate);
                    }
                }
            }
        }
        sendBroadcast(intent);
    }
    
    

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(final BluetoothGattCharacteristic characteristic){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }


    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        
        //boolean isEnableNotification = 
        Log.d(TAG, "setCharacteristicNotification " + enabled);
        boolean isEnableNotification = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if(isEnableNotification){
        	Log.d(TAG, "isEnableNotification is true");
        	 BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
             descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
             mBluetoothGatt.writeDescriptor(descriptor);
        }

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public void setDataAccess(boolean enabled)
    {
    	if(mBluetoothGatt == null){
    		Log.e(TAG, "mBluetoothGatt is abnormal");
    		return;
    	}
    	
    	BluetoothGattService service = mBluetoothGatt.getService(
    			UUID.fromString(SampleGattAttributes.TRUSTED_DATA_SERVICE));
    	BluetoothGattCharacteristic trustDataCharacteristic = service.getCharacteristic(
    			UUID.fromString(SampleGattAttributes.TRUSTED_DATA_NOTIFY));
    	
    	mBluetoothGatt.setCharacteristicNotification(trustDataCharacteristic, enabled);
    	
    	try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	BluetoothGattDescriptor descriptor = trustDataCharacteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    
    
    
    private class NtfContext
    {
    	private byte[] mPkt;
    	private int mLen;
    	private int mOffset;
    	private int mCrc;
    	private static final int HEADER_LEN = 4;
    	private static final int MAGIC = 0xabba;
    	
    	
    	
    	public NtfContext(byte[] pkt)
    	{
    		if(pkt.length < HEADER_LEN){
    			Log.e("BandInfo", "pkt length is less than HEARD_LEN at new NtfContext() "+pkt.length);
    			return;
    		}
    		
    		int magic = getMagic(pkt);
    		if(magic != MAGIC){
    			Log.e("BandInfo",  "magic is not match at new NtfContext()");
    			return;
    		}
    		Log.e("BandInfo", "Magic matched");
    		
    		mLen = getLen(pkt);
    		mPkt = new byte[mLen];
    		int len = pkt.length > mPkt.length ? mPkt.length : pkt.length;
    		
    		System.arraycopy(pkt, 4, mPkt, 0, len);
    		mOffset = pkt.length;
    		
    		mCrc = getCrc(pkt);
    	}
    	
    	
    	 public void addPktSeg(byte[] pkt) 
    	 {
             int len = 0;
             if ((mPkt != null) && (pkt != null)) 
             {
                 if (pkt.length + mOffset <= mPkt.length) 
                 {
                     len = pkt.length;
                 }
                 else 
                 {
                     len = mPkt.length - mOffset;
                 }
                 System.arraycopy(pkt, 0, mPkt, mOffset, len);

                 mOffset += len;
             }
         }
    	
    	
    	
    	public boolean isComplete(){
    		if(mPkt == null) 
    			return true;
    		
    		boolean ret  = mOffset > mLen;
    		return ret;
    	}
    	
    	public byte[] getPkt() {
            return mPkt;
        }
    	
    	
    	public boolean checkCrc()
    	{
    		 int relCrc = 0;
    		 
    		 relCrc = Crc16.crc16(mPkt, mLen);	 
    		 return (relCrc == mCrc);
    	}
    	
    	private int getCrc(byte[] pkt) 
    	{
    		int m = 0;
    	    m = ((int) pkt[15] & 0xff) << 8 | (pkt[14] & 0xff);
     
    	    return m;
    	}
    	
    	private int getMagic(byte[] pkt) 
    	{
            int m = 0;
            m = (0xff & ((int) pkt[0])) | (0xff00 & ((int) pkt[1] << 8));
            return m;
        }
    	
    	private int getLen(byte[] pkt)
    	{
            return ((int) pkt[3] & 0xff) << 8 | ((int) pkt[2] & 0xff);
        }
    	
    }
    
}
