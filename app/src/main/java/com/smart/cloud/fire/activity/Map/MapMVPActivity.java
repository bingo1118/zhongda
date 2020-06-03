package com.smart.cloud.fire.activity.Map;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.overlayutil.MyOverlayManager;
import com.smart.cloud.fire.activity.NFCDev.NFCRecordBean;
import com.smart.cloud.fire.base.ui.MvpActivity;
import com.smart.cloud.fire.base.ui.MvpFragment;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.mvp.fragment.MapFragment.MapFragment;
import com.smart.cloud.fire.mvp.fragment.MapFragment.MapFragmentPresenter;
import com.smart.cloud.fire.mvp.fragment.MapFragment.MapFragmentView;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Smoke;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.view.AreaChooceListView;
import com.smart.cloud.fire.view.ShowAlarmDialog;
import com.smart.cloud.fire.view.ShowSmokeDialog;
import com.smart.cloud.fire.view.ZDAreaChooseListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;

public class MapMVPActivity extends MvpActivity<MapFragmentPresenter> implements MapFragmentView {

    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.bmapView)
    MapView mMapView;
    @Bind(R.id.search_fire_btn)
    Button search_fire_btn;
    @Bind(R.id.add_fire)
    ImageView add_fire;
    @Bind(R.id.area_condition)
    ZDAreaChooseListView areaCondition;
    @Bind(R.id.area_search)
    EditText area_search;//@@
    @Bind(R.id.lin_search)
    LinearLayout lin_search;//@@
    private BaiduMap mBaiduMap;
    private Context mContext;
    private String userID;
    private int privilege;
    private Area mArea;
    private String parentId="";
    private String areaId = "";
    private MapFragmentPresenter mMapFragmentPresenter;
    private String devType;//@@7.21

    List<Area> parent = null;//@@9.12

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_mvp);

        ButterKnife.bind(this);
        mBaiduMap = mMapView.getMap();// 获得MapView

        mContext = this;
        userID = MyApp.getUserID();
        privilege = MyApp.app.getPrivilege();
        devType=getIntent().getStringExtra("devType");//@@7.21
        if(devType.equals("7")){
            areaCondition.setIfHavaChooseAll(false);
        }//@@11.06
