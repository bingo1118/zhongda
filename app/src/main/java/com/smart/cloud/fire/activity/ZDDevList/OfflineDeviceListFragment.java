package com.smart.cloud.fire.activity.ZDDevList;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.smart.cloud.fire.activity.Functions.model.ApplyTable;
import com.smart.cloud.fire.activity.SecurityDev.SecurityDevActivity;
import com.smart.cloud.fire.adapter.ShopCameraAdapter;
import com.smart.cloud.fire.adapter.ShopSmokeAdapter;
import com.smart.cloud.fire.base.ui.MvpFragment;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.SmokeSummary;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Smoke;
import com.smart.cloud.fire.utils.T;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;

public class OfflineDeviceListFragment extends MvpFragment<DeviceListByTypePresenter> implements DeviceListByTypeView {

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
    private boolean isSearching = false;
    private int page;
    private String userID;
    private int privilege;
    private int loadMoreCount;
    private int preseterTpye=1;
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext=getActivity();
        userID = MyApp.getUserID();
        privilege = MyApp.app.getPrivilege();
        page = 1;
        list = new ArrayList<>();
        refreshListView();
        devType=((DeviceListByTypeActivity)getActivity()).getDevType();
        mvpPresenter.getNeedLossSmoke(userID, privilege + "", "","", "", page+"",devType.getId(),list,false);
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

                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                if(firstVisibleItem>3){
                    return_top_ib.setVisibility(View.VISIBLE);
                }else{
                    return_top_ib.setVisibility(View.GONE);
                }
                int count = shopSmokeAdapter.getItemCount();
                int itemCount = lastVisibleItem+1;
                if (newState == RecyclerView.SCROLL_STATE_IDLE && itemCount == count) {
                    if(loadMoreCount>=20){
                        page = page + 1 ;
                        if(isSearching){
                            mvpPresenter.getNeedLossSmoke(userID, privilege + "",((DeviceListByTypeActivity)getActivity()).getParentId()
                                    ,((DeviceListByTypeActivity)getActivity()).getAreaId()
                                    ,"", page+"",devType.getId(),list,false);
                        }else{
                            mvpPresenter.getNeedLossSmoke(userID, privilege + "","", "", "", page+"",devType.getId(),list,false);
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
        return "OffLineDevFragment";
    }

    @Override
    public void getDataSuccess(List<?> smokeList,boolean search) {
        loadMoreCount = smokeList.size();
        isSearching = search;
        list.clear();
        list.addAll((List<Smoke>)smokeList);
        shopSmokeAdapter = new ShopSmokeAdapter(mContext, list);
        recyclerView.setAdapter(shopSmokeAdapter);
        swipereFreshLayout.setRefreshing(false);
        shopSmokeAdapter.changeMoreStatus(ShopSmokeAdapter.NO_DATA);
    }

    @Override
    public void getDataFail(String msg) {
        swipereFreshLayout.setRefreshing(false);
        if(!isSearching){
            T.showShort(mContext,msg);
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


