package com.shady.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.shady.fehlstunden.R;
import com.shady.fehlstunden.SeminarView;
import com.shady.logic.Constants;
import com.shady.logic.Seminar;

public class SeminarAdapter extends RecyclerView.Adapter<SeminarAdapter.ViewHolder> {
    private ArrayList<Seminar> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
        public TextView txtHeader;
        public TextView txtFooter;
        public ISeminarViewHolderClicks mListener;

        public ViewHolder(View v, ISeminarViewHolderClicks listener) {
            super(v);
            mListener = listener;
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);

            txtHeader.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
                if(v instanceof TextView){
                    mListener.onClickSeminar(v);
                }
        }

        public static interface ISeminarViewHolderClicks {
            public void onClickSeminar(View caller);
        }
    }

    public void add(int position, Seminar item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Seminar item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SeminarAdapter(List<Seminar> myDataset) {
        mDataset = (ArrayList<Seminar>) myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SeminarAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seminar, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v, new ViewHolder.ISeminarViewHolderClicks() {
            @Override
            public void onClickSeminar(View caller) {
                Toast.makeText(caller.getContext(), "Clicked RV item", Toast.LENGTH_LONG).show();
            }
        });

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String name = ((Seminar) mDataset.get(position)).getName();
        final String missed = ((Seminar) mDataset.get(position)).getMissed() + "";
        final String allowedToMiss = ((Seminar) mDataset.get(position)).getAllowedToMiss()+ "";

        holder.txtHeader.setText(name);
        holder.txtFooter.setText(missed + " / "+ allowedToMiss);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}