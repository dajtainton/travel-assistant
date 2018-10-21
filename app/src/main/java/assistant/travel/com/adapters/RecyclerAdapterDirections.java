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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import assistant.travel.com.R;

/**
 * RecyclerView Adapter for list that displays the directions in the
 * bottom sheet in MainActivity
 */

public class RecyclerAdapterDirections extends RecyclerView.Adapter<RecyclerAdapterDirections.ViewHolder> {

    private List<String> mInstructions;
    private List<String> mDistances;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    public RecyclerAdapterDirections(Context context, List<String> instructions, List<String> distances) {
        mInflater = LayoutInflater.from(context);
        mInstructions = instructions;
        mDistances = distances;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_row_directions, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.instructions.setText(Html.fromHtml(mInstructions.get(position)));
        holder.distance.setText(String.format("Continue %s", Html.fromHtml(mDistances.get(position))));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mInstructions.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView instructions;
        TextView distance;

        ViewHolder(View itemView) {
            super(itemView);
            instructions = itemView.findViewById(R.id.html_instructions);
            distance = itemView.findViewById(R.id.distance);
        }

    }

}
