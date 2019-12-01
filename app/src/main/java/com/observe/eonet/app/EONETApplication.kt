package com.observe.eonet.app

import androidx.multidex.MultiDexApplication
import com.observe.eonet.data.repository.DataRepository
import com.observe.eonet.data.repository.DataSource
import com.observe.eonet.util.schedulers.BaseSchedulerProvider
import com.observe.eonet.util.schedulers.SchedulerProvider

class EONETApplication : MultiDexApplication() {

    companion object {
        lateinit var dataSource: DataSource
        lateinit var schedulerProvider: BaseSchedulerProvider
    }
    override fun onCreate() {
        super.onCreate()


        dataSource = DataRepository()
        schedulerProvider = SchedulerProvider
    }
}