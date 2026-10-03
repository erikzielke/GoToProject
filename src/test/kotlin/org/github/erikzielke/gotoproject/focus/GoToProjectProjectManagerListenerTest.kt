package org.github.erikzielke.gotoproject.focus

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for GoToProjectProjectManagerListener
 *
 * Uses BasePlatformTestCase because the listener looks up the WindowManager and the application service.
 */
class GoToProjectProjectManagerListenerTest : BasePlatformTestCase() {
    private lateinit var listener: GoToProjectProjectManagerListener
    private lateinit var mockProject: Project

    override fun setUp() {
        super.setUp()
        listener = GoToProjectProjectManagerListener()
        mockProject = mock()
        whenever(mockProject.name).thenReturn("Mock Project")
    }

    override fun tearDown() {
        try {
            projects.remove(mockProject)
            listeners.remove(mockProject)
        } finally {
            super.tearDown()
        }
    }

    fun testExecuteRegistersProject() {
        runBlocking { listener.execute(mockProject) }

        assertTrue(mockProject in projects)
    }

    fun testExecuteRegistersFocusListenerForProject() {
        runBlocking { listener.execute(mockProject) }

        assertNotNull(listeners[mockProject])
    }

    fun testProjectClosedUnregistersProject() {
        runBlocking { listener.execute(mockProject) }

        listener.projectClosed(mockProject)

        assertFalse(mockProject in projects)
    }

    fun testProjectClosedForUnknownProjectDoesNotThrow() {
        listener.projectClosed(mockProject)

        assertFalse(mockProject in projects)
    }
}
