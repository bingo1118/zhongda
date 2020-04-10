package com.smart.cloud.fire.activity.AlarmDevDetail;

import com.smart.cloud.fire.base.presenter.BasePresenter;
import com.smart.cloud.fire.mvp.fragment.CollectFragment.AlarmMessageModel;
import com.smart.cloud.fire.mvp.fragment.MapFragment.HttpError;
import com.smart.cloud.fire.rxjava.ApiCallback;
import com.smart.cloud.fire.rxjava.SubscriberCallBack;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class AlarmDevDetailPresenter extends BasePresenter<AlarmDevDetailView> {
    public AlarmDevDetailPresenter(AlarmDevDetailView view){
        attachView(view);
    }

    //type:1表示获取第一页的报警消息，2表示根据条件查询相应的报警消息
    public void getAllAlarm(String userId, String privilege, String page){
        mvpView.showLoading();
        Observable observable=null;
        observable = apiStores1.getNeedAlarmDev(userId,privilege,page);

        addSubscription(observable,new SubscriberCallBack<>(new ApiCallback<HttpError>() {
            @Override
            public void onSuccess(HttpError model) {
                int errorCode = model.getErrorCode();
                if(errorCode==0){
                    List<AlarmMessageModel> alarmMessageModels = model.getAlarm();
                    mvpView.getDataSuccess(alarmMessageModels);
                }else{
                    List<AlarmMessageModel> alarmMessageModels = new ArrayList<AlarmMessageModel>();//@@5.3
                    mvpView.getDataSuccess(alarmMessageModels);//@@5.3
                    mvpView.getDataFail("无数据");
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

    public void dealAlarm(String userId, String smokeMac, String privilege, final int index){//@@5.19添加取消报警信息的位置
        mvpView.showLoading();
        Observable mObservable = apiStores1.dealAlarm(userId,smokeMac);
        final Observable Observable2 = apiStores1.getAllAlarm(userId,privilege,"1");
        twoSubscription(mObservable, new Func1<HttpError,Observable<HttpError>>() {
            @Override
            public Observable<HttpError> call(HttpError httpError) {
                int errorCode = httpError.getErrorCode();
                if(errorCode==0){
                    return Observable2;
                }else{
                    Observable<HttpError> observable = Observable.just(httpError);
                    return observable;
                }
            }
        },new SubscriberCallBack<>(new ApiCallback<HttpError>() {
            @Override
            public void onSuccess(HttpError model) {
                List<AlarmMessageModel> list = model.getAlarm();
                if(list==null){
//                    mvpView.getDataFail("取消失败");
                }else{
                    int errorCode = model.getErrorCode();
                    if(errorCode==0){
                        List<AlarmMessageModel> alarmMessageModels = model.getAlarm();
//                        mvpView.dealAlarmMsgSuccess(alarmMessageModels);
//                        mvpView.updateAlarmMsgSuccess(alarmMessageModels);//@@5.18
                        mvpView.updateAlarmMsgSuccess(index);//@@5.18
//                        mvpView.getDataFail("取消成功");
                    }else{
//                        mvpView.getDataFail("取消失败");
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



    public void dealAlarmDetail(String userId, String smokeMac, String privilege, final int index
            ,String dealPeople,String alarmTruth,String dealDetail,String image_path,String video_path){//@@5.19添加取消报警信息的位置
        mvpView.showLoading();
        Observable mObservable = apiStores1.dealAlarmDetail(userId,smokeMac,dealPeople,alarmTruth,dealDetail,image_path,video_path);
        final Observable Observable2 = apiStores1.getAllAlarm(userId,privilege,"1");
        twoSubscription(mObservable, new Func1<HttpError,Observable<HttpError>>() {
            @Override
            public Observable<HttpError> call(HttpError httpError) {
                int errorCode = httpError.getErrorCode();
                if(errorCode==0){
                    return Observable2;
                }else{
                    Observable<HttpError> observable = Observable.just(httpError);
                    return observable;
                }
            }
        },new SubscriberCallBack<>(new ApiCallback<HttpError>() {
            @Override
            public void onSuccess(HttpError model) {
                List<AlarmMessageModel> list = model.getAlarm();
                if(list==null){
//                    mvpView.getDataFail("取消失败");
                }else{
                    int errorCode = model.getErrorCode();
                    if(errorCode==0){
                        List<AlarmMessageModel> alarmMessageModels = model.getAlarm();
//                        mvpView.dealAlarmMsgSuccess(alarmMessageModels);
//                        mvpView.updateAlarmMsgSuccess(alarmMessageModels);//@@5.18
                        mvpView.updateAlarmMsgSuccess(index);//@@5.18
//                        mvpView.getDataFail("取消成功");
                    }else{
//                        mvpView.getDataFail("取消失败");
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

