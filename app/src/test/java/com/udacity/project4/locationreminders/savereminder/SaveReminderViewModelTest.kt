package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.pauseDispatcher
import org.junit.Assert.assertFalse

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // Initialise the data source with no reminders.
        dataSource = FakeDataSource()
        // Initialize the view model
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource
        )
    }

    @After
    fun stopDown() {
        GlobalContext.stop()
    }

    @Test
    fun saveReminderTest() = runBlocking {
        // GIVEN a reminder item
        val reminder = ReminderDataItem(
            "title",
            "description",
            "location",
            latitude = 47.5456551,
            longitude = 122.0101731
        )
        // WHEN request save reminder from view model
        viewModel.saveReminder(reminder)

        //Assert validation when all data available  Toast Shows "Reminder Saved !"
        assertEquals("Reminder Saved !", viewModel.showToast.getOrAwaitValue())
    }

    @Test
    fun validateAndSaveReminderTest_valid_returnTrue() = runBlocking {
        // GIVEN a reminder item
        val reminder = ReminderDataItem(
            "title",
            "description",
            "location",
            latitude = 47.5456551,
            longitude = 122.0101731
        )
        // WHEN request save reminder from view model
        val result = viewModel.validateEnteredData(reminder)

        //Assert validation when all data available return true
        assertTrue(result)
    }


    @Test
    fun validateAndSaveReminderTest_emptyTitle_returnFalse() = runBlocking {
        // GIVEN a reminder item without title
        val reminder = ReminderDataItem(
            "",
            "description",
            "location",
            latitude = 47.5456551,
            longitude = 122.0101731
        )
        // WHEN validate reminder data
        val result = viewModel.validateEnteredData(reminder)

        //Assert validation when no title return false
        assertFalse(result)
    }

    @Test
    fun validateAndSaveReminderTest_emptyLocation_returnFalse() = runBlocking {
        // GIVEN a reminder item without location
        val reminder = ReminderDataItem(
            "title",
            "description",
            "",
            latitude = 47.5456551,
            longitude = 122.0101731
        )
        // WHEN validate reminder data
        val result = viewModel.validateEnteredData(reminder)

        //Assert validation when no location return false
        assertFalse(result)
    }

    @Test
    fun validateAndSaveReminderTest_emptyTitleAndLocation_returnFalse() = runBlocking {
        // GIVEN a reminder item without title and location
        val reminder = ReminderDataItem(
            "",
            "description",
            "",
            latitude = 47.5456551,
            longitude = 122.0101731
        )
        // WHEN validate reminder data
        val result = viewModel.validateEnteredData(reminder)

        //Assert validation when no title & no location return false
        assertFalse(result)
    }

    @Test
    fun checkLoading() {
        mainCoroutineRule.pauseDispatcher()

        // GIVEN a reminder item
        val reminder = ReminderDataItem(
            "",
            "description",
            "",
            latitude = 47.5456551,
            longitude = 122.0101731
        )

        // WHEN request save reminder from view model
        viewModel.saveReminder(reminder)

        //Assert showLoading is true
        assertTrue(viewModel.showLoading.getOrAwaitValue())
    }

}