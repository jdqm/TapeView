# 漂亮的卷尺效果

![漂亮的卷尺效果](https://raw.githubusercontent.com/jdqm/TapeView/master/tapeView.gif)

# 1、Usege

```
<com.jdqm.tapelibrary.TapeView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:maxValue="230"
    app:minValue="100"
    app:value="164" />
 ``` 
 
 也可以在Java代码中设置初始化参数，
 
 ```
 /**
   * 初始化配置参数
   * 
   * @param value 当前值
   * @param minValue 最小值
   * @param maxValue 最大值
   * @param per 每一隔所代表的值
   * @param perCount 相邻两条长刻度线之间被分成的隔数量
   */
  public void setValue(float value, float minValue, float maxValue, float per, int perCount)
 ```
 
# 2、支持哪些自定义属性

|name|说明|format|默认值|
|:--|:--|:--|:--:|
|bgColor|背景颜色|color|```#FBE40C```|
|calibrationColor|刻度线的颜色|color|```#FFFFFF```|
|calibrationWidth|刻度线的宽度|dimension|1dp|
|calibrationShort|短的刻度线的长度|dimension|20dp|
|calibrationLong|长的刻度线的长度|dimension|35dp|
|triangleColor|三角形指示器的颜色|color|```#FFFFFF```|
|triangleHeight|三角形的高度|dimension|18dp|
|textColor|刻度尺上数值字体颜色|color|```#FFFFFF```|
|textSize|刻度尺上数值字体大小|dimension|14sp|
|per|两个刻度之间的代表的数值|float|1|
|perCount|两条长的刻度线之间的per数量|integer|10|
|gapWidth|刻度之间的物理距离|dimension|10dp|
|minValue|刻度尺的最小值|float|0|
|maxValue|刻度之间的最大值|float|100|
|value|当前值|float|0|

# 3、实现原理分析博客
[实现原理http://www.jianshu.com/p/06e65ef3f3f1][1]

[1]: http://www.jianshu.com/p/06e65ef3f3f1
