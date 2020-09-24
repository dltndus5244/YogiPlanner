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
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class WeekFragment extends Fragment {
    static GridView gridView;
    static GridAdapter adapter;
    static ArrayList<String> timeList;
    ArrayList<Schedule> items;
    TextView tv_date;

    int curMonth;
    int curWeek;
    int curYear;

    static ArrayList<Schedule> week_items;
    int mPosition;

    static int passedIndex;
    static int passedPosition;
    private ScheduleDialog2 dialog;

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

        Calendar calendar = Calendar.getInstance();

        curYear = calendar.get(Calendar.YEAR);
        curMonth = calendar.get(Calendar.MONTH) + 1;
        curWeek = calendar.get(Calendar.WEEK_OF_MONTH);

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

        items = new ArrayList<Schedule>();
        week_items = new ArrayList<Schedule>();

        findWeekSchedule();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //아이템 클릭하면 다이얼로그 창 띄워주는거 구현해야함
                //클릭한 포지션의 스케줄의 아이디를 넘기면 되지 않을까? 아이디를 어떻게 알아낼건데....?
                if (timeList.get(position) != "") {
                    passedPosition = position;
                    passedIndex = Integer.parseInt(timeList.get(position));
                    Log.d("WeekFragment", "포지션 : " + passedIndex);
                    dialog = new ScheduleDialog2(getContext());
                    dialog.show();
                }
            }
        });

        return rootView;
    }

    public void setTimeList() { //시간표 셋팅해주는 함수
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

    public void findWeekSchedule() { //주에 해당하는 스케줄 가져오기
        week_items.clear();
        int week;
        Calendar cal = Calendar.getInstance();

        String sql = "SELECT _id, name, location, start_date, start_time, " +
                "end_date, end_time, repeat, memo from schedule ORDER BY start_date, start_time";

        items = ((MainActivity)getActivity()).selectSchedule(sql);

        for (int i=0; i<items.size(); i++) {
            String start_date = items.get(i).getStart_date();
            String splitDate[] = start_date.split("/");

            int year = Integer.parseInt(splitDate[0]);
            int month = Integer.parseInt(splitDate[1]);
            int day = Integer.parseInt(splitDate[2]);

            cal.set(year, month-1, day);

            week = cal.get(Calendar.WEEK_OF_MONTH);

            if (week == curWeek) {
                week_items.add(items.get(i));
            }
        }
    }

    public int findMaxWeek() { //해당 달의 마지막 주 찾아주는 함수
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

        /*
    그리드뷰의 아이템을 관리해주는 그리드어댑터
     */
    public class GridAdapter extends BaseAdapter {
        private final List<String> list;
        private final LayoutInflater inflater;
        ArrayList<Schedule> items = new ArrayList<>();

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

        public void setItems(ArrayList<Schedule> items) {
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_timetable_gridview, parent, false);
                holder = new ViewHolder();

                holder.textView = convertView.findViewById(R.id.textView);
                holder.linearLayout = convertView.findViewById(R.id.linearLayout);
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
                holder.linearLayout.setBackgroundColor(Color.WHITE);
            }

            holder.textView.setText(getItem(position));

            if (position % 8 == 0) { //시간 부분(8~22) 클릭 못하게
                holder.linearLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }

            int[] colors = getResources().getIntArray(R.array.Rainbow);

            for (int i=0; i<week_items.size(); i++) {
                String name = week_items.get(i).getName();

                String splitStartTime[] = week_items.get(i).start_time.split(":");
                int start_hour = Integer.parseInt(splitStartTime[0]);

                String splitEndTime[] = week_items.get(i).end_time.split(":");
                int end_hour = Integer.parseInt(splitEndTime[0]);

                String splitDate[] = week_items.get(i).start_date.split("/");

                int year = Integer.parseInt(splitDate[0]);
                int month = Integer.parseInt(splitDate[1]);
                int day = Integer.parseInt(splitDate[2]);

                Calendar cal = Calendar.getInstance();
                cal.set(year, month-1, day);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

                int startPosition = dayOfWeek + (8 * (start_hour - 8));
                int endPosition = (dayOfWeek + (8 * ((start_hour - 8) + (end_hour - start_hour - 1))));

                mPosition = startPosition;
                    //시간표 채워줌
                    while(mPosition <= endPosition) {
                        if (position == mPosition) {
                            holder.linearLayout.setBackgroundColor(colors[i]);

                            if (position == startPosition) {
                                holder.textView.setText(name);
                                holder.textView.setTextSize(13);
                            }
                        }
                        mPosition += 8;
                    }
                list.set(startPosition, String.valueOf(i));
            }

            return convertView;
        }
    }

    private class ViewHolder {
        TextView textView;
        LinearLayout linearLayout;
    }

}
