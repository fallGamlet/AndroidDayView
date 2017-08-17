package ru.fallgamlet.dayview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class TimeLineView extends FrameLayout {

    //region Fields
    private List<Cluster> clusters = new ArrayList<>();
    private final List<String> hourTextList = new ArrayList<>(24);
    private float hourColWidth = 0;

    // Attributes
    private float attrHourHeight = 60;
    private int attrDisabledTimeColor = Color.parseColor("#22000000");
    private float attrHourLineWidth = 1;
    private float attrHourLinePadding = 0;
    private float attrHourLinePaddingLeft = attrHourLinePadding;
    private float attrHourLinePaddingRight = attrHourLinePadding;
    private int attrHourLineColor = Color.GRAY;
    private int attrHourTextColor = Color.DKGRAY;
    private float attrHourTextSize = 12;
    private float attrHourPadding = 8;
    private float attrHourPaddingRight = attrHourPadding;
    private float attrHourPaddingLeft = attrHourPadding;
    private int attrHourBackground = Color.TRANSPARENT;
    private int attrMinHour = 0;
    private int attrMaxHour = 24;

    // Paints
    protected Paint hourLinePaint;
    protected Paint hourTextPaint;
    protected Paint hourBgPaint;
    protected Paint disabledTimePaint;
    protected Paint coloredTimePaint;

    // Listeners
    IOnTimeSelectListener onTimeSelectListener;

    private GestureDetectorCompat gestureDetector;
    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (onTimeSelectListener != null) {
                float y = e.getY();
                int minute = getMinutesByPositionY(y);
                boolean checkDisabled = false;
                for (MinuteInterval disInterval : getDisabledTimes()) {
                    if (disInterval.isCollide(minute, minute)) {
                        checkDisabled = true;
                        break;
                    }
                }

                if (!checkDisabled) {
                    onTimeSelectListener.onTimePress(TimeLineView.this, minute);
                }
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onTimeSelectListener != null) {
                float y = e.getY();
                int minute = getMinutesByPositionY(y);
                boolean checkDisabled = false;
                for (MinuteInterval disInterval : getDisabledTimes()) {
                    if (disInterval.isCollide(minute, minute)) {
                        checkDisabled = true;
                        break;
                    }
                }
                if (!checkDisabled) {
                    onTimeSelectListener.onTimeLongPressed(TimeLineView.this, minute);
                }
            }
            super.onLongPress(e);
        }
    };
    //endregion

    //region Constructors
    public TimeLineView(Context context) {
        super(context);
        init(null, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    //endregion

    //region Getters and Setters
    public int getMinHour() { return attrMinHour; }
    public void setMinHour(int hour) {
        attrMinHour = hour;
        if (attrMaxHour < attrMinHour) { attrMaxHour = attrMinHour; }
        initMinimumHeight();
    }

    public int getMaxHour() { return attrMaxHour; }
    public void setMaxHour(int hour) {
        attrMaxHour = hour;
        if (attrMaxHour < attrMinHour) { attrMinHour = attrMaxHour; }
        initMinimumHeight();
    }

    public boolean setHourInterval(int minHour, int maxHour) {
        if (minHour > maxHour) {
            minHour = minHour + maxHour;
            maxHour = minHour - maxHour;
            minHour = minHour - maxHour;
        }
        attrMinHour = minHour;
        attrMaxHour = maxHour;
        initMinimumHeight();
        return true;
    }

    public float getHourHeight() {
        return attrHourHeight;
    }

    public void setHourHeight(float attrHourHeight) {
        this.attrHourHeight = attrHourHeight;
    }

    public float getHourLineWidth() {
        return attrHourLineWidth;
    }

    public void setHourLineWidth(float attrHourLineWidth) {
        this.attrHourLineWidth = attrHourLineWidth;
        if (hourLinePaint != null) {
            hourLinePaint.setStrokeWidth(attrHourLineWidth);
        }
    }

    public float getHourLinePadding() {
        return attrHourLinePadding;
    }

    public void setHourLinePadding(float padding) {
        attrHourLinePaddingLeft = attrHourLinePaddingRight = attrHourLinePadding = padding;
    }

    public float getHourLinePaddingLeft() {
        return attrHourLinePaddingLeft;
    }

    public void setHourLinePaddingLeft(float padding) {
        attrHourLinePaddingLeft = padding;
    }

    public float getHourLinePaddingRight() {
        return attrHourLinePaddingRight;
    }

    public void setHourLinePaddingRight(float padding) {
        attrHourLinePaddingRight = padding;
    }

    public int getDisabledTimeColor() {
        return attrDisabledTimeColor;
    }

    public void setDisabledTimeColor(int attrDisabledTimeColor) {
        this.attrDisabledTimeColor = attrDisabledTimeColor;
    }

    public int getHourLineColor() {
        return attrHourLineColor;
    }

    public void setHourLineColor(int attrHourLineColor) {
        this.attrHourLineColor = attrHourLineColor;
        if (hourLinePaint != null) {
            hourLinePaint.setColor(attrHourLineColor);
        }
    }

    public int getHourTextColor() {
        return attrHourTextColor;
    }

    public void setHourTextColor(int attrHourTextColor) {
        this.attrHourTextColor = attrHourTextColor;
        if (hourTextPaint != null) {
            hourTextPaint.setColor(attrHourTextColor);
        }
    }

    public float getHourTextSize() {
        return attrHourTextSize;
    }

    public void setHourTextSize(float attrHourTextSize) {
        this.attrHourTextSize = attrHourTextSize;
        if (hourTextPaint != null) {
            hourTextPaint.setTextSize(attrHourTextSize);
        }
    }

    public float getHourPadding() {
        return attrHourPadding;
    }

    public void setHourPadding(float attrHourPadding) {
        this.attrHourPaddingRight = this.attrHourPaddingLeft = this.attrHourPadding = attrHourPadding;
    }

    public float getHourPaddingRight() {
        return attrHourPaddingRight;
    }

    public void setHourPaddingRight(float attrHourPaddingRight) {
        this.attrHourPaddingRight = attrHourPaddingRight;
    }

    public float getHourPaddingLeft() {
        return attrHourPaddingLeft;
    }

    public void setHourPaddingLeft(float attrHourPaddingLeft) {
        this.attrHourPaddingLeft = attrHourPaddingLeft;
    }

    public int getHourBackground() {
        return attrHourBackground;
    }

    public void setHourBackground(int attrHourBackground) {
        this.attrHourBackground = attrHourBackground;
        if (hourBgPaint != null) {
            hourBgPaint.setColor(attrHourBackground);
        }
    }

    public void setOnTimeSelectListener(IOnTimeSelectListener listener) {
        this.onTimeSelectListener = listener;
    }
    //endregion

    //region Colored intervals
    private ArrayList<ColoredInterval> coloredIntervals;
    @NonNull
    protected ArrayList<ColoredInterval> getColoredIntervals() {
        if (coloredIntervals == null) {
            coloredIntervals = new ArrayList<>();
        }
        return coloredIntervals;
    }

    public ArrayList<ColoredInterval> getColoredIntervalsCopy() {
        return new ArrayList<>(getColoredIntervals());
    }

    public void addColoredInterval(ColoredInterval item) {
        if (item == null) { return; }
        List<ColoredInterval> intervals = getColoredIntervals();
        intervals.add(item);
        ColoredInterval.sort(intervals);
    }

    public <T extends ColoredInterval> void addColoredInterval(List<ColoredInterval> items) {
        if (items != null && !items.isEmpty()) {
            List<ColoredInterval> intervals = getColoredIntervals();
            for (ColoredInterval item : items) {
                intervals.add(item);
            }
            ColoredInterval.sort(intervals);
        }
    }

    /**
     * Remove color interval if exists
     * @param item
     * @return result of removing
     */
    public boolean removeColoredInterval(ColoredInterval item) {
        return getColoredIntervals().remove(item);
    }

    /**
     * remove all colored intervals by color
     * @param color Color for searching
     * @return count of deleted items
     */
    public int removeColoredInterval(int color) {
        int count = 0;
        List<ColoredInterval> items = getColoredIntervals();
        if (items.isEmpty()) { return count; }

        for (int i=items.size()-1; i>=0; i--) {
            ColoredInterval ci = items.get(i);
            if (ci.getColor() == color) {
                items.remove(i);
                count++;
            }
        }
        return count;
    }

    /**
     * Remove all colored intervals that collide with the parameter interval
     * @param interval Minute interval
     * @return count of deleted items
     */
    public int removeColoredInterval(MinuteInterval interval) {
        int count = 0;
        if (interval == null) { return count; }
        List<ColoredInterval> items = getColoredIntervals();
        if (items.isEmpty()) { return count; }

        for (int i=items.size()-1; i>=0; i--) {
            ColoredInterval ci = items.get(i);
            if (interval.isCollide(ci.getInterval())) {
                items.remove(i);
                count++;
            }
        }
        return count;
    }

    public int removeAllColoredIntervals() {
        List<ColoredInterval> intervals = getColoredIntervals();
        int count = intervals.size();
        intervals.clear();
        return count;
    }
    //endregion

    //region Disabled intervals
    private ArrayList<MinuteInterval> disabledTimes;

    @NonNull
    public ArrayList<MinuteInterval> getDisabledTimes() {
        if (disabledTimes == null) {
            disabledTimes = new ArrayList<>();
        }
        return disabledTimes;
    }
    //endregion

    //region Init methods
    private void init(AttributeSet attrs, int defStyle) {
        initAttributes(attrs, defStyle);
        initGestureDetector();
        initPaints();
        initHourTextList();
        initMinimumHeight();
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimeLineView, defStyle, 0);
        DisplayMetrics dm = getResources().getDisplayMetrics();

        try {
            setMinHour(a.getInt(R.styleable.TimeLineView_hourMin, attrMinHour));
            setMaxHour(a.getInt(R.styleable.TimeLineView_hourMax, attrMaxHour));
            attrHourHeight = a.getDimension(R.styleable.TimeLineView_hourHeight, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourHeight, dm));
            attrHourLineWidth = a.getDimension(R.styleable.TimeLineView_hourLineWidth, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourLineWidth, dm));
            attrHourLineColor = a.getColor(R.styleable.TimeLineView_hourLineColor, attrHourLineColor);
            attrHourBackground = a.getColor(R.styleable.TimeLineView_hourBackground, attrHourBackground);
            attrHourTextColor = a.getColor(R.styleable.TimeLineView_hourTextColor, attrHourTextColor);
            attrHourTextSize = a.getDimension(R.styleable.TimeLineView_hourTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourTextSize, dm));
            attrDisabledTimeColor = a.getColor(R.styleable.TimeLineView_disabledTimeColor, attrDisabledTimeColor);

            attrHourLinePadding = a.getDimension(R.styleable.TimeLineView_hourLinePadding, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourLinePadding, dm));
            attrHourLinePaddingLeft = attrHourLinePaddingRight = attrHourLinePadding;
            attrHourLinePaddingLeft = a.getDimension(R.styleable.TimeLineView_hourLinePaddingLeft, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourLinePaddingLeft, dm));
            attrHourLinePaddingRight = a.getDimension(R.styleable.TimeLineView_hourLinePaddingRight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourLinePaddingRight, dm));

            attrHourPadding = a.getDimension(R.styleable.TimeLineView_hourPadding, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPadding, dm));
            attrHourPaddingLeft = attrHourPaddingRight = attrHourPadding;
            attrHourPaddingLeft = a.getDimension(R.styleable.TimeLineView_hourPaddingLeft, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPaddingLeft, dm));
            attrHourPaddingRight = a.getDimension(R.styleable.TimeLineView_hourPaddingRight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPaddingRight, dm));
        } finally {
            a.recycle();
        }
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
    }

    private void initPaints() {
        hourLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourLinePaint.setStrokeWidth(attrHourLineWidth);
        hourLinePaint.setColor(attrHourLineColor);

        hourTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourTextPaint.setColor(attrHourTextColor);
        hourTextPaint.setTextSize(attrHourTextSize);
        hourTextPaint.setTextAlign(Paint.Align.LEFT);

        hourBgPaint = new Paint();
        hourBgPaint.setColor(attrHourBackground);

        disabledTimePaint = new Paint();
        disabledTimePaint.setColor(attrDisabledTimeColor);

        coloredTimePaint = new Paint();
        coloredTimePaint.setColor(attrDisabledTimeColor);
    }

    public void setAttributes(AttributeSet attrs, int defStyle) {
        init(attrs, defStyle);
    }

    public void initMinimumHeight() {
        int hours = attrMaxHour - attrMinHour;
        setMinimumHeight(hours * (int) attrHourHeight + getPaddingTop() + getPaddingBottom());
    }

    private void initHourTextList() {
        hourTextList.clear();
        for (int i=0; i<24; i++) {
            hourTextList.add(getHourText(i));
        }
        hourColWidth = hourTextPaint.measureText(getCachedHourText(0)) + attrHourPaddingLeft + attrHourPaddingRight;
    }
    //endregion

    //region Hour methods
    public String getHourText(int hour) {
        return String.format(Locale.getDefault(), "%02d:00", hour);
    }

    public String getCachedHourText(int hour) {
        hour = ((hour%24)+24)%24;
        return hourTextList.get(hour);
    }
    //endregion

    //region Draw methods
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        onDrawDisabledIntervals(canvas);
        onDrawColoredIntervals(canvas);
        onDrawHourLinesAndText(canvas);
    }

    protected void onDrawHourLinesAndText(Canvas canvas) {
        int padLeft = getPaddingLeft();
        float padHourLeft = padLeft + attrHourPaddingLeft;
        float hourColRight = getStartX();
        float hourLineLeft = hourColRight + attrHourLinePaddingLeft;
        float hourLineRight = getEndX() - attrHourLinePaddingRight;

        float textShiftY = -((hourTextPaint.descent() + hourTextPaint.ascent()) / 2);

        if (hourBgPaint.getColor() != Color.TRANSPARENT) {
            canvas.drawRect(
                    padLeft,
                    0,
                    hourColRight,
                    canvas.getHeight(),
                    hourBgPaint);
        }

        for (int hour = attrMinHour; hour<= attrMaxHour; hour++) {
            float y = getPositionByHours(hour);
            canvas.drawLine(hourLineLeft, y, hourLineRight, y, hourLinePaint);
            canvas.drawText(getCachedHourText(hour), padHourLeft, y + textShiftY, hourTextPaint);
        }
    }

    protected void onDrawDisabledIntervals(Canvas canvas) {
        float left = getStartX();
        float right = getEndX();
        int minMinute = attrMinHour * 60;
        int maxMinute = attrMaxHour * 60;
        disabledTimePaint.setColor(attrDisabledTimeColor);
        for (MinuteInterval interval: getDisabledTimes()) {
            if (interval.isValid() && interval.isCollide(minMinute, maxMinute)) {
                float yStart = getPositionYByMinutes(interval.getStart());
                float yEnd = getPositionYByMinutes(interval.getEnd());
                canvas.drawRect(left, yStart, right, yEnd, disabledTimePaint);
            }
        }
    }

    protected void onDrawColoredIntervals(Canvas canvas) {
        float left = getStartX();
        float right = getEndX();
        int minMinute = attrMinHour * 60;
        int maxMinute = attrMaxHour * 60;
        for (ColoredInterval ci: getColoredIntervals()) {
            MinuteInterval interval = ci.getInterval();
            if (interval.isValid() && interval.isCollide(minMinute, maxMinute)) {
                float yStart = getPositionYByMinutes(interval.getStart());
                float yEnd = getPositionYByMinutes(interval.getEnd());
                coloredTimePaint.setColor(ci.getColor());
                canvas.drawRect(left, yStart, right, yEnd, coloredTimePaint);
            }
        }
    }
    //endregion

    //region Override view methods
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureEventsChange();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void measureEventsChange() {
        for (Cluster cluster: clusters) {
            measureEventViews(cluster);
        }
    }

    private void measureEventViews(@NonNull Cluster cluster) {
        if (cluster.isEmpty()) {
            return;
        }

        int count = cluster.getColumns();
        float offsetLeft = getStartX();
        float width = getEndX() - offsetLeft;
        float itemWidth = width / count;

        for (List<IEventHolder> column : cluster.columns) {
            measureColumn(column, itemWidth, attrHourHeight);
        }
    }

    private void measureColumn(List<IEventHolder> column, float itemWidth, float hourHeight) {
        int height;
        for (IEventHolder item: column) {
            View view = item.getView();
            if (view != null && view.getVisibility() != GONE) {
                try {
                    float hoursStart = item.getTimeInterval().getStart() / 60.0f;
                    float hoursEnd = item.getTimeInterval().getEnd() / 60.0f;
                    height = (int)((hoursEnd-hoursStart)*hourHeight);

                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    if (lp == null) {
                        lp = new LayoutParams((int)itemWidth, height);
                    } else {
                        lp.width = (int) itemWidth;
                        lp.height = height;
                    }

                    view.setLayoutParams(lp);
                    view.measure((int)itemWidth, height);
                } catch (Exception ignored) {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        invalidate();
        layoutEventsChange();
    }

    public void layoutEventsChange() {
        for (Cluster cluster: clusters) {
            layoutEventViews(cluster);
        }
    }

    private void layoutEventViews(@NonNull Cluster cluster) {
        if (cluster.isEmpty()) {
            return;
        }

        int count = cluster.getColumns();
        int colNum = 0;
        float offsetLeft = getStartX();
        float width = getEndX() - offsetLeft;
        float offsetTop = getPositionByHours(0);
        float itemWidth = width / count;

        for (List<IEventHolder> column : cluster.columns) {
            layoutColumn(column, colNum, itemWidth, offsetLeft, offsetTop, attrHourHeight);
            colNum++;
        }
    }

    private void layoutColumn(List<IEventHolder> column, int colNum, float itemWidth, float offsetLeft, float offsetTop, float hourHeight) {
        int left = (int)(colNum*itemWidth + offsetLeft);
        int right = (int) (left + itemWidth);
        int top, bottom;
        for (IEventHolder item: column) {
            View view = item.getView();
            if (view != null && view.getVisibility() != GONE) {
                try {
                    float hoursStart = item.getTimeInterval().getStart() / 60.0f;
                    float hoursEnd = item.getTimeInterval().getEnd() / 60.0f;
                    top = (int) (offsetTop + hoursStart*hourHeight);
                    bottom = top + (int)((hoursEnd-hoursStart)*hourHeight);
                    layoutView(view, left, top, right, bottom);
                } catch (Exception ignored) {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    private void layoutView(View view, int left, int top, int right, int bottom) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp != null) {
            left += lp.leftMargin;
            top += lp.topMargin;
            right -= lp.rightMargin;
            bottom -= lp.bottomMargin;
        }

        view.layout(left, top, right, bottom);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event); //super.onTouchEvent(event);
    }

    public <T extends IEventHolder> boolean add(T holder) {
        if (holder == null || holder.getTimeInterval() == null || !holder.getTimeInterval().isValid()) {
            return false;
        }
        List<Cluster> collideClusters = new ArrayList<>();
        for (Cluster cluster: clusters) {
            if (cluster.isCollide(holder.getTimeInterval())) {
                collideClusters.add(cluster);
            }
        }

        Cluster cluster = null;
        if (collideClusters.isEmpty()) {
            cluster = new Cluster();
            cluster.add(holder);
            clusters.add(cluster);
        } else {
            if (collideClusters.size() == 1) {
                cluster = collideClusters.get(0);
                cluster.add(holder);
            } else {
                Cluster[] arr = clusters.toArray(new Cluster[collideClusters.size()]);
                cluster = Cluster.unionClusters(arr);
                cluster.add(holder);
                clusters.removeAll(collideClusters);
                clusters.add(cluster);
            }
        }

        View view = holder.getView();
        this.addView(view);
        return true;
    }

    public void clearEvents() {
        clusters.clear();
        removeAllViews();
    }

    public <T extends IEventHolder> void  setData(List<T> holders) {
        clearEvents();
        if (holders == null || holders.isEmpty()) {
            return;
        }
        Collections.sort(holders, new Comparator<IEventHolder>() {
            @Override
            public int compare(IEventHolder holder1, IEventHolder holder2) {
                if (holder1 == holder2) { return 0; }
                if (holder1 == null) {  return -1; }
                if (holder2 == null) { return 1; }
                MinuteInterval ti1 = holder1.getTimeInterval();
                MinuteInterval ti2 = holder2.getTimeInterval();
                if (ti1 == ti2) { return 0; }
                if (ti1 == null) {  return -1; }
                if (ti2 == null) { return 1; }
                return ti1.getStart() - ti2.getStart();
            }
        });

        for (IEventHolder holder: holders) {
            add(holder);
        }
    }
    //endregion

    //region Math positions and time methods
    public float getPositionByHours(float hour) {
        return attrHourHeight * (hour - attrMinHour) + getPaddingTop();
    }

    public float getPositionYByMinutes(int minutes) {
        return getPositionByHours(minutes/60.0f);
    }

    public float getHoursByPosition(float pos) {
        return (pos - getPaddingTop()) / attrHourHeight + attrMinHour;
    }

    public int getMinutesByPositionY(float pos) {
        return (int)(60 * getHoursByPosition(pos));
    }

    public float getStartX() {
        return getPaddingLeft() + hourColWidth + attrHourPaddingLeft;
    }

    public float getEndX() {
        return getWidth() - getPaddingRight();
    }
    //endregion

}
