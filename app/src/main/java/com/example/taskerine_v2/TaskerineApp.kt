package com.example.taskerine_v2

import android.app.Application
import com.example.taskerine_v2.data.local.TaskerineDatabase
import com.example.taskerine_v2.data.repository.TaskerineRepository

class TaskerineApp : Application() {

    val database by lazy { TaskerineDatabase.getInstance(this) }
    val repository by lazy { TaskerineRepository(database) }
}

