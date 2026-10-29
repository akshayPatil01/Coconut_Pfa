package com.patil.coconut_pfa.db

import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import org.junit.Assert.assertNotNull
import org.junit.Test

class VaultMigrationTest {

    @Test
    fun testMigrationDefinitionNotNull() {
        val migration = CoconutDatabase.MIGRATION_1_2
        assertNotNull(migration)
        org.junit.Assert.assertEquals(1, migration.startVersion)
        org.junit.Assert.assertEquals(2, migration.endVersion)
    }
}
