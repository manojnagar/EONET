package com.observe.eonet.firebase

import com.google.firebase.messaging.FirebaseMessagingService

class FCMService : FirebaseMessagingService() {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(newToken: String) {
        println("OnNewToken $newToken")
    }
}