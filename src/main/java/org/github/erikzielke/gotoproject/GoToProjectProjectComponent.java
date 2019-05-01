package org.github.erikzielke.gotoproject;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import java.awt.event.WindowFocusListener;
import java.util.Optional;

/**
 * Created by Erik on 20-02-14.
 */
public class GoToProjectProjectComponent implements ProjectComponent {
    private final Project project;
    private WindowFocusListener projectFocusListener;


    protected GoToProjectProjectComponent(Project project) {
        this.project = project;
        this.projectFocusListener = new FocusOnOpenedProject(project);
    }

    @Override
    public void projectOpened() {
        Optional.ofNullable(WindowManager.getInstance().getFrame(project))
                .ifPresent(frame -> frame.addWindowFocusListener(projectFocusListener));
    }

    @Override
    public void projectClosed() {

        Optional.ofNullable(WindowManager.getInstance())
                .map(manager -> manager.getFrame(project))
                .ifPresent(frame -> frame.removeWindowFocusListener(projectFocusListener));
    }

}
