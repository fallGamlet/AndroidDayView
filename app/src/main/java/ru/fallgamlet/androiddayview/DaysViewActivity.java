package ru.fallgamlet.androiddayview;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import ru.fallgamlet.dayview.DayPagerAdapter;
import ru.fallgamlet.dayview.DayViewPager;
import ru.fallgamlet.dayview.TimeLineView;

public class DaysViewActivity extends AppCompatActivity {

    //region Fields
    Button dateBtn = null;
    Button refreshBtn = null;
    DayViewPager dayViewPager;
    Random random = new Random();
    Date curDate;
    List<TimeLineView.MinuteInterval> workIntervals = new ArrayList<>(7);
    List<Integer> colors = new ArrayList<>();
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_days_view);

        dateBtn = (Button) findViewById(R.id.dateBtn);
        if (dateBtn != null) {
            dateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePicker();
                }
            });
        }

        refreshBtn = (Button) findViewById(R.id.refreshBtn);
        if (refreshBtn != null) {
            refreshBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dayViewPager != null) {
                        dayViewPager.notifyDataChanged();
                    }
                }
            });
        }

        dayViewPager = (DayViewPager) findViewById(R.id.dayViewPager);
        dayViewPager.setOnContentListener(getContextListener());
        dayViewPager.setOnDesignListener(getDesignListener());
        dayViewPager.setOnPageListener(getOnPageChangeListener());
        dayViewPager.setOnDateTimeSelectListener(getDateTimeSelectListener());
        dayViewPager.setFocusable(false);

        Calendar calendar = Calendar.getInstance();

        setDate(calendar.getTime());
    }

    private void showSnackbar(String msg) {
        if (dayViewPager != null) {
            Snackbar.make(dayViewPager, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, R.style.AppTheme_Dialog, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

                setDate(calendar.getTime());
            }
        },year, month, day);
        dialog.show();
    }

    private void setDate(Date date) {
        if (date == null) {
            return;
        }

        if (curDate != null) {
            Calendar curCalendar = Calendar.getInstance();
            curCalendar.setTime(curDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int curYear = curCalendar.get(Calendar.YEAR);
            int curDay = curCalendar.get(Calendar.DAY_OF_YEAR);
            int year = calendar.get(Calendar.YEAR);
            int day = calendar.get(Calendar.DAY_OF_YEAR);

            if (curYear == year && curDay == day) {
                return;
            }
        }

        curDate = date;
        if (dateBtn != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            String title = formatter.format(curDate);
            dateBtn.setText(title);
        }
        if (dayViewPager != null) {
            dayViewPager.setCurrentItem(date);

        }
    }

    private void generateWorkTimes() {
        if (workIntervals == null || workIntervals.isEmpty()) {
            for (int i=0; i<7; i++) {
                int start = 5+random.nextInt(5);
                int end = 16+random.nextInt(8);
                TimeLineView.MinuteInterval interval = new TimeLineView.MinuteInterval(start, end);
                workIntervals.add(interval);
            }
        }
    }

    private List<Integer> getColors() {
        if(colors == null) { colors = new ArrayList<>(); }
        if (colors.isEmpty()) {
            colors.add(ContextCompat.getColor(this, R.color.event_color_01));
            colors.add(ContextCompat.getColor(this, R.color.event_color_02));
            colors.add(ContextCompat.getColor(this, R.color.event_color_03));
            colors.add(ContextCompat.getColor(this, R.color.event_color_04));
            colors.add(ContextCompat.getColor(this, R.color.event_color_05));
            colors.add(ContextCompat.getColor(this, R.color.event_color_06));
        }
        return colors;
    }

    private int getRandomColor() {
        List<Integer> colors = getColors();
        int i = random.nextInt();
        int size = colors.size();
        i = ((i%size)+size)%size;
        System.out.println("Color index:"+i);
        return colors.get(i);
    }

    private TimeLineView.MinuteInterval getWorkTime(int calendarWeekDay) {
        generateWorkTimes();
        int i;
        switch (calendarWeekDay) {
            case Calendar.MONDAY:
                i = 0;
                break;
            case Calendar.TUESDAY:
                i = 1;
                break;
            case Calendar.WEDNESDAY:
                i = 2;
                break;
            case Calendar.THURSDAY:
                i = 3;
                break;
            case Calendar.FRIDAY:
                i = 4;
                break;
            case Calendar.SATURDAY:
                i = 5;
                break;
            case Calendar.SUNDAY:
                i = 6;
                break;
            default:
                i = 0;
                break;
        }
        return workIntervals.get(i);
    }

    private List<TimeLineView.IEventHolder> generateEventHolders(Calendar date) {
        List<TimeLineView.IEventHolder> holderList = new ArrayList<>();
        int weekDay = date.get(Calendar.DAY_OF_WEEK);
        TimeLineView.MinuteInterval workTime = getWorkTime(weekDay);
        int count = 5+random.nextInt(10);
        int hour = 60;
        int minMinute = workTime.getStart()*hour;
        int maxMinute = workTime.getEnd()*hour;

        for (int i=0; i< count; i++) {
            int startMinute = minMinute + random.nextInt(maxMinute-minMinute);
            int minLength = 20;
            int length = maxMinute - startMinute - minLength;
            length = length <= 0? minLength: minLength+random.nextInt(length);
            if (length > 2*hour) { length = 2*hour; }

            String title = "title";
            String subtitle = "subtitle";

            Calendar calendar = (Calendar)date.clone();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.MINUTE, startMinute);
            final Date startDate = calendar.getTime();
            calendar.add(Calendar.MINUTE, length);
            final Date endDate = calendar.getTime();


            MyEventHolder holder = new MyEventHolder(DaysViewActivity.this, title, subtitle, startDate, endDate);
            int bgColor = getRandomColor();
            holder.getView().setBackgroundColor(bgColor);
            holderList.add(holder);

            holder.setListener(new OnClickListener() {
                @Override
                public void onClick(TimeLineView.IEventHolder holder) {
                    MyEventHolder myholder = ((MyEventHolder) holder);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String text = "Select item\n "+
                            dateFormat.format(myholder.getStart()) + " " +
                            timeFormat.format(myholder.getStart()) + " - " +
                            timeFormat.format(myholder.getEnd());
                    showSnackbar(text);
                }
            });
        }

        return holderList;
    }


    //region OnContextListener
    DayViewPager.OnContentListener contextListener;
    @NonNull
    protected DayViewPager.OnContentListener getContextListener() {
        if (contextListener == null) {
            contextListener = new DayViewPager.OnContentListener() {
                @Override
                public int getMinHour(Calendar date) {
                    int hour = -1;
                    if (date != null) {
                        int weekDay = date.get(Calendar.DAY_OF_WEEK);
                        hour = getWorkTime(weekDay).getStart();
                    }
                    return hour;
                }

                @Override
                public int getMaxHour(Calendar date) {
                    int hour = -1;
                    if (date != null) {
                        int weekDay = date.get(Calendar.DAY_OF_WEEK);
                        hour = getWorkTime(weekDay).getEnd();
                    }
                    return hour;
                }

                @Override
                public List<TimeLineView.IEventHolder> getEvents(Calendar date) {
                    return generateEventHolders(date);
                }

                @Override
                public List<TimeLineView.ColoredInterval> getColoredIntervals(Calendar date) {
                    return null;
                }

                @Override
                public List<TimeLineView.MinuteInterval> getDisabledIntervals(Calendar date) {
                    return null;
                }
            };
        }
        return contextListener;
    }
    //endregion

    //region OnDesignListener
    DayViewPager.OnDesignListener designListener;
    @NonNull
    protected DayViewPager.OnDesignListener getDesignListener() {
        if (designListener == null) {
            designListener = new DayViewPager.OnDesignListener() {
                @Override
                public DateFormat getDayFormat() {
                    return new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                }

                @Override
                public void onDesignDateTitle(@Nullable TextView dayTitleView) {
                    if (dayTitleView != null) {
                        Object obj = dayTitleView.getLayoutParams();
                        dayTitleView.setGravity(GravityCompat.START);
                        dayTitleView.setPadding(32,16,0,0);
                    }
                }

                @Override
                public void onDesignTimeLineView(@Nullable TimeLineView timeLineView) {
                    if (timeLineView == null) {
                        return;
                    }

//                    timeLineView.requestLayout();
                }
            };
        }
        return designListener;
    }
    //endregion

    //region OnPageChangeListener
    DayViewPager.OnPageChangeListener onPageChangeListener;
    @NonNull
    DayViewPager.OnPageChangeListener getOnPageChangeListener() {
        if (onPageChangeListener == null) {
            onPageChangeListener = new DayViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(Calendar selectedDate) {
                    setDate(selectedDate==null? null: selectedDate.getTime());
                }
            };
        }
        return onPageChangeListener;
    }
    //endregion

    //region OnDateTimeSelectListener
    DayViewPager.OnDateTimeSelectListener onDateTimeSelectListener;
    @NonNull
    DayViewPager.OnDateTimeSelectListener getDateTimeSelectListener() {
        if (onDateTimeSelectListener == null) {
            onDateTimeSelectListener = new DayViewPager.OnDateTimeSelectListener() {
                @Override
                public void onTimePress(Object sender, Calendar date) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault());
                    String dateStr = format.format(date.getTime());
                    showSnackbar(dateStr);
                }

                @Override
                public void onTimeLongPressed(Object sender, Calendar date) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault());
                    String dateStr = format.format(date.getTime());
                    showSnackbar(dateStr);
                }
            };
        }
        return onDateTimeSelectListener;
    }
    //endregion

    //region Sub classes and interfaces
    interface OnClickListener {
        void onClick(TimeLineView.IEventHolder holder);
    }

    public static class  MyEventHolder implements TimeLineView.IEventHolder, View.OnClickListener {
        private String title;
        private String subtitle;
        private Date start, end;
        private TimeLineView.MinuteInterval timeInterval;

        private View rootView;
        private TextView startView;
        private TextView endView;
        private TextView titleView;
        private TextView subtitleView;
        private SimpleDateFormat timeFormatter;
        private OnClickListener listener;

        public MyEventHolder(Context context) {
            initView(context);
        }

        public MyEventHolder(Context context, String title, String subtitle, Date start, Date end) {
            initView(context);
            initData(title, subtitle, start, end);
        }

        //region Getters and Setters
        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;

        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        public void setListener(OnClickListener listener) {
            this.listener = listener;
        }

        public void setView(View rootView) {
            this.rootView = rootView;
            if (rootView != null) {
                rootView.setOnClickListener(this);
                startView = (TextView) rootView.findViewById(R.id.startView);
                endView = (TextView) rootView.findViewById(R.id.endView);
                titleView = (TextView) rootView.findViewById(R.id.titleView);
                subtitleView = (TextView) rootView.findViewById(R.id.subtitleView);
            }
        }

        @Override
        public View getView() {
            return rootView;
        }

        @Override
        public TimeLineView.MinuteInterval getTimeInterval() {
            if (start == null || end == null) {
                return null;
            }
            if (timeInterval == null) {
                timeInterval = new TimeLineView.MinuteInterval();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

            int startMinute = calendar.get(Calendar.MINUTE) + calendar.get(Calendar.HOUR_OF_DAY)*60;

            calendar.setTime(end);

            int endMinute = calendar.get(Calendar.MINUTE) + calendar.get(Calendar.HOUR_OF_DAY)*60;

            timeInterval.setData(startMinute, endMinute);

            return timeInterval;
        }

        public SimpleDateFormat getTimeFormatter() {
            if (timeFormatter == null) {
                timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
            }
            return timeFormatter;
        }

        public void setTimeFormatter(SimpleDateFormat formatter) {
            timeFormatter = formatter;
        }
        //endregion

        //region Methods
        protected void initView(Context context) {
            View view = View.inflate(context, R.layout.layout_event, null);
            setView(view);
        }

        public void initData(String title, String subtitle, Date start, Date end) {
            this.title = title;
            this.subtitle = subtitle;
            this.start = start;
            this.end = end;
            notifyDataChanged();
        }

        protected String formatTime(Date date) {
            if (date == null) { return null; }
            return getTimeFormatter().format(date);
        }

        public void notifyDataChanged() {
            if (titleView != null) { startView.setText(title); }
            if (subtitleView != null) { subtitleView.setText(subtitle); }

            String startStr = null
                    , endStr = null;

            TimeLineView.MinuteInterval timeInterval = getTimeInterval();
            if (getTimeInterval() != null) {
                int startMinute = timeInterval.getStart();
                int endMinute = timeInterval.getEnd();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, startMinute / 60);
                calendar.set(Calendar.MINUTE, startMinute % 60);

                startStr = formatTime(calendar.getTime());

                calendar.set(Calendar.HOUR_OF_DAY, endMinute/60);
                calendar.set(Calendar.MINUTE, endMinute%60);

                endStr = formatTime(calendar.getTime());
            }

            if (startView != null) { startView.setText(startStr); }
            if (endView != null) { endView.setText(endStr); }
        }

        @Override
        public void onClick(View view) {
            if (this.listener != null) {
                this.listener.onClick(this);
            }
        }

        @Override
        public String toString() {
            String str = timeInterval==null? "": "["+timeInterval.toString()+"]";
            return str+" "+title+" "+subtitle;
        }
        //endregion
    }
    //endregion
}
