package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for GoToProjectSearchEverywhereContributor
 *
 * This class uses BasePlatformTestCase to set up a proper IntelliJ platform test environment,
 * which provides access to the necessary platform services.
 *
 * Note: We're only testing methods that don't depend on the static GoToProjectApplicationComponent.instance,
 * as mocking static methods would require more complex testing frameworks like PowerMock.
 */
class GoToProjectSearchEverywhereContributorTest : BasePlatformTestCase() {
    private lateinit var contributor: GoToProjectSearchEverywhereContributor
    private lateinit var mockEvent: AnActionEvent

    override fun setUp() {
        super.setUp()
        mockEvent = mock()
        contributor = GoToProjectSearchEverywhereContributor()
    }

    fun testGetSearchProviderId() {
        assertEquals("GoToProjectSearchEverywhereContributor", contributor.searchProviderId)
    }

    fun testGetGroupName() {
        assertEquals("Projects", contributor.groupName)
    }

    fun testGetSortWeight() {
        assertEquals(300, contributor.sortWeight)
    }

    fun testShowInFindResults() {
        assertFalse(contributor.showInFindResults())
    }

    fun testIsEmptyPatternSupported() {
        assertTrue(contributor.isEmptyPatternSupported())
    }

    fun testProcessSelectedItemReturnsTrue() {
        // Test with ReopenProjectAction
        val reopenAction = mock<ReopenProjectAction>()
        whenever(reopenAction.projectPath).thenReturn("/path/to/project")
        assertTrue(contributor.processSelectedItem(reopenAction, 0, "test"))

        // Test with ProjectWindowAction
        val projectWindowAction = mock<Project>()
        assertTrue(contributor.processSelectedItem(projectWindowAction, 0, "test"))
    }

    fun testProcessSelectedItemReturnsFalse() {
        // Test with unknown type
        assertFalse(contributor.processSelectedItem("unknown", 0, "test"))
    }

    fun testGetElementsRenderer() {
        val renderer = contributor.elementsRenderer
        assertNotNull(renderer)
        assertTrue(renderer is GoToProjectProjectListCellRenderer)
    }

    fun testGetDataForItem() {
        assertNull(contributor.getDataForItem(mock(), "test"))
    }
}
