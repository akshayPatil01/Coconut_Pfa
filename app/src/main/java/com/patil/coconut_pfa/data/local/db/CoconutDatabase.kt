package com.patil.coconut_pfa.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.patil.coconut_pfa.data.local.dao.AccountDao
import com.patil.coconut_pfa.data.local.dao.BillDao
import com.patil.coconut_pfa.data.local.dao.BillOccurrenceDao
import com.patil.coconut_pfa.data.local.dao.CardDetailsDao
import com.patil.coconut_pfa.data.local.dao.CardStatementDao
import com.patil.coconut_pfa.data.local.dao.CategoryDao
import com.patil.coconut_pfa.data.local.dao.FdDetailsDao
import com.patil.coconut_pfa.data.local.dao.GoalContributionDao
import com.patil.coconut_pfa.data.local.dao.GoalDao
import com.patil.coconut_pfa.data.local.dao.LedgerEntryDao
import com.patil.coconut_pfa.data.local.dao.LoanDetailsDao
import com.patil.coconut_pfa.data.local.dao.MerchantMemoryDao
import com.patil.coconut_pfa.data.local.dao.NetWorthSnapshotDao
import com.patil.coconut_pfa.data.local.dao.PersonDao
import com.patil.coconut_pfa.data.local.dao.SmsItemDao
import com.patil.coconut_pfa.data.local.dao.TxnDao
import com.patil.coconut_pfa.data.local.dao.ValuationDao
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.BillEntity
import com.patil.coconut_pfa.data.local.entity.BillOccurrenceEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.CategoryEntity
import com.patil.coconut_pfa.data.local.entity.FdDetailsEntity
import com.patil.coconut_pfa.data.local.entity.GoalContributionEntity
import com.patil.coconut_pfa.data.local.entity.GoalEntity
import com.patil.coconut_pfa.data.local.entity.LedgerEntryEntity
import com.patil.coconut_pfa.data.local.entity.LoanDetailsEntity
import com.patil.coconut_pfa.data.local.entity.MerchantMemoryEntity
import com.patil.coconut_pfa.data.local.entity.NetWorthSnapshotEntity
import com.patil.coconut_pfa.data.local.entity.PersonEntity
import com.patil.coconut_pfa.data.local.entity.SmsItemEntity
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import com.patil.coconut_pfa.data.local.security.PassphraseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        AccountEntity::class,
        CardDetailsEntity::class,
        CardStatementEntity::class,
        LoanDetailsEntity::class,
        FdDetailsEntity::class,
        ValuationEntity::class,
        CategoryEntity::class,
        TxnEntity::class,
        BillEntity::class,
        BillOccurrenceEntity::class,
        PersonEntity::class,
        LedgerEntryEntity::class,
        GoalEntity::class,
        GoalContributionEntity::class,
        SmsItemEntity::class,
        MerchantMemoryEntity::class,
        NetWorthSnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CoconutDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun cardDetailsDao(): CardDetailsDao
    abstract fun cardStatementDao(): CardStatementDao
    abstract fun loanDetailsDao(): LoanDetailsDao
    abstract fun fdDetailsDao(): FdDetailsDao
    abstract fun valuationDao(): ValuationDao
    abstract fun categoryDao(): CategoryDao
    abstract fun txnDao(): TxnDao
    abstract fun billDao(): BillDao
    abstract fun billOccurrenceDao(): BillOccurrenceDao
    abstract fun personDao(): PersonDao
    abstract fun ledgerEntryDao(): LedgerEntryDao
    abstract fun goalDao(): GoalDao
    abstract fun goalContributionDao(): GoalContributionDao
    abstract fun smsItemDao(): SmsItemDao
    abstract fun merchantMemoryDao(): MerchantMemoryDao
    abstract fun netWorthSnapshotDao(): NetWorthSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: CoconutDatabase? = null

        fun getInstance(context: Context): CoconutDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): CoconutDatabase {
            val passphraseBytes = PassphraseManager.getOrCreatePassphrase(context)
            val factory = SupportOpenHelperFactory(passphraseBytes)

            return Room.databaseBuilder(
                context,
                CoconutDatabase::class.java,
                "coconut.db"
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).categoryDao().insertAll(CategoryPresets.presets)
                        }
                    }
                })
                .build()
        }
    }
}
