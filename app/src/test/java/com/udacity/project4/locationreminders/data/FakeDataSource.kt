package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf<ReminderDTO>()) :
    ReminderDataSource {

    var returnError = false

    fun setError(value: Boolean) {
        returnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (!returnError) {
            reminders?.let {
                return Result.Success(ArrayList(it))
            }
        }
        return Result.Error("Tasks not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (!returnError) {
            for (i in reminders!!) {
                if (i.id == id)
                    return Result.Success(i)
            }
        }
        return Result.Error("not Found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}