package org.techtown.yogiplanner;

public class Todo {
    int _id;
    String name;
    String date; //마감날짜
    String time; //마감시간
    String req_time; //예상소요시간
    String memo;
    float priority;

    public Todo(int _id, String name, String date, String time, String req_time, String memo, float priority) {
        this._id = _id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.req_time = req_time;
        this.memo = memo;
        this.priority = priority;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getReq_time() {
        return req_time;
    }

    public void setReq_time(String req_time) {
        this.req_time = req_time;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public float getPriority() {
        return priority;
    }

    public void setPriority(float priority) {
        this.priority = priority;
    }


}
