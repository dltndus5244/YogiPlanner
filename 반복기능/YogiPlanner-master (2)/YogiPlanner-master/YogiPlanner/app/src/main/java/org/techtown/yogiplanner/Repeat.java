package org.techtown.yogiplanner;

import android.util.Log;

public class Repeat {
    int _id;
    int repeat_type;
    String start_date;
    String end_date;
    int renew;

    public Repeat(int _id, int repeat_type, String start_date, String end_date, int renew) {
        this._id = _id;
        this.repeat_type = repeat_type;
        this.start_date = start_date;
        this.end_date = end_date;
        this.renew = renew;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getRepeat_type() {
        return repeat_type;
    }

    public void setRepeat_type(int repeat_type) {
        this.repeat_type = repeat_type;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public int getRenew() {
        return renew;
    }

    public void setRenew(int renew) {
        this.renew = renew;
    }
}