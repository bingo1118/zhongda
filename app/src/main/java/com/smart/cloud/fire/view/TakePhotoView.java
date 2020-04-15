package com.smart.cloud.fire.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.smart.cloud.fire.utils.ImageUtils;
import com.yuyh.library.imgsel.common.ImageLoader;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fire.cloud.smart.com.smartcloudfire.R;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class TakePhotoView extends LinearLayout implements ImageLoader {

    @Bind(R.id.image_view)
    ImageView image_view;
    @Bind(R.id.line)
    LinearLayout line;
    @Bind(R.id.btn_delete)
    Button btn_delete;
    @Bind(R.id.btn_retake)
    Button btn_retake;

    public String getPath() {
        return path;
    }

    private String oldImage="";
    private String path;
    public IvClickListener mIvClickListener;

    public void setmIvClickListener(IvClickListener mIvClickListener) {
        this.mIvClickListener = mIvClickListener;
    }

    public String getOldImage() {
        return oldImage;
    }

    public interface IvClickListener{
        public void onClick();
    }


    public TakePhotoView(Context context) {
        super(context);
        initView(context);
    }

    public TakePhotoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TakePhotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TakePhotoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.photo_imageview,this);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_delete,R.id.btn_retake,R.id.image_view})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_view:
                if(path!=null){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
                    startActivity(intent);
                }else{
                    if(mIvClickListener!=null){
                        mIvClickListener.onClick();
                    }
                }
                break;
            case R.id.btn_delete:
                if(path!=null){
                    new File(path).delete();
                }
                path=null;
                oldImage="";
                image_view.setImageResource(R.drawable.take_photo);
                line.setVisibility(GONE);
                break;
            case R.id.btn_retake:
                if(mIvClickListener!=null){
                    mIvClickListener.onClick();
                }
                break;
        }
    }

    public void displayOldImage(Context context, String path) {
        Glide.with(context)
                .load(path).skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image_view);
        this.path=path;
        this.oldImage=path;
        line.setVisibility(VISIBLE);
    }

    @Override
    public void displayImage(Context context, String p, ImageView imageView) {
        if(path!=null){
            new File(path).delete();
        }
        path=p;
        ImageUtils.changeBitmapSizeAndSave(path,1500,2000);
        Glide.with(context).load(path).into(image_view);
        line.setVisibility(VISIBLE);
    }

    public void clear(){
        image_view.setImageResource(R.drawable.take_photo);
        line.setVisibility(GONE);
        if(path!=null){
            File temp=new File(path);
            if(temp.exists()){
                temp.delete();
            }
        }
        path=null;
    }

}

