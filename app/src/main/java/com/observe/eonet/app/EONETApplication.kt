package com.observe.eonet.app

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.observe.eonet.Injection
import com.observe.eonet.data.repository.CategoryRepository
import com.observe.eonet.data.repository.DataRepository
import com.observe.eonet.data.repository.DataSource
import com.observe.eonet.data.repository.EventRepository
import com.observe.eonet.data.repository.local.AppDatabase
import com.observe.eonet.data.repository.remote.RemoteDataSource
import com.observe.eonet.util.schedulers.BaseSchedulerProvider
import com.observe.eonet.util.schedulers.SchedulerProvider

class EONETApplication : MultiDexApplication() {

    companion object {
        private val TAG = "EOApp"
        lateinit var dataSource: DataSource
        lateinit var schedulerProvider: BaseSchedulerProvider
        lateinit var appDatabase: AppDatabase
        lateinit var categoryRepository: CategoryRepository
        lateinit var eventRepository: EventRepository
        private val remoteDataSource: DataSource by lazy {
            RemoteDataSource()
        }
    }
    override fun onCreate() {
        super.onCreate()

        dataSource = DataRepository()
        schedulerProvider = SchedulerProvider
        appDatabase = AppDatabase.getInstance(applicationContext)
        categoryRepository = Injection.provideCategoryRepository(this)
        eventRepository = EventRepository(
            appDatabase.eventDao(),
            remoteDataSource
        )
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