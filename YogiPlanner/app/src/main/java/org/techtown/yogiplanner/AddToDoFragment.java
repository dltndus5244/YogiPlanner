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
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    MonthFragment monthFragment = new MonthFragment();
    Boolean exception = false;

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
        calendar1.add(Calendar.HOUR_OF_DAY, 1);
        time.setText(calendar1.get(Calendar.HOUR_OF_DAY)+":00");

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar1.get(Calendar.HOUR_OF_DAY);
                int minute = calendar1.get(Calendar.MINUTE);

                CustomTimePicker timePickerDialog = new CustomTimePicker(getContext(), myTimePicker, hour, minute, false);
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

                try {
                    Date dDate = simpleDateFormat.parse(_date);
                    Date dTime = simpleDateFormat2.parse(_time);

                    Calendar calendar = Calendar.getInstance();
                    String sCurDate = simpleDateFormat.format(calendar.getTime());
                    String sCurTime = simpleDateFormat2.format(calendar.getTime());

                    Date dCurDate = simpleDateFormat.parse(sCurDate); //현재날짜
                    Date dCurTime = simpleDateFormat2.parse(sCurTime); //현재시간

                    //dDate와 dCurDate가 같으면 시간 비교(dTime이 dCurTime보다 작으면 안됨)
                    //dDate가 dCurDate보다 작으면 안됨

                    if (dDate.before(dCurDate)) {
                        Toast.makeText(getContext(), "날짜를 다시 입력하세요!", Toast.LENGTH_LONG).show();
                        exception = true;
                    } else if (dDate.equals(dCurDate) && dTime.before(dCurTime)) {
                        Toast.makeText(getContext(), "시간을 다시 입력하세요!", Toast.LENGTH_LONG).show();
                        exception = true;
                    } else {
                        exception = false;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (exception == false) {

                    ((MainActivity)getActivity()).insertTodoRecord(_name, _date, _time, _reqTime, _memo, _priority);

                    ((MainActivity)getActivity()).assignTodo();
                    clearText();

                    ((MainActivity)getActivity()).replaceFragment(monthFragment);
                }


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

    CustomTimePicker.OnTimeSetListener myTimePicker = new CustomTimePicker.OnTimeSetListener() {

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
