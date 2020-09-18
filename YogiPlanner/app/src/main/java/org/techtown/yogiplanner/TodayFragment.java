package org.techtown.yogiplanner;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TodayFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_today, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        TodoAdapter adapter = new TodoAdapter();
        recyclerView.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

        String todayDate = sdf.format(calendar.getTime());
        String todayTime = sdf2.format(calendar.getTime());

        Log.d("TodayFragment", todayDate + ", " + todayTime);

        String sql = "SELECT * FROM todo WHERE date >= '" + todayDate + "' AND time >= '" + todayTime + "' order by priority";
        ArrayList<Todo> result = ((MainActivity) getActivity()).selectTodo(sql);
        adapter.setItems(result);

        return rootView;
    }
}
