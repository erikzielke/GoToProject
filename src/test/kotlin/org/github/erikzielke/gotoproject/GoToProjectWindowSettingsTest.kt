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
    fun `default settings should have showTabInSearchEverywhere set to false`() {
        // Arrange
        val settings = GoToProjectWindowSettings()

        // Act & Assert
        assertFalse(settings.showTabInSearchEverywhere)
    }

    @Test
    fun `default settings should have openTabInSearchEverywhere set to false`() {
        // Arrange
        val settings = GoToProjectWindowSettings()

        // Act & Assert
        assertFalse(settings.openTabInSearchEverywhere)
    }

    @Test
    fun `settings should be modifiable`() {
        // Arrange
        val settings = GoToProjectWindowSettings()

        // Act
        settings.isIncludeRecent = true
        settings.showTabInSearchEverywhere = true
        settings.openTabInSearchEverywhere = true

        // Assert
        assertTrue(settings.isIncludeRecent)
        assertTrue(settings.showTabInSearchEverywhere)
        assertTrue(settings.openTabInSearchEverywhere)
    }
}
