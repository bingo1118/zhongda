package com.smart.cloud.fire.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.smart.cloud.fire.activity.AddNFC.NFCDeviceType;
import com.smart.cloud.fire.base.presenter.BasePresenter;
import com.smart.cloud.fire.global.AlarmType;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.Point;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.utils.VolleyHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fire.cloud.smart.com.smartcloudfire.R;

public class ZDAlarmTypeChooseListView extends LinearLayout {

    private BasePresenter basePresenter;
    private TextView editText;
    private ImageView imageView;
    private LinearLayout search_line;
    private PopupWindow popupWindow = null;
    private List<Object> dataList = new ArrayList<>();
    private View mView;
    private Context mContext;
    ProgressBar loading_prg_monitor;
    private RelativeLayout clear_choice;

    List<AlarmType> mData;

    public ZDAlarmTypeChooseListView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public ZDAlarmTypeChooseListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public ZDAlarmTypeChooseListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView();
    }

    public void initView() {
        mContext = getContext();

        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.dropdownlist_view, this, true);
        loading_prg_monitor = (ProgressBar) findViewById(R.id.loading_prg_monitor);
        editText = (TextView) findViewById(R.id.text);
        imageView = (ImageView) findViewById(R.id.btn);
        clear_choice = (RelativeLayout) findViewById(R.id.clear_choice);
        clear_choice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onChildChooceClickListener!=null){
                    onChildChooceClickListener.OnChildClick(null);
                    editText.setText("");
                    clear_choice.setVisibility(GONE);
                    imageView.setVisibility(VISIBLE);
                }
            }
        });
        getAreaListData();
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopWindow();
            }
        });
    }

    private void getAreaListData() {
        showLoading();
        String url = ConstantValues.SERVER_IP_NEW + "getAlarmType" ;
        VolleyHelper.getInstance(mContext).getStringResponse(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getInt("errorCode") == 0) {
                                JSONArray jsonArrayParent = jsonObject.getJSONArray("alarmTypeList");
                                List<AlarmType> alarmList = new ArrayList<>();
                                for (int i = 0; i < jsonArrayParent.length(); i++) {
                                    JSONObject tempParent = jsonArrayParent.getJSONObject(i);
                                    AlarmType alarmType = new AlarmType();
                                    alarmType.setAlarmCode(tempParent.getInt("alarmCode"));
                                    alarmType.setAlarmName(tempParent.getString("alarmName"));
                                    alarmList.add(alarmType);
                                }
                                mData = alarmList;
                            }
                            closeLoading();
                            setClickable(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", "error");
                        List<AlarmType> alarmList = new ArrayList<>();
                        alarmList.add(new AlarmType(1,"222"));
                        alarmList.add(new AlarmType(2,"4545c"));
                        mData = alarmList;
                        closeLoading();
                    }
                });
    }

    public void showLoading() {
        imageView.setVisibility(View.GONE);
        loading_prg_monitor.setVisibility(View.VISIBLE);
    }

    public void closeLoading() {
        loading_prg_monitor.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
    }

    /**
     * 打开下拉列表弹窗
     */
    public void showPopWindow() {
        // 加载popupWindow的布局文件
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
        View contentView = layoutInflater.inflate(R.layout.dropdownlist_popupwindow, null, false);
        search_line = contentView.findViewById(R.id.search_line);
        TextView info_tv = contentView.findViewById(R.id.info_tv);
        search_line.setVisibility(GONE);
        ZDAlarmTypeChooseListView.XCDropDownListAdapter xcDropDownListAdapter = new ZDAlarmTypeChooseListView.XCDropDownListAdapter(getContext());
        ListView listView = (ListView) contentView.findViewById(R.id.listView);

        listView.setAdapter(xcDropDownListAdapter);
        popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.showAsDropDown(this);
    }

    /**
     * 关闭下拉列表弹窗
     */
    public void closePopWindow() {
        popupWindow.dismiss();
        popupWindow = null;
    }

    public boolean ifShow() {
        if (popupWindow == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 设置数据
     *
     * @param list
     */
    public void setItemsData(List<Object> list, BasePresenter basePresenter) {
        dataList = list;
        editText.setText("");
        this.basePresenter = basePresenter;
    }

    public void setEditTextData(String editTextData) {
        editText.setText(editTextData);
    }

    /**
     * 数据适配器
     *
     * @author caizhiming
     */
    class XCDropDownListAdapter extends BaseAdapter {

        Context mContext;
        LayoutInflater inflater;

        public XCDropDownListAdapter(Context ctx) {
            mContext = ctx;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            // 自定义视图
            ZDAlarmTypeChooseListView.ListItemView listItemView = null;
            if (convertView == null) {
                // 获取list_item布局文件的视图
                convertView = LayoutInflater.from(mContext).inflate(R.layout.dropdown_list_item, null);

                listItemView = new ZDAlarmTypeChooseListView.ListItemView();
                // 获取控件对象
                listItemView.tv = (TextView) convertView
                        .findViewById(R.id.tv);
                listItemView.layout = (LinearLayout) convertView.findViewById(R.id.layout_container);
                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ZDAlarmTypeChooseListView.ListItemView) convertView.getTag();
            }
            final AlarmType mAlarmType = mData.get(position);;
            // 设置数据
            listItemView.tv.setText(mAlarmType.getAlarmName());
            final String text = mAlarmType.getAlarmName();
            listItemView.layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    editText.setText(text);
                    imageView.setVisibility(View.GONE);
                    clear_choice.setVisibility(View.VISIBLE);
                    if(onChildChooceClickListener!=null){
                        onChildChooceClickListener.OnChildClick(mAlarmType);
                    }
                    closePopWindow();
                }
            });

            return convertView;
        }

    }

    private static class ListItemView {
        TextView tv;
        LinearLayout layout;
    }

    public void addFinish() {
        imageView.setVisibility(View.VISIBLE);
        clear_choice.setVisibility(View.GONE);
    }

    public void setEditTextHint(String textStr) {
        editText.setHint(textStr);
    }

    ZDAlarmTypeChooseListView.OnChildChooceClickListener onChildChooceClickListener = null;//@@9.12

    public interface OnChildChooceClickListener {
        void OnChildClick(AlarmType info);
    }

    public void setOnChildChooceClickListener(ZDAlarmTypeChooseListView.OnChildChooceClickListener o) {
        this.onChildChooceClickListener = o;
    }
}

