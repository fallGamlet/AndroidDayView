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
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TimeLineView extends FrameLayout {
    //region Sub classes and interfaces
    public static class MinuteInterval{
        private int start, end;

        //region Constructors
        public MinuteInterval() {
            setData(0,0);
        }

        public MinuteInterval(int start, int end) {
            setData(start, end);
        }
        //endregion

        //region Getters and Setters
        public int getStart() { return start; }

        public int getEnd() { return end; }
        //endregion

        //region Methods
        public void setData(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public boolean isValid() {
            return start < end;
        }

        public static int getCollide(int start1, int end1, int start2, int end2) {
            return (end1 - start2) * (end2 - start1);
        }

        public static int getCollide(@NonNull MinuteInterval mi1, @NonNull MinuteInterval mi2) {
            return getCollide(mi1.start, mi1.end, mi2.start, mi2.end);
        }

        public static boolean isCollide(int start1, int end1, int start2, int end2) {
            return getCollide(start1, end1, start2, end2) > 0;
        }

        public static boolean isCollide(MinuteInterval mi1, MinuteInterval mi2) {
            return !(mi1 == null || mi2 == null) && isCollide(mi1.start, mi1.end, mi2.start, mi2.end);
        }

        public boolean isCollide(int start, int end) {
            return isCollide(this.start, this.end, start, end);
        }

        public boolean isCollide(MinuteInterval mi) {
            return isCollide(this, mi);
        }

        public static MinuteInterval union(MinuteInterval mi1, MinuteInterval mi2) {
            if (mi1 == null) { return mi2; }
            if (mi2 == null) { return mi1; }
            return new MinuteInterval(
                    Math.min(mi1.start, mi2.start),
                    Math.max(mi1.end, mi2.end));
        }

        public static MinuteInterval intersection(MinuteInterval ti1, MinuteInterval ti2) {
            MinuteInterval interval = null;
            if (isCollide(ti1, ti2)) {
                interval = new MinuteInterval(
                        Math.max(ti1.start, ti2.start),
                        Math.min(ti1.end, ti2.end));
            }
            return interval;
        }

        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "%02d:%02d - %02d:%02d", start/60, start%60, end/60, end%60);
        }

        //endregion
    }

    public static class ColoredInterval {
        private int color;
        private MinuteInterval interval;

        public ColoredInterval() {
            setData(0, null);
        }

        public ColoredInterval(int color, MinuteInterval interval) {
            setData(color, interval);
        }

        //region Getters and Setters
        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public MinuteInterval getInterval() {
            return interval;
        }

        public void setInterval(MinuteInterval interval) {
            this.interval = interval;
        }
        //endregion

        //region Methods
        public void setData(int color, MinuteInterval interval) {
            this.color = color;
            this.interval = interval;
        }

        public static void sort(List<? extends ColoredInterval> list) {
            Collections.sort(list, new Comparator<ColoredInterval>() {
                @Override
                public int compare(ColoredInterval ci1, ColoredInterval ci2) {
                    if (ci1 == ci2) { return 0; }
                    if (ci1 == null) { return -1; }
                    if (ci2 == null) { return 1; }

                    MinuteInterval mi1 = ci1.getInterval();
                    MinuteInterval mi2 = ci1.getInterval();

                    if (mi1 == mi2) { return 0; }
                    if (mi1 == null) { return -1; }
                    if (mi2 == null) { return 1; }

                    return mi1.start - mi2.start;
                }
            });
        }
        //endregion
    }

    public interface IEventHolder {
        View getView();
        MinuteInterval getTimeInterval();
    }

    public interface IOnTimeSelectListener {
        void onTimePress(Object sender, int minute);
        void onTimeLongPressed(Object sender, int minute);
    }

    public static class Cluster {
        //region Fields
        MinuteInterval timeInterval;
        List<List<IEventHolder>> columns = new ArrayList<>();
        //endregion

        //region Constructor
        public Cluster() {
            timeInterval = null;
        }
        //endregion

        //region Methods
        public boolean isEmpty() {
            return columns.isEmpty();
        }

        public int getColumns() {
            return columns.size();
        }

        public boolean isCollide(MinuteInterval timeInterval) {
            return MinuteInterval.isCollide(this.timeInterval, timeInterval);
        }

        protected boolean isCollideForColumn(List<IEventHolder> column, IEventHolder holder) {
            if (holder == null || column == null || column.isEmpty()) { return false; }
            boolean check = false;
            for (IEventHolder item: column) {
                if (MinuteInterval.isCollide(item.getTimeInterval(), holder.getTimeInterval())) {
                    check = true;
                    break;
                }
            }
            return check;
        }

        protected int indexOfCollideColumn(IEventHolder holder) {
            int index = -1;
            if (holder != null && !isEmpty()) {
                int i=0;
                 for (List<IEventHolder> column: columns) {
                     if(isCollideForColumn(column, holder)) {
                         index = i;
                         break;
                     }
                     i++;
                 }
            }
            return index;
        }

        protected int indexOfNotCollideColumn(IEventHolder holder) {
            int index = -1;
            if (holder != null && !isEmpty()) {
                int i=0;
                for (List<IEventHolder> column: columns) {
                    if(!isCollideForColumn(column, holder)) {
                        index = i;
                        break;
                    }
                    i++;
                }
            }
            return index;
        }

        public MinuteInterval getTimeInterval() {
            return timeInterval;
        }

        public boolean add(IEventHolder holder) {
            if (holder == null || holder.getTimeInterval() == null) {
                return false;
            }
            if (isEmpty()) {
                timeInterval = holder.getTimeInterval();
                List<IEventHolder> column = new ArrayList<>();
                column.add(holder);
                columns.add(column);
                return true;
            }
            if (!isCollide(holder.getTimeInterval())) {
                return false;
            }

            MinuteInterval ti = MinuteInterval.union(timeInterval, holder.getTimeInterval());
            int i = indexOfNotCollideColumn(holder);
            if (i>=0) {
                columns.get(i).add(holder);
            } else {
                List<IEventHolder> column = new ArrayList<>();
                column.add(holder);
                columns.add(column);
            }
            this.timeInterval = ti;
            return true;
        }

        /* Without test for collide clusters
         */
        public static Cluster unionClusters(Cluster... clusters) {
            if (clusters == null || clusters.length == 0) {
                return null;
            }

            Cluster cluster = null;
            Cluster bufCluster = null;
            List<IEventHolder> holders = new ArrayList<>(100);
            MinuteInterval timeInterval = null;
            for (int i=0; i<clusters.length; i++) {
                bufCluster = clusters[i];
                if (bufCluster != null && !bufCluster.isEmpty()) {
                    timeInterval = MinuteInterval.union(timeInterval, bufCluster.getTimeInterval());
                    holders.addAll(bufCluster.getAllItems());
                }
            }
            if (timeInterval != null && !holders.isEmpty()) {
                cluster = new Cluster();
                cluster.timeInterval = timeInterval;
                for (IEventHolder holder: holders) {
                    cluster.add(holder);
                }
            }
            return cluster;
        }

        public MinuteInterval remathTimeInterval() {
            MinuteInterval mi = null;
            for (List<IEventHolder> column : columns) {
                for (IEventHolder item: column) {
                    mi = MinuteInterval.union(mi, item.getTimeInterval());
                }
            }
            this.timeInterval = mi;
            return mi;
        }

        public void remathViewRect(float offsetLeft, float width, float offsetTop, float hourHeight) {
            if (isEmpty()) { return; }
            int count = getColumns();
            float itemWidth = width / count;
            int colNum = 0;
            for (List<IEventHolder> column : columns) {
                int left = (int)(colNum*itemWidth + offsetLeft);
                for (IEventHolder item: column) {
                    View view = item.getView();
                    if (view != null) {
                        try {
                            float hoursStart = item.getTimeInterval().getStart() / 60.0f;
                            float hoursEnd = item.getTimeInterval().getEnd() / 60.0f;
                            int top = (int) (offsetTop + hoursStart*hourHeight);
                            int bottom = top + (int)((hoursEnd-hoursStart)*hourHeight);
                            int right = (int) (left + itemWidth);
                            view.layout(left, top, right, bottom);
                        } catch (Exception ignored) {
//                            view.setVisibility(View.GONE);
                        }
                    }
                }
                colNum++;
            }
        }

        public void clear() {
            timeInterval = null;
            columns.clear();
        }

        @NonNull
        public ArrayList<IEventHolder> getAllItems() {
            ArrayList<IEventHolder> items = new ArrayList<>();
            for (List<IEventHolder> column : columns) {
                items.addAll(column);
            }
            return items;
        }
        //endregion
    }
    //endregion

    //region Fields
    // Attributes
    private float attrHourHeight = 60;
    private float attrHourLineWidth = 1;
    private int attrDisabledTimeColor = Color.parseColor("#22000000");
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
                onTimeSelectListener.onTimePress(TimeLineView.this, minute);
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onTimeSelectListener != null) {
                float y = e.getY();
                int minute = getMinutesByPositionY(y);
                onTimeSelectListener.onTimeLongPressed(TimeLineView.this, minute);
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
        if (hour < 0) { attrMinHour = 0; }
        else if (hour > 24) { attrMinHour = 24; }
        else { attrMinHour = hour; }
        if (attrMaxHour < attrMinHour) { attrMaxHour = attrMinHour; }
        initMinimumHeight();
    }

    public int getMaxHour() { return attrMaxHour; }
    public void setMaxHour(int hour) {
        if (hour < 0) { attrMaxHour = 0; }
        else if (hour > 24) { attrMaxHour = 24; }
        else { attrMaxHour = hour; }
        if (attrMaxHour < attrMinHour) { attrMinHour = attrMaxHour; }
        initMinimumHeight();
    }

    public boolean setHourInterval(int minHour, int maxHour) {
        if ( minHour < 0 || maxHour < minHour || 24 < maxHour) {
            return false;
        }
        this.attrMinHour = minHour;
        this.attrMaxHour = maxHour;
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
    }

    public int getHourTextColor() {
        return attrHourTextColor;
    }

    public void setHourTextColor(int attrHourTextColor) {
        this.attrHourTextColor = attrHourTextColor;
    }

    public float getHourTextSize() {
        return attrHourTextSize;
    }

    public void setHourTextSize(float attrHourTextSize) {
        this.attrHourTextSize = attrHourTextSize;
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
        // Load attributes
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
            attrHourPadding = a.getDimension(R.styleable.TimeLineView_hourPadding, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPadding, dm));
            attrHourPaddingLeft = attrHourPaddingRight = attrHourPadding;
            attrHourPaddingLeft = a.getDimension(R.styleable.TimeLineView_hourPaddingLeft, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPaddingLeft, dm));
            attrHourPaddingRight = a.getDimension(R.styleable.TimeLineView_hourPaddingRight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPaddingRight, dm));
            attrDisabledTimeColor = a.getColor(R.styleable.TimeLineView_disabledTimeColor, attrDisabledTimeColor);
