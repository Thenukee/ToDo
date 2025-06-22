package com.example.to_do.data.worker

import android.content.Context
import androidx.work.*
import com.example.to_do.data.local.TodoDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: TodoDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val uid = Firebase.auth.currentUser?.uid ?: return Result.failure()
        val fs = Firebase.firestore

        db.todoDao().getListsWithTasksSync().forEach { lwt ->
            fs.collection("users").document(uid)
                .collection("lists").document(lwt.list.id)
                .set(lwt.list).await()

            lwt.tasks.forEach { task ->
                fs.collection("users").document(uid)
                    .collection("lists").document(lwt.list.id)
                    .collection("tasks").document(task.id)
                    .set(task).await()
            }
        }
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
