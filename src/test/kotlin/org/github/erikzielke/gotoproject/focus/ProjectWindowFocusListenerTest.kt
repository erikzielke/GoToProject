package org.github.erikzielke.gotoproject.focus

import com.intellij.openapi.project.Project
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.awt.Window
import java.awt.event.WindowEvent
import kotlin.test.BeforeTest
import kotlin.test.Test

class ProjectWindowFocusListenerTest {
    private lateinit var mockComponent: GoToProjectApplicationComponent

    @BeforeTest
    fun setup() {
        // Create a mock component
        mockComponent = mock()
    }

    @Test
    fun `windowLostFocus should set lastFocusLost to the current project`() {
        // Arrange
        val mockProject = mock<Project>()
        val mockWindow = mock<Window>()
        val mockEvent = WindowEvent(mockWindow, WindowEvent.WINDOW_LOST_FOCUS)

        val listener = ProjectWindowFocusListener(mockProject, mockComponent)

        // Act
        listener.windowLostFocus(mockEvent)

        // Assert
        verify(mockComponent).lastFocusLost = mockProject
    }

    @Test
    fun `windowGainedFocus should update focusedBefore when lastFocusLost is different project`() {
        // Arrange
        val currentProject = mock<Project>()
        val previousProject = mock<Project>()
        val mockWindow = mock<Window>()
        val mockEvent = WindowEvent(mockWindow, WindowEvent.WINDOW_GAINED_FOCUS)

        whenever(mockComponent.lastFocusLost).thenReturn(previousProject)
        whenever(previousProject.name).thenReturn("Previous Project")
        whenever(currentProject.name).thenReturn("Current Project")

        val listener = ProjectWindowFocusListener(currentProject, mockComponent)

        // Act
        listener.windowGainedFocus(mockEvent)

        // Assert
        verify(mockComponent).focusedBefore = previousProject
        verify(mockComponent).lastFocusLost = null
    }

    @Test
    fun `windowGainedFocus should not update focusedBefore when lastFocusLost is null`() {
        // Arrange
        val currentProject = mock<Project>()
        val mockWindow = mock<Window>()
        val mockEvent = WindowEvent(mockWindow, WindowEvent.WINDOW_GAINED_FOCUS)

        whenever(mockComponent.lastFocusLost).thenReturn(null)

        val listener = ProjectWindowFocusListener(currentProject, mockComponent)

        // Act
        listener.windowGainedFocus(mockEvent)

        // Assert
        verify(mockComponent, never()).focusedBefore = any()
        verify(mockComponent, never()).lastFocusLost = any()
    }

    @Test
    fun `windowGainedFocus should not update focusedBefore when lastFocusLost is same project`() {
        // Arrange
        val currentProject = mock<Project>()
        val mockWindow = mock<Window>()
        val mockEvent = WindowEvent(mockWindow, WindowEvent.WINDOW_GAINED_FOCUS)

        whenever(mockComponent.lastFocusLost).thenReturn(currentProject)
        whenever(currentProject.name).thenReturn("Current Project")

        val listener = ProjectWindowFocusListener(currentProject, mockComponent)

        // Act
        listener.windowGainedFocus(mockEvent)

        // Assert
        verify(mockComponent, never()).focusedBefore = any()
        verify(mockComponent, never()).lastFocusLost = any()
    }
}
