package org.techtown.yogiplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.Month;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    MonthFragment monthFragment;
    WeekFragment weekFragment;
    TodayFragment todayFragment;
    AddScheduleFragment scheduleFragment;
    AddToDoFragment toDoFragment;

    public static SQLiteDatabase database;

    public static Context mContext;

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
                        return true;
                    case R.id.tab2:
                        replaceFragment(weekFragment);
                        return true;
                    case R.id.tab3:
                        replaceFragment(todayFragment);
                        return true;
                    case R.id.tab4:
                        replaceFragment(scheduleFragment);
                        return true;
                    case R.id.tab5:
                        replaceFragment(toDoFragment);
                }
                return false;
            }
        });

        createDatabase();
        createTable();

    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    private void createDatabase() { //데이터베이스 생성 : planner.db
        database = openOrCreateDatabase("planner.db", MODE_PRIVATE, null);
        Log.d("MainActivity", "데이터베이스 생성");
    }

    private void createTable() { // 테이블 생성 1. schedule 2. todo
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

        String sql2 = "CREATE TABLE IF NOT EXISTS todo ("
                + "_id integer PRIMARY KEY autoincrement, "
                + "name text, "
                + "date text, "
                + "time text, "
                + "req_time text, "
                + "memo text)";

        database.execSQL(sql2);
        Log.d("MainActivity", "todo 테이블 생성");
    }

    public void insertRecord(String name, String location, String start_date, String start_time,
                             String end_date, String end_time, int repeat, String memo) { //스케줄 추가 함수
        Log.d("MainActivity", "insertRecord 실행됨");

        String sql = "INSERT INTO schedule"
                + "(name, location, start_date, start_time, end_date, end_time, repeat, memo)"
                + " VALUES ( "
                + "'" + name + "' , '" + location + "', '" + start_date + "', '" + start_time
                + "', '" +  end_date + "', '" + end_time + "', '" + repeat + "', '" + memo + "')";
        database.execSQL(sql);
    }

    public void insertRecord2(String name, String date, String time, String req_time, String memo) { //할 일 추가 함수
        String sql = "INSERT INTO todo"
                + "(name, date, time, req_time, memo)"
                + "VALUES ("
                + "'" + name + "', '" + date + "', '" + time + "', '" + req_time + "', '" + memo + "')";
        database.execSQL(sql);
    }

    public void executeQuery() { //schedule 테이블 조회 함수
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

    public void executeQuery2() { //todo 테이블 조회 함수
        String sql = "SELECT _id, name, date, time, req_time, memo from todo ORDER BY date, time";
        Cursor cursor = database.rawQuery(sql,null);

        for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToNext();

            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String date = cursor.getString(2);
            String time = cursor.getString(3);
            String req_time = cursor.getString(4);
            String memo = cursor.getString(5);

            Log.d("MainActivity", "레코드#" + i + " : " + id + ", " + name + ", " + date + ", " +
                    time + ", " + req_time + ", " + memo);
        }
        cursor.close();
    }

    public ArrayList<Schedule> selectAll() { //schedule 테이블에 있는 모든 데이터를 schedule형 result 배열에 넣어줌
        ArrayList<Schedule> result = new ArrayList<Schedule>();

        try {

            String sql = "SELECT * FROM schedule WHERE start_date = " + "'" + MonthFragment.click_date + "'" + " ORDER BY start_date, start_time";
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

    public ArrayList<Todo> selectAll2() { //todo 테이블에 있는 모든 데이터를 todo형 result 배열에 넣어줌
        ArrayList<Todo> result = new ArrayList<Todo>();

        try {
            Cursor cursor = database.rawQuery("select _id, name, date, time, req_time, memo from todo order by date, time", null);
            for (int i=0; i<cursor.getCount(); i++) {
                cursor.moveToNext();

                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String date = cursor.getString(2);
                String time = cursor.getString(3);
                String req_time = cursor.getString(4);
                String memo = cursor.getString(5);

                Todo todo_item = new Todo(id, name, date, time, req_time, memo);
                result.add(todo_item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void updateSchedule(int position, String dname, String dlocation, String dstart_date, String dstart_time,
                               String dend_date, String dend_time, int drepeat, String dmemo) { //schedule 데이터 수정

        ArrayList<Schedule> items = selectAll();
        Schedule item = items.get(position);
//        Log.d("MainActivity", "이름 : " + item.getName());

        String sql = "UPDATE schedule SET name = " + "'" + dname + "', location =" + "'" + dlocation + "', start_date = " + "'" + dstart_date +
                "', start_time = " + "'" + dstart_time + "', end_date = " + "'" + dend_date +
                "', end_time = " + "'" + dend_time + "', repeat = " + "'" + drepeat + "', memo = " + "'" + dmemo +
                "' WHERE _id = " + item.get_id();
        database.execSQL(sql);

        items = selectAll();
        ScheduleAdapter adapter2 = MonthFragment.adapter;
        adapter2.setItems(items);
        MonthFragment.recyclerView.setAdapter(adapter2);

        executeQuery();
    }

    public void deleteSchedule(int position) { // schedule 데이터 삭제
        ArrayList<Schedule> items = selectAll();
        Schedule item = items.get(position);

        String sql = "DELETE FROM schedule WHERE name = " + "'" + item.getName() + "'" + "AND start_date = " + "'" + item.getStart_date() + "'";
        database.execSQL(sql);

        items = selectAll();
        ScheduleAdapter adapter2 = MonthFragment.adapter;
        adapter2.setItems(items);
        MonthFragment.recyclerView.setAdapter(adapter2);

    }

    public ArrayList<Schedule> selectWeekSchedule() {
        ArrayList<Schedule> result = new ArrayList<Schedule>();
        String sql = "SELECT * FROM schedule ORDER BY start_date, start_time";

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


}
