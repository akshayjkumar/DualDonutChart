# Dual Donut Chart
Android chart widget plotting dual donuts to represent two sectors/ portions of a whole. Supports animations, works on both LTR and RTL layout directions, customization of colors, direction etc.

![Dual Donut](https://github.com/akshayjkumar/DualDonutChart/blob/master/dual_donut_chart.gif?raw=true)

# Usage
Include the view in the layout file, specify necessary dimensions and provide appropriate colors. Voila! 
```

<com.ajdev.dualdonutchart.DualDonutChart
     android:layout_width="250dp"
     android:layout_height="170dp"
     app:title="@string/chartTitle"
     app:baseColor="@color/colorRingBase"
     app:textColor="@color/colorChartText"
     app:positiveColor="@color/colorRingPrimary"
     app:positiveColorLight="@color/colorRingPrimaryLight"
     app:negativeColor="@color/colorRingSecondary"
     app:negativeColorLight="@color/colorRingSecondaryLight"
     app:positiveValue="200"
     app:negativeValue="150"
     app:animation="true"
     app:ltr="true"/>

```

# Attributes
## baseColor(Color)
Sets the color of the base ring.
## textColor(Color)
Sets the color of inner text.
## positiveColor(Color)
Specifies the primary color of left/ positive ring.
## positiveColorLight(Color)
Specifies the secondary color of the left/ positive ring Providing light color will enable gradient effect on the ring.
## negativeColor(Color)
Specifies the primary color of right/ negative ring.
## negativeColorLight(Color)
Specifies the secondary color of the right/ negative ring Providing light color will enable gradient effect on the ring.
## positiveValue(Number)
Provide a value to plot the left/ positive ring. Rings are ploted in propotion to the value it occupies in relation to the whole (Both positive and negative values).
## negativeValue(Number)
Provide a value to plot the right/ negative ring. Rings are ploted in propotion to the value it occupies in relation to the whole (Both positive and negative values).
## title(String)
Title text provided to the chart. Displayed in center of the two rings.
## animation(Boolean)
Enables/ disables animation while plotting the rings. Defaults to True.
## ltr(Boolean)
Specifies the layout direction. LTR if true and RTL otherwise. Defaults to True.

# APIs
## setValues(int positive, int negative, boolean animation)
setValues() helps to plot the chart dynamically. First and second params specifies the positive and negative values respectively. Third boolean parameter specifies if the chart animation is shown when values are changed dynamically.
```

DualDonutChart dualDonutChart = (DualDonutChart) findViewById(R.id.dualDonutChart);
Random r = new Random();
int low = 100, high = 500;
int positive = r.nextInt(high-low) + low;
int negative = r.nextInt(high-low) + low;
/**
* setValues() method of the DualDonutChart helps to dynamically
* change the chart values. The last boolean flag enables the animation.
*/
dualDonutChart.setValues(positive,negative,true);

```

# Events
## setAnimationListener(DualDonutChart.AnimationListener)
Callback events to listen to the chart animation states. onAnimationStart() is called when chart animation commences and onAnimationEnd() when all animations are completed.
```

dualDonutChart.setAnimationListener(new DualDonutChart.AnimationListener() {
    @Override
    public void onAnimationStart() {
        // Animation started
    }

    @Override
    public void onAnimationEnd() {
        // Animations completed
    }
});

```
