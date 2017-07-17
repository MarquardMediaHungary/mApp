package com.onceapps.m.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.m.api.RestClient;
import com.onceapps.m.utils.Preferences;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

/**
 * MFirebaseInstanceIDService
 * Created by szipe on 21/06/16.
 */

@EService
public class MFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Bean
    protected Preferences mPreferences;

    @Bean
    protected RestClient mRestClient;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        if (mPreferences.app.authToken().exists() && refreshedToken != null) {
            try {
                mRestClient.sendDeviceToken(refreshedToken);
            } catch (Exception e) {
                if (!(e instanceof OfflineException))
                    Logger.error(e, "error sending device token");
            }
        } else {
            Logger.debug("user not yet logged in, sending device token after succesful login");
        }

    }

}
