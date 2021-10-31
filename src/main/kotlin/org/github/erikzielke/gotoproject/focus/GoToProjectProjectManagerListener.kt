package org.github.erikzielke.gotoproject.focus

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.wm.WindowManager
import javax.swing.JFrame

class GoToProjectProjectManagerListener : ProjectManagerListener {
    private val listeners = mutableMapOf<Project, ProjectWindowFocusListener>()

    override fun projectOpened(project: Project) {
        val projectFrame: JFrame? = WindowManager.getInstance().getFrame(project)
        val listener = ProjectWindowFocusListener(project)
        listeners[project] = listener
        projectFrame?.addWindowFocusListener(listener)
    }

    override fun projectClosed(project: Project) {
        val listener = listeners[project]
        WindowManager.getInstance().getFrame(project)?.removeWindowFocusListener(listener)
    }
}
