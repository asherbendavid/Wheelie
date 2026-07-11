package cvc.dashingdog.wheelie

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.taskDao()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        dao.insert(Task(text = "Mow the lawn"))
        dao.insert(Task(text = "Pack away shopping"))
        val all = dao.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun deleteById() = runBlocking {
        val id = dao.insert(Task(text = "Fix loose db board cover"))
        dao.deleteById(id)
        val all = dao.getAll()
        assertEquals(0, all.size)
    }

    @Test
    fun getAllOrderedByCreatedAt() = runBlocking {
        dao.insert(Task(text = "First"))
        Thread.sleep(5) // ensure distinct timestamps
        dao.insert(Task(text = "Second"))
        val all = dao.getAll()
        assertEquals("First", all[0].text)
        assertEquals("Second", all[1].text)
    }
}