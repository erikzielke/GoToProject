package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for GoToProject action
 *
 * Uses BasePlatformTestCase so the fixture project is registered as an open project.
 */
class GoToProjectTest : BasePlatformTestCase() {
    private var savedFocusedBefore: Project? = null
    private var savedIncludeRecent: Boolean = false

    override fun setUp() {
        super.setUp()
        val component = GoToProjectApplicationComponent.instance
        savedFocusedBefore = component.focusedBefore
        savedIncludeRecent = component.state.isIncludeRecent
    }

    override fun tearDown() {
        try {
            val component = GoToProjectApplicationComponent.instance
            component.focusedBefore = savedFocusedBefore
            component.state.isIncludeRecent = savedIncludeRecent
            savedFocusedBefore = null
        } finally {
            super.tearDown()
        }
    }

    /**
     * Test that the action is dumb aware (can be executed during indexing)
     */
    fun testIsDumbAware() {
        val goToProject = GoToProject()
        assertTrue("GoToProject action should be dumb aware", goToProject.isDumbAware())
    }

    fun testAddOpenProjectsAddsSeparatorAndProjectAction() {
        val group = DefaultActionGroup()

        val paths = GoToProject().addOpenProjects(group)

        val children = group.childActionsOrStubs
        val first = children.first()
        assertTrue("First child should be a separator", first is Separator)
        assertEquals("Open Projects", (first as Separator).text)
        val projectActions = children.filterIsInstance<GoToProjectWindowAction>()
        assertTrue(
            "Group should contain an action for the test project",
            projectActions.any { it.project == project },
        )
        assertTrue("Returned paths should contain the project's base path", project.basePath in paths)
    }

    fun testAddOpenProjectsPutsLastActiveProjectFirst() {
        GoToProjectApplicationComponent.instance.focusedBefore = project
        val group = DefaultActionGroup()

        GoToProject().addOpenProjects(group)

        val firstProjectAction = group.childActionsOrStubs.filterIsInstance<GoToProjectWindowAction>().first()
        assertEquals(project, firstProjectAction.project)
    }

    fun testFindLastActiveReturnsFocusedBeforeProject() {
        GoToProjectApplicationComponent.instance.focusedBefore = project

        val result = GoToProject().findLastActive(listOf(project))

        assertEquals(project, result)
    }

    fun testFindLastActiveReturnsNullWhenFocusedBeforeIsNull() {
        GoToProjectApplicationComponent.instance.focusedBefore = null

        val result = GoToProject().findLastActive(listOf(project))

        assertNull(result)
    }

    fun testFindLastActiveReturnsNullWhenFocusedBeforeIsUnmatched() {
        val other = mock<Project>()
        whenever(other.presentableUrl).thenReturn("/some/other/project")
        GoToProjectApplicationComponent.instance.focusedBefore = other

        val result = GoToProject().findLastActive(listOf(project))

        assertNull(result)
    }

    fun testAddRecentProjectsAddsNothingWhenRecentDisabled() {
        GoToProjectApplicationComponent.instance.state.isIncludeRecent = false
        val group = DefaultActionGroup()

        GoToProject().addRecentProjects(emptySet(), group)

        assertEquals(0, group.childrenCount)
    }

    fun testAddRecentProjectsAddsSeparatorWhenRecentEnabled() {
        GoToProjectApplicationComponent.instance.state.isIncludeRecent = true
        val group = DefaultActionGroup()

        GoToProject().addRecentProjects(setOfNotNull(project.basePath), group)

        val first = group.childActionsOrStubs.first()
        assertTrue("First child should be a separator", first is Separator)
        assertEquals("Recent Projects", (first as Separator).text)
        val recentActions = group.childActionsOrStubs.drop(1)
        assertTrue(
            "Recent projects should only contain GoToRecentProjectAction entries",
            recentActions.all { it is GoToRecentProjectAction },
        )
    }
}
