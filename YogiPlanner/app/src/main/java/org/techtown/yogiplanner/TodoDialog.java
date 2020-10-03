package org.techtown.yogiplanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TodoDialog extends Dialog {
    EditText name;
    EditText due_date;
    EditText due_time;
    EditText req_time;
    EditText memo;

    ArrayList<Todo> items;
    int position = TodayFragment.passedPosition;

    Calendar calendar;
    Calendar calendar1;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm");

    Date date;
    Date time;

    public TodoDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_dialog);

        name = findViewById(R.id.name);
        due_date = findViewById(R.id.due_date);
        due_time = findViewById(R.id.due_time);
        req_time = findViewById(R.id.req_time);
        memo = findViewById(R.id.memo);

        calendar = Calendar.getInstance();

        String todayDate = simpleDateFormat.format(calendar.getTime());
        String todayTime = simpleDateFormat2.format(calendar.getTime());

        String sql = "SELECT * FROM todo WHERE (date = '" + todayDate + "' AND time >= '" + todayTime + "') OR date > '" + todayDate + "' ORDER BY date, time";
        items = ((MainActivity)MainActivity.mContext).selectTodo(sql);

        Todo item = items.get(position);

        name.setText(item.getName());
        due_date.setText(item.getDate());
        due_time.setText(item.getTime());
        req_time.setText(item.getReq_time());
        memo.setText(item.getMemo());

        // 날짜 선택 창
        try {
            date = simpleDateFormat.parse(item.getDate());
        } catch (ParseException e) {}

        calendar.setTime(date);

//        due_date.setText(simpleDateFormat.format(date));
        due_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), myDatePicker, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // 시간 선택 창
        calendar1 = Calendar.getInstance();
        try {
            time = (simpleDateFormat2.parse(item.getTime()));
        } catch (ParseException e) {}

        calendar1.setTime(time);

        due_time.setText(simpleDateFormat2.format(time));
        due_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar1.get(Calendar.HOUR_OF_DAY);
                int minute = calendar1.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), myTimePicker, hour, minute, false);
                timePickerDialog.show();
            }
        });

        ImageButton close_button = findViewById(R.id.close_button);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 수정 버튼 클릭 이벤트 -> 데이터 수정
        Button re_button = findViewById(R.id.re_button);
        re_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dname = name.getText().toString();
                String dDate = due_date.getText().toString();
                String dtime = due_time.getText().toString();
                String dreq_time = req_time.getText().toString();
                String dmemo = memo.getText().toString();

                ((MainActivity)MainActivity.mContext).updateTodo(position, dname,
                        dDate, dtime, dreq_time, dmemo);
                dismiss();

                ((MainActivity)MainActivity.mContext).assignTodo();
            }
        });

        //삭제 버튼 클릭 이벤트 -> 데이터 삭제
        Button del_button = findViewById(R.id.del_button);
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)MainActivity.mContext).deleteTodo(position);
                dismiss();

                ((MainActivity)MainActivity.mContext).assignTodo();
            }
        });
    }

    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener myDatePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            due_date.setText(simpleDateFormat.format(myCalendar.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener myTimePicker = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            due_time.setText(addZero(hourOfDay) + ":" + addZero(minute));
        }
    };

    public String addZero(int i) {
        if (i < 10) {
            return "0" + String.valueOf(i);
        }
        else {
            return String.valueOf(i);
        }
    }
}
