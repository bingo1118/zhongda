package com.smart.cloud.fire.activity.ZDDevList;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.smart.cloud.fire.activity.Functions.model.ApplyTable;
import com.smart.cloud.fire.activity.SecurityDev.SecurityDevActivity;
import com.smart.cloud.fire.activity.SecurityDev.SecurityDevPresenter;
import com.smart.cloud.fire.adapter.ShopCameraAdapter;
import com.smart.cloud.fire.adapter.ShopSmokeAdapter;
import com.smart.cloud.fire.base.ui.MvpFragment;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.global.SmokeSummary;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Smoke;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.Security.SecurityFragment;
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
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;

public class AllDeviceFragment extends MvpFragment<DeviceListByTypePresenter> implements DeviceListByTypeView {
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.swipere_fresh_layout)
    SwipeRefreshLayout swipereFreshLayout;
    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.return_top_ib)
    ImageButton return_top_ib;
    private LinearLayoutManager linearLayoutManager;
    private ShopSmokeAdapter shopSmokeAdapter;
    private int lastVisibleItem;
    private Context mContext;
    private List<Smoke> list;
    private int loadMoreCount;
    private boolean isSearching = false;
    private String page;
    private String userID;
    private int privilege;
    public DeviceListByTypePresenter mPresenter;
    private ApplyTable devType;
    int firstVisibleItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_dev, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext=getActivity();
        userID = SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTNAME);
        privilege = MyApp.app.getPrivilege();
        page = "1";
        list = new ArrayList<>();
        refreshListView();
        devType=((DeviceListByTypeActivity)getActivity()).getDevType();
        mvpPresenter.getNeedDev(userID, privilege + "", page,"","","",devType.getId(), list,false);//@@5.15
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
                ((DeviceListByTypeActivity)getActivity()).refreshView();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int count = shopSmokeAdapter.getItemCount();
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                if(firstVisibleItem>3){
                    return_top_ib.setVisibility(View.VISIBLE);
                }else{
                    return_top_ib.setVisibility(View.GONE);
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem+1 == count) {
                    if(loadMoreCount>=20){
                        page = Integer.parseInt(page) + 1 + "";
                        if(isSearching){
                            mvpPresenter.getNeedDev(userID, privilege + "", page
                                    ,((DeviceListByTypeActivity)getActivity()).getParentId()
                                    ,((DeviceListByTypeActivity)getActivity()).getAreaId()
                                    ,"",devType.getId(),list,true);//@@5.15
                        }else{
                            mvpPresenter.getNeedDev(userID, privilege + "", page,"","","",devType.getId(),list,true);
                        }
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

    @OnClick({R.id.return_top_ib})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.return_top_ib:
                returnTop();
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected DeviceListByTypePresenter createPresenter() {
        mPresenter = new DeviceListByTypePresenter(this);
        return mPresenter;
    }

    @Override
    public String getFragmentName() {
        return "SecurityFragment";
    }

    @Override
    public void getDataSuccess(List<?> smokeList,boolean search) {
        isSearching = search;
        loadMoreCount = smokeList.size();
        page="1";
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
        recyclerView.setAdapter(shopSmokeAdapter);
        swipereFreshLayout.setRefreshing(false);
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
        if(!isSearching){
            T.showShort(mContext, msg);
        }
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
    public void getLostCount(String count) {
    }


    @Override
    public void getSmokeSummary(SmokeSummary smokeSummary) {
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void returnTop(){
        recyclerView.scrollToPosition(0);
        return_top_ib.setVisibility(View.GONE);
        T.showShort(mContext,"已返回顶部");
    }

}

