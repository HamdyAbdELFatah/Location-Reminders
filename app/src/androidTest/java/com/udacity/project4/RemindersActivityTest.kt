package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }



    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val backgroundPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun reminderActivity_addReminder_endToEnd() {

        // Start up reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Verify No Data is displayed
        Espresso.onView(ViewMatchers.withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Click on add reminder
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Type in title and description
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Reminder title"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Reminder description"), ViewActions.closeSoftKeyboard())

        // Click on select location
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
        runBlocking { delay(1000) }


        // Verify map is displayed
        Espresso.onView(ViewMatchers.withId(R.id.map))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // long click on map
        Espresso.onView(ViewMatchers.withId(R.id.map)).perform(ViewActions.click())
        runBlocking { delay(1000) }

        // Click on save button
        Espresso.onView(ViewMatchers.withId(R.id.save)).perform(ViewActions.click())
        runBlocking { delay(1000) }

        // Click on save reminder button
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // Verify reminder is displayed on screen in the task list.
        Espresso.onView(ViewMatchers.withText("Reminder title"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("Reminder description"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Verify No data is not displayed
        Espresso.onView(ViewMatchers.withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }



    //Espresso tests for Toast messages
    @Test
    fun saveReminderScreen_showToastMessage() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(ViewMatchers.withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("title"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("description"))
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.map)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.save)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                CoreMatchers.not(CoreMatchers.`is`(getActivity(activityScenario).window.decorView)))
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    //A Snackbar message is supposed to be displayed when the reminder title is empty (save reminder screen).
    @Test
    fun saveReminderScreen_showSnackBarTitleError() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        val snackBarMessage = appContext.getString(R.string.err_enter_title)
        Espresso.onView(ViewMatchers.withText(snackBarMessage))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }
    //A Snackbar message is supposed to be displayed when the reminder location is empty (save reminder screen).
    @Test
    fun saveReminderScreen_showSnackBarLocationError() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        val snackBarMessage = appContext.getString(R.string.err_select_location)
        Espresso.onView(ViewMatchers.withText(snackBarMessage))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }
}
