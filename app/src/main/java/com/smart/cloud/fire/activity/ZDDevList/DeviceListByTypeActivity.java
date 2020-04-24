package com.smart.cloud.fire.activity.ZDDevList;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.smart.cloud.fire.activity.Functions.model.ApplyTable;
import com.smart.cloud.fire.activity.Map.MapActivity;
import com.smart.cloud.fire.activity.SecurityDev.OfflineSecurityDevFragment;
import com.smart.cloud.fire.activity.SecurityDev.SecurityDevActivity;
import com.smart.cloud.fire.activity.SecurityDev.SecurityDevPresenter;
import com.smart.cloud.fire.activity.SecurityDev.SecurityDevView;
import com.smart.cloud.fire.base.ui.MvpActivity;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.global.SmokeSummary;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.Security.SecurityFragment;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.Utils;
import com.smart.cloud.fire.utils.VolleyHelper;
import com.smart.cloud.fire.view.AreaChooceListView;
import com.smart.cloud.fire.view.ZDAreaChooseListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;

public class DeviceListByTypeActivity extends MvpActivity<DeviceListByTypePresenter> implements DeviceListByTypeView {


    Context mContext;
    private DeviceListByTypePresenter mPresenter;
    private String userID;
    private int privilege;

    private AllDeviceFragment allDevFragment;
    private OfflineDeviceListFragment offLineDevFragment;
    private FragmentManager fragmentManager;
    public static final int FRAGMENT_ALL = 1;
    public static final int FRAGMENT_OFFLINE =2;
    private int position;
    private boolean visibility = false;
    private ShopType mShopType;
    private Area mArea;

    private String areaId = "";
    private String parentId="";//@@9.1
    private String shopTypeId = "";
    private ApplyTable devType;


    @Bind(R.id.area_condition)
    ZDAreaChooseListView areaCondition;
    @Bind(R.id.online_num)
    TextView onlineNum;
    @Bind(R.id.offline_num)
    TextView offlineNum;
    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.turn_map_btn)
    TextView turn_map_btn;
    @Bind(R.id.title_name_text)
    TextView title_name_tv;
    @Bind(R.id.title_lose_dev_text)
    TextView title_lose_dev_tv;
    @Bind(R.id.title_name)
    LinearLayout title_name_rela;
    @Bind(R.id.title_lose_dev)
    LinearLayout title_lose_dev_rela;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list_by_type);
        ButterKnife.bind(this);

        //透明状态栏          
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // 透明导航栏          
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        mContext=this;
        devType=(ApplyTable) getIntent().getSerializableExtra("ApplyTable");
        init();
        title_name_rela.setEnabled(false);
        title_name_rela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title_lose_dev_rela.setEnabled(true);
                title_name_rela.setEnabled(false);
                showFragment(FRAGMENT_ALL);
                position=FRAGMENT_ALL;//@@在线设备
            }
        });
        title_lose_dev_rela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title_name_rela.setEnabled(true);
                title_lose_dev_rela.setEnabled(false);
                showFragment(FRAGMENT_OFFLINE);
                position=FRAGMENT_OFFLINE;//@@离线设备
            }
        });
        title_name_tv.setText(devType.getName());
        title_lose_dev_tv.setText("失联设备");
        mvpPresenter.getSmokeSummary(userID,privilege+"",parentId,areaId,shopTypeId,devType.getId());

        areaCondition.setOnChildAreaChooceClickListener(new AreaChooceListView.OnChildAreaChooceClickListener() {
            @Override
            public void OnChildClick(Area info) {
                mArea=info;
                if (mArea != null && mArea.getAreaId() != null) {
                    if(mArea.getIsParent()==1){
                        parentId= mArea.getAreaId();//@@9.1
                        areaId="";
                    }else{
                        areaId = mArea.getAreaId();
                        parentId="";
                    }
                } else {
                    areaId = "";
                    parentId="";
                }
                mvpPresenter.getSmokeSummary(userID,privilege+"",parentId,areaId,shopTypeId,devType.getId());
                allDevFragment.mPresenter.getNeedDev(userID, privilege + "", "1",parentId,areaId,shopTypeId,devType.getId(),null,true);//@@5.15
                offLineDevFragment.mPresenter.getNeedLossSmoke(userID, privilege + "",parentId, areaId, shopTypeId, "",devType.getId(),null,true);

                mArea = null;
            }
        });
    }

    @OnClick({ R.id.turn_map_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.turn_map_btn:
                Intent intent=new Intent(DeviceListByTypeActivity.this, MapActivity.class);
                intent.putExtra("devType",devType.getId());
                startActivity(intent);
                break;
            default:
                break;
        }
    }


    @Override
    protected DeviceListByTypePresenter createPresenter() {
       mPresenter=new DeviceListByTypePresenter(this);
        return mPresenter;
    }

    private void init() {
        fragmentManager = getFragmentManager();
        userID = MyApp.getUserID();
        privilege = MyApp.app.getPrivilege();
        showFragment(FRAGMENT_ALL);
    }

    public void showFragment(int index) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        hideFragment(ft);
        //注意这里设置位置
        position = index;
        if (areaCondition.ifShow()) {
            areaCondition.closePopWindow();
        }//@@5.5关闭下拉选项
        switch (index) {
            case FRAGMENT_ALL:
                if (allDevFragment == null) {
                    offLineDevFragment = new OfflineDeviceListFragment();
                    ft.add(R.id.fragment_content, offLineDevFragment);
                    allDevFragment = new AllDeviceFragment();
                    ft.add(R.id.fragment_content, allDevFragment);
                } else {
                    ft.show(allDevFragment);
                }
                break;
            case FRAGMENT_OFFLINE:
                if (offLineDevFragment == null) {
                    offLineDevFragment = new OfflineDeviceListFragment();
                    ft.add(R.id.fragment_content, offLineDevFragment);
                } else {
                    ft.show(offLineDevFragment);
                }
                break;
        }
        ft.commit();
    }

    public void hideFragment(FragmentTransaction ft) {
        //如果不为空，就先隐藏起来
        if (allDevFragment != null) {
            ft.hide(allDevFragment);
        }
        if (offLineDevFragment != null) {
            ft.hide(offLineDevFragment);
        }
    }

    @Override
    public void getDataSuccess(List<?> smokeList, boolean research) {
    }

    @Override
    public void getSmokeSummary(SmokeSummary smokeSummary) {
        onlineNum.setText("总数:"+smokeSummary.getAllSmokeNumber()+"");
        offlineNum.setText("失联:"+smokeSummary.getLossSmokeNumber()+"");
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
    public void getDataFail(String msg) {
        T.show(mContext,msg, Toast.LENGTH_SHORT);//@@4.27
    }

    @Override
    public void onLoadingMore(List<?> smokeList) {
    }

    @Override
    public void getLostCount(String count) {

    }

    public void refreshView() {
        parentId="";
        areaId="";
        shopTypeId="";
        areaCondition.clearView();
        allDevFragment.mPresenter.getNeedDev(userID, privilege + "", "1",parentId,areaId,shopTypeId,devType.getId(),null,false);//@@5.15;
        offLineDevFragment.mPresenter.getNeedLossSmoke(userID, privilege + "",parentId,areaId,shopTypeId, "1",devType.getId(),null,false);
        mvpPresenter.getSmokeSummary(userID,privilege+"",parentId,areaId,shopTypeId,devType.getId());
    }

    public ApplyTable getDevType() {
        return devType;
    }

    public String getParentId() {
        return parentId;
    }

    public String getAreaId() {
        return areaId;
    }


}


