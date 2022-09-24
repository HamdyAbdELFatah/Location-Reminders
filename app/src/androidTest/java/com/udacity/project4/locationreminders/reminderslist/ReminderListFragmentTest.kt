package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainAndroidTestCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.hamcrest.CoreMatchers.not
import org.koin.androidx.viewmodel.dsl.viewModel
import com.udacity.project4.locationreminders.data.data.FakeDataSource


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var appContext: Application
    private lateinit var dataSource: FakeDataSource

    @get: Rule
    val mainCoroutineRule = MainAndroidTestCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        dataSource = FakeDataSource()
        // stop the original app koin
        stopKoin()

        appContext = getApplicationContext()
        var testModules = module {
            viewModel {
                RemindersListViewModel(appContext, get() as ReminderDataSource)
            }
            single {
                SaveReminderViewModel(appContext, get() as ReminderDataSource)
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        // Declare a new koin module.
        startKoin {
            modules(listOf(testModules))
        }

        // Clear the data to start fresh
        runBlocking {
            dataSource.deleteAllReminders()
        }
    }

    @After
    fun tearDown() {
        runBlocking{
            dataSource.deleteAllReminders()
        }
        stopKoin()
    }

    private val reminder1 = ReminderDTO(
        "Reminder title 1",
        "Reminder description 1",
        "Reminder location 1",
        24.46017677941061,
        54.42401049833613)

    /**
     * Test the navigation to [SaveReminderFragment].
     */
    @Test
    fun clickAddReminderFAB_navigateToSaveReminderFragment() {
        // GIVEN on the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN click on "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN Verify that we navigate to save reminder screen
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    /**
     * Test the displayed data on the UI.
     */
    @Test
    fun reminderList_displayRemindersDataInUI() {

        // GIVEN a list of reminders
        runBlocking {
            dataSource.saveReminder(reminder1)
        }

        // WHEN on the reminder list screen
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN reminders are displayed on the screen
        onView(withId(R.id.reminderssRecyclerView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(reminder1.title))))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(reminder1.description))))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(reminder1.location))))
    }

    /**
     * Add testing for the error messages.
     */
    @Test
    fun remindersList_displayNoData() {
        // Delete  a reminders list
        runBlocking {
            dataSource.deleteAllReminders()
        }

        // WHEN on the reminder list screen
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN "No Data" is displayed on the screen
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(not(isDisplayed())))
    }
}