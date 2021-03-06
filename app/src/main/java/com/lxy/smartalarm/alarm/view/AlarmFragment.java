package com.lxy.smartalarm.alarm.view;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lxy.smartalarm.R;
import com.lxy.smartalarm.alarm.adapter.AlarmAdapter;
import com.lxy.smartalarm.alarm.db.AlarmDB;
import com.lxy.smartalarm.alarm.ui.AddAlarmActivity;
import com.lxy.smartalarm.alarm.ui.AlarmServer;
import com.lxy.smartalarm.base.BaseAdapter;
import com.lxy.smartalarm.utils.DBUtil;
import com.lxy.smartalarm.utils.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AlarmFragment extends Fragment {

    private RecyclerView alarmRecycler;
    private FloatingActionButton alarmAdd;
    private AlarmAdapter alarmAdapter;
    private List<AlarmDB> list;
    public static final int REQUEST_CODE = 505;
    private AlarmManager alarmManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener();
//        initAlarmManager();
    }

    private void initView(View view) {
        alarmRecycler = view.findViewById(R.id.alarm_recycler);
        alarmAdd = view.findViewById(R.id.alarm_add);
        list = new ArrayList<>();
        alarmAdapter = new AlarmAdapter(getContext(),list,R.layout.fragment_item);
        alarmRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        alarmRecycler.setAdapter(alarmAdapter);
        alarmRecycler.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        initData();
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
    }

    private void initData() {
        List<AlarmDB> dbList = DBUtil.getInstance().getDaoSession().getAlarmDBDao().queryBuilder().list();
        list.clear();
        list.addAll(dbList);
        alarmAdapter.notifyDataSetChanged();
    }

    private void initListener() {
        alarmAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddAlarmActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }

        });

    }


    /**
     * 初始化闹钟提醒器
     */
    private void initAlarmManager() {
        Intent intent = new Intent();
        intent.setAction("com.Android.AlarmManager.action.BACK_ACTION");

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getOpen()) {
                String db = list.get(i).getTime();
                intent.putExtra("note",list.get(i).getNote());
                PendingIntent pendingIntent =
                        PendingIntent.getBroadcast(getContext(), i, intent, PendingIntent.FLAG_IMMUTABLE);
                int hour = Integer.parseInt(db.split(":")[0]);
                int min = Integer.parseInt(db.split(":")[1]);
                Calendar calendar = Calendar.getInstance();

                int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
                int nowMin = calendar.get(Calendar.MINUTE);
                Log.i("time",nowHour + "    nowHour");
                long subTime = 0L;
                String timeOut = "";
                if (hour >= nowHour && min > nowMin) {
                    // 当前时间还未到响铃时间
                    subTime = ((hour - nowHour) * 60 + (nowMin - min)) * 60 * 1000;
                    timeOut = (hour - nowHour) + "分钟" + (nowMin - min) + "分钟后响铃";
                } else {
                    long temp = (Math.abs(hour - nowHour) * 60 + Math.abs(nowMin - min)) * 60 * 1000;
                    subTime = (24 * 60 * 60 * 1000) - temp;
                }
                Log.i("time",subTime + "    cc");
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ subTime, pendingIntent);
            }
            alarmAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        initData();
//        initAlarmManager();
    }


}
