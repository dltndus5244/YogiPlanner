package org.techtown.yogiplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.TintableBackgroundView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;

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
//                fab_sub1.hide();
//                fab_sub2.hide();
            }
        });

        fab_sub2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFab();
                replaceFragment(toDoFragment);
                fab_main.hide();
//                fab_sub1.hide();
//                fab_sub2.hide();
            }
        });

        createDatabase();

        createScheduleTable();
        createTodoTable();
        createTimeTable();
    }

    private void toggleFab() {
        Log.d("MainActivity", "toggleFab() 호출");
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
        Log.d("MainActivity", "replaceFragment() 호출");
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    private void createDatabase() { //데이터베이스 생성 : planner.db
        Log.d("MainActivity", "createDatabase() 호출");
        database = openOrCreateDatabase("planner.db", MODE_PRIVATE, null);
        Log.d("MainActivity", "데이터베이스 생성");
    }

    private void createScheduleTable() { // Schedule 테이블 생성
        Log.d("MainActivity", "createScheduleTable() 호출");
        String sql = "CREATE TABLE IF NOT EXISTS schedule ("
                + "_id integer PRIMARY KEY autoincrement, "
                + "name text, "
                + "location text, "
                + "start_date text, "
                + "start_time text, "
                + "end_date text, "
                + "end_time text, "
                + "repeat text, "
                + "memo text)";

        database.execSQL(sql);
        Log.d("MainActivity", "schedule 테이블 생성");
    }

    private void createTodoTable() { //Todo 테이블 생성
        Log.d("MainActivity", "createTodoTable() 호출");
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

    private void createTimeTable() { // Time 테이블 생성
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

    public void insertScheduleRecord(String name, String location, String start_date, String start_time,
                             String end_date, String end_time, int repeat, String memo) { //스케줄 추가 함수 - AddScheduleFragment 에서 사용
        Log.d("MainActivity", "insertScheduleRecord() 호출");

        String sql = "INSERT INTO schedule"
                + "(name, location, start_date, start_time, end_date, end_time, repeat, memo)"
                + " VALUES ( "
                + "'" + name + "' , '" + location + "', '" + start_date + "', '" + start_time
                + "', '" +  end_date + "', '" + end_time + "', " + repeat + " , '" + memo + "')";

        database.execSQL(sql);
        Log.d("MainActivity", "schedule 데이터 추가");
    }

    public void insertTodoRecord(String name, String date, String time, String req_time, String memo, float priority) { //할 일 추가 함수 - AddTodoFragment 에서 사용
        Log.d("MainActivity", "insertTodoRecord() 호출");

        String sql = "INSERT INTO todo"
                + "(name, date, time, req_time, memo, priority)"
                + " VALUES ( "
                + "'" + name + "', '" + date + "', '" + time + "', '" + req_time + "', '" + memo + "' , " + priority + ")";

        database.execSQL(sql);
        Log.d("MainActivity", "todo 데이터 추가");
    }

    public void insertTimeRecord(String name, String location, String start_date, String start_time,
                                     String end_date, String end_time, int repeat, String memo, String type, int item_id) { //스케줄 추가 함수 - ?? 에서 사용
        Log.d("MainActivity", "insertTimeRecord() 호출");

        String sql = "INSERT INTO time"
                + "(name, location, start_date, start_time, end_date, end_time, repeat, memo, type, item_id)"
                + " VALUES ( "
                + "'" + name + "' , '" + location + "', '" + start_date + "', '" + start_time
                + "', '" +  end_date + "', '" + end_time + "', " + repeat + ", '" + memo + "', '" + type + "', " + item_id + ")";

        database.execSQL(sql);
        Log.d("MainActivity", "time 데이터 추가");
    }

    public void executeScheduleQuery() { //schedule 테이블 조회 함수(확인용) - AddScheduleFragment
        Log.d("MainActivity", "executeScheduleQuery() 호출");
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

            Log.d("MainActivity", "레코드#" + (i+1) + " : " + id + ", " + name + ", " + location + ", " +
                    start_date + ", " + start_time + ", " + end_date + ", " + end_time + ", " + repeat + ", " + memo);
        }
        cursor.close();
    }

    public void executeTodoQuery() { //todo 테이블 조회 함수(확인용) - AddToDoFragment
        Log.d("MainActivity", "executeTodoQuery() 호출");
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

            Log.d("MainActivity", "레코드#" + (i+1) + " : " + id + ", " + name + ", " + date + ", " +
                    time + ", " + req_time + ", " + memo + ", " + priority);
        }
        cursor.close();
    }

    public void executeTimeQuery() { //time 테이블 조회 함수(확인용) - assignTodo()
        Log.d("MainActivity", "executeTimeQuery() 호출");
        Cursor cursor = database.rawQuery("SELECT * from time ORDER BY start_date, start_time" , null);

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

    /* 쿼리 수행 결과에 따라 Schedule 테이블의 데이터를 배열에 넣어줌
        매개변수 : String sql
        사용 : MonthFragment, ScheduleDialog
     */
    public ArrayList<Schedule> selectSchedule(String sql) {
        Log.d("MainActivity", "selectSchedule() 호출");
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

        return result; //쿼리 수행 결과 저장 배열
    }

    /* 쿼리 수행 결과에 따라 Todo 테이블의 데이터를 배열에 넣어줌
    매개변수 : String sql
    사용 : TodayFragment
 */
    public ArrayList<Todo> selectTodo(String sql) {
        Log.d("MainActivity", "selectTodo() 호출");
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

    /* Schedule 데이터 수정 함수
        매개변수로 수정할 데이터의 position을 받아서 해당 위치에 맞는 데이터를 수정해줌
        사용 : ScheduleDialog
     */
    public void updateSchedule(int position, String dname, String dlocation, String dstart_date, String dstart_time,
                               String dend_date, String dend_time, int drepeat, String dmemo) {
        Log.d("MainActivity", "updateSchedule() 호출");

        String sql = "SELECT * FROM schedule WHERE start_date = " + "'" + MonthFragment.click_date + "'" + " ORDER BY start_date, start_time";

        ArrayList<Schedule> items = selectSchedule(sql);
        Schedule item = items.get(position);

        String update_sql = "UPDATE schedule SET name = " + "'" + dname + "', location = " + "'" + dlocation + "', start_date = " + "'" + dstart_date +
                "', start_time = " + "'" + dstart_time + "', end_date = " + "'" + dend_date +
                "', end_time = " + "'" + dend_time + "', repeat = " + drepeat + ", memo = " + "'" + dmemo +
                "' WHERE _id = " + item.get_id();

        Log.d("MainActivity", "아이디 : " + item.get_id());
        database.execSQL(update_sql);

        items = selectSchedule(sql);
        ScheduleAdapter adapter = MonthFragment.adapter;
        adapter.setItems(items);
        MonthFragment.recyclerView.setAdapter(adapter);
    }

    public void updateSchedule2(int position, String dname, String dlocation, String dstart_date, String dstart_time,
                               String dend_date, String dend_time, int drepeat, String dmemo) {
        Log.d("MainActivity", "updateSchedule2() 호출");

        ArrayList<Schedule> items = WeekFragment.week_items;
        Schedule item = items.get(position);

        String update_sql = "UPDATE schedule SET name = " + "'" + dname + "', location = " + "'" + dlocation + "', start_date = " + "'" + dstart_date +
                "', start_time = " + "'" + dstart_time + "', end_date = " + "'" + dend_date +
                "', end_time = " + "'" + dend_time + "', repeat = " + drepeat + ", memo = " + "'" + dmemo +
                "' WHERE _id = " + item.get_id();

        database.execSQL(update_sql);

        weekFragment.findWeekSchedule();
        WeekFragment.adapter.setItems(items);
        WeekFragment.adapter.notifyDataSetChanged();
    }

    public void deleteSchedule(int position) { // schedule 데이터 삭제
        Log.d("MainActivity", "deleteSchedule() 호출");
        String sql = "SELECT * FROM schedule WHERE start_date = " + "'" + MonthFragment.click_date + "'" + " ORDER BY start_date, start_time";
        ArrayList<Schedule> items = selectSchedule(sql);
        Schedule item = items.get(position);

        Log.d("MainActivity", "아이디 : " + item.get_id());

//        String delete_sql = "DELETE FROM schedule WHERE name = " + "'" + item.getName() + "'" + "AND start_date = " + "'" + item.getStart_date() + "'";
        String delete_sql = "DELETE FROM schedule WHERE _id = " + item.get_id();
        database.execSQL(delete_sql);

        items = selectSchedule(sql);
        ScheduleAdapter adapter = MonthFragment.adapter;
        adapter.setItems(items);
        MonthFragment.recyclerView.setAdapter(adapter);

    }

    public void deleteSchedule2(int position) { // schedule 데이터 삭제
        Log.d("MainActivity", "deleteSchedule2() 호출");

        ArrayList<Schedule> items = WeekFragment.week_items;
        Schedule item = items.get(position);

        String delete_sql = "DELETE FROM schedule WHERE _id = " + item.get_id();
        database.execSQL(delete_sql);

        WeekFragment.timeList.set(WeekFragment.passedPosition, "");
        weekFragment.findWeekSchedule();
        WeekFragment.adapter.setItems(items);
        WeekFragment.adapter.notifyDataSetChanged();
    }

    public void updateTodo(int position, String dname, String dDate, String dtime,
                           String dreq_time, String dmemo) {
        Log.d("MainActivity", "updateTodo() 호출");

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

        items = selectTodo(sql);
        TodoAdapter adapter = TodayFragment.adapter;
        adapter.setItems(items);
        TodayFragment.recyclerView.setAdapter(adapter);
    }

    public void deleteTodo(int position) {
        Log.d("MainActivity", "deleteTodo() 호출");

        Calendar calendar = Calendar.getInstance();

        String todayDate = simpleDateFormat.format(calendar.getTime());
        String todayTime = simpleDateFormat2.format(calendar.getTime());

        String sql = "SELECT * FROM todo WHERE (date = '" + todayDate + "' AND time >= '" + todayTime + "') OR date > '" + todayDate + "' ORDER BY date, time";

        ArrayList<Todo> items = selectTodo(sql);
        Todo item = items.get(position);

        String delete_sql = "DELETE FROM todo WHERE _id = " + item.get_id();
        database.execSQL(delete_sql);

        items = selectTodo(sql);
        TodoAdapter adapter = TodayFragment.adapter;
        adapter.setItems(items);
        TodayFragment.recyclerView.setAdapter(adapter);
    }

    public void assignTodo() { //여유시간 배열 생성 및 할일 할당
        Log.d("MainActivity", "assignTodo() 함수 호출");
        database.execSQL("DELETE FROM time");
        SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        LinkedList<ArrayList<String>> timeBlocks = new LinkedList<ArrayList<String>>();
        LinkedList<Integer> times = new LinkedList<Integer>();
        LinkedHashMap<Integer, LinkedList<ArrayList<String>>> spareTimes = new LinkedHashMap<Integer, LinkedList<ArrayList<String>>>();
        Date dToday = new Date(System.currentTimeMillis());
        String today = simpleDateFormat.format(dToday), now = simpleDateFormat2.format(dToday);

        // 일정을 불러오고 time테이블에 우선 할당
        Cursor cursor = database.rawQuery("SELECT * from schedule " +
                                                "WHERE (start_date = '"+ today+ "' AND start_time >= '" + now + "') " +
                                                "OR start_date > '" + today + "' " +
                                                "ORDER BY start_date, start_time", null);
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
        executeTodoQuery();

        // 할 일들을 여유시간에 할당
        while(!todos.isEmpty()){
            Todo it = todos.poll();
            // time에 먼저 할당된 일정들을 할 일의 마감 전까지의 일정만 불러와서 timeblocks 배열에 일정 사이의 여유시간을 저장
            cursor = database.rawQuery("SELECT * from time WHERE end_date < '" + it.date + "' OR (end_date = '" + it.date + "' AND end_time < '" + it.time + "') ORDER BY start_date, start_time",null);
            cursor.moveToNext();
            // 첫 번째 일정과 현재 시간 사이의 여유시간 계산
            if( (today.compareTo(cursor.getString(3)) == 0 && Integer.parseInt(now.substring(0, 2)) < Integer.parseInt(cursor.getString(4).substring(0, 2))) || today.compareTo(cursor.getString(3)) < 0){
                int start = Integer.parseInt(now.substring(0,2));
                if(Integer.parseInt(now.substring(3, 5)) > 0)
                    start++;
                ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(today, String.format("%02d", start) + ":00", cursor.getString(3), cursor.getString(4).substring(0, 2) + ":00"));
                timeBlocks.add(spareTime);
            }
            int maxtime=0;
            for (int i=1; i<cursor.getCount(); i++) {
                int start = Integer.parseInt(cursor.getString(6).substring(0,2));
                if(Integer.parseInt(cursor.getString(6).substring(3, 5)) > 0)
                    start++;
                ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(cursor.getString(5), String.format("%02d", start) + ":00")); // 이전 스케줄의 날짜, 끝시간을 여유시간 블럭에 저장.
                cursor.moveToNext();
                spareTime.addAll(Arrays.asList(cursor.getString(3), cursor.getString(4).substring(0, 2) + ":00"));
                timeBlocks.add(spareTime);
/*
                int time;
                if(spareTime.get(0) != cursor.getString(3)) { // 다음 스케줄의 날짜가 이전 스케줄의 날짜와 다르면 23시 까지만 여유시간으로 계산
                    spareTime.add("23:00");
                    time = (23 - Integer.parseInt(spareTime.get(1).substring(0, 2))); // 여유시간 블럭의 시간을 계산하여 헤시맵의 키 값으로 사용
                    if(maxtime < time)
                        maxtime = time;
                    if(Integer.parseInt(cursor.getString(4).substring(0, 2)) > 8){ // 다음 일정의 시작시간이 8시 이후면 8시부터 다음 일정의 시작 전까지 여유시간으로 계산하도록 분기
                        if(!spareTimes.containsKey(time))
                            spareTimes.put(time, timeBlock); // 키값이 없으면 키값 생성
                        spareTimes.get(time).add(spareTime); // 키값에 여유시간 블럭 삽입
                        spareTime.clear();
                        spareTime = new ArrayList<String>(Arrays.asList(cursor.getString(5), "08:00", cursor.getString(4))); // 8시부터 다음 일정 시작 전까지 여유시간블럭 생성
                        time = (Integer.parseInt(spareTime.get(2).substring(0, 2)) - Integer.parseInt(spareTime.get(1).substring(0, 2))); // 키 값 계산
                        if(maxtime < time)
                            maxtime = time;
                    }
                } else{
                    spareTime.add(cursor.getString(4));
                    time = (Integer.parseInt(spareTime.get(2).substring(0, 2)) - Integer.parseInt(spareTime.get(1).substring(0, 2))); // 다음 스케줄이 이전 스케줄과 같은 날짜 일 때 여유시간
                    if(maxtime < time)
                        maxtime = time;
                }
                if(!spareTimes.containsKey(time))
                    spareTimes.put(time, timeBlock);
                spareTimes.get(time).add(spareTime);
*/
            }
            // 마지막 일정과 할 일의 마감시간 사이의 여유시간 계산
            if( (it.date.compareTo(cursor.getString(3)) == 0 && Integer.parseInt(it.time.substring(0, 2)) > Integer.parseInt(cursor.getString(4).substring(0, 2))) || it.date.compareTo(cursor.getString(3)) > 0){
                int start = Integer.parseInt(cursor.getString(6).substring(0,2));
                if(Integer.parseInt(cursor.getString(6).substring(3, 5)) > 0)
                    start++;
                ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(cursor.getString(5), String.format("%02d", start) + ":00", it.date, it.time.substring(0, 2) + ":00"));
                timeBlocks.add(spareTime);
            }
            cursor.close();

            // 여유시간들을 08:00~23:00 사이의 값으로 분할 및 여유시간이 0인 시간블럭 삭제
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
                    if(timeBlocks.get(i).get(1).compareTo(timeBlocks.get(i).get(3)) >= 0){
                        timeBlocks.remove(i);
                        i--;
                        continue;
                    }
                }
            }

            // 각 여유시간의 크기 계산후 times배열에 저장 및 저장된 여유시간을 로그로 출력
            for(int i=0; i<timeBlocks.size(); i++){
                int start = Integer.parseInt(timeBlocks.get(i).get(1).substring(0, 2)), end = Integer.parseInt(timeBlocks.get(i).get(3).substring(0, 2));
                if(Integer.parseInt(timeBlocks.get(i).get(1).substring(3, 5)) > 0)
                    start++;
                times.add(end - start);
                Log.d("MainActivity", "TimeBlock #" + (i+1) + ": " +times.get(i) + "시간 " +
                        "[" + timeBlocks.get(i).get(0) + " " + timeBlocks.get(i).get(1) + " ~ " + timeBlocks.get(i).get(2) + " " + timeBlocks.get(i).get(3) + "]");
            }
            timeBlocks.clear();
        }
        /*
        스케줄 테이블의 값 불러오기, 시간 순서대로 불러오며 [이전 스케줄의 끝시간 ~ 다음 스케줄의 시작시간] 을 여유시간으로 계산.
        schedules 배열 리스트에 기존의 모든 스케줄 저장. 나중에 할당된 할일들과 합칠 예정.
        여유시간 블럭은 ArrayList에 {"날짜", "시작시간", "종료시간"}으로 저장되며
        각각의 블럭을 LikedList에서 시간 순서대로 저장.
        LinkedHashMap에서 블럭이 가진 여유시간의 양을 Key값으로 각각의 블럭 LinkedList를 저장.
        **
        여유시간 = 일정이 할당되지 않은, 사용자가 해야 할 일이 없는 무료한 시간의 개념
        여유시간 블럭 = 여유시간을 날짜, 시작시간, 끝시간의 값으로 나타낸 데이터
        키 값 = 여유시간 블럭이 가진 여유시간의 크기
        **
         */
