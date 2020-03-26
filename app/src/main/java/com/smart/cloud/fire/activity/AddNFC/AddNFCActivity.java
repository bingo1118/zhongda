package com.smart.cloud.fire.activity.AddNFC;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.google.gson.JsonObject;
import com.jakewharton.rxbinding.view.RxView;
import com.smart.cloud.fire.GetLocationActivity;
import com.smart.cloud.fire.base.ui.MvpActivity;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Smoke;
import com.smart.cloud.fire.rqcode.Capture2Activity;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.Utils;
import com.smart.cloud.fire.view.XCDropDownListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;
import rx.functions.Action1;

public class AddNFCActivity extends MvpActivity<AddNFCPresenter> implements AddNFCView{

    public static final int REQUEST_CODE_SCAN_REPEATER=8;

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
    @Bind(R.id.makeTime_edit)
    TextView makeTime_text;//生产时间@@11.16
    @Bind(R.id.makeAddress_edit)
    EditText makeAddress_edit;//生产地址@@11.28
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
    @Bind(R.id.makeTime_rela)
    RelativeLayout makeTime_rela;//生产日期
    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;//加载进度。。
    @Bind(R.id.add_camera_name)
    EditText addCameraName;
    @Bind(R.id.producer_edit)
    EditText producer_edit;
    @Bind(R.id.workerPhone_edit)
    EditText workerPhone_edit;
    @Bind(R.id.info_line)
    LinearLayout info_line;//@@11.16
    private Context mContext;
    private int privilege;
    private String userID;
    private ShopType mShopType;
    private Area mArea;
    private NFCDeviceType nfcDeviceType;//@@8.16
    private String areaId = "";
    private String shopTypeId = "";

    private boolean mWriteMode = false;
    NfcAdapter mNfcAdapter;
    AlertDialog alertDialog;

    private NFCInfo nfcInfo;



    private static final int DATE_DIALOG_ID = 1;
    private static final int SHOW_DATAPICK = 0;
    private int mYear;
    private int mMonth;
    private int mDay;

