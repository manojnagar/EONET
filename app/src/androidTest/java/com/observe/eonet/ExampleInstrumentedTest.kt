package com.observe.eonet

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.observe.eonet.data.model.db.DBCategory
import com.observe.eonet.data.model.db.DBEvent
import com.observe.eonet.data.repository.local.AppDatabase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.observe.eonet", appContext.packageName)
    }

    @Test
    fun roomDB() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getInstance(appContext)
        assertNotNull(db)
        val eventDao = db.eventDao()
        assertNotNull(eventDao)
        val categoryDao = db.categoryDao()
        assertNotNull(categoryDao)
    }

    @Test
    fun eventDAO() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getInstance(appContext)
        val eventDao = db.eventDao()
        eventDao.deleteAll()

        var allEvents = eventDao.getAll()
        assertTrue(allEvents.isEmpty())

        val categoryFirst = DBCategory("1", "Title-1", null, "link-1")
        val categorySecond = DBCategory("2", "Title-2", null, "link-2")
        db.categoryDao().insertAll(categoryFirst, categorySecond)
        eventDao.insertAll(
            DBEvent("1", "1", "Title-1", null),
            DBEvent("2", "2", "Title-2", null)
        )

        val items = eventDao.getEventsAndCategory()
        println(items)
        allEvents = eventDao.getAll()
        assertEquals(2, allEvents.size)

        eventDao.delete(allEvents[0])
        allEvents = eventDao.getAll()
        assertEquals(1, allEvents.size)

        eventDao.deleteAll()
        allEvents = eventDao.getAll()
        assertTrue(allEvents.isEmpty())
    }

    @Test
    fun categoryDAO() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getInstance(appContext)
        val categoryDao = db.categoryDao()
        categoryDao.deleteAll()

        var allCategory = categoryDao.getAll()
        assertTrue(allCategory.isEmpty())

        categoryDao.insertAll(
            DBCategory("1", "Title-1", null, "link-1"),
            DBCategory("2", "Title-2", null, "link-2")
        )
        allCategory = categoryDao.getAll()
        assertEquals(2, allCategory.size)

        categoryDao.delete(allCategory[0])
        allCategory = categoryDao.getAll()
        assertEquals(1, allCategory.size)

        categoryDao.deleteAll()
        allCategory = categoryDao.getAll()
        assertTrue(allCategory.isEmpty())
    }


}