//        cursor.moveToNext();
//        for (int i=1; i<cursor.getCount(); i++) {
//            int id = cursor.getInt(0);
//            String name = cursor.getString(1);
//            String location = cursor.getString(2);
//            String start_date = cursor.getString(3);
//            String start_time = cursor.getString(4);
//            String end_date = cursor.getString(5);
//            String end_time = cursor.getString(6);
//            int repeat = cursor.getInt(7);
//            String memo = cursor.getString(8);
//
//            Schedule schedule_item = new Schedule(id, name, location, start_date, start_time, end_date, end_time, repeat, memo);
//            schedules.add(schedule_item); // 스케줄을 배열에 저장
//
//            ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(start_date, end_time)); // 이전 스케줄의 날짜, 끝시간을 여유시간 블럭에 저장.
//            cursor.moveToNext();
//            int time;
//            if(spareTime.get(0) != cursor.getString(3)) { // 다음 스케줄의 날짜가 이전 스케줄의 날짜와 다르면 23시 까지만 여유시간으로 계산
//                spareTime.add("23:00");
//                time = (23 - Integer.parseInt(spareTime.get(1).substring(0, 2))); // 여유시간 블럭의 시간을 계산하여 헤시맵의 키 값으로 사용
//                if(Integer.parseInt(cursor.getString(4).substring(0, 2)) > 8){ // 다음 일정의 시작시간이 8시 이후면 8시부터 다음 일정의 시작 전까지 여유시간으로 계산하도록 분기
//                    if(!spareTimes.containsKey(time))
//                        spareTimes.put(time, null); // 키값이 없으면 키값 생성
//                    spareTimes.get(time).add(spareTime); // 키값에 여유시간 블럭 삽입
//                    spareTime.clear();
//                    spareTime = new ArrayList<String>(Arrays.asList(cursor.getString(3), "08:00", cursor.getString(4))); // 8시부터 다음 일정 시작 전까지 여유시간블럭 생성
//                    time = (Integer.parseInt(spareTime.get(2).substring(0, 2)) - Integer.parseInt(spareTime.get(1).substring(0, 2))); // 키 값 계산
//                }
//            } else{
//                spareTime.add(cursor.getString(4));
//                time = (Integer.parseInt(spareTime.get(2).substring(0, 2)) - Integer.parseInt(spareTime.get(1).substring(0, 2))); // 다음 스케줄이 이전 스케줄과 같은 날짜 일 때 여유시간
//            }
//            if(!spareTimes.containsKey(time))
//                spareTimes.put(time, null);
//            spareTimes.get(time).add(spareTime);
//        }
//        cursor.close();
//
//        // 여유시간이 가장 긴 블럭의 시간 저장.
//        int maxSpace=0;
//        for(int key:spareTimes.keySet()){
//            if(maxSpace<key)
//                maxSpace = key;
//        }
//
//
//        /*
//        할 일들을 여유시간에 할당
//         */
//        while(!todos.isEmpty()){
//            Todo it = todos.poll();
//            if(Integer.parseInt(it.req_time) > maxSpace){
//                todos.addFirst(new Todo(it._id, it.name, it.date, it.time, Integer.toString(Integer.parseInt(it.req_time)-maxSpace), it.memo, it.priority));
//                it.req_time = Integer.toString(maxSpace);
//            }
//            for(int i=Integer.parseInt(it.req_time); i<=maxSpace; i++){
//                if(spareTimes.containsKey(i)){
//                    Date spare = null;
//                    Date due = null;
//                    try{
//                        spare = DateFormat.parse(spareTimes.get(i).getFirst().get(0)+" "+spareTimes.get(i).getFirst().get(1));
//                        due = DateFormat.parse(it.date+" "+it.time);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    Calendar cal = Calendar.getInstance();
//                    cal.setTime(spare);
//
//                    cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(it.req_time));
//
//                    int compare = spare.compareTo(due);
//                    if(compare > 0)
//                        continue;
//                }
//            }
//        }
//        for(Todo it:todos){
//            boolean isAssigned = false;
//            int requireTime = Integer.parseInt(it.req_time);
//            for(int i=requireTime; i<=maxSpace; i++){
//                if(spareTimes.containsKey(i) && !isAssigned){
//                    for(int j=0; j<spareTimes.get(i).size(); j++){
//                        String tdate = spareTimes.get(i).get(j).get(0);
//                        String ts_time = spareTimes.get(i).get(j).get(1);
//                        String te_time = spareTimes.get(i).get(j).get(2);
//                        String end_time = Integer.toString(Integer.parseInt(ts_time)+requireTime);
//                        if(Integer.parseInt(tdate) > Integer.parseInt(it.date))
//                            break;
//                        else if(Integer.parseInt(tdate) == Integer.parseInt(it.date)){
//                            if(Integer.parseInt(end_time) > Integer.parseInt(it.time))
//                                break;
//                        }
//                        Schedule schedule_item = new Schedule(it._id, it.name, "", tdate, ts_time, tdate, end_time, 0, it.memo);
//                        int left = (Integer.parseInt(te_time) - Integer.parseInt(end_time))/1;
//                        spareTimes.get(i).remove(j);
//                        if(spareTimes.get(i).size() == 0)
//                            spareTimes.remove(i);
//                        if(left > 0){
//                            ArrayList<String> spareTime = new ArrayList<String>(Arrays.asList(tdate, end_time, te_time));
//                            if(!spareTimes.containsKey(left))
//                                spareTimes.put(left, null);
//                            spareTimes.get(left).add(spareTime);
//                            Collections.sort(spareTimes.get(left), new Comparator<ArrayList<String>>() {
//                                @Override
//                                public int compare(ArrayList<String> o1, ArrayList<String> o2) {
//                                    if(Integer.parseInt(o1.get(0)) > Integer.parseInt(o2.get(0)))
//                                        return 1;
//                                    else if(Integer.parseInt(o1.get(0)) > Integer.parseInt(o2.get(0))){
//                                        if(Integer.parseInt(o1.get(1)) > Integer.parseInt(o2.get(1)))
//                                            return 1;
//                                        else
//                                            return -1;
//                                    }
//                                    else
//                                        return -1;
//
//                                }
//                            });
//                        }
//                        isAssigned = true;
//                        break;
//                    }
//                }
//            }
//        }
    }
}