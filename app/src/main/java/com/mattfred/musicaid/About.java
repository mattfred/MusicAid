package com.mattfred.musicaid;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by matthewfrederick on 7/23/15.
 */
public class About extends DialogFragment
{
    private Context context;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        context = getActivity().getApplicationContext();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.about, null);
        TextView version = (TextView) mainView.findViewById(R.id.tv_version);

        version.setText(getString(R.string.version) + getAppVersion());

        String title = getString(R.string.action_about);
        String ok = getString(R.string.ok);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(mainView)
                .setPositiveButton(ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dismiss();
                    }
                });

        return builder.create();
    }

    private String getAppVersion()
    {
        try
        {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
}
