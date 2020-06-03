package com.smart.cloud.fire.activity.AlarmDeviceByType;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.smart.cloud.fire.activity.AllSmoke.AllSmokeActivity;
import com.smart.cloud.fire.activity.AllSmoke.AllSmokePresenter;
import com.smart.cloud.fire.activity.AllSmoke.AllSmokeView;
import com.smart.cloud.fire.adapter.ShopSmokeAdapter;
import com.smart.cloud.fire.base.ui.MvpActivity;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.global.SmokeSummary;
import com.smart.cloud.fire.mvp.ChuangAn.ChuangAnActivity;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Smoke;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.AllDevFragment.AllDevFragment;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.ShopInfoFragmentView;
import com.smart.cloud.fire.utils.BingoDialog;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import fire.cloud.smart.com.smartcloudfire.R;

public class AlarmDeviceByTypeActivity extends MvpActivity<AllSmokePresenter> implements AllSmokeView {

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.sum_tv)
    TextView sum_tv;
    @Bind(R.id.title_tv)
    TextView title_tv;
    @Bind(R.id.swipere_fresh_layout)
    SwipeRefreshLayout swipereFreshLayout;
    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;
    private LinearLayoutManager linearLayoutManager;
    private ShopSmokeAdapter shopSmokeAdapter;
    private int lastVisibleItem;
    private Context mContext;
    private List<Smoke> list;
    private int loadMoreCount;
    private boolean research = false;
    private String page;
    private String userID;
    private int privilege;
    private AllSmokePresenter mShopInfoFragmentPresenter;

    String type;
    String sum;
    String mac;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_device_by_type);
        ButterKnife.bind(this);

        mContext=this;
        userID = SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTNAME);
        privilege = MyApp.app.getPrivilege();
        page = "1";
        list = new ArrayList<>();
        sum=getIntent().getStringExtra("sum");
        mac=getIntent().getStringExtra("mac");
        name=getIntent().getStringExtra("name");
        type=getIntent().getIntExtra("type",1)+"";
        switch (type){
            case "1":
                title_tv.setText("报警设备数");
                break;
            case "2":
                title_tv.setText("失联设备数");
                break;
            case "3":
                title_tv.setText("故障设备数");
                break;
            case "4":
                title_tv.setText("低电设备数");
                break;
            case "5":
                title_tv.setText("正常设备数");
                break;
            case "6":
                title_tv.setText("全部设备数");
                break;
        }
        sum_tv.setText(sum);
        refreshListView();
        mvpPresenter.getAlarmDeviceByType(userID, privilege + "", page,type, true);
    }

    private void refreshListView() {
        //设置刷新时动画的颜色，可以设置4个
        swipereFreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        swipereFreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        swipereFreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        linearLayoutManager=new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        //下拉刷新。。
        swipereFreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mvpPresenter.getAlarmDeviceByType(userID, privilege + "", page,type, true);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//            if (research) {
//                if(shopSmokeAdapter!=null){
//                    shopSmokeAdapter.changeMoreStatus(ShopCameraAdapter.NO_DATA);
//                }
//                return;
//            }
                if(research){//@@9.5 条件查询分页
                    if(shopSmokeAdapter==null){
                        return;
                    }
                    int count = shopSmokeAdapter.getItemCount();
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem+1 == count) {
                        if(loadMoreCount>=20){
                            page = Integer.parseInt(page) + 1 + "";
                            mvpPresenter.getAlarmDeviceByType(userID, privilege + "", page,type, false);
                        }else{
                            T.showShort(mContext,"已经没有更多数据了");
                        }
                    }
                    return;
                }//@@9.5
                if(shopSmokeAdapter==null){
                    return;
                }
                int count = shopSmokeAdapter.getItemCount();
