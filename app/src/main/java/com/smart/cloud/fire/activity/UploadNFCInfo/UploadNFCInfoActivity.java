package com.smart.cloud.fire.activity.UploadNFCInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.smart.cloud.fire.base.ui.MvpActivity;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.rqcode.Capture2Activity;
import com.smart.cloud.fire.utils.FileUtil;
import com.smart.cloud.fire.utils.FormFile;
import com.smart.cloud.fire.utils.NFCHelper;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.Utils;
import com.smart.cloud.fire.utils.VolleyHelper;
import com.smart.cloud.fire.view.TakePhotoView;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISCameraConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import fire.cloud.smart.com.smartcloudfire.R;


public class UploadNFCInfoActivity extends MvpActivity<UploadNFCInfoPresenter> implements UploadNFCInfoView {

    public static final int REQUEST_CODE_SCAN_REPEATER=8;

    @Bind(R.id.uid_name)
    EditText uid_name;//@@uid
    @Bind(R.id.add_fire_name)
    EditText addFireName;//设备名称。。
    @Bind(R.id.add_fire_address)
    EditText addFireAddress;//设备地址。。
    @Bind(R.id.add_fire_dev_btn)
    RelativeLayout addFireDevBtn;//添加设备按钮。。
    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;//加载进度。。
    @Bind(R.id.area_name)
    EditText area_name;//@@区域
    @Bind(R.id.device_type_name)
    EditText device_type_name;//@@类型
    @Bind(R.id.memo_name)
    EditText memo_name;//@@备注
    @Bind(R.id.photo_image)
    TakePhotoView photo_image;//@@拍照上传
    @Bind(R.id.scan_er_wei_ma)
    ImageView scan_er_wei_ma;
    private Context mContext;
    private int privilege;
    private String userID;


    private String deviceState="1";
    String lon="";
    String lat="";

    private NFCHelper nfcHelper;

    Dialog alertDialog;

    private UploadNFCInfoPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_nfcinfo);

        ButterKnife.bind(this);
        mContext = this;
        userID = SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTNAME);
        privilege = MyApp.app.getPrivilege();

        initView();
    }

    @Override
    protected UploadNFCInfoPresenter createPresenter() {
        mPresenter=new UploadNFCInfoPresenter(this);
        return mPresenter;
    }

    private void initView() {
        addFireDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uid_name.getText()==null||uid_name.getText().toString().equals("")){
                    toast("请先录入设备标签信息");
                    return;
                }

                File f = new File(photo_image.getPath());
                mPresenter.uploadNFCInfo(userID, uid_name.getText().toString(), lon, lat, deviceState, URLEncoder.encode(memo_name.getText().toString()),f);
            }
        });

        photo_image.setmIvClickListener(new TakePhotoView.IvClickListener() {
            @Override
            public void onClick() {
                ISNav.getInstance().init(photo_image);
                ISCameraConfig config = new ISCameraConfig.Builder()
                        .needCrop(false) // 裁剪
                        .build();
                ISNav.getInstance().toCameraActivity(mContext, config, 666);
            }
        });

        scan_er_wei_ma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseDialog();
            }
        });

        RadioGroup group = (RadioGroup)this.findViewById(R.id.radio_group);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                           @Override
                         public void onCheckedChanged(RadioGroup arg0, int arg1) {
                                // TODO Auto-generated method stub
                                 int radioButtonId = arg0.getCheckedRadioButtonId();
                               switch (radioButtonId){
                                   case R.id.radio1:
                                       deviceState="1";
                                       break;
                                   case R.id.radio2:
                                       deviceState="2";
                                       break;
                               }
                             }
                    });
    }

    private void clearView() {
        uid_name.setText("");
        addFireName.setText("");
        addFireAddress.setText("");
        area_name.setText("");
        device_type_name.setText("");
        memo_name.setText("");
        photo_image.clear();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // NDEF exchange mode
        if (!nfcHelper.ismWriteMode() && (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                ||NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))) {//@@10.19
            byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String UID = Utils.ByteArrayToHexString(myNFCID);
            uid_name.setText(UID);
            getNormalDevInfo(UID);

            if(alertDialog!=null){
                alertDialog.dismiss();
            }
        }
        // Tag writing mode
        if (nfcHelper.ismWriteMode() && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfcHelper.writeTag(detectedTag);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 666:
                if ( resultCode == RESULT_OK && data != null) {
                    String path = data.getStringExtra("result"); // 图片地址
                    ISNav.getInstance().displayImage(this,path,null);
                }
                break;
            case  REQUEST_CODE_SCAN_REPEATER:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString("result");
                    uid_name.setText(scanResult);
                    getNormalDevInfo(scanResult);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void toast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    private void getNormalDevInfo(String uid) {
        if(uid==null){
            return;
        }
        VolleyHelper helper=VolleyHelper.getInstance(mContext);
        RequestQueue mQueue = helper.getRequestQueue();
        String url="";

        url= ConstantValues.SERVER_IP_NEW+"getItemInfo?uid="+uid+"&userId="+userID;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(uid!=null){
                                uid_name.setText(uid);
                                addFireName.setText(response.getString("deviceName"));
                                addFireAddress.setText(response.getString("address"));
                                area_name.setText(response.getString("areaName"));
                                device_type_name.setText(response.getString("deviceTypeName"));
                                lat=response.getString("latitude");
                                lon=response.getString("longitude");
                            }else{
                                T.showShort(mContext,"无数据");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                T.showShort(mContext,"网络错误");
            }
        });
        mQueue.add(jsonObjectRequest);
    }

    @Override
    public void T(String t) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearView();
    }

    @Override
    public void dealResult(String t, int resultCode) {
        T.showShort(mContext,t);
        if(resultCode==0){
            clearView();
        }
    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void showChooseDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this);
        normalDialog.setTitle("选取获取ID方式");
        normalDialog.setPositiveButton("二维码",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent scanRepeater = new Intent(mContext, Capture2Activity.class);
                        scanRepeater.putExtra("isNeedResult",true);
                        startActivityForResult(scanRepeater, REQUEST_CODE_SCAN_REPEATER);
                    }
                });
        normalDialog.setNegativeButton("NFC",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nfcHelper=NFCHelper.getInstance((Activity) mContext);
                        if (!nfcHelper.isSupportNFC()) {
                            toast("设备不支持NFC功能");
                            return;
                        }
                        nfcHelper.changeToReadMode();

                        TextView textView=new TextView(mContext);//@@10.19
                        textView.setText("接触标签进行读取操作");
                        textView.setTextSize(18);
                        textView.setGravity(Gravity.CENTER);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setView(textView);
                        builder.setCancelable(true);
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                nfcHelper.changeToReadMode();
                            }
                        });
                        alertDialog=builder.create();
                        alertDialog.show();
                    }
                });
        // 显示
        normalDialog.show();
    }
}
