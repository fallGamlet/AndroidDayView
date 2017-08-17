package ru.fallgamlet.dayview;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fallgamlet on 17.08.17.
 */

public class Cluster {

    //region Fields
    MinuteInterval timeInterval;
    List<List<IEventHolder>> columns = new ArrayList<>();
    //endregion

    //region Constructor
    public Cluster() {
        timeInterval = null;
    }
    //endregion

    //region Methods
    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public int getColumns() {
        return columns.size();
    }

    public boolean isCollide(MinuteInterval timeInterval) {
        return MinuteInterval.isCollide(this.timeInterval, timeInterval);
    }

    protected boolean isCollideForColumn(List<IEventHolder> column, IEventHolder holder) {
        if (holder == null || column == null || column.isEmpty()) { return false; }
        boolean check = false;
        for (IEventHolder item: column) {
            if (MinuteInterval.isCollide(item.getTimeInterval(), holder.getTimeInterval())) {
                check = true;
                break;
            }
        }
        return check;
    }

    protected int indexOfCollideColumn(IEventHolder holder) {
        int index = -1;
        if (holder != null && !isEmpty()) {
            int i=0;
            for (List<IEventHolder> column: columns) {
                if(isCollideForColumn(column, holder)) {
                    index = i;
                    break;
                }
                i++;
            }
        }
        return index;
    }

    protected int indexOfNotCollideColumn(IEventHolder holder) {
        int index = -1;
        if (holder != null && !isEmpty()) {
            int i=0;
            for (List<IEventHolder> column: columns) {
                if(!isCollideForColumn(column, holder)) {
                    index = i;
                    break;
                }
                i++;
            }
        }
        return index;
    }

    public MinuteInterval getTimeInterval() {
        return timeInterval;
    }

    public boolean add(IEventHolder holder) {
        if (holder == null || holder.getTimeInterval() == null) {
            return false;
        }
        if (isEmpty()) {
            timeInterval = holder.getTimeInterval();
            List<IEventHolder> column = new ArrayList<>();
            column.add(holder);
            columns.add(column);
            return true;
        }
        if (!isCollide(holder.getTimeInterval())) {
            return false;
        }

        MinuteInterval ti = MinuteInterval.union(timeInterval, holder.getTimeInterval());
        int i = indexOfNotCollideColumn(holder);
        if (i>=0) {
            columns.get(i).add(holder);
        } else {
            List<IEventHolder> column = new ArrayList<>();
            column.add(holder);
            columns.add(column);
        }
        this.timeInterval = ti;
        return true;
    }

    /* Without test for collide clusters
     */
    public static Cluster unionClusters(Cluster... clusters) {
        if (clusters == null || clusters.length == 0) {
            return null;
        }

        Cluster cluster = null;
        Cluster bufCluster = null;
        List<IEventHolder> holders = new ArrayList<>(100);
        MinuteInterval timeInterval = null;
        for (int i=0; i<clusters.length; i++) {
            bufCluster = clusters[i];
            if (bufCluster != null && !bufCluster.isEmpty()) {
                timeInterval = MinuteInterval.union(timeInterval, bufCluster.getTimeInterval());
                holders.addAll(bufCluster.getAllItems());
            }
        }
        if (timeInterval != null && !holders.isEmpty()) {
            cluster = new Cluster();
            cluster.timeInterval = timeInterval;
            for (IEventHolder holder: holders) {
                cluster.add(holder);
            }
        }
        return cluster;
    }

    public MinuteInterval remathTimeInterval() {
        MinuteInterval mi = null;
        for (List<IEventHolder> column : columns) {
            for (IEventHolder item: column) {
                mi = MinuteInterval.union(mi, item.getTimeInterval());
            }
        }
        this.timeInterval = mi;
        return mi;
    }

    public void clear() {
        timeInterval = null;
        columns.clear();
    }

    @NonNull
    public ArrayList<IEventHolder> getAllItems() {
        ArrayList<IEventHolder> items = new ArrayList<>();
        for (List<IEventHolder> column : columns) {
            items.addAll(column);
        }
        return items;
    }
    //endregion
}