//                int itemCount = lastVisibleItem+2;
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem+1 == count) {
                    if(loadMoreCount>=20){
                        page = Integer.parseInt(page) + 1 + "";
                        mvpPresenter.getAlarmDeviceByType(userID, privilege + "", page,type, false);
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

    public void refreshView() {
        page = "1";
        list.clear();
        mvpPresenter.getAlarmDeviceByType(userID, privilege + "", page,type, true);    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected AllSmokePresenter createPresenter() {
        mShopInfoFragmentPresenter = new AllSmokePresenter(this);
        return mShopInfoFragmentPresenter;
    }


    @Override
    public void getDataSuccess(List<?> smokeList,int sum,boolean search) {
        research = search;
        sum_tv.setText(sum+"");
        if(search!=false&&!page.equals("1")){
            page="1";
        }//@@9.5
        loadMoreCount = smokeList.size();
        list.clear();
        list.addAll((List<Smoke>)smokeList);
        shopSmokeAdapter = new ShopSmokeAdapter(mContext, list);
        shopSmokeAdapter.setOnLongClickListener(new ShopSmokeAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(View view, int position) {
                Smoke smoke =list.get(position);
                if(smoke.getDeviceType()==22||smoke.getDeviceType()==23||smoke.getDeviceType()==58||smoke.getDeviceType()==61
                        ||smoke.getDeviceType()==73||smoke.getDeviceType()==75||smoke.getDeviceType()==77
                        ||smoke.getDeviceType()==78||smoke.getDeviceType()==79||smoke.getDeviceType()==80
                        ||smoke.getDeviceType()==83||smoke.getDeviceType()==85||smoke.getDeviceType()==86
                        ||smoke.getDeviceType()==87||smoke.getDeviceType()==89||smoke.getDeviceType()==90
                        ||smoke.getDeviceType()==91||smoke.getDeviceType()==92||smoke.getDeviceType()==93
                        ||smoke.getDeviceType()==94||smoke.getDeviceType()==95||smoke.getDeviceType()==96
                        ||smoke.getDeviceType()==97||smoke.getDeviceType()==98||smoke.getDeviceType()==99
                        ||smoke.getDeviceType()==100||smoke.getDeviceType()==101||smoke.getDeviceType()==102
                        ||smoke.getDeviceType()==103||smoke.getDeviceType()==104||smoke.getDeviceType()==105){
                    showNormalDialog(smoke.getMac(),smoke.getDeviceType(),position);
                }else{
                    T.showShort(mContext,"该设备无法删除");
                }
            }
        });
        shopSmokeAdapter.setOnClickListener(new ShopSmokeAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position) {
                Smoke smoke=list.get(position);
                switch (smoke.getDeviceType()){
                    case 51://@@创安燃气
                        Intent intent = new Intent(mContext, ChuangAnActivity.class);
                        intent.putExtra("Mac",smoke.getMac());
                        intent.putExtra("Position",smoke.getName());
                        mContext.startActivity(intent);
                        break;
                }
            }
        });
        recyclerView.setAdapter(shopSmokeAdapter);
        swipereFreshLayout.setRefreshing(false);
//        shopSmokeAdapter.changeMoreStatus(ShopSmokeAdapter.NO_DATA);
    }

    private void showNormalDialog(final String mac, final int deviceType, final int position){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        normalDialog.setTitle("提示");
        normalDialog.setMessage("确认删除该设备?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userid= MyApp.getUserID();
                        String url="";
                        switch (deviceType){
                            case 58:
                                url= ConstantValues.SERVER_IP_NEW+"deleteOneNetDevice?imei="+mac+"&userid="+userid;
                                break;
                            default:
                                url= ConstantValues.SERVER_IP_NEW+"deleteDeviceById?imei="+mac+"&userid="+userid;
                                break;
                        }
                        VolleyHelper.getInstance(mContext).getStringResponse(url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject jsonObject=new JSONObject(response);
                                            int errorCode=jsonObject.getInt("errorCode");
                                            if(errorCode==0){
                                                list.remove(position);
                                                shopSmokeAdapter.notifyDataSetChanged();
                                                T.showShort(mContext,"删除成功");
                                            }else{
                                                T.showShort(mContext,"删除失败");
                                            }
                                            T.showShort(mContext,jsonObject.getString("error"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            T.showShort(mContext,"删除失败");
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("TAG", error.getMessage(), error);
                                        T.showShort(mContext,"删除失败");
                                    }
                                });
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        BingoDialog dialog=new BingoDialog(normalDialog);
        dialog.show();
    }

    @Override
    public void getDataFail(String msg) {
        T.showShort(mContext, msg);
        swipereFreshLayout.setRefreshing(false);
        if(shopSmokeAdapter!=null){
            shopSmokeAdapter.changeMoreStatus(ShopSmokeAdapter.NO_DATA);
        }
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
    public void onLoadingMore(List<?> smokeList) {
        loadMoreCount = smokeList.size();
        list.addAll((List<Smoke>)smokeList);
        shopSmokeAdapter.changeMoreStatus(ShopSmokeAdapter.LOADING_MORE);
    }

    @Override
    public void refreshFragment() {

    }

    @Override
    public void getAreaType(ArrayList<?> shopTypes, int type) {
    }

    @Override
    public void getAreaTypeFail(String msg, int type) {
    }

    @Override
    public void unSubscribe(String type) {
    }

    @Override
    public void getChoiceArea(Area area) {

    }

    @Override
    public void getChoiceShop(ShopType shopType) {

    }

    @Override
    public void getSmokeSummary(SmokeSummary smokeSummary) {
//        totalNum.setText(smokeSummary.getAllSmokeNumber()+"");
//        onlineNum.setText(smokeSummary.getOnlineSmokeNumber()+"");
//        offlineNum.setText(smokeSummary.getLossSmokeNumber()+"");
    }


}
