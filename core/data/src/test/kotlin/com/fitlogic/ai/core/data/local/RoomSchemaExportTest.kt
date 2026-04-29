package com.fitlogic.ai.core.data.local

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RoomSchemaExportTest {
    @Test
    fun roomSchemaFileShouldExistForVersion2() {
        assertSchemaExists(2)
    }

    @Test
    fun roomSchemaFileShouldExistForVersion4() {
        assertSchemaExists(4)
    }

    private fun assertSchemaExists(version: Int) {
        val candidates =
            listOf(
                File("schemas/com.fitlogic.ai.core.data.local.FitLogicDatabase/$version.json"),
                File("core/data/schemas/com.fitlogic.ai.core.data.local.FitLogicDatabase/$version.json"),
            )
        assertTrue(
            "Room schema dosyasi bulunamadi: ${candidates.joinToString()}",
            candidates.any { it.exists() },
        )
    }
}
