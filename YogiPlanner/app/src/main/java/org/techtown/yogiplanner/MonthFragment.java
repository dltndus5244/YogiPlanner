package org.techtown.yogiplanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MonthFragment extends Fragment {
    static ScheduleAdapter adapter;
    private ScheduleDialog schedule_dialog;
    private TodoDialog3 todo_dialog;
    static ArrayList<TimeItem> items;
    static RecyclerView recyclerView;
    static int mPosition;

    int maxDay;
    ArrayList<TimeItem> curMonthItems;
    TextView tvDate;
    static GridAdapter gridAdapter;
    ArrayList<String> dayList;
    GridView gridView;
    static Calendar mCal;
    Date date;

    int curYear;
    int curMonth;

    ArrayList<String> isTodoOwn = new ArrayList<>();
    ArrayList<String> isScheduleOwn = new ArrayList<>();

    static String click_date;

    int dayNum;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_month, container, false);

        tvDate = rootView.findViewById(R.id.tv_date);
        gridView = rootView.findViewById(R.id.gridview);

        date = new Date(System.currentTimeMillis());

        final SimpleDateFormat curYearFormat = new SimpleDateFormat("YYYY", Locale.KOREA);
        final SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);

        curYear = Integer.parseInt(curYearFormat.format(date));
        curMonth = Integer.parseInt(curMonthFormat.format(date))-1;

        tvDate.setText(curYear + "년 " + (curMonth+1) + "월"); //현재 년, 월 표시

        dayList = new ArrayList<String>(); //'일'을 저장할 배열리스트

        mCal = Calendar.getInstance();
        mCal.set(curYear, curMonth, 1);
        dayNum = mCal.get(Calendar.DAY_OF_WEEK); //이번달 1일이 무슨 요일인지 판단 (1~7:일~월)

        setCalendarDate(mCal.get(Calendar.MONTH)+1);

        gridAdapter = new GridAdapter(getContext(), dayList);
        gridView.setAdapter(gridAdapter);

        Button prev_button = rootView.findViewById(R.id.prev_btn);
        prev_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preCalendar();
            }
        });

        Button next_button = rootView.findViewById(R.id.next_btn);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextCalendar();
            }
        });

        recyclerView = rootView.findViewById(R.id.recyclerView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                click_date = curYear + "/" + addZero(curMonth+1) + "/" + addZero(Integer.parseInt(dayList.get(position)));

                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(layoutManager);

                adapter = new ScheduleAdapter();
                recyclerView.setAdapter(adapter);

                //클릭한 날짜에 맞는 스케줄을 가져와서 리사이클러뷰에 보여줌
                String sql = "SELECT * FROM time WHERE start_date = " + "'" + click_date + "'" + " ORDER BY start_date, start_time";
                items = ((MainActivity)getActivity()).selectTime(sql);
                adapter.setItems(items);

                //리사이클러뷰의 아이템을 클릭했을 경우 다이얼로그창을 띄워 상세 정보 보여줌
                adapter.setOnItemClickListener(new OnScheduleItemClickListener() {
                        @Override
                        public void onItemClick(ScheduleAdapter.ViewHolder holder, View view, int position) {
                            mPosition = position;
                            TimeItem item = items.get(position);

                            if (item.getType().equals("schedule")) {
                                schedule_dialog = new ScheduleDialog(getContext());
                                schedule_dialog.show();
                            }

                            else if (item.getType().equals("todo")) {
                                todo_dialog = new TodoDialog3(getContext());
                                todo_dialog.show();
                            }
                        }
                    });
                }
        });


        return rootView;
    }

    public void setCalendarDate(int month) { //날짜를 채워주는 함수
        isTodoOwn.clear();
        isScheduleOwn.clear();
        dayList.clear();
        mCal.set(Calendar.MONTH, month - 1);

        for (int i=1; i<dayNum; i++)
            dayList.add("");

        for (int i = 0; i < mCal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            dayList.add("" + (i + 1));
        }
        maxDay = mCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String first = "'"+mCal.get(Calendar.YEAR)+"/"+addZero(mCal.get(Calendar.MONTH)+1)+"/01'";
        String last = "'"+mCal.get(Calendar.YEAR)+"/"+addZero(mCal.get(Calendar.MONTH)+1)+"/"+maxDay+"'";

        String sql = "SELECT * FROM time WHERE start_date BETWEEN " + first + "AND " + last;
        curMonthItems = ((MainActivity)getActivity()).selectTime(sql);

        if (curMonthItems.size() > 0) {
            for (int i = 0; i < curMonthItems.size(); i++) {
                String[] date = curMonthItems.get(i).getStart_date().split("/");
                if (curMonthItems.get(i).getType().equals("todo")) {
                    if (isTodoOwn.contains(date[2]) == false)
                        isTodoOwn.add(date[2]);
                } else if (curMonthItems.get(i).getType().equals("schedule")) {
                    if (isScheduleOwn.contains(date[2]) == false)
                        isScheduleOwn.add(date[2]);
                }
            }
        }

        Log.d("MonthFragment", "-----");
        for (int i=0; i<isScheduleOwn.size(); i++) {
            Log.d("MonthFragment", isScheduleOwn.get(i)+"");
        }
    }

    public void preCalendar() { //이전 달 달력 셋팅 함수
        dayList.clear();
        mCal = Calendar.getInstance();

        if (curMonth == 0) {
            curYear--;
            curMonth = 11;
        }
        else {
            curMonth--;
        }

        tvDate.setText(curYear + "년 " + (curMonth+1) + "월");

        mCal.set(curYear, curMonth, 1);
        dayNum = mCal.get(Calendar.DAY_OF_WEEK);

        setCalendarDate(mCal.get(Calendar.MONTH)+1);
        gridAdapter.notifyDataSetChanged();

    }

    public void nextCalendar() { //다음 달 달력 셋팅 함수
        dayList.clear();
        mCal = Calendar.getInstance();

        if (curMonth == 11) {
            curYear++;
            curMonth = 0;
        }
        else {
            curMonth++;
        }

        tvDate.setText(curYear + "년 " + (curMonth+1) + "월");

        mCal.set(curYear, curMonth, 1);
        dayNum = mCal.get(Calendar.DAY_OF_WEEK);

        setCalendarDate(mCal.get(Calendar.MONTH)+1);
        gridAdapter.notifyDataSetChanged();
    }

    /*
    그리드뷰의 아이템을 관리해주는 그리드어댑터
     */
    public class GridAdapter extends BaseAdapter {
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
                convertView = inflater.inflate(R.layout.item_calendar_gridview, parent, false);
                holder = new ViewHolder();

                holder.tvItemGridView = convertView.findViewById(R.id.tv_item_gridview);
                holder.imageView = convertView.findViewById(R.id.imageView);
                holder.itemLinear = convertView.findViewById(R.id.itemLinear);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();

                holder.tvItemGridView.setTextColor(getResources().getColor(R.color.button_text));
                holder.tvItemGridView.setTypeface(null, Typeface.NORMAL);

                holder.itemLinear.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

                holder.imageView.setImageResource(0);

            }

            holder.tvItemGridView.setText("" + getItem(position)); //그리드뷰에 날짜 표시해줌

            mCal = Calendar.getInstance();

            int month = mCal.get(Calendar.MONTH);
            int today = mCal.get(Calendar.DAY_OF_MONTH);
            String sToday = String.valueOf(today);

            //오늘날짜에 빨간색 표시(색은 후에 수정)
            if (month == curMonth && sToday.equals(getItem(position))) {
                holder.tvItemGridView.setTextColor(getResources().getColor(R.color.today));
                holder.tvItemGridView.setTypeface(null, Typeface.BOLD);
            }

            //빈 공간 클릭 못하게
            if (getItem(position) == "") {
                holder.itemLinear.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }

            //할 일이 있는 날짜면 별 표시
            if (isTodoOwn.contains(getItem(position))) {
                holder.imageView.setImageResource(R.drawable.star);
            }

            if (isScheduleOwn.contains(getItem(position)) && isTodoOwn.contains(getItem(position)) == false) {
                holder.imageView.setImageResource(R.drawable.color_dot);
            }

            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvItemGridView;
        ImageView imageView;
        LinearLayout itemLinear;
    }

    public String addZero(int i) {
        if (i < 10) {
            return "0" + String.valueOf(i);
        }
        else {
            return String.valueOf(i);
        }
    }
}
