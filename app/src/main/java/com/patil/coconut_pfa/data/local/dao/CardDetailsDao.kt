package com.patil.coconut_pfa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDetailsDao {
    @Query("SELECT * FROM card_details WHERE accountId = :accountId")
    suspend fun getByAccountId(accountId: String): CardDetailsEntity?

    @Query("SELECT * FROM card_details WHERE accountId = :accountId")
    fun getByAccountIdFlow(accountId: String): Flow<CardDetailsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cardDetails: CardDetailsEntity)

    @Update
    suspend fun update(cardDetails: CardDetailsEntity)

    @Delete
    suspend fun delete(cardDetails: CardDetailsEntity)
}
