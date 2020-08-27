package com.myfav.rider.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.myfav.rider.R;


public class Utilities {
    private Context context;
    public Utilities(Context context)
    {
        this.context = context;
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void hideKeypad(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static  String capsWord(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    public static void disableEditText(EditText view)
    {
        view.setFocusableInTouchMode(false);
        view.setFocusable(false);
        view.setEnabled(false);
        view.setCursorVisible(false);
        view.setKeyListener(null);
    }

    public static void setError(EditText formView, TextView errorView, String Message) {
        errorView.setText(Message);
        formView.setBackgroundResource(R.drawable.text_border_error);
    }

    public static void clearError(EditText formView, TextView errorView) {
        errorView.setText("");
        formView.setBackgroundResource(R.drawable.text_border_selector);
    }


}
