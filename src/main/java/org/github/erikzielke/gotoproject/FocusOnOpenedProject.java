package org.github.erikzielke.gotoproject;

import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * Created by Guchakov Nikita on 01-05-2019
 */
@RequiredArgsConstructor
public class FocusOnOpenedProject implements WindowFocusListener {

    private final Project project;

    @Override
    public void windowGainedFocus(WindowEvent event) {

    }

    @Override
    public void windowLostFocus(WindowEvent event) {
        GoToProjectApplicationComponent.getInstance()
                                       .setFocusedBefore(project);
    }


}
