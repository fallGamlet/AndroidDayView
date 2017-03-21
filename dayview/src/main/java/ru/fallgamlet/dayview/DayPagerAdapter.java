package ru.fallgamlet.dayview;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.List;

/**
 * Created by fallgamlet on 06.03.17.
 */

public class DayPagerAdapter extends PagerAdapter {
    //region Sub classes and interfaces
    public abstract static class ViewHolder {
        //region Fields
        private View view;
        //endregion

        //region Constructors
        public ViewHolder() {
            this.view = null;
        }

        public ViewHolder(View view) {
            this.view = view;
        }
        //endregion

        //region Getters and Setters
        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }
        //endregion
    }


    public interface OnContentListener {
        @NonNull
        ViewHolder onCreateViewHolder(int position);
        void onBindData(ViewHolder holder, int position);
    }
    //endregion

    //region Fields
    public static final int MAX_PAGES = 20000;
    private final int PAGE_CACH_COUNT = 3;
    private final long DAY = 24*60*60*1000;
    private ViewHolder[] dayViews = new ViewHolder[PAGE_CACH_COUNT];
    private OnContentListener listener;
    //endregion

    public DayPagerAdapter(@NonNull OnContentListener listener) {
        super();
        setOnContentListener(listener);
    }

    public void setOnContentListener(OnContentListener listener) {
        this.listener = listener;
    }

    private int getInnerPosition(int position) {
        int localPos = position % PAGE_CACH_COUNT;
        if (localPos < 0) { localPos += PAGE_CACH_COUNT; }
//        return ((position % PAGE_CACH_COUNT) + PAGE_CACH_COUNT) % PAGE_CACH_COUNT;
        return localPos;
    }

    private ViewHolder getViewHolder(int innerPosition) {
        if (innerPosition < 0 || innerPosition >= PAGE_CACH_COUNT) {
            innerPosition = getInnerPosition(innerPosition);
        }
        return dayViews[innerPosition];
    }

    private void setViewHolder(ViewHolder holder, int innerPosition) {
        if (innerPosition < 0 || innerPosition >= PAGE_CACH_COUNT) {
            innerPosition = getInnerPosition(innerPosition);
        }
        dayViews[innerPosition] = holder;
    }

    public void clear() {
        for (int i=0; i<PAGE_CACH_COUNT; i++) {
            dayViews[i] = null;
        }
    }

    @Override
    public int getCount() {
        return MAX_PAGES;
    }

    public int getRealCount() {
        return PAGE_CACH_COUNT;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view != null && view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int innerPos = getInnerPosition(position);

        ViewHolder holder = getViewHolder(innerPos);
        if (holder == null) {
            holder = listener.onCreateViewHolder(position);
            setViewHolder(holder, innerPos);
            container.addView(holder.getView(), 0);
        }
        listener.onBindData(holder, position);
        if (holder.getView() != null) {
//            holder.getView().setVisibility(View.VISIBLE);
            holder.getView().invalidate();
        }

        if (container == null) { return null; }

//        container.removeView(holder.getView());
//        container.addView(holder.getView(), 0);
        return holder.getView();
//        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        int innerPos = getInnerPosition(position);
//        if (0<= innerPos && innerPos < PAGE_CACH_COUNT) {
//            ViewHolder holder = getViewHolder(innerPos);
//            View view = holder == null? null: holder.getView();
//            if (view != null) {
//                container.removeView(view);
//            }
//        }
    }
}
