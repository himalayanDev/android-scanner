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
import com.myfav.rider.ui.home.data.UserSelection;

import java.util.ArrayList;


public class UserSelectionActionAdapter extends RecyclerView.Adapter<UserSelectionActionAdapter.MyViewHolder> {

    //UI

    //Data
    private ArrayList<UserSelection> mData;
    private Context mContext;
    private UserSelectionListener mListener;

    public UserSelectionActionAdapter(Context mCtx, ArrayList<UserSelection> mData, UserSelectionListener mListener) {
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
            view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.item_user_selection,parent,false);
        else
            view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.item_user_selection,parent,false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.title.setText (mData.get (position).getTitle());
        holder.surface.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onUserSelection(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.i("getItemCount",""+mData.size ());
        return mData.size ();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        RelativeLayout surface;
        public MyViewHolder(View itemView) {
            super (itemView);
            title = itemView.findViewById(R.id.title);
            surface = itemView.findViewById(R.id.surface);
        }
    }

    public interface UserSelectionListener {
        void onUserSelection(int position);
    }
}
