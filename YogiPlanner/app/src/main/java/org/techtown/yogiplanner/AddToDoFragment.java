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
import java.util.Calendar;


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

        // 추가 버튼 클릭 이벤트
        Button add_button = rootView.findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _name = name.getText().toString();
                String _date = date.getText().toString();
                String _time = time.getText().toString();
                String _reqTime = reqTime.getText().toString();
                String _memo = memo.getText().toString();

                ((MainActivity) getActivity()).insertRecord2(_name, _date, _time, _reqTime, _memo);
                clearText();
            }
        });

        // 취소 버튼 클릭 이벤트
        Button close_button = rootView.findViewById(R.id.close_button);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).executeQuery2();
            }
        });

        return rootView;
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