//            if (a.hasValue(R.styleable.DayView_exampleDrawable)) {
//                mExampleDrawable = a.getDrawable(R.styleable.DayView_exampleDrawable);
//                mExampleDrawable.setCallback(this);
//            }
        } finally {
            a.recycle();
        }

        initViews();
        initMinimumHeight();
    }

    private void initViews() {
//        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);

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
        setMinimumHeight(hours * (int) attrHourHeight);
    }
    //endregion

    //region Draw methods
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        float shiftY = -attrMinHour *attrHourHeight;
//        float startX = hourColWidth;

        onDrawDisabledIntervals(canvas);
        onDrawColoredIntervals(canvas);
        onDrawHourLinesAndText(canvas);
    }

    protected void onDrawHourLinesAndText(Canvas canvas) {
        String hourText = "00:00";
        float maxWidth = canvas.getWidth();
        float hourColWidth = hourTextPaint.measureText(hourText) + attrHourPaddingLeft + attrHourPaddingRight;

        canvas.drawRect(0, 0, hourColWidth, canvas.getHeight(), hourBgPaint);
        for (int hour = attrMinHour+1; hour< attrMaxHour; hour++) {
            float y = attrHourHeight*(hour- attrMinHour);
            canvas.drawLine(hourColWidth, y, maxWidth, y, hourLinePaint);
            hourText = String.format(Locale.getDefault(), "%02d:00", hour);
            canvas.drawText(hourText, attrHourPaddingLeft, y + attrHourTextSize / 3, hourTextPaint);
        }
    }

    protected void onDrawDisabledIntervals(Canvas canvas) {
        float left = 0;
        float right = canvas.getWidth();
        int minMinute = attrMinHour *60;
        int maxMinute = attrMaxHour *60;
        for (MinuteInterval interval: getDisabledTimes()) {
            if (interval.isValid() && interval.isCollide(minMinute, maxMinute)) {
                float y0 = getPositionY(interval.start);
                float y1 = getPositionY(interval.end);
                canvas.drawRect(left, y0, right, y1, disabledTimePaint);
            }
        }
    }

    protected void onDrawColoredIntervals(Canvas canvas) {
        float left = 0;
        float right = canvas.getWidth();
        int minMinute = attrMinHour *60;
        int maxMinute = attrMaxHour *60;
        for (ColoredInterval ci: getColoredIntervals()) {
            MinuteInterval interval = ci.interval;
            if (interval.isValid() && interval.isCollide(minMinute, maxMinute)) {
                float y0 = getPositionY(interval.start);
                float y1 = getPositionY(interval.end);
                coloredTimePaint.setColor(ci.color);
                canvas.drawRect(left, y0, right, y1, coloredTimePaint);
            }
        }
    }
    //endregion

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean val = gestureDetector.onTouchEvent(event);
        return val; //super.onTouchEvent(event);
    }

    public float getPositionY(int minutes) {
        float pos = attrHourHeight * (minutes/60.0f - attrMinHour);
        return pos;
    }

    public int getMinutesByPositionY(float pos) {
        float minutes = 60*(attrMinHour + pos/attrHourHeight);
        return (int) minutes;
    }

    //region Clusters
    private List<Cluster> clusters = new ArrayList<>();
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

        this.addView(holder.getView());
        return true;
    }

    protected void reinitEventsRect(@NonNull Cluster cluster) {
        String hourText = "00:00";
        float hourColWidth = hourTextPaint.measureText(hourText) + attrHourPaddingLeft + attrHourPaddingRight;
        float width = this.getMeasuredWidth();
        float eventsWidth = width - hourColWidth;
        cluster.remathViewRect(hourColWidth, eventsWidth, -attrMinHour*attrHourHeight, attrHourHeight);
    }

    public void layoutChanged() {
        for (Cluster cluster: clusters) {
            reinitEventsRect(cluster);
        }
    }

    public void clear() {
        clusters.clear();
        removeAllViews();
    }

    public <T extends IEventHolder> void  setData(List<T> holders) {
        clear();
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
}
