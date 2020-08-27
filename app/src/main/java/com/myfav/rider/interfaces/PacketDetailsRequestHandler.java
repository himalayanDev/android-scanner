package com.myfav.rider.interfaces;

import com.myfav.rider.ui.home.data.PacketInfo;

import org.json.JSONObject;

import java.util.ArrayList;

public interface PacketDetailsRequestHandler {
    public void onResult(JSONObject result);
    public void onError(String message);
}
