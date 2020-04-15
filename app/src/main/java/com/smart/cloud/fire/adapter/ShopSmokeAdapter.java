package com.smart.cloud.fire.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smart.cloud.fire.activity.AddDev.AddDevActivity;
import com.smart.cloud.fire.activity.AlarmDevDetail.AlarmDevDetailActivity;
import com.smart.cloud.fire.activity.AllSmoke.AllSmokePresenter;
import com.smart.cloud.fire.activity.GasDevice.OneGasInfoActivity;
import com.smart.cloud.fire.activity.NFCDev.NFCImageShowActivity;
import com.smart.cloud.fire.activity.THDevice.OneTHDevInfoActivity;
import com.smart.cloud.fire.base.presenter.BasePresenter;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.mvp.ChuangAn.ChuangAnActivity;
import com.smart.cloud.fire.mvp.LineChart.LineChartActivity;
import com.smart.cloud.fire.mvp.electric.ElectricActivity;
import com.smart.cloud.fire.mvp.fragment.MapFragment.HttpError;
import com.smart.cloud.fire.mvp.fragment.MapFragment.Smoke;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.Security.AirInfoActivity;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.Security.NewAirInfoActivity;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.ShopInfoFragmentPresenter;
import com.smart.cloud.fire.mvp.fragment.ShopInfoFragment.WiredDevFragment.WiredSmokeListActivity;
import com.smart.cloud.fire.mvp.main.Main3Activity;
import com.smart.cloud.fire.retrofit.ApiStores;
import com.smart.cloud.fire.retrofit.AppClient;
import com.smart.cloud.fire.ui.CallManagerDialogActivity;
import com.smart.cloud.fire.utils.SharedPreferencesManager;
import com.smart.cloud.fire.utils.T;
import com.smart.cloud.fire.utils.VolleyHelper;
import com.smart.cloud.fire.view.LochoLineChartView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import fire.cloud.smart.com.smartcloudfire.R;
import retrofit2.Call;
import retrofit2.Callback;

public class ShopSmokeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnLongClickListener, View.OnClickListener{

    public static final int PULLUP_LOAD_MORE = 0;//上拉加载更多
    public static final int LOADING_MORE = 1;//正在加载中
    public static final int NO_MORE_DATA = 2;//正在加载中
    public static final int NO_DATA = 3;//无数据
    private static final int TYPE_ITEM = 0;  //普通Item View
    private static final int TYPE_FOOTER = 1;  //顶部FootView
    private int load_more_status = 0;
    private LayoutInflater mInflater;
    private Context mContext;
    private List<Smoke> listNormalSmoke;


    public interface OnLongClickListener {
        void onLongClick(View view, int position);
    }
    public interface OnClickListener{
        void onClick(View view, int position);
    }

    private OnLongClickListener mOnLongClickListener = null;
    private OnClickListener mOnClickListener=null;

    public void setOnLongClickListener(OnLongClickListener listener) {
        mOnLongClickListener = listener;
    }

    public void setOnClickListener(OnClickListener listener){
        mOnClickListener=listener;
    }

