package org.github.erikzielke.gotoproject

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.Container
import javax.swing.JCheckBox

/**
 * Tests for GotoProjectApplicationConfigurable
 */
@Suppress("TooManyFunctions")
class GotoProjectApplicationConfigurableTest : BasePlatformTestCase() {
    private lateinit var configurable: GotoProjectApplicationConfigurable
    private lateinit var originalState: GoToProjectWindowSettings

    override fun setUp() {
        super.setUp()
        originalState = GoToProjectApplicationComponent.instance.state
        GoToProjectApplicationComponent.instance.loadState(GoToProjectWindowSettings())
        configurable = GotoProjectApplicationConfigurable()
    }

    override fun tearDown() {
        try {
            configurable.disposeUIResources()
            GoToProjectApplicationComponent.instance.loadState(originalState)
        } finally {
            super.tearDown()
        }
    }

    fun testDisplayName() {
        assertEquals("Go To Project Window", configurable.displayName)
    }

    fun testHelpTopicIsNull() {
        assertNull(configurable.helpTopic)
    }

    fun testIsNotModifiedBeforeComponentIsCreated() {
        assertFalse(configurable.isModified)
    }

    fun testCreateComponentReturnsSameInstance() {
        val first = configurable.createComponent()
        val second = configurable.createComponent()

        assertNotNull(first)
        assertSame(first, second)
    }

    fun testComponentHasOneCheckBoxPerSetting() {
        val component = configurable.createComponent() as Container

        assertEquals(3, findCheckBoxes(component).size)
    }

    fun testCheckBoxesReflectCurrentState() {
        GoToProjectApplicationComponent.instance.state.isIncludeRecent = true

        val checkBoxes = findCheckBoxes(configurable.createComponent() as Container)

        assertEquals(listOf(true, false, false), checkBoxes.map { it.isSelected })
    }

    fun testChangingCheckBoxMarksAsModifiedAndApplyUpdatesState() {
        val checkBoxes = findCheckBoxes(configurable.createComponent() as Container)
        assertFalse(configurable.isModified)

        checkBoxes.forEach { it.isSelected = true }

        assertTrue(configurable.isModified)

        configurable.apply()

        val state = GoToProjectApplicationComponent.instance.state
        assertTrue(state.isIncludeRecent)
        assertTrue(state.showTabInSearchEverywhere)
        assertTrue(state.openTabInSearchEverywhere)
        assertFalse(configurable.isModified)
    }

    fun testResetRestoresCheckBoxesFromState() {
        val checkBoxes = findCheckBoxes(configurable.createComponent() as Container)
        checkBoxes.forEach { it.isSelected = true }

        configurable.reset()

        assertFalse(configurable.isModified)
        assertTrue(checkBoxes.none { it.isSelected })
    }

    fun testDisposeUIResourcesCreatesNewComponentNextTime() {
        val first = configurable.createComponent()

        configurable.disposeUIResources()

        assertFalse(configurable.isModified)
        assertNotSame(first, configurable.createComponent())
    }

    fun testApplyAndResetWithoutComponentDoNotThrow() {
        configurable.apply()
        configurable.reset()
    }

    private fun findCheckBoxes(container: Container): List<JCheckBox> =
        container.components.flatMap { child ->
            val nested = if (child is Container) findCheckBoxes(child) else emptyList()
            if (child is JCheckBox) listOf(child) + nested else nested
        }
}
