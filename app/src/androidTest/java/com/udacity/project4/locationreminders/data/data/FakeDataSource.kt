package com.udacity.project4.locationreminders.data.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(var reminders : MutableList<ReminderDTO> = mutableListOf() ):ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if (reminders.isEmpty()) {
            return Result.Success(reminders)
        } else {
            return Result.Success(reminders)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // save reminders
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //get reminder with id

        // if reminder found with this id
        val result = reminders.firstOrNull{it.id == id}
        result?.let {
            return Result.Success(it)
        }

        // if reminder not found with this id
        return Result.Error("This Reminder not available")

    }

    override suspend fun deleteAllReminders() {
        //delete all reminders
        reminders.clear()
    }
}