    @Override
    public boolean onLongClick(View view) {
        if (null != mOnLongClickListener) {
            mOnLongClickListener.onLongClick(view, (int) view.getTag());
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if(null!=mOnClickListener){
            mOnClickListener.onClick(v,(int)v.getTag());
        }
    }

    public ShopSmokeAdapter(Context mContext, List<Smoke> listNormalSmoke) {
        this.mInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
        this.listNormalSmoke = listNormalSmoke;
        this.mContext = mContext;
    }
    /**
     * item显示类型
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //进行判断显示类型，来创建返回不同的View
        if (viewType == TYPE_ITEM) {
            View view = mInflater.inflate(R.layout.shop_info_adapter, parent, false);
            //这边可以做一些属性设置，甚至事件监听绑定
            ItemViewHolder viewHolder = new ItemViewHolder(view);
//            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            return viewHolder;
        } else if (viewType == TYPE_FOOTER) {
            View foot_view = mInflater.inflate(R.layout.recycler_load_more_layout, parent, false);
            //这边可以做一些属性设置，甚至事件监听绑定
            FootViewHolder footViewHolder = new FootViewHolder(foot_view);
            return footViewHolder;
        }
        return null;
    }

    /**
     * 数据的绑定显示
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final Smoke normalSmoke = listNormalSmoke.get(position);
            final int devType = normalSmoke.getDeviceType();
            int netStates = normalSmoke.getNetState();
            ((ItemViewHolder) holder).right_into_image.setVisibility(View.VISIBLE);//@@9.14
            if(normalSmoke.getRssivalue()==null||normalSmoke.getRssivalue().equals("0")){
                ((ItemViewHolder) holder).rssi_value.setVisibility(View.GONE);
                ((ItemViewHolder) holder).rssi_image.setVisibility(View.GONE);
            }else{
                ((ItemViewHolder) holder).rssi_value.setVisibility(View.VISIBLE);
                ((ItemViewHolder) holder).rssi_image.setVisibility(View.VISIBLE);
                ((ItemViewHolder) holder).rssi_value.setText(normalSmoke.getRssivalue());
            }

            ((ItemViewHolder) holder).update_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext,AddDevActivity.class);
                    intent.putExtra("devType",normalSmoke.getDeviceType()+"");
                    intent.putExtra("mac",normalSmoke.getMac());
                    intent.putExtra("oldImage",normalSmoke.getImage());
                    mContext.startActivity(intent);
                }
            });


            int lowvaltage=normalSmoke.getVoltage();//是否低电
            int voltage=normalSmoke.getLowVoltage();//电量
            if(lowvaltage==1){
                ((ItemViewHolder) holder).lowvaltage_image.setVisibility(View.VISIBLE);
                ((ItemViewHolder) holder).voltage_image.setVisibility(View.GONE);
            }else{
                ((ItemViewHolder) holder).voltage_image.setVisibility(View.VISIBLE);
                ((ItemViewHolder) holder).lowvaltage_image.setVisibility(View.GONE);
                setVoltageView(((ItemViewHolder) holder).voltage_image,voltage);
            }

            int ifAlarm=normalSmoke.getIfAlarm();
            if(ifAlarm==0){
                ((ItemViewHolder) holder).alarm_image.setVisibility(View.VISIBLE);
            }else{
                ((ItemViewHolder) holder).alarm_image.setVisibility(View.GONE);
            }

            int ifFault=normalSmoke.getIfFault();
            if(ifFault==1){
                ((ItemViewHolder) holder).fault_image.setVisibility(View.VISIBLE);
            }else{
                ((ItemViewHolder) holder).fault_image.setVisibility(View.GONE);
            }

            ((ItemViewHolder) holder).dev_hearttime_set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    heartTimeSet();
                }
            });
            if(devType==18){
                ((ItemViewHolder) holder).state_name_tv.setVisibility(View.VISIBLE);
                ((ItemViewHolder) holder).state_tv.setVisibility(View.VISIBLE);
                if(normalSmoke.getElectrState()==1){
                    ((ItemViewHolder) holder).state_tv.setText("开");
                }else{
                    ((ItemViewHolder) holder).state_tv.setText("关");
                }
            }else{
                ((ItemViewHolder) holder).state_name_tv.setVisibility(View.GONE);
                ((ItemViewHolder) holder).state_tv.setVisibility(View.GONE);
            }//@@11.01
            ((ItemViewHolder) holder).power_button.setVisibility(View.GONE);
            ((ItemViewHolder) holder).category_group_lin.setOnClickListener(null);

            if (normalSmoke.getImage()!=null&&normalSmoke.getImage().length()>0){
                ((ItemViewHolder) holder).dev_image.setVisibility(View.VISIBLE);
            }else{
                ((ItemViewHolder) holder).dev_image.setVisibility(View.GONE);
            }

            ((ItemViewHolder) holder).dev_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path=ConstantValues.NFC_IMAGES+"devimages/"+normalSmoke.getMac()+".jpg";
                    Intent intent=new Intent(mContext, NFCImageShowActivity.class);
                    intent.putExtra("path",path);
                    mContext.startActivity(intent);
                }
            });
            final TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                    -0.1f, Animation.RELATIVE_TO_SELF, 0.0f);
            mShowAction.setDuration(500);
            ((ItemViewHolder) holder).dev_info_rela.setVisibility(View.GONE);
            ((ItemViewHolder) holder).show_info_text.setText("展开");
            ((ItemViewHolder) holder).show_info_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String s= (String) ((ItemViewHolder) holder).show_info_text.getText();
                        if(s.equals("展开")){
                            ((ItemViewHolder) holder).dev_info_rela.setVisibility(View.VISIBLE);
                            ((ItemViewHolder) holder).dev_info_rela.startAnimation(mShowAction);
                            ((ItemViewHolder) holder).show_info_text.setText("收起");
                        }else{
                            ((ItemViewHolder) holder).dev_info_rela.setVisibility(View.GONE);
                            ((ItemViewHolder) holder).show_info_text.setText("展开");
                        }
                    }
            });
            switch (devType){
                case 89:
                case 87:
                case 86:
                case 61://@@嘉德南京烟感
                case 58://@@嘉德移动烟感
                case 41://@@NB烟感
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("烟感："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("烟感："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    ((ItemViewHolder) holder).power_button.setVisibility(View.VISIBLE);
                    if(devType==89||devType==86){
                        ((ItemViewHolder) holder).power_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("请选择消音类型");
                                final String[] types = {"单次消音", "连续消音"};
                                //    设置一个单项选择下拉框
                                /**
                                 * 第一个参数指定我们要显示的一组下拉单选框的数据集合
                                 * 第二个参数代表索引，指定默认哪一个单选框被勾选上，1表示默认'女' 会被勾选上
                                 * 第三个参数给每一个单选项绑定一个监听器
                                 */
                                builder.setSingleChoiceItems(types, -1, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        cencelSound(normalSmoke,which+1+"");
//                                        Toast.makeText(mContext, "类型为：" + types[which], Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                                builder.setNegativeButton("取消",null);
                                builder.show();
                            }
                        });
                    }else{
                        ((ItemViewHolder) holder).power_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cencelSound(normalSmoke,"1");
                            }
                        });
                    }
                    if(normalSmoke.getElectrState()==1){
                        ((ItemViewHolder) holder).power_button.setText("已消音");
                        ((ItemViewHolder) holder).power_button.setEnabled(false);
                    }else{
                        ((ItemViewHolder) holder).power_button.setText("消音");
                    }
                    break;
                case 56://@@NBIot烟感
                    ((ItemViewHolder) holder).power_button.setVisibility(View.VISIBLE);
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("烟感："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                        ((ItemViewHolder) holder).power_button.setBackgroundColor(Color.GRAY);
                        ((ItemViewHolder) holder).power_button.setClickable(false);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("烟感："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                        ((ItemViewHolder) holder).power_button.setBackgroundColor(Color.RED);
                        ((ItemViewHolder) holder).power_button.setClickable(true);
                        ((ItemViewHolder) holder).power_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final ProgressDialog dialog1 = new ProgressDialog(mContext);
                                dialog1.setTitle("提示");
                                dialog1.setMessage("设置中，请稍候");
                                dialog1.setCanceledOnTouchOutside(false);
                                dialog1.show();
                                String userid= SharedPreferencesManager.getInstance().getData(mContext,
                                        SharedPreferencesManager.SP_FILE_GWELL,
                                        SharedPreferencesManager.KEY_RECENTNAME);
                                ApiStores apiStores1 = AppClient.retrofit(ConstantValues.SERVER_IP_NEW).create(ApiStores.class);
                                Call<HttpError> call=apiStores1.EasyIot_erasure_control(userid,normalSmoke.getMac(),"1");
                                if (call != null) {
                                    call.enqueue(new Callback<HttpError>() {
                                        @Override
                                        public void onResponse(Call<HttpError> call, retrofit2.Response<HttpError> response) {
                                            T.showShort(mContext,response.body().getError()+"");
                                            dialog1.dismiss();
                                        }

                                        @Override
                                        public void onFailure(Call<HttpError> call, Throwable t) {
                                            T.showShort(mContext,"失败");
                                            dialog1.dismiss();
                                        }
                                    });
                                }
                            }
                        });
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 92://@@金特莱南京烟感
                case 57://@@
                case 55://@@嘉德烟感
                case 31://@@12.26 三江iot烟感
                case 21://@@12.01 Lora烟感
                case 1://烟感。。
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("烟感："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("烟感："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 131://标签Lora
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("标签："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线
                        ((ItemViewHolder) holder).smoke_name_text.setText("标签："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 96://南京防爆燃气
                case 73://南京7020燃气
                case 72://防爆燃气
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("燃气探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("燃气探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, OneGasInfoActivity.class);
                            intent.putExtra("Mac",normalSmoke.getMac());
                            intent.putExtra("devType",normalSmoke.getDeviceType());
                            intent.putExtra("devName",normalSmoke.getName());
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                case 106://移动防爆燃气
                case 93://金特莱南京燃气
                case 16://@@9.29
                case 2://燃气。。
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("燃气探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("燃气探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 22:
                case 23:
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("燃气探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("燃气探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 45://海曼气感
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("HM气感："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("HM气感："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 104://热电偶温度器
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("热电偶温度器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("热电偶温度器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }

                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(mContext, LineChartActivity.class);
                            intent.putExtra("electricMac",normalSmoke.getMac());
                            intent.putExtra("isWater", LochoLineChartView.TYPE_TEM);
                            mContext.startActivity(intent);
                        }
                    });


                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 113://南京中电单相
                case 91://金特莱南京烟感
                case 83://南京中电电气
                case 53://NB电气
                case 52://@@Lara电气设备
                case 5://电气。。
                    ((ShopSmokeAdapter.ItemViewHolder) holder).right_into_image.setVisibility(View.VISIBLE);
                    ((ShopSmokeAdapter.ItemViewHolder) holder).setting_button.setVisibility(View.GONE);
                    if (netStates == 0) {//设备不在线。。
                        ((ShopSmokeAdapter.ItemViewHolder) holder).smoke_name_text.setText("电气设备："+normalSmoke.getName()+"（已离线)");
                        ((ShopSmokeAdapter.ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ShopSmokeAdapter.ItemViewHolder) holder).smoke_name_text.setText("电气设备："+normalSmoke.getName());
                        ((ShopSmokeAdapter.ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    final int privilege = MyApp.app.getPrivilege();
                    final int eleState = normalSmoke.getElectrState();
                    //if(privilege==3){//@@8.28权限3有切换电源功能
                    if (netStates == 0) {//设备不在线。。
                        ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setVisibility(View.GONE);
                    }else{
                        if(devType!=80&&devType!=81&&devType!=83){
                            switch (eleState){
                                case 1:
                                    ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setVisibility(View.VISIBLE);
                                    ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setText("切断电源");
                                    break;
                                case 2:
                                    ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setVisibility(View.VISIBLE);
                                    ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setText("打开电源");
                                    break;
                                case 3:
                                    ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setVisibility(View.VISIBLE);
                                    ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setText("设置中");
                                    break;
                                default:
                                    ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setVisibility(View.GONE);
                                    break;
                            }
                        }
                    }


                    ((ShopSmokeAdapter.ItemViewHolder) holder).dev_info_rela.setVisibility(View.GONE);
                    ((ShopSmokeAdapter.ItemViewHolder) holder).show_info_text.setText("展开详情");
                    ((ShopSmokeAdapter.ItemViewHolder) holder).show_info_text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String s= (String) ((ShopSmokeAdapter.ItemViewHolder) holder).show_info_text.getText();
                            if(s.equals("展开详情")){
                                ((ShopSmokeAdapter.ItemViewHolder) holder).dev_info_rela.setVisibility(View.VISIBLE);
                                ((ShopSmokeAdapter.ItemViewHolder) holder).dev_info_rela.startAnimation(mShowAction);
                                ((ShopSmokeAdapter.ItemViewHolder) holder).show_info_text.setText("收起详情");
                            }else{
                                ((ShopSmokeAdapter.ItemViewHolder) holder).dev_info_rela.setVisibility(View.GONE);
                                ((ShopSmokeAdapter.ItemViewHolder) holder).show_info_text.setText("展开详情");
                            }
                        }
                    });
                    if (netStates == 0) {//设备不在线。。
                        ((ShopSmokeAdapter.ItemViewHolder) holder).online_state_image.setImageResource(R.drawable.sblb_lixian);
                    } else {//设备在线。。
                        ((ShopSmokeAdapter.ItemViewHolder) holder).online_state_image.setImageResource(R.drawable.sblb_zaixian);
                        ((ShopSmokeAdapter.ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }

                    ((ShopSmokeAdapter.ItemViewHolder) holder).power_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(netStates==0){
                                Toast.makeText(mContext,"设备不在线",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(privilege!=3&&privilege!=4){
                                Toast.makeText(mContext,"您没有该权限",Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if(eleState==2){
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setMessage("如未排除故障，合闸将造成严重事故!");
                                builder.setTitle("警告");
                                builder.setPositiveButton("我已知晓", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        changepower(2,normalSmoke);
                                        dialog.dismiss();
                                    }
                                });
                                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.create().show();
                            }else if(eleState==1){
                                changepower(1,normalSmoke);
                            }
                        }
                    });
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(normalSmoke.getDeviceType()!=35){
                                Intent intent = new Intent(mContext, ElectricActivity.class);
                                intent.putExtra("ElectricMac",normalSmoke.getMac());
                                intent.putExtra("devType",normalSmoke.getDeviceType());
                                intent.putExtra("repeatMac",normalSmoke.getRepeater());
                                mContext.startActivity(intent);
                            }
                        }
                    });
                    break;
                case 36:
                case 35://NB电弧
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("NB电弧："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("NB电弧："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    break;
                case 99://南京温湿度
                case 79://南京温湿度
                case 26://万科温湿度
                case 25://温湿度传感器
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("温湿度设备："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("温湿度设备："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, OneTHDevInfoActivity.class);
                            intent.putExtra("Mac",normalSmoke.getMac());
                            intent.putExtra("Position",normalSmoke.getName());
                            intent.putExtra("devType",normalSmoke.getDeviceType()+"");
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                case 102:
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("声光报警器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("声光报警器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    ((ItemViewHolder) holder).power_button.setVisibility(View.GONE);
                    ((ItemViewHolder) holder).power_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final ProgressDialog dialog1 = new ProgressDialog(mContext);
                            dialog1.setTitle("提示");
                            dialog1.setMessage("设置中，请稍候");
                            dialog1.setCanceledOnTouchOutside(false);
                            dialog1.show();
                            String userid= SharedPreferencesManager.getInstance().getData(mContext,
                                    SharedPreferencesManager.SP_FILE_GWELL,
                                    SharedPreferencesManager.KEY_RECENTNAME);
                            ApiStores apiStores1 = AppClient.retrofit(ConstantValues.SERVER_IP_NEW).create(ApiStores.class);
                            Call<HttpError> call=apiStores1.nanjing_jiade_cancel(normalSmoke.getMac(),normalSmoke.getDeviceType()+"","2");
                            if (call != null) {
                                call.enqueue(new Callback<HttpError>() {
                                    @Override
                                    public void onResponse(Call<HttpError> call, retrofit2.Response<HttpError> response) {
                                        T.showShort(mContext,response.body().getError()+"");
                                        dialog1.dismiss();
                                    }

                                    @Override
                                    public void onFailure(Call<HttpError> call, Throwable t) {
                                        T.showShort(mContext,"失败");
                                        dialog1.dismiss();
                                    }
                                });
                            }
                        }
                    });
                    break;
                case 7://声光。。
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("声光报警器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("声光报警器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 20://@@无线输入输出模块
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("无线输入输出模块："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("无线输入输出模块："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 108:
                case 103:
                case 84:
                case 8://手动。。
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("手报按钮："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("手报按钮："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 9://三江设备@@5.11。。
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("三江设备："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("三江设备："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    break;
                case 12://门磁
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("门磁："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("门磁："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 11://红外
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("红外探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("红外探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 13://环境
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("环境探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("环境探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(normalSmoke.getNetState()==0){
                                Toast.makeText(mContext,"设备不在线",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Intent intent = new Intent(mContext, NewAirInfoActivity.class);
                            intent.putExtra("Mac",normalSmoke.getMac());
                            intent.putExtra("Position",normalSmoke.getName());
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                case 51://创安
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("CA燃气："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("CA燃气："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ChuangAnActivity.class);
                            intent.putExtra("Mac",normalSmoke.getMac());
                            intent.putExtra("Position",normalSmoke.getName());
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                case 125:
                case 100://南京防爆水压
                case 97://南京普通水压
                case 94://金特莱南京水压
                case 78:
                case 70:
                case 68:
                case 47:
                case 42:
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水压探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水压探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, LineChartActivity.class);
                            intent.putExtra("electricMac",normalSmoke.getMac());
                            intent.putExtra("isWater", LochoLineChartView.TYPE_WATER_PRESURE);//@@是否为水压
                            intent.putExtra("devType",devType);
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                case 10://水压设备@@5.11。。
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水压探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水压探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, LineChartActivity.class);
                            intent.putExtra("electricMac",normalSmoke.getMac());
                            intent.putExtra("isWater",LochoLineChartView.TYPE_WATER_PRESURE);
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                case 43://@@lora水压
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水压探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水压探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, LineChartActivity.class);
                            intent.putExtra("electricMac",normalSmoke.getMac());
                            intent.putExtra("isWater",LochoLineChartView.TYPE_WATER_PRESURE_WITH_MORE);//@@是否为水压
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                case 124:
                case 101://南京防爆水位
                case 98://南京普通水位
                case 95://金特莱南京水位
                case 69:
                case 85:
                case 48:
                case 46:
                case 44://万科水位
                case 19://水位设备@@2018.01.02
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水位探测器："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水位探测器："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, LineChartActivity.class);
                            intent.putExtra("electricMac",normalSmoke.getMac());
                            intent.putExtra("isWater",LochoLineChartView.TYPE_WATER_LEVEL);//@@是否为水压
                            intent.putExtra("devType",devType);
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                case 27://万科水浸
                case 15://水浸设备@@8.3。。
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水浸："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("水浸："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 90://南京喷淋
                case 82://NB直连喷淋
                case 18://喷淋@@10.31
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("喷淋："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("喷淋："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).right_into_image.setVisibility(View.GONE);
                    break;
                case 14://GPS设备@@8.8
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("GPS："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("GPS："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    break;
                case 126:
                case 119://三江有线传输装置
                    if (netStates == 0) {//设备不在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("传输装置："+normalSmoke.getName()+"（已离线)");
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                    } else {//设备在线。。
                        ((ItemViewHolder) holder).smoke_name_text.setText("传输装置："+normalSmoke.getName());
                        ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                    }
                    ((ItemViewHolder) holder).category_group_lin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, WiredSmokeListActivity.class);
                            intent.putExtra("Mac",normalSmoke.getMac());
                            intent.putExtra("Position",normalSmoke.getName());
                            mContext.startActivity(intent);
                        }
                    });
                    ((ItemViewHolder) holder).power_button.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).power_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final ProgressDialog dialog1 = new ProgressDialog(mContext);
                            dialog1.setTitle("提示");
                            dialog1.setMessage("设置中，请稍候");
                            dialog1.setCanceledOnTouchOutside(false);
                            dialog1.show();
                            ApiStores apiStores1 = AppClient.retrofit(ConstantValues.SERVER_IP_NEW).create(ApiStores.class);
                            Call<HttpError> call=apiStores1.cancelSound(normalSmoke.getMac());
                            if (call != null) {
                                call.enqueue(new Callback<HttpError>() {
                                    @Override
                                    public void onResponse(Call<HttpError> call, retrofit2.Response<HttpError> response) {
                                        T.showShort(mContext,response.body().getError()+"");
                                        dialog1.dismiss();
                                    }
                                    @Override
                                    public void onFailure(Call<HttpError> call, Throwable t) {
                                        T.showShort(mContext,"失败");
                                        dialog1.dismiss();
                                    }
                                });
                            }
                        }
                    });
                    break;
                    default:
                        if (netStates == 0) {//设备不在线。。
                            ((ItemViewHolder) holder).smoke_name_text.setText("未知设备："+normalSmoke.getName()+"（已离线)");
                            ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.RED);
                        } else {//设备在线。。
                            ((ItemViewHolder) holder).smoke_name_text.setText("未知设备"+normalSmoke.getName());
                            ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
                        }
                        break;

            }

            if (netStates == 0) {//设备不在线。。
                ((ItemViewHolder) holder).online_state_image.setImageResource(R.drawable.sblb_lixian);
            } else {//设备在线。。
                ((ItemViewHolder) holder).online_state_image.setImageResource(R.drawable.sblb_zaixian);
                ((ItemViewHolder) holder).smoke_name_text.setTextColor(Color.BLACK);
            }

            ((ItemViewHolder) holder).address_tv.setText(normalSmoke.getAddress());
            ((ItemViewHolder) holder).mac_tv.setText(normalSmoke.getMac());//@@
            ((ItemViewHolder) holder).repeater_tv.setText(normalSmoke.getRepeater());
            ((ItemViewHolder) holder).type_tv.setText(normalSmoke.getPlaceType());
            ((ItemViewHolder) holder).area_tv.setText(normalSmoke.getAreaName());

            ((ItemViewHolder) holder).alarm_history_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext,AlarmDevDetailActivity.class);
                    intent.putExtra("mac",normalSmoke.getMac()+"");
                    intent.putExtra("name",normalSmoke.getName()+"");
                    mContext.startActivity(intent);
                }
            });

            ((ItemViewHolder) holder).manager_img.setOnClickListener(new View.OnClickListener() {//拨打电话提示框。。
                @Override
                public void onClick(View v) {
                    if(normalSmoke.getPrincipal1()!=null&&normalSmoke.getPrincipal1().length()>0){
                        Intent intent=new Intent(mContext, CallManagerDialogActivity.class);
                        intent.putExtra("people1",normalSmoke.getPrincipal1());
                        intent.putExtra("people2",normalSmoke.getPrincipal2());
                        intent.putExtra("phone1",normalSmoke.getPrincipal1Phone());
                        intent.putExtra("phone2",normalSmoke.getPrincipal2Phone());
                        mContext.startActivity(intent);
                    }else{
                        T.showShort(mContext,"无联系人信息");
                    }
                }
            });
            ((ItemViewHolder) holder).category_group_lin.setOnLongClickListener(this);
            ((ItemViewHolder) holder).category_group_lin.setTag(position);
            holder.itemView.setTag(position);
        } else if (holder instanceof FootViewHolder) {
            FootViewHolder footViewHolder = (FootViewHolder) holder;
            switch (load_more_status) {
                case PULLUP_LOAD_MORE:
                    footViewHolder.footViewItemTv.setText("上拉加载更多...");
                    break;
                case LOADING_MORE:
                    footViewHolder.footViewItemTv.setText("正在加载更多数据...");
                    break;
                case NO_MORE_DATA:
                    T.showShort(mContext, "没有更多数据");
                    footViewHolder.footer.setVisibility(View.GONE);
                    break;
                case NO_DATA:
                    footViewHolder.footer.setVisibility(View.GONE);
                    break;
            }
        }

    }

    /**
     * 设置心跳时间
     */
    private void heartTimeSet() {
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(mContext);
        final View dialogView = LayoutInflater.from(mContext)
                .inflate(R.layout.dev_heart_time_setting,null);
        customizeDialog.setView(dialogView);
        final EditText heartTime_edit=(EditText) dialogView.findViewById(R.id.hearttime_value);
        Button commit_btn=(Button)dialogView.findViewById(R.id.commit);
        commit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(heartTime_edit.getText().length()>0){
                    T.showShort(mContext,"设置成功");
                }else{
                    T.showShort(mContext,"输入不合法");
                }
            }
        });
        customizeDialog.show();
    }

    /**
     * 进行判断是普通Item视图还是FootView视图
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        // 最后一个item设置为footerView
        if (position == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return listNormalSmoke.size();
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.category_group_lin)
        LinearLayout category_group_lin;
        @Bind(R.id.smoke_name_text)
        TextView smoke_name_text;
        @Bind(R.id.mac_tv)
        TextView mac_tv;
        @Bind(R.id.repeater_tv)
        TextView repeater_tv;
        @Bind(R.id.area_tv)
        TextView area_tv;
        @Bind(R.id.type_tv)
        TextView type_tv;
        @Bind(R.id.address_tv)
        TextView address_tv;
        @Bind(R.id.manager_img)
        TextView manager_img;
        @Bind(R.id.right_into_image)
        ImageView right_into_image;
        @Bind(R.id.item_lin)
        LinearLayout item_lin;//@@8.8
        @Bind(R.id.state_name_tv)
        TextView state_name_tv;//@@11.01
        @Bind(R.id.state_tv)
        TextView state_tv;//@@11.01
        @Bind(R.id.rssi_value)
        TextView rssi_value;//@@2018.03.07
        @Bind(R.id.xy_button)
        TextView power_button;//@@2018.03.07
        @Bind(R.id.dev_image)
        TextView dev_image;//@@2018.03.07
        @Bind(R.id.dev_hearttime_set)
        TextView dev_hearttime_set;//@@2018.03.07
        @Bind(R.id.voltage_image)
        ImageView voltage_image;
        @Bind(R.id.rssi_image)
        ImageView rssi_image;
        @Bind(R.id.show_info_text)
        TextView show_info_text;
        @Bind(R.id.dev_info_rela)
        RelativeLayout dev_info_rela;
        @Bind(R.id.online_state_image)
        ImageView online_state_image;
        @Bind(R.id.setting_button)
        TextView setting_button;
        @Bind(R.id.update_tv)
        TextView update_tv;
        @Bind(R.id.alarm_history_button)
        TextView alarm_history_button;

        @Bind(R.id.alarm_image)
        ImageView alarm_image;
        @Bind(R.id.fault_image)
        ImageView fault_image;
        @Bind(R.id.lowvaltage_image)
        ImageView lowvaltage_image;

        public ItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * 底部FootView布局
     */
    public static class FootViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.foot_view_item_tv)
        TextView footViewItemTv;
        @Bind(R.id.footer)
        LinearLayout footer;
        public FootViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    //添加数据
    public void addItem(List<Smoke> smokeList) {
        smokeList.addAll(listNormalSmoke);
        listNormalSmoke.removeAll(listNormalSmoke);
        listNormalSmoke.addAll(smokeList);
        notifyDataSetChanged();
    }

    public void addMoreItem(List<Smoke> smokeList) {
        listNormalSmoke.addAll(smokeList);
        notifyDataSetChanged();
    }

    /**
     * //上拉加载更多
     * PULLUP_LOAD_MORE=0;
     * //正在加载中
     * LOADING_MORE=1;
     * //加载完成已经没有更多数据了
     * NO_MORE_DATA=2;
     *
     * @param status
     */
    public void changeMoreStatus(int status) {
        load_more_status = status;
        notifyDataSetChanged();
    }

    //烟感设备消音
    private void cencelSound(Smoke normalSmoke,String state) {
        final ProgressDialog dialog1 = new ProgressDialog(mContext);
        dialog1.setTitle("提示");
        dialog1.setMessage("设置中，请稍候");
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.show();
        String userid= SharedPreferencesManager.getInstance().getData(mContext,
                SharedPreferencesManager.SP_FILE_GWELL,
                SharedPreferencesManager.KEY_RECENTNAME);
        ApiStores apiStores1 = AppClient.retrofit(ConstantValues.SERVER_IP_NEW).create(ApiStores.class);
        Call<HttpError> call=null;
        switch (normalSmoke.getDeviceType()){
            case 41:
                call=apiStores1.NB_IOT_Control(userid,normalSmoke.getMac(),"1");
                break;
            case 58:
                call=apiStores1.nanjing_jiade_cancel(normalSmoke.getMac(),"58",state);
                break;
            case 61:
                call=apiStores1.nanjing_jiade_cancel(normalSmoke.getMac(),"61",state);
                break;
            case 86:
                call=apiStores1.nanjing_jiade_cancel(normalSmoke.getMac(),"86",state);
                break;
            case 89:
                call=apiStores1.nanjing_jiade_cancel(normalSmoke.getMac(),"89",state);
                break;
        }
        if (call != null) {
            call.enqueue(new Callback<HttpError>() {
                @Override
                public void onResponse(Call<HttpError> call, retrofit2.Response<HttpError> response) {
                    T.showShort(mContext,response.body().getError()+"");
                    dialog1.dismiss();
                }

                @Override
                public void onFailure(Call<HttpError> call, Throwable t) {
                    T.showShort(mContext,"失败");
                    dialog1.dismiss();
                }
            });
        }
    }

    public void changepower(final int eleState, final Smoke normalSmoke){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if(eleState==1){
            builder.setMessage("确认切断电源吗？");
        }else{
            builder.setMessage("隐患已解决，确定合闸？");
        }
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userID = SharedPreferencesManager.getInstance().getData(mContext,
                        SharedPreferencesManager.SP_FILE_GWELL,
                        SharedPreferencesManager.KEY_RECENTNAME);
                RequestQueue mQueue = Volley.newRequestQueue(mContext);
                String url="";
                if(normalSmoke.getDeviceType()==53){
                    if(eleState==1){
                        url= ConstantValues.SERVER_IP_NEW+"EasyIot_Switch_control?devSerial="+normalSmoke.getMac()+"&eleState=2&appId=1&userId="+userID;
                    }else{
                        url=ConstantValues.SERVER_IP_NEW+"EasyIot_Switch_control?devSerial="+normalSmoke.getMac()+"&eleState=1&appId=1&userId="+userID;
                    }
                }else if(normalSmoke.getDeviceType()==75||normalSmoke.getDeviceType()==77){
                    String userid= SharedPreferencesManager.getInstance().getData(mContext,
                            SharedPreferencesManager.SP_FILE_GWELL,
                            SharedPreferencesManager.KEY_RECENTNAME);
                    if(eleState==1){
                        url= ConstantValues.SERVER_IP_NEW+"Telegraphy_Uool_control?imei="+normalSmoke.getMac()+"&deviceType="+normalSmoke.getDeviceType()+"&devCmd=12&userid="+userid;
                    }else{
                        url=ConstantValues.SERVER_IP_NEW+"Telegraphy_Uool_control?imei="+normalSmoke.getMac()+"&deviceType="+normalSmoke.getDeviceType()+"&devCmd=13&userid="+userid;
                    }
                }else{
                    if(eleState==1){
                        url= ConstantValues.SERVER_IP_NEW+"ackControl?smokeMac="+normalSmoke.getMac()+"&eleState=2&userId="+userID;
                    }else{
                        url=ConstantValues.SERVER_IP_NEW+"ackControl?smokeMac="+normalSmoke.getMac()+"&eleState=1&userId="+userID;
                    }
                }
                final ProgressDialog dialog1 = new ProgressDialog(mContext);
                dialog1.setTitle("提示");
                dialog1.setMessage("设置中，请稍候...");
                new CountDownTimer(60000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        if(dialog1.isShowing()&&millisUntilFinished<40000){
                            dialog1.setMessage("网络延迟，请耐心等待...");
                        }
                    }

                    public void onFinish() {
                        dialog1.setMessage("设置中，请稍候...");
                    }
                }.start();

                dialog1.setCanceledOnTouchOutside(false);
                dialog1.show();
