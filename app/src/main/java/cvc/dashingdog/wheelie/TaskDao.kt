package cvc.dashingdog.wheelie

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM tasks ORDER BY createdAt ASC")
    suspend fun getAll(): List<Task>

    @Query("SELECT * FROM tasks ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<Task>>
}