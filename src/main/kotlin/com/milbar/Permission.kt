package com.milbar
object Permission {
    fun from(domain: Domain, action: Action, instance: Any = "*"): String =
            listOf(domain.name.toLowerCase(), action.value.toLowerCase(), instance.toString()).joinToString(":")

    enum class Domain {
        USER, ROLE, SUBJECT, CAR, RENT, REPAIR, MECHANIC
    }

    enum class Action(val value: String, val description: String) {
        ALL("*", "all"),
        CREATE("create", "Create new instance in selected domain"),
        READ("read", "Read from domain"),
        UPDATE("update", "Update instance in domain"),
        DELETE("delete", "Delete instance in domain")
    }
}