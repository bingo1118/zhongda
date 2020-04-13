package com.smart.cloud.fire.mvp.fragment.ConfireFireFragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.jakewharton.rxbinding.view.RxView;
import com.smart.cloud.fire.GetLocationActivity;
import com.smart.cloud.fire.utils.FileUtil;
import com.smart.cloud.fire.utils.FormFile;
import com.smart.cloud.fire.base.ui.MvpFragment;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Camera;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Smoke;
import com.smart.cloud.fire.rqcode.Capture2Activity;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.Utils;
import com.smart.cloud.fire.view.TakePhotoView;
import com.smart.cloud.fire.view.XCDropDownListView;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.config.ISCameraConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;
import rx.functions.Action1;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2016/9/21.
 */
public class ConfireFireFragment extends MvpFragment<ConfireFireFragmentPresenter> implements ConfireFireFragmentView {

    public static final int REQUEST_CODE_LOCATION=1;
    public static final int REQUEST_CODE_SCAN_REPEATER=8;
    public static final int REQUEST_CODE_SCAN_DEV=9;
    public static final int REQUEST_CODE_CAMERA=102;

    @Bind(R.id.add_repeater_mac)
    EditText addRepeaterMac;//集中器。。
    @Bind(R.id.add_fire_mac)
    EditText addFireMac;//探测器。。
    @Bind(R.id.add_fire_name)
    EditText addFireName;//设备名称。。
    @Bind(R.id.add_fire_lat)
    EditText addFireLat;//经度。。
    @Bind(R.id.add_fire_lon)
    EditText addFireLon;//纬度。。
    @Bind(R.id.add_fire_address)
    EditText addFireAddress;//设备地址。。
    @Bind(R.id.add_fire_man)
    EditText addFireMan;//负责人姓名。。
    @Bind(R.id.add_fire_man_phone)
    EditText addFireManPhone;//负责人电话。。
    @Bind(R.id.add_fire_man_two)
    EditText addFireManTwo;//负责人2.。
    @Bind(R.id.add_fire_man_phone_two)
    EditText addFireManPhoneTwo;//负责人电话2.。
    @Bind(R.id.scan_repeater_ma)
    ImageView scanRepeaterMa;
    @Bind(R.id.scan_er_wei_ma)
    ImageView scanErWeiMa;
    @Bind(R.id.location_image)
    ImageView locationImage;
    @Bind(R.id.add_fire_zjq)
    XCDropDownListView addFireZjq;//选择区域。。
    @Bind(R.id.add_fire_type)
    XCDropDownListView addFireType;//选择类型。。
    @Bind(R.id.add_fire_dev_btn)
    RelativeLayout addFireDevBtn;//添加设备按钮。。
    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;//加载进度。。
    @Bind(R.id.add_camera_name)
    EditText addCameraName;
    @Bind(R.id.add_camera_relative)
    RelativeLayout addCameraRelative;
    @Bind(R.id.device_type_name)
    TextView device_type_name;
    @Bind(R.id.photo_image)
    TakePhotoView photo_image;//@@拍照上传
    @Bind(R.id.tip_line)
    LinearLayout tip_line;
    @Bind(R.id.clean_all)
    TextView clean_all;
    @Bind(R.id.yc_mac)
    TextView yc_mac;

    private Context mContext;
    private int privilege;
    private String userID;
    private ShopType mShopType;
    private Area mArea;
    private String areaId = "";
    private String shopTypeId = "";
    private String camera = "";

    String mac="";
    String devType="0";

