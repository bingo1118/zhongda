package com.smart.cloud.fire.sms;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.mob.MobSDK;
import com.smart.cloud.fire.utils.T;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class SmssdkHelper {

    private static EventHandler eh;

    public static  void getCode(final Handler mHandler, String phone){
        MobSDK.submitPolicyGrantResult(true, null);
            eh=new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        finish(0,"验证成功",mHandler);
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
                    }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }
                }else{
                    ((Throwable)data).printStackTrace();
                    finish(1,"验证失败",mHandler);
                }
            }
        };
        //注册一个事件回调监听，用于处理SMSSDK接口请求的结果
        SMSSDK.registerEventHandler(eh);
        SMSSDK.getVerificationCode("86", phone);
    }

    public static void finish(int resultCode,String error,Handler mHandler){
        Message msg=new Message();
        msg.what=resultCode;
        msg.obj=error;
        mHandler.sendMessage(msg);
        if(eh!=null){
            SMSSDK.unregisterEventHandler(eh);
        }
    }

}
