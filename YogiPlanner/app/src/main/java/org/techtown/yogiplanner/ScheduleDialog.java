package org.techtown.yogiplanner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog; //☆
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ScheduleDialog extends Dialog { //MonthFragment에서 사용하는 ScheduleDialog
    EditText name;
    EditText location;
    EditText start_date;
    EditText start_time;
    EditText end_date;
    EditText end_time;

    RadioGroup rg;
    RadioButton radioButton1;
    RadioButton radioButton2;
    RadioButton radioButton3;
    RadioButton radioButton4;

    EditText memo;

    ArrayList<TimeItem> timeItems;

    int position = MonthFragment.mPosition;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm");

    Calendar calendar;
    Calendar calendar1;
    Calendar calendar2;
    Calendar calendar3;

    Date date;
    Date time;

    int drepeat;

    public ScheduleDialog(@NonNull Context context) {
        super(context);
        this.position = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { //MonthFragment에서 사용
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_dialog);

        name = findViewById(R.id.name);
        location = findViewById(R.id.location);
        start_date = findViewById(R.id.start_date);
        start_time = findViewById(R.id.start_time);
        end_date = findViewById(R.id.end_date);
        end_time = findViewById(R.id.end_time);
        rg = findViewById(R.id.rg);
        radioButton1 = findViewById(R.id.radioButton);
        radioButton2 = findViewById(R.id.radioButton2);
        radioButton3 = findViewById(R.id.radioButton3);
        radioButton4 = findViewById(R.id.radioButton4);

        memo = findViewById(R.id.memo);

        String sql = "SELECT * FROM time WHERE start_date = " + "'" + MonthFragment.click_date + "'" + " ORDER BY start_date, start_time";
        timeItems = ((MainActivity)MainActivity.mContext).selectTime(sql);
        TimeItem item = timeItems.get(position);

        name.setText(item.getName());
        location.setText(item.getLocation());
        start_date.setText(item.getStart_date());
        start_time.setText(item.getStart_time());
        end_date.setText(item.getEnd_date());
        end_time.setText(item.getEnd_time());

        final int repeat_type = item.getRepeat(); //☆ 반복삭제용, final 첨써봐서 혹시몰라서,, 상관없으면 repeat랑 합쳐도 됨

        int repeat = item.getRepeat();

        if (repeat == 1) {
            radioButton1.setChecked(true);
        }
        else if (repeat == 2) {
            radioButton2.setChecked(true);
        }
        else if (repeat == 3) {
            radioButton3.setChecked(true);
        }
        else if (repeat == 4) {
            radioButton4.setChecked(true);
        }
        else if (repeat == -1) {
            radioButton1.setChecked(true);
        }

        memo.setText(item.getMemo());

        //시작 날짜
        calendar = Calendar.getInstance();

        try {
            date = simpleDateFormat.parse(item.getStart_date());
        } catch (ParseException e) {}

        calendar.setTime(date);

        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), myDatePicker, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //종료 날짜
        calendar1 = Calendar.getInstance();
        try {
            date = simpleDateFormat.parse(item.getEnd_date());
        } catch (ParseException e) {}

        calendar1.setTime(date);
        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), myDatePicker2, calendar1.get(Calendar.YEAR),
                        calendar1.get(Calendar.MONTH), calendar1.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //시작 시간
        calendar2 = Calendar.getInstance();

        try {
            time = simpleDateFormat2.parse(item.getStart_time());
        } catch (ParseException e) {}

        calendar2.setTime(time);
        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar2.get(Calendar.HOUR_OF_DAY);
                int minute = calendar2.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), myTimePicker, hour, minute, false);
                timePickerDialog.setTitle("시작 시간");
                timePickerDialog.show();
            }
        });

        // 종료 시간
        calendar3 = Calendar.getInstance();
        try {
            time = simpleDateFormat2.parse(item.getEnd_time());
        } catch (ParseException e) {}

        calendar3.setTime(time);
        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar3.get(Calendar.HOUR_OF_DAY);
                int minute = calendar3.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), myTimePicker2, hour, minute, false);
                timePickerDialog.setTitle("종료 시간");
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
                String dlocation = location.getText().toString();
                String dstart_date = start_date.getText().toString();
                String dstart_time = start_time.getText().toString();
                String dend_date = end_date.getText().toString();
                String dend_time = end_time.getText().toString();

                if (radioButton1.isChecked()) drepeat = 1;
                else if (radioButton2.isChecked()) drepeat = 2;
                else if (radioButton3.isChecked()) drepeat = 3;
                else if (radioButton4.isChecked()) drepeat = 4;
                else drepeat = -1;

                String dmemo = memo.getText().toString();

                ((MainActivity)MainActivity.mContext).updateSchedule(position, dname, dlocation,
                        dstart_date, dstart_time, dend_date, dend_time, drepeat, dmemo);
                dismiss();
            }
        });

        final int[] selectedItem = {0}; //☆

        Button del_button = findViewById(R.id.del_button);
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //참고: https://loveiskey.tistory.com/171
                //☆ 반복 일정일 경우 삭제 고르는 코드, 여기부터
                if(repeat_type != 1) {
                    final String[] items = new String[]{"이 일정만 삭제", "이후 일정 모두 삭제", "전체 반복 일정 삭제"};

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog  .setTitle("반복 일정 삭제")
                            .setCancelable(true)
                            .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    selectedItem[0] = which;
                                }
                            })
                            .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    if(items[selectedItem[0]] == "이 일정만 삭제"){ //이건 계속 갱신 됨
                                        ((MainActivity)MainActivity.mContext).deleteSchedule(position);}
                                    else if(items[selectedItem[0]] == "이후 일정 모두 삭제"){
                                        ((MainActivity)MainActivity.mContext).deleteSchedule(position, 2);}
                                    else if(items[selectedItem[0]] == "전체 반복 일정 삭제"){
                                        ((MainActivity)MainActivity.mContext).deleteSchedule(position, 3);}
                                }
                            })
                            .setNeutralButton("취소", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    dismiss();
                                    /*Toast.makeText(getContext()
                                            , "취소 버튼을 눌렀습니다."
                                            , Toast.LENGTH_SHORT).show();*/
                                }
                            });
                    dialog.create();
                    dialog.show();
                } else//여기까지
                ((MainActivity)MainActivity.mContext).deleteSchedule(position);
                dismiss();
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

    public String addZero(int i) {
       if (i < 10) {
           return "0" + String.valueOf(i);
       }
       else {
           return String.valueOf(i);
       }
    }

}
