package com.example.studynotesapp

import org.junit.Assert.*
import org.junit.Test

class NoteValidatorTest {

    private fun isNoteValid(title: String): Boolean {
        return title.trim().isNotEmpty()
    }

    private fun isReminderTimeValid(time: Long): Boolean {
        return time > System.currentTimeMillis()
    }

    @Test
    fun `note is invalid if title is empty`() {
        val title = ""
        assertFalse(isNoteValid(title))
    }

    @Test
    fun `note is valid if title is non-empty`() {
        val title = "My Note"
        assertTrue(isNoteValid(title))
    }

    @Test
    fun `reminder time is valid if in future`() {
        val futureTime = System.currentTimeMillis() + 60000
        assertTrue(isReminderTimeValid(futureTime))
    }

    @Test
    fun `reminder time is invalid if in past`() {
        val pastTime = System.currentTimeMillis() - 60000
        assertFalse(isReminderTimeValid(pastTime))
    }
}
