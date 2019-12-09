package com.observe.eonet.app

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.observe.eonet.data.repository.DataRepository
import com.observe.eonet.data.repository.DataSource
import com.observe.eonet.util.schedulers.BaseSchedulerProvider
import com.observe.eonet.util.schedulers.SchedulerProvider

class EONETApplication : MultiDexApplication() {

    companion object {
        private final val TAG = "EOApp"
        lateinit var dataSource: DataSource
        lateinit var schedulerProvider: BaseSchedulerProvider
    }
    override fun onCreate() {
        super.onCreate()


        dataSource = DataRepository()
        schedulerProvider = SchedulerProvider

        registerFCMToken()
    }

    private fun registerFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                Log.d(TAG, "Token : $token")
            })

    }
}