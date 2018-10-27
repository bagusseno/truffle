package com.radicalless.tratra.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.radicalless.tratra.R;
import com.radicalless.tratra.listadapter.CheckInListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_USERNAME = "userUsername";
    private static final String USER_PROFILE = "userProfile";
    private static final String USER_DISTANCE = "userDistance";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView txtDistance;
    TextView txtUsername;
    ImageView ivProfile;

    ListView checkInListView;
    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sp.edit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        txtDistance = root.findViewById(R.id.TxtDistance);
        txtUsername = root.findViewById(R.id.TxtUsername);
        ivProfile = root.findViewById(R.id.ProfilePicture);
        checkInListView = root.findViewById(R.id.checkInList);

        txtDistance.setText(""+sp.getInt(USER_DISTANCE, 0));

        String username = sp.getString(USER_USERNAME, null);
        if(username != null) {
            txtUsername.setText("@" + username);
        }

        String profileImg = sp.getString(USER_PROFILE, null);
        if(profileImg != null) {
            byte[] byteImage = Base64.decode(profileImg.getBytes(), Base64.DEFAULT);
            ivProfile.setImageBitmap(BitmapFactory.decodeByteArray(byteImage,0, byteImage.length));
        }

        // initialize map fragment
        //MapAddonFragment mapAddonFragment = new MapAddonFragment();
        //getChildFragmentManager().beginTransaction().replace(R.id.MapAddonFrame, mapAddonFragment).commit();
        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        updateData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    public void updateData() {

        final RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://indomotorart.com/tratra/RESTApi/getCheckIns.php";

        StringRequest getDataReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // parse response JSON to array
                JSONObject responseJSON = null;
                try {
                    responseJSON = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                boolean status = false;
                if(responseJSON != null) {
                    try {
                        status = Boolean.valueOf(responseJSON.getString("status"));
                        if(status) {
                            // TODO: SUCCESS
                            // get lat longs and calculate distance
                            JSONArray checkInList = responseJSON.getJSONArray("data");

                            // set up username
                            String username = responseJSON.getString("username");
                            editor.putString("username", username);
                            txtUsername.setText("@"+username);

                            // set up profile image
                            String ppUrl = responseJSON.getString("photo");
                            Log.d("Main", ppUrl);
                            // download image
                            ImageRequest imageRequest = new ImageRequest(ppUrl, new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    response.compress(Bitmap.CompressFormat.PNG, 90, stream);
                                    byte[] imageByte = stream.toByteArray();
                                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageByte);
                                    if(response.hasMipMap()) {
                                        ivProfile.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                                        // save to session
                                        String encodedImg = Base64.encodeToString(imageByte, 0);
                                        editor.putString("profileImg", encodedImg);
                                    }
                                }
                            }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_4444, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                }
                            });
                            queue.add(imageRequest);

                            // set up distance
                            int distance = responseJSON.getInt("distance");
                            String unit = " M";
                            StringBuilder distanceTxt = new StringBuilder(distance+"");

                            if(distance > 999) {
                                unit = " KM";
                                distanceTxt.delete(distanceTxt.length()-2, distanceTxt.length());
                                distanceTxt.insert(distanceTxt.length()-1, ".");
                            }
                            txtDistance.setText(distanceTxt + unit);
                            editor.putInt("myDistance", distance);
                            // update check in list
                            String[] dummy = new String[responseJSON.getJSONArray("data").length()];
                            CheckInListAdapter checkInListAdapter = new CheckInListAdapter(getActivity(), responseJSON.getJSONArray("data"), dummy);
                            checkInListView.setAdapter(checkInListAdapter);
                        } else {
                            // TODO: FAIL
                            Log.d("RecordingFragment", "Response: FAIL. " + responseJSON.getString("msg"));
                            updateData();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("PostActivity", "Unable to process response!");
                    final AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("Unable to get data from server.")
                            .setMessage("An error occurred while trying to get your data.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("PostActivity", "Error response. " + error.getMessage());
                final AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Unable to get data from server.")
                        .setMessage("An error occurred while trying to get your data. Error: " + error.getMessage())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String,String>();
                params.put("token", sp.getString("sessionToken", "0"));
                return params;
            }
        };
        queue.add(getDataReq);
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double latDiff = lat2-lat1;
        double lonDiff = lon2-lon1;
        double diffLat = Math.toRadians(latDiff);
        double diffLon = Math.toRadians(lonDiff);
        double a = Math.sin(diffLat/2) * Math.sin(diffLat/2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(diffLon/2) * Math.sin(diffLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return c * 6371e3;
    }
}
