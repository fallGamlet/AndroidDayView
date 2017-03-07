package ru.fallgamlet.androiddayview;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

import ru.fallgamlet.dayview.TimeLineView;


public class DayViewActivity extends AppCompatActivity {

    TimeLineView timeLineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);


        timeLineView = (TimeLineView) findViewById(R.id.timeLineView);

        //region Init events
        List<MyEventHolder> events = new ArrayList<>();

        final int HOUR = 60;
        int startMinute, endMinute;
        startMinute = 6*HOUR;
        endMinute = startMinute + 2*HOUR;
        events.add(new MyEventHolder(this, "Title", "Subtile", new TimeLineView.MinuteInterval(startMinute, endMinute)));

        startMinute = 9*HOUR;
        endMinute = startMinute + 1*HOUR;
        events.add(new MyEventHolder(this, "Title", "Subtile", new TimeLineView.MinuteInterval(startMinute, endMinute)));

        startMinute = 9*HOUR;
        endMinute = startMinute + 2*HOUR;
        events.add(new MyEventHolder(this, "Title", "Subtile", new TimeLineView.MinuteInterval(startMinute, endMinute)));

        startMinute = 10*HOUR + 30;
        endMinute = startMinute + 1*HOUR;
        events.add(new MyEventHolder(this, "Title", "Subtile", new TimeLineView.MinuteInterval(startMinute, endMinute)));

        startMinute = 10*HOUR+30;
        endMinute = startMinute + 2*HOUR;
        events.add(new MyEventHolder(this, "Title", "Subtile", new TimeLineView.MinuteInterval(startMinute, endMinute)));

        startMinute = 12*HOUR;
        endMinute = startMinute + 1*HOUR;
        events.add(new MyEventHolder(this, "Title", "Subtile", new TimeLineView.MinuteInterval(startMinute, endMinute)));

        startMinute = 12*HOUR+40;
        endMinute = startMinute + 1*HOUR+30;
        events.add(new MyEventHolder(this, "Title", "Subtile", new TimeLineView.MinuteInterval(startMinute, endMinute)));
        //endregion

        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(TimeLineView.IEventHolder holder) {
                showSnackbar(holder.toString());
            }
        };

        for (MyEventHolder event: events) {
            event.setListener(clickListener);
        }

        //region Add disabled intervals
        List<TimeLineView.MinuteInterval> disabledTimes = timeLineView.getDisabledTimes();
        disabledTimes.add(new TimeLineView.MinuteInterval(8*HOUR+48, 10*HOUR+25));
        disabledTimes.add(new TimeLineView.MinuteInterval(15*HOUR+15, 17*HOUR+5));
        //endregion

        //region Add colored intervals
        timeLineView.addColoredInterval(new TimeLineView.ColoredInterval(Color.parseColor("#200000FF"), new TimeLineView.MinuteInterval(0, 7*HOUR+30)));
        timeLineView.addColoredInterval(new TimeLineView.ColoredInterval(Color.parseColor("#2000FF00"), new TimeLineView.MinuteInterval(7*HOUR+25, 11*HOUR+40)));
        timeLineView.addColoredInterval(new TimeLineView.ColoredInterval(Color.parseColor("#20FF0000"), new TimeLineView.MinuteInterval(11*HOUR+40, 16*HOUR)));
        timeLineView.addColoredInterval(new TimeLineView.ColoredInterval(Color.parseColor("#20FFFF00"), new TimeLineView.MinuteInterval(16*HOUR, 21*HOUR)));
        timeLineView.addColoredInterval(new TimeLineView.ColoredInterval(Color.parseColor("#2000FFFF"), new TimeLineView.MinuteInterval(21*HOUR, 25*HOUR)));
        //endregion

        timeLineView.setData(events);
        timeLineView.invalidate();

        timeLineView.setOnTimeSelectListener(new TimeLineView.IOnTimeSelectListener() {
            @Override
            public void onTimePress(Object sender, int minute) {
                onPressed(sender, minute);
            }

            @Override
            public void onTimeLongPressed(Object sender, int minute) {
                onPressed(sender, minute);
            }

            private void onPressed(Object sender, int minute) {
                String txt = String.format(Locale.getDefault(), "Selected on %02d:%02d", minute/60, minute%60);
                showSnackbar(txt);
            }
        });
    }

    private void showSnackbar(String msg) {
        Snackbar.make(timeLineView, msg, Snackbar.LENGTH_SHORT).show();
    }

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
                timeFormatter = new SimpleDateFormat("hh:mm", Locale.getDefault());
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
            view.setFocusable(false);
//            view.setFocusableInTouchMode(false);
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
            String str = timeInterval==null? "": timeInterval.toString();
            return str+"\n"+title+"\n"+subtitle;
        }

        //endregion
    }
}
