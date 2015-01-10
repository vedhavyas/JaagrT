package org.jaagrT.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jaagrT.R;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.listeners.OnItemClickListener;
import org.jaagrT.model.User;

import java.util.List;

/**
 * Authored by vedhavyas on 17/12/14.
 * Project JaagrT
 */

public class CirclesAdapter extends RecyclerView.Adapter<CirclesAdapter.ContactViewHolder> {

    private List<User> circles;
    private OnItemClickListener onItemClickListener;

    public CirclesAdapter(List<User> circles) {
        this.circles = circles;
    }

    @Override
    public CirclesAdapter.ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.circle_card_view, viewGroup, false);

        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CirclesAdapter.ContactViewHolder holder, int position) {
        User contact = circles.get(position);
        holder.title.setText(contact.getFirstName());
        if (contact.getThumbnailPicture() != null) {
            holder.profilePic.setImageBitmap(contact.getThumbnailPicture());
        } else {
            holder.profilePic.setImageDrawable(Utilities.getRoundedDrawable(contact.getFirstName()));
        }
    }

    @Override
    public int getItemCount() {
        if (circles != null) {
            return circles.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView profilePic;
        protected TextView title;

        public ContactViewHolder(View v) {
            super(v);
            profilePic = (ImageView) v.findViewById(R.id.profilePic);
            title = (TextView) v.findViewById(R.id.title);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, getPosition());
            }
        }
    }

}
