package org.github.erikzielke.gotoproject

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.BitUtil.isSet
import com.intellij.util.BitUtil.set
import java.awt.Frame
import java.awt.event.KeyEvent

internal class GoToProjectWindowAction(
    var project: Project,
) : DumbAwareToggleAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    init {
        templatePresentation.setText(project.name, false)
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
        val frameState = projectFrame.extendedState
        if (SystemInfoRt.isMac && isSet(projectFrame.extendedState, Frame.ICONIFIED) && e.inputEvent is KeyEvent) {
            // On Mac minimized window should not be restored this way
            return
        }

        if (isSet(frameState, Frame.ICONIFIED)) {
            // restore the frame if it is minimized
            projectFrame.setExtendedState(set(frameState, Frame.ICONIFIED, false))
        }

        projectFrame.toFront()
        IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown {
            val mostRecentFocusOwner = projectFrame.mostRecentFocusOwner
            if (mostRecentFocusOwner != null) {
                IdeFocusManager.getGlobalInstance().requestFocus(mostRecentFocusOwner, true)
            }
        }
    }
}
