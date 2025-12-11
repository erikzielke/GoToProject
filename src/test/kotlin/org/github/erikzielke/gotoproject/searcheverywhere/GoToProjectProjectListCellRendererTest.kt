package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.ColoredListCellRenderer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.reflect.Method
import javax.swing.JList

/**
 * Tests for GoToProjectProjectListCellRenderer
 *
 * This class uses BasePlatformTestCase to set up a proper IntelliJ platform test environment,
 * which provides access to the necessary platform services like EditorColorsManager and
 * RecentProjectsManagerBase.
 */
class GoToProjectProjectListCellRendererTest : BasePlatformTestCase() {
    private lateinit var disposable: Disposable
    private lateinit var renderer: GoToProjectProjectListCellRenderer
    private lateinit var customizeMethod: Method

    override fun setUp() {
        super.setUp()
        disposable = Disposer.newDisposable()
        renderer = GoToProjectProjectListCellRenderer(disposable)

        // Use reflection to access the protected method
        customizeMethod =
            GoToProjectProjectListCellRenderer::class.java.getDeclaredMethod(
                "customizeNonPsiElementLeftRenderer",
                ColoredListCellRenderer::class.java,
                JList::class.java,
                Any::class.java,
                Int::class.java,
                Boolean::class.java,
                Boolean::class.java,
            )
        customizeMethod.isAccessible = true
    }

    override fun tearDown() {
        Disposer.dispose(disposable)
        super.tearDown()
    }

    fun testRendererCanBeCreated() {
        // Just verify that the renderer was created successfully in setUp()
        assertNotNull(renderer)
    }

    fun testCustomizeNonPsiElementLeftRendererReturnsFalseWhenRendererIsNull() {
        val result =
            customizeMethod.invoke(
                renderer,
                null,
                mock<JList<Any>>(),
                "value",
                0,
                false,
                false,
            ) as Boolean

        assertFalse("Should return false when renderer is null", result)
    }

    fun testCustomizeNonPsiElementLeftRendererReturnsFalseWhenListIsNull() {
        val mockRenderer = mock<ColoredListCellRenderer<Any>>()

        val result =
            customizeMethod.invoke(
                renderer,
                mockRenderer,
                null,
                "value",
                0,
                false,
                false,
            ) as Boolean

        assertFalse("Should return false when list is null", result)
    }

    fun testCustomizeNonPsiElementLeftRendererReturnsTrueForReopenProjectAction() {
        val mockRenderer = mock<ColoredListCellRenderer<Any>>()
        val mockList = mock<JList<Any>>()
        val reopenAction = mock<ReopenProjectAction>()

        // Set up the mock ReopenProjectAction
        whenever(reopenAction.projectName).thenReturn("Test Project")
        whenever(reopenAction.projectPath).thenReturn("/path/to/project")

        // Configure the mock list to return a foreground color
        whenever(mockList.foreground).thenReturn(java.awt.Color.BLACK)

        val result =
            customizeMethod.invoke(
                renderer,
                mockRenderer,
                mockList,
                reopenAction,
                0,
                false,
                false,
            ) as Boolean

        assertTrue("Should return true for ReopenProjectAction", result)
    }

    fun testCustomizeNonPsiElementLeftRendererReturnsTrueForProjectWindowAction() {
        val mockRenderer = mock<ColoredListCellRenderer<Any>>()
        val mockList = mock<JList<Any>>()
        val projectWindowAction = mock<Project>()

        // Set up the mock ProjectWindowAction
        whenever(projectWindowAction.name).thenReturn("Test Project")
        whenever(projectWindowAction.basePath).thenReturn("/path/to/project")

        // Configure the mock list to return a foreground color
        whenever(mockList.foreground).thenReturn(java.awt.Color.BLACK)

        val result =
            customizeMethod.invoke(
                renderer,
                mockRenderer,
                mockList,
                projectWindowAction,
                0,
                false,
                false,
            ) as Boolean

        assertTrue("Should return true for ProjectWindowAction", result)
    }

    fun testCustomizeNonPsiElementLeftRendererReturnsFalseForUnknownValueType() {
        val mockRenderer = mock<ColoredListCellRenderer<Any>>()
        val mockList = mock<JList<Any>>()

        val result =
            customizeMethod.invoke(
                renderer,
                mockRenderer,
                mockList,
                "Unknown Value Type",
                0,
                false,
                false,
            ) as Boolean

        assertFalse("Should return false for unknown value types", result)
    }
}
