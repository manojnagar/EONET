package com.observe.eonet

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.observe.eonet.data.repository.local.AppDatabase
import com.observe.eonet.data.repository.local.model.DBCategory
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.local.model.DBEvent
import com.observe.eonet.data.repository.local.model.DBSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        // Context of the app under test.
        val appContext = ApplicationProvider.getApplicationContext<Context>()
//        db = AppDatabase.getInstance(appContext)
        db = Room.inMemoryDatabaseBuilder(
            appContext, AppDatabase::class.java
        ).build()
    }

    @Test
    fun verifyDBExist() {
        assertNotNull(db)
    }

    @Test
    fun roomDB() {
        val eventDao = db.eventDao()
        assertNotNull(eventDao)
        val categoryDao = db.categoryDao()
        assertNotNull(categoryDao)
    }

    @Test
    fun eventDAO() {
        val eventDao = db.eventDao()
        eventDao.deleteAll()

        var allEvents = eventDao.getAll()
        assertTrue(allEvents.isEmpty())

        val categoryFirst =
            DBCategory(
                "1",
                "Title-1",
                null,
                "link-1"
            )
        val categorySecond =
            DBCategory(
                "2",
                "Title-2",
                null,
                "link-2"
            )
        db.categoryDao().insertAll(categoryFirst, categorySecond)
        eventDao.insertAll(
            DBEvent(
                "1",
                "Title-1",
                null
            ),
            DBEvent(
                "2",
                "Title-2",
                null
            )
        )

        val items = eventDao.getAll()
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
        val categoryDao = db.categoryDao()
        categoryDao.deleteAll()

        var allCategory = categoryDao.getAll()
        assertTrue(allCategory.isEmpty())

        categoryDao.insertAll(
            DBCategory(
                "1",
                "Title-1",
                null,
                "link-1"
            ),
            DBCategory(
                "2",
                "Title-2",
                null,
                "link-2"
            )
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

    @Test
    fun sourceDao() {
        val sourceDao = db.sourceDao()

        //Verify empty
        sourceDao.deleteAll()
        assertTrue(sourceDao.getAll().isEmpty())

        val first = DBSource(
            eventId = "1",
            id = "x",
            link = "Link-x"
        )
        val second = DBSource(
            eventId = "1",
            id = "y",
            link = "Link-y"
        )
        sourceDao.insertAll(first, second)
        assertEquals(2, sourceDao.getAll().size)
        val items = sourceDao.getAll()
        println(items)
        assertTrue(items[0].id != items[1].id)

        sourceDao.deleteAll()
        assertTrue(sourceDao.getAll().isEmpty())
    }

    @Test
    fun eventToSourceMapping() {
        /**
         * DBEvent -> DBSource mapping is 1:N mapping
         */
        val eventDao = db.eventDao()
        val sourceDao = db.sourceDao()

        val E1 = DBEvent(
            id = "1",
            title = "Title-1",
            description = null
        )
        val E2 = DBEvent(
            id = "2",
            title = "Title-2",
            description = null
        )
        val E3 = DBEvent(
            id = "3",
            title = "Title-3",
            description = null
        )

        val S11 = DBSource(
            "1",
            "S11",
            "link-s11"
        )
        val S12 = DBSource(
            "1",
            "S12",
            "link-s12"
        )
        val S13 = DBSource(
            "1",
            "S13",
            "link-s13"
        )
        val S21 = DBSource(
            "2",
            "S21",
            "link-s21"
        )

        //Insert dummy data
        eventDao.insertAll(E1, E2, E3)
        sourceDao.insertAll(S11, S12, S13, S21)

        //Read data
        val result = eventDao.getEventsWithSources()
        result.forEach { println("${it.event}    -->   ${it.sources}") }
        assertEquals(3, result.size)

        //Verify first
        val firstResult = result[0]
        assertEquals("1", firstResult.event.id)
        assertEquals("Title-1", firstResult.event.title)
        assertEquals(3, firstResult.sources.size)

        //Verify second
        val secondResult = result[1]
        assertEquals("2", secondResult.event.id)
        assertEquals("Title-2", secondResult.event.title)
        assertEquals(1, secondResult.sources.size)

        //Verify third
        val thirdResult = result[2]
        assertEquals("3", thirdResult.event.id)
        assertEquals("Title-3", thirdResult.event.title)
        assertTrue(thirdResult.sources.isEmpty())
    }

    @Test
    fun categoryToEventMapping() {
        /**
         * DBCategory <--> DBEvent mapping is N:M mapping
         */
        val categoryDao = db.categoryDao()
        val eventDao = db.eventDao()

        //Generate data
        val C1 = generateCategory("1")
        val C2 = generateCategory("2")
        val C3 = generateCategory("3")

        val E1 = generateEvent("1")
        val E2 = generateEvent("2")
        val E3 = generateEvent("3")

        /**
         * Use case: We have three events and three categories
         *
         * C1 -> E1 and E3
         * C2 -> E1, E2, and E3
         * C3 -> E3
         *
         * E1 -> C1 and C2
         * E2 -> C2
         * E3 -> C1, C2 and C3
         */
        //Insert data
        categoryDao.insertAll(C1, C2, C3)
        eventDao.insertAll(E1, E2, E3)

        val C1Mapping = arrayOf(
            DBCategoryEventCrossRef(
                "1",
                "1"
            ),
            DBCategoryEventCrossRef(
                "1",
                "3"
            )
        )

        val C2Mapping = arrayOf(
            DBCategoryEventCrossRef(
                "2",
                "1"
            ),
            DBCategoryEventCrossRef(
                "2",
                "2"
            ),
            DBCategoryEventCrossRef(
                "2",
                "3"
            )
        )
        val C3Mapping = arrayOf(
            DBCategoryEventCrossRef(
                "3",
                "3"
            )
        )
        categoryDao.insertAllCategoryEventCrossRef(*C1Mapping, *C2Mapping, *C3Mapping)

        //Read categories
        val categories = categoryDao.getCategoryWithEvents()
        assertEquals(3, categories.size)
        categories.forEach { println("${it.category} -> ${it.events}") }
        assertEquals(2, categories[0].events.size)
        assertEquals(3, categories[1].events.size)
        assertEquals(1, categories[2].events.size)

        //Read events
        val events = eventDao.getEventWithCategories()
        assertEquals(3, events.size)
        events.forEach { println("${it.event} -> ${it.categories}") }
        assertEquals(2, events[0].categories.size)
        assertEquals(1, events[1].categories.size)
        assertEquals(3, events[2].categories.size)
    }


    private fun generateCategory(id: String): DBCategory {
        return DBCategory(
            id,
            "Category-Title-$id",
            null,
            "Category-link-$id"
        )
    }

    private fun generateEvent(id: String): DBEvent {
        return DBEvent(
            id,
            "Event-Title- $id",
            "Event-Desc-$id"
        )
    }
}
