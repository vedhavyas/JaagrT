package org.jaagrT.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jaagrT.R;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.listeners.OnItemClickListener;
import org.jaagrT.model.UserContact;

import java.util.List;

/**
 * Authored by vedhavyas on 17/12/14.
 * Project JaagrT
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<UserContact> contacts;
    private OnItemClickListener onItemClickListener;
    private Context context;

    public ContactsAdapter(Context context, List<UserContact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public ContactsAdapter.ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.circle_card_view, viewGroup, false);

        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactsAdapter.ContactViewHolder holder, int position) {
        UserContact contact = contacts.get(position);
        holder.title.setText(contact.getName());
        if (contact.getThumbnailPicture() != null) {
            holder.profilePic.setImageBitmap(contact.getThumbnailPicture());
        } else {
            holder.profilePic.setImageDrawable(Utilities.getRoundedDrawable(context, contact.getName()));
        }
    }

    @Override
    public int getItemCount() {
        if (contacts != null) {
            return contacts.size();
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
