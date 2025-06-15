package org.github.erikzielke.gotoproject.focus

import com.intellij.openapi.project.Project
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent.Companion.instance
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

class ProjectWindowFocusListener(
    private val project: Project,
    private val component: GoToProjectApplicationComponent = instance,
) : WindowFocusListener {
    override fun windowGainedFocus(e: WindowEvent) {
        if (component.lastFocusLost != null && component.lastFocusLost!!.name != project.name) {
            component.focusedBefore = component.lastFocusLost
            component.lastFocusLost = null
        }
    }

    override fun windowLostFocus(e: WindowEvent) {
        component.lastFocusLost = project
    }
}
