package org.techtown.yogiplanner;

public class Schedule {
    int _id;
    String name;
    String location;
    String start_date;
    String start_time;
    String end_date;
    String end_time;
    int repeat;
    String memo;

    public Schedule(int _id, String name, String location, String start_date, String start_time, String end_date, String end_time,
                    int repeat, String memo) {
        this._id = _id;
        this.name = name;
        this.location = location;
        this.start_date = start_date;
        this.start_time = start_time;
        this.end_date = end_date;
        this.end_time = end_time;
        this.repeat = repeat;
        this.memo = memo;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
