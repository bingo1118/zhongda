package com.smart.cloud.fire.mvp.register;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.smart.cloud.fire.base.ui.MvpActivity;
import com.smart.cloud.fire.mvp.login.LoginActivity;
import com.smart.cloud.fire.mvp.login.SplashActivity;
import com.smart.cloud.fire.mvp.register.presenter.RegisterPresenter;
import com.smart.cloud.fire.mvp.register.view.RegisterView;
import com.smart.cloud.fire.sms.SmssdkHelper;
import com.smart.cloud.fire.utils.T;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import cn.smssdk.SMSSDK;
import fire.cloud.smart.com.smartcloudfire.R;
import rx.functions.Action1;

/**
 * Created by Administrator on 2016/9/19.
 */
public class RegisterPhoneActivity extends MvpActivity<RegisterPresenter> implements RegisterView {
    private Context mContext;
    @Bind(R.id.register_user)
    EditText register_user;
    @Bind(R.id.register_pwd)
    EditText register_pwd;
    @Bind(R.id.register_comfire_pwd)
    EditText register_comfire_pwd;
    @Bind(R.id.register_code)
    EditText register_code;
    @Bind(R.id.register_get_code)
    Button register_get_code;
    @Bind(R.id.register_btn_phone)
    Button register_btn_phone;
    @Bind(R.id.register_old_user_tv)
    TextView register_old_user_tv;
    @Bind(R.id.title_tv)
    TextView title_tv;
    @Bind(R.id.mProgressBar)
    ProgressBar mProgressBar;
    private String phoneNO;
    String pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);
        mContext=this;
        doAction();
        boolean ifreset=getIntent().getBooleanExtra("isReset",false);
        if(ifreset){
            register_btn_phone.setText("提交");
            title_tv.setText("注册账号");
        }else{
            title_tv.setText("重设密码");
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    mvpPresenter.addUser(phoneNO,pwd,mContext);
                    break;
                case 1:
                    T.showShort(mContext,msg.obj.toString());
            }

        }
    };

    private void doAction() {
        //获取验证码
        RxView.clicks(register_get_code).throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        phoneNO = register_user.getText().toString().trim();
//                        mvpPresenter.getMesageCode(phoneNO);
                        SmssdkHelper.getCode(handler,phoneNO);
                        register_get_code.setClickable(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for(int i=60;i>0;i--){
                                    final int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            register_get_code.setText(finalI +"秒后重新获取");
                                        }
                                    });
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        register_get_code.setText("重新获取");
                                        register_get_code.setClickable(true);
                                    }
                                });
                            }
                        }).start();
                    }
                });
        RxView.clicks(register_btn_phone).throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(imm.isActive()){
                            imm.hideSoftInputFromWindow(register_btn_phone.getWindowToken(),0);//隐藏输入软键盘@@4.28
                        }
                        phoneNO = register_user.getText().toString().trim();
                        pwd = register_pwd.getText().toString().trim();
                        String rePwd = register_comfire_pwd.getText().toString().trim();
                        String code = register_code.getText().toString().trim();
                        if(phoneNO.length()==0){
                            T.showShort(mContext,"请输入注册的手机号");
                            return;
                        };
                        if(pwd.length()==0){
                            T.showShort(mContext,"请输入注册密码");
                            return;
                        };
                        if(rePwd.length()==0){
                            T.showShort(mContext,"请再次输入注册密码");
                            return;
                        };
                        SMSSDK.submitVerificationCode("86", phoneNO, code);
//                        mvpPresenter.register(phoneNO,pwd,rePwd,code,mContext);
                    }
                });
        RxView.clicks(register_old_user_tv).throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        //跳转到登录界面
                        Intent intent1 = new Intent(mContext,LoginActivity.class);
                        startActivity(intent1);
                        finish();
                        //一个参数是第一个activity进入时的动画，另外一个参数则是第二个activity退出时的动画。。
                        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    }
                });
    }

    @Override
    protected RegisterPresenter createPresenter() {
        return new RegisterPresenter(this);
    }

    @Override
    public void getDataFail(String msg) {
        T.showShort(mContext,msg);
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
    public void register() {
        T.showShort(mContext,"注册成功,正在登陆");
        Intent login = new Intent(mContext, SplashActivity.class);
        startActivity(login);
        finish();
    }

    @Override
    public void getMesageSuccess() {
        T.showShort(mContext,"获取验证码成功");
    }
}
