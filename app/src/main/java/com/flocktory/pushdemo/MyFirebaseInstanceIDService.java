package com.flocktory.pushdemo;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        MainActivity.log("Refreshed token: " + refreshedToken);

        FlocktoryApiClient flApi = new FlocktoryApiClient(getApplicationContext());

        flApi.regToken(refreshedToken);
    }

}
