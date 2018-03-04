package app.bandmodule;

import android.app.Activity;
import android.app.DownloadManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pm.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import app.Entity.HeartRate;
import app.services.DataServiceUtil;
import app.utils.DBHelper;



public class MonitorDisplayActivity extends Activity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = MonitorDisplayActivity.class.getSimpleName();
    private boolean mGPSReadEnabled = false;
    private TextView mTestStatus;
    private int times_count = 1;
    private int packet_num = 0;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();//new ArrayList<>();
    private TextView mConnectionState;
    private TextView mLocationTx;
    private TextView mHeartRateTx;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private BluetoothLeService mBluetoothLeService;

    DBHelper dbHelper;
    TextView mSavedheart;
    TextView mFindedheart;
    int tempcount;
    int tempsave;
    private Button BSave;
    private Button FButton;
    private Button DeleteButton;
    private Button UploadButton;
    private SQLiteDatabase db;
    // 新建一个list来保存数据
    List<Integer> heartratelist = new LinkedList<Integer>();
    List<Long> timelist = new LinkedList<Long>();
    List<Integer> getheartlist = new LinkedList<Integer>();
    List<Integer> getisuploadlist = new LinkedList<Integer>();
    List<Long> gettimelist = new LinkedList<Long>();
    private long timecount_flag = 0;
    private long timecountafter_flag = 0;
    private long time_instant = System.currentTimeMillis();


    Boolean SaveHeartRateButtonFlag = true;
    Boolean FindHeartRateButtonFlag = true;
    Boolean UploadHeartRateButtonFlag = true;

    String readid = "";
    int readheartrate = 0;
    Long readtime = null;

    // orm存储模式
    private static boolean ORM = true;
    private static DataServiceUtil dataServiceUtil = null;
    private boolean time_flag = false;
    private TextView mTransStatus;

    public String sendOkhttp(String url, String params,String id,String heartrate,Long time) throws IOException {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        //创建一个Request
        final Request request = new Request.Builder()
                .url(url+params)
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        Response reponse = call.execute();
        if(reponse.isSuccessful()){
            if (db.isOpen() == false) {
                db = dbHelper.getWritableDatabase();
            }

            // 加入事务处理，保护数据库由于频繁访问而造成的崩溃问题
            try{
                db.beginTransaction();
                Cursor cursor = null;

                try{
                    cursor = db.query("banddata",null,"_id=?",new String[]{id},null,null,null);
                    Thread.sleep(10);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(cursor.getCount() != 0 || cursor == null){
                    while (cursor.moveToNext()){
                        readid = cursor.getString(cursor.getColumnIndex("_id"));
                        readheartrate = cursor.getInt(cursor.getColumnIndex("heartrate"));
                        readtime = cursor.getLong(cursor.getColumnIndex("time"));

                        ContentValues contentValues = new ContentValues();
                        contentValues.put("isupload", 1);
                        db.update("banddata",contentValues,"_id=?",new String[]{String.valueOf(id)});

                    }
                }
                db.setTransactionSuccessful();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }



        }
        Log.e("BandInfo-",String.valueOf(reponse));
        //请求加入调度

        /*
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {
            }

            @Override
            public void onResponse(final Response response) throws IOException
            {
                //String htmlStr =  response.body().string();
            }
        });
        */

        return null;
    }


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    Toast.makeText(getApplicationContext(), "数据保存完毕", Toast.LENGTH_SHORT).show();
                    mSavedheart.setText(" " + tempsave);
                    SaveHeartRateButtonFlag = true;
                    FindHeartRateButtonFlag = true;
                    UploadHeartRateButtonFlag = true;
                }
                break;
                case 1:{
                    Toast.makeText(getApplicationContext(), "数据查询完毕", Toast.LENGTH_SHORT).show();
                    mFindedheart.setText(" " + tempcount);
                    SaveHeartRateButtonFlag = true;
                    FindHeartRateButtonFlag = true;
                    UploadHeartRateButtonFlag = true;
                }
                break;
                case 2:{
                    Toast.makeText(getApplicationContext(), "数据上传完毕", Toast.LENGTH_SHORT).show();
                    SaveHeartRateButtonFlag = true;
                    FindHeartRateButtonFlag = true;
                    UploadHeartRateButtonFlag = true;
                    /*DeleteButton.performClick();*/
                }
                break;
                case 3:{
                    Toast.makeText(getApplicationContext(), "本地一周前数据已删除", Toast.LENGTH_SHORT).show();
                    /*DeleteButton.performClick();*/
                }
                break;
                default:
                    break;
            }
        };

    };


    class DeleteThread extends  Thread{
        public void run(){
            if(ORM == false){
                long time_flag = time_instant - 7*24*3600*1000;
                db.delete("banddata","time < ?",new String[]{String.valueOf(time_flag)});
            }else {
                dataServiceUtil.deleteWeekData();
            }
            handler.sendEmptyMessage(3);
        }
    }

    class UploadThread extends Thread {
        public void run() {
            SaveHeartRateButtonFlag = false;
            FindHeartRateButtonFlag = false;
            UploadHeartRateButtonFlag = false;
            LinkedList<String> uploadid = new LinkedList<String>();
            LinkedList<Integer> uploadheartlist = new LinkedList<Integer>();
            LinkedList<Long> uploadtimelist = new LinkedList<Long>();
            SimpleDateFormat year_form = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat hour_form = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat time_form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.e("BandInfo-", "正在查询需要上传的数据");

            if(ORM == false){
                if (db.isOpen() == false) {
                    db = dbHelper.getWritableDatabase();
                }
                Cursor cursor;
                try {
                    cursor = db.query("banddata", null, null, null, null, null, null);
                    while (cursor.moveToNext()) {
                        String _id = cursor.getString(cursor.getColumnIndex("_id"));
                        int heartratenum = cursor.getInt(cursor.getColumnIndex("heartrate"));
                        long timenum = cursor.getLong(cursor.getColumnIndex("time"));
                        Log.e("BandInfo-","在正在查找的id为:"+_id);
                        // 增加判断该条数据是否上传过的逻辑，如果上传过，不把数据加入上传列表
                        if(cursor.getInt(cursor.getColumnIndex("isupload")) != 1){
                            uploadid.add(_id);
                            uploadheartlist.add(heartratenum);
                            uploadtimelist.add(timenum);
                        }
                    }
                } catch (Exception e) {

                }
                Log.e("BandInfo-", "banddata中的数据量为: " + uploadheartlist.size());
                tempcount = uploadheartlist.size();
                for (int numbers : uploadheartlist) {
                    Log.e("BandInfo-", " " + numbers);
                }

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String url = "http://106.14.63.93:8080/post?";
                String id;
                int username = dataServiceUtil.getUserIdFromCache();
                String beats;
                String time_point;
                String postbody;
                // 开始数据上传
                for (int i=0;i<uploadheartlist.size();i++) {
                    id = String.valueOf(uploadid.get(i));
                    beats = String.valueOf(uploadheartlist.get(i));
                    time_point = time_form.format(uploadtimelist.get(i));

                    postbody = "user_id=" + "116&"
                            + "user_name=" + "chao&"
                            + "time_point=" + time_point + "&"
                            + "beats=" + beats;
                    try {
                        sendOkhttp(url, postbody, id, beats,uploadtimelist.get(i));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(i%200==0){
                        try {
                            sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("BandInfo-", url + postbody);
                }
            }else{
                // 使用ORM之后的上传逻辑
                dataServiceUtil.uploadHeartRateToInternet();
            }

            handler.sendEmptyMessage(2);
        }
    }
        class TaskThread extends Thread {
            public void run() {
                SaveHeartRateButtonFlag = false;
                FindHeartRateButtonFlag = false;
                UploadHeartRateButtonFlag = false;
                Log.e("BandInfo-", "正在保存数据");

                /*if (db.isOpen() == false) {
                    db = dbHelper.getWritableDatabase();
                }

                ContentValues values = new ContentValues();
                //db.beginTransaction();
                List<Integer> tempheartlist = new LinkedList<Integer>();
                List<Long> temptimelist = new LinkedList<Long>();
                HeartRate heartrate = new HeartRate();
                try {
                    tempheartlist = heartratelist;
                    temptimelist = timelist;
                    Log.e("BandInfo", "list长度为: " + tempheartlist.size());
                    tempsave = heartratelist.size();
                    // 保存之后，清空内容
                    heartratelist = new LinkedList<Integer>();
                    timelist = new LinkedList<Long>();

                    for (int i = 0; i < tempheartlist.size(); i++) {
                        //db.execSQL("insert into banddata(heartrate)values(heart)");
                        if(ORM == false){
                            values.put("heartrate", tempheartlist.get(i));
                            values.put("time", temptimelist.get(i));
                        }else{
                            heartrate.setHeartrate(tempheartlist.get(i));
                            heartrate.setTime(temptimelist.get(i));
                        }
                        Log.e("Monitor", "保存:" + tempheartlist.get(i));
                        Log.e("Monitor", "保存:" + temptimelist.get(i));



                        // 如果插入数据失败，判断数据库banddata表出问题
                        try{
                            if(ORM == false){
                                db.insert("banddata", null, values);
                            }else{
                                dataServiceUtil.insertHeartRate(heartrate);
                                Log.e("BandInfo-","使用ORM存储");
                            }
                        }catch (Exception e){
                            if(ORM == false){
                                db.execSQL("CREATE TABLE IF NOT EXISTS banddata(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                        "username STRING," +
                                        "heartrate INTEGER," +
                                        "time LONG," +
                                        "gps STRING," +
                                        "isupload INTEGER," +
                                        "isdelete INTEGER);");
                            }
                        }
                    }
                    //db.setTransactionSuccessful();
                } finally {
                    //db.endTransaction();
                }

                Log.e("BandInfo-", "数据保存成功");
                Log.e("BandInfo-", "保存了:" + tempheartlist.size());

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                handler.sendEmptyMessage(0);
            };

        }

        class SearchThread extends Thread {
            public void run() {
                SaveHeartRateButtonFlag = false;
                FindHeartRateButtonFlag = false;
                UploadHeartRateButtonFlag = false;
                getheartlist = new LinkedList<Integer>();
                gettimelist = new LinkedList<Long>();
                getisuploadlist = new LinkedList<Integer>();
                Log.e("BandInfo-", "正在查询数据");
                if(ORM == false){
                    if (db.isOpen() == false) {
                        db = dbHelper.getWritableDatabase();
                    }
                    Cursor cursor = null;
                    try{
                        cursor = db.query("banddata", null, null, null, null, null, null);
                        while (cursor.moveToNext()) {
                            String _id = cursor.getString(cursor.getColumnIndex("_id"));
                            int heartratenum = cursor.getInt(cursor.getColumnIndex("heartrate"));
                            long timenum = cursor.getLong(cursor.getColumnIndex("time"));
                            int isupload = cursor.getInt(cursor.getColumnIndex("isupload"));
                            getheartlist.add(heartratenum);
                            gettimelist.add(timenum);
                            getisuploadlist.add(isupload);
                        }
                    }catch (Exception e){
                        db.execSQL("CREATE TABLE IF NOT EXISTS banddata(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "username STRING," +
                                "heartrate INTEGER," +
                                "time LONG," +
                                "gps STRING," +
                                "isupload INTEGER," +
                                "isdelete INTEGER);");
                    }finally {
                        if(cursor != null){
                            cursor.close();
                        }
                    }
                    Log.e("BandInfo-", "banddata中的数据量为: " + getheartlist.size());
                    tempcount = getheartlist.size();
                    for (int numbers : getheartlist) {
                        Log.e("BandInfo-", " " + numbers);
                    }
                    for (long times : gettimelist){
                        Log.e("BandInfo-"," " + new Date(times));
                    }
                    for (int uploadflag : getisuploadlist){
                        Log.e("BandInfo-"," " + uploadflag);
                    }
                }else{
                    tempcount = dataServiceUtil.getAllHeartRate().size();
                    Log.e("BandInfo-"," " + tempcount);
                    for(HeartRate heartRate:dataServiceUtil.getAllHeartRate()){
                        //Log.e("BandInfo-"," username: " + heartRate.getUsername() + "  isUpload: " + heartRate.getIsupload() + "  heartrate:" +heartRate.getHeartrate()+"  id:" +heartRate.getId());
                    }

                }


                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(1);
            };

        }




    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_display_activity);
        getActionBar().setTitle(R.string.monitor_title);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        mConnectionState = (TextView) findViewById(R.id.connection_state);
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mLocationTx = (TextView) findViewById(R.id.tx_loncation);
        mHeartRateTx = (TextView) findViewById(R.id.tx_hr);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //Begin: add by T0213-ZH, Date: 20170414
        mTestStatus = (TextView) findViewById(R.id.test_status);
        final Button BTest = (Button) findViewById(R.id.button);

        // 2017.7.7
        BSave = (Button) findViewById(R.id.savedata);
        mSavedheart = (TextView) findViewById(R.id.dbheart);
        FButton = (Button) findViewById(R.id.querydatanumbers);
        mFindedheart = (TextView) findViewById(R.id.datanumbers);
        DeleteButton = (Button) findViewById(R.id.clearalldata);
        UploadButton = (Button) findViewById(R.id.uploaddata);


        mTransStatus = (TextView) findViewById(R.id.transport_status);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dataServiceUtil =  DataServiceUtil.getInstance(getApplicationContext());


        // 创建数据库的操作对象
        /*
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        if(ORM == false){
            db.execSQL("CREATE TABLE IF NOT EXISTS banddata(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username STRING," +
                    "heartrate INTEGER," +
                    "time LONG," +
                    "gps STRING," +
                    "isupload INTEGER," +
                    "isdelete INTEGER);");
        }
        */


        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteThread().run();
            }
        });

        BTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] clear_flash_cmd = new byte[]{(byte) 0xAA, 0x55, 0x02, 0x01, 0x00};
                mWriteCharacteristic.setValue(clear_flash_cmd);
                mBluetoothLeService.writeCharacteristic(mWriteCharacteristic);
                //mTestStatus.setText("set erase flash mark completed!");
            }
        });

        BSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("BandInfo-", "开始存储数据");
                if (SaveHeartRateButtonFlag == true) {
                    new TaskThread().start();
                } else {
                    Toast.makeText(getApplicationContext(), "正在保存数据，请勿频繁点击", Toast.LENGTH_SHORT).show();
                }

            }
        });

        FButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("BandInfo-", "开始查询数据");
                if (FindHeartRateButtonFlag == true) {
                    new SearchThread().start();
                } else {
                    Toast.makeText(getApplicationContext(), "正在查询数据，请勿频繁点击", Toast.LENGTH_SHORT).show();
                }

            }
        });

        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("BandInfo-", "开始上传数据");
                if(UploadHeartRateButtonFlag == true){
                    new UploadThread().start();
                }else{
                    Toast.makeText(getApplicationContext(), "正在上传数据，请勿频繁点击", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //End
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        dataServiceUtil =  DataServiceUtil.getInstance(getApplicationContext());
        // 在onResume为时间参考点设置新
        time_instant = System.currentTimeMillis();
        timecount_flag = 0;
        Log.e("Monitor", "onResume " + time_instant);


        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mGattUpdateReceiver);
        stopDataAccess();
        super.onPause();

    }

    @Override
    protected void onDestroy() {


        // 增加保存未手动保存的数据
        // BSave.performClick();

        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        super.onDestroy();
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayGattServices(final List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        final String unknownServiceString = getResources().getString(R.string.unknown_service);
        final String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        final List<Map<String, String>> gattServiceData = new ArrayList<>();
        final List<List<Map<String, String>>> gattCharacteristicData = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();
        for (final BluetoothGattService gattService : gattServices) {
            final Map<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            final List<Map<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            final List<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                final Map<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                Log.i("TZH", "UUID:" + uuid);
                if (uuid.equals(getString(R.string.SEND_UUID))) {
                    Log.i("TZH", "SEND UUID:" + uuid);
                    mWriteCharacteristic = gattCharacteristic;
                }
            }
        }
    }

    //End
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                Log.e("Monitor", "连接");

                times_count = 1;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.d(TAG, "Receive ACTION_GATT_SERVICES_DISCOVERED");
                Log.e("Monitor", "Receive ACTION_GATT_SERVICES_DISCOVERED");
                //Begin: add by T0213-ZH, Date:20170425
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                //End
                startDataAccess();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                int flag = 0;

                flag = intent.getIntExtra(BluetoothLeService.STORE_COUNT, 0);
                if (flag > 0) {
                    /*Log.e("GongZu", "normal data");
                    Log.e("Monitor", "normal data");
                    Log.e("Monitor", " " + flag);
                    Log.e("Monitor", " " + packet_num);
                    Log.e("Monitor", " " + timecount_flag);
                    Log.e("Monitor", " " + time_instant);*/
                    updateInfo(intent.getStringExtra(BluetoothLeService.GPS_DATA), intent.getIntExtra(BluetoothLeService.HEART_RATE_DATA, 0));

                    packet_num += 1;
                    // 增加存储到数据库的逻辑
                    HeartRate heartrate = new HeartRate();
                    if(flag == 1){
                        timecount_flag = timecount_flag + 1000;
                        // 增加实时存储逻辑，如果实时存储出错，那么采用手动保存的方式

                        try{
                            heartrate.setUserid(dataServiceUtil.getUserIdFromCache());
                            heartrate.setUsername(dataServiceUtil.getUserNameFromCache());
                            heartrate.setHeartrate(intent.getIntExtra(BluetoothLeService.HEART_RATE_DATA, 0));
                            heartrate.setTime(time_instant - timecount_flag);
                            Log.e("BandInfo-", "用户ID:" + dataServiceUtil.getUserIdFromCache());
                            Log.e("BandInfo-", "用户名:" + dataServiceUtil.getUserNameFromCache());
                            dataServiceUtil.insertHeartRate(heartrate);
                            //Log.e("BandInfo-", "使用ORM存储:存储数据 " + "心率:" + intent.getIntExtra(BluetoothLeService.HEART_RATE_DATA, 0) + " " + new Date(time_instant - timecount_flag));
                        }catch (Exception e){
                            e.printStackTrace();
                            heartratelist.add(intent.getIntExtra(BluetoothLeService.HEART_RATE_DATA, 0));
                            timelist.add(time_instant - timecount_flag);
                        }
                    }else if(flag == 2){
                        timecountafter_flag = timecountafter_flag + 1000;
                        try{
                            heartrate.setUserid(dataServiceUtil.getUserIdFromCache());
                            heartrate.setUsername(dataServiceUtil.getUserNameFromCache());
                            heartrate.setHeartrate(intent.getIntExtra(BluetoothLeService.HEART_RATE_DATA, 0));
                            heartrate.setTime(time_instant + timecountafter_flag);
                            Log.e("BandInfo-", "用户ID:" + dataServiceUtil.getUserIdFromCache());
                            Log.e("BandInfo-", "用户名:" + dataServiceUtil.getUserNameFromCache());
                            dataServiceUtil.insertHeartRate(heartrate);
                            //Log.e("BandInfo-", "使用ORM存储:实时数据 " + "心率:" + intent.getIntExtra(BluetoothLeService.HEART_RATE_DATA, 0) + " " + new Date(time_instant + timecountafter_flag));
                        }catch (Exception e){
                            e.printStackTrace();
                            heartratelist.add(intent.getIntExtra(BluetoothLeService.HEART_RATE_DATA, 0));
                            timelist.add(time_instant + timecountafter_flag);
                        }
                    }

                    if (flag == 1){
                        mTestStatus.setText("第" + times_count + "次" + "，数据量：" + packet_num);

                        mTransStatus.setText("传输模式:存储数据");
                        //Log.e("BandInfo-","在传输存储的数据" + packet_num);
                    }else if(flag == 2){
                        mTestStatus.setText("第" + times_count + "次" + "，数据量：" + packet_num);

                        mTransStatus.setText("传输模式:实时数据");
                        //Log.e("BandInfo-", "在传输实时数据" + packet_num);
                    }

                } else {
                    //Log.e("Monitor", "next time");
                    //Log.e("BandInfo-", "进行实时数据传输");
                    times_count += 1;
                    packet_num = 0;
                    mTestStatus.setText("第" + times_count + "次" + "，数据量：" + packet_num);
                }
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void startDataAccess() {
        mBluetoothLeService.setDataAccess(true);
        mGPSReadEnabled = true;
    }

    private void stopDataAccess() {
        if (mGPSReadEnabled) {
            mBluetoothLeService.setDataAccess(false);
            mGPSReadEnabled = false;
        }
    }

    private void updateInfo(String locationStr, int hr) {
        mLocationTx.setText(locationStr);
        mHeartRateTx.setText(hr + "");
    }

}