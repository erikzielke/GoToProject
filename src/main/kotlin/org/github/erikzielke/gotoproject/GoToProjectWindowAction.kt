package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.wm.WindowManager
import org.github.erikzielke.gotoproject.git.GitBranchResolver
import java.awt.event.KeyEvent

internal class GoToProjectWindowAction(
    var project: Project,
) : DumbAwareToggleAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    init {
        templatePresentation.setText(Util.displayText(project), false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        e.getData(CommonDataKeys.PROJECT)?.let {
            return it == project
        }
        return false
    }

    override fun setSelected(
        e: AnActionEvent,
        p1: Boolean,
    ) {
        val projectFrame = WindowManager.getInstance().getFrame(project) ?: return
        ProjectFrames.bringToFront(projectFrame, fromKeyboardOnMac = SystemInfoRt.isMac && e.inputEvent is KeyEvent)
    }

    object Util {
        fun displayText(project: Project): String {
            val branchInfo = GitBranchResolver.resolve(project.basePath) ?: return project.name
            val worktreeSuffix = if (branchInfo.isWorktree) " (worktree)" else ""
            return "${project.name} [${branchInfo.branchName}]$worktreeSuffix"
        }
    }
}
