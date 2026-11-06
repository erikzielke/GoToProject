package org.github.erikzielke.gotoproject

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.awt.Frame
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectFramesTest {
    @Test
    fun `brings a normal frame to the front without changing its state`() {
        val frame = frameWithState(Frame.NORMAL)

        assertTrue(ProjectFrames.bringToFront(frame))

        verify(frame, never()).extendedState = any()
        verify(frame).toFront()
    }

    @Test
    fun `restores a minimized frame and brings it to the front`() {
        val frame = frameWithState(Frame.ICONIFIED)

        assertTrue(ProjectFrames.bringToFront(frame))

        verify(frame).extendedState = Frame.NORMAL
        verify(frame).toFront()
    }

    @Test
    fun `keeps a minimized frame maximized when restoring it`() {
        val frame = frameWithState(Frame.ICONIFIED or Frame.MAXIMIZED_BOTH)

        ProjectFrames.bringToFront(frame)

        verify(frame).extendedState = Frame.MAXIMIZED_BOTH
    }

    @Test
    fun `leaves a minimized frame alone when opened from the keyboard on macOS`() {
        val frame = frameWithState(Frame.ICONIFIED)

        assertFalse(ProjectFrames.bringToFront(frame, fromKeyboardOnMac = true))

        verify(frame, never()).extendedState = any()
        verify(frame, never()).toFront()
    }

    @Test
    fun `brings a normal frame to the front when opened from the keyboard on macOS`() {
        val frame = frameWithState(Frame.NORMAL)

        assertTrue(ProjectFrames.bringToFront(frame, fromKeyboardOnMac = true))

        verify(frame).toFront()
    }

    private fun frameWithState(state: Int): Frame =
        mock<Frame>().also {
            whenever(it.extendedState).thenReturn(state)
        }
}
