package org.jaagrT.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jaagrT.R;
import org.jaagrT.helpers.BitmapHolder;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.listeners.BitmapGetListener;
import org.jaagrT.listeners.OnItemClickListener;
import org.jaagrT.model.User;

import java.util.List;

/**
 * Authored by vedhavyas on 17/12/14.
 * Project JaagrT
 */

public class CirclesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> circles;
    private OnItemClickListener onItemClickListener;
    private Context context;
    private BitmapHolder bitmapHolder;

    public CirclesAdapter(Context context, List<User> circles) {
        this.context = context;
        this.circles = circles;
        this.bitmapHolder = BitmapHolder.getInstance(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == -1) {
            View emptyView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.empty_circles_view, viewGroup, false);

            return new EmptyViewHolder(emptyView);

        } else {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.circle_card_view, viewGroup, false);
            return new CirclesViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CirclesViewHolder) {
            final CirclesViewHolder holder = (CirclesViewHolder) viewHolder;
            User circle = circles.get(position);
            String name = circle.getFirstName();
            if (circle.getLastName() != null) {
                name += " " + circle.getLastName();
            }
            holder.title.setText(name);
            holder.profilePic.setImageDrawable(Utilities.getRoundedDrawable(context, name));
            bitmapHolder.getBitmapThumbAsync(circle.getEmail(), new BitmapGetListener() {
                @Override
                public void onGet(Bitmap bitmap) {
                    if (bitmap != null) {
                        holder.profilePic.setImageBitmap(Utilities.getRoundedBitmap(bitmap));
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return circles.size() > 0 ? circles.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (circles.size() == 0) {
            return -1;
        }
        return super.getItemViewType(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public List<User> getCircles() {
        return this.circles;
    }

    public void setCircles(List<User> circles) {
        this.circles.clear();
        this.circles.addAll(circles);
        notifyDataSetChanged();
    }

    public User getCircle(int position) {
        return circles.get(position);
    }

    public void removeCircle(int position) {
        this.circles.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void addCircle(User circle) {
        int position = circles.size();
        circles.add(position, circle);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    public class CirclesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView profilePic;
        protected TextView title;

        public CirclesViewHolder(View v) {
            super(v);
            profilePic = (ImageView) v.findViewById(R.id.profilePic);
            title = (TextView) v.findViewById(R.id.title);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, circles.get(getPosition()).getID());
            }
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

}
