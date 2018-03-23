package domain.client

import java.util.*

data class ClientEntry(val PESEL: String, val name: String, val lastname: String, val driverLicence: Long, val validDate: Date)
