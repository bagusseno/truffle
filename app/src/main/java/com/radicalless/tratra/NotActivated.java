package com.radicalless.tratra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotActivated extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Button mVerifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_activated);

        pref = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        Button logoutBtn = (Button)findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.remove("sessionToken");
                editor.commit();
                Log.d("UAAAAAAAAAAAAAAAAAA", "CHanges: " + pref.getString("sessionToken", "NULL"));
                startActivity(new Intent(NotActivated.this, LoginActivity.class));
                finish();
            }
        });

        mVerifyButton = findViewById(R.id.verifyBtn);
        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyNewAccount();
            }
        });
    }

    private void verifyNewAccount() {
        // show progress
        mVerifyButton.setVisibility(View.GONE);
        final ProgressBar loading = findViewById(R.id.progressBar);
        loading.setVisibility(View.VISIBLE);

        final Button mVerifyButton = findViewById(R.id.verifyBtn);
        final EditText mVerifyTxt = findViewById(R.id.verifyTxt);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://indomotorart.com/tratra/RESTApi/verifyNewAccount.php";
        StringRequest verifyRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // hide progress
                mVerifyButton.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                // check response
                try {
                    JSONObject responseJSON = new JSONObject(response);
                    String status = responseJSON.getString("status");
                    Log.d("SESSION TOKEN", ""+pref.getString("sessionToken", null));
                    Log.d("RESPONSE STATUS", ""+status);
                    if(status.equals("true")) {
                        editor.putBoolean("activated", true);
                        editor.commit();
                        startActivity(new Intent(NotActivated.this, MainActivity.class));
                        finish();
                    } else {
                        TextView infoText = findViewById(R.id.textView3);
                        infoText.setText(responseJSON.getString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mVerifyButton.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", pref.getString("sessionToken", null));
                params.put("code", mVerifyTxt.getText().toString());
                return params;
            }
        };
        queue.add(verifyRequest);
    }
}
