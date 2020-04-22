package com.smart.cloud.fire.activity.AddDev;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.smart.cloud.fire.activity.AddNFC.AddNFCActivity;
import com.smart.cloud.fire.mvp.camera.AddCameraFirstActivity;
import com.smart.cloud.fire.rqcode.Capture2Activity;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;

public class ChioceDevTypeActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chioce_dev_type);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.nfc_lin,R.id.sdtj_lin,R.id.smsr_lin})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.nfc_lin:
                Intent intent6=new Intent(ChioceDevTypeActivity.this,AddNFCActivity.class);
                intent6.putExtra("devType",6);
                startActivity(intent6);
                break;
            case R.id.sdtj_lin:
                Intent intent7=new Intent(ChioceDevTypeActivity.this,AddDevActivity.class);
                intent7.putExtra("devType",1);
                startActivity(intent7);
                break;
            case R.id.smsr_lin:
                Intent intent8=new Intent(ChioceDevTypeActivity.this,Capture2Activity.class);
                intent8.putExtra("devType",6);
                startActivity(intent8);
                break;
        }
    }


}
