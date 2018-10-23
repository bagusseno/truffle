package com.radicalless.tratra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

/**
 * A login screen that offers login via email/password.
 */
public class SignupActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mPasswordConfView;
    private EditText mEmailView;
    private View mProgressView;
    private View mLoginFormView;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sp.edit();

        // check for session availability
        if(sp.getString("sessionToken", null) != null) {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Set up the login form.
        mUsernameView = findViewById(R.id.signup_username);
        mPasswordView = findViewById(R.id.signup_password);
        mPasswordConfView = findViewById(R.id.signup_passwordConfirm);
        mEmailView = findViewById(R.id.signup_email);

        Button mEmailSignUpButton = findViewById(R.id.signup_submit);
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        TextView signup_login = findViewById(R.id.signup_login);
        signup_login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });

        mLoginFormView = findViewById(R.id.signup_form);
        mProgressView = findViewById(R.id.ProgressLayout);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);
        mPasswordConfView.setError(null);
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordConf = mPasswordConfView.getText().toString();
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }

        // check for password confirmation
        if (TextUtils.isEmpty(passwordConf)) {
            mPasswordConfView.setError(getString(R.string.error_field_required));
            focusView = mPasswordConfView;
            cancel = true;
        }

        if(!TextUtils.equals(password, passwordConf)) {
            mPasswordConfView.setError("Password confirmation doesn't match.");
            focusView = mPasswordConfView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://indomotorart.com/tratra/RESTApi/signup.php";
            StringRequest signupRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    showProgress(false);
                    // parse response JSON to array
                    JSONObject responseJSON = null;
                    try {
                        responseJSON = new JSONObject(response);
                        boolean status = Boolean.valueOf(responseJSON.getString("status"));
                        if(status) {
                            SharedPreferences pref = getSharedPreferences("MyPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("sessionToken", responseJSON.getString("token"));
                            editor.putBoolean("activated", false);
                            editor.commit();
                            startActivity(new Intent(SignupActivity.this, NotActivated.class));
                            finish();
                        } else {
                            TextView errorMsg = (TextView) findViewById(R.id.ErrorMsg);
                            errorMsg.setText(responseJSON.getString("msg"));
                            errorMsg.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        TextView errorMsg = (TextView) findViewById(R.id.ErrorMsg);
                        errorMsg.setText("Fatal error on the code! ");
                        e.printStackTrace();
                        errorMsg.setVisibility(View.VISIBLE);
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    TextView errorMsg = (TextView) findViewById(R.id.ErrorMsg);
                    errorMsg.setText("Something just went wrong...");
                    errorMsg.setVisibility(View.VISIBLE);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String,String>();
                    params.put("username", mUsernameView.getText().toString());
                    params.put("password", mPasswordView.getText().toString());
                    params.put("passwordConf", mPasswordConfView.getText().toString());
                    params.put("email", mEmailView.getText().toString());

                    return params;
                }
            };
            queue.add(signupRequest);

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

}

