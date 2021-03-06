package com.smart.cloud.fire.mvp.fragment.MapFragment;

import com.smart.cloud.fire.activity.NFCDev.NFCRecordBean;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ShopType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/21.
 */
public interface MapFragmentView {
    void getDataSuccess(List<Smoke> smokeList);

    void getNFCSuccess(List<NFCRecordBean> smokeList);

    void getDataFail(String msg);

    void showLoading();

    void hideLoading();

    void showSmokeDialog(Smoke smoke);

    void showNFCDialog(NFCRecordBean smoke);//@@8.18

    void showAlarmDialog(Smoke smoke);

}
