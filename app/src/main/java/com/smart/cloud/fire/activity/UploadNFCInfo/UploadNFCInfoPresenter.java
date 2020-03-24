package com.smart.cloud.fire.activity.UploadNFCInfo;

import com.smart.cloud.fire.base.presenter.BasePresenter;
import com.smart.cloud.fire.mvp.fragment.MapFragment.HttpError;
import com.smart.cloud.fire.rxjava.ApiCallback;
import com.smart.cloud.fire.rxjava.SubscriberCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import rx.Observable;

public class UploadNFCInfoPresenter extends BasePresenter<UploadNFCInfoView> {

    public UploadNFCInfoPresenter(UploadNFCInfoView view ) {
        attachView(view);
    }

    public void uploadNFCInfo(String userId, String uid, String longitude, String latitude, String devicestate, String memo, File file){


        List<MultipartBody.Part> parts = new ArrayList<>();
        parts.add(toRequestBodyOfText("userId", userId));
        parts.add(toRequestBodyOfText("uid", uid));
        parts.add(toRequestBodyOfText("longitude", longitude));
        parts.add(toRequestBodyOfText("latitude", latitude));
        parts.add(toRequestBodyOfText("devicestate", devicestate));
        parts.add(toRequestBodyOfText("memo", memo));

        if(file.exists()){
            parts.add(toRequestBodyOfImage("imagefile",file));//图片
        }

        Observable mObservable=apiStores1.addNFCRecordWithImage(parts);
        addSubscription(mObservable,new SubscriberCallBack<>(new ApiCallback<HttpError>() {

            @Override
            public void onSuccess(HttpError model) {
                int result=model.getErrorCode();
                mvpView.dealResult(model.getError(),result);
            }

            @Override
            public void onFailure(int code, String msg) {
                mvpView.T("失败");
            }

            @Override
            public void onCompleted() {
                if(file.exists()){
                    file.delete();
                }
            }
        }));
    }
}
