/*
 * Copyright 2018 David Tainton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package assistant.travel.com.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import assistant.travel.com.R;
import assistant.travel.com.database.Trip;

/**
 * Adapter for the Trips Recycler View
 */

public class RecyclerAdapterTrips extends RecyclerView.Adapter<RecyclerAdapterTrips.MyViewHolder> {

    private ArrayList<Trip> data;

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTransportMode;
        private TextView mDate;
        private TextView mDistance;
        private TextView mTime;

        MyViewHolder(View itemView) {
            super(itemView);

            mTransportMode = itemView.findViewById(R.id.transport_mode);
            mDate = itemView.findViewById(R.id.date);
            mDistance = itemView.findViewById(R.id.distance);
            mTime = itemView.findViewById(R.id.time_taken);
        }
    }

    public RecyclerAdapterTrips(ArrayList<Trip> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_trips, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.mDistance.setText(String.format("Trip Distance: %s m", data.get(position).getDistance()));
        holder.mTime.setText(String.format("Time Taken: %s seconds", data.get(position).getTimeTaken()));
        holder.mDate.setText(String.format("Trip Timestamp: %s", data.get(position).getStartDateTime()));
        holder.mTransportMode.setText(String.format("Transport Mode: %s", data.get(position).getTransportMode()));
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    public void removeItem(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Trip item, int position) {
        data.add(position, item);
        notifyItemInserted(position);
    }

    public ArrayList<Trip> getData() {
        return data;
    }

}
