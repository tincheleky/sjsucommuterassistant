package com.tin.sjsucommuterassistant.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.widget.LinearLayout;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.tin.sjsucommuterassistant.R;
import com.tin.sjsucommuterassistant.activities.MainActivity;

/**
 * Created by mbp on 12/9/16.
 */

public class LoginDialog extends Dialog
{
    private GoogleApiClient mGoogleApiClient;

    public LoginDialog(Context context, MainActivity mainActivity) {
        super(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage((MainActivity)context, (MainActivity)context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        setContentView(R.layout.dialog_login);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

    }

}
