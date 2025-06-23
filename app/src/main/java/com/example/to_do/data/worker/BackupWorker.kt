package com.example.to_do.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker           // ← Hilt-WorkManager glue
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.to_do.data.local.TodoDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker                                     // Hilt annotation instead of plain class
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,              // ← rename param to “appContext” is fine
    @Assisted params: WorkerParameters,
    private val db: TodoDatabase                // Hilt injects the Room db
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {     // ← ***block body***  (fixes “return” error)
        return try {
            val uid = Firebase.auth.currentUser?.uid ?: return Result.failure()
            val fs  = Firebase.firestore

            // ----- grab every list + its tasks in one shot -----
            db.taskDao()                        // ← was todoDaa / todoDao; correct name
                .getListsWithTasksSync()
                .forEach { lwt ->

                    // ---------- push list ----------
                    fs.collection("users").document(uid)
                        .collection("lists").document(lwt.list.id)
                        .set(lwt.list)                // .set() is unambiguous now
                        .await()

                    // ---------- push each task in that list ----------
                    lwt.tasks.forEach { task ->
                        fs.collection("users").document(uid)
                            .collection("lists").document(lwt.list.id)
                            .collection("tasks").document(task.id)
                            .set(task)
                            .await()
                    }
                }
            Result.success()
        } catch (e: Exception) {                // network / auth / Firestore error
            Result.retry()
        }
    }
}
