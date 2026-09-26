package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    @Query("SELECT * FROM launcher_items ORDER BY pageIndex ASC, cellY ASC, cellX ASC")
    fun getAllItems(): Flow<List<LauncherItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LauncherItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LauncherItemEntity>)

    @Update
    suspend fun updateItem(item: LauncherItemEntity)

    @Query("DELETE FROM launcher_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM launcher_items WHERE folderId = :folderId")
    suspend fun deleteItemsInFolder(folderId: Long)

    @Query("DELETE FROM launcher_items")
    suspend fun clearAllItems()

    @Query("SELECT * FROM folders ORDER BY id ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>): List<Long>

    @Query("DELETE FROM folders")
    suspend fun clearAllFolders()

    @Query("SELECT * FROM app_overrides")
    fun getAllOverrides(): Flow<List<AppOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: AppOverrideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverrides(overrides: List<AppOverrideEntity>)

    @Query("DELETE FROM app_overrides WHERE packageName = :packageName")
    suspend fun deleteOverride(packageName: String)

    @Query("DELETE FROM app_overrides")
    suspend fun clearAllOverrides()

    @Query("SELECT * FROM launcher_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<LauncherSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: LauncherSettingsEntity)

    @Query("SELECT * FROM quick_notes WHERE id = 1 LIMIT 1")
    fun getQuickNote(): Flow<QuickNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickNote(note: QuickNoteEntity)

    @Query("SELECT * FROM app_usage ORDER BY launchCount DESC")
    fun getAllUsage(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :pkg LIMIT 1")
    suspend fun getUsageForPackage(pkg: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: AppUsageEntity)
}
