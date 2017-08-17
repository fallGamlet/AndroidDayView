package ru.fallgamlet.dayview;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Created by fallgamlet on 17.08.17.
 */

public class MinuteInterval{

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
