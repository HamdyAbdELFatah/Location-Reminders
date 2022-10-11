package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel
    // Use a fake data source to be injected into the view model.
    private lateinit var dataSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    //Initialize dataSource & viewModel
    @Before
    fun setup(){
        // Initialise the data source with no reminders.
        dataSource = FakeDataSource()
        // Initialize the view model
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)
    }

    @After
    fun teardown(){
        stopKoin()
    }


    @Test
    fun loadRemindersTest_withEmptyList_returnTrue(){

        // WHEN load reminders from data source
        viewModel.loadReminders()

        // Assert remindersList is empty & return true
        assertEquals(emptyList<ReminderDataItem>(),viewModel.remindersList.getOrAwaitValue())
    }


    @Test
    fun loadRemindersTest_withThreeReminders_returnFalse(){

        // GIVEN 3 reminders to reminders list

        val reminder1 = ReminderDTO("T1","hello 1","location 1",
            30.254191579122224, 31.455338343915514)

        val reminder2 = ReminderDTO("T2","hello 2","location 2",
            30.256518719233206, 31.50189038390654)

        val reminder3 = ReminderDTO("T3","hello 3","location 3",
            30.17187552186255, 31.492371551805117 )

        val list = mutableListOf<ReminderDTO>(reminder1,reminder2,reminder3)
        dataSource = FakeDataSource(list)
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)


        // WHEN load reminders from data source
        viewModel.loadReminders()

        // Assert that remindersList is Not empty after add three reminders to list
        assertFalse(emptyList<ReminderDataItem>() == viewModel.remindersList.getOrAwaitValue())

    }


    @Test
    fun checkLoading() {
        mainCoroutineRule.pauseDispatcher()

        // WHEN load reminders from data source
        viewModel.loadReminders()

        // Assert showLoading has data value
        assertTrue(viewModel.showLoading.getOrAwaitValue())

        mainCoroutineRule.resumeDispatcher()

        //check showLoading has no data
        assertFalse(viewModel.showLoading.getOrAwaitValue())
    }


    @Test
    fun checkError() {
        // test application reacts appropriately when there's an error
        // (such as data being unavailable).
        // a boolean flag called setReturnError and set it initially to false which means that by default an error is not returned.

        // setting the error flag to true
        dataSource.setError(true)

        // WHEN request reminders from data source
        viewModel.loadReminders()

        // Assert showSnackbar value is Returning data is not found
        assertEquals("Tasks not found",viewModel.showSnackBar.getOrAwaitValue())
    }
}