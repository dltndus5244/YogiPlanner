package org.techtown.yogiplanner;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
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
    ArrayList<TimeItem> items;
    TextView tv_date;

    int curMonth;
    int curWeek;
    int curYear;

    static ArrayList<TimeItem> week_items;
    int mPosition;

    static int passedIndex;
    static int passedPosition;
    private ScheduleDialog2 schedule_dialog;
    private TodoDialog2 todo_dialog;

    ArrayList<String> isTodoOwn = new ArrayList<>();


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

        items = new ArrayList<TimeItem>();
        week_items = new ArrayList<TimeItem>();

        findWeekSchedule();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (timeList.get(position) != "") {
                    passedPosition = position;
                    passedIndex = Integer.parseInt(timeList.get(position));
//                    Log.d("WeekFragment", "passedPosition : " + passedPosition);
//                    Log.d("WeekFragment", "passedIndex : " + passedIndex);

                    TimeItem timeItem = week_items.get(passedIndex);

                    if (timeItem.getType().equals("schedule")) {
                        schedule_dialog = new ScheduleDialog2(getContext());
                        schedule_dialog.show();

                    }

                    else if (timeItem.getType().equals("todo")) {
                        todo_dialog = new TodoDialog2(getContext());
                        todo_dialog.show();
                    }
                }
            }
        });

        return rootView;
    }

    public void setTimeList() { //시간표 셋팅해주는 함수
        timeList.clear();
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
        isTodoOwn.clear();
        week_items.clear();
        int week;
        Calendar cal = Calendar.getInstance();

        String sql = "SELECT * FROM time ORDER BY start_date, start_time";

        items = ((MainActivity)getActivity()).selectTime(sql);

        for (int i=0; i<items.size(); i++) {
            String start_date = items.get(i).getStart_date();
            String splitDate[] = start_date.split("/");

            int year = Integer.parseInt(splitDate[0]);
            int month = Integer.parseInt(splitDate[1]);
            int day = Integer.parseInt(splitDate[2]);

            cal.set(year, month-1, day);
            week = cal.get(Calendar.WEEK_OF_MONTH);

            if (year == curYear && month == curMonth &&week == curWeek) {
                week_items.add(items.get(i));
            }
        }

        for (int i=0; i<week_items.size(); i++) {
            if (week_items.get(i).getType().equals("todo")) {
                String splitStartTime[] = week_items.get(i).start_time.split(":");
                int start_hour = Integer.parseInt(splitStartTime[0]);

                String splitDate[] = week_items.get(i).start_date.split("/");

                int year = Integer.parseInt(splitDate[0]);
                int month = Integer.parseInt(splitDate[1]);
                int day = Integer.parseInt(splitDate[2]);

                Calendar cal2 = Calendar.getInstance();
                cal2.set(year, month-1, day);
                int dayOfWeek = cal2.get(Calendar.DAY_OF_WEEK);

                int startPosition = dayOfWeek + (8 * (start_hour - 8));

                isTodoOwn.add(Integer.toString(startPosition));
            }
        }
        Log.d("WeekFragment", "-----");
        for (int i=0; i<isTodoOwn.size(); i++) {
            Log.d("WeekFragment", isTodoOwn.get(i)+"");
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
        ArrayList<TimeItem> items = new ArrayList<>();

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

        public void setItems(ArrayList<TimeItem> items) {
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_timetable_gridview, parent, false);
                holder = new ViewHolder();

                holder.imageView = convertView.findViewById(R.id.imageView);
                holder.textView = convertView.findViewById(R.id.textView);
                holder.linearLayout = convertView.findViewById(R.id.linearLayout);
                holder.minute_layout1 = convertView.findViewById(R.id.minute_layout1);
                holder.minute_layout2 = convertView.findViewById(R.id.minute_layout2);
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

                holder.minute_layout1.setBackgroundColor(Color.WHITE);
                holder.minute_layout2.setBackgroundColor(Color.WHITE);
                holder.imageView.setImageResource(0);
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

            for (int i=0; i<week_items.size(); i++) {
                String name = week_items.get(i).getName();
                if (name.length() > 5) {
                    name = name.substring(0, 5);
                }

                String splitStartTime[] = week_items.get(i).start_time.split(":");
                int start_hour = Integer.parseInt(splitStartTime[0]);
                int start_minute = Integer.parseInt(splitStartTime[1]);

                String splitEndTime[] = week_items.get(i).end_time.split(":");
                int end_hour = Integer.parseInt(splitEndTime[0]);
                int end_minute = Integer.parseInt(splitEndTime[1]);

                String splitDate[] = week_items.get(i).start_date.split("/");

                int year = Integer.parseInt(splitDate[0]);
                int month = Integer.parseInt(splitDate[1]);
                int day = Integer.parseInt(splitDate[2]);

                Calendar cal = Calendar.getInstance();
                cal.set(year, month-1, day);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

                if (start_hour < 8) {
                    start_hour = 8;
                }
                int startPosition = dayOfWeek + (8 * (start_hour - 8));
                int endPosition;

                if (end_minute == 30) {
                    endPosition = (dayOfWeek + (8 * ((start_hour - 8) + (end_hour - start_hour))));
                } else {
                    endPosition = (dayOfWeek + (8 * ((start_hour - 8) + (end_hour - start_hour - 1))));
                }

                mPosition = startPosition;

                //같은 일정은 같은 색으로 표시
                int my_id; //색 결정할 time테이블에서의 id

                final String item_type = week_items.get(i).getType();

                if(item_type.equals("schedule")) {

                    Cursor cursor = ((MainActivity) getActivity()).database.rawQuery("SELECT * from schedule WHERE _id = " + week_items.get(i).getItem_id(), null);
                    cursor.moveToFirst();
                    my_id = cursor.getInt(9);
                    if (my_id == 0) {
                        my_id = week_items.get(i).getItem_id();
                    }

                }
                else if(item_type.equals("todo")) my_id = week_items.get(i).getItem_id() + 365;
                else my_id = 0;

                final Random mRandom = new Random(my_id);

                final int baseColor = Color.WHITE;

                final int baseRed = Color.red(baseColor);
                final int baseGreen = Color.green(baseColor);
                final int baseBlue = Color.blue(baseColor);

                final int red = (baseRed + mRandom.nextInt(256)) / 2;
                final int green = (baseGreen + mRandom.nextInt(256)) / 2;
                final int blue = (baseBlue + mRandom.nextInt(256)) / 2;

                if (startPosition == endPosition) { //30분이거나 1시간인 경우
                    if (position == startPosition) {
                        if (start_minute == 30) {
                            holder.minute_layout2.setBackgroundColor(Color.rgb(red, green, blue));
                            holder.textView.setText("");
                        } else if (end_minute == 30) {
                            holder.minute_layout1.setBackgroundColor(Color.rgb(red, green, blue));
                            holder.textView.setText("");
                        } else if (start_minute == 30 && end_minute == 30) {
                            holder.minute_layout2.setBackgroundColor(Color.rgb(red, green, blue));
                            int p = startPosition + 8;
                            if (position == p) {
                                holder.minute_layout1.setBackgroundColor(Color.rgb(red, green, blue));
                            }
                        } else {
                            holder.minute_layout1.setBackgroundColor(Color.rgb(red, green, blue));
                            holder.minute_layout2.setBackgroundColor(Color.rgb(red, green, blue));

                            holder.textView.setEllipsize(TextUtils.TruncateAt.END);
                            holder.textView.setSingleLine();
                            holder.textView.setText(name);
                            holder.textView.setTextSize(11);
                        }

                        if (isTodoOwn.contains(String.valueOf(startPosition)))
                            holder.imageView.setImageResource(R.drawable.star);
                    }
                } else { //1시간 이상인 경우

                    if (position == startPosition) {
                        if (start_minute == 30) {
                            holder.minute_layout2.setBackgroundColor(Color.rgb(red, green, blue));
                        } else {
                            holder.minute_layout1.setBackgroundColor(Color.rgb(red, green, blue));
                            holder.minute_layout2.setBackgroundColor(Color.rgb(red, green, blue));
                        }

                        holder.textView.setText(name);
                        holder.textView.setTextSize(11);

                        if (isTodoOwn.contains(String.valueOf(startPosition)))
                            holder.imageView.setImageResource(R.drawable.star);
                    }

                    mPosition += 8;

                    while(mPosition < endPosition) {
                        if (position == mPosition) {
                            holder.minute_layout1.setBackgroundColor(Color.rgb(red, green, blue));
                            holder.minute_layout2.setBackgroundColor(Color.rgb(red, green, blue));
                        }
                        mPosition += 8;
                    }

                    if (position == endPosition) {
                        if (end_minute == 30) {
                            holder.minute_layout1.setBackgroundColor(Color.rgb(red, green, blue));
                        } else {
                            holder.minute_layout1.setBackgroundColor(Color.rgb(red, green, blue));
                            holder.minute_layout2.setBackgroundColor(Color.rgb(red, green, blue));
                        }
                    }
                }

                list.set(startPosition, String.valueOf(i));

            }

            return convertView;
        }
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textView;
        LinearLayout linearLayout;
        LinearLayout minute_layout1;
        LinearLayout minute_layout2;

    }

}
