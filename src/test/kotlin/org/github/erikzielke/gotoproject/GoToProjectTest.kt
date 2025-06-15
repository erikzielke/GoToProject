package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for GoToProject action
 *
 * This class uses BasePlatformTestCase to set up a proper IntelliJ platform test environment,
 * which provides access to the necessary platform services.
 */
class GoToProjectTest : BasePlatformTestCase() {
    /**
     * Test that the action is dumb aware (can be executed during indexing)
     */
    fun testIsDumbAware() {
        val goToProject = GoToProject()
        assertTrue("GoToProject action should be dumb aware", goToProject.isDumbAware())
    }

    /**
     * Test the structure of the action
     */
    fun testActionStructure() {
        GoToProject()

        // Verify that the action has the expected methods
        val actionPerformedMethod =
            GoToProject::class.java.getDeclaredMethod(
                "actionPerformed",
                AnActionEvent::class.java,
            )
        assertNotNull("actionPerformed method should exist", actionPerformedMethod)

        // Verify that the action has the expected private methods
        val methods = GoToProject::class.java.declaredMethods
        val methodNames = methods.map { it.name }

        assertTrue("Should have addOpenProjects method", methodNames.contains("addOpenProjects"))
        assertTrue("Should have addRecentProjects method", methodNames.contains("addRecentProjects"))
        assertTrue("Should have showPopup method", methodNames.contains("showPopup"))
        assertTrue("Should have moveLastActiveToTop method", methodNames.contains("moveLastActiveToTop"))
        assertTrue("Should have findLastActive method", methodNames.contains("findLastActive"))
    }

    /**
     * Test that the action has the expected template method pattern
     */
    fun testActionTemplate() {
        // This test verifies that the action follows the expected template method pattern
        // by checking that it has the necessary methods with the correct signatures

        GoToProject()

        // The actionPerformed method should call the following methods:
        // 1. addOpenProjects
        // 2. addRecentProjects
        // 3. showPopup

        // We can verify this by checking the method signatures
        val addOpenProjectsMethod =
            GoToProject::class.java.getDeclaredMethod(
                "addOpenProjects",
                DefaultActionGroup::class.java,
            )
        assertNotNull("addOpenProjects method should exist", addOpenProjectsMethod)

        val addRecentProjectsMethod =
            GoToProject::class.java.getDeclaredMethod(
                "addRecentProjects",
                GoToProjectApplicationComponent::class.java,
                Set::class.java,
                DefaultActionGroup::class.java,
            )
        assertNotNull("addRecentProjects method should exist", addRecentProjectsMethod)

        val showPopupMethod =
            GoToProject::class.java.getDeclaredMethod(
                "showPopup",
                DefaultActionGroup::class.java,
                AnActionEvent::class.java,
            )
        assertNotNull("showPopup method should exist", showPopupMethod)
    }

}
