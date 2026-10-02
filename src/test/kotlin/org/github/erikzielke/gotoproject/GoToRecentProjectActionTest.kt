package org.github.erikzielke.gotoproject

import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText

/**
 * Tests for GoToRecentProjectAction
 *
 * Uses BasePlatformTestCase because the action looks up the project icon via RecentProjectsManagerBase.
 */
class GoToRecentProjectActionTest : BasePlatformTestCase() {
    fun testTextIsProjectNameWhenNotAGitRepository() {
        val projectDir = createTempDir("recent-no-git")
        val delegate = mockReopenAction("Recent Project", projectDir.toString())

        val action = GoToRecentProjectAction(delegate)

        assertEquals("Recent Project", action.templatePresentation.text)
    }

    fun testTextFallsBackToProjectPathWhenNameIsNull() {
        val projectDir = createTempDir("recent-no-name")
        val delegate = mockReopenAction(null, projectDir.toString())

        val action = GoToRecentProjectAction(delegate)

        assertEquals(projectDir.toString(), action.templatePresentation.text)
    }

    fun testTextIncludesBranchName() {
        val projectDir = createTempDir("recent-git")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("ref: refs/heads/develop\n")
        val delegate = mockReopenAction("Recent Project", projectDir.toString())

        val action = GoToRecentProjectAction(delegate)

        assertEquals("Recent Project [develop]", action.templatePresentation.text)
    }

    fun testTextIncludesWorktreeSuffix() {
        val mainRepo = createTempDir("recent-main")
        val worktreeGitDir = (mainRepo / ".git" / "worktrees" / "wt").createDirectories()
        (worktreeGitDir / "HEAD").writeText("ref: refs/heads/feature\n")
        val worktreeDir = createTempDir("recent-worktree")
        (worktreeDir / ".git").writeText("gitdir: $worktreeGitDir\n")
        val delegate = mockReopenAction("Recent Project", worktreeDir.toString())

        val action = GoToRecentProjectAction(delegate)

        assertEquals("Recent Project [feature] (worktree)", action.templatePresentation.text)
    }

    fun testActionUpdateThreadIsBackground() {
        val delegate = mockReopenAction("Recent Project", createTempDir("recent-bgt").toString())

        val action = GoToRecentProjectAction(delegate)

        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    fun testActionPerformedDelegatesToReopenProjectAction() {
        val delegate = mockReopenAction("Recent Project", createTempDir("recent-delegate").toString())
        val action = GoToRecentProjectAction(delegate)
        val event = mock<AnActionEvent>()

        action.actionPerformed(event)

        verify(delegate).actionPerformed(event)
    }

    private fun mockReopenAction(
        name: String?,
        path: String,
    ): ReopenProjectAction {
        val delegate = mock<ReopenProjectAction>()
        whenever(delegate.projectName).thenReturn(name)
        whenever(delegate.projectPath).thenReturn(path)
        return delegate
    }

    private fun createTempDir(prefix: String): Path =
        Files.createTempDirectory(prefix).also {
            it.toFile().deleteOnExit()
        }
}
