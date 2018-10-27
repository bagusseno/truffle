package com.radicalless.tratra.listadapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.radicalless.tratra.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class LeaderboardListAdapter extends ArrayAdapter {

    private final Activity context;
    private final JSONArray data;

    public LeaderboardListAdapter(Activity context, JSONArray data, String[] dummy) throws JSONException {
        super(context, R.layout.leaderboard_list_row, dummy);
        this.context = context;
        this.data = data;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.leaderboard_list_row, null, true);
        // define text place
        TextView txtUsername = rowView.findViewById(R.id.txtUsername);
        TextView txtDistance = rowView.findViewById(R.id.txtDistance);
        // set text based on position
        try {
            txtUsername.setText(data.getJSONObject(position).getString("username"));
            // set up distance
            int distance = data.getJSONObject(position).getInt("distance");
            String unit = " M";
            StringBuilder distanceTxt = new StringBuilder(distance+"");
            if(distance > 999) {
                unit = " KM";
                distanceTxt.delete(distanceTxt.length()-2, distanceTxt.length());
                distanceTxt.insert(distanceTxt.length()-1, ".");
            }
            txtDistance.setText(distanceTxt + unit);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;
    }
}
