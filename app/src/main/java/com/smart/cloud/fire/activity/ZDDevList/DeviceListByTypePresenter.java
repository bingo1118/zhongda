package com.smart.cloud.fire.activity.ZDDevList;

import com.smart.cloud.fire.base.presenter.BasePresenter;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.global.SmokeSummary;
import com.smart.cloud.fire.mvp.fragment.MapFragment.HttpAreaResult;
import com.smart.cloud.fire.mvp.fragment.MapFragment.HttpError;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Smoke;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.ShopInfoFragmentView;
import com.smart.cloud.fire.rxjava.ApiCallback;
import com.smart.cloud.fire.rxjava.SubscriberCallBack;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class DeviceListByTypePresenter extends BasePresenter<DeviceListByTypeView> {


    public DeviceListByTypePresenter(DeviceListByTypeView view) {
        attachView(view);
    }


    //@@5.13安防界面查询设备
    public void getNeedDev(String userId, String privilege,String page, String parentId, String areaId, String placeTypeId, String devType, final List<Smoke> list, boolean isResearch) {
        mvpView.showLoading();
        Observable mObservable = apiStores1.getNeedDev2(userId, privilege, parentId, areaId, page, placeTypeId, devType);
        addSubscription(mObservable, new SubscriberCallBack<>(new ApiCallback<HttpError>() {
            @Override
            public void onSuccess(HttpError model) {
                if (model != null) {
                    int errorCode = model.getErrorCode();
                    if (errorCode == 0) {
                        List<Smoke> smokeList = model.getSmoke();
                        if (list == null || list.size() == 0) {
                            mvpView.getDataSuccess(smokeList, isResearch);
                        } else if (list != null && list.size() >= 20) {
                            mvpView.onLoadingMore(smokeList);
                        }
                    } else {
                        List<Smoke> mSmokeList = new ArrayList<>();
                        if(list != null && list.size() != 0){
                            mvpView.onLoadingMore(mSmokeList);
                            mvpView.getDataFail("无更多数据");
                        }else{
                            mvpView.getDataSuccess(mSmokeList, isResearch);
                            mvpView.getDataFail("无数据");
                        }

                    }
                } else {
                    mvpView.getDataFail("无数据");
                    List<Smoke> smokes = new ArrayList<Smoke>();//@@4.27
                    mvpView.getDataSuccess(smokes, isResearch);//@@4.27
                }
            }

            @Override
            public void onFailure(int code, String msg) {
                mvpView.getDataFail("网络错误");
            }

            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }
        }));
    }

    public void getSmokeSummary(String userId, String privilege, String parentId, String areaId, String placeTypeId, String devType) {
        Observable mObservable = apiStores1.getDevSummary(userId, privilege, parentId, areaId, placeTypeId, devType);
        addSubscription(mObservable, new SubscriberCallBack<>(new ApiCallback<SmokeSummary>() {
            @Override
            public void onSuccess(SmokeSummary model) {
                int resultCode = model.getErrorCode();
                if (resultCode == 0) {
                    mvpView.getSmokeSummary(model);
                }
            }

            @Override
            public void onFailure(int code, String msg) {
            }

            @Override
            public void onCompleted() {
            }
        }));
    }

    public void getNeedLossSmoke(String userId, String privilege, String parentId, String areaId, String placeTypeId, final String page, String devType, final List<Smoke> list, boolean isReserch) {
        mvpView.showLoading();
        Observable mObservable = apiStores1.getNeedLossDev(userId, privilege, parentId, areaId, page, placeTypeId, devType);
        addSubscription(mObservable, new SubscriberCallBack<>(new ApiCallback<HttpError>() {
            @Override
            public void onSuccess(HttpError model) {
                int result = model.getErrorCode();
                if (result == 0) {
                    List<Smoke> smokeList = model.getSmoke();
                    if (list == null || list.size() == 0) {
                        mvpView.getDataSuccess(smokeList, isReserch);
                    } else if (list != null && list.size() >= 20) {
                        mvpView.onLoadingMore(smokeList);
                    }
                } else {
                    List<Smoke> mSmokeList = new ArrayList<>();
                    if(list != null && list.size() != 0){
                        mvpView.onLoadingMore(mSmokeList);
                        mvpView.getDataFail("无更多数据");
                    }else{
                        mvpView.getDataSuccess(mSmokeList, isReserch);
                        mvpView.getDataFail("无数据");
                    }
                }

            }

            @Override
            public void onFailure(int code, String msg) {
                mvpView.getDataFail("网络错误");
            }

            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }
        }));
    }

}
