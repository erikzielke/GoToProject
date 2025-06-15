package org.github.erikzielke.gotoproject

import com.intellij.openapi.project.Project
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import org.mockito.kotlin.mock

class GoToProjectApplicationComponentTest {

    @Test
    fun `getState should return the current state`() {
        // Arrange
        val component = GoToProjectApplicationComponent()

        // Act
        val state = component.state

        // Assert
        assertFalse(state.isIncludeRecent)
        assertFalse(state.panelInSearchEverywhere)
    }

    @Test
    fun `loadState should update the component's state`() {
        // Arrange
        val component = GoToProjectApplicationComponent()
        val newState = GoToProjectWindowSettings()
        newState.isIncludeRecent = true
        newState.panelInSearchEverywhere = true

        // Act
        component.loadState(newState)

        // Assert
        val currentState = component.state
        assertEquals(true, currentState.isIncludeRecent)
        assertEquals(true, currentState.panelInSearchEverywhere)
    }

    @Test
    fun `focusedBefore should be null by default`() {
        // Arrange
        val component = GoToProjectApplicationComponent()

        // Assert
        assertNull(component.focusedBefore)
    }

    @Test
    fun `lastFocusLost should be null by default`() {
        // Arrange
        val component = GoToProjectApplicationComponent()

        // Assert
        assertNull(component.lastFocusLost)
    }

    @Test
    fun `focusedBefore should be updatable`() {
        // Arrange
        val component = GoToProjectApplicationComponent()
        val mockProject = mock<Project>()

        // Act
        component.focusedBefore = mockProject

        // Assert
        assertSame(mockProject, component.focusedBefore)
    }

    @Test
    fun `lastFocusLost should be updatable`() {
        // Arrange
        val component = GoToProjectApplicationComponent()
        val mockProject = mock<Project>()

        // Act
        component.lastFocusLost = mockProject

        // Assert
        assertSame(mockProject, component.lastFocusLost)
    }
}
