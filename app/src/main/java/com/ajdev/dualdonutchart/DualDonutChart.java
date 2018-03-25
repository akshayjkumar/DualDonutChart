package com.ajdev.dualdonutchart;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.Locale;

/**
 * Created by Akshay.Jayakumar on 3/25/2018.
 */

public class DualDonutChart extends View {

    /**
     * Static members to align each arcs and to specify stroke width of the pain
     */
    private static final int ARC_BASE_OFFSET = 10;
    private static final int ARC_POSITIVE_OFFSET = 20;
    private static final int ARC_NEGATIVE_OFFSET = 30;
    private static final int ARC_STROKE_WIDTH = 35;
    private static final int MAX_SWEEP_ANGLE = 240;
    private static final int DEFAULT_START_ANGLE = 150;
    private static final int DEFAULT_START_ANGLE_NEGATIVE = 30;
    private static final int DEFAULT_ARC_ANGLE = 360;

    /**
     * Hold the context
     */
    private Context context;

    /**
     * Locale direction
     */
    private boolean LTR = true;

    /**
     * Dimensions of this view
     */
    private int viewW = 0;
    private int viewH = 0;
    /**
     * Paint objects for drawing the arc
     */
    private Paint paintPositive;
    private Paint paintPositiveShadow;
    private Paint paintNegative;
    private Paint paintNegativeShadow;
    private Paint paintBase;
    private TextPaint textPaint;
    private TextPaint titlePaint;
    /**
     * paintArcMask is used as erase paint object to remove previously drawn
     * objects in the canvas.
     */
    private Paint paintArcMask;
    /**
     * Rectangular bounds used to draw individual arcs
     */
    private RectF rectFPositive;
    private RectF rectFNegative;
    private RectF rectFBase;
    private RectF rectFClearSheet;
    /**
     * Specify start and sweep angles for Positive Arc
     */
    private float arcPositiveStart = DEFAULT_START_ANGLE;
    private float arcPositiveSweep = 0;
    /**
     * Specify start and sweep angles for Negative Arc
     */
    private float arcNegativeStart = DEFAULT_START_ANGLE_NEGATIVE;
    private float arcNegativeSweep = 0;
    /**
     * Specify start and sweep angles for Base Arc
     */
    private float arcBaseStart = DEFAULT_START_ANGLE;
    private float arcBaseSweep = MAX_SWEEP_ANGLE;
    /**
     * Position for legend text
     */
    private int textX = 0;
    private int textY = 0;
    private String legend = "";
    /**
     * Position for legend text
     */
    private int titleX = 0;
    private int titleY = 0;
    /**
     * Initial drawings are performed on the interim Canvas which in turn is written into a bitmap
     */
    private Canvas intrimCanvas;
    private Paint paintPrimary;
    private Bitmap masterBmp;

    /**
     * Custom animation listener interface to notify the host when animation is complete
     */
    private AnimationListener listener;

    /**
     *
     * Attributes defining Chart behaviour
     * Base color used by the base arc, positive color used by the positive arc and negative color
     * used by the negative arc. Light colors are used by gradient.
     *
     */
    private int baseColor;
    private int positiveColor;
    private int negativeColor;
    private int positiveColorLight;
    private int negativeColorLight;
    /**
     * Text color used by the legend and chart name.
     */
    private int textColor;
    private String title = "";

    /**
     *  Value animator will animate drawing arc on the canvas.
     *  ValueAnimator can perform drawing frame/frame which otherwise would
     *  have to be done manually.
     */
    private ValueAnimator positiveArcAnimation;
    private ValueAnimator negativeArcAnimation;
    private ValueAnimator endArcAnimation;

    public int getPositiveValue() {
        return positiveValue;
    }

    public void setPositiveValue(int positiveValue) {
        this.positiveValue = positiveValue;
    }

    public int getNegativeValue() {
        return negativeValue;
    }

    public void setNegativeValue(int negativeValue) {
        this.negativeValue = negativeValue;
    }

