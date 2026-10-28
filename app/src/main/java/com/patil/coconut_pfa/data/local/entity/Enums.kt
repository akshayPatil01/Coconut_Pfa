package com.patil.coconut_pfa.data.local.entity

enum class DbAccountType {
    BANK,
    CREDIT_CARD,
    CASH,
    WALLET,
    LOAN,
    INVESTMENT,
    FD
}

enum class DbAccountInstitution {
    HDFC,
    ICICI,
    SBI,
    AXIS,
    OTHER
}

enum class DbCardStatementSource {
    AUTO,
    SMS,
    MANUAL
}

enum class DbCategoryKind {
    EXPENSE,
    INCOME
}

enum class DbTxnType {
    EXPENSE,
    INCOME,
    TRANSFER,
    REFUND,
    ADJUSTMENT,
    SETTLEMENT
}

enum class DbTxnSource {
    MANUAL,
    SMS,
    SCAN,
    IMPORT,
    BILL,
    RECURRING
}

enum class DbBillKind {
    RENT,
    UTILITY,
    INSURANCE,
    SUBSCRIPTION,
    EMI,
    OTHER
}

enum class DbBillFrequency {
    MONTHLY,
    QUARTERLY,
    YEARLY,
    EVERY_N_DAYS
}

enum class DbBillOccurrenceStatus {
    UPCOMING,
    PAID,
    SKIPPED
}

enum class DbLedgerEntryKind {
    LENT,
    BORROWED,
    SPLIT_SHARE,
    SETTLEMENT
}

enum class DbSmsStatus {
    PENDING,
    CONFIRMED,
    IGNORED,
    DUPLICATE
}

enum class DbSmsSource {
    SMS,
    IMPORT
}
