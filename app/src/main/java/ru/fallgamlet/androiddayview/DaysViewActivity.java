package ru.fallgamlet.androiddayview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import ru.fallgamlet.dayview.DayViewPager;
import ru.fallgamlet.dayview.TimeLineView;

public class DaysViewActivity extends AppCompatActivity {

    //region Fields
    DayViewPager dayViewPager;
    Random random = new Random();
    List<TimeLineView.MinuteInterval> workIntervals = new ArrayList<>(7);
    List<Integer> colors = new ArrayList<>();
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_days_view);

        dayViewPager = (DayViewPager) findViewById(R.id.dayViewPager);
        dayViewPager.setOnContentListener(getContextListener());
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
        int count = 5+random.nextInt(15);
        int hour = 60;
        int minMinute = workTime.getStart()*hour;
        int maxMinute = workTime.getEnd()*hour;

        for (int i=0; i< count; i++) {
            int start = minMinute + random.nextInt(maxMinute-minMinute);
            int minLength = 20;
            int length = maxMinute - start - minLength;
            length = length < 0? minLength: minLength+random.nextInt(length);
            if (length > 2*hour) { length = 2*hour; }
            int end = start + length;

            String title = "title";
            String subtitle = "subtitle";
            TimeLineView.MinuteInterval interval = new TimeLineView.MinuteInterval(start, end);

            MyEventHolder holder = new MyEventHolder(DaysViewActivity.this, title, subtitle, interval);
            int bgColor = getRandomColor();
            holder.getView().setBackgroundColor(bgColor);
            holderList.add(holder);
        }

        return holderList;
    }


    //region OnContextListener
    DayViewPager.OnContentListener contextListener;
    @NonNull
    protected DayViewPager.OnContentListener getContextListener() {
        return new DayViewPager.OnContentListener() {

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
    //endregion


    interface OnClickListener {
        void onClick(TimeLineView.IEventHolder holder);
    }

    public static class  MyEventHolder implements TimeLineView.IEventHolder, View.OnClickListener {
        private TimeLineView.MinuteInterval timeInterval;
        private String title;
        private String subtitle;
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

        public MyEventHolder(Context context, String title, String subtitle, TimeLineView.MinuteInterval timeInterval) {
            initView(context);
            initData(title, subtitle, timeInterval);
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
            return timeInterval;
        }

        public void setTimeInterval(TimeLineView.MinuteInterval timeInterval) {
            this.timeInterval = timeInterval;
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

        public void initData(String title, String subtitle, TimeLineView.MinuteInterval timeInterval) {
            this.title = title;
            this.subtitle = subtitle;
            this.timeInterval = timeInterval;
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
            if (timeInterval != null) {
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

}
