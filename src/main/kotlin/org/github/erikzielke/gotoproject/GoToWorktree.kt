package org.github.erikzielke.gotoproject

import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import org.github.erikzielke.gotoproject.git.GitWorktreeResolver
import org.github.erikzielke.gotoproject.git.GitWorktreeResolver.Worktree
import java.io.IOException
import java.nio.file.Path

/**
 * Shows a speed-search popup with every worktree of the current project's git repository.
 * Worktrees that are already open focus their window; the others are opened like a recent project.
 */
class GoToWorktree : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null && GitWorktreeResolver.listWorktrees(project.basePath).isNotEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val worktrees = GitWorktreeResolver.listWorktrees(project.basePath)
        if (worktrees.isEmpty()) return

        val group = buildGroup(worktrees, ProjectManager.getInstance().openProjects.toList())
        JBPopupFactory
            .getInstance()
            .createActionGroupPopup(
                GoToProjectBundle.message("popup.worktrees.title"),
                group,
                e.dataContext,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true,
            ).showCenteredInCurrentWindow(project)
    }

    internal companion object {
        fun buildGroup(
            worktrees: List<Worktree>,
            openProjects: List<Project>,
        ): DefaultActionGroup {
            val openByRoot =
                openProjects
                    .filterNot { it.isDisposed }
                    .mapNotNull { project ->
                        GitWorktreeResolver.findWorktreeRoot(project.basePath)?.let { pathKey(it) to project }
                    }.toMap()
            val (open, notOpen) = worktrees.partition { pathKey(it.path) in openByRoot }

            return DefaultActionGroup().apply {
                if (open.isNotEmpty()) {
                    addSeparator(GoToProjectBundle.message("popup.worktrees.open"))
                    open.forEach { add(GoToProjectWindowAction(openByRoot.getValue(pathKey(it.path)))) }
                }
                if (notOpen.isNotEmpty()) {
                    addSeparator(GoToProjectBundle.message("popup.worktrees.notOpen"))
                    notOpen.forEach { add(OpenWorktreeAction(it)) }
                }
            }
        }

        fun displayText(worktree: Worktree): String {
            val name = worktree.path.fileName?.toString() ?: worktree.path.toString()
            val branch = worktree.branchName?.let { " [$it]" }.orEmpty()
            val mainSuffix = if (worktree.isMain) " (main)" else ""
            return "$name$branch$mainSuffix"
        }

        /** Compares paths by their real location so symlinked temp dirs and `..` segments still match. */
        @Suppress("SwallowedException")
        fun pathKey(path: Path): Path =
            try {
                path.toRealPath()
            } catch (e: IOException) {
                path.toAbsolutePath().normalize()
            }
    }
}

/** Opens a worktree that has no project window yet, honouring the IDE's "open in new window" preference. */
internal class OpenWorktreeAction(
    private val worktree: Worktree,
) : AnAction(),
    DumbAware {
    init {
        templatePresentation.setText(GoToWorktree.displayText(worktree), false)
        templatePresentation.description = worktree.path.toString()
        templatePresentation.icon =
            RecentProjectsManagerBase.getInstanceEx().getProjectIcon(worktree.path.toString(), true)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val path = worktree.path.toString()
        val name = worktree.path.fileName?.toString() ?: path
        ReopenProjectAction(path, name, GoToWorktree.displayText(worktree)).actionPerformed(e)
    }
}
