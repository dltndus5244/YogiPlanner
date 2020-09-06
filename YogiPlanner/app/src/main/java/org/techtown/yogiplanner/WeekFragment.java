package org.techtown.yogiplanner;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class WeekFragment extends Fragment {
    GridView gridView;
    GridAdapter adapter;
    ArrayList<String> timeList;
    ArrayList<Schedule> result;
    TextView tv_date;

    int curMonth;
    int curWeek;
    int curYear;

    ArrayList<Schedule> last_result;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_week, container, false);

        gridView = rootView.findViewById(R.id.gridView);
        tv_date = rootView.findViewById(R.id.tv_date);

        timeList = new ArrayList<String>();

        setTimeList();

        adapter = new GridAdapter(getContext(), timeList);
        gridView.setAdapter(adapter);

        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat curYearFormat = new SimpleDateFormat("YYYY", Locale.KOREA);
        SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);
        SimpleDateFormat curWeekFormat = new SimpleDateFormat("W", Locale.KOREA);

        curWeek = Integer.parseInt(curWeekFormat.format(date));
        curMonth = Integer.parseInt(curMonthFormat.format(date));
        curYear = Integer.parseInt(curYearFormat.format(date));

        tv_date.setText(curMonth + "월 " + curWeek + "째주");

        Button prev_btn = rootView.findViewById(R.id.prev_btn);
        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preTimeTable();
            }
        });

        Button next_btn = rootView.findViewById(R.id.next_btn);
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTimeTable();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        result = new ArrayList<Schedule>();
        last_result = new ArrayList<Schedule>();

        findWeekSchedule();

        return rootView;
    }

    public void findWeekSchedule() {
        last_result.clear();

        result = ((MainActivity)getActivity()).selectWeekSchedule();
        for (int i=0; i<result.size(); i++) {
            String start_date = result.get(i).getStart_date();
            String splitDate[] = start_date.split("/");

            int year = Integer.parseInt(splitDate[0]);
            int month = Integer.parseInt(splitDate[1]);
            int day = Integer.parseInt(splitDate[2]);

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);

            int week = cal.get(Calendar.WEEK_OF_MONTH);

            if (week == curWeek) {
                last_result.add(result.get(i));
            }
        }
        Log.d("WeekFragment", "크기 : " + last_result.size());
    }

    public void setTimeList() {
        int count = 0;

        for (int i=0; i<120; i++) {
            if (i % 8 == 0) {
                int j = 8;
                timeList.add("" + (j + count));
                count++;
            } else {
                timeList.add("");
            }
        }

    }

    public int findMaxWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, curYear);
        calendar.set(Calendar.MONTH, curMonth-1);

        int lastDate = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DATE, lastDate);

        int maxWeek = calendar.get(Calendar.WEEK_OF_MONTH);

        return maxWeek;
    }

    public void preTimeTable() {
        timeList.clear();

        setTimeList();

        if (curWeek == 1) {
            curMonth--;
            curWeek = findMaxWeek();
        }
        else {
            curWeek--;
        }

        if (curMonth == 0) {
            curYear--;
            curMonth = 12;
        }

        tv_date.setText(curMonth + "월 " + curWeek + "째주");
        findWeekSchedule();
        adapter.notifyDataSetChanged();

    }

    public void nextTimeTable() {
        timeList.clear();

        setTimeList();

        if (curWeek == findMaxWeek()) {
            curMonth++;
            curWeek = 1;
        }
        else {
            curWeek++;
        }

        if (curMonth == 13) {
            curYear++;
            curMonth = 1;
        }

        tv_date.setText(curMonth + "월 " + curWeek + "째주");
        findWeekSchedule();
        adapter.notifyDataSetChanged();
    }

    private class GridAdapter extends BaseAdapter {
        private final List<String> list;
        private final LayoutInflater inflater;

        public GridAdapter(Context context, List<String> list) {
            this.list = list;
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_timetable_gridview, parent, false);
                holder = new ViewHolder();

                holder.textView = (TextView) convertView.findViewById(R.id.textView);
                holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.linearLayout);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();

                holder.linearLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

            }

            holder.textView.setText(getItem(position));

            if (position % 8 == 0) {
                holder.linearLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }


            return convertView;
        }
    }

    private class ViewHolder {
        TextView textView;
        LinearLayout linearLayout;
    }
}
