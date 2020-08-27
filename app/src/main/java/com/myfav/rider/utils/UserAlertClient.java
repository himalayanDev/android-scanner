package com.myfav.rider.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.myfav.rider.interfaces.UserActionHandler;

public class UserAlertClient {
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private final String TAG = "UserAlertClient";
    private Activity activityRef;

    public UserAlertClient(Activity activity)
    {
        activityRef = activity;
    }

    public void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(activityRef);
        waitDialog.setTitle(message);
        waitDialog.setCancelable(false);
        waitDialog.show();
    }

    public void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new MaterialAlertDialogBuilder(activityRef);
        builder.setTitle(title).setMessage(body).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exit)
                        activityRef.onBackPressed();

                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG, " -- Dialog dismiss failed");

                }
            }
        });
        if(exit)
        builder.setCancelable(false);
        userDialog = builder.create();
        userDialog.show();
    }

    public void showDialogMessage(String title, String body, final UserActionHandler callback) {
        final AlertDialog.Builder builder = new MaterialAlertDialogBuilder(activityRef);
        builder.setTitle(title).setMessage(body).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    callback.onUserAction();

                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG, " -- Dialog dismiss failed");

                }
            }
        });

        builder.setCancelable(false);
        userDialog = builder.create();
        userDialog.show();
    }


    public void showDialogMessage(String title, String body, final Intent intent) {
        final AlertDialog.Builder builder = new MaterialAlertDialogBuilder(activityRef);
        builder.setTitle(title).setMessage(body).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    activityRef.startActivity(intent);
                    activityRef.finish();

                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG, " -- Dialog dismiss failed");

                }
            }
        });
        builder.setCancelable(false);
        userDialog = builder.create();
        userDialog.show();
    }

    public void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        } catch (Exception e) {
            //
        }
    }

//    public void showSingleSelectionDialog(String title,String selection, String[] items, SimpleSelectionHandler callback) {
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityRef);
//        alertDialog.setTitle(title);
//
//        int checkedItem = -1;
//        int index = 0;
//        for(String item : items){
//            if(item.compareToIgnoreCase(selection)==0)
//                checkedItem = index;
//            index++;
//        }
//        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                callback.onSelect(items[which]);
//                userDialog.dismiss();
//            }
//        });
//        userDialog = alertDialog.create();
//        userDialog.setCanceledOnTouchOutside(true);
//        userDialog.show();
//    }

}
