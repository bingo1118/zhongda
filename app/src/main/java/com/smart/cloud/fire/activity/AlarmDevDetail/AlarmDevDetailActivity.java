package com.smart.cloud.fire.activity.AlarmDevDetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.smart.cloud.fire.adapter.DateNumericAdapter;
import com.smart.cloud.fire.adapter.RefreshRecyclerAdapter;
import com.smart.cloud.fire.base.ui.MvpActivity;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.mvp.fragment.CollectFragment.AlarmMessageModel;
import com.smart.cloud.fire.mvp.fragment.CollectFragment.CollectFragment;
import com.smart.cloud.fire.mvp.fragment.CollectFragment.CollectFragmentPresenter;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.Utils;
import com.smart.cloud.fire.utils.VolleyHelper;
import com.smart.cloud.fire.view.AreaChooceListView;
import com.smart.cloud.fire.view.OnWheelScrollListener;
import com.smart.cloud.fire.view.WheelView;
import com.smart.cloud.fire.view.XCDropDownListViewFire;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;

public class AlarmDevDetailActivity extends MvpActivity<AlarmDevDetailPresenter> implements AlarmDevDetailView, View.OnFocusChangeListener {

    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.start_time)
    EditText startTime;
    @Bind(R.id.end_time)
    EditText endTime;

    @Bind(R.id.delete_start_time_rela)
    RelativeLayout deleteStartTimeRela;
    @Bind(R.id.delete_end_time_rela)
    RelativeLayout deleteEndTimeRela;
    @Bind(R.id.type_lin)
    LinearLayout typeLin;
    @Bind(R.id.demo_recycler)
    RecyclerView demoRecycler;
    @Bind(R.id.demo_swiperefreshlayout)
    SwipeRefreshLayout demoSwiperefreshlayout;
    @Bind(R.id.layout_cNumber)
    RelativeLayout layoutCNumber;
    @Bind(R.id.layout_cNumber2)
    RelativeLayout layoutCNumber2;
    private String userID;
    private int privilege;
    private String page;
    private Context mContext;
    private AlarmDevDetailPresenter mPresenter;
    private boolean research = false;
    private List<AlarmMessageModel> messageModelList;
    private int loadMoreCount;
    boolean isDpShow = false;
    private LinearLayoutManager linearLayoutManager;
    private RefreshRecyclerAdapter adapter;
    private int lastVisibleItem;
    private int deal_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_dev_detail);
        ButterKnife.bind(this);

        mContext = this;
        userID = MyApp.getUserID();
        privilege = MyApp.app.getPrivilege();
        page = "1";
        mvpPresenter.getAllAlarm(userID, privilege + "", page);
        init();
    }
    private void init() {
        //设置刷新时动画的颜色，可以设置4个
        demoSwiperefreshlayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        demoSwiperefreshlayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        demoSwiperefreshlayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        linearLayoutManager=new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        demoRecycler.setLayoutManager(linearLayoutManager);

        demoSwiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                research = false;
                page = "1";
                mvpPresenter.getAllAlarm(userID, privilege + "", page);
                mProgressBar.setVisibility(View.GONE);
            }
        });
        if (privilege == 1) {
            typeLin.setVisibility(View.GONE);
        }
        startTime.setOnFocusChangeListener(this);
        endTime.setOnFocusChangeListener(this);
        startTime.setInputType(InputType.TYPE_NULL);
        endTime.setInputType(InputType.TYPE_NULL);
        demoRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(adapter==null){
                    return;
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    if (loadMoreCount >= 20 ) {//@@7.12
                        page = Integer.parseInt(page) + 1 + "";
                        mvpPresenter.getAllAlarm(userID, privilege + "", page);
                        mProgressBar.setVisibility(View.GONE);
                    }else{
                        T.showShort(mContext,"已经没有更多数据了");
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });

    }


    @Override
    protected AlarmDevDetailPresenter createPresenter() {
        mPresenter = new AlarmDevDetailPresenter(this);
        return mPresenter;
    }

    @Override
    public void getDataSuccess(List<AlarmMessageModel> alarmMessageModels) {
        int pageInt = Integer.parseInt(page);
        if (messageModelList != null && messageModelList.size() >= 20 && pageInt > 1) {
            loadMoreCount=alarmMessageModels.size();
            messageModelList.addAll(alarmMessageModels);
            adapter.changeMoreStatus(RefreshRecyclerAdapter.NO_DATA);
        } else {
            messageModelList = new ArrayList<>();
            loadMoreCount=alarmMessageModels.size();
            messageModelList.addAll(alarmMessageModels);
            adapter = new RefreshRecyclerAdapter(this, messageModelList, mPresenter, userID, privilege + "");
            adapter.setOnClickListener(new RefreshRecyclerAdapter.onClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if(view.getId()==R.id.deal_alarm_action_tv){
                        deal_position=position;

                        mPresenter.dealAlarmDetail(userID, messageModelList.get(deal_position).getMac(), privilege+"" ,deal_position,userID,
                                "","",
                                "","");
                    }
                }
            });
            demoRecycler.setAdapter(adapter);
            demoSwiperefreshlayout.setRefreshing(false);
            adapter.changeMoreStatus(RefreshRecyclerAdapter.NO_DATA);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==6){
            if(data!=null)
                mPresenter.dealAlarmDetail(userID, messageModelList.get(deal_position).getMac(), privilege+"" ,deal_position,userID,
                        data.getStringExtra("alarmTruth"),data.getStringExtra("dealDetail"),
                        data.getStringExtra("image_path"),data.getStringExtra("video_path"));//@@5.19添加index位置参数
        }
    }

    @Override
    public void getDataFail(String msg) {
        demoSwiperefreshlayout.setRefreshing(false);
        if(adapter!=null){
            adapter.changeMoreStatus(RefreshRecyclerAdapter.NO_DATA);
        }
        T.showShort(mContext, msg);
    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void dealAlarmMsgSuccess(List<AlarmMessageModel> alarmMessageModels) {
        messageModelList.clear();
        messageModelList.addAll(alarmMessageModels);
        loadMoreCount=alarmMessageModels.size();//@@7.13
        adapter = new RefreshRecyclerAdapter(this, messageModelList, mPresenter, userID, privilege + "");
        demoRecycler.setAdapter(adapter);
        adapter.changeMoreStatus(RefreshRecyclerAdapter.NO_DATA);
    }

    @Override
    public void updateAlarmMsgSuccess(int index) {

        adapter.setList(index);
    }


    @Override
    public void onStop() {
        super.onStop();
        isDpShow = false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.start_time:
                startTime.setTextColor(getResources().getColor(R.color.login_btn));
                startTime.setHintTextColor(getResources().getColor(R.color.hint_color_text));
                endTime.setTextColor(Color.BLACK);
                endTime.setHintTextColor(Color.BLACK);
                break;
            case R.id.end_time:
                startTime.setTextColor(Color.BLACK);
                startTime.setHintTextColor(Color.BLACK);
                endTime.setTextColor(getResources().getColor(R.color.login_btn));
                endTime.setHintTextColor(getResources().getColor(R.color.hint_color_text));
                break;
        }
    }

}