    public void setValues(int positive, int negative, boolean animation) {
        this.positiveValue = positive;
        this.negativeValue = negative;
        if(!animation){
            if(LTR)
                drawChartLTR();
            else
                drawChartRTL();
        }else {
            if(LTR)
                commenceAnimationLTR();
            else
                commenceAnimationRTL();
        }
    }

    private int positiveValue;
    private int negativeValue;
    private boolean animation = true;

    public DualDonutChart(Context context) {
        super(context);
        this.context = context;
        this.setLayerType(LAYER_TYPE_SOFTWARE,null);
    }

    public DualDonutChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        /**
         * Fetch the attributes from the view definition in the layout
         */
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DualDonutChart, 0, 0);
        this.baseColor = ta.getColor(R.styleable.DualDonutChart_baseColor, 0);
        this.positiveColor = ta.getColor(R.styleable.DualDonutChart_positiveColor, 0);
        this.positiveColorLight = ta.getColor(R.styleable.DualDonutChart_positiveColorLight, 0);
        this.negativeColor = ta.getColor(R.styleable.DualDonutChart_negativeColor, 0);
        this.negativeColorLight = ta.getColor(R.styleable.DualDonutChart_negativeColorLight, 0);
        this.textColor = ta.getColor(R.styleable.DualDonutChart_textColor, 0);
        this.title = ta.getString(R.styleable.DualDonutChart_title);
        this.positiveValue = ta.getInteger(R.styleable.DualDonutChart_positiveValue, 0);
        this.negativeValue = ta.getInteger(R.styleable.DualDonutChart_negativeValue, 0);
        this.animation = ta.getBoolean(R.styleable.DualDonutChart_animation, true);
        this.LTR = ta.getBoolean(R.styleable.DualDonutChart_ltr, true);
        /**
         * Ensure to recycle the TyperArray to free memory
         */
        ta.recycle();
        this.setLayerType(LAYER_TYPE_SOFTWARE,null);
    }

    public DualDonutChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        /**
         * Fetch the attributes from the view definition in the layout
         */
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DualDonutChart, 0, 0);
        this.baseColor = ta.getColor(R.styleable.DualDonutChart_baseColor, 0);
        this.positiveColor = ta.getColor(R.styleable.DualDonutChart_positiveColor, 0);
        this.positiveColorLight = ta.getColor(R.styleable.DualDonutChart_positiveColorLight, 0);
        this.negativeColor = ta.getColor(R.styleable.DualDonutChart_negativeColor, 0);
        this.negativeColorLight = ta.getColor(R.styleable.DualDonutChart_negativeColorLight, 0);
        this.textColor = ta.getColor(R.styleable.DualDonutChart_textColor, 0);
        this.title = ta.getString(R.styleable.DualDonutChart_title);
        this.positiveValue = ta.getInteger(R.styleable.DualDonutChart_positiveValue, 0);
        this.negativeValue = ta.getInteger(R.styleable.DualDonutChart_negativeValue, 0);
        this.animation = ta.getBoolean(R.styleable.DualDonutChart_animation, true);
        this.LTR = ta.getBoolean(R.styleable.DualDonutChart_ltr, true);
        /**
         * Ensure to recycle the TyperArray to free memory
         */
        ta.recycle();
        this.setLayerType(LAYER_TYPE_SOFTWARE,null);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /**
         * Get dimensions of the view when inflated in the layout
         */
        viewW = getMeasuredWidth();
        viewH = getMeasuredHeight();
        /**
         * Initialize the drawing canvas, bitmaps and all paint objects
         */
        init();
        /**
         * Setup up the base arc drawing bounds
         */
        setupBase(ARC_BASE_OFFSET);
        /**
         * Setup up the positive arc drawing bounds
         */
        setupPositiveArc(ARC_POSITIVE_OFFSET);
        /**
         * Setup up the negative arc drawing bounds
         */
        setupNegativeArc(ARC_NEGATIVE_OFFSET);
        /**
         * Setup up the clear sheet arc
         */
        setupClearSheetRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         *
         * ************ CAUTION CAUTION CAUTION CAUTION *****************
         *
         * ****** onDraw() method should perform the simplest task ******
         * ****** This method shouldn't contain and initializations *****
         * ****** nor it should contain memory allocations **************
         *
         */

        /**
         * Prior to drawing on the canvas the master bitmap should be erased since
         * it will retain previous drawing.
         */
        masterBmp.eraseColor(Color.TRANSPARENT);
        /**
         * Any drawing on the canvas should follow an order as it is drawn on top of another one.
         * Base arc should form the bottom most or the first one to get drawn on the canvas.
         */
        intrimCanvas.drawArc(rectFBase, arcBaseStart, arcBaseSweep, false, paintBase);

        if(LTR) {
            /**
             * Negative arc occupies the second position in the layer
             */
            intrimCanvas.drawArc(rectFNegative, arcNegativeStart, arcNegativeSweep, false, paintNegativeShadow);
            intrimCanvas.drawArc(rectFNegative, arcNegativeStart, arcNegativeSweep, false, paintNegative);
            /**
             * Third position is occupied by the positive arc as it should sit on top of the negative arc
             */
            intrimCanvas.drawArc(rectFPositive, arcPositiveStart, arcPositiveSweep - 0.4f, false, paintPositiveShadow);
            intrimCanvas.drawArc(rectFPositive, arcPositiveStart, arcPositiveSweep, false, paintPositive);
        }else{
            /**
             * Inverse the drawing in RTL
             * As always, second in the layer is negative arc
             **/
            intrimCanvas.drawArc(rectFNegative, arcPositiveStart, arcNegativeSweep, false, paintNegativeShadow);
            intrimCanvas.drawArc(rectFNegative, arcPositiveStart, arcNegativeSweep, false, paintNegative);
            /**
             * Third in the layer is positive arc
             *
             **/
            intrimCanvas.drawArc(rectFPositive, arcNegativeStart, arcPositiveSweep - 0.4f, false, paintPositiveShadow);
            intrimCanvas.drawArc(rectFPositive, arcNegativeStart, arcPositiveSweep, false, paintPositive);
        }
        /**
         * Draw the legend text onto the canvas
         */
        intrimCanvas.drawText(legend, textX, textY, textPaint);
        /**
         * Draw the view title under the legend text in the canvas
         */
        intrimCanvas.drawText(title, titleX, titleY, titlePaint);

        /**
         * Draw bottom clear sheet
         */
        intrimCanvas.drawRect(rectFClearSheet,paintArcMask);
        /**
         * Draw on to the canvas
         */
        canvas.drawBitmap(masterBmp, 0, 0, paintPrimary);

    }

    private void init() {
        /**
         * Master canvas and bitmap object that holds the drawing.
         * Drawing can be directly performed on the canvas associated with this view.
         * However a secondary canvas can be used to have better control on what is drawn.
         * For eg: supporting PorterDuffXfermode on primary canvas will not work properly
         * if the view is designed to be transparent.
         * This secondary canvas is written to the bitmap and that is in turn drawn on the
         * views canvas.
         */
        masterBmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        intrimCanvas = new Canvas(masterBmp);
        paintPrimary = new Paint();


        /**
         * Gradient colors for positive and negative arcs
         */
        int[] colorsPositive = {positiveColorLight, positiveColor};
        int[] colorsNegative = {negativeColorLight, negativeColor};
        float[] positionsPositive = {0f, 0.2f};
        float[] positionsNegative = {0.0f, 0.3f};
        SweepGradient gradientPositive = new SweepGradient(10,10, colorsPositive , positionsPositive);
        SweepGradient gradientNegative = new SweepGradient(0,10, colorsNegative , positionsNegative);

        /**
         * Paint used to draw the positive arc of this view.
         * Color of the paint is specified through xml.
         */
        paintPositive = new Paint();
        paintPositive.setColor(positiveColor);
        paintPositive.setStrokeWidth(convertToDPI(context,ARC_STROKE_WIDTH));
        paintPositive.setStyle(Paint.Style.STROKE);
        paintPositive.setAntiAlias(true);
        paintPositive.setStrokeCap(Paint.Cap.BUTT);
        paintPositive.setShader(gradientPositive);

        /**
         * ShadowLayer will provide shadow of desired value underneath the positive arc
         */
        paintPositiveShadow = new Paint();
        paintPositiveShadow.setColor(positiveColor);
        paintPositiveShadow.setShadowLayer(10.0f, 0.0f, 2.0f, 0xAB000000);
        paintPositiveShadow.setStrokeWidth(convertToDPI(context,ARC_STROKE_WIDTH));
        paintPositiveShadow.setStyle(Paint.Style.STROKE);
        paintPositiveShadow.setAntiAlias(true);
        paintPositiveShadow.setStrokeCap(Paint.Cap.BUTT);

        /**
         * Paint used to draw the negative arc of this view.
         * Color of the paint is specified through xml.
         * ShadowLayer will provide shadow of desired value underneath the arc
         */
        paintNegative = new Paint();
        paintNegative.setColor(negativeColor);
        paintNegative.setStrokeWidth(convertToDPI(context,ARC_STROKE_WIDTH));
        paintNegative.setStyle(Paint.Style.STROKE);
        paintNegative.setAntiAlias(true);
        paintNegative.setStrokeCap(Paint.Cap.BUTT);
        paintNegative.setShader(gradientNegative);

        /**
         * ShadowLayer will provide shadow of desired value underneath the positive arc
         */
        paintNegativeShadow = new Paint();
        paintNegativeShadow.setColor(negativeColor);
        paintNegativeShadow.setShadowLayer(10.0f, 0.0f, 2.0f, 0xAB000000);
        paintNegativeShadow.setStrokeWidth(convertToDPI(context,ARC_STROKE_WIDTH));
        paintNegativeShadow.setStyle(Paint.Style.STROKE);
        paintNegativeShadow.setAntiAlias(true);
        paintNegativeShadow.setStrokeCap(Paint.Cap.BUTT);


        /**
         * Paint used to draw the base arc of this view.
         * Color of the paint is specified through xml.
         * ShadowLayer will provide shadow of desired value underneath the arc
         */
        paintBase = new Paint();
        paintBase.setColor((baseColor == 0) ? Color.WHITE : baseColor);
        paintBase.setStrokeWidth(convertToDPI(context,ARC_STROKE_WIDTH));
        paintBase.setStyle(Paint.Style.STROKE);
        paintBase.setAntiAlias(true);
        paintBase.setStrokeCap(Paint.Cap.BUTT);

        /**
         * TextPaint used to draw legend text on this view
         * Color of the paint is specified through xml.
         * ShadowLayer will provide shadow of desired value underneath the arc
         */
        textPaint = new TextPaint();
        textPaint.setColor((textColor == 0) ? Color.WHITE : textColor);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(convertToDPI(context,38));
        textPaint.setShadowLayer(15.0f, 0.0f, 2.0f, 0xAB000000);

        /**
         * TextPaint used to draw title text on this view
         * Color of the paint is specified through xml.
         * Title text is also specified as xml attributes.
         * ShadowLayer will provide shadow of desired value underneath the arc.
         */
        titlePaint = new TextPaint();
        titlePaint.setColor((textColor == 0) ? Color.WHITE : textColor);
        titlePaint.setAntiAlias(true);
        titlePaint.setTextSize(convertToDPI(context,20));
        titlePaint.setShadowLayer(15.0f, 0.0f, 2.0f, 0xAB000000);

        /**
         * Paint used to remove anything drawn on the canvas
         * Color of the paint is set to be transparent which when drawn over any previously
         * drawn objects, will give an illusion of being erased as it is transparent paint.
         * A larger stroke width will paint a large area as required
         */
        paintArcMask = new Paint();
        paintArcMask.setStrokeWidth(convertToDPI(context,ARC_STROKE_WIDTH));
        paintArcMask.setStyle(Paint.Style.STROKE);
        paintArcMask.setAntiAlias(true);
        paintArcMask.setStrokeCap(Paint.Cap.ROUND);
        paintArcMask.setColor(ResourcesCompat.getColor(getResources(),android.R.color.transparent,null));
        paintArcMask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        /**
         * Initialize text position for the title
         */
        Rect bounds = new Rect();
        titlePaint.getTextBounds(title, 0, title.length(), bounds);
        int height = bounds.height();
        int width = bounds.width();
        titleX = (viewW / 2) - ((int) width/2);
        titleY = (int) ((viewW / 2)+(height));

        /**
         * Initialize text position for the legend
         */
        legend =  String.format(Locale.ENGLISH,"%d",0);
        textX = (viewW / 2) - ((int) textPaint.measureText(legend)/2);
        textY = (int) ((viewW / 2) - (height))  ;

        setValues(positiveValue,negativeValue, animation);
    }

    public void commenceAnimationLTR(){
        if ((arcPositiveSweep >= 0) || (arcNegativeSweep >= 0)) {
            endAnimationLTR();
        } else {
            startAnimationLTR();
        }
    }

    public void commenceAnimationRTL(){
        if ((arcPositiveSweep >= 0) || (arcNegativeSweep >= 0)) {
            endAnimationRTL();
        } else {
            startAnimationRTL();
        }
    }


    public void startAnimationLTR() {
        if (animation) {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {

                        final float total = positiveValue + negativeValue;
                        float computeAngle, computeMaxPositiveSweep;
                        if (positiveValue <= 0)
                            computeAngle = (float) round(((float) negativeValue / total), 2);
                        else
                            computeAngle = (float) round(((float) positiveValue / total), 2);

                        final float angle = computeAngle;

                        int oldValue = (int) (angle * DEFAULT_ARC_ANGLE);
                        /**
                         *  Convert a range of numbers to another range
                         *  (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax - OldMin)) + NewMin
                         *  Convert angle from 0 - 360 to 0 - 200
                         *
                         */
                        if (positiveValue <= 0)
                            computeMaxPositiveSweep = positiveValue;
                        else
                            computeMaxPositiveSweep = (((oldValue - 0) * (MAX_SWEEP_ANGLE - 0)) / (DEFAULT_ARC_ANGLE - 0)) + 0;

                        final float maxPositiveSweep = computeMaxPositiveSweep;
                        final float maxNegativeSweep = MAX_SWEEP_ANGLE - maxPositiveSweep;
                        positiveArcAnimation = ValueAnimator.ofFloat(0.0f, angle);
                        positiveArcAnimation.setDuration(2000);
                        positiveArcAnimation.setInterpolator(new OvershootInterpolator());
                        positiveArcAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float instantV = (float) animation.getAnimatedValue();
                                int oldValue = (int) (instantV * DEFAULT_ARC_ANGLE);

                                if (positiveValue <= 0) {
                                    arcPositiveSweep = positiveValue;
                                    arcNegativeSweep = -1 * ((((oldValue) * (MAX_SWEEP_ANGLE - 0)) / (DEFAULT_ARC_ANGLE - 0)) + 0);
                                } else {
                                    arcPositiveSweep = (((oldValue) * (MAX_SWEEP_ANGLE - 0)) / (DEFAULT_ARC_ANGLE - 0)) + 0;
                                    arcNegativeSweep = -1 * ((((arcPositiveSweep - 0) * (maxNegativeSweep - 0)) / (maxPositiveSweep - 0)) + 0);
                                }

                                float rangeConv = ((((instantV - 0) * (total - 0)) / (angle - 0)) + 0);
                                legend = String.format(Locale.ENGLISH,"%d", (rangeConv <= total) ? (int) rangeConv : (int) total);
                                if (textPaint != null)
                                    textX = (viewW / 2) - ((int) textPaint.measureText(legend) / 2);
                                DualDonutChart.this.postInvalidate();
                            }
                        });

                        if (listener != null) {
                            positiveArcAnimation.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    listener.onAnimationStart();
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    listener.onAnimationEnd();
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                }
                            });
                        }
                        positiveArcAnimation.start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    public void startAnimationRTL() {
        if(animation) {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {

                        final float total = positiveValue + negativeValue;
                        float computeAngle, computeMaxNegativeSweep;
                        if(negativeValue <= 0)
                            computeAngle = (float) round(((float) positiveValue / total), 2);
                        else
                            computeAngle = (float) round(((float) negativeValue / total), 2);

                        final float angle = computeAngle;
                        int oldValue = (int) (angle * DEFAULT_ARC_ANGLE);
                        /**
                         *  Convert a range of numbers to another range
                         *  (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax - OldMin)) + NewMin
                         *  Convert angle from 0 - 360 to 0 - 200
                         *
                         */
                        if(negativeValue <= 0)
                            computeMaxNegativeSweep = negativeValue;
                        else
                            computeMaxNegativeSweep = (((oldValue - 0) * (MAX_SWEEP_ANGLE - 0)) / (DEFAULT_ARC_ANGLE - 0)) + 0;

                        final float maxNegativeSweep = computeMaxNegativeSweep;
                        final float maxPositiveSweep = MAX_SWEEP_ANGLE - maxNegativeSweep;
                        negativeArcAnimation = ValueAnimator.ofFloat(0.0f, angle);
                        negativeArcAnimation.setDuration(2000);
                        negativeArcAnimation.setInterpolator(new OvershootInterpolator());
                        negativeArcAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float instantV = (float) animation.getAnimatedValue();
                                int oldValue = (int) (instantV * DEFAULT_ARC_ANGLE);
                                if(negativeValue <= 0) {
                                    arcNegativeSweep = negativeValue;
                                    arcPositiveSweep = -1 * ((((oldValue) * (MAX_SWEEP_ANGLE - 0)) / (DEFAULT_ARC_ANGLE - 0)) + 0);
                                }else {
                                    arcNegativeSweep = (((oldValue) * (MAX_SWEEP_ANGLE - 0)) / (DEFAULT_ARC_ANGLE - 0)) + 0;
                                    arcPositiveSweep = -1 * ((((arcNegativeSweep - 0) * (maxPositiveSweep - 0)) / (maxNegativeSweep - 0)) + 0);
                                }
                                float rangeConv = ((((instantV - 0) * (total - 0)) / (angle - 0)) + 0);
                                legend = String.format(Locale.ENGLISH,"%d",(rangeConv <= total) ? (int) rangeConv : (int) total);
                                if(textPaint != null)
                                    textX = (viewW / 2) - ((int) textPaint.measureText(legend) / 2);
                                DualDonutChart.this.postInvalidate();
                            }
                        });

                        if(listener != null) {
                            negativeArcAnimation.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    listener.onAnimationStart();
                                }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    listener.onAnimationEnd();
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) {}
                                @Override
                                public void onAnimationRepeat(Animator animation) {}
                            });
                        }
                        negativeArcAnimation.start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Static draw Left to Right
     */
    public void drawChartLTR(){
        final float total = positiveValue + negativeValue;
        float computeAngle, computeMaxPositiveSweep;
        if (positiveValue <= 0)
            computeAngle = (float) round(((float) negativeValue / total), 2);
        else
            computeAngle = (float) round(((float) positiveValue / total), 2);
        final float angle = computeAngle;
        int oldValue = (int) (angle * DEFAULT_ARC_ANGLE);
        if (positiveValue <= 0)
            computeMaxPositiveSweep = positiveValue;
        else
            computeMaxPositiveSweep = (((oldValue - 0) * (MAX_SWEEP_ANGLE - 0)) / (DEFAULT_ARC_ANGLE - 0)) + 0;
        final float maxPositiveSweep = computeMaxPositiveSweep;
        final float maxNegativeSweep = MAX_SWEEP_ANGLE - maxPositiveSweep;
        arcPositiveSweep = maxPositiveSweep;
        arcNegativeSweep = -1 * maxNegativeSweep;
        legend = String.format(Locale.ENGLISH,"%d",(int)total);
        if(textPaint != null)
            textX = (viewW / 2) - ((int) textPaint.measureText(legend) / 2);
        invalidate();
    }

    public void drawChartRTL(){
        final float total = positiveValue + negativeValue;
        float computeAngle, computeMaxNegativeSweep;
        if(negativeValue <= 0)
            computeAngle = (float) round(((float) positiveValue / total), 2);
        else
            computeAngle = (float) round(((float) negativeValue / total), 2);
        final float angle = computeAngle;
        int oldValue = (int) (angle * DEFAULT_ARC_ANGLE);
        if(negativeValue <= 0)
            computeMaxNegativeSweep = negativeValue;
        else
            computeMaxNegativeSweep = (((oldValue - 0) * (MAX_SWEEP_ANGLE - 0)) / (DEFAULT_ARC_ANGLE - 0)) + 0;
        final float maxNegativeSweep = computeMaxNegativeSweep;
        final float maxPositiveSweep = MAX_SWEEP_ANGLE - maxNegativeSweep;
        arcPositiveSweep = -1 * maxPositiveSweep;
        arcNegativeSweep =  maxNegativeSweep;
        legend = String.format(Locale.ENGLISH,"%d",(int)total);
        if(textPaint != null)
            textX = (viewW / 2) - ((int) textPaint.measureText(legend) / 2);
        invalidate();
    }

    /**
     * Setup rectangular bounds for base arc. Position the base arc by a value
     * as provided in the arcOffset
     * @param arcOffset
     */
    public void setupBase(int arcOffset){
        float conArcOffset = convertToDPI(context,arcOffset);
        float left = (conArcOffset  + ARC_STROKE_WIDTH);
        float right = viewW - (conArcOffset  + ARC_STROKE_WIDTH);
        float top = (conArcOffset  + ARC_STROKE_WIDTH);
        float bottom = (viewW) - (conArcOffset  + ARC_STROKE_WIDTH);
        rectFBase = new RectF(left, top, right, bottom);
    }

    /**
     * Setup rectangular bounds for positive arc. Position the positive arc by a value
     * as provided in the arcOffset
     * @param arcOffset
     */
    public void setupPositiveArc(int arcOffset){
        float conArcOffset = convertToDPI(context,arcOffset);
        float left = (conArcOffset  + ARC_STROKE_WIDTH);
        float right = viewW - (conArcOffset  + ARC_STROKE_WIDTH);
        float top = (conArcOffset  + ARC_STROKE_WIDTH);
        float bottom = (viewW) - (conArcOffset  + ARC_STROKE_WIDTH);
        rectFPositive = new RectF(left, top, right, bottom);
    }

    /**
     * Setup rectangular bounds for negative arc. Position the negative arc by a value
     * as provided in the arcOffset
     * @param arcOffset
     */
    public void setupNegativeArc(int arcOffset){
        float conArcOffset = convertToDPI(context,arcOffset);
        float left = (conArcOffset  + ARC_STROKE_WIDTH);
        float right = viewW - (conArcOffset  + ARC_STROKE_WIDTH);
        float top = (conArcOffset  + ARC_STROKE_WIDTH);
        float bottom = (viewW) - (conArcOffset  + ARC_STROKE_WIDTH);
        rectFNegative = new RectF(left, top, right, bottom);
    }

    /**
     * Setup rectangular bounds for clear sheet.
     */
    public void setupClearSheetRect(){
        float conArcOffset = convertToDPI(context,ARC_BASE_OFFSET);
        Rect bounds = new Rect();
        titlePaint.getTextBounds(title, 0, title.length(), bounds);
        float left = 0;
        float right = viewW;
        float top = (float) (titleY + (bounds.height()*1.9));
        float bottom = viewH;
        rectFClearSheet = new RectF(left, top, right, bottom);
    }

    public static float convertToDPI(Context context, int dp){
        return  dp * context.getResources().getDisplayMetrics().density;
    }

    private static double round (float value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (float) Math.round(value * scale) / scale;
    }

    public static float getPixelEquivalent(Context context, int dp){
        DisplayMetrics dm = context.getResources().getDisplayMetrics() ;
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    public interface AnimationListener{
        public void onAnimationStart();
        public void onAnimationEnd();
    }

    public AnimationListener getAnimationListener() {
        return listener;
    }

    public void setAnimationListener(AnimationListener listener) {
        this.listener = listener;
    }

    public void endAnimationLTR() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if(legend == null && legend.isEmpty())
                        legend = "0";

                    //This might throw number format exception,
                    //so encapsulated in try catch
                    final float total = Integer.parseInt(legend.trim());

                    final float computeAngle;
                    final float tempPositiveSweep = arcPositiveSweep;
                    final float tempNegativeSweep = arcNegativeSweep;
                    if (arcPositiveSweep <= 0)
                        computeAngle = arcNegativeSweep;
                    else
                        computeAngle = arcPositiveSweep;

                    endArcAnimation = ValueAnimator.ofFloat(0.0f, computeAngle);
                    endArcAnimation.setDuration(2000);
                    endArcAnimation.setInterpolator(new DecelerateInterpolator());
                    endArcAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float instaSweep = (float) animation.getAnimatedValue();

                            if (arcPositiveSweep <= 0) {
                                arcNegativeSweep = tempNegativeSweep - instaSweep;
                            } else {
                                arcPositiveSweep = tempPositiveSweep - instaSweep;
                                arcNegativeSweep = -1 * (((((arcPositiveSweep - 0) * (tempNegativeSweep - 0)) / (tempNegativeSweep - 0)) + 0));
                            }

                            float rangeConv = total - ((((instaSweep - 0) * (total - 0)) / (computeAngle - 0)) + 0);
                            legend = String.format(Locale.ENGLISH,"%d", (total >= 0) ? (int) rangeConv : 0);
                            if (textPaint != null)
                                textX = (viewW / 2) - ((int) textPaint.measureText(legend) / 2);

                            DualDonutChart.this.postInvalidate();
                        }
                    });

                    if(listener != null) {
                        endArcAnimation.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                listener.onAnimationStart();
                            }
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                startAnimationLTR();
                            }
                            @Override
                            public void onAnimationCancel(Animator animation) {}
                            @Override
                            public void onAnimationRepeat(Animator animation) {}
                        });
                    }

                    endArcAnimation.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    public void endAnimationRTL() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {

                    //This might throw number format exception,
                    //so encapsulated in try catch
                    final float total = Integer.parseInt(legend.trim());

                    final float computeAngle;
                    final float tempPositiveSweep = arcPositiveSweep;
                    final float tempNegativeSweep = arcNegativeSweep;
                    if(arcNegativeSweep <= 0)
                        computeAngle = arcPositiveSweep;
                    else
                        computeAngle = arcNegativeSweep;

                    endArcAnimation = ValueAnimator.ofFloat(0.0f, computeAngle);
                    endArcAnimation.setDuration(2000);
                    endArcAnimation.setInterpolator(new DecelerateInterpolator());
                    endArcAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float instaSweep = (float) animation.getAnimatedValue();

                            if(arcNegativeSweep <= 0){
                                arcPositiveSweep = tempPositiveSweep - instaSweep;
                            }else {
                                arcNegativeSweep = tempNegativeSweep - instaSweep;
                                arcPositiveSweep =  -1 *  (((((arcNegativeSweep - 0) * (tempPositiveSweep - 0)) / (tempPositiveSweep - 0)) + 0));
                            }

                            float rangeConv = total - ((((instaSweep - 0) * (total - 0)) / (computeAngle - 0)) + 0);
                            legend = String.format(Locale.ENGLISH,"%d",(total >= 0) ? (int) rangeConv : 0);
                            if(textPaint != null)
                                textX = (viewW / 2) - ((int) textPaint.measureText(legend) / 2);

                            DualDonutChart.this.postInvalidate();
                        }
                    });

                    if(listener != null) {
                        endArcAnimation.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                listener.onAnimationStart();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                startAnimationRTL();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
                    }

                    endArcAnimation.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

}