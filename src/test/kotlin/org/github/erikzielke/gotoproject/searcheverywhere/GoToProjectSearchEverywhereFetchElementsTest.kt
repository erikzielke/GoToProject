package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.Processor
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent
import org.github.erikzielke.gotoproject.GoToProjectWindowSettings
import org.github.erikzielke.gotoproject.focus.projects
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for GoToProjectSearchEverywhereContributor behaviour that depends on the plugin settings
 * and the list of open projects.
 */
class GoToProjectSearchEverywhereFetchElementsTest : BasePlatformTestCase() {
    private lateinit var contributor: GoToProjectSearchEverywhereContributor
    private lateinit var originalState: GoToProjectWindowSettings
    private val addedProjects = mutableListOf<Project>()

    override fun setUp() {
        super.setUp()
        originalState = GoToProjectApplicationComponent.instance.state
        GoToProjectApplicationComponent.instance.loadState(GoToProjectWindowSettings())
        contributor = GoToProjectSearchEverywhereContributor()
    }

    override fun tearDown() {
        try {
            projects.removeAll(addedProjects)
            GoToProjectApplicationComponent.instance.loadState(originalState)
        } finally {
            super.tearDown()
        }
    }

    fun testIsShownInSeparateTabFollowsSetting() {
        assertFalse(contributor.isShownInSeparateTab)

        GoToProjectApplicationComponent.instance.state.showTabInSearchEverywhere = true

        assertTrue(contributor.isShownInSeparateTab)
    }

    fun testFetchElementsReturnsNothingWhenTabIsDisabled() {
        addOpenProject("Alpha Project")

        assertEmpty(fetch("alpha"))
    }

    fun testFetchElementsReturnsMatchingOpenProjects() {
        GoToProjectApplicationComponent.instance.state.showTabInSearchEverywhere = true
        val alpha = addOpenProject("Alpha Project")
        addOpenProject("Beta Project")

        assertEquals(listOf<Any>(alpha), fetch("alpha"))
    }

    fun testFetchElementsMatchesFuzzyPattern() {
        GoToProjectApplicationComponent.instance.state.showTabInSearchEverywhere = true
        val project = addOpenProject("zebra-quokka-xylophone")

        assertEquals(listOf<Any>(project), fetch("zqx"))
    }

    fun testFetchElementsStopsWhenConsumerReturnsFalse() {
        GoToProjectApplicationComponent.instance.state.showTabInSearchEverywhere = true
        addOpenProject("Gamma One")
        addOpenProject("Gamma Two")

        val collected = mutableListOf<Any>()
        contributor.fetchElements(
            "gamma",
            EmptyProgressIndicator(),
            Processor {
                collected.add(it)
                false
            },
        )

        assertEquals(1, collected.size)
    }

    fun testFocusOpenProjectReturnsTrueWhenProjectHasNoFrame() {
        assertTrue(contributor.focusOpeProject(mock()))
    }

    private fun addOpenProject(name: String): Project {
        val openProject = mock<Project>()
        whenever(openProject.name).thenReturn(name)
        projects.add(openProject)
        addedProjects.add(openProject)
        return openProject
    }

    private fun fetch(pattern: String): List<Any> {
        val collected = mutableListOf<Any>()
        contributor.fetchElements(
            pattern,
            EmptyProgressIndicator(),
            Processor {
                collected.add(it)
                true
            },
        )
        return collected
    }
}
