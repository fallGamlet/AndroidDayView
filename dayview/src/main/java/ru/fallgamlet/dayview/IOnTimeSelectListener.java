package ru.fallgamlet.dayview;

/**
 * Created by fallgamlet on 17.08.17.
 */

public interface IOnTimeSelectListener {
    void onTimePress(Object sender, int minute);
    void onTimeLongPressed(Object sender, int minute);
}
