package org.techtown.yogiplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

        createScheduleTable();
        createTodoTable();

    }

    public void replaceFragment(Fragment fragment) { //프래그먼트 교체 함수
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    private void createDatabase() { //데이터베이스 생성 : planner.db
        database = openOrCreateDatabase("planner.db", MODE_PRIVATE, null);
        Log.d("MainActivity", "데이터베이스 생성");
    }

    private void createScheduleTable() { // Schedule 테이블 생성
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

    public void insertScheduleRecord(String name, String location, String start_date, String start_time,
                             String end_date, String end_time, int repeat, String memo) { //스케줄 추가 함수 - AddScheduleFragment 에서 사용
        Log.d("MainActivity", "insertRecord 실행됨");

        String sql = "INSERT INTO schedule"
                + "(name, location, start_date, start_time, end_date, end_time, repeat, memo)"
                + " VALUES ( "
                + "'" + name + "' , '" + location + "', '" + start_date + "', '" + start_time
                + "', '" +  end_date + "', '" + end_time + "', " + repeat + " , '" + memo + "')";
        database.execSQL(sql);
    }

    public void insertTodoRecord(String name, String date, String time, String req_time, String memo, float priority) { //할 일 추가 함수 - AddTodoFragment 에서 사용
        String sql = "INSERT INTO todo"
                + "(name, date, time, req_time, memo, priority)"
                + " VALUES ( "
                + "'" + name + "', '" + date + "', '" + time + "', '" + req_time + "', '" + memo + "' , " + priority + ")";

        database.execSQL(sql);
        Log.d("MainActivity", "todo 데이터 추가");
    }

    public void executeScheduleQuery() { //schedule 테이블 조회 함수(확인용) - AddScheduleFragment
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

    public void executeTodoQuery() { //todo 테이블 조회 함수(확인용) - AddToDoFragment
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

    /* 쿼리 수행 결과에 따라 Schedule 테이블의 데이터를 배열에 넣어줌
        매개변수 : String sql
        사용 : MonthFragment, ScheduleDialog
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

        return result; //쿼리 수행 결과 저장 배열
    }

    /* 쿼리 수행 결과에 따라 Todo 테이블의 데이터를 배열에 넣어줌
    매개변수 : String sql
    사용 : TodayFragment
 */
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

    /* Schedule 데이터 수정 함수
        매개변수로 수정할 데이터의 position을 받아서 해당 위치에 맞는 데이터를 수정해줌
        사용 : ScheduleDialog
     */
    public void updateSchedule(int position, String dname, String dlocation, String dstart_date, String dstart_time,
                               String dend_date, String dend_time, int drepeat, String dmemo) {

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

    public void deleteSchedule(int position) { // schedule 데이터 삭제
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

}