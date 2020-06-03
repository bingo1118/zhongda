package com.smart.cloud.fire.mvp.fragment.SettingFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smart.cloud.fire.activity.AddNFC.AddNFCMacActivity;
import com.smart.cloud.fire.activity.ChuangAnWifiSet.ChuanganWifiStep1Activity;
import com.smart.cloud.fire.activity.UploadNFCInfo.UploadNFCInfoActivity;
import com.smart.cloud.fire.activity.WebServer.WebServerActivity;
import com.smart.cloud.fire.base.ui.MvpFragment;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.mvp.camera.AddCameraFirstActivity;
import com.smart.cloud.fire.ui.AboutActivity;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;


public class SettingFragment extends MvpFragment<SettingFragmentPresenter> implements SettingFragmentView {
    @Bind(R.id.setting_user_id)
    TextView settingUserId;
    @Bind(R.id.setting_user_code)
    TextView settingUserCode;
    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.line_state)
    TextView lineState;

    private Context mContext;

    @Bind(R.id.setting_pay)
    RelativeLayout setting_pay;//@@2018.03.14

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container,
                false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        init();
    }

    private void init() {
        String userID = SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTPASS_NUMBER);
        String username = SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTNAME);

        settingUserId.setText(userID);
        settingUserCode.setText(username);
        String state = MyApp.app.getPushState();
        if(state!=null&&state.length()>0){
            if(state.equals("Online")){
                lineState.setText("在线");
            }
            if(state.equals("Offline")){
                lineState.setText("失联");
            }
        }
    }

    @OnClick({R.id.app_update, R.id.setting_help_about, R.id.setting_help_rela, R.id.setting_help_exit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_update:
                mvpPresenter.checkUpdate(mContext);
                break;
            case R.id.setting_help_about:
                Intent intent = new Intent(mContext, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_help_exit:
                showLoading();
                Intent in = new Intent();
                in.setAction("APP_EXIT");
                in.setPackage(MyApp.getAppProcessName(mContext));//@@7.13只传当前应用
                mContext.sendBroadcast(in);
                getActivity().finish();//@@7.17
                break;
            default:
                break;
        }
    }

    @Override
    protected SettingFragmentPresenter createPresenter() {
        SettingFragmentPresenter mMapFragmentPresenter = new SettingFragmentPresenter(SettingFragment.this);
        return mMapFragmentPresenter;
    }

    @Override
    public String getFragmentName() {
        return "settingFragment";
    }

    @Override
    public void onDestroy() {
        ButterKnife.unbind(this);
        hideLoading();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        if(mProgressBar!=null){
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void bindResult(String msg) {
        T.showShort(mContext, msg);
    }
}
