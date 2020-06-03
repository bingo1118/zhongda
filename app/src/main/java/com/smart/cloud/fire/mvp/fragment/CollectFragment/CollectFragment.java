package com.smart.cloud.fire.mvp.fragment.CollectFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smart.cloud.fire.adapter.RefreshRecyclerAdapter;
import com.smart.cloud.fire.base.ui.MvpFragment;
import com.smart.cloud.fire.global.AlarmType;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.Utils;
import com.smart.cloud.fire.utils.WindowUtils;
import com.smart.cloud.fire.view.AreaChooceListView;
import com.smart.cloud.fire.view.TimePickerViewHelper;
import com.smart.cloud.fire.view.ZDAlarmTypeChooseListView;
import com.smart.cloud.fire.view.ZDAreaChooseListView;


import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;

/**
 * Created by Administrator on 2016/9/21.
 */
public class CollectFragment extends MvpFragment<CollectFragmentPresenter> implements CollectFragmentView {

    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.add_fire)
    ImageView addFire;


    @Bind(R.id.demo_recycler)
    RecyclerView demoRecycler;
    @Bind(R.id.demo_swiperefreshlayout)
    SwipeRefreshLayout demoSwiperefreshlayout;
    @Bind(R.id.date_pick)
    LinearLayout date_pick;

    @Bind(R.id.zd_area)
    ZDAreaChooseListView zdarea;
    @Bind(R.id.search_btn)
    Button search_btn;
    @Bind(R.id.start_time_picker)
    TimePickerViewHelper start_picker;
    @Bind(R.id.end_time_picker)
    TimePickerViewHelper end_picker;
    @Bind(R.id.alarm_view)
    ZDAlarmTypeChooseListView zdAlarmTypeChooseListView;
    @Bind(R.id.return_top_ib)
    ImageButton return_top_ib;

    private boolean date_pick_isShow=false;
    private String userID;
    private int privilege;
    private String page;
    private Context mContext;
    private CollectFragmentPresenter collectFragmentPresenter;
    private List<AlarmMessageModel> messageModelList;
    private int loadMoreCount;
    boolean isDpShow = false;
    private boolean wheelScrolled = false;
    private static final int START_TIME = 0;
    private static final int END_TIME = 1;
    private Area mArea;
    AlarmType mAlarmType;
    private LinearLayoutManager linearLayoutManager;
    private RefreshRecyclerAdapter adapter;
    private int lastVisibleItem;

    private boolean isSearching = false;//@@是否是按条件查询

    private String startStr="";
    private String endStr="";
    private String areaId;
    private String placeTypeId;
    private String parentId;
    private String alarmType="";
    String areaStr ;

    List<Area> parent = null;//@@9.11
    Map<String, List<Area>> map = null;//@@9.11
    private int deal_position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collect_fire, container,
                false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        userID = SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTNAME);
        privilege = MyApp.app.getPrivilege();
        page = "1";
        mvpPresenter.getAllAlarm(userID, privilege + "", page, 1, "", "", "", "", "","");
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
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        demoRecycler.setLayoutManager(linearLayoutManager);

        demoSwiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = "1";
                mvpPresenter.getAllAlarm(userID, privilege + "", page, 1, "", "", "", "", "","");
                isSearching = false;
                mProgressBar.setVisibility(View.GONE);
                startStr="";
                endStr="";
                areaId="";
                alarmType="";
            }
        });

        demoRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (adapter == null) {
                    return;
                }
                if(linearLayoutManager.findFirstVisibleItemPosition()>3){
                    return_top_ib.setVisibility(View.VISIBLE);
                }else{
                    return_top_ib.setVisibility(View.GONE);
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
//                    if (loadMoreCount >= 20 && research == false) {
                    if (loadMoreCount >= 20) {//@@7.12
                        page = Integer.parseInt(page) + 1 + "";
                        if (isSearching) {
                            mvpPresenter.getAllAlarm(userID, privilege + "", page, 2, startStr, endStr, areaId, placeTypeId, parentId,alarmType);
                        } else {
                            mvpPresenter.getAllAlarm(userID, privilege + "", page, 1, "", "", "", "", "","");
                        }
                        mProgressBar.setVisibility(View.GONE);
                    } else {
                        T.showShort(mContext, "已经没有更多数据了");
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });

        zdAlarmTypeChooseListView.setEditTextHint("报警类型");
        zdAlarmTypeChooseListView.setOnChildChooceClickListener(new ZDAlarmTypeChooseListView.OnChildChooceClickListener() {
            @Override
            public void OnChildClick(AlarmType info) {
                mAlarmType=info;
            }
        });

        start_picker.setmOnTimeGetListener(new TimePickerViewHelper.OnTimeGetListener() {
            @Override
            public void getDate(String dateString) {
                startStr=dateString;
            }
        });

        end_picker.setmOnTimeGetListener(new TimePickerViewHelper.OnTimeGetListener() {
            @Override
            public void getDate(String dateString) {
                endStr=dateString;
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.isNetworkAvailable(getActivity())) {
                    return;
                }

                if((startStr.length() > 0 && endStr.length() == 0)||(startStr.length() == 0 && endStr.length() > 0)){
                    T.showShort(mContext, "时间区间选择错误");
                    return;
                }
                if (startStr.length() > 0 && endStr.length() > 0) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        long startLong = df.parse(startStr).getTime();
                        long endLong = df.parse(endStr).getTime();
                        if (startLong > endLong) {
                            T.showShort(mContext, "开始时间不能大于结束时间");
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return;
                    }
                }


                if (mArea != null && mArea.getAreaId() != null) {
                    if (mArea.getIsParent() == 1) {
                        parentId = mArea.getAreaId();//@@9.1
                        areaId = "";
                    } else {
                        areaId = mArea.getAreaId();
                        parentId = "";
                    }
                } else {
                    areaId = "";
                }

                if (mAlarmType != null ) {
                    alarmType=mAlarmType.getAlarmCode()+"";
                }

                if(startStr.length()==0&&endStr.length()==0&&areaId.length()==0&&alarmType.length()==0){
                    T.showShort(mContext,"筛选条件不能为空");
                    return;
                }
                page = "1";//@@9.11
                mvpPresenter.getAllAlarm(userID, privilege + "", page, 2, startStr, endStr, areaId, placeTypeId, parentId,alarmType);
                isSearching=false;
                showdatepick();
            }
        });

        zdarea.setOnChildAreaChooceClickListener(new AreaChooceListView.OnChildAreaChooceClickListener() {
            @Override
            public void OnChildClick(Area info) {
                mArea=info;
            }
        });
    }



    @OnClick({R.id.add_fire,R.id.return_top_ib})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_fire:
                showdatepick();
                break;
            case R.id.return_top_ib:
                demoRecycler.scrollToPosition(0);
                return_top_ib.setVisibility(View.GONE);
                T.showShort(mContext,"已返回至顶部");
                break;
            default:
                break;
        }
    }

    @Override
    protected CollectFragmentPresenter createPresenter() {
        collectFragmentPresenter = new CollectFragmentPresenter(CollectFragment.this);
        return collectFragmentPresenter;
    }

    @Override
    public String getFragmentName() {
        return "CollectFragment";
    }

    @Override
    public void getDataSuccess(List<AlarmMessageModel> alarmMessageModels) {
        int pageInt = Integer.parseInt(page);
        if (messageModelList != null && messageModelList.size() >= 20 && pageInt > 1) {
            loadMoreCount = alarmMessageModels.size();
            messageModelList.addAll(alarmMessageModels);
            adapter.changeMoreStatus(RefreshRecyclerAdapter.NO_DATA);
        } else {
            messageModelList = new ArrayList<>();
            loadMoreCount = alarmMessageModels.size();
            messageModelList.addAll(alarmMessageModels);
            adapter = new RefreshRecyclerAdapter(getActivity(), messageModelList, collectFragmentPresenter, userID, privilege + "");
            adapter.setOnClickListener(new RefreshRecyclerAdapter.onClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (view.getId() == R.id.deal_alarm_action_tv) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("提示:");
                        builder.setMessage("确认处理该设备报警？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deal_position = position;
                                collectFragmentPresenter.dealAlarmDetail(userID, messageModelList.get(deal_position).getMac(), privilege + "", deal_position, userID,
                                        "", "",
                                        "", "");
                            }
                        });
                        builder.show();
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
        if (requestCode == 6) {
            if (data != null)
                collectFragmentPresenter.dealAlarmDetail(userID, messageModelList.get(deal_position).getMac(), privilege + "", deal_position, userID,
                        data.getStringExtra("alarmTruth"), data.getStringExtra("dealDetail"),
                        data.getStringExtra("image_path"), data.getStringExtra("video_path"));//@@5.19添加index位置参数
        }
    }

    @Override
    public void getDataFail(String msg) {
        demoSwiperefreshlayout.setRefreshing(false);
        if (adapter != null) {
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
        loadMoreCount = alarmMessageModels.size();//@@7.13
        adapter = new RefreshRecyclerAdapter(getActivity(), messageModelList, collectFragmentPresenter, userID, privilege + "");
        demoRecycler.setAdapter(adapter);
        adapter.changeMoreStatus(RefreshRecyclerAdapter.NO_DATA);
    }

    @Override
    public void updateAlarmMsgSuccess(int index) {
        adapter.setList(index);
        T.showShort(mContext,"处理成功");
    }

    @Override
    public void getShopType(ArrayList<Object> shopTypes) {

    }

    @Override
    public void getShopTypeFail(String msg) {
        T.showShort(mContext, msg);
    }

    @Override
    public void getAreaType(ArrayList<Object> shopTypes) {

    }

    @Override
    public void getAreaTypeFail(String msg) {
        T.showShort(mContext, msg);
    }

    @Override
    public void getDataByCondition(List<AlarmMessageModel> alarmMessageModels) {
        if (!isSearching) {
            isSearching = true;
            messageModelList.clear();
        }//@@7.13
        int pageInt = Integer.parseInt(page);
        if (messageModelList != null && messageModelList.size() >= 20 && pageInt > 1) {
            loadMoreCount = alarmMessageModels.size();
            messageModelList.addAll(alarmMessageModels);
            adapter.changeMoreStatus(RefreshRecyclerAdapter.NO_DATA);
        } else {
            loadMoreCount = alarmMessageModels.size();
            messageModelList.addAll(alarmMessageModels);
            adapter = new RefreshRecyclerAdapter(getActivity(), messageModelList, collectFragmentPresenter, userID, privilege + "");//@@9.11
            demoRecycler.setAdapter(adapter);
        }//@@7.13 添加条件查询分页

    }

    @Override
    public void getChoiceArea(Area area) {
        mArea = area;
    }

    @Override
    public void getChoiceShop(ShopType shopType) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        isDpShow = false;
    }

    private void showdatepick(){
        if(date_pick_isShow){
            date_pick.setVisibility(View.GONE);
            date_pick_isShow=false;
        }else{
            date_pick.setVisibility(View.VISIBLE);
            date_pick_isShow=true;

        }
    }

}
