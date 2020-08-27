/*
 * Copyright 2013-2017 Amazon.com,
 * Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the
 * License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, express or implied. See the License
 * for the specific language governing permissions and
 * limitations under the License.
 */

package com.myfav.rider.ui.home.adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.myfav.rider.R;
import com.myfav.rider.interfaces.PacketSelectionHandler;
import com.myfav.rider.ui.home.data.PacketInfo;
import com.myfav.rider.ui.home.data.UserSelection;

import java.util.ArrayList;


public class TripAdapter extends RecyclerView.Adapter<TripAdapter.MyViewHolder> {

    //UI

    //Data
    private ArrayList<PacketInfo> mData;
    private Context mContext;
    private PacketSelectionHandler mListener;

    public TripAdapter(Context mCtx, ArrayList<PacketInfo> mData, PacketSelectionHandler mListener) {
        this.mData = mData;
        this.mContext = mCtx;
        this.mListener = mListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == 0 )
            view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.item_trip,parent,false);
        else
            view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.item_trip,parent,false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.packetId.setText (mData.get (position).getId());
        holder.address.setText(mData.get(position).getAddress());
        holder.status.setText(mData.get(position).getStatus());
        holder.surface.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPacketSelected(mData.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.i("getItemCount",""+mData.size ());
        return mData.size ();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView packetId,address,status;
        View surface;
        public MyViewHolder(View itemView) {
            super (itemView);
            packetId = itemView.findViewById(R.id.packetId);
            address = itemView.findViewById(R.id.address);
            status = itemView.findViewById(R.id.status);
            surface = itemView;
        }
    }
}
