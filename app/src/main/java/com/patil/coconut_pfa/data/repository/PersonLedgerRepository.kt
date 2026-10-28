package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.LedgerEntryDao
import com.patil.coconut_pfa.data.local.dao.PersonDao
import com.patil.coconut_pfa.data.local.entity.LedgerEntryEntity
import com.patil.coconut_pfa.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

class PersonLedgerRepository(
    private val personDao: PersonDao,
    private val ledgerEntryDao: LedgerEntryDao
) {
    fun getAllPersons(): Flow<List<PersonEntity>> = personDao.getAllPersonsFlow()
    suspend fun getPersonById(id: String): PersonEntity? = personDao.getPersonById(id)
    suspend fun savePerson(person: PersonEntity) = personDao.insert(person)
    suspend fun updatePerson(person: PersonEntity) = personDao.update(person)
    suspend fun deletePerson(person: PersonEntity) = personDao.delete(person)

    fun getEntriesForPerson(personId: String): Flow<List<LedgerEntryEntity>> = ledgerEntryDao.getEntriesForPersonFlow(personId)
    fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>> = ledgerEntryDao.getAllEntriesFlow()
    suspend fun saveLedgerEntry(entry: LedgerEntryEntity) = ledgerEntryDao.insert(entry)
    suspend fun updateLedgerEntry(entry: LedgerEntryEntity) = ledgerEntryDao.update(entry)
    suspend fun deleteLedgerEntry(entry: LedgerEntryEntity) = ledgerEntryDao.delete(entry)
}
