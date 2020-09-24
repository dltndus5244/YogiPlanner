package org.techtown.yogiplanner;

import android.view.View;

public interface OnTodoItemClickListener {
    public void onItemClick(TodoAdapter.ViewHolder holder, View view, int position);
}
