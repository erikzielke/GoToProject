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
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JList
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText

/**
 * Tests for GoToProjectProjectListCellRenderer
 *
 * This class uses BasePlatformTestCase to set up a proper IntelliJ platform test environment,
 * which provides access to the necessary platform services like EditorColorsManager and
 * RecentProjectsManagerBase.
 */
@Suppress("TooManyFunctions")
class GoToProjectProjectListCellRendererTest : BasePlatformTestCase() {
    private lateinit var disposable: Disposable
    private lateinit var renderer: GoToProjectProjectListCellRenderer
    private lateinit var customizeMethod: Method
    private val tempDirs = mutableListOf<Path>()

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
        try {
            Disposer.dispose(disposable)
            tempDirs.forEach { it.toFile().deleteRecursively() }
        } finally {
            super.tearDown()
        }
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

    fun testRecentProjectInGitRepoShowsBranch() {
        val projectDir = createTempDir("renderer-git")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("ref: refs/heads/develop\n")

        val text = renderRecentProject("Repo Project", projectDir.toString())

        assertTrue("Expected branch label in '$text'", text.contains("[develop]"))
        assertFalse("Did not expect worktree marker in '$text'", text.contains("(worktree)"))
        assertTrue("Expected project location in '$text'", text.contains(projectDir.toString()))
    }

    fun testRecentProjectInLinkedWorktreeShowsBranchAndWorktreeMarker() {
        val mainRepo = createTempDir("renderer-main")
        val worktreeGitDir = (mainRepo / ".git" / "worktrees" / "wt").createDirectories()
        (worktreeGitDir / "HEAD").writeText("ref: refs/heads/feature\n")
        val worktreeDir = createTempDir("renderer-worktree")
        (worktreeDir / ".git").writeText("gitdir: $worktreeGitDir\n")

        val text = renderRecentProject("Worktree Project", worktreeDir.toString())

        assertTrue("Expected branch label in '$text'", text.contains("[feature] (worktree)"))
    }

    fun testOpenProjectInGitRepoShowsBranch() {
        val projectDir = createTempDir("renderer-open-git")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("ref: refs/heads/develop\n")
        val openProject = mock<Project>()
        whenever(openProject.name).thenReturn("Open Project")
        whenever(openProject.basePath).thenReturn(projectDir.toString())

        val text = render(openProject)

        assertTrue("Expected branch label in '$text'", text.startsWith("Open Project [develop] "))
    }

    private fun renderRecentProject(
        name: String,
        path: String,
    ): String {
        val reopenAction = mock<ReopenProjectAction>()
        whenever(reopenAction.projectName).thenReturn(name)
        whenever(reopenAction.projectPath).thenReturn(path)
        return render(reopenAction)
    }

    private fun render(value: Any): String {
        val coloredRenderer =
            object : ColoredListCellRenderer<Any>() {
                override fun customizeCellRenderer(
                    list: JList<out Any>,
                    value: Any?,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean,
                ) = Unit
            }
        val handled = customizeMethod.invoke(renderer, coloredRenderer, JList<Any>(), value, 0, false, false) as Boolean
        assertTrue("Renderer should handle $value", handled)
        return coloredRenderer.getCharSequence(false).toString()
    }

    private fun createTempDir(prefix: String): Path = Files.createTempDirectory(prefix).also { tempDirs.add(it) }
}
