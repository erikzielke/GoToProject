package org.github.erikzielke.gotoproject

import com.intellij.util.BitUtil.isSet
import com.intellij.util.BitUtil.set
import java.awt.Frame

/** Shared logic for bringing a project window to the front. */
internal object ProjectFrames {
    /**
     * Restores [frame] if it is minimized and brings it to the front.
     *
     * When [fromKeyboardOnMac] is true a minimized frame is left alone, because on macOS a
     * minimized window should not be restored from a keyboard shortcut.
     *
     * @return false if the frame was left alone, true otherwise.
     */
    fun bringToFront(
        frame: Frame,
        fromKeyboardOnMac: Boolean = false,
    ): Boolean {
        val state = frame.extendedState
        val minimized = isSet(state, Frame.ICONIFIED)
        if (minimized && fromKeyboardOnMac) return false
        if (minimized) {
            frame.extendedState = set(state, Frame.ICONIFIED, false)
        }
        frame.toFront()
        return true
    }
}
