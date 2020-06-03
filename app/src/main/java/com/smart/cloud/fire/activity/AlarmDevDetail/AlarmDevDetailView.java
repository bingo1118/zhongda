package com.smart.cloud.fire.activity.AlarmDevDetail;

import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.mvp.fragment.CollectFragment.AlarmMessageModel;

import java.util.ArrayList;
import java.util.List;

public interface AlarmDevDetailView {

    void getDataSuccess(List<AlarmMessageModel> alarmMessageModels);

    void getDataFail(String msg);

    void showLoading();

    void hideLoading();

    void dealAlarmMsgSuccess(List<AlarmMessageModel> alarmMessageModels);

    void updateAlarmMsgSuccess(int index);//@@5.18

}
