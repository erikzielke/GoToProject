package org.github.erikzielke.gotoproject.focus

import com.intellij.openapi.project.Project
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent.Companion.instance
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

class ProjectWindowFocusListener(private val project: Project) : WindowFocusListener {
    override fun windowGainedFocus(e: WindowEvent) {
        if (instance.lastFocusLost != null && instance.lastFocusLost!!.name != project.name) {
            instance.focusedBefore = instance.lastFocusLost
            instance.lastFocusLost = null
        }
    }
    override fun windowLostFocus(e: WindowEvent) {
        instance.lastFocusLost = project
    }
}