//                Toast.makeText(mContext,"设置中，请稍候",Toast.LENGTH_SHORT).show();
                StringRequest stringRequest = new StringRequest(url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject=new JSONObject(response);
                                    int errorCode=jsonObject.getInt("errorCode");
                                    if(errorCode==0){
                                        switch (eleState){
                                            case 2:
                                                normalSmoke.setElectrState(1);
                                                break;
                                            case 1:
                                                normalSmoke.setElectrState(2);
                                                break;
                                        }
                                        notifyDataSetChanged();
                                        Toast.makeText(mContext,jsonObject.getString("error"),Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(mContext,jsonObject.getString("error"),Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                dialog1.dismiss();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog1.dismiss();
                        Toast.makeText(mContext,"设置超时",Toast.LENGTH_SHORT).show();
                    }
                });
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(300000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
//                        0,
//                        0.0f));
                mQueue.add(stringRequest);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 设置电量视图显示
     * @param voltage_image
     * @param voltage
     */
    private void setVoltageView(ImageView voltage_image, int voltage) {
        voltage_image.setVisibility(View.VISIBLE);

        if(voltage==0){
            voltage_image.setVisibility(View.GONE);
        }else if(voltage>0&&voltage<10){
            voltage_image.setVisibility(View.VISIBLE);
            voltage_image.setImageResource(R.drawable.p0);
        }else if(voltage>=10&&voltage<30){
            voltage_image.setVisibility(View.VISIBLE);
            voltage_image.setImageResource(R.drawable.p1);
        }else if(voltage>=30&&voltage<60){
            voltage_image.setVisibility(View.VISIBLE);
            voltage_image.setImageResource(R.drawable.p2);
        }else if(voltage>=60&&voltage<80){
            voltage_image.setVisibility(View.VISIBLE);
            voltage_image.setImageResource(R.drawable.p3);
        }else if(voltage>=80){
            voltage_image.setVisibility(View.VISIBLE);
            voltage_image.setImageResource(R.drawable.p4);
        }
    }
}
