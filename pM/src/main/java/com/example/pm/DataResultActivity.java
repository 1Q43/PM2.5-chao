package com.example.pm;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.Inflater;

import app.Entity.State;
import app.utils.ACache;
import app.utils.DBHelper;
import app.utils.ShortcutUtil;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by Administrator on 1/11/2016.
 */
public class DataResultActivity extends Activity implements OnClickListener{

    public static final String TAG = "DataResultActivity";
    ListView mListView;
    List<State> todayStates;
    StateAdapter mAdapter;
    DBHelper dbHelper;
    private SQLiteDatabase db;
    Button mNext;
    Button mLast;
    TextView mAirTitle;
    TextView mTitle;
    Long currentTime = Long.valueOf(0);
    int year;
    int month;
    int day;
    private int resolution_type  = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_result);
        setResolutionParams();
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
        todayStates = getTodayState();
        mListView = (ListView)findViewById(R.id.data_result_listview);
        mNext = (Button)findViewById(R.id.data_result_center_next);
        mNext.setOnClickListener(this);
        mLast = (Button)findViewById(R.id.data_result_center_last);
        mLast.setOnClickListener(this);
        mTitle = (TextView)findViewById(R.id.data_result_center_title);
        mAdapter = new StateAdapter(this,todayStates);
        mAirTitle = (TextView)findViewById(R.id.data_result_airtitle);
        if(resolution_type == 1) mAirTitle.setText("air");
        else mAirTitle.setText("vol_volume");
        mListView.setAdapter(mAdapter);
        setTile(currentTime);
    }

    private void setResolutionParams(){
        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = d.getWidth();
        int height = d.getHeight();
        if(width < 1440) resolution_type = 1;
        else resolution_type = 2;
    }

    private List<State> getTodayState(){
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);

        Long nowTime = calendar.getTime().getTime();
        calendar.set(year, month, day, 23, 59, 59);
        Long nextTime = calendar.getTime().getTime();
        currentTime = nextTime;
        /**Get states of today **/
        List<State> states = cupboard().withDatabase(db).query(State.class).withSelection("time_point > ? AND time_point < ?", nowTime.toString(), nextTime.toString()).list();
        if(states != null)
            return states;
        else return new ArrayList<>();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.data_result_center_last:
                last();
                break;
            case R.id.data_result_center_next:
                next();
                break;
        }
    }

    private void next(){
        Calendar calendar = Calendar.getInstance();
        day++;
        // TODO: 1/18/2016 some cases of the calendar
        calendar.set(year, month, day, 0, 0, 0);

        Long nowTime = calendar.getTime().getTime();
        calendar.set(year, month, day, 23, 59, 59);
        Long nextTime = calendar.getTime().getTime();
        currentTime = nextTime;
        /**Get states of today **/
        List<State> states = cupboard().withDatabase(db).query(State.class).withSelection("time_point > ? AND time_point < ?", nowTime.toString(), nextTime.toString()).list();
        if(states == null) states = new ArrayList<>();
        mAdapter.setData(states);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        setTile(currentTime);

    }

    private void last(){
        Calendar calendar = Calendar.getInstance();
        day--;
        // TODO: 1/18/2016 some cases of the calendar
        calendar.set(year, month, day, 0, 0, 0);

        Long nowTime = calendar.getTime().getTime();
        calendar.set(year, month, day, 23, 59, 59);
        Long nextTime = calendar.getTime().getTime();
        currentTime = nextTime;
        /**Get states of today **/
        List<State> states = cupboard().withDatabase(db).query(State.class).withSelection("time_point > ? AND time_point < ?", nowTime.toString(), nextTime.toString()).list();
        if(states == null) states = new ArrayList<>();
        mAdapter.setData(states);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        setTile(currentTime);
    }

    private void setTile(Long time){
        String str = ShortcutUtil.refFormatNowDate(time);
        mTitle.setText(str);
    }

    private class StateAdapter extends BaseAdapter{

        Context mContext;
        LayoutInflater mInflater;
        List<State> mdata;

        public StateAdapter(Context context,List<State> data){
            mContext = context;
            mdata = data;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(List<State> data){
            mdata = data;
        }

        @Override
        public int getCount() {
            return mdata.size();
        }

        @Override
        public Object getItem(int position) {
            return mdata.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mdata.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.item_data_result,null);
                viewHolder = new ViewHolder();
                viewHolder.mId = (TextView)convertView.findViewById(R.id.data_result_id);
                viewHolder.mUserId = (TextView)convertView.findViewById(R.id.data_result_user_id);
                viewHolder.mDate = (TextView)convertView.findViewById(R.id.data_result_time);
                viewHolder.mLongi = (TextView)convertView.findViewById(R.id.data_result_longi);
                viewHolder.mLati = (TextView)convertView.findViewById(R.id.data_result_lati);
                viewHolder.mStep = (TextView)convertView.findViewById(R.id.data_result_steps);
                viewHolder.mAvgRate = (TextView)convertView.findViewById(R.id.data_result_avg_rate);
                viewHolder.mStatus = (TextView)convertView.findViewById(R.id.data_result_status);
                viewHolder.mOutdoor = (TextView)convertView.findViewById(R.id.data_result_outdoor);
                viewHolder.mAir = (TextView)convertView.findViewById(R.id.data_result_air);
                viewHolder.mPMDensity = (TextView)convertView.findViewById(R.id.data_result_density);
                viewHolder.mPMResult = (TextView)convertView.findViewById(R.id.data_result_pm);
                viewHolder.mSource = (TextView)convertView.findViewById(R.id.data_result_source);
                viewHolder.mUpload = (TextView)convertView.findViewById(R.id.data_result_upload);
                viewHolder.mConnection = (TextView)convertView.findViewById(R.id.data_result_connection);
                viewHolder.mVentilationRate = (TextView) convertView.findViewById(R.id.data_ventilation_rate);
                viewHolder.mPMIntake = (TextView) convertView.findViewById(R.id.data_result_intake);
                viewHolder.mMonitor = (TextView) convertView.findViewById(R.id.data_monitor);
                viewHolder.mToken = (TextView)convertView.findViewById(R.id.data_result_token);
                viewHolder.mH_rate = (TextView)convertView.findViewById(R.id.data_result_heart_rate);
                viewHolder.mV_rate = (TextView)convertView.findViewById(R.id.data_result_ventilation_rate);
                viewHolder.mV_vol = (TextView)convertView.findViewById(R.id.data_result_ventilation_vol);
                viewHolder.mCon = (TextView)convertView.findViewById(R.id.data_result_concen);
                viewHolder.mInk = (TextView)convertView.findViewById(R.id.data_result_intake);
                viewHolder.mMot = (TextView)convertView.findViewById(R.id.data_result_monitor);
                viewHolder.mA_ver = (TextView)convertView.findViewById(R.id.data_result_app_version);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.mId.setText(mdata.get(position).getId() == null?"null":String.valueOf(mdata.get(position).getId()));
            viewHolder.mUserId.setText(mdata.get(position).getUserid() == null? "null":mdata.get(position).getUserid());
            viewHolder.mDate.setText(ShortcutUtil.refFormatDateAndTime(Long.valueOf(mdata.get(position).getTime_point())));
            viewHolder.mLati.setText(cutStringByType(mdata.get(position).getLatitude()));
            viewHolder.mLongi.setText(cutStringByType(mdata.get(position).getLongtitude()));
            viewHolder.mStep.setText(mdata.get(position).getSteps());
            viewHolder.mAvgRate.setText(mdata.get(position).getVentilation_rate());
            viewHolder.mToken.setText("$w34534");
            viewHolder.mH_rate.setText("121");
            viewHolder.mV_rate.setText("64");
            viewHolder.mV_vol.setText("63");
            viewHolder.mCon.setText("rand");
            viewHolder.mInk.setText("185");
            viewHolder.mMot.setText("Running");
            viewHolder.mA_ver.setText("2017.1.16");
            String status = mdata.get(position).getStatus();
            if(status == "1")
                viewHolder.mStatus.setText("S");
            else if(status == "2")
                viewHolder.mStatus.setText("W");
            else if(status == "3")
                viewHolder.mStatus.setText("R");
            viewHolder.mOutdoor.setText(Integer.valueOf(mdata.get(position).getOutdoor())== 1? "out":"in");
            viewHolder.mAir.setText(mdata.get(position).getVentilation_volume().length() < 5?
                    mdata.get(position).getVentilation_volume() : mdata.get(position).getVentilation_volume().substring(0, 4));
            viewHolder.mPMDensity.setText(mdata.get(position).getDensity().length() < 5?
                    mdata.get(position).getDensity() : mdata.get(position).getDensity().substring(0,4));
            viewHolder.mPMResult.setText(mdata.get(position).getPm25().length() < 5?
                    mdata.get(position).getPm25() : mdata.get(position).getPm25().substring(0,4));
            viewHolder.mSource.setText(mdata.get(position).getSource());
            viewHolder.mUpload.setText(mdata.get(position).getUpload() == 0? "N":"Y");
            viewHolder.mConnection.setText(mdata.get(position).getConnection() == 0? "N":"Y");
            return convertView;
        }

        private class ViewHolder{
            TextView mId;
            TextView mUserId;
            TextView mDate;
            TextView mLongi;
            TextView mLati;
            TextView mStep;
            TextView mAvgRate;
            TextView mStatus;
            TextView mOutdoor;
            TextView mAir;
            TextView mPMDensity;
            TextView mPMResult;
            TextView mSource;
            TextView mUpload;
            TextView mConnection;
            TextView mVentilationRate;
            TextView mPMIntake;
            TextView mMonitor;
            TextView mToken;
            TextView mH_rate;
            TextView mV_rate;
            TextView mV_vol;
            TextView mCon;
            TextView mInk;
            TextView mMot;
            TextView mA_ver;
        }

        private String cutStringByType(String str){
            if(resolution_type == 1){
                int length = str.length();
                if(length > 6){
                    str = str.substring(0,6);
                }
            }
            return str;
        }
    }

}
