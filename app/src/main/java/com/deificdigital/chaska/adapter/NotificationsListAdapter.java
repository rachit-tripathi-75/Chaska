package com.deificdigital.chaska.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.raccoonsquare.dating.ProfileActivity;
import com.deificdigital.chaska.R;
import com.deificdigital.chaska.constants.Constants;
import com.deificdigital.chaska.model.Chat;
import com.deificdigital.chaska.model.Notify;

import java.util.List;
import java.util.Locale;

import github.ankushsachdevaemojicon.*;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.ViewHolder> implements Constants {

    private Context ctx;
    private List<Notify> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Notify item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, time;
        public CircularImageView image, online, verified, icon;
        public LinearLayout parent;
        public EmojiconTextView message;

        public ViewHolder(View view) {

            super(view);

            title = view.findViewById(R.id.title);
            message = view.findViewById(R.id.message);
            time = view.findViewById(R.id.time);
            image = view.findViewById(R.id.image);
            parent = view.findViewById(R.id.parent);

            online = view.findViewById(R.id.online);
            verified = view.findViewById(R.id.verified);
            icon = view.findViewById(R.id.icon);
        }
    }

    public NotificationsListAdapter(Context mContext, List<Notify> items) {

        this.ctx = mContext;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Notify item = items.get(position);

        holder.online.setVisibility(View.GONE);
        holder.verified.setVisibility(View.GONE);

        if (item.getFromUserPhotoUrl().length() > 0 && item.getFromUserId() != 0) {

            final ImageView img = holder.image;

            try {

                Glide.with(ctx)
                        .load(item.getFromUserPhotoUrl())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                img.setImageResource(R.drawable.profile_default_photo);
                                img.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                img.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(holder.image);

            } catch (Exception e) {

                Log.e("NotifyListAdapter", e.toString());
            }

        } else {

            holder.image.setImageResource(R.drawable.profile_default_photo);
        }

        if (item.getFromUserId() != 0) {

            holder.title.setText(item.getFromUserFullname());

        } else {

            holder.image.setImageResource(R.drawable.def_photo);
            holder.title.setText(ctx.getString(R.string.app_name));
        }

        if (item.getType() == NOTIFY_TYPE_LIKE) {

            holder.message.setText(item.getFromUserFullname() + " " + ctx.getText(R.string.label_likes_profile));
            holder.icon.setImageResource(R.drawable.notify_like);

         } else if (item.getType() == NOTIFY_TYPE_COMMENT) {

            holder.message.setText(item.getFromUserFullname() + " " + ctx.getText(R.string.label_comment_added));
            holder.icon.setImageResource(R.drawable.notify_comment);

        } else if (item.getType() == NOTIFY_TYPE_COMMENT_REPLY) {

            holder.message.setText(item.getFromUserFullname() + " " + ctx.getText(R.string.label_comment_reply_added));
            holder.icon.setImageResource(R.drawable.notify_reply);

        } else if (item.getType() == NOTIFY_TYPE_GIFT) {

            holder.message.setText(item.getFromUserFullname() + " " + ctx.getText(R.string.label_gift_added));
            holder.icon.setImageResource(R.drawable.notify_gift);

        } else if (item.getType() == NOTIFY_TYPE_IMAGE_COMMENT) {

            holder.message.setText(item.getFromUserFullname() + " " + ctx.getText(R.string.label_comment_added));
            holder.icon.setImageResource(R.drawable.notify_comment);

        } else if (item.getType() == NOTIFY_TYPE_IMAGE_COMMENT_REPLY) {

            holder.message.setText(item.getFromUserFullname() + " " + ctx.getText(R.string.label_comment_reply_added));
            holder.icon.setImageResource(R.drawable.notify_comment);

        } else if (item.getType() == NOTIFY_TYPE_IMAGE_LIKE) {

            holder.message.setText(item.getFromUserFullname() + " " + ctx.getText(R.string.label_likes_item));
            holder.icon.setImageResource(R.drawable.notify_like);

        } else if (item.getType() == NOTIFY_TYPE_MEDIA_APPROVE) {

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_media_approved), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_approved);

        } else if (item.getType() == NOTIFY_TYPE_MEDIA_REJECT) {

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_media_rejected), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_rejected);

        } else if (item.getType() == NOTIFY_TYPE_ACCOUNT_APPROVE) {

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_approved_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_approved);

        } else if (item.getType() == NOTIFY_TYPE_ACCOUNT_REJECT) {

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_rejected_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_rejected);

        } else if (item.getType() == NOTIFY_TYPE_PROFILE_PHOTO_APPROVE) {

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_approved_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_approved);

        } else if (item.getType() == NOTIFY_TYPE_PROFILE_PHOTO_REJECT) {

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_rejected_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_rejected);

        } else if (item.getType() == NOTIFY_TYPE_PROFILE_COVER_APPROVE) {

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_cover_approved_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_approved);

        } else if (item.getType() == NOTIFY_TYPE_PROFILE_COVER_REJECT) {

            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_cover_rejected_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.notify_rejected);

        } else {

            holder.message.setText(item.getFromUserFullname() + " " + ctx.getText(R.string.label_friend_request_added));
            holder.icon.setImageResource(R.drawable.notify_follower);
        }

        holder.time.setText(item.getTimeAgo());

        holder.parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(v, items.get(position), position);
                }
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Notify item = items.get(position);

                if (item.getFromUserId() != 0) {

                    Intent intent = new Intent(ctx, ProfileActivity.class);
                    intent.putExtra("profileId", item.getFromUserId());
                    ctx.startActivity(intent);
                }
            }
        });
    }

    public Notify getItem(int position) {

        return items.get(position);
    }

    @Override
    public int getItemCount() {

        return items.size();
    }

    public interface OnClickListener {

        void onItemClick(View view, Chat item, int pos);
    }
}