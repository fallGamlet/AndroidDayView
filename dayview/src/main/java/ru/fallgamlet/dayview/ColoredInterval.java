package ru.fallgamlet.dayview;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fallgamlet on 17.08.17.
 */

public class ColoredInterval {

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

                return mi1.getStart() - mi2.getStart();
            }
        });
    }
    //endregion
}
