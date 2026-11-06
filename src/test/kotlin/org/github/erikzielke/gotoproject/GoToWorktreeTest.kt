package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.github.erikzielke.gotoproject.git.GitWorktreeResolver.Worktree
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText

class GoToWorktreeTest : BasePlatformTestCase() {
    fun testIsDumbAwareAndUsesBackgroundThread() {
        val action = GoToWorktree()
        assertTrue(action.isDumbAware)
        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    fun testDisplayText() {
        val main = Worktree(Path.of("/x/repo"), "main", isMain = true)
        assertEquals("repo [main] (main)", GoToWorktree.displayText(main))
        assertEquals("wt [feature]", GoToWorktree.displayText(Worktree(Path.of("/x/wt"), "feature", isMain = false)))
        assertEquals("wt", GoToWorktree.displayText(Worktree(Path.of("/x/wt"), null, isMain = false)))
    }

    fun testGroupSplitsOpenAndNotOpenWorktrees() {
        val root = Files.createTempDirectory("goto-worktree").also { it.toFile().deleteOnExit() }
        val main = (root / "repo").createDirectories()
        (main / ".git").createDirectories()
        (main / ".git" / "HEAD").writeText("ref: refs/heads/main\n")
        val other = (root / "other").createDirectories()
        val openProject = mock<Project>()
        whenever(openProject.basePath).thenReturn(main.toString())
        whenever(openProject.name).thenReturn("repo")

        val group =
            GoToWorktree.buildGroup(
                listOf(Worktree(main, "main", isMain = true), Worktree(other, "feature", isMain = false)),
                listOf(openProject),
            )

        val children = group.getChildren(null)
        assertEquals(4, children.size)
        assertTrue(children[0] is Separator)
        assertTrue(children[1] is GoToProjectWindowAction)
        assertSame(openProject, (children[1] as GoToProjectWindowAction).project)
        assertTrue(children[2] is Separator)
        assertTrue(children[3] is OpenWorktreeAction)
        assertEquals("other [feature]", children[3].templatePresentation.text)
    }
}
