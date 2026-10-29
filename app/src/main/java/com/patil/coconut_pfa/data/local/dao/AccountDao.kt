package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountByIdFlow(id: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    suspend fun getAllAccountsList(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY createdAt DESC")
    fun getActiveAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY createdAt DESC")
    suspend fun getActiveAccountsList(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE accounts SET archived = :archived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, updatedAt: Long = System.currentTimeMillis())
}
