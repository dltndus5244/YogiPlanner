package org.techtown.yogiplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder>
                                        implements OnTodoItemClickListener {
    ArrayList<Todo> items = new ArrayList<Todo>();
    OnTodoItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.todo_item, parent, false);

        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Todo item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Todo item) {
        items.add(item);
    }

    public void setItems(ArrayList<Todo> items) {
        this.items = items;
    }

    public Todo getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, Todo item) {
        items.set(position, item);
    }

    public void setOnItemClickListener(OnTodoItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView dueDate;
        TextView dueTime;
        TextView dDay;
        TextView dHour;
        TextView dMinute;

        public ViewHolder(@NonNull View itemView, final OnTodoItemClickListener listener) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            dueDate = itemView.findViewById(R.id.dueDate);
            dueTime = itemView.findViewById(R.id.dueTime);

            dDay = itemView.findViewById(R.id.dDay);
            dHour = itemView.findViewById(R.id.dHour);
            dMinute = itemView.findViewById(R.id.dMinute);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (listener != null) {
                        listener.onItemClick(ViewHolder.this, v, position);
                    }
                }
            });
        }

        public void setItem(Todo item) {
            name.setText(item.getName());
            dueDate.setText(item.getDate());
            dueTime.setText(item.getTime());

            Calendar nowDateTime = Calendar.getInstance();
            Calendar dueDateTime = Calendar.getInstance();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            int year = 0, month = 0, day = 0;
            int hour = 0, minute = 0;

            try {
                Date date = dateFormat.parse(item.getDate());
                Date time = timeFormat.parse(item.getTime());

                year = date.getYear() + 1900;
                month = date.getMonth();
                day = date.getDate();

                hour = time.getHours();
                minute =time.getMinutes();

                dueDateTime.set(year, month, day, hour, minute, 0); //마감날짜 시간을 캘린더에 설정

            } catch (Exception e) {};

            long diff = dueDateTime.getTimeInMillis() - nowDateTime.getTimeInMillis();
            long diffDay = diff / (24*60*60*1000);
            long diffHour = diff / (60*60*1000);
            long diffMinute = diff / (60*1000);

            if (diffDay >= 1) {
                dDay.setText("D-" + diffDay);

                if (diffHour - (diffDay)*24 == 0) {
                    dHour.setText("");
                    dMinute.setText((diffMinute - (diffHour)*60) + 1 + "분");
                } else {
                    dHour.setText(diffHour - (diffDay)*24 + "시간");
                    dMinute.setText((diffMinute - (diffHour)*60) + 1 + "분");
                }
            } else if (diffDay == 0) {
                dDay.setText("D-day");

                if (diffHour - (diffDay)*24 == 0) {
                    dHour.setText("");
                    dMinute.setText((diffMinute - (diffHour)*60) + 1 + "분");
                } else {
                    dHour.setText(diffHour - (diffDay)*24 + "시간");
                    dMinute.setText((diffMinute - (diffHour)*60) + 1 + "분");
                }

            }

        }
    }
}
