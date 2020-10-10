package org.techtown.yogiplanner;

import android.view.View;

public interface OnScheduleItemClickListener {
    public void onItemClick(ScheduleAdapter.ViewHolder holder, View view, int position);
}