//        if (privilege == 1) {
//            add_fire.setVisibility(View.GONE);//权限为1时没有搜索功能。。
//            areaCondition.setVisibility(View.GONE);//@@9.29
//            mvpPresenter.getAllSmoke(userID, privilege + "");//@@9.29
//        } else {
//            add_fire.setVisibility(View.VISIBLE);
//            add_fire.setImageResource(R.drawable.search);
//        }
//        areaCondition.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                areaCondition.showPopWindow();
//            }
//        });
        areaCondition.setOnChildAreaChooceClickListener(new AreaChooceListView.OnChildAreaChooceClickListener() {
            @Override
            public void OnChildClick(Area info) {
                if (info != null && info.getAreaId() != null) {
                    if(info.getIsParent()==1){
                        parentId= info.getAreaId();//@@9.1
                        areaId="";
                    }else{
                        areaId = info.getAreaId();
                        parentId="";
                    }
                } else {
                    areaId = "";
                    parentId="";
                }
                initLastMap();
            }
        });
        initLastMap();
    }



    @Override
    protected MapFragmentPresenter createPresenter() {
        mMapFragmentPresenter = new MapFragmentPresenter(this);
        return mMapFragmentPresenter;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        mMapView.setVisibility(View.VISIBLE);
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMapView.setVisibility(View.INVISIBLE);
        mMapView.onPause();
        super.onPause();
    }

    private MyOverlayManager mMyOverlayManager;
    @Override
    public void getDataSuccess(List<Smoke> smokeList) {
        mBaiduMap.clear();
        List<BitmapDescriptor> viewList =  initMark();
        if(mMyOverlayManager==null){
            mMyOverlayManager = new MyOverlayManager();
        }
        mMyOverlayManager.init(mContext,mBaiduMap,smokeList, mMapFragmentPresenter,viewList);
        mMyOverlayManager.removeFromMap();
        mBaiduMap.setOnMarkerClickListener(mMyOverlayManager);
        mMyOverlayManager.addToMap();
        mMyOverlayManager.zoomToSpan();
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMyOverlayManager.zoomToSpan();
            }
        });

    }

    @Override
    public void getNFCSuccess(List<NFCRecordBean> smokeList) {
        mBaiduMap.clear();
        List<BitmapDescriptor> viewList =  initMark();
        if(mMyOverlayManager==null){
            mMyOverlayManager = new MyOverlayManager();
        }
        mMyOverlayManager.initNFC(mBaiduMap,smokeList, mMapFragmentPresenter,viewList);
        mMyOverlayManager.removeFromMap();
        mBaiduMap.setOnMarkerClickListener(mMyOverlayManager);
        mMyOverlayManager.addToMap();
        mMyOverlayManager.zoomToSpan();
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMyOverlayManager.zoomToSpan();
            }
        });
    }

    /**
     * 初始化各种设备的标记图标。。
     * @return
     */
    private List<BitmapDescriptor> initMark(){
        View viewA = LayoutInflater.from(mContext).inflate(
                R.layout.image_mark, null);
        View viewB = LayoutInflater.from(mContext).inflate(
                R.layout.image_mark_alarm, null);
        View viewRQ = LayoutInflater.from(mContext).inflate(
                R.layout.image_rq_mark, null);
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.image_test, null);
        View view2 = LayoutInflater.from(mContext).inflate(
                R.layout.image_test2, null);
        View viewDq = LayoutInflater.from(mContext).inflate(
                R.layout.image_test_dq, null);
        View viewSG = LayoutInflater.from(mContext).inflate(
                R.layout.image_sg_mark, null);
        View viewSB = LayoutInflater.from(mContext).inflate(
                R.layout.image_sb_mark, null);
        View viewSY = LayoutInflater.from(mContext).inflate(
                R.layout.image_sy_mark, null);//@@水压5.4
        View viewSY_BJ = LayoutInflater.from(mContext).inflate(
                R.layout.image_sy_bj_mark, null);//@@水压报警5.4
        View viewSJSB = LayoutInflater.from(mContext).inflate(
                R.layout.image_sjsb_mark, null);//@@三江设备5.4
        View viewMC = LayoutInflater.from(mContext).inflate(
                R.layout.image_mc_mark, null);//@@门磁8.10
        View viewHW = LayoutInflater.from(mContext).inflate(
                R.layout.image_hw_mark, null);//@@红外8.10
        View viewHJTCQ = LayoutInflater.from(mContext).inflate(
                R.layout.image_hjtcq_mark, null);//@@环境探测器8.10
        View viewZJ = LayoutInflater.from(mContext).inflate(
                R.layout.image_zj_mark, null);//@@有线主机8.10
        View viewSJ = LayoutInflater.from(mContext).inflate(
                R.layout.image_sj_mark, null);//@@水禁8.10
        View viewPL = LayoutInflater.from(mContext).inflate(
                R.layout.image_pl_mark, null);//@@喷淋
        BitmapDescriptor bdA = BitmapDescriptorFactory
                .fromView(viewA);
        BitmapDescriptor bdDq = BitmapDescriptorFactory
                .fromView(viewDq);
        BitmapDescriptor bdC = BitmapDescriptorFactory
                .fromView(viewB);
        BitmapDescriptor bdRQ = BitmapDescriptorFactory
                .fromView(viewRQ);
        BitmapDescriptor bdSG = BitmapDescriptorFactory
                .fromView(viewSG);
        BitmapDescriptor bdSB = BitmapDescriptorFactory
                .fromView(viewSB);
        BitmapDescriptor cameraImage = BitmapDescriptorFactory
                .fromView(view);
        BitmapDescriptor cameraImage2 = BitmapDescriptorFactory
                .fromView(view2);
        BitmapDescriptor syImage = BitmapDescriptorFactory
                .fromView(viewSY);//@@5.4
        BitmapDescriptor sy_bj_Image = BitmapDescriptorFactory
                .fromView(viewSY_BJ);//@@5.4
        BitmapDescriptor sjsbImage = BitmapDescriptorFactory
                .fromView(viewSJSB);//@@5.4
        BitmapDescriptor mcImage = BitmapDescriptorFactory
                .fromView(viewMC);//@@8.10
        BitmapDescriptor hwImage = BitmapDescriptorFactory
                .fromView(viewHW);//@@8.10
        BitmapDescriptor hjtcqImage = BitmapDescriptorFactory
                .fromView(viewHJTCQ);//@@8.10
        BitmapDescriptor zjImage = BitmapDescriptorFactory
                .fromView(viewZJ);//@@8.10
        BitmapDescriptor sjImage = BitmapDescriptorFactory
                .fromView(viewSJ);//@@8.10
        BitmapDescriptor plImage = BitmapDescriptorFactory
                .fromView(viewPL);//@@11.02
        List<BitmapDescriptor> listView = new ArrayList<>();
        listView.add(bdA);
        listView.add(bdC);
        listView.add(bdRQ);
        listView.add(cameraImage);
        listView.add(cameraImage2);
        listView.add(bdDq);
        listView.add(bdSG);
        listView.add(bdSB);
        listView.add(syImage);
        listView.add(sy_bj_Image);
        listView.add(sjsbImage);
        listView.add(mcImage);
        listView.add(hwImage);
        listView.add(hjtcqImage);
        listView.add(zjImage);
        listView.add(sjImage);
        listView.add(plImage);
        return listView;
    }

    @Override
    public void getDataFail(String msg) {
        T.showShort(mContext, msg);
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
    public void showSmokeDialog(Smoke smoke) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.user_smoke_address_mark, null,false);
        new ShowSmokeDialog(mActivity,view,smoke);
    }

    @Override
    public void showNFCDialog(NFCRecordBean smoke) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.user_smoke_address_mark, null,false);
        new ShowSmokeDialog(mActivity,view,smoke);
    }

    @Override
    public void showAlarmDialog(Smoke smoke) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.user_do_alarm_msg_dialog, null);
        new ShowAlarmDialog(mActivity,view,smoke,mMapFragmentPresenter,userID);
    }

    private boolean visibility = false;

    @OnClick({R.id.search_fire_btn, R.id.add_fire,R.id.text})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_fire_btn://@@4.27
                String search=area_search.getText().toString();//@@4.27
                area_search.setText("");
                if(search.length()!=0&&search!=null){
                    lin_search.setVisibility(View.GONE);//@@4.27
                    mvpPresenter.getSearchSmoke(userID, privilege + "",search );//@@4.27获取查询内容获取设备。。
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm.isActive()){
                        imm.hideSoftInputFromWindow(search_fire_btn.getWindowToken(),0);//隐藏输入软键盘@@4.28
                    }
                }else{
                    T.show(mContext,"查询内容不能为空", Toast.LENGTH_SHORT);
                }
                break;
            case R.id.add_fire:
                if (visibility) {
                    visibility = false;
                    lin_search.setVisibility(View.GONE);
                    if (areaCondition.ifShow()) {
                        areaCondition.closePopWindow();
                    }
                } else {
                    visibility = true;
                    areaCondition.clearView();
                    lin_search.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    private void initLastMap(){
//        String area_name=SharedPreferencesManager.getInstance().getData(mContext,
//                "LASTAREANAME",
//                devType);//@@11.13
//        String area_id=SharedPreferencesManager.getInstance().getData(mContext,
//                "LASTAREAID",
//                devType);//@@11.13
//        int isParent=SharedPreferencesManager.getInstance().getIntData(mContext,
//                "LASTAREAISPARENT",
//                devType);//@@11.13
//        if(area_name.length()>0){
        if(devType.equals("7")){
            mvpPresenter.getNeedNFC(userID, privilege + "", areaId, "","");//@@8.18
        }else{
            mvpPresenter.getNeedSmoke(userID, privilege + "", parentId,areaId, "",devType);//获取按照要求获取设备。。
        }
//        }
    }

}