package org.github.erikzielke.gotoproject;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * Created by Erik on 20-02-14.
 */
public class GoToProjectProjectComponent extends AbstractProjectComponent {
    private WindowFocusListener projectFocusListener;


    protected GoToProjectProjectComponent(Project project) {
        super(project);
    }

    @Override
    public void projectOpened() {
        JFrame projectFrame = WindowManager.getInstance().getFrame(myProject);
        projectFocusListener = new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {

            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                GoToProjectApplicationComponent.getInstance().setFocusedBefore(myProject);
            }
        };
        projectFrame.addWindowFocusListener(projectFocusListener);
    }

    @Override
    public void projectClosed() {
        JFrame projectFrame = WindowManager.getInstance().getFrame(myProject);
        if(projectFrame != null) {
            projectFrame.removeWindowFocusListener(projectFocusListener);
        }
    }
}
