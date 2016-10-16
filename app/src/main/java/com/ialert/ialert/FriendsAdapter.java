package com.ialert.ialert;

import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import java.util.ArrayList;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * Created by aashimagarg on 10/16/16.
 */

public class FriendsAdapter extends ArrayAdapter<Friend> {
    public FriendsAdapter(Context context, ArrayList<Friend> friends) {
        super(context, 0, friends);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Friend friend = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friend, parent, false);
        }

        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        ImageView ivProfile = (ImageView) convertView.findViewById(R.id.ivProfile);

        tvName.setText(friend.getName());
        Picasso.with(getContext()).load(friend.getImageUrl()).into(ivProfile);

        return convertView;
    }

}
