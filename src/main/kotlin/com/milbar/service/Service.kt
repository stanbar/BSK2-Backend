package com.milbar.service

import com.j256.ormlite.dao.Dao

abstract class Service<T, out D : Dao<T, Long>> {
    abstract val dao: D

    fun findById(id: Long): T? = dao.queryForId(id)

    fun findBy(selector: String, value: String): T? = dao.queryForEq(selector, value).firstOrNull()

    fun getAll(): List<T> = dao.queryForAll()

}