    String getDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_nfc);

        ButterKnife.bind(this);
        mContext = this;
        userID = SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTNAME);
        privilege = MyApp.app.getPrivilege();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        init();
    }

    private void init() {
        addFireZjq.setEditTextHint("区域");
        addFireType.setEditTextHint("类型");
        makeTime_text.setOnClickListener(new DateButtonOnClickListener());//@@11.16
        RxView.clicks(addFireDevBtn).throttleFirst(2, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                addFire();
            }
        });
        nfcInfo=new NFCInfo();


        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        setDateTime();
    }


    /**
     * 添加设备，提交设备厂家信息。。
     */
    private void addFire2() {
        if (nfcDeviceType != null) {
            shopTypeId = nfcDeviceType.getPlaceTypeId();//@@8.16
        }
        if (mArea != null) {
            areaId = mArea.getAreaId();
        }

        String smokeMac = addFireMac.getText().toString().trim();
        String producer=producer_edit.getText().toString().trim();
        String makeTime=makeTime_text.getText().toString().trim();

        if(smokeMac.length()==0){
            toast("请填写探测器MAC");
            return;
        }
        if(producer.length()==0||producer.length()==0){
            toast("请填写厂家");
            return;
        }
        if(makeTime.length()==0||makeTime.length()==0){
            toast("请填写生产日期");
            return;
        }
        nfcInfo=new NFCInfo(smokeMac,"","","","","","","","",producer,makeTime,"","");
    }


    /**
     * 添加设备，提交设备信息。。
     */
    private void addFire() {
        if (nfcDeviceType != null) {
            shopTypeId = nfcDeviceType.getPlaceTypeId();//@@8.16
        }
        if (mArea != null) {
            areaId = mArea.getAreaId();
        }
        String longitude = addFireLon.getText().toString().trim();
        String latitude = addFireLat.getText().toString().trim();
        String smokeName = addFireName.getText().toString().trim();
        String smokeMac = addFireMac.getText().toString().trim();
        String address = addFireAddress.getText().toString().trim();

        String producer=producer_edit.getText().toString().trim();
        String makeTime=makeTime_text.getText().toString().trim();
        String makeAddress=makeAddress_edit.getText().toString().trim();

        String workerPhone=workerPhone_edit.getText().toString().trim();

        if(longitude.length()==0||latitude.length()==0){
            toast("请获取经纬度");
            return;
        }
        if(smokeName.length()==0||smokeName.length()==0){
            toast("请填写名称");
            return;
        }
        if(smokeMac.length()==0){
            toast("请填写探测器MAC");
            return;
        }
        if(areaId==null||areaId.length()==0){
            toast("请填选择区域");
            return;
        }
        if(shopTypeId==null||shopTypeId.length()==0){
            toast("请填选择类型");
            return;
        }
        nfcInfo=new NFCInfo(smokeMac,longitude,latitude,areaId,mArea.getAreaName(),shopTypeId,nfcDeviceType.getPlaceTypeName(),smokeName,address,producer,makeTime,makeAddress,workerPhone);

        mvpPresenter.addNFC(userID, privilege + "", nfcInfo.getDeviceName(), nfcInfo.getUid(), nfcInfo.getAddress(),
                nfcInfo.getLon(), nfcInfo.getLat(), nfcInfo.getDeviceTypeId(),nfcInfo.getAreaId(),nfcInfo.getProducer(),
                nfcInfo.getMakeTime(),nfcInfo.getWorkerPhone(),nfcInfo.getMakeAddress());
    }

    @Override
    protected AddNFCPresenter createPresenter() {
        AddNFCPresenter addNFCPresenter = new AddNFCPresenter(this);
        return addNFCPresenter;
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

    @OnClick({ R.id.scan_er_wei_ma, R.id.location_image, R.id.add_fire_zjq, R.id.add_fire_type})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_er_wei_ma:

                Intent scanRepeater = new Intent(mContext, Capture2Activity.class);
                scanRepeater.putExtra("isNeedResult",true);
                scanRepeater.putExtra("func",1);
                startActivityForResult(scanRepeater, REQUEST_CODE_SCAN_REPEATER);

                break;
            case R.id.location_image:
                Intent intent=new Intent(mContext, GetLocationActivity.class);
                startActivityForResult(intent,1);//@@6.20
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
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void getDataFail(String msg) {
        T.showShort(mContext, msg);
    }

    @Override
    public void getDataSuccess(Smoke smoke) {

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
    public void getNFCDeviceType(ArrayList<Object> deviceTypes) {
        addFireType.setItemsData(deviceTypes,mvpPresenter);
        addFireType.showPopWindow();
        addFireType.setClickable(true);
        addFireType.closeLoading();
    }

    @Override
    public void getNFCDeviceTypeFail(String msg) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1://@@6.20
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle=data.getBundleExtra("data");
                    addFireLat.setText(String.format("%.8f",bundle.getDouble("lat")));
                    addFireLon.setText(String.format("%.8f",bundle.getDouble("lon")));
                    addFireAddress.setText(bundle.getString("address"));
                }
                break;
            case REQUEST_CODE_SCAN_REPEATER:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString("result");
                    addFireMac.setText(scanResult);
                }
                break;
        }

    }



    @Override
    public void addSmokeResult(String msg, int errorCode) {
        T.showShort(mContext, msg);
        if (errorCode == 0) {
            mShopType = null;
            mArea = null;
            clearText();
            shopTypeId = "";
            addFireMac.setText("");
            addFireZjq.addFinish();
            addFireType.addFinish();
        }
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
    public void getChoiceNFCDeviceType(NFCDeviceType nfcDeviceType) {
        this.nfcDeviceType=nfcDeviceType;
    }




    /**
     * 清空其他编辑框内容。。
     */
    private void clearText() {
        producer_edit.setText("");//@@11.16
        makeTime_text.setText("");//@@11.16
        addFireLon.setText("");
        addFireLat.setText("");
        addFireAddress.setText("");
        addFireName.setText("");
        addFireType.setEditTextData("");
        addFireZjq.setEditTextData("");//@@10.19
        addCameraName.setText("");
        makeAddress_edit.setText("");//@@11.28
    }



    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }



    NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                        record
                });
                msgs = new NdefMessage[] {
                        msg
                };
            }
        } else {
            finish();
        }
        return msgs;
    }

    boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    toast("Tag is read-only.");
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.");
                    return false;
                }

                toast("写入数据成功.");

                mWriteMode=false;//@@10.19
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        toast("Formatted tag and wrote message");
                        return true;
                    } catch (IOException e) {
                        toast("Failed to format tag.");
                        return false;
                    }
                } else {
                    toast("Tag doesn't support NDEF.");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            toast("写入数据失败");
        }finally {
            if(alertDialog!=null){
                alertDialog.dismiss();
            }
        }

        return false;
    }






    private void setDateTime() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
//        updateDisplay(c);
    }

    private class DateButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Message msg = new Message();
            msg.what = SHOW_DATAPICK;
            saleHandler.sendMessage(msg);
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        }
        return null;
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }
    /**

     * 处理日期控件的Handler

     */

    Handler saleHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_DATAPICK:
                    showDialog(DATE_DIALOG_ID);
                    break;
            }
        }
    };
    /**
     * 日期控件的事件
     */

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay(null);
        }
    };
    /**
     * 更新日期
     */
    private void updateDisplay(Calendar c) {
        getDate=new StringBuilder().append(mYear).append(
                (mMonth + 1) < 10 ? "-0" + (mMonth + 1) : "-"+(mMonth + 1)).append(
                (mDay < 10) ? "-0" + mDay : "-"+mDay).toString();
        makeTime_text.setText(getDate);
    }
}
