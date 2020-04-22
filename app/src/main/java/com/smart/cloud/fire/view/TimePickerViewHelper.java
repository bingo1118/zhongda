package com.smart.cloud.fire.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import fire.cloud.smart.com.smartcloudfire.R;

public class TimePickerViewHelper extends LinearLayout{

    TimePickerView pvTime;
    TextView time_tv;
    ImageView clear_im;
    String hint_Text;

    private OnTimeGetListener mOnTimeGetListener;

    public TimePickerViewHelper(Context context) {
        this(context,null);
    }

    public TimePickerViewHelper(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TimePickerViewHelper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimePickerViewHelper);
        if (typedArray != null) {
            hint_Text= typedArray.getString(R.styleable.TimePickerViewHelper_timerpicker_hint_Text);
            typedArray.recycle();
        }
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TimePickerViewHelper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setmOnTimeGetListener(OnTimeGetListener mOnTimeGetListener) {
        this.mOnTimeGetListener = mOnTimeGetListener;
    }

    public interface OnTimeGetListener{
        public void getDate(String dateString);
    }

    public void init(Context context) {

        LayoutInflater.from(context).inflate(R.layout.time_picker_view,this,true);
        time_tv=(TextView)findViewById(R.id.time_tv);
        time_tv.setHint(hint_Text);
        clear_im=(ImageView)findViewById(R.id.clear_im) ;

        clear_im.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clearView();
            }
        });

        pvTime = new TimePickerBuilder(context, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                if(mOnTimeGetListener!=null){
                    DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //HH表示24小时制；
                    String formatDate = dFormat.format(date);
                    mOnTimeGetListener.getDate(formatDate);
                    time_tv.setText(formatDate);
                }
                clear_im.setVisibility(VISIBLE);
            }
        }) .setType(new boolean[]{true, true, true, true, true, true})// 默认全部显示
//                        .setCancelText("Cancel")//取消按钮文字
//                        .setSubmitText("Sure")//确认按钮文字
//                        .setTitleSize(20)//标题文字大小
                .setContentTextSize(20)
                .setTitleText("时间选择")//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(true)//是否循环滚动
//                        .setTitleColor(Color.BLACK)//标题文字颜色
//                        .setSubmitColor(Color.BLUE)//确定按钮文字颜色
//                        .setCancelColor(Color.BLUE)//取消按钮文字颜色
//                        .setTitleBgColor(0xFF666666)//标题背景颜色 Night mode
//                        .setBgColor(0xFF333333)//滚轮背景颜色 Night mode
                .setLabel("","","","","","")//默认设置为年月日时分秒
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(true)//是否显示为对话框样式
                .build();
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });
    }

    public void show(){
        if(pvTime!=null&&!pvTime.isShowing()){
            pvTime.show();
        }
    }

    public void clearView(){
        time_tv.setText("");
        if(mOnTimeGetListener!=null){
            mOnTimeGetListener.getDate("");
        }
        clear_im.setVisibility(GONE);
    }
}
