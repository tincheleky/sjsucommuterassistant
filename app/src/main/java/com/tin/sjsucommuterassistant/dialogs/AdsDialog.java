package com.tin.sjsucommuterassistant.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.maps.model.LatLng;
import com.tin.sjsucommuterassistant.R;
import com.tin.sjsucommuterassistant.activities.MainActivity;


/**
 * Created by mbp on 12/10/16.
 */

public class AdsDialog extends Dialog {

    private Context context;
    private Dialog thisPtr;
    public AdsDialog(Context context, LatLng latLng)
    {
        super(context);
        this.context = context;
        thisPtr = this;
        setContentView(R.layout.dialog_ads);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView tvNope = (TextView)findViewById(R.id.dialog_noope);
        TextView tvViewAds = (TextView)findViewById(R.id.dialog_view_ads);
        onClickToDismiss clickToDismiss = new onClickToDismiss();
        tvNope.setOnClickListener(clickToDismiss);
        tvViewAds.setOnClickListener(clickToDismiss);

    }

    private class onClickToDismiss implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            thisPtr.dismiss();
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(MainActivity.SJSU_LAT) + "," + String.valueOf(MainActivity.SJSU_LONG));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);

        }
    }



}
