package com.smart.cloud.fire.activity.ZDDevList;

import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.global.SmokeSummary;

import java.util.ArrayList;
import java.util.List;

public interface DeviceListByTypeView {
    void getDataSuccess(List<?> smokeList, boolean research);
    void getSmokeSummary(SmokeSummary smokeSummary);
    void showLoading();
    void hideLoading();
    void getDataFail(String msg);
    void onLoadingMore(List<?> smokeList);
    void getLostCount(String count);
}
