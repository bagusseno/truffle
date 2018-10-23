package com.radicalless.tratra;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {

    private static Session session;
    SharedPreferences sessionPref;

    public static Session getInstance(Context context) {
        if(session == null) {
            session = new Session(context);
        }
        return session;
    }

    public Session(Context context) {
        sessionPref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
    }

    public SharedPreferences.Editor editor() {

        return sessionPref.edit();
    }

    public boolean isLoggedIn() {
        if (sessionPref.getString("sessionToken", null) == null) {
            return false;
        }
        return true;
    }

}
