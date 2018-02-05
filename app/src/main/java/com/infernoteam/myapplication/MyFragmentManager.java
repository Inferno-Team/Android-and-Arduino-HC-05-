package com.infernoteam.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mohammed Issa on 2/5/2018.
 */

public class MyFragmentManager extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.myfragmentmanager,container,false);
        TextView T=view.findViewById(R.id.T);
        TextView H=view.findViewById(R.id.H);
        String str=getArguments().getString("mess");
        if(str!=null && str.equals("PAUSED !!")){T.setText("PAUSED !!");}
        else if(str!=null && str.equals("help")){
            ArrayList<String>arr_help=getArguments().getStringArrayList("helps");
            fill_help(arr_help,T);
        }else fill(str,T,H);
        return view;
    }

    private void fill_help(ArrayList<String> arr_help, TextView t) {
        StringBuilder full_string= new StringBuilder();
        for (String s:arr_help)
            full_string.append(t.getText()).append("\n").append(s);
        t.setText(full_string.toString());
    }
    private void fill(String str, TextView t, TextView h) {
        String[]arr=str.split(" ");
        if(arr.length>0){
            String temp=getString(R.string.temp)+arr[1];
            String hum=getString(R.string.hum)+arr[0];
            t.setText(temp);
            h.setText(hum);
        }
    }
}
