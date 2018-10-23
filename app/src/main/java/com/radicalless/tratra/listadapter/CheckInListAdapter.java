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

public class CheckInListAdapter extends ArrayAdapter {

    private final Activity context;
    private final JSONArray data;

    public CheckInListAdapter(Activity context, JSONArray data, String[] dummy) throws JSONException {
        super(context, R.layout.check_in_list_row, dummy);
        this.context = context;
        this.data = data;
        Log.d("MainXX", data.getJSONObject(1).getString("placeName"));
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.check_in_list_row, null, true);
        // define text place
        TextView txtPlaceName = rowView.findViewById(R.id.txtPlaceName);
        TextView txtTime = rowView.findViewById(R.id.txtTime);
        // set text based on position
        try {
            txtPlaceName.setText(data.getJSONObject(position).getString("placeName"));
            txtTime.setText(data.getJSONObject(position).getString("date"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;
    }
}
