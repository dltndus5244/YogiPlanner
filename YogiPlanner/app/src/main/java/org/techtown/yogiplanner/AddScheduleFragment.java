package org.techtown.yogiplanner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    Boolean exception1 = false;
    Boolean exception2 = false;

    MonthFragment monthFragment = new MonthFragment();

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
        start_time.setText(calendar1.get(Calendar.HOUR_OF_DAY) + ":00");

        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar1.get(Calendar.HOUR_OF_DAY);
                int minute = calendar1.get(Calendar.MINUTE);

                CustomTimePicker timePickerDialog = new CustomTimePicker(getContext(), myTimePicker, hour, minute, false);
                timePickerDialog.setTitle("시작 시간");
                timePickerDialog.show();

            }
        });

        calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.HOUR_OF_DAY, 1);
        end_time.setText(calendar2.get(Calendar.HOUR_OF_DAY) + ":00");

        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar2.get(Calendar.HOUR_OF_DAY);
                int minute = calendar2.get(Calendar.MINUTE);

                CustomTimePicker timePickerDialog = new CustomTimePicker(getContext(), myTimePicker2, hour, minute, false);
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

                /*
                1. 입력날짜, 입력시간 예외처리
                2. 추가하려는 시간에 이미 일정이 있을 경우 추가 안되게
                 */
                try {
                    Date dstart_date = simpleDateFormat.parse(_start_date);
                    Date dend_date = simpleDateFormat.parse(_end_date);
                    Date dstart_time = simpleDateFormat2.parse(_start_time);
                    Date dend_time = simpleDateFormat2.parse(_end_time);

                    if (dend_date.before(dstart_date)) {
                        exception1 = true;
                        Toast.makeText(getContext(), "날짜를 다시 입력하세요!", Toast.LENGTH_LONG).show();
                    } else if (dend_time.before(dstart_time) || dend_time.equals(dstart_time)) {
                        exception1 = true;
                        Toast.makeText(getContext(), "시간을 다시 입력하세요!", Toast.LENGTH_LONG).show();
                    } else {
                        exception1 = false;
                    }

                    String sql = "SELECT * FROM schedule WHERE start_date = '" + _start_date + "'";
                    ArrayList<Schedule> schedules = ((MainActivity)getActivity()).selectSchedule(sql);

                    for (int i=0; i<schedules.size(); i++) {
                        Date s1 = simpleDateFormat2.parse(schedules.get(i).start_time);
                        Date e1 = simpleDateFormat2.parse(schedules.get(i).end_time);

                        Date s2 = dstart_time;
                        Date e2 = dend_time;

                        if ((s1.getTime() < e2.getTime()) && (s2.getTime() < e1.getTime())) {
                            exception2 = true;
                            Toast.makeText(getContext(), "해당 시간에 이미 일정이 있습니다!", Toast.LENGTH_LONG).show();
                            break;
                        } else {
                            exception2 = false;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (exception1 == false && exception2 == false) {
                    if (_name.equals("s1")) {
                        ((MainActivity)getActivity()).setSchedule1();
                    } else {

                        ((MainActivity) getActivity()).insertScheduleRecord(_name, _location, _start_date, _start_time,
                                _end_date, _end_time, _repeat, _memo, 0);

<<<<<<< HEAD
                        if (_repeat == 2 || _repeat == 3 || _repeat == 4) {
                            ((MainActivity) getActivity()).repeatSchedule100(_repeat);
                        }
=======

                    if(_repeat ==  2 || _repeat == 3 || _repeat == 4){
                        ((MainActivity)getActivity()).repeatSchedule100(_repeat);
>>>>>>> de3d5531bc33d54c3e42dd88cb588fbdf702a19f
                    }

                    ((MainActivity)getActivity()).assignTodo();
                    clearText();

                    ((MainActivity)getActivity()).replaceFragment(monthFragment);
                }
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

    CustomTimePicker.OnTimeSetListener myTimePicker = new CustomTimePicker.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            start_time.setText(addZero(hourOfDay) + ":" + addZero(minute));
        }
    };

    CustomTimePicker.OnTimeSetListener myTimePicker2 = new CustomTimePicker.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            end_time.setText(addZero(hourOfDay) + ":" + addZero(minute));
        }
    };

    public void clearText() {
        name.setText(null);
        location.setText(null);
        start_date.setText(simpleDateFormat.format(calendar.getTime()));
        end_date.setText(simpleDateFormat.format(calendar.getTime()));
        start_time.setText(calendar1.get(Calendar.HOUR_OF_DAY) + ":00");
        end_time.setText(calendar2.get(Calendar.HOUR_OF_DAY) + ":00");
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
