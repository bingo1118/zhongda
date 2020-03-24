package com.smart.cloud.fire.activity.UploadNFCInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.VolleyHelper;
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
    ImageView photo_image;//@@拍照上传
    @Bind(R.id.scan_er_wei_ma)
    ImageView scan_er_wei_ma;
    private Context mContext;
    private int privilege;
    private String userID;
    private String areaId;//@@9.27
    private String uploadTime;

    private boolean mWriteMode = false;

    private String deviceState="1";
    String lon="";
    String lat="";
    private String imageFilePath;
    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/filename.jpg");//@@9.30

    Handler handler = new Handler() {//@@9.29
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case 1:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    toast("图片上传失败");
                    break;
            }
            super.handleMessage(msg);
        }
    };


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
        if(f.exists()){
            f.delete();
        }
        initView();

        ISNav.getInstance().init(new ImageLoader() {
            @Override
            public void displayImage(Context context, String path, ImageView imageView) {
                Glide.with(context).load(path).into(photo_image);
            }
        });

        ISCameraConfig config = new ISCameraConfig.Builder()
//                .needCrop(true) // 裁剪
//                .cropSize(1, 1, 200, 200)
                .build();

        ISNav.getInstance().toCameraActivity(this, config, 666);
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

                File f = new File(imageFilePath);
                mPresenter.uploadNFCInfo(userID, uid_name.getText().toString(), lon, lat, deviceState, URLEncoder.encode(memo_name.getText().toString()),f);
            }
        });

        photo_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/filename.jpg";
                File temp = new File(imageFilePath);
                if(!temp.exists()){
                    Uri imageFileUri = Uri.fromFile(temp);//获取文件的Uri
                    Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//跳转到相机Activity
                    it.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);//告诉相机拍摄完毕输出图片到指定的Uri
                    startActivityForResult(it, 102);
                }else{
                    //使用Intent
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(temp), "image/*");
                    startActivity(intent);
                }

            }
        });

        scan_er_wei_ma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanRepeater = new Intent(mContext, Capture2Activity.class);
                scanRepeater.putExtra("isNeedResult",true);
                startActivityForResult(scanRepeater, REQUEST_CODE_SCAN_REPEATER);
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
        photo_image.setImageResource(R.drawable.add_photo);
        imageFilePath=null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 666:
                if ( resultCode == RESULT_OK && data != null) {
                    String path = data.getStringExtra("result"); // 图片地址
                    Glide.with(this).load(path).into(photo_image);
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
            case 102:
                if (resultCode == Activity.RESULT_OK) {
                    Bitmap bmp = BitmapFactory.decodeFile(imageFilePath);
                    try {
                        saveFile(compressBySize(Environment.getExternalStorageDirectory().getAbsolutePath()+"/filename.jpg",150,200),Environment.getExternalStorageDirectory().getAbsolutePath()+"/filename.jpg");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    photo_image.setImageBitmap(bmp);
                }
                break;
            case 103:
                Bitmap bm = null;
                // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
                ContentResolver resolver = getContentResolver();

                try {
                    Uri originalUri = data.getData(); // 获得图片的uri

                    bm = MediaStore.Images.Media.getBitmap(resolver, originalUri); // 显得到bitmap图片

                    // 这里开始的第二部分，获取图片的路径：

                    String[] proj = {MediaStore.Images.Media.DATA};

                    // 好像是android多媒体数据库的封装接口，具体的看Android文档
                    @SuppressWarnings("deprecation")
                    Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                    // 按我个人理解 这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    // 最后根据索引值获取图片路径
                    String path = cursor.getString(column_index);
                    photo_image.setImageURI(originalUri);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //@@10.12压缩图片尺寸
    public Bitmap compressBySize(String pathName, int targetWidth,
                                 int targetHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;// 不去真的解析图片，只是获取图片的头部信息，包含宽高等；
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, opts);// 得到图片的宽度、高度；
        float imgWidth = opts.outWidth;
        float imgHeight = opts.outHeight;// 分别计算图片宽度、高度与目标宽度、高度的比例；取大于等于该比例的最小整数；
        int widthRatio = (int) Math.ceil(imgWidth / (float) targetWidth);
        int heightRatio = (int) Math.ceil(imgHeight / (float) targetHeight);
        opts.inSampleSize = 1;
        if (widthRatio > 1 || widthRatio > 1) {
            if (widthRatio > heightRatio) {
                opts.inSampleSize = widthRatio;
            } else {
                opts.inSampleSize = heightRatio;
            }
        }//设置好缩放比例后，加载图片进内容；
        opts.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(pathName, opts);
        return bitmap;
    }

    //@@10.12存储文件到sd卡
    public void saveFile(Bitmap bm, String fileName) throws Exception {
        File dirFile = new File(fileName);//检测图片是否存在
        if(dirFile.exists()){
            dirFile.delete();  //删除原图片
        }
        File myCaptureFile = new File(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));//100表示不进行压缩，70表示压缩率为30%
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
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
    public void dealResult(String t, int resultCode) {

    }
}
