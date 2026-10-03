package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText

/**
 * Tests for GoToProjectWindowAction
 */
@Suppress("TooManyFunctions")
class GoToProjectWindowActionTest : BasePlatformTestCase() {
    fun testDisplayTextIsProjectNameWhenNotAGitRepository() {
        val projectDir = createTempDir("window-action-no-git")
        val mockProject = mockProject("My Project", projectDir.toString())

        assertEquals("My Project", GoToProjectWindowAction.Util.displayText(mockProject))
    }

    fun testDisplayTextIsProjectNameWhenBasePathIsNull() {
        val mockProject = mockProject("My Project", null)

        assertEquals("My Project", GoToProjectWindowAction.Util.displayText(mockProject))
    }

    fun testDisplayTextIncludesBranchName() {
        val projectDir = createTempDir("window-action-git")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("ref: refs/heads/main\n")
        val mockProject = mockProject("My Project", projectDir.toString())

        assertEquals("My Project [main]", GoToProjectWindowAction.Util.displayText(mockProject))
    }

    fun testDisplayTextIncludesWorktreeSuffix() {
        val mainRepo = createTempDir("window-action-main")
        val worktreeGitDir = (mainRepo / ".git" / "worktrees" / "wt").createDirectories()
        (worktreeGitDir / "HEAD").writeText("ref: refs/heads/feature\n")
        val worktreeDir = createTempDir("window-action-worktree")
        (worktreeDir / ".git").writeText("gitdir: $worktreeGitDir\n")
        val mockProject = mockProject("My Project", worktreeDir.toString())

        assertEquals("My Project [feature] (worktree)", GoToProjectWindowAction.Util.displayText(mockProject))
    }

    fun testTemplatePresentationUsesDisplayText() {
        val mockProject = mockProject("Presented Project", null)

        val action = GoToProjectWindowAction(mockProject)

        assertEquals("Presented Project", action.templatePresentation.text)
    }

    fun testActionUpdateThreadIsBackground() {
        val action = GoToProjectWindowAction(mockProject("Project", null))

        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    fun testIsSelectedWhenEventProjectIsActionProject() {
        val mockProject = mockProject("Project", null)
        val action = GoToProjectWindowAction(mockProject)
        val event = mockEvent(mockProject)

        assertTrue(action.isSelected(event))
    }

    fun testIsNotSelectedWhenEventProjectIsAnotherProject() {
        val action = GoToProjectWindowAction(mockProject("Project", null))
        val event = mockEvent(mockProject("Other", null))

        assertFalse(action.isSelected(event))
    }

    fun testIsNotSelectedWhenEventHasNoProject() {
        val action = GoToProjectWindowAction(mockProject("Project", null))
        val event = mockEvent(null)

        assertFalse(action.isSelected(event))
    }

    fun testSetSelectedDoesNothingWhenProjectHasNoFrame() {
        val mockProject = mockProject("Project", null)
        val action = GoToProjectWindowAction(mockProject)

        // In the headless test environment there is no frame for the project, so this must not throw.
        action.setSelected(mockEvent(mockProject), true)
    }

    private fun mockProject(
        name: String,
        basePath: String?,
    ): Project {
        val mockProject = mock<Project>()
        whenever(mockProject.name).thenReturn(name)
        whenever(mockProject.basePath).thenReturn(basePath)
        return mockProject
    }

    private fun mockEvent(eventProject: Project?): AnActionEvent {
        val event = mock<AnActionEvent>()
        whenever(event.getData(CommonDataKeys.PROJECT)).thenReturn(eventProject)
        return event
    }

    private fun createTempDir(prefix: String): Path =
        Files.createTempDirectory(prefix).also {
            it.toFile().deleteOnExit()
        }
}
