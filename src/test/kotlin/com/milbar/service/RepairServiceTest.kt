package com.milbar.service

import com.milbar.kodein
import org.junit.Test
import org.kodein.di.generic.instance

internal class RepairServiceTest {


    val service: RepairService by kodein.instance()

    @Test
    fun testAddRepair() {

    }
}