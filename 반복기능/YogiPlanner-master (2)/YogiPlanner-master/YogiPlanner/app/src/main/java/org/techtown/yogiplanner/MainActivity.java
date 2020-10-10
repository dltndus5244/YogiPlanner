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
import java.util.GregorianCalendar; //★

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

        /*database.execSQL("DROP TABLE schedule");
        database.execSQL("DROP TABLE todo");
        database.execSQL("DROP TABLE repeat");*/

        createScheduleTable();
        createTodoTable();
        createRepeatTable();    // ★

        //database.execSQL("ALTER TABLE schedule ADD COLUMN ori_id integer"); //반복할 일정의 처음 id를 저장할 속성 추가 ★

        //---이밑으로 테스트용---//
        /*repeatSchedule(2131296485);
        repeatSchedule(2131296486);
        repeatSchedule(2131296487);*/

        /*Log.d("MainActivity", "---------------일정--------------");
        executeScheduleQuery(); //Schedule Table 내용 보기
        Log.d("MainActivity", "---------------리핏--------------");
        executeRepeatQuery(); //Repeat Table 내용 보기*/

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
                + "memo text, "
                + "ori_id integer)";

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

    private void createRepeatTable() { // Repeat 테이블 생성★
        String sql = "CREATE TABLE IF NOT EXISTS repeat ("
                + "_id integer PRIMARY KEY autoincrement, "
                + "repeat_type integer, "  // 매일/매주/매월
                + "start_date text, "
                + "end_date text, "
                + "renew integer default 1 check(renew=1 or renew=0))"; //1이면 계속 o, 0이면 더 이상 갱신x (이 이후로 모두 삭제 썼을 경우)

        database.execSQL(sql);
        Log.d("MainActivity", "repeat 테이블 생성");
    }


    public void insertScheduleRecord(String name, String location, String start_date, String start_time,
                             String end_date, String end_time, int repeat, String memo) { //스케줄 추가 함수 - AddScheduleFragment 에서 사용
        Log.d("MainActivity", "insertRecord 실행됨");

        String sql = "INSERT INTO schedule"
                + "(name, location, start_date, start_time, end_date, end_time, repeat, memo)"
                + " VALUES ( "
                + "'" + name + "' , '" + location + "', '" + start_date + "', '" + start_time
                + "', '" +  end_date + "', '" + end_time + "', " + repeat + " , '" + memo +  "')";
        database.execSQL(sql);
    }

    public void insertScheduleRecord2(String name, String location, String start_date, String start_time,
                                     String end_date, String end_time, int repeat, String memo, int ori_id) { // ★repeat용 스케줄 추가 함수2
        Log.d("MainActivity", "insertRecord2 실행됨");

        String sql = "INSERT INTO schedule"
                + "(name, location, start_date, start_time, end_date, end_time, repeat, memo, ori_id)"
                + " VALUES ( "
                + "'" + name + "' , '" + location + "', '" + start_date + "', '" + start_time
                + "', '" +  end_date + "', '" + end_time + "', " + repeat + " , '" + memo + "', " + ori_id + ")";
        database.execSQL(sql);
    }

    public void insertTodoRecord(String name, String date, String time, String req_time, String memo, float priority) { //할 일 추가 함수 - AddTodoFragment 에서 사용
        String sql = "INSERT INTO todo"
                + "(name, date, time, req_time, memo, priority)"
                + " VALUES ( "
                + "'" + name + "', '" + date + "', '" + time + "', '" + req_time + "', '" + memo + "' , " + priority + ")";

        database.execSQL(sql);
        Log.d("MainActivity", "todo 데이터 추가");

        // ★ 여기부터 추가함
        String what_is_last_date = "SELECT * FROM todo ORDER BY date desc LIMIT 1";//할일의 마지막 마감일 = item2.getDate() [Todo]
        ArrayList<Todo> items = selectTodo(what_is_last_date);
        Todo item = items.get(0);
        String last_date = item.getDate(); //마지막마감일

        if(last_date.compareTo(date) != 1){    //'가장 늦은 마감일 =< 새로 입력된 마감일'일 경우
            Log.d("AddToDoFragment", "일정추가된당!!!!!!!");
            repeatSchedule(2131296485);
            repeatSchedule(2131296486);
            repeatSchedule(2131296487);
        }
        //여기까지 ★

    }

    public void insertRepeatRecord(int repeat_type, String start_date, String end_date) { //}, int renew) { //반복일정 추가 함수 - AddScheduleFragment 에서 사용★
        Log.d("MainActivity", "insertRepeatRecord 실행됨");

        String sql1 = "SELECT * FROM schedule ORDER BY _id desc";   //맨마지막줄=방금입력된일정의 id를 받기 위함

        ArrayList<Schedule> items = selectSchedule(sql1);
        Schedule item = items.get(0);   //마지막 레코드 받아옴

        String sql = "INSERT INTO repeat"
                + "(_id, repeat_type, start_date, end_date, renew)"
                + " VALUES ( "
                + "'" + item.get_id() + "' , '" + repeat_type + "' , '" + start_date + "', '" +  end_date + "', '" + "1" + "')";  //renew는 수정-삭제에서 설정(이 이후로 모두 삭제)
        database.execSQL(sql);
    }

    public void executeScheduleQuery() { //schedule 테이블 조회 함수(확인용) - AddScheduleFragment
        Cursor cursor = database.rawQuery("SELECT _id, name, location, start_date, start_time, " +
                "end_date, end_time, repeat, memo, ori_id from schedule ORDER BY start_date, start_time" , null);

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
            int ori_id = cursor.getInt(9);

            Log.d("MainActivity", "레코드#" + i + " : " + id + ", " + name + ", " + location + ", " +
                    start_date + ", " + start_time + ", " + end_date + ", " + end_time + ", " + repeat + ", " + memo + ", " + ori_id);
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

    public void executeRepeatQuery() { //repeat 테이블 조회 함수(확인용) - AddScheduleFragment ★
        Cursor cursor = database.rawQuery("SELECT _id, repeat_type, start_date, end_date, renew" +
                " from repeat ORDER BY _id" , null);

        for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToNext();
            int id = cursor.getInt(0);
            int repeat_type = cursor.getInt(1);
            String start_date = cursor.getString(2);
            String end_date = cursor.getString(3);
            int renew = cursor.getInt(4);

            Log.d("MainActivity", "Repeat 레코드#" + i + " : " + id + ", " + repeat_type + ", " +
                    start_date + ", " + end_date + ", " + renew);
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
                int ori_id = cursor.getInt(9);

                Schedule schedule_item = new Schedule(id, name, location, start_date, start_time, end_date, end_time,
                                                        repeat, memo, ori_id);
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

    /* 쿼리 수행 결과에 따라 Repeat 테이블의 데이터를 배열에 넣어줌
        매개변수 : String sql
        사용 : MonthFragment, ScheduleDialog
     */
    public ArrayList<Repeat> selectRepeat(String sql) {   //★
        ArrayList<Repeat> result = new ArrayList<Repeat>();

        try {
            Cursor cursor = database.rawQuery(sql,null);

            for (int i=0; i<cursor.getCount(); i++) {
                cursor.moveToNext();

                int id = cursor.getInt(0);
                int repeat_type = cursor.getInt(1);
                String start_date = cursor.getString(2);
                String end_date = cursor.getString(3);
                int renew = cursor.getInt(4);

                Repeat repeat_item = new Repeat(id, repeat_type, start_date, end_date, renew);
                result.add(repeat_item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result; //쿼리 수행 결과 저장 배열
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

/*    public void updateRepeatDate(int ori_id, String dstart_date, String dend_date) { //★

        String sql = "SELECT * FROM repeat WHERE _id = " + "'" + ori_id + "'";
        ArrayList<Repeat> items = selectRepeat(sql);
        Repeat item = items.get(0);

        String update_sql = "UPDATE repeat SET start_date = '" + dstart_date + "', end_date = '" + dend_date + "' WHERE _id = '" + ori_id + "'";

        Log.d("MainActivity", "아이디 : " + ori_id);
        database.execSQL(update_sql);

        items = selectSchedule(sql);
        ScheduleAdapter adapter = MonthFragment.adapter;
        adapter.setItems(items);
        MonthFragment.recyclerView.setAdapter(adapter);
    }*/

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

    public void repeatSchedule(int repeat_type) {     //★ repeat_type 매일(2131296485).매주(2131296486).매월(2131296487) - AddScheduleFragment 에서 사용

        String sql = "SELECT * FROM repeat WHERE repeat_type = '" + repeat_type + "'AND renew = 1";   //반복타입이 매일이고 renew가 1인 레코드 [Repeat]
        ArrayList<Repeat> items = selectRepeat(sql);
        int len = items.size();

        String sql2 = "SELECT * FROM todo ORDER BY date desc LIMIT 1";//할일의 마지막 마감일 = item2.getDate() [Todo]
        ArrayList<Todo> items2 = selectTodo(sql2);
        Todo item2 = items2.get(0);
        String last_date = item2.getDate(); //마지막마감일

        for (int i = 0; i < len; i++) {   //리핏테이블의 모든 일정들 체크,,,,비효율적인디..?
            Repeat item = items.get(i);
            if (item.getStart_date().compareTo(last_date) >= 0) {
                Log.d("MainActivity", "응 끝~" + repeat_type + "스타트데이" + item.getStart_date() + "라스트데이" + last_date);
                continue; //만약 '레코드의 마지막시작일자' 가 'todo 테이블의 마지막 마감기한'보다 크거나 같다면 break하고 다음레코드로 continue
            }
            Log.d("MainActivity", "응 시작~" + repeat_type + "스타트데이" + item.getStart_date() + "라스트데이" + last_date);

            //만약 item.getStart_date()가 item2.getDate()보다 작으면

            String sql3 = "SELECT * FROM schedule WHERE _id = '" + item.get_id() + "' ORDER BY start_date";   //반복 아닌 original 해당 일정 레코드 [Schedule], 시작일-종료일 빼고 다 받아올거임
            ArrayList<Schedule> items3 = selectSchedule(sql3);
            Schedule item3 = items3.get(0);
            //Log.d("MainActivity", "sql3 ok");

            String new_start_date = item.getStart_date();
            String new_end_date = item.getEnd_date();

            SimpleDateFormat fm = new SimpleDateFormat("yyyy/MM/dd");

            while (new_start_date.compareTo(last_date) < 0) {//nsd가 마지막마감일보다 작을(-1) 동안
                //날짜바꾸기, 일정테이블에 넣기*/
                String inp_start_date = null, inp_end_date = null;

                String[] splitDate = new_start_date.split("/"); //시작일 나누는거
                int year = Integer.parseInt(splitDate[0]);
                int month = Integer.parseInt(splitDate[1]);
                int day = Integer.parseInt(splitDate[2]);

                String[] splitDate2 = new_end_date.split("/");  //종료일 나누는거
                int year2 = Integer.parseInt(splitDate2[0]);
                int month2 = Integer.parseInt(splitDate2[1]);
                int day2 = Integer.parseInt(splitDate2[2]);

                try {    //repeat_type 매일(2131296485).매주(2131296486).매월(2131296487)
                    Calendar cal = new GregorianCalendar(year, month - 1, day);
                    Calendar cal2 = new GregorianCalendar(year2, month2 - 1, day2);

                    switch (repeat_type) {
                        case 2131296485:
                            cal.add(Calendar.DAY_OF_MONTH, 1);
                            cal2.add(Calendar.DAY_OF_MONTH, 1);
                            break;
                        case 2131296486:
                            cal.add(Calendar.DAY_OF_MONTH, 7);
                            cal2.add(Calendar.DAY_OF_MONTH, 7);
                            break;
                        case 2131296487:
                            cal.add(Calendar.MONTH, 1);
                            cal2.add(Calendar.MONTH, 1);
                            break;
                    }

                    inp_start_date = fm.format(cal.getTime());
                    inp_end_date = fm.format(cal2.getTime());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                new_start_date = inp_start_date;
                new_end_date = inp_end_date;

                //일정테이블에 일정 추가
                insertScheduleRecord2(item3.getName(), item3.getLocation(), new_start_date, item3.getStart_time(),
                        new_end_date, item3.getEnd_time(), item3.getRepeat(), item3.getMemo(), item3.get_id());

            }
            //리핏테이블 시작일&종료일 바꾸기(최종반복일자로)
            String update_sql = "UPDATE repeat SET start_date = '" + new_start_date + "', end_date = '" + new_end_date + "' WHERE _id = '" + item3.get_id() + "'";
            //Log.d("MainActivity", "아이디 : " + item3.get_id());
            database.execSQL(update_sql);

        }
    }

}