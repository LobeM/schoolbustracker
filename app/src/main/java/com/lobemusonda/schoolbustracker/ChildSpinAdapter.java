package com.lobemusonda.schoolbustracker;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lobemusonda on 10/17/18.
 */

public class ChildSpinAdapter extends ArrayAdapter<Child> {

    private Context context;
    //    Your custom values for the spinner
    private ArrayList<Child> values;


    public ChildSpinAdapter(@NonNull Context context, int textViewResourceId, ArrayList<Child> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Child getItem(int position){
        return values.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

//    And the magic goes here
//    This is for the "passive" state of the spinner

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        A dynamic text view is created here
        TextView label = (TextView) super.getView(position, convertView, parent);
//        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
//        Then you get the current item using the values array and the current position
//        You can now reference each method you created in your bean object
        label.setText(values.get(position).getFirstName() + " " + values.get(position).getLastName());

//        And finally return your dynamic view for each spinner
        return label;
    }

//    And here is when the "chooser" is popped up
//    Normally is the same view, but you can customize it if you want

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
//        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getFirstName() + " " + values.get(position).getLastName());
        return label;
    }
}
