package com.milbar.service

import com.milbar.clearTables
import com.milbar.kodein
import org.junit.Before
import org.junit.Test
import org.kodein.di.generic.instance

internal class SubjectServiceTest {
    private val service: SubjectService by kodein.instance()

    @Before
    fun setup() {
        clearTables()
    }

    @Test
    fun testAddSubject() {
        val login = "test"
        val password = "password"

        service.createSubject(login, password)
        assert(service.getAll().size == 1)
    }
    @Test
    fun testCannotAddTwoTheSameSubjects() {
        val login = "test"
        val password = "password"

        service.createSubject(login, password)
        service.createSubject(login, password)
        assert(service.getAll().size == 1)
    }

}