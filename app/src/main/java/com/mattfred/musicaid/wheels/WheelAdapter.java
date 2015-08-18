package com.mattfred.musicaid.wheels;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mattfred.musicaid.R;

import kankan.wheel.widget.adapters.AbstractWheelAdapter;

/**
 * Created by matthewfrederick on 7/24/15.
 */
public class WheelAdapter extends AbstractWheelAdapter
{
    private final Context context;
    private final String[] keys;

    public WheelAdapter(Context context)
    {
        this.context = context;

        keys = context.getResources().getStringArray(R.array.keys_array);
    }

    @Override
    public int getItemsCount()
    {
        return keys.length;
    }

    @Override
    public View getItem(int i, View view, ViewGroup viewGroup)
    {
        TextView text;
        if (view != null)
        {
            text = (TextView) view;
        }
        else
        {
            text = new TextView(context);
        }
        text.setText(keys[i]);
        return text;
    }
}
