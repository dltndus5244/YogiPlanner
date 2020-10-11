package org.techtown.yogiplanner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    MonthFragment monthFragment;
    WeekFragment weekFragment;
    TodayFragment todayFragment;
    AddScheduleFragment scheduleFragment;
    AddToDoFragment toDoFragment;

    public static SQLiteDatabase database;

    public static Context mContext;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm");

    private FloatingActionButton fab_main, fab_sub1, fab_sub2;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    public static ArrayList<TimeItem> alarm_items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this; // 클래스에서 메인 액티비티를 참조하기 위함

        monthFragment = new MonthFragment();
        weekFragment = new WeekFragment();
        todayFragment = new TodayFragment();
        scheduleFragment = new AddScheduleFragment();
        toDoFragment = new AddToDoFragment();

        //하단탭 선택하면 프래그먼트 바꿔 보여주기
        getSupportFragmentManager().beginTransaction().replace(R.id.container, monthFragment).commit();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab1:
                        replaceFragment(monthFragment);
                        fab_main.show();
                        return true;
                    case R.id.tab2:
                        replaceFragment(weekFragment);
                        fab_main.show();
                        return true;
                    case R.id.tab3:
                        replaceFragment(todayFragment);
                        fab_main.show();
                        return true;
                }
                return false;
            }
        });

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab_main = findViewById(R.id.fab_main);
        fab_sub1 = findViewById(R.id.fab_sub1);
        fab_sub2 = findViewById(R.id.fab_sub2);

        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               toggleFab();
            }
        });

        fab_sub1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFab();
                replaceFragment(scheduleFragment);
                fab_main.hide();

            }
        });

        fab_sub2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFab();
                replaceFragment(toDoFragment);
                fab_main.hide();
            }
        });

        createDatabase();

        createScheduleTable();
        createTodoTable();
        createRepeatTable();
        createTimeTable();

        assignTodo();

        notification();

    }

    private void toggleFab() {
        if(isFabOpen) {
            fab_main.setImageResource(R.drawable.add);
            fab_sub1.startAnimation(fab_close);
            fab_sub2.startAnimation(fab_close);

            fab_sub1.setClickable(false);
            fab_sub2.setClickable(false);
            isFabOpen = false;
        } else {
            fab_main.setImageResource(R.drawable.close);
            fab_sub1.startAnimation(fab_open);
            fab_sub2.startAnimation(fab_open);

            fab_sub1.setClickable(true);
            fab_sub2.setClickable(true);
            isFabOpen = true;
        }
    }

    public void replaceFragment(Fragment fragment) { //프래그먼트 교체 함수
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    /*
    데이터베이스, 테이블 생성(schedule, Todo, repeat, Time)
     */
    private void createDatabase() { //데이터베이스 생성 : planner.db
        database = openOrCreateDatabase("planner.db", MODE_PRIVATE, null);
        Log.d("MainActivity", "데이터베이스 생성");
    }

    private void createScheduleTable() {
        String sql = "CREATE TABLE IF NOT EXISTS schedule ("
                + "_id integer PRIMARY KEY autoincrement, "
                + "name text, "
                + "location text, "
                + "start_date text, "
                + "start_time text, "
                + "end_date text, "
                + "end_time text, "
                + "repeat text, "
                + "memo text, "
                + "ori_id integer)";

        database.execSQL(sql);
        Log.d("MainActivity", "schedule 테이블 생성");
    }

    private void createTodoTable() {
        String sql = "CREATE TABLE IF NOT EXISTS todo ("
                + "_id integer PRIMARY KEY autoincrement, "
                + "name text, "
                + "date text, "
                + "time text, "
                + "req_time text, "
                + "memo text, "
                + "priority real)";

        database.execSQL(sql);
        Log.d("MainActivity", "todo 테이블 생성");
    }

    private void createTimeTable() {
        Log.d("MainActivity", "createTimeTable() 호출");
        database.execSQL("DROP TABLE IF EXISTS time");
        String sql = "CREATE TABLE time ("
                + "_id integer PRIMARY KEY autoincrement, "
                + "name text, "
                + "location text, "
                + "start_date text, "
                + "start_time text, "
                + "end_date text, "
                + "end_time text, "
                + "repeat text, "
                + "memo text, "
                + "type text, "
                + "item_id integer)";

        database.execSQL(sql);
        Log.d("MainActivity", "time 테이블 생성");
    }

    private void createRepeatTable() {
        String sql = "CREATE TABLE IF NOT EXISTS repeat ("
                + "_id integer PRIMARY KEY autoincrement, "
                + "repeat_type integer, "  // 매일/매주/매월
                + "start_date text, "
                + "end_date text, "
                + "renew integer default 1 check(renew=1 or renew=0))"; //1이면 계속 o, 0이면 더 이상 갱신x (이 이후로 모두 삭제 썼을 경우)

        database.execSQL(sql);
        Log.d("MainActivity", "repeat 테이블 생성");
    }

    /*
    insert 함수(schedule, Todo, Time)
     */
    public void insertScheduleRecord(String name, String location, String start_date, String start_time,
                             String end_date, String end_time, int repeat, String memo) {
        Log.d("MainActivity", "insertRecord 실행됨");

        String sql = "INSERT INTO schedule"
                + "(name, location, start_date, start_time, end_date, end_time, repeat, memo)"
                + " VALUES ( "
                + "'" + name + "' , '" + location + "', '" + start_date + "', '" + start_time
                + "', '" +  end_date + "', '" + end_time + "', " + repeat + " , '" + memo + "')";
        database.execSQL(sql);
    }

    public void insertTodoRecord(String name, String date, String time, String req_time, String memo, float priority) {
        String sql = "INSERT INTO todo"
                + "(name, date, time, req_time, memo, priority)"
                + " VALUES ( "
                + "'" + name + "', '" + date + "', '" + time + "', '" + req_time + "', '" + memo + "' , " + priority + ")";

        database.execSQL(sql);
        Log.d("MainActivity", "todo 데이터 추가");

    }

    public void insertTimeRecord(String name, String location, String start_date, String start_time,
                                 String end_date, String end_time, int repeat, String memo, String type, int item_id) {
        Log.d("MainActivity", "insertTimeRecord() 호출");

        String sql = "INSERT INTO time"
                + "(name, location, start_date, start_time, end_date, end_time, repeat, memo, type, item_id)"
                + " VALUES ( "
                + "'" + name + "' , '" + location + "', '" + start_date + "', '" + start_time
                + "', '" +  end_date + "', '" + end_time + "', " + repeat + ", '" + memo + "', '" + type + "', " + item_id + ")";

        database.execSQL(sql);
        Log.d("MainActivity", "time 데이터 추가");
    }

    /*
    테이블 조회 함수(schedule, Todo, Time)
     */
    public void executeScheduleQuery() {
        Cursor cursor = database.rawQuery("SELECT _id, name, location, start_date, start_time, " +
                "end_date, end_time, repeat, memo from schedule ORDER BY start_date, start_time" , null);

        for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToNext();
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String location = cursor.getString(2);
            String start_date = cursor.getString(3);
            String start_time = cursor.getString(4);
            String end_date = cursor.getString(5);
            String end_time = cursor.getString(6);
            String repeat = cursor.getString(7);
            String memo = cursor.getString(8);

            Log.d("MainActivity", "레코드#" + i + " : " + id + ", " + name + ", " + location + ", " +
                    start_date + ", " + start_time + ", " + end_date + ", " + end_time + ", " + repeat + ", " + memo);
        }
        cursor.close();
    }

    public void executeTodoQuery() {
        String sql = "SELECT * from todo ORDER BY date, time";
        Cursor cursor = database.rawQuery(sql,null);

        for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToNext();

            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String date = cursor.getString(2);
            String time = cursor.getString(3);
            String req_time = cursor.getString(4);
            String memo = cursor.getString(5);
            float priority = cursor.getFloat(6);

            Log.d("MainActivity", "레코드#" + i + " : " + id + ", " + name + ", " + date + ", " +
                    time + ", " + req_time + ", " + memo + ", " + priority);
        }
        cursor.close();
    }

    public void executeTimeQuery() {
        Log.d("MainActivity", "executeTimeQuery() 호출");
        Cursor cursor = database.rawQuery("SELECT * from time WHERE type = 'schedule' ORDER BY start_date, start_time" , null);

        Log.d("MainActivity", "--- schedule ---");
        for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToNext();
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String location = cursor.getString(2);
            String start_date = cursor.getString(3);
            String start_time = cursor.getString(4);
            String end_date = cursor.getString(5);
            String end_time = cursor.getString(6);
            String repeat = cursor.getString(7);
            String memo = cursor.getString(8);
            String type = cursor.getString(9);
            String item_id = cursor.getString(10);

            Log.d("MainActivity", "레코드#" + (i+1) + " : " + id + ", " + name + ", " + location + ", " + start_date + ", " +
                    start_time + ", " + end_date + ", " + end_time + ", " + repeat + ", " + memo + ", " + type + ", " + item_id);
        }

        cursor = database.rawQuery("SELECT * from time WHERE type = 'todo' ORDER BY start_date, start_time" , null);

        Log.d("MainActivity", "--- todo ---");
        for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToNext();
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String location = cursor.getString(2);
            String start_date = cursor.getString(3);
            String start_time = cursor.getString(4);
            String end_date = cursor.getString(5);
            String end_time = cursor.getString(6);
            String repeat = cursor.getString(7);
            String memo = cursor.getString(8);
            String type = cursor.getString(9);
            String item_id = cursor.getString(10);

            Log.d("MainActivity", "레코드#" + (i+1) + " : " + id + ", " + name + ", " + location + ", " + start_date + ", " +
                    start_time + ", " + end_date + ", " + end_time + ", " + repeat + ", " + memo + ", " + type + ", " + item_id);
        }
        cursor.close();
    }

    /*
    select 함수(schedule, Todo, Time)
    쿼리를 매개변수로 받아 select 하여 데이터를 ArrayList에 저장
     */
    public ArrayList<Schedule> selectSchedule(String sql) {
        ArrayList<Schedule> result = new ArrayList<Schedule>();

        try {
            Cursor cursor = database.rawQuery(sql,null);

            for (int i=0; i<cursor.getCount(); i++) {
                cursor.moveToNext();

                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String location = cursor.getString(2);
                String start_date = cursor.getString(3);
                String start_time = cursor.getString(4);
                String end_date = cursor.getString(5);
                String end_time = cursor.getString(6);
                int repeat = cursor.getInt(7);
                String memo = cursor.getString(8);

                Schedule schedule_item = new Schedule(id, name, location, start_date, start_time, end_date, end_time,
                                                        repeat, memo);
                result.add(schedule_item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<Todo> selectTodo(String sql) {
        ArrayList<Todo> result = new ArrayList<Todo>();

        try {
            Cursor cursor = database.rawQuery(sql, null);
            for (int i=0; i<cursor.getCount(); i++) {
                cursor.moveToNext();

                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String date = cursor.getString(2);
                String time = cursor.getString(3);
                String req_time = cursor.getString(4);
                String memo = cursor.getString(5);
                float priority = cursor.getFloat(6);

                Todo todo_item = new Todo(id, name, date, time, req_time, memo, priority);
                result.add(todo_item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<TimeItem> selectTime(String sql) {
        ArrayList<TimeItem> result = new ArrayList<TimeItem>();

        try {
            Cursor cursor = database.rawQuery(sql, null);
            for (int i=0; i<cursor.getCount(); i++) {
                cursor.moveToNext();

                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String location = cursor.getString(2);
                String start_date = cursor.getString(3);
                String start_time = cursor.getString(4);
                String end_date = cursor.getString(5);
                String end_time = cursor.getString(6);
                int repeat = cursor.getInt(7);
                String memo = cursor.getString(8);
                String type = cursor.getString(9);
                int item_id = cursor.getInt(10);

                TimeItem time_item = new TimeItem(id, name, location, start_date, start_time,
                        end_date, end_time, repeat, memo, type, item_id);
                result.add(time_item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /*
    update 함수(schedule, Todo)
     */
    public void updateSchedule(int position, String dname, String dlocation, String dstart_date, String dstart_time,
                               String dend_date, String dend_time, int drepeat, String dmemo) { //Month-ScheduleDialog에서 사용

        String sql = "SELECT * FROM time WHERE start_date = " + "'" + MonthFragment.click_date + "'" + " ORDER BY start_date, start_time";
        ArrayList<TimeItem> timeItems = selectTime(sql);
        TimeItem timeItem = timeItems.get(position);
        int item_id = timeItem.getItem_id();

        String update_sql = "UPDATE schedule SET name = " + "'" + dname + "', location = " + "'" + dlocation + "', start_date = " + "'" + dstart_date +
                "', start_time = " + "'" + dstart_time + "', end_date = " + "'" + dend_date +
                "', end_time = " + "'" + dend_time + "', repeat = " + drepeat + ", memo = " + "'" + dmemo +
                "' WHERE _id = " + item_id;

        database.execSQL(update_sql);

        assignTodo();
        timeItems = selectTime(sql);
        MonthFragment.adapter.setItems(timeItems);
        MonthFragment.recyclerView.setAdapter(MonthFragment.adapter);

        monthFragment.setCalendarDate(MonthFragment.mCal.get(Calendar.MONTH)+1);
        MonthFragment.gridAdapter.notifyDataSetChanged();
    }

    public void updateSchedule2(int position, String dname, String dlocation, String dstart_date, String dstart_time,
                               String dend_date, String dend_time, int drepeat, String dmemo) { //Week-ScheduleDialog2에서 사용

        ArrayList<TimeItem> timeItems = WeekFragment.week_items;
        TimeItem timeItem = timeItems.get(position);
        int item_id = timeItem.getItem_id();

        String update_sql = "UPDATE schedule SET name = " + "'" + dname + "', location = " + "'" + dlocation + "', start_date = " + "'" + dstart_date +
                "', start_time = " + "'" + dstart_time + "', end_date = " + "'" + dend_date +
                "', end_time = " + "'" + dend_time + "', repeat = " + drepeat + ", memo = " + "'" + dmemo +
                "' WHERE _id = " + item_id;

        database.execSQL(update_sql);

        assignTodo();
        weekFragment.setTimeList();
        weekFragment.findWeekSchedule();
        WeekFragment.adapter.setItems(timeItems);
        WeekFragment.adapter.notifyDataSetChanged();
    }

    public void updateTodo(int position, String dname, String dDate, String dtime,
                           String dreq_time, String dmemo) { //Today-TodoDialog에서 사용

        Calendar calendar = Calendar.getInstance();

        String todayDate = simpleDateFormat.format(calendar.getTime());
        String todayTime = simpleDateFormat2.format(calendar.getTime());

        String sql = "SELECT * FROM todo WHERE (date = '" + todayDate + "' AND time >= '" + todayTime + "') OR date > '" + todayDate + "' ORDER BY date, time";

        ArrayList<Todo> items = selectTodo(sql);
        Todo item = items.get(position);

        float dpriority = (float) toDoFragment.getRemainTime(dDate, dtime) / Float.parseFloat(dreq_time);

        String update_sql = "UPDATE todo SET name = " + "'" + dname + "', date = " + "'" + dDate +
                "', time = " + "'" + dtime + "', req_time = " + "'" + dreq_time + "', memo = " + "'" + dmemo +
                "', priority = " + dpriority + " WHERE _id = " + item.get_id();

        database.execSQL(update_sql);

        assignTodo();
        items = selectTodo(sql);
        TodoAdapter adapter = TodayFragment.adapter;
        adapter.setItems(items);
        TodayFragment.recyclerView.setAdapter(adapter);
    }

    public void updateTodo2(int position, String dname, String dDate, String dtime,
                            String dreq_time, String dmemo) { //Week-TodoDialog2에서 사용

        ArrayList<TimeItem> timeItems = WeekFragment.week_items;
        TimeItem timeItem = timeItems.get(position);
        int item_id = timeItem.getItem_id();

        float dpriority = (float) toDoFragment.getRemainTime(dDate, dtime) / Float.parseFloat(dreq_time);

        String update_sql = "UPDATE todo SET name = " + "'" + dname + "', date = " + "'" + dDate +
                "', time = " + "'" + dtime + "', req_time = " + "'" + dreq_time + "', memo = " + "'" + dmemo +
                "', priority = " + dpriority + " WHERE _id = " + item_id;

        database.execSQL(update_sql);

        assignTodo();
        weekFragment.setTimeList();
        weekFragment.findWeekSchedule();
        WeekFragment.adapter.setItems(timeItems);
        WeekFragment.adapter.notifyDataSetChanged();
    }

    public void updateTodo3(int position, String dname, String dDate, String dtime,
                            String dreq_time, String dmemo) { //Month-TodoDialog3에서 사용

        String sql = "SELECT * FROM time WHERE start_date = " + "'" + MonthFragment.click_date + "'" + " ORDER BY start_date, start_time";
        ArrayList<TimeItem> timeItems = selectTime(sql);
        TimeItem timeItem = timeItems.get(position);
        int item_id = timeItem.getItem_id();

        float dpriority = (float) toDoFragment.getRemainTime(dDate, dtime) / Float.parseFloat(dreq_time);

        String update_sql = "UPDATE todo SET name = " + "'" + dname + "', date = " + "'" + dDate +
                "', time = " + "'" + dtime + "', req_time = " + "'" + dreq_time + "', memo = " + "'" + dmemo +
                "', priority = " + dpriority + " WHERE _id = " + item_id;

        database.execSQL(update_sql);

        assignTodo();
        timeItems = selectTime(sql);
        MonthFragment.adapter.setItems(timeItems);
        MonthFragment.recyclerView.setAdapter(MonthFragment.adapter);

        monthFragment.setCalendarDate(MonthFragment.mCal.get(Calendar.MONTH)+1);
        MonthFragment.gridAdapter.notifyDataSetChanged();
    }

    /*
    delete 함수(schedule, Todo)
     */
    public void deleteSchedule(int position) { //ScheduleDialog

        String sql = "SELECT * FROM time WHERE start_date = " + "'" + MonthFragment.click_date + "'" + " ORDER BY start_date, start_time";
        ArrayList<TimeItem> timeItems = selectTime(sql);
        TimeItem timeItem = timeItems.get(position);
        int item_id = timeItem.getItem_id();

        String delete_sql = "DELETE FROM schedule WHERE _id = " + item_id;
        database.execSQL(delete_sql);

        assignTodo();
        timeItems = selectTime(sql);
        MonthFragment.adapter.setItems(timeItems);
        MonthFragment.recyclerView.setAdapter(MonthFragment.adapter);

        monthFragment.setCalendarDate(MonthFragment.mCal.get(Calendar.MONTH)+1);
        MonthFragment.gridAdapter.notifyDataSetChanged();
    }

    public void deleteSchedule2(int position) { //ScheduleDialog3

        ArrayList<TimeItem> timeItems = WeekFragment.week_items;
        TimeItem timeItem = timeItems.get(position);
        int item_id = timeItem.getItem_id();

        String delete_sql = "DELETE FROM schedule WHERE _id = " + item_id;
        database.execSQL(delete_sql);

        assignTodo();
        weekFragment.setTimeList();
        weekFragment.findWeekSchedule();
        WeekFragment.adapter.setItems(timeItems);
        WeekFragment.adapter.notifyDataSetChanged();
    }

    public void deleteTodo(int position) { //TodoDialog

        Calendar calendar = Calendar.getInstance();

        String todayDate = simpleDateFormat.format(calendar.getTime());
        String todayTime = simpleDateFormat2.format(calendar.getTime());

        String sql = "SELECT * FROM todo WHERE (date = '" + todayDate + "' AND time >= '" + todayTime + "') OR date > '" + todayDate + "' ORDER BY date, time";

        ArrayList<Todo> items = selectTodo(sql);
        Todo item = items.get(position);

        String delete_sql = "DELETE FROM todo WHERE _id = " + item.get_id();
        database.execSQL(delete_sql);

        assignTodo();
        items = selectTodo(sql);
        TodoAdapter adapter = TodayFragment.adapter;
        adapter.setItems(items);
        TodayFragment.recyclerView.setAdapter(adapter);
    }

    public void deleteTodo2(int position) { //TodoDialog2

        ArrayList<TimeItem> timeItems = WeekFragment.week_items;
        TimeItem timeItem = timeItems.get(position);
        int item_id = timeItem.getItem_id();

        String delete_sql = "DELETE FROM todo WHERE _id = " + item_id;
        database.execSQL(delete_sql);

        assignTodo();
        weekFragment.setTimeList();
        weekFragment.findWeekSchedule();
        WeekFragment.adapter.setItems(timeItems);
        WeekFragment.adapter.notifyDataSetChanged();
    }

    public void deleteTodo3(int position) { //TodoDialog3

        String sql = "SELECT * FROM time WHERE start_date = " + "'" + MonthFragment.click_date + "'" + " ORDER BY start_date, start_time";
        ArrayList<TimeItem> timeItems = selectTime(sql);
        TimeItem timeItem = timeItems.get(position);
        int item_id = timeItem.getItem_id();

        String delete_sql = "DELETE FROM todo WHERE _id = " + item_id;
        database.execSQL(delete_sql);

        assignTodo();
        timeItems = selectTime(sql);
        MonthFragment.adapter.setItems(timeItems);
        MonthFragment.recyclerView.setAdapter(MonthFragment.adapter);

        monthFragment.setCalendarDate(MonthFragment.mCal.get(Calendar.MONTH)+1);
        MonthFragment.gridAdapter.notifyDataSetChanged();
    }

    /*
    여유시간 배열 생성 및 할일 할당 함수
     */
    public void assignTodo() {
        Log.d("MainActivity", "assignTodo() 함수 호출");
        database.execSQL("DELETE FROM time");
        SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        LinkedList<ArrayList<String>> timeBlocks = new LinkedList<ArrayList<String>>();
        LinkedList<Integer> times = new LinkedList<Integer>();
        LinkedHashMap<Integer, LinkedList<ArrayList<String>>> spareTimes = new LinkedHashMap<Integer, LinkedList<ArrayList<String>>>();
        Date dToday = new Date(System.currentTimeMillis());
        String today = simpleDateFormat.format(dToday), now = simpleDateFormat2.format(dToday);

        // 일정을 불러오고 time테이블에 우선 할당
        Cursor cursor = database.rawQuery("SELECT * from schedule ORDER BY start_date, start_time", null);
        for(int i=0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            insertTimeRecord(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), cursor.getInt(7), cursor.getString(8), "schedule", cursor.getInt(0));
        }
        executeTimeQuery();

        // 할 일 불러오기
        LinkedList<Todo> todos = new LinkedList<Todo>();
        cursor = database.rawQuery("SELECT * from todo " +
                "WHERE (date = '"+ today+ "' AND time >= '" + now + "') " +
                "OR date > '" + today + "' " +
                "ORDER BY priority, date, time",null);
        Log.d("MainActivity", "todo | No | name | date | time | require_time | memo | priority");
        for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToNext();
            Todo todo_item = new Todo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5), cursor.getFloat(6));
            todos.add(todo_item); // 할 일들을 우선순위 값 순서대로 배열에 저장

            Log.d("MainActivity", "레코드#" + (i+1) + " : " + todo_item._id + ", " + todo_item.name + ", " + todo_item.date + ", " +
                    todo_item.time + ", " + todo_item.req_time + ", " + todo_item.memo + ", " + todo_item.priority);
        }
        cursor.close();

        // 할 일들을 여유시간에 할당
        while(!todos.isEmpty()){
            Todo it = todos.poll();
            // time에 먼저 할당된 일정들을 할 일의 마감 전까지의 일정만 불러와서 timeblocks 배열에 일정 사이의 여유시간을 저장
            cursor = database.rawQuery("SELECT * from time " +
                    "WHERE (end_date < '" + it.date + "' OR (end_date = '" + it.date + "' AND end_time < '" + it.time + "')) " +
                    "AND ((start_date = '"+ today+ "' AND end_time >= '" + now + "') OR start_date > '" + today + "') " +
                    "ORDER BY start_date, start_time",null);
            cursor.moveToNext();

            // 첫 번째 일정과 현재 시간 사이의 여유시간 계산
            if( (today.compareTo(cursor.getString(3)) == 0 && Integer.parseInt(now.substring(0, 2)) < Integer.parseInt(cursor.getString(4).substring(0, 2))) || today.compareTo(cursor.getString(3)) < 0){
                int start = Integer.parseInt(now.substring(0,2));
                if(Integer.parseInt(now.substring(3, 5)) > 0)
                    start++;
                ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(today, String.format("%02d", start) + ":00", cursor.getString(3), cursor.getString(4).substring(0, 2) + ":00"));
                timeBlocks.add(spareTime);
            }
            // 일정들 사이의 여유시간 계산
            for (int i=1; i<cursor.getCount(); i++) {
                int start = Integer.parseInt(cursor.getString(6).substring(0,2));
                if(Integer.parseInt(cursor.getString(6).substring(3, 5)) > 0)
                    start++;
                ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(cursor.getString(5), String.format("%02d", start) + ":00")); // 이전 스케줄의 날짜, 끝시간을 여유시간 블럭에 저장.
                cursor.moveToNext();
                spareTime.addAll(Arrays.asList(cursor.getString(3), cursor.getString(4).substring(0, 2) + ":00"));
                timeBlocks.add(spareTime);
            }
            // 마지막 일정과 할 일의 마감시간 사이의 여유시간 계산
            if( (it.date.compareTo(cursor.getString(3)) == 0 && it.time.compareTo(cursor.getString(4)) > 0) || it.date.compareTo(cursor.getString(3)) > 0 ){
                int start = Integer.parseInt(cursor.getString(6).substring(0,2));
                if(Integer.parseInt(cursor.getString(6).substring(3, 5)) > 0)
                    start++;
                ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(cursor.getString(5), String.format("%02d", start) + ":00", it.date, it.time.substring(0, 2) + ":00"));
                timeBlocks.add(spareTime);
            }
            cursor.close();

            // 여유시간들을 08:00~23:00 사이의 값으로 분할 및 여유시간이 0인 시간블럭 삭제
            int maxtime=0, mintime=24;
            for(int i=0; i<timeBlocks.size(); i++){
                if(timeBlocks.get(i).get(0).compareTo(timeBlocks.get(i).get(2)) < 0){
                    Date nextDay = null;
                    try{
                        nextDay = simpleDateFormat.parse(timeBlocks.get(i).get(0));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(nextDay);
                    cal.add(Calendar.DATE, 1);

                    ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(simpleDateFormat.format(cal.getTime()), "08:00", timeBlocks.get(i).get(2), timeBlocks.get(i).get(3)));
                    timeBlocks.get(i).remove(2);
                    timeBlocks.get(i).remove(2);
                    timeBlocks.get(i).addAll(Arrays.asList(timeBlocks.get(i).get(0), "23:00"));
                    timeBlocks.add(i+1, spareTime);
                } else {
                    if(timeBlocks.get(i).get(1).compareTo("08:00")<0){
                        if(timeBlocks.get(i).get(3).compareTo("08:00")<0) {
                            timeBlocks.remove(i);
                            i--;
                            continue;
                        } else {
                            timeBlocks.get(i).remove(1);
                            timeBlocks.get(i).add(1, "08:00");
                        }
                    } else if(timeBlocks.get(i).get(3).compareTo("23:00")>0) {
                        if(timeBlocks.get(i).get(1).compareTo("23:00")>0) {
                            timeBlocks.remove(i);
                            i--;
                            continue;
                        } else {
                            timeBlocks.get(i).remove(3);
                            timeBlocks.get(i).add(3, "23:00");
                        }
                    }
                }

                int start = Integer.parseInt(timeBlocks.get(i).get(1).substring(0, 2)), end = Integer.parseInt(timeBlocks.get(i).get(3).substring(0, 2));
                if(Integer.parseInt(timeBlocks.get(i).get(1).substring(3, 5)) > 0)
                    start++;
                if(end-start <= 0){
                    timeBlocks.remove(i);
                    i--;
                    continue;
                }
                times.add(end - start);

                // 여유시간의 최대 크기를 변수에 저장
                if(maxtime < end - start)
                    maxtime = end - start;
                // 여유시간의 최적 크기를 변수에 저장
                // * 최적값 : 할일이 할당될 수 있을 만큼 큰 여유시간들 중에서 가장 작은 값, 요구시간보다는 큰 여유시간 값들 중 최소값
                if(Integer.parseInt(it.req_time) <= end - start) {
                    if (mintime > end - start)
                        mintime = end - start;
                }

                // 각 여유시간의 크기 계산후 times배열에 저장 및 저장된 여유시간을 로그로 출력
                Log.d("MainActivity", "TimeBlock #" + (i+1) + ": " +times.get(i) + "시간 " +
                        "[" + timeBlocks.get(i).get(0) + " " + timeBlocks.get(i).get(1) + " ~ " + timeBlocks.get(i).get(2) + " " + timeBlocks.get(i).get(3) + "]");
            }

            if(Integer.parseInt(it.req_time) > maxtime) {
                int req_time = Integer.parseInt(it.req_time) - maxtime;
                Todo todo_item = new Todo(it._id, it.name, it.date, it.time, Integer.toString(req_time), it.memo, it.priority);
                todos.addFirst(todo_item);
                Log.d("MainActivity", it.name + "(" + it.req_time + "시간)이 너무 커서 둘로 쪼개어 " + maxtime +"시간 만 할당하고, " + req_time + "은 다시 todo배열에 추가");
                it.setReq_time(Integer.toString(maxtime));
                mintime = maxtime;
            }
            for(int i=0; i<timeBlocks.size(); i++){
                if(times.get(i) == mintime) {
                    int end = Integer.parseInt(timeBlocks.get(i).get(1).substring(0, 2)) + Integer.parseInt(it.req_time);
                    Log.d("MainActivity", it.name + "을 TimeBlock #" + (i+1) + "에 할당 >> [" + timeBlocks.get(i).get(0) + " " + timeBlocks.get(i).get(1) + " ~ " + timeBlocks.get(i).get(2) + " " + String.format("%02d", end) + ":00" + "]");
                    insertTimeRecord(it.name, "", timeBlocks.get(i).get(0), timeBlocks.get(i).get(1), timeBlocks.get(i).get(2), String.format("%02d", end) + ":00", 0, "", "todo", it._id);
                    break;
                }
            }

            Log.d("MainActivity", "--- TimeBlock의 개수: " + timeBlocks.size() + ", times의 개수: " + times.size() + " ---");
            timeBlocks.clear();
            times.clear();
        }
        executeTimeQuery();
        notification();
    }

    /*
    할 일 알림 함수 - 할 일의 시작 시간이 되면 상단바 알림
     */
    public void notification() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/ddHH:mm");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat format3 = new SimpleDateFormat("HH:mm:ss");

        Date now = new Date();
        Date dStart = null;
        Calendar calendar = Calendar.getInstance();
        String todayDate = format2.format(now);
        String todayTime = format3.format(now);

        String sql = "SELECT * FROM time WHERE type = 'todo' AND (start_date = '" + todayDate + "' AND start_time >= '" + todayTime + "') " +
        "OR (type = 'todo' AND start_date > '" + todayDate + "') ORDER BY start_date, start_time";
        alarm_items = selectTime(sql);

        for (int i=0; i<alarm_items.size(); i++) {
            TimeItem item = alarm_items.get(i);
            String start = item.getStart_date().concat(item.getStart_time()).concat(":00");
            Log.d("MainActivity", item.getName() + ", " + item.getStart_time());

            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(MainActivity.this, alarmReceiver.class);
            intent.putExtra("id", i);

            PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, i, intent, 0);

            try {
                dStart = format.parse(start);
                calendar.setTime(dStart);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            } else {
                manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            }
        }
    }
}