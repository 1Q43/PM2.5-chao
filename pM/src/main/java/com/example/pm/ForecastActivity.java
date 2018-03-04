package com.example.pm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.Entity.Forecast;
import app.Entity.State;
import app.services.BackgroundService;
import app.services.DataServiceUtil;
import app.utils.DBHelper;
import app.utils.HttpUtil;
import app.utils.VolleyQueue;

import static com.facebook.FacebookSdk.getApplicationContext;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class ForecastActivity extends Activity {

    private Button mButton;
    private Button HttpButton;
    private Button InfoButton;
    private DBHelper dbHelper;
    private TextView AQIText;
    private TextView HTEMPText;
    private TextView LTEMPText;
    private TextView PM25Text;
    private TextView confirm;
    private TextView TOMOPM25Text;
    private String AQI = "-";
    private String HTEMP = "-";
    private String LTEMP = "-";
    private String PM25 = "-";
    private String TOMOPM25 = "";

    private DataServiceUtil mDataService;
    private NotificationManager mNotifyMgr;
    private Notification notification;


    private TextView top_label;

    // 11/12 获取用户当前已经吸入的总空气量
    private int total_time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        dbHelper = new DBHelper(getApplicationContext());
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, ForecastActivity.class), 0);

        notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("PM警报")
                .setContentText("PM爆表了")
                .setContentIntent(contentIntent)
                .build();
        viewInitial();
        listenerInital();

    }

    /**
     * Init all the views
     */
    private void viewInitial(){

        AQIText = (TextView) findViewById(R.id.AQI);
        HTEMPText = (TextView) findViewById(R.id.HTEMP);
        LTEMPText = (TextView) findViewById(R.id.LTEMP);
        PM25Text = (TextView) findViewById(R.id.PM25);
        TOMOPM25Text = (TextView) findViewById(R.id.TOMO_PM25);
        confirm = (TextView) findViewById(R.id.forecast_sure);

        top_label = (TextView) findViewById(R.id.top_label);

        AQIText.setText(AQI);
        HTEMPText.setText(HTEMP);
        LTEMPText.setText(LTEMP);
        PM25Text.setText(PM25);
        TOMOPM25Text.setText("PM吸入量：计算中...");

        mDataService = DataServiceUtil.getInstance(getApplicationContext());

        mButton = (Button) findViewById(R.id.forecast_test_db);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Forecast> forecasts =  DataServiceUtil.getInstance(getApplicationContext()).getAllForecast();
                for (Forecast forecast: forecasts){
                    forecast.print();
                    forecast.debugs("tomorrow");
                }
                Log.e("forcast number", String.valueOf(forecasts.size()));
                Log.e("unupload", String.valueOf(DataServiceUtil.getInstance(getApplicationContext()).seeUnUploadStatesNumber()));
                DataServiceUtil.getInstance(getApplicationContext()).getTomorrowForecast();
            }
        });

        HttpButton = (Button) findViewById(R.id.forecast_test_http);
        HttpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pmWarningDetecter();
                checkPMDataForUpload();

                double[] inOutData = DataServiceUtil.getInstance(getApplicationContext()).getTomorrowForecast();

                if (inOutData[0] < 1.0) {
                    TOMOPM25 = "运行时间过短，无法预测！";
                }else {

                    double rate_indoor = 0;
                    double rate_outdoor = 0;
                    rate_outdoor = inOutData[1]/(inOutData[0] + inOutData[1]);
                    rate_indoor = 1 - rate_outdoor;
                    //TOMOPM25 = "PM吸入量:" + String.valueOf(new DecimalFormat("####.##").format( (10*rate_indoor + 10*rate_outdoor) * Double.valueOf(PM25)) );//室内体积为L

                    // 根据当前吸入量来估计
                    TOMOPM25 = "PM吸入量:" + String.valueOf( new DecimalFormat("####.##").format(( inOutData[0]* getAllDayTime()/ (24 * 60)/1000 + inOutData[1] * getAllDayTime()/ (24 * 60)/1000) * Double.valueOf(PM25)));

                    top_label.setText("明日预测"+"("+ getPredict() + ")");

                    getAllDayTime();
                }


            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForecastActivity.this.finish();
            }
        });

        InfoButton = (Button) findViewById(R.id.forecast_test_info);
        InfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotifyMgr.notify(0, notification);
                mDataService.insertForecast(mDataService.calculateOutAndInTime(mDataService.getStateToday()));
            }
        });

        String url = HttpUtil.Predict_url + DataServiceUtil.getInstance(getApplicationContext()).getCityNameFromCache();
        url = url.substring(0, url.length() - 1);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Wea_Back", response.toString());

                try {
                    AQI = response.getString("AQI");
                    AQIText.setText(AQI);
                    HTEMP = response.getString("HTEMP");
                    HTEMPText.setText(HTEMP);
                    LTEMP = response.getString("LTEMP");
                    LTEMPText.setText(LTEMP);
                    PM25 = response.getString("PM25");
                    PM25Text.setText(PM25);

                    double[] inOutData = DataServiceUtil.getInstance(getApplicationContext()).getTomorrowForecast();

                    if (inOutData[0] < 1.0) {
                        TOMOPM25 = "运行时间过短，无法预测！";
                    }else {
                        Log.e("tomorrow","从服务器端得到的PM25:"+PM25);
                        Log.e("tomorrow","预测在室内吸入总体积:"+inOutData[0]);
                        Log.e("tomorrow","预测在室外吸入总体积:"+inOutData[1]);

                        // 采用固定吸入量来预测
                        double rate_indoor = 0;
                        double rate_outdoor = 0;
                        rate_outdoor = inOutData[1]/(inOutData[0] + inOutData[1]);
                        rate_indoor = 1 - rate_outdoor;
                        //TOMOPM25 = "PM吸入量:" + String.valueOf( new DecimalFormat("####.##").format((9.78*rate_indoor + 9.78*rate_outdoor) * Double.valueOf(PM25)));

                        // 根据当前吸入量来估计
                        TOMOPM25 = "PM吸入量:" + String.valueOf( new DecimalFormat("####.##").format(( inOutData[0]* getAllDayTime()/ (24 * 60)/1000 + inOutData[1] * getAllDayTime()/ (24 * 60)/1000) * Double.valueOf(PM25)));

                        Log.e("tomorrow", "PM吸入量:" + String.valueOf(new DecimalFormat("####.##").format((inOutData[0] * getAllDayTime()/ (24 * 60)/1000 + inOutData[1] * getAllDayTime()/ (24 * 60)/1000) * Double.valueOf(PM25))));
                        top_label.setText("明日预测" + "(" + getPredict() + ")");

                        getAllDayTime();
                    }

                    TOMOPM25Text.setText(TOMOPM25);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        VolleyQueue.getInstance(getApplicationContext()).addToRequestQueue(request);


    }

    /**
     * Init all the listener
     */
    private void listenerInital(){

    }


    public void checkPMDataForUpload() {
        final DataServiceUtil dataServiceUtil = DataServiceUtil.getInstance(getApplicationContext());

        dataServiceUtil.cacheLastUploadTime(System.currentTimeMillis());
        int idStr = dataServiceUtil.getUserIdFromCache();
        String tokenStr=dataServiceUtil.getTokenFromCache();

        if (idStr != 0) {
            final List<State> states = dataServiceUtil.getPMDataForUpload();
            //FileUtil.appendStrToFile(DBRunTime, "1.checkPMDataForUpload upload batch start size = " + states.size());
            String url = HttpUtil.UploadBatch_url;
            JSONArray array = new JSONArray();
            final int size = states.size() < 1000 ? states.size() : 1000;
            for (int i = 0; i < size; i++) {
                JSONObject tmp = State.toJsonobject(states.get(i), String.valueOf(idStr));
                array.put(tmp);
            }
            JSONObject batchData = null;
            try {
                batchData = new JSONObject();
                batchData.put("data", array);
                batchData.put("access_token", tokenStr);
                //batchData.put(tokenStr,array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, batchData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.e("Back_upload",response.toString());
                        int token_status = response.getInt("token_status");
                        Log.e("token_status", String.valueOf(token_status));
                        if (token_status == 1) {
                            String value = response.getString("succeed_count");
                            if (Integer.valueOf(value) == size) {
                                for (int i = 0; i < size; i++) {
                                    dataServiceUtil.updateStateUpLoad(states.get(i), 1);
                                }
                            }
                            if (Integer.valueOf(value) == 0){
                                for (int i = 0; i < 1000; i++) {
                                    try{
                                        dataServiceUtil.updateStateUpLoad(states.get(i), 1);//
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.e("size", String.valueOf(states.size()));
                    if (states.size() > 1000) {
                        for (int i = 0; i < 1000; i++) {
                            dataServiceUtil.updateStateUpLoad(states.get(i), 1);
                        }
                    }
                }
            }) {
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };

            VolleyQueue.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        }
    }

    public void notifyUser(){

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ForecastActivity.class), 0);


        // 增加判断PM25的值是否为“-”
        if(PM25.equals("-")){
            String url = HttpUtil.Predict_url + DataServiceUtil.getInstance(getApplicationContext()).getCityNameFromCache();
            url = url.substring(0, url.length() - 1);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("Wea_Back", response.toString());

                    try {
                        PM25 = response.getString("PM25");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }) {
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
        }


        Notification notificationes = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("PM 警报")
                .setContentText("PM爆表了(PM2.5为:" + Double.valueOf(PM25) + ")")
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .build();
        mNotifyMgr.notify(0, notificationes);
    }

    public void pmWarningDetecter(){
        String url = HttpUtil.Predict_url + DataServiceUtil.getInstance(getApplicationContext()).getCityNameFromCache();
        url = url.substring(0, url.length() - 1);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Wea_Back", response.toString());

                try {
                    String pm25 = response.getString("PM25");
                    int pmVal =  Integer.valueOf(pm25);

                    if (pmVal > 100){// 11.5从100改到10
                        notifyUser();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        VolleyQueue.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void saveInOutdoor(List<State> states){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 19, 0, 0);
        Long sevenClock = calendar.getTime().getTime();
        calendar.set(year, month, day, 0, 0, 0);
        Date date = new Date();
        Long now = date.getTime();

        DataServiceUtil.getInstance(getApplicationContext()).insertForecast(DataServiceUtil.getInstance(getApplicationContext()).calculateOutAndInTime(states));
    }


    private  String getPredict(){
        long time=System.currentTimeMillis();
        Date date=new Date(time);
        SimpleDateFormat format=new SimpleDateFormat("EEEE");
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String predict = "";
        if(hour<19){
            predict += (month+1) + "-" + day + " " + format.format(date);
        }else{
            predict += (month+1) + "-" + (day+1) + " " + format.format(new Date(time+43200000));//如果超过当天夜晚7点，在当前时间+12小时，得到第二天的星期数
        }
        return predict;
    }

    private int getAllDayTime(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);
        Long nowTime = calendar.getTime().getTime();
        calendar.set(year, month, day, 23, 59, 59);
        Long nextTime = calendar.getTime().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /**Get forcast of today **/
        List<State> state = cupboard().withDatabase(db).query(State.class).withSelection("time_point > ? AND time_point < ?", nowTime.toString(), nextTime.toString()).list();
        if (state.size() > 0){
            Log.e("tomorrow","" + state.size());
            for(int i=0;i<state.size();i++){
                //Log.e("tomorrow", "" + sdf.format(new Date( (int) Double.parseDouble(state.get(i).getTime_point()) )));
                Log.e("tomorrow", "当前时间:" +  sdf.format(new Date( Long.valueOf(state.get(i).getTime_point())) ) );
            }
            Log.e("tomorrow", "当前state.size()为:" +  state.size() );
            return state.size();
        }
        return 0;
    }

}
