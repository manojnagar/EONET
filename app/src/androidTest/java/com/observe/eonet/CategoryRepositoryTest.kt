package com.observe.eonet

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.observe.eonet.data.repository.CategoryRepository
import com.observe.eonet.data.repository.local.AppDatabase
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class CategoryRepositoryTest {

    private lateinit var repository: CategoryRepository
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        // Context of the app under test.
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        repository = Injection.provideCategoryRepository(appContext)
//        db = Room.databaseBuilder(
//            appContext, AppDatabase::class.java,
//            "app_database"
//        ).build()
//            .allowMainThreadQueries() // allowing main thread queries, just for testing
//        val db = Room.inMemoryDatabaseBuilder(
//            appContext, AppDatabase::class.java
//        ).build()
        /*            .test()
            .assertValue { category: EOCategory ->
                true
            }*/
    }

    @After
    @Throws(Exception::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun fetchCategory() {
        val latch = CountDownLatch(1)
        repository.getCategory("6555")
            .subscribeBy(
                onNext = {
                    println("Output: $it")
                },
                onComplete = {
                    println("OnComplete")
                    latch.countDown()
                },
                onError = {
                    println("Error to subscriber: $it")
                    val exception = it as HttpException
                    println(exception)
                    println(exception.code())
                    println(exception.message())
                    println(exception.response())
                    latch.countDown()
                }
            )
        latch.await()

        runBlocking { delay(1000) }
    }
}