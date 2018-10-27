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
        try {
            // data
            String placeName = data.getJSONObject(position).getString("placeName");
            String dateString = data.getJSONObject(position).getString("date");
            String dateToShow = "";
            // control data
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(dateString);
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(date);
            //- today
            Date today = new Date();
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(today);
            // compare
            if(dateCal.get(Calendar.DATE) == todayCal.get(Calendar.DATE) &&
                    dateCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                    dateCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    dateCal.get(Calendar.ERA) == todayCal.get(Calendar.ERA)
                    ) {
                dateToShow = "today";
            } else if(dateCal.get(Calendar.DATE) == -1 + todayCal.get(Calendar.DATE) &&
                    dateCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                    dateCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    dateCal.get(Calendar.ERA) == todayCal.get(Calendar.ERA)) {
                dateToShow = "yesterday";
            } else {
                dateToShow = new SimpleDateFormat("yyyy-MM-dd").format(date);
            }

            // set text based on position
            txtPlaceName.setText(data.getJSONObject(position).getString("placeName"));
            txtTime.setText(dateToShow);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rowView;
    }
}
