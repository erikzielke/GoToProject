package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.WindowManager
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent.Companion.instance

class GoToLastProject : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val focusedBefore = instance.focusedBefore ?: return
        val manager = WindowManager.getInstance()
        val jFrame = manager.getFrame(focusedBefore)
        jFrame?.requestFocus()
    }

    override fun update(e: AnActionEvent) {
        val shouldBeEnabled = instance.focusedBefore != null
        e.presentation.isEnabled = shouldBeEnabled
    }

    override fun isDumbAware() = true
}
