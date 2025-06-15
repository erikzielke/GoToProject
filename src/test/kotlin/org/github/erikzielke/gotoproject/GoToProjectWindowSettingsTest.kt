package org.github.erikzielke.gotoproject

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoToProjectWindowSettingsTest {
    @Test
    fun `default settings should have isIncludeRecent set to false`() {
        // Arrange
        val settings = GoToProjectWindowSettings()

        // Act & Assert
        assertFalse(settings.isIncludeRecent)
    }

    @Test
    fun `default settings should have panelInSearchEverywhere set to false`() {
        // Arrange
        val settings = GoToProjectWindowSettings()

        // Act & Assert
        assertFalse(settings.panelInSearchEverywhere)
    }

    @Test
    fun `settings should be modifiable`() {
        // Arrange
        val settings = GoToProjectWindowSettings()

        // Act
        settings.isIncludeRecent = true
        settings.panelInSearchEverywhere = true

        // Assert
        assertTrue(settings.isIncludeRecent)
        assertTrue(settings.panelInSearchEverywhere)
    }
}
