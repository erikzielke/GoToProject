package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.WindowManager
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent.Companion.instance

class GoToLastProject : AnAction() {
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val focusedBefore = instance.focusedBefore ?: return
        val manager = WindowManager.getInstance()
        val jFrame = manager.getFrame(focusedBefore)
        jFrame?.requestFocus()
    }

    override fun isDumbAware() = true
}
