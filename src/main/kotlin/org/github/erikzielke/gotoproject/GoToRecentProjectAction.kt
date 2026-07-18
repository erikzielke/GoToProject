package org.github.erikzielke.gotoproject

import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import org.github.erikzielke.gotoproject.git.GitBranchResolver

/**
 * Wraps a [ReopenProjectAction] so the presentation text can include the project's git branch.
 * [ReopenProjectAction.update] recomputes its own text on every popup refresh, so appending the
 * branch to its template presentation directly would just get overwritten.
 */
internal class GoToRecentProjectAction(
    private val delegate: ReopenProjectAction,
) : AnAction(),
    DumbAware {
    init {
        val projectName = delegate.projectName ?: delegate.projectPath
        val branchInfo = GitBranchResolver.resolve(delegate.projectPath)
        val text =
            if (branchInfo != null) {
                val worktreeSuffix = if (branchInfo.isWorktree) " (worktree)" else ""
                "$projectName [${branchInfo.branchName}]$worktreeSuffix"
            } else {
                projectName
            }
        templatePresentation.setText(text, false)
        templatePresentation.icon = RecentProjectsManagerBase.getInstanceEx().getProjectIcon(delegate.projectPath, true)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        delegate.actionPerformed(e)
    }
}
