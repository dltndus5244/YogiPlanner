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
    private TodoDialog dialog;
    static TodoAdapter adapter;
    static int passedPosition;
    static RecyclerView recyclerView;

    public static ArrayList<Todo> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_today, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new TodoAdapter();
        recyclerView.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

        String todayDate = sdf.format(calendar.getTime());
        String todayTime = sdf2.format(calendar.getTime());

        String sql = "SELECT * FROM todo WHERE (date = '" + todayDate + "' AND time >= '" + todayTime + "') OR date > '" + todayDate + "' ORDER BY date, time";
        items = ((MainActivity) getActivity()).selectTodo(sql);


        adapter.setItems(items);

        adapter.setOnItemClickListener(new OnTodoItemClickListener() {
            @Override
            public void onItemClick(TodoAdapter.ViewHolder holder, View view, int position) {
                passedPosition = position;

                dialog = new TodoDialog(getContext());
                dialog.show();
            }
        });

        return rootView;
    }
}
