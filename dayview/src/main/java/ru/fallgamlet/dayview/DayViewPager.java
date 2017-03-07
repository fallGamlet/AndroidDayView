package ru.fallgamlet.dayview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DayViewPager extends ViewPager implements DayPagerAdapter.OnContentListener {

    //region Sub classes and interfaces
    public static class DayViewHolder extends DayPagerAdapter.ViewHolder {
        //region Fields
        private TextView dayTitleView;
        private TimeLineView timeLineView;

        private Date date;
        private SimpleDateFormat dateFormatter = new SimpleDateFormat("E, d MMM", Locale.getDefault());
        //endregion

        //region Constructors
        public DayViewHolder() {
            initView(null);
        }

        public DayViewHolder(View rootView) {
            initView(rootView);
        }
        //endregion

        //region Getters and Setters
        public TextView getDayTitleView() {
            return dayTitleView;
        }

        public void setDayTitleView(TextView dayTitleView) {
            this.dayTitleView = dayTitleView;
            refreshDate();
        }

        public TimeLineView getTimeLineView() {
            return timeLineView;
        }

        public void setTimeLineView(TimeLineView timeLineView) {
            this.timeLineView = timeLineView;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
            refreshDate();
        }

        public SimpleDateFormat getDateFormatter() {
            return dateFormatter;
        }

        public void setDateFormatter(@NonNull SimpleDateFormat formatter) {
            dateFormatter = formatter;
            refreshDate();
        }
        //endregion

        //region Methods
        protected void initView(View rootView) {
            setView(rootView);
            if (rootView != null) {
                dayTitleView = (TextView) rootView.findViewById(R.id.dayTitle);
                timeLineView = (TimeLineView) rootView.findViewById(R.id.timeLineView);
            } else {
                dayTitleView = null;
                timeLineView = null;
            }
        }

        public void inflateView(Context context) {
            View view = View.inflate(context, R.layout.layout_day_view, null);
            initView(view);
        }

        protected void refreshDate() {
            if (this.date == null || this.dayTitleView == null || this.dateFormatter == null) {
                return;
            }
            this.dayTitleView.setText(this.dateFormatter.format(this.date));
        }

        public boolean setAttributes(AttributeSet attrs, int defStyle) {
            boolean check = false;
            if (timeLineView != null) {
                timeLineView.setAttributes(attrs, defStyle);
                check = true;
            }
            return check;
        }
        //endregion
    }

    public interface OnContentListener {
        int getMinHour(Calendar date);
        int getMaxHour(Calendar date);
        List<TimeLineView.IEventHolder> getEvents(Calendar date);
        List<TimeLineView.ColoredInterval> getColoredIntervals(Calendar date);
        List<TimeLineView.MinuteInterval> getDisabledIntervals(Calendar date);
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

//    InfinitePagerAdapter infAdapter;
    DayPagerAdapter adapter;
    OnContentListener contentListener;
    Calendar startDate;
    int shiftPosition;
    Calendar curDate;
    //endregion

    //region Constructors
    public DayViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(attrs, 0);
        init();
    }

    public DayViewPager(Context context) {
        super(context);
        initAttributes(null, 0);
        init();
    }
    //endregion

    //region Getters and Setters attributes
    public int getMinHour() { return attrMinHour; }
    public void setMinHour(int hour) {
        if (hour < 0) { attrMinHour = 0; }
        else if (hour > 24) { attrMinHour = 24; }
        else { attrMinHour = hour; }
        if (attrMaxHour < attrMinHour) { attrMaxHour = attrMinHour; }
    }

    public int getMaxHour() { return attrMaxHour; }
    public void setMaxHour(int hour) {
        if (hour < 0) { attrMaxHour = 0; }
        else if (hour > 24) { attrMaxHour = 24; }
        else { attrMaxHour = hour; }
        if (attrMaxHour < attrMinHour) { attrMinHour = attrMaxHour; }
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
    //endregion

    //region Getters and Setters listeners
    public OnContentListener getOnContentListener() {
        return contentListener;
    }

    public void setOnContentListener(OnContentListener listener) {
        this.contentListener = listener;
    }

    public void setAdapter(DayPagerAdapter adapter) {
        this.adapter = adapter;
        super.setAdapter(adapter);
    }

    @Deprecated
    @Override
    public void setAdapter(PagerAdapter adapter) {
//        super.setAdapter(adapter);
    }
    //endregion

    //region Init methods
    protected void init() {
        startDate = Calendar.getInstance();
        shiftPosition = Integer.MAX_VALUE / 2;

        curDate = (Calendar) startDate.clone();
        adapter = new DayPagerAdapter(this);
        setAdapter(adapter);
        setCurrentItem(0, false);
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimeLineView, defStyle, 0);
        DisplayMetrics dm = getResources().getDisplayMetrics();

        attrHourHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourHeight, dm);
        attrHourLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourLineWidth, dm);
        attrHourTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourTextSize, dm);
        attrHourPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPadding, dm);
        attrHourPaddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPaddingLeft, dm);
        attrHourPaddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPaddingRight, dm);

        if (attrs != null) {
            try {
                setMinHour(a.getInt(R.styleable.TimeLineView_hourMin, attrMinHour));
                setMaxHour(a.getInt(R.styleable.TimeLineView_hourMax, attrMaxHour));
                attrHourHeight = a.getDimension(R.styleable.TimeLineView_hourHeight, attrHourHeight);
                attrHourLineWidth = a.getDimension(R.styleable.TimeLineView_hourLineWidth, attrHourLineWidth);
                attrHourLineColor = a.getColor(R.styleable.TimeLineView_hourLineColor, attrHourLineColor);
                attrHourBackground = a.getColor(R.styleable.TimeLineView_hourBackground, attrHourBackground);
                attrHourTextColor = a.getColor(R.styleable.TimeLineView_hourTextColor, attrHourTextColor);
                attrHourTextSize = a.getDimension(R.styleable.TimeLineView_hourTextSize, attrHourTextSize);
                attrHourPadding = a.getDimension(R.styleable.TimeLineView_hourPadding, attrHourPadding);
                attrHourPaddingLeft = attrHourPaddingRight = attrHourPadding;
                attrHourPaddingLeft = a.getDimension(R.styleable.TimeLineView_hourPaddingLeft, attrHourPaddingLeft);
                attrHourPaddingRight = a.getDimension(R.styleable.TimeLineView_hourPaddingRight, attrHourPaddingRight);
                attrDisabledTimeColor = a.getColor(R.styleable.TimeLineView_disabledTimeColor, attrDisabledTimeColor);
            } finally {
                a.recycle();
            }
        }
    }
    //endregion

    //region Set and get current item methods
    public void setCurrentItem(@NonNull Calendar date) {
        setCurrentItem(date, false);
    }

    public void setCurrentItem(@NonNull Calendar date, boolean smoothScroll) {
        setCurrentItem(date.getTime(), smoothScroll);
    }

    public void setCurrentItem(@NonNull Date date) {
        setCurrentItem(date, false);
    }

    public void setCurrentItem(@NonNull Date date, boolean smoothScroll) {
        int localPos = getLocalPosition(date);
        setCurrentItem(localPos, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        // offset the current item to ensure there is space to scroll
        setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (getAdapter().getCount() == 0) {
            super.setCurrentItem(item, smoothScroll);
            return;
        }
        item = getShiftPosition() + item;
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public int getCurrentItem() {
        int position = super.getCurrentItem();
        return -getShiftPosition() + position;
    }
    //endregion

    //region Position methods
    private int getShiftPosition() {
        return shiftPosition;
    }

    private int getAbsolutePosition(int position) {
        return shiftPosition + position;
    }

    private int getLocalPosition(int absolutePosition) {
        return -shiftPosition + absolutePosition;
    }

    protected Calendar getCalendar(int localPosition) {
        Calendar calendar = (Calendar) startDate.clone();
//        localPosition -= getShiftPosition();
        calendar.add(Calendar.DAY_OF_YEAR, localPosition);
        return calendar;
    }

    protected Date getDate(int localPosition) {
        return getCalendar(localPosition).getTime();
    }

    protected int getLocalPosition(@NonNull Date date) {
        long DAY = 24*60*60*1000;
        long shiftDays = startDate.getTimeInMillis()/DAY - date.getTime()/DAY;
        return (int)shiftDays;
    }

    protected int getLocalPosition(@NonNull Calendar date) {
        return getLocalPosition(date.getTime());
    }
    //endregion

    //region DayPagerAdapter.OnContentListener implementation
    @NonNull
    @Override
    public DayPagerAdapter.ViewHolder onCreateViewHolder(int position) {
        DayViewHolder holder = new DayViewHolder();
        holder.inflateView(getContext());
        TimeLineView timeLineView = holder.timeLineView;
        if (timeLineView != null) {
            // Configure TimeLineView
            timeLineView.setMinHour(attrMinHour);
            timeLineView.setMaxHour(attrMaxHour);
            timeLineView.setHourBackground(attrHourBackground);
            timeLineView.setHourHeight(attrHourHeight);
            timeLineView.setHourLineColor(attrHourLineColor);
            timeLineView.setHourLineWidth(attrHourLineWidth);
            timeLineView.setHourPaddingLeft(attrHourPaddingLeft);
            timeLineView.setHourPaddingRight(attrHourPaddingRight);
            timeLineView.setDisabledTimeColor(attrDisabledTimeColor);
            timeLineView.setHourTextColor(attrHourTextColor);
            timeLineView.setHourTextSize(attrHourTextSize);
        }
        return holder;
    }

    @Override
    public void onBindData(DayPagerAdapter.ViewHolder holder, int position) {
        if (holder == null || !(holder instanceof DayViewHolder)) {
            return;
        }

        DayViewHolder dayHolder = (DayViewHolder) holder;

        int localPos = getLocalPosition(position);
        Calendar calendar = getCalendar(localPos);
        int minHour = attrMinHour;
        int maxHour = attrMaxHour;
        List<TimeLineView.MinuteInterval> disabledIntervals = null;
        List<TimeLineView.ColoredInterval> coloredIntervals = null;
        List<TimeLineView.IEventHolder> eventHolders = null;

        ((DayViewHolder) holder).setDate(calendar.getTime());

        if (contentListener != null) {
            minHour = contentListener.getMinHour(calendar);
            maxHour = contentListener.getMaxHour(calendar);
            if (minHour < 0) { minHour = attrMinHour;}
            if (minHour < 0) { minHour = 0;}
            if (minHour > 24) { minHour = 24;}

            if (maxHour < minHour || maxHour > 24 ) { maxHour = attrMaxHour; }
            if (maxHour < minHour || maxHour > 24 ) { maxHour = minHour; }

            disabledIntervals = contentListener.getDisabledIntervals(calendar);
            coloredIntervals = contentListener.getColoredIntervals(calendar);
            eventHolders = contentListener.getEvents(calendar);
        }

        TimeLineView timeLineView = dayHolder.timeLineView;
        if (timeLineView != null) {
            timeLineView.setHourInterval(minHour, maxHour);

            timeLineView.getDisabledTimes().clear();
            if (disabledIntervals != null && !disabledIntervals.isEmpty()) {
                timeLineView.getDisabledTimes().addAll(disabledIntervals);
            }

            timeLineView.removeAllColoredIntervals();
            timeLineView.addColoredInterval(coloredIntervals);

            timeLineView.setData(eventHolders);
        }
    }
    //endregion

}
