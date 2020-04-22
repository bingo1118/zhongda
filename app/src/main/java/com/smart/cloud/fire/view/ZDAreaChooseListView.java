package com.smart.cloud.fire.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.smart.cloud.fire.base.presenter.BasePresenter;
import com.smart.cloud.fire.global.Area;
import com.smart.cloud.fire.global.ConstantValues;
import com.smart.cloud.fire.global.MyApp;
import com.smart.cloud.fire.global.ShopType;
import com.smart.cloud.fire.utils.VolleyHelper;
import com.smart.cloud.fire.utils.WindowUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fire.cloud.smart.com.smartcloudfire.R;

public class ZDAreaChooseListView extends LinearLayout {

    private TextView mTextView;
    private ImageView imageView;
    private PopupWindow popupWindow = null;
    private Context mContext;
    ProgressBar loading_prg_monitor;
    private RelativeLayout clear_choice;

    List<Area> parent = null;
    Map<String, List<Area>> map = null;

    private boolean ifHavaChooseAll = true;

    int hint_TextColor ;
    int edit_TextColor ;
    String edit_Text ;
    String hint_Text;
    boolean clear_choice_isShow = true;

    private Area choosed_area;


    public ZDAreaChooseListView(Context context) {
        this(context, null);
    }

    public ZDAreaChooseListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ZDAreaChooseListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initParams(context, attrs);
        initView();
    }

    private void initParams(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ZDAreaChooseListView);
        if (typedArray != null) {
            hint_TextColor = typedArray.getColor(R.styleable.ZDAreaChooseListView_hint_TextColor, Color.GRAY);
            edit_TextColor = typedArray.getColor(R.styleable.ZDAreaChooseListView_edit_TextColor, Color.BLACK);
            edit_Text = typedArray.getString(R.styleable.ZDAreaChooseListView_edit_Text);
            hint_Text= typedArray.getString(R.styleable.ZDAreaChooseListView_hint_Text);
            clear_choice_isShow = typedArray.getBoolean(R.styleable.ZDAreaChooseListView_clear_choice_IsShow,true);
            typedArray.recycle();
        }
    }


    public void initView() {
        mContext = getContext();

        LayoutInflater.from(mContext).inflate(R.layout.dropdownlist_view_fire_zd, this, true);
        loading_prg_monitor = (ProgressBar) findViewById(R.id.loading_prg_monitor);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setHint(hint_Text);
        mTextView.setHintTextColor(hint_TextColor);
        imageView = (ImageView) findViewById(R.id.btn);
        clear_choice = (RelativeLayout) findViewById(R.id.clear_choice);

        clear_choice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setVisibility(View.VISIBLE);
                clear_choice.setVisibility(View.GONE);
                mTextView.setText("");
                choosed_area=null;
            }
        });
        imageView.setVisibility(VISIBLE);
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
        String url = ConstantValues.SERVER_IP_NEW + "getAreaInfo?userId=" + MyApp.getUserID() + "&privilege=" + MyApp.getPrivilege();
        VolleyHelper.getInstance(mContext).getStringResponse(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getInt("errorCode") == 0) {
                                parent = new ArrayList<>();
                                map = new HashMap<>();
                                JSONArray jsonArrayParent = jsonObject.getJSONArray("areas");
                                for (int i = 0; i < jsonArrayParent.length(); i++) {
                                    JSONObject tempParent = jsonArrayParent.getJSONObject(i);
                                    Area tempArea = new Area();
                                    tempArea.setAreaId(tempParent.getString("areaId"));
                                    tempArea.setAreaName(tempParent.getString("areaName"));
                                    tempArea.setIsParent(1);
                                    parent.add(tempArea);
                                    List<Area> child = new ArrayList<>();
                                    JSONArray jsonArrayChild = tempParent.getJSONArray("areas");
                                    for (int j = 0; j < jsonArrayChild.length(); j++) {
                                        JSONObject tempChild = jsonArrayChild.getJSONObject(j);
                                        Area tempAreaChild = new Area();
                                        tempAreaChild.setAreaId(tempChild.getString("areaId"));
                                        tempAreaChild.setAreaName(tempChild.getString("areaName"));
                                        tempAreaChild.setIsParent(0);
                                        child.add(tempAreaChild);
                                    }
                                    map.put(tempParent.getString("areaName"), child);
                                }
                                closeLoading();
                            }
                            setItemsData(parent, map);
                            setClickable(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", "error");
                    }
                });
    }


    public boolean ifShow() {
        if (popupWindow == null) {
            return false;
        } else {
            return true;
        }
    }

    public void showLoading() {
        imageView.setVisibility(View.GONE);
        clear_choice.setVisibility(View.GONE);
        loading_prg_monitor.setVisibility(View.VISIBLE);
    }

    public void closeLoading() {
        loading_prg_monitor.setVisibility(View.GONE);
        clear_choice.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
    }

    public void searchClose() {
        imageView.setVisibility(View.VISIBLE);
        clear_choice.setVisibility(View.GONE);
    }





    /**
     * 打开下拉列表弹窗
     */
    public void showPopWindow() {
        // 加载popupWindow的布局文件
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.area_chooce_listview, null, false);
        ExpandableListView mainlistview = (ExpandableListView) contentView
                .findViewById(R.id.main_expandablelistview);
        mainlistview.setAdapter(new ZDAreaChooseListView.MyAdapter());
        popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, 800, true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_item_color_bg));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupWindow = null;
            }
        });//@@12.20
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(this);
        mTextView.setOnClickListener(new OnClickListener() {//@@9.12
            @Override
            public void onClick(View v) {
                if (popupWindow == null) {
                    showPopWindow();
                } else {
                    closePopWindow();
                }
            }
        });
    }

    /**
     * 关闭下拉列表弹窗
     */
    public void closePopWindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
            WindowUtils.backgroundAlpha(mContext,1f);
        }
    }


    public void setItemsData(List<Area> parent, Map<String, List<Area>> map) {
        this.parent = parent;
        this.map = map;
    }

    public void setIfHavaChooseAll(boolean ifHavaChooseAll) {
        this.ifHavaChooseAll = ifHavaChooseAll;
    }

    /**
     * 数据适配器
     *
     * @author caizhiming
     */
    class MyAdapter extends BaseExpandableListAdapter {

        //得到子item需要关联的数据
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            String key = parent.get(groupPosition).getAreaName();
            return (map.get(key).get(childPosition));
        }

        //得到子item的ID
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        //设置子item的组件
        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parentView) {
            String key = parent.get(groupPosition).getAreaName();
            final Area info = map.get(key).get(childPosition);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.layout_children, null);
            }
            TextView tv = (TextView) convertView
                    .findViewById(R.id.second_textview);
            tv.setText(info.getAreaName());
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTextView.setText(info.getAreaName());
                    imageView.setVisibility(View.GONE);
                    if (clear_choice_isShow) {//@@9.12
                        clear_choice.setVisibility(View.VISIBLE);
                    }
                    if (onChildAreaChooceClickListener != null) {
                        onChildAreaChooceClickListener.OnChildClick(info);//@@9.12
                    }
                    choosed_area=info;
                    closePopWindow();
                }
            });
            return tv;
        }

        //获取当前父item下的子item的个数
        @Override
        public int getChildrenCount(int groupPosition) {
            String key = parent.get(groupPosition).getAreaName();
            if (map.get(key) == null) {
                return 0;
            } else {
                int size = map.get(key).size();
                return size;
            }
        }

        //获取当前父item的数据
        @Override
        public Object getGroup(int groupPosition) {
            return parent.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return parent.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        //设置父item组件
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parentView) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.layout_parent, null);
            }
            TextView tv = (TextView) convertView
                    .findViewById(R.id.parent_textview);
            ImageView iv = (ImageView) convertView
                    .findViewById(R.id.all_cheak);
            final Area info = parent.get(groupPosition);
            tv.setText(info.getAreaName());
            if (ifHavaChooseAll) {
                if (isExpanded) {
                    iv.setVisibility(VISIBLE);
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mTextView.setText(info.getAreaName());
                            imageView.setVisibility(View.GONE);
                            if (clear_choice_isShow) {//@@9.12
                                clear_choice.setVisibility(View.VISIBLE);
                            }
                            if (onChildAreaChooceClickListener != null) {
                                onChildAreaChooceClickListener.OnChildClick(info);
                            }
                            choosed_area=info;
                            closePopWindow();
                        }
                    });
                } else {
                    iv.setVisibility(GONE);
                }
            }
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }


    AreaChooceListView.OnChildAreaChooceClickListener onChildAreaChooceClickListener = null;//@@9.12

    public interface OnChildAreaChooceClickListener {
        void OnChildClick(Area info);
    }

    public void setOnChildAreaChooceClickListener(AreaChooceListView.OnChildAreaChooceClickListener o) {
        this.onChildAreaChooceClickListener = o;
    }


}