    Fragment mmm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_fire, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        mmm=this;
        userID = SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTNAME);
        privilege = MyApp.app.getPrivilege();
        init();
    }

    private void init() {
        Intent intent=getActivity().getIntent();
        String mac=intent.getStringExtra("mac");
        devType=intent.getStringExtra("devType");
        if(devType==null){
            devType="";
        }
        addFireMac.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(devType.equals("221")&&!hasFocus){
                    String temp=addFireMac.getText().toString();
                    if(temp.length()!=12){
                        T.showShort(mContext,"用传设备MAC长度必须为12位数字");
                        return;
                    }
                    if(!Utils.isNumeric(temp)){
                        T.showShort(mContext,"用传设备MAC长度必须为12位数字");
                        return;
                    }
                    yc_mac.setText("您输入的设备码:"+temp);
                    yc_mac.setVisibility(View.VISIBLE);

                    temp=changeYongChuanMac(temp);
                    addFireMac.setText("A"+temp.toUpperCase());
                    T.showShort(mContext,"用传设备MAC转换成功");
                }
                if (!hasFocus&&addFireMac.getText().toString().length()>0) {
                    mvpPresenter.getOneSmoke(userID, privilege + "", addFireMac.getText().toString());//@@5.5如果添加过该烟感则显示出原来的信息
                }
            }
        });//@@10.18
        addCameraRelative.setVisibility(View.VISIBLE);
        addFireZjq.setEditTextHint("区域");
        addFireType.setEditTextHint("类型");
        RxView.clicks(addFireDevBtn).throttleFirst(2, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                addFire();
            }
        });

        if (mac!=null){
            addFireMac.setText(mac);
            device_type_name.setVisibility(View.VISIBLE);
            device_type_name.setText("设备类型:"+devType);
            mvpPresenter.getOneSmoke(userID, privilege + "", mac);
        }
        photo_image.setmIvClickListener(new TakePhotoView.IvClickListener() {
            @Override
            public void onClick() {
                ISNav.getInstance().init(photo_image);
                ISCameraConfig config = new ISCameraConfig.Builder()
                        .needCrop(false) // 裁剪
                        .build();
                ISNav.getInstance().toCameraActivity(mmm, config, 666);
            }
        });

        clean_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanAllView();
            }
        });
    }

    private String changeYongChuanMac(String temp) {
        String s="";
        String[] ss=new String[6];//等长切分
        for(int i=0;i<temp.length();i+=2){
            ss[i/2]=temp.substring(i,i+2);
        }
        for (String each:ss) {
            String t=Integer.toHexString(Integer.parseInt(each));
            s=(t.length()==2?t:"0"+t)+s;
        }
        return s;
    }

    /**
     * 添加设备，提交设备信息。。
     */
    private void addFire() {
        if (mShopType != null) {
            shopTypeId = mShopType.getPlaceTypeId();
        }
        if (mArea != null) {
            areaId = mArea.getAreaId();
        }
        final String longitude = addFireLon.getText().toString().trim();
        final String latitude = addFireLat.getText().toString().trim();
        final String smokeName = addFireName.getText().toString().trim();
        final String smokeMac = addFireMac.getText().toString().trim();
        final String address = addFireAddress.getText().toString().trim();
        final String placeAddress = "";
        final String principal1 = addFireMan.getText().toString().trim();
        final String principal2 = addFireManTwo.getText().toString().trim();
        final String principal1Phone = addFireManPhone.getText().toString().trim();
        final String principal2Phone = addFireManPhoneTwo.getText().toString().trim();
        final String repeater = addRepeaterMac.getText().toString().trim();
        camera = addCameraName.getText().toString().trim();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File f=photo_image.getPath()==null?null:new File(photo_image.getPath());
                mvpPresenter.addSmoke(userID, privilege + "", smokeName, smokeMac, address, longitude,
                        latitude, placeAddress, shopTypeId, principal1, principal1Phone, principal2,
                        principal2Phone, areaId, repeater, camera , f,devType);
            }
        }).start();

    }

    @Override
    protected ConfireFireFragmentPresenter createPresenter() {
        ConfireFireFragmentPresenter mConfireFireFragmentPresenter = new ConfireFireFragmentPresenter(ConfireFireFragment.this);
        return mConfireFireFragmentPresenter;
    }

    @Override
    public String getFragmentName() {
        return "ConfireFireFragment";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (addFireZjq.ifShow()) {
            addFireZjq.closePopWindow();
        }
        if (addFireType.ifShow()) {
            addFireType.closePopWindow();
        }
        cleanAllView();
        ButterKnife.unbind(this);

    }

    @Override
    public void onDestroy() {
        mvpPresenter.stopLocation();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        mvpPresenter.initLocation();
        super.onStart();
    }

    @OnClick({R.id.scan_repeater_ma, R.id.scan_er_wei_ma, R.id.location_image, R.id.add_fire_zjq, R.id.add_fire_type})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_repeater_ma:
                Intent scanRepeater = new Intent(mContext, Capture2Activity.class);
                scanRepeater.putExtra("isNeedResult",true);
                startActivityForResult(scanRepeater, REQUEST_CODE_SCAN_REPEATER);
                break;
            case R.id.scan_er_wei_ma:
                Intent openCameraIntent = new Intent(mContext, Capture2Activity.class);
                openCameraIntent.putExtra("isNeedResult",true);
                startActivityForResult(openCameraIntent, REQUEST_CODE_SCAN_DEV);
                break;
            case R.id.location_image:
                mvpPresenter.startLocation();
                Intent intent=new Intent(mContext, GetLocationActivity.class);
                startActivityForResult(intent,REQUEST_CODE_LOCATION);//@@6.20
                break;
            case R.id.add_fire_zjq:
                if (addFireZjq.ifShow()) {
                    addFireZjq.closePopWindow();
                } else {
                    mvpPresenter.getPlaceTypeId(userID, privilege + "", 2);
                    addFireZjq.setClickable(false);
                    addFireZjq.showLoading();
                }
                break;
            case R.id.add_fire_type:
                if (addFireType.ifShow()) {
                    addFireType.closePopWindow();
                } else {
                    mvpPresenter.getPlaceTypeId(userID, privilege + "", 1);
                    addFireType.setClickable(false);
                    addFireType.showLoading();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void getLocationData(BDLocation location) {
        addFireLon.setText(location.getLongitude() + "");
        addFireAddress.setText(location.getAddrStr());
        addFireLat.setText(location.getLatitude() + "");
    }

    @Override
    public void showLoading() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void hideLoading() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void getDataFail(String msg) {
        T.showShort(mContext, msg);
    }

    @Override
    public void getDataSuccess(Smoke smoke) {
        tip_line.setVisibility(View.VISIBLE);
        addFireLon.setText(smoke.getLongitude() + "");
        addFireLat.setText(smoke.getLatitude() + "");
        addFireAddress.setText(smoke.getAddress());
        addFireName.setText(smoke.getName());
        addFireMan.setText(smoke.getPrincipal1());
        addFireManPhone.setText(smoke.getPrincipal1Phone());
        addFireManTwo.setText(smoke.getPrincipal2());
        addFireManPhoneTwo.setText(smoke.getPrincipal2Phone());
        addFireZjq.setEditTextData(smoke.getAreaName());
        addFireType.setEditTextData(smoke.getPlaceType());//@@10.18
        areaId=smoke.getAreaId()+"";
        shopTypeId=smoke.getPlaceTypeId();//@@10.18
        Camera mCamera = smoke.getCamera();
        if (mCamera != null) {
            addCameraName.setText(mCamera.getCameraId());
        }
        addRepeaterMac.setText(smoke.getRepeater().trim());
    }

    @Override
    public void getShopType(ArrayList<Object> shopTypes) {
        addFireType.setItemsData(shopTypes,mvpPresenter);
        addFireType.showPopWindow();
        addFireType.setClickable(true);
        addFireType.closeLoading();
    }

    @Override
    public void getShopTypeFail(String msg) {
        T.showShort(mContext, msg);
        addFireType.setClickable(true);
        addFireType.closeLoading();
    }

    @Override
    public void getAreaType(ArrayList<Object> shopTypes) {
        addFireZjq.setItemsData(shopTypes,mvpPresenter);
        addFireZjq.showPopWindow();
        addFireZjq.setClickable(true);
        addFireZjq.closeLoading();
    }

    @Override
    public void getAreaTypeFail(String msg) {
        T.showShort(mContext, msg);
        addFireZjq.setClickable(true);
        addFireZjq.closeLoading();
    }

    @Override
    public void addSmokeResult(String msg, int errorCode) {
        if (errorCode == 0) {
            cleanAllView();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    T.showShort(mContext,msg);
                }
            });
        }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    T.showShort(mContext,msg);
                }
            });
        }

        tip_line.setVisibility(View.GONE);
    }

    private void cleanAllView() {
        mShopType = null;
        mArea = null;
        clearText();
        areaId = "";
        shopTypeId = "";
        camera = "";
        addFireMac.setText("");
        addFireZjq.addFinish();
        addFireType.addFinish();
        tip_line.setVisibility(View.GONE);
        yc_mac.setVisibility(View.GONE);
    }

    @Override
    public void getChoiceArea(Area area) {
        mArea = area;
    }

    @Override
    public void getChoiceShop(ShopType shopType) {
        mShopType = shopType;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 666:
                if ( resultCode == RESULT_OK && data != null) {
                    String path = data.getStringExtra("result"); // 图片地址
                    ISNav.getInstance().displayImage(mContext,path,null);
                }
                break;
            case REQUEST_CODE_SCAN_REPEATER:
                if (resultCode == getActivity().RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString("result");
                    addRepeaterMac.setText(scanResult);

                }
                break;
            case REQUEST_CODE_SCAN_DEV:
                if (resultCode == getActivity().RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString("result");

                    addFireMac.setText(scanResult);
                    clearText();
                    mvpPresenter.getOneSmoke(userID, privilege + "", scanResult);//@@5.5如果添加过该烟感则显示出原来的信息

                }
                break;
            case REQUEST_CODE_LOCATION://@@6.20
                if (resultCode == getActivity().RESULT_OK) {
                    Bundle bundle=data.getBundleExtra("data");
                    try{
                        addFireLat.setText(String.format("%.8f",bundle.getDouble("lat")));
                        addFireLon.setText(String.format("%.8f",bundle.getDouble("lon")));
                        addFireAddress.setText(bundle.getString("address"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }

    }

    /**
     * 清空其他编辑框内容。。
     */
    private void clearText() {
        addFireLon.setText("");
        addFireLat.setText("");
        addFireAddress.setText("");
        addFireName.setText("");
        addFireMan.setText("");
        addFireManPhone.setText("");
        addFireManTwo.setText("");
        addFireManPhoneTwo.setText("");
        addFireZjq.setEditTextData("");
        addFireType.setEditTextData("");
        addCameraName.setText("");
        photo_image.clear();
    }


}
