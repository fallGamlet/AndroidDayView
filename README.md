# AndroidDayView
Android custom views for present day TimeLine or DayPager

![](images/img1.png =50x150)

Features
------------

* Day view timeline
* Day view calendar
* Horizontal and vertical scrolling
* Infinite horizontal scrolling
* Live preview of custom styling in xml preview window


Usage
---------
**TimeLine**

Add TimeLineView into a layout
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFF">

    <android.support.v4.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ru.fallgamlet.dayview.TimeLineView android:id="@+id/timeLineView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="#FFF"
            app:hourMin="6"
            app:hourMax="20"
            app:hourHeight="60dp"
            app:hourPaddingLeft="8dp"
            app:hourPaddingRight="4dp"
            app:hourLineWidth="0.5dp"
            app:hourLineColor="#777"
            app:hourBackground="#C0FFFFFF"
            app:hourTextColor="#4e4e4e"
            app:hourTextSize="12sp"
            app:disabledTimeColor="#99555555"/>

    </android.support.v4.widget.NestedScrollView>

</RelativeLayout>
```

Get *TimeLineView*
```java

TimeLineView timeLineView = (TimeLineView) findViewById(R.id.timeLineView);
```

For add events into *timeline* need create class with implement *TimeLineView.IEventHolder*. It must be holder with contain view
```java
public static class  MyEventHolder implements TimeLineView.IEventHolder {
  View rootView;
  public TimeLineView.MinuteInterval timeInterval;

  @Override
  public View getView() {
      return rootView;
  }

  @Override
  public TimeLineView.MinuteInterval getTimeInterval() {
      return timeInterval;
  }
  
  public MyEventHolder(Context context) {
      rootView = View.inflate(context, R.layout.layout_event, null);
  }
}
```

Add event view into *TimeLineView*
```java
MyEventHolder holder = new MyEventHolder(getContext());
holder.timeInterval = new TimeLineView.MinuteInterval(6*60, 7*60+20);
timeline.add(holder);
```
Add disabled interval into *TimeLineView*
```java
List<TimeLineView.MinuteInterval> disabledTimes = timeLineView.getDisabledTimes();
disabledTimes.add(new TimeLineView.MinuteInterval(8*HOUR+48, 10*HOUR+25));
disabledTimes.add(new TimeLineView.MinuteInterval(15*HOUR+15, 17*HOUR+5));
```

Add colored interval into *TimeLineView*
```java
timeLineView.addColoredInterval(new TimeLineView.ColoredInterval(Color.parseColor("#200000FF"), new TimeLineView.MinuteInterval(0, 7*HOUR+30)));
```

Set listener for time selection
```java
timeLineView.setOnTimeSelectListener(new TimeLineView.IOnTimeSelectListener() {
    @Override
    public void onTimePress(Object sender, int minute) {
        String txt = String.format(Locale.getDefault(), "Selected on %02d:%02d", minute/60, minute%60);
        showSnackbar(txt);
    }

    @Override
    public void onTimeLongPressed(Object sender, int minute) {
        String txt = String.format(Locale.getDefault(), "Selected on %02d:%02d", minute/60, minute%60);
        showSnackbar(txt);
    }
});

private void showSnackbar(String msg) {
    Snackbar.make(timeLineView, msg, Snackbar.LENGTH_SHORT).show();
}
```
