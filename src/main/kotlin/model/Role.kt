package model

class Role(
        val id: Long,
        val name: String,
        val description: String,
        val permissions: Set<String>)