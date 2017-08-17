package ru.fallgamlet.dayview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
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
        private DateFormat dateFormatter;
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

        @NonNull
        public static SimpleDateFormat getDefaultFormatter() {
            return new SimpleDateFormat("E, d MMM", Locale.getDefault());
        }

        @NonNull
        public DateFormat getDateFormatter() {
            if (dateFormatter == null) {
                dateFormatter = getDefaultFormatter();
            }
            return dateFormatter;
        }

        public void setDateFormatter(DateFormat formatter) {
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

        public void createView(Context context) {
            View view = View.inflate(context, R.layout.layout_day_view, null);
            initView(view);
        }

        protected void refreshDate() {
            if (this.date == null || this.dayTitleView == null) {
                return;
            }
            this.dayTitleView.setText(this.getDateFormatter().format(this.date));
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

    public interface OnPageChangeListener {
        void onPageSelected(Calendar selectedDate);
    }

    public interface OnDateTimeSelectListener {
        void onTimePress(Object sender, Calendar date);
        void onTimeLongPressed(Object sender, Calendar date);
    }

    public interface OnContentListener {
        int getMinHour(Calendar date);
        int getMaxHour(Calendar date);
        List<IEventHolder> getEvents(Calendar date);
        List<ColoredInterval> getColoredIntervals(Calendar date);
        List<MinuteInterval> getDisabledIntervals(Calendar date);
    }

    public interface OnDesignListener {
        DateFormat getDayFormat();
        void onDesignDateTitle(@Nullable TextView dayTitleView);
        void onDesignTimeLineView(@Nullable TimeLineView timeLineView);
    }
    //endregion

    //region Fields
    private static long DAY = 24*60*60*1000;

    // Attributes
    private float attrDensity;
    private float attrHourHeight = 60;
    private float attrHourLineWidth = 1;
    private float attrHourLinePadding = 0;
    private float attrHourLinePaddingLeft = attrHourLinePadding;
    private float attrHourLinePaddingRight = attrHourLinePadding;
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
    private float attrDayTextSize = 14;
    private int attrDayTextColor = Color.DKGRAY;
    private float attrDayTextPadding = 0;
    private float attrDayTextPaddingLeft;
    private float attrDayTextPaddingRight;
    private float attrDayTextPaddingTop;
    private float attrDayTextPaddingBottom;
    private float attrTimeLinePadding = 8;
    private float attrTimeLinePaddingLeft;
    private float attrTimeLinePaddingRight;
    private float attrTimeLinePaddingTop;
    private float attrTimeLinePaddingBottom;

    DayPagerAdapter adapter;
    OnContentListener contentListener;
    OnDesignListener designListener;
    OnPageChangeListener pageChangeListener;
    OnDateTimeSelectListener dateTimeSelectListener;
    Calendar startDate;
    int shiftPosition;
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
        attrMinHour = hour;
        if (attrMaxHour < attrMinHour) { attrMaxHour = attrMinHour; }
    }

    public int getMaxHour() { return attrMaxHour; }
    public void setMaxHour(int hour) {
        attrMaxHour = hour;
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

    public float getDayTextSize() {
        return attrDayTextSize;
    }

    public void setDayTextSize(float attrDayTextSize) {
        this.attrDayTextSize = attrDayTextSize;
    }

    public int getDayTextColor() {
        return attrDayTextColor;
    }

    public void setDayTextColor(int attrDayTextColor) {
        this.attrDayTextColor = attrDayTextColor;
    }

    public float getDayTextPadding() {
        return attrDayTextPadding;
    }

    public void setDayTextPadding(float attrDayTextPadding) {
        this.attrDayTextPadding = attrDayTextPadding;
    }

    public float getDayTextPaddingLeft() {
        return attrDayTextPaddingLeft;
    }

    public void setDayTextPaddingLeft(float attrDayTextPaddingLeft) {
        this.attrDayTextPaddingLeft = attrDayTextPaddingLeft;
    }

    public float getDayTextPaddingRight() {
        return attrDayTextPaddingRight;
    }

    public void setDayTextPaddingRight(float attrDayTextPaddingRight) {
        this.attrDayTextPaddingRight = attrDayTextPaddingRight;
    }

    public float getDayTextPaddingTop() {
        return attrDayTextPaddingTop;
    }

    public void setDayTextPaddingTop(float attrDayTextPaddingTop) {
        this.attrDayTextPaddingTop = attrDayTextPaddingTop;
    }

    public float getDayTextPaddingBottom() {
        return attrDayTextPaddingBottom;
    }

    public void setDayTextPaddingBottom(float attrDayTextPaddingBottom) {
        this.attrDayTextPaddingBottom = attrDayTextPaddingBottom;
    }

    //endregion

    //region Getters and Setters listeners
    public OnContentListener getOnContentListener() {
        return contentListener;
    }

    public void setOnContentListener(OnContentListener listener) {
        this.contentListener = listener;
    }


    public OnDesignListener getOnDesignListener() {
        return designListener;
    }

    public void setOnDesignListener(OnDesignListener listener) {
        this.designListener = listener;
    }


    public OnPageChangeListener getOnPageListener() {
        return pageChangeListener;
    }

    public void setOnPageListener(OnPageChangeListener listener) {
        this.pageChangeListener = listener;
    }


    public OnDateTimeSelectListener getOnDateTimeSelectListener() {
        return this.dateTimeSelectListener;
    }

    public void setOnDateTimeSelectListener(OnDateTimeSelectListener listener) {
        this.dateTimeSelectListener = listener;
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
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        shiftPosition = DayPagerAdapter.MAX_PAGES/2;

        adapter = new DayPagerAdapter(this);
        setAdapter(adapter);
        setCurrentItem(0, false);

        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                System.out.println("DayViewPager page scrolled to pos: "+position);
            }

            @Override
            public void onPageSelected(int position) {
                int pos = getLocalPosition(position);
                Date date = getDate(pos);
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    if (pageChangeListener != null) {
                        pageChangeListener.onPageSelected(calendar);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                System.out.println("DayViewPager page change state to: "+state);
            }
        });
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {
        // Load attributes
        DisplayMetrics dm = getResources().getDisplayMetrics();
        attrDensity = dm.density;

        attrHourHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, attrHourHeight, dm);
        attrHourLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, attrHourLineWidth, dm);
        attrHourTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourTextSize, dm);
        attrHourPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, attrHourPadding, dm);
        attrHourPaddingLeft = attrHourPaddingRight = attrHourPadding;

        attrTimeLinePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, attrTimeLinePadding, dm);
        attrTimeLinePaddingLeft = attrTimeLinePaddingRight = attrTimeLinePaddingTop = attrTimeLinePaddingBottom = attrTimeLinePadding;

        attrDayTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrDayTextSize, dm);
        attrDayTextPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, attrDayTextPadding, dm);
        attrDayTextPaddingLeft = attrDayTextPaddingRight = attrDayTextPaddingTop = attrDayTextPaddingBottom = attrDayTextPadding;

        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DayViewPager, defStyle, 0);
            try {
                setMinHour(a.getInt(R.styleable.DayViewPager_hourMin, attrMinHour));
                setMaxHour(a.getInt(R.styleable.DayViewPager_hourMax, attrMaxHour));
                attrHourHeight = a.getDimension(R.styleable.DayViewPager_hourHeight, attrHourHeight);
                attrHourLineWidth = a.getDimension(R.styleable.DayViewPager_hourLineWidth, attrHourLineWidth);
                attrHourLineColor = a.getColor(R.styleable.DayViewPager_hourLineColor, attrHourLineColor);
                attrHourBackground = a.getColor(R.styleable.DayViewPager_hourBackground, attrHourBackground);
                attrHourTextColor = a.getColor(R.styleable.DayViewPager_hourTextColor, attrHourTextColor);
                attrHourTextSize = a.getDimension(R.styleable.DayViewPager_hourTextSize, attrHourTextSize);
                attrDisabledTimeColor = a.getColor(R.styleable.DayViewPager_disabledTimeColor, attrDisabledTimeColor);

                attrHourPadding = a.getDimension(R.styleable.DayViewPager_hourPadding, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPadding, dm));
                attrHourPaddingLeft = attrHourPaddingRight = attrHourPadding;
                attrHourPaddingLeft = a.getDimension(R.styleable.DayViewPager_hourPaddingLeft, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPaddingLeft, dm));
                attrHourPaddingRight = a.getDimension(R.styleable.DayViewPager_hourPaddingRight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourPaddingRight, dm));

                attrHourLinePadding = a.getDimension(R.styleable.DayViewPager_hourLinePadding, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourLinePadding, dm));
                attrHourLinePaddingLeft = attrHourLinePaddingRight = attrHourLinePadding;
                attrHourLinePaddingLeft = a.getDimension(R.styleable.DayViewPager_hourLinePaddingLeft, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourLinePaddingLeft, dm));
                attrHourLinePaddingRight = a.getDimension(R.styleable.DayViewPager_hourLinePaddingRight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, attrHourLinePaddingRight, dm));

                attrTimeLinePadding = a.getDimension(R.styleable.DayViewPager_timeLinePadding, attrTimeLinePadding);
                attrTimeLinePaddingLeft = attrTimeLinePaddingRight = attrTimeLinePaddingTop = attrTimeLinePaddingBottom = attrTimeLinePadding;
                attrTimeLinePaddingLeft = a.getDimension(R.styleable.DayViewPager_timeLinePaddingLeft, attrTimeLinePaddingLeft);
                attrTimeLinePaddingRight = a.getDimension(R.styleable.DayViewPager_timeLinePaddingRight, attrTimeLinePaddingRight);
                attrTimeLinePaddingTop = a.getDimension(R.styleable.DayViewPager_timeLinePaddingTop, attrTimeLinePaddingTop);
                attrTimeLinePaddingBottom = a.getDimension(R.styleable.DayViewPager_timeLinePaddingBottom, attrTimeLinePaddingBottom);

                attrDayTextColor = a.getColor(R.styleable.DayViewPager_dayTextColor, attrDayTextColor);
                attrDayTextSize = a.getDimension(R.styleable.DayViewPager_dayTextSize, attrDayTextSize);
                attrDayTextPadding = a.getDimension(R.styleable.DayViewPager_dayTextPadding, attrDayTextPadding);
                attrDayTextPaddingLeft = attrDayTextPaddingRight = attrDayTextPaddingTop = attrDayTextPaddingBottom = attrDayTextPadding;
                attrDayTextPaddingLeft = a.getDimension(R.styleable.DayViewPager_dayTextPaddingLeft, attrDayTextPaddingLeft);
                attrDayTextPaddingRight = a.getDimension(R.styleable.DayViewPager_dayTextPaddingRight, attrDayTextPaddingRight);
                attrDayTextPaddingTop = a.getDimension(R.styleable.DayViewPager_dayTextPaddingTop, attrDayTextPaddingTop);
                attrDayTextPaddingBottom = a.getDimension(R.styleable.DayViewPager_dayTextPaddingBottom, attrDayTextPaddingBottom);
            } catch (Exception e) {
                Log.e("DayViewpager", e.toString());
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
        return getLocalPosition(position);
    }

    public Calendar getCurrentCalendar() {
        int pos = getCurrentItem();
        Calendar calendar = getCalendar(pos);
        return calendar;
    }

    public Date getCurrentDate() {
        return getCurrentCalendar().getTime();
    }
    //endregion

    //region Position methods
    private int getShiftPosition() {
        return shiftPosition;
    }

    private int getAbsolutePosition(int position) {
        return position + shiftPosition;
    }

    private int getLocalPosition(int absolutePosition) {
        return absolutePosition - shiftPosition;
    }

    protected Calendar getCalendar(int localPosition) {
        Calendar calendar = (Calendar) startDate.clone();
        calendar.add(Calendar.DAY_OF_YEAR, localPosition);
        return calendar;
    }

    protected Date getDate(int localPosition) {
        return getCalendar(localPosition).getTime();
    }

    protected int getLocalPosition(@NonNull Date date) {
        Calendar dayDate = Calendar.getInstance();
        dayDate.setTime(date);
        return getLocalPosition(dayDate);
    }

    protected int getLocalPosition(@NonNull Calendar date) {
        Calendar dayDate = (Calendar) date.clone();
        dayDate.set(Calendar.HOUR_OF_DAY, 0);
        dayDate.set(Calendar.MINUTE, 0);
        dayDate.set(Calendar.SECOND, 0);
        dayDate.set(Calendar.MILLISECOND, 0);

        long d = dayDate.getTimeInMillis() / DAY;
        long dStart = startDate.getTimeInMillis() / DAY;
        long shiftDays = d - dStart;
        return (int)shiftDays;
    }
    //endregion

    //region Notify methods
    public void notifyDataChanged() {
        PagerAdapter adapter = getAdapter();
        if (adapter != null) {
            int pos = getCurrentItem();
            pos = getAbsolutePosition(pos);
            adapter.instantiateItem(this, pos);
            adapter.instantiateItem(this, pos-1);
            adapter.instantiateItem(this, pos+1);
        }
    }
    //endregion

    //region DayPagerAdapter.OnContentListener implementation
    @NonNull
    @Override
    public DayPagerAdapter.ViewHolder onCreateViewHolder(int position) {
        final DayViewHolder holder = new DayViewHolder();
        holder.createView(getContext());

        TextView textView = holder.dayTitleView;
        if (textView != null) {
            //Configure date title
            textView.setTextSize(attrDayTextSize / attrDensity);
            textView.setTextColor(attrDayTextColor);
            textView.setPadding((int)attrDayTextPaddingLeft, (int)attrDayTextPaddingTop, (int)attrDayTextPaddingRight, (int)attrDayTextPaddingBottom);
        }

        TimeLineView timeLineView = holder.timeLineView;
        if (timeLineView != null) {
            initTimeLineViewAttributes(timeLineView);
            timeLineView.invalidate();
            timeLineView.setOnTimeSelectListener(new IOnTimeSelectListener() {
                @Override
                public void onTimePress(Object sender, int minute) {
                    if (DayViewPager.this.dateTimeSelectListener != null) {
                        Calendar date = Calendar.getInstance();
                        date.setTime(holder.getDate());
                        date.set(Calendar.HOUR_OF_DAY, 0);
                        date.set(Calendar.MINUTE, 0);
                        date.set(Calendar.SECOND, 0);
                        date.set(Calendar.MILLISECOND, 0);
                        date.add(Calendar.MINUTE, minute);

                        DayViewPager.this.dateTimeSelectListener.onTimePress(sender, date);
                    }
                }

                @Override
                public void onTimeLongPressed(Object sender, int minute) {
                    if (DayViewPager.this.dateTimeSelectListener != null) {
                        Calendar date = Calendar.getInstance();
                        date.setTime(holder.getDate());
                        date.set(Calendar.HOUR_OF_DAY, 0);
                        date.set(Calendar.MINUTE, 0);
                        date.set(Calendar.SECOND, 0);
                        date.set(Calendar.MILLISECOND, 0);
                        date.add(Calendar.MINUTE, minute);

                        DayViewPager.this.dateTimeSelectListener.onTimeLongPressed(sender, date);
                    }
                }
            });
        }

        if (designListener != null) {
            holder.setDateFormatter(designListener.getDayFormat());
            designListener.onDesignDateTitle(textView);
            designListener.onDesignTimeLineView(timeLineView);
        }

        return holder;
    }

    private void initTimeLineViewAttributes(TimeLineView view) {
        view.setPadding((int)attrTimeLinePaddingLeft, (int)attrTimeLinePaddingTop, (int)attrTimeLinePaddingRight, (int)attrTimeLinePaddingBottom);
        view.setMinHour(attrMinHour);
        view.setMaxHour(attrMaxHour);
        view.setDisabledTimeColor(attrDisabledTimeColor);

        view.setHourLinePadding(attrHourLinePadding);
        view.setHourLinePaddingLeft(attrHourLinePaddingLeft);
        view.setHourLinePaddingRight(attrHourLinePaddingRight);

        view.setHourPadding(attrHourPadding);
        view.setHourPaddingLeft(attrHourPaddingLeft);
        view.setHourPaddingRight(attrHourPaddingRight);

        view.setHourBackground(attrHourBackground);
        view.setHourHeight(attrHourHeight);
        view.setHourLineColor(attrHourLineColor);
        view.setHourLineWidth(attrHourLineWidth);
        view.setHourTextColor(attrHourTextColor);
        view.setHourTextSize(attrHourTextSize);
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
        List<MinuteInterval> disabledIntervals = null;
        List<ColoredInterval> coloredIntervals = null;
        List<IEventHolder> eventHolders = null;

        dayHolder.setDate(calendar.getTime());

        if (contentListener != null) {
            minHour = contentListener.getMinHour(calendar);
            maxHour = contentListener.getMaxHour(calendar);

            if (maxHour < minHour) { maxHour = 1+minHour; }

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


    @Override
    public void onUnbindData(DayPagerAdapter.ViewHolder holder, int position) {
        if (holder == null || !(holder instanceof DayViewHolder)) {
            return;
        }

        DayViewHolder dayHolder = (DayViewHolder) holder;
        TextView titleView = dayHolder.getDayTitleView();
        TimeLineView timeLineView = dayHolder.getTimeLineView();

        if (titleView != null) {
            titleView.setText(null);
        }

        if (timeLineView != null) {
            timeLineView.clearEvents();
            timeLineView.getColoredIntervals().clear();
            timeLineView.getDisabledTimes().clear();
        }
    }

    //endregion
}
