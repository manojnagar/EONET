package com.observe.eonet

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.observe.eonet.ui.category.CategoriesAction
import com.observe.eonet.ui.category.CategoriesProcessorHolder
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class CategoriesProcessorHolderTest {

    @Test
    fun loadActionToResult() {
        val latch = CountDownLatch(1)
        Observable.just(CategoriesAction.LoadCategoriesAction)
            .compose(CategoriesProcessorHolder().actionProcessor)
            .subscribeBy(
                onNext = {
                    println("Print Output: $it")
                },
                onComplete = {
                    println("Print OnComplete")
                    latch.countDown()
                },
                onError = {
                    println("Error to subscriber: $it")
                }
            )
        latch.await()
        runBlocking { delay(1000) }
    }
}