# AndroidDayView
Android custom views for present day TimeLine or DayPager


### How to use
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
