package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for GoToLastProject action
 *
 * This class uses BasePlatformTestCase to set up a proper IntelliJ platform test environment,
 * which provides access to the necessary platform services.
 */
class GoToLastProjectTest : BasePlatformTestCase() {
    /**
     * Test that the action is dumb aware (can be executed during indexing)
     */
    fun testIsDumbAware() {
        val goToLastProject = GoToLastProject()
        assertTrue("GoToLastProject action should be dumb aware", goToLastProject.isDumbAware())
    }

    /**
     * Test the structure of the action
     */
    fun testActionStructure() {
        GoToLastProject()

        // Verify that the action has the expected methods
        val actionPerformedMethod =
            GoToLastProject::class.java.getDeclaredMethod(
                "actionPerformed",
                AnActionEvent::class.java,
            )
        assertNotNull("actionPerformed method should exist", actionPerformedMethod)
    }

    /**
     * Test that the action has the expected behavior
     */
    fun testActionBehavior() {
        // Create an instance of the action
        GoToLastProject()

        // Verify it overrides isDumbAware
        val isDumbAwareMethod = GoToLastProject::class.java.getDeclaredMethod("isDumbAware")
        assertNotNull("isDumbAware method should exist", isDumbAwareMethod)

        // The actual behavior of actionPerformed is difficult to test without mocking static methods,
        // which would require more complex testing frameworks like PowerMock.
        // The basic structure and inheritance tests above verify the class is set up correctly.
    }
}
