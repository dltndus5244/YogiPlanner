package org.techtown.yogiplanner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class AddToDoFragment extends Fragment {
    EditText name;
    EditText date;
    EditText time;
    EditText reqTime;
    EditText memo;

    Calendar calendar;
    Calendar calendar1;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_add_to_do, container, false);

        name = rootView.findViewById(R.id.name);
        date = rootView.findViewById(R.id.date);
        time = rootView.findViewById(R.id.time);
        reqTime = rootView.findViewById(R.id.req_time);
        memo = rootView.findViewById(R.id.memo);

        // 날짜 선택 창
        calendar = Calendar.getInstance();
        date.setText(simpleDateFormat.format(calendar.getTime()));

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), myDatePicker, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // 시간 선택 창
        calendar1 = Calendar.getInstance();
        time.setText(simpleDateFormat2.format(calendar1.getTime()));

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar1.get(Calendar.HOUR);
                int minute = calendar1.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), myTimePicker, hour, minute, false);
                timePickerDialog.show();
            }
        });

        /*
            추가 버튼 이벤트 -> 할 일 DB에 추가
            우선순위를 계산해서 db에 넣어지도록 수정함
         */
        Button add_button = rootView.findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _name = name.getText().toString();
                String _date = date.getText().toString();
                String _time = time.getText().toString();
                String _reqTime = reqTime.getText().toString();
                String _memo = memo.getText().toString();
                float _priority = (float) getRemainTime(_date, _time) / Float.parseFloat(_reqTime);
                Log.d("Todo", "우선순위 : " + _priority);

                ((MainActivity)getActivity()).insertTodoRecord(_name, _date, _time, _reqTime, _memo, _priority);
                clearText();
            }
        });

        // 취소 버튼 클릭 이벤트
        Button close_button = rootView.findViewById(R.id.close_button);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).executeTodoQuery();
            }
        });

        return rootView;
    }

    public int getRemainTime(String endDate, String endTime)  { //여유시간 구하는 함수
        int result;
        int result1, result2 = 0, result3;

        String sEndDate = "";
        String sCurDate = "";

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH"); //분 단위는 해결해야함.,
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        Date dToday = new Date(System.currentTimeMillis());
        int curTime = Integer.parseInt(hourFormat.format(dToday));

        String[] splitEndDate = endDate.split("/");

        for (int i=0; i<splitEndDate.length; i++)
            sEndDate = sEndDate + splitEndDate[i];

        sCurDate = dateFormat.format(dToday);

        //result1
        if (sEndDate.equals(sCurDate))
            result1 = 0;
        else
            result1 = 24 - curTime;

        //result2
        if (sEndDate.equals(sCurDate))
            result2 = 0;
        else {
            try {
                Date dCurDate = dateFormat.parse(sCurDate);
                Date dEndDate = dateFormat.parse(sEndDate);

                long diffDay = (dEndDate.getTime() - dCurDate.getTime()) / (24 * 60 * 60 * 1000);
                result2 = ((int) diffDay - 1) * 24;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String splitEndTime[] = endTime.split(":");

        //result3
        if (sEndDate.equals(sCurDate)) {
            result3 = Integer.parseInt(splitEndTime[0]) - curTime;
        }
        else {
            result3 = Integer.parseInt(splitEndTime[0]);
        }

        result = result1 + result2 + result3;
        return result;
    }

    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener myDatePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            date.setText(simpleDateFormat.format(myCalendar.getTime()));
        }
    };


    TimePickerDialog.OnTimeSetListener myTimePicker = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            time.setText(addZero(hourOfDay) + ":" + addZero(minute));
        }
    };

    public void clearText() {
        name.setText(null);
        date.setText(simpleDateFormat.format(calendar.getTime()));
        time.setText(simpleDateFormat2.format(calendar1.getTime()));
        reqTime.setText(null);
        memo.setText(null);
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
