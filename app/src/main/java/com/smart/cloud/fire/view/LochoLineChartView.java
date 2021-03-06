package com.smart.cloud.fire.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.smart.cloud.fire.global.TemperatureTime;
import com.smart.cloud.fire.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class LochoLineChartView extends LineChartView {

    public final static int TYPE_ELECTRIC = 200;//电气类型
    public final static int TYPE_CHUANAN = 201;//创安燃气类型
    public final static int TYPE_TEM = 202;//温度类型
    public final static int TYPE_HUM = 203;//湿度类型
    public final static int TYPE_GAS = 204;//燃气类型
    public final static int TYPE_WATER_PRESURE = 205;//水压类型
    public final static int TYPE_WATER_LEVEL = 206;//水位类型
    public final static int TYPE_WATER_PRESURE_WITH_MORE = 207;//水压类型（带选项）


    /*=========== 数据相关 ==========*/
    private LineChartData mLineData;                    //图表数据
    private int numberOfLines = 1;                      //图上折线/曲线的显示条数
    private int maxNumberOfLines = 4;                   //图上折线/曲线的最多条数
    private int numberOfPoints = 8;                    //图上的节点数

    /*=========== 状态相关 ==========*/
    private boolean isHasAxes = true;                   //是否显示坐标轴
    private boolean isHasAxesNames = true;              //是否显示坐标轴名称
    private boolean isHasLines = true;                  //是否显示折线/曲线
    private boolean isHasPoints = true;                 //是否显示线上的节点
    private boolean isFilled = true;                   //是否填充线下方区域
    private boolean isHasPointsLabels = false;          //是否显示节点上的标签信息
    private boolean isCubic = false;                    //是否是立体的
    private boolean isPointsHasSelected = false;        //设置节点点击后效果(消失/显示标签)
    private boolean isPointsHaveDifferentColor;         //节点是否有不同的颜色

    /*=========== 其他相关 ==========*/
    private ValueShape pointsShape = ValueShape.CIRCLE; //点的形状(圆/方/菱形)
    float[][] randomNumbersTab ; //将线上的点放在一个数组中
    private Map<Integer, String> data = new HashMap<>();
    private String electricType;
    private int isWater;

    public LochoLineChartView(Context context) {
        super(context);
    }

    public LochoLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LochoLineChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initChartView(String axisYtext,List<TemperatureTime.ElectricBean> list,String electricType,int isWater){
        numberOfPoints=list.size()+2;
        randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];
        this.electricType=electricType;
        this.isWater=isWater;
        initView();
        setPointsValues(list);
        setLinesDatas(axisYtext,list);
        resetViewport(list);
    }

    private void initView() {
        /**
         * 禁用视图重新计算 主要用于图表在变化时动态更改，不是重新计算
         * 类似于ListView中数据变化时，只需notifyDataSetChanged()，而不用重新setAdapter()
         */
        setViewportCalculationEnabled(false);
        setZoomEnabled(false);
    }

    /**
     * 设置曲线图内容
     * @param axisYtext 纵坐标标注
     * @param list 数据
     */
    private void setLinesDatas(String axisYtext,List<TemperatureTime.ElectricBean> list) {
        List<Line> lines = new ArrayList<>();
        ArrayList<AxisValue> axisValuesX = new ArrayList<>();
        //循环将每条线都设置成对应的属性
        for (int i = 0; i < numberOfLines; ++i) {
            //节点的值
            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < (list.size()+1); ++j) {
                if (j > 0 && j < (list.size()+1)) {
                    values.add(new PointValue(j, randomNumbersTab[i][j]));
                    axisValuesX.add(new AxisValue(j).setLabel(getTime(list.get(j-1).getElectricTime())));
                }
            }

            Line line = new Line(values);               //根据值来创建一条线
            line.setColor(ChartUtils.COLORS[i]);        //设置线的颜色
            line.setShape(pointsShape);                 //设置点的形状
            line.setHasLines(isHasLines);               //设置是否显示线
            line.setHasPoints(isHasPoints);             //设置是否显示节点
            line.setCubic(isCubic);                     //设置线是否立体或其他效果
            line.setFilled(isFilled);                   //设置是否填充线下方区域
            line.setHasLabels(isHasPointsLabels);       //设置是否显示节点标签
            //设置节点点击的效果
            line.setHasLabelsOnlyForSelected(isPointsHasSelected);
            //如果节点与线有不同颜色 则设置不同颜色
            if (isPointsHaveDifferentColor) {
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        Axis axisX = new Axis().setHasLines(true);                    //X轴
        Axis axisY = new Axis().setHasLines(true);  //Y轴          //设置名称
        axisY.setName(axisYtext);

        axisX.setTextColor(Color.GRAY);//X轴灰色
        axisX.setMaxLabelChars(3);
        axisX.setValues(axisValuesX);
        axisX.setHasTiltedLabels(true);//X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextSize(10);
        axisX.setInside(true);
        axisY.setTextColor(Color.GRAY);

        mLineData = new LineChartData(lines);                      //将所有的线加入线数据类中
        mLineData.setBaseValue(Float.NaN);
        mLineData.setAxisXBottom(axisX);            //设置X轴位置 下方
        mLineData.setAxisYLeft(axisY);
        mLineData.setValueLabelBackgroundColor(Color.BLUE);     //设置数据背景颜色
        mLineData.setValueLabelTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        //设置基准数(大概是数据范围)
        /* 其他的一些属性方法 可自行查看效果
         * mLineData.setValueLabelBackgroundAuto(true);            //设置数据背景是否跟随节点颜色
         * mLineData.setValueLabelBackgroundColor(Color.BLUE);     //设置数据背景颜色
         * mLineData.setValueLabelBackgroundEnabled(true);         //设置是否有数据背景
         * mLineData.setValueLabelsTextColor(Color.RED);           //设置数据文字颜色
         * mLineData.setValueLabelTextSize(15);                    //设置数据文字大小
         * mLineData.setValueLabelTypeface(Typeface.MONOSPACE);    //设置数据文字样式
         */

        setLineChartData(mLineData);    //设置图表控件
    }

    /**
     * 利用随机数设置每条线对应节点的值
     */
    private void setPointsValues(List<TemperatureTime.ElectricBean> list) {
        data.clear();
        for (int i = 0; i < maxNumberOfLines; ++i) {
            for (int j = 0; j < (list.size()+1); ++j) {
                if (j > 0 && j < list.size()+1) {
                    String str = list.get(j - 1).getElectricValue();
                    if (electricType.equals("7")) {
                        data.put(j, str);
                    }
                    float f = new BigDecimal(str).floatValue();
                    randomNumbersTab[i][j] = (f);
                }
            }
        }
    }

    /**
     * 重点方法，计算绘制图表
     */
    private void resetViewport(List<TemperatureTime.ElectricBean> tem) {
        //创建一个图标视图,大小为控件的最大大小
        float value=0;
        if(tem!=null&&tem.size()>0){
            value = Utils.getMaxFloat(tem)*1.5f;
        }
        final Viewport v = new Viewport(getMaximumViewport());
        v.left = 0;                             //坐标原点在左下
        v.bottom = 0;
        v.top = value;
        if(value==0){
            switch (electricType) {
                case "6":
                    v.top = 400;
                    break;
                case "7":
                    v.top = 50;
                    break;
                case "8":
                    v.top = 700;
                    break;
                case "9":
                    v.top = 80;
                    break;
                default:
                    v.top=100;
                    break;
            }
        }

        //最高点为100
        v.right = numberOfPoints - 1;           //右边为点 坐标从0开始 点号从1 需要 -1
        setMaximumViewport(v);   //给最大的视图设置 相当于原图
        setCurrentViewport(v);   //给当前的视图设置 相当于当前展示的图
    }


    private String getTime(String str) {
        if(str.length()<5){
            return "";
        }else{
            String strings = str.substring(5, str.length());
            return strings;
        }
    }
}
