package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    lateinit var database: RemindersDatabase
    lateinit var repository: RemindersLocalRepository


    @Before
    fun initRepo() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        repository = RemindersLocalRepository(database.reminderDao())
    }

    @Test
    fun testRepo() = runBlockingTest {
        var item = ReminderDTO("title", "description", "location", 0.0, 0.0)


        runBlocking {
            repository.saveReminder(item)

            var result = repository.getReminder(item.id) as Result.Success

            assertThat(false, `is`(false))

            assertThat(item.id, `is`(result.data.id))
            assertThat(item.description, `is`(result.data.description))
            assertThat(item.title, `is`(result.data.title))
            assertThat(item.location, `is`(result.data.location))
            assertThat(item.latitude, `is`(result.data.latitude))
            assertThat(item.longitude, `is`(result.data.longitude))

            repository.deleteAllReminders()

            assertThat(repository.getReminder("Hamdy") is Result.Error, `is`(true))

            var result1 = repository.getReminders() as Result.Success
            assertThat(result1.data.size, `is`(0))

        }

    }


    @After
    fun closeDatabase() {
        if (::database.isInitialized)
            database.close()
    }

}