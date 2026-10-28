package com.patil.coconut_pfa.data.local.db

import androidx.room.TypeConverter
import com.patil.coconut_pfa.data.local.entity.DbAccountInstitution
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.DbBillFrequency
import com.patil.coconut_pfa.data.local.entity.DbBillKind
import com.patil.coconut_pfa.data.local.entity.DbBillOccurrenceStatus
import com.patil.coconut_pfa.data.local.entity.DbCardStatementSource
import com.patil.coconut_pfa.data.local.entity.DbCategoryKind
import com.patil.coconut_pfa.data.local.entity.DbLedgerEntryKind
import com.patil.coconut_pfa.data.local.entity.DbSmsSource
import com.patil.coconut_pfa.data.local.entity.DbSmsStatus
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType

class Converters {
    @TypeConverter
    fun fromAccountType(value: DbAccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): DbAccountType? = value?.let { enumValueOf<DbAccountType>(it) }

    @TypeConverter
    fun fromAccountInstitution(value: DbAccountInstitution?): String? = value?.name

    @TypeConverter
    fun toAccountInstitution(value: String?): DbAccountInstitution? = value?.let { enumValueOf<DbAccountInstitution>(it) }

    @TypeConverter
    fun fromCardStatementSource(value: DbCardStatementSource?): String? = value?.name

    @TypeConverter
    fun toCardStatementSource(value: String?): DbCardStatementSource? = value?.let { enumValueOf<DbCardStatementSource>(it) }

    @TypeConverter
    fun fromCategoryKind(value: DbCategoryKind?): String? = value?.name

    @TypeConverter
    fun toCategoryKind(value: String?): DbCategoryKind? = value?.let { enumValueOf<DbCategoryKind>(it) }

    @TypeConverter
    fun fromTxnType(value: DbTxnType?): String? = value?.name

    @TypeConverter
    fun toTxnType(value: String?): DbTxnType? = value?.let { enumValueOf<DbTxnType>(it) }

    @TypeConverter
    fun fromTxnSource(value: DbTxnSource?): String? = value?.name

    @TypeConverter
    fun toTxnSource(value: String?): DbTxnSource? = value?.let { enumValueOf<DbTxnSource>(it) }

    @TypeConverter
    fun fromBillKind(value: DbBillKind?): String? = value?.name

    @TypeConverter
    fun toBillKind(value: String?): DbBillKind? = value?.let { enumValueOf<DbBillKind>(it) }

    @TypeConverter
    fun fromBillFrequency(value: DbBillFrequency?): String? = value?.name

    @TypeConverter
    fun toBillFrequency(value: String?): DbBillFrequency? = value?.let { enumValueOf<DbBillFrequency>(it) }

    @TypeConverter
    fun fromBillOccurrenceStatus(value: DbBillOccurrenceStatus?): String? = value?.name

    @TypeConverter
    fun toBillOccurrenceStatus(value: String?): DbBillOccurrenceStatus? = value?.let { enumValueOf<DbBillOccurrenceStatus>(it) }

    @TypeConverter
    fun fromLedgerEntryKind(value: DbLedgerEntryKind?): String? = value?.name

    @TypeConverter
    fun toLedgerEntryKind(value: String?): DbLedgerEntryKind? = value?.let { enumValueOf<DbLedgerEntryKind>(it) }

    @TypeConverter
    fun fromSmsStatus(value: DbSmsStatus?): String? = value?.name

    @TypeConverter
    fun toSmsStatus(value: String?): DbSmsStatus? = value?.let { enumValueOf<DbSmsStatus>(it) }

    @TypeConverter
    fun fromSmsSource(value: DbSmsSource?): String? = value?.name

    @TypeConverter
    fun toSmsSource(value: String?): DbSmsSource? = value?.let { enumValueOf<DbSmsSource>(it) }
}
