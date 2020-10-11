package org.techtown.yogiplanner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddScheduleFragment extends Fragment {
    EditText name;
    EditText location;
    EditText start_date;
    EditText start_time;
    EditText end_date;
    EditText end_time;

    RadioGroup rg;
    RadioButton radio1;
    RadioButton radio2;
    RadioButton radio3;
    RadioButton radio4;

    int _repeat;

    EditText memo;

    Calendar calendar;
    Calendar calendar1;
    Calendar calendar2;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_add_schedule, container, false);

        name = rootView.findViewById(R.id.name);
        location = rootView.findViewById(R.id.location);

        start_date = rootView.findViewById(R.id.start_date);
        start_time = rootView.findViewById(R.id.start_time);

        end_date = rootView.findViewById(R.id.end_date);
        end_time = rootView.findViewById(R.id.end_time);

        rg = rootView.findViewById(R.id.rg);
        radio1 = rootView.findViewById(R.id.radioButton);
        radio2 = rootView.findViewById(R.id.radioButton2);
        radio3 = rootView.findViewById(R.id.radioButton3);
        radio4 = rootView.findViewById(R.id.radioButton4);

        memo = rootView.findViewById(R.id.memo);

        // 날짜 선택 창
        calendar = Calendar.getInstance();
        start_date.setText(simpleDateFormat.format(calendar.getTime()));

        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), myDatePicker, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        end_date.setText(simpleDateFormat.format(calendar.getTime()));
        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), myDatePicker2, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // 시간 선택 창
        calendar1 = Calendar.getInstance();
        start_time.setText(simpleDateFormat2.format(calendar1.getTime()));

        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar1.get(Calendar.HOUR_OF_DAY);
                int minute = calendar1.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), myTimePicker, hour, minute, false);
                timePickerDialog.setTitle("시작 시간");
                timePickerDialog.show();
            }
        });

        calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.HOUR, 1);
        end_time.setText(simpleDateFormat2.format(calendar2.getTime()));

        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar2.get(Calendar.HOUR_OF_DAY);
                int minute = calendar2.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), myTimePicker2, hour, minute, false);
                timePickerDialog.setTitle("종료 시간");
                timePickerDialog.show();
            }
        });

        // 추가 버튼 클릭 이벤트 -> 데이터 삽입
        Button add_button = rootView.findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _name = name.getText().toString();
                String _location = location.getText().toString();

                String _start_date = start_date.getText().toString();
                String _start_time = start_time.getText().toString();

                String _end_date = end_date.getText().toString();
                String _end_time = end_time.getText().toString();

                if (radio1.isChecked()) _repeat = 1;
                else if (radio2.isChecked()) _repeat = 2;
                else if (radio3.isChecked()) _repeat = 3;
                else if (radio4.isChecked()) _repeat = 4;
                else _repeat = -1;

                String _memo = memo.getText().toString();


                ((MainActivity)getActivity()).insertScheduleRecord(_name, _location, _start_date, _start_time,
                        _end_date, _end_time, _repeat, _memo, 0); //★ ori_id에 0 넣기 추가

                if(_repeat ==  2 || _repeat == 3 || _repeat == 4){   //반복할경우 repeat table에 data 넣어주는 역할 ★
                    ((MainActivity)getActivity()).insertRepeatRecord(_repeat, _start_date, _end_date);
                    ((MainActivity)getActivity()).repeatSchedule(_repeat);
                    /*Log.d("MainActivity", "---------------바뀐일정--------------");
                    ((MainActivity)getActivity()).executeScheduleQuery(); //Schedule Table 내용 보기
                    Log.d("MainActivity", "---------------바뀐리핏--------------");
                    ((MainActivity)getActivity()).executeRepeatQuery(); //Repeat Table 내용 보기*/
                }

                ((MainActivity)getActivity()).assignTodo();
                clearText();
            }
        });

        // 취소 버튼 클릭 이벤트 (임시로 db 조회 함수)
        Button close_button = rootView.findViewById(R.id.close_button);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).executeScheduleQuery();

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

            start_date.setText(simpleDateFormat.format(myCalendar.getTime()));
        }
    };

    DatePickerDialog.OnDateSetListener myDatePicker2 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            end_date.setText(simpleDateFormat.format(myCalendar.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener myTimePicker = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            start_time.setText(addZero(hourOfDay) + ":" + addZero(minute));
        }
    };

    TimePickerDialog.OnTimeSetListener myTimePicker2 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            end_time.setText(addZero(hourOfDay) + ":" + addZero(minute));
        }
    };

    public void clearText() {
        name.setText(null);
        location.setText(null);
        start_date.setText(simpleDateFormat.format(calendar.getTime()));
        end_time.setText(simpleDateFormat2.format(calendar1.getTime()));
        rg.clearCheck();
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
