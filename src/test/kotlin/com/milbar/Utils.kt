package com.milbar

import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.milbar.data.domain.car.Car
import com.milbar.data.domain.mechanic.Mechanic
import com.milbar.data.domain.rent.Rent
import com.milbar.data.domain.repair.Repair
import com.milbar.data.domain.user.User
import com.milbar.data.rbac.role.Role
import com.milbar.data.rbac.rolepermission.RolePermission
import com.milbar.data.rbac.subject.Subject
import com.milbar.data.rbac.subject_role.SubjectRole
import org.kodein.di.generic.instance

fun clearTables() {
    val connectionSourceDomain: ConnectionSource by kodein.instance()

    with(connectionSourceDomain) {
        TableUtils.createTableIfNotExists(this, Car::class.java)
        TableUtils.createTableIfNotExists(this, Mechanic::class.java)
        TableUtils.createTableIfNotExists(this, Rent::class.java)
        TableUtils.createTableIfNotExists(this, Repair::class.java)
        TableUtils.createTableIfNotExists(this, User::class.java)

        TableUtils.createTableIfNotExists(this, Role::class.java)
        TableUtils.createTableIfNotExists(this, RolePermission::class.java)
        TableUtils.createTableIfNotExists(this, Subject::class.java)
        TableUtils.createTableIfNotExists(this, SubjectRole::class.java)
        
        //TEST

        TableUtils.clearTable(this, Car::class.java)
        TableUtils.clearTable(this, Mechanic::class.java)
        TableUtils.clearTable(this, Rent::class.java)
        TableUtils.clearTable(this, Repair::class.java)
        TableUtils.clearTable(this, User::class.java)

        TableUtils.clearTable(this, Role::class.java)
        TableUtils.clearTable(this, RolePermission::class.java)
        TableUtils.clearTable(this, Subject::class.java)
        TableUtils.clearTable(this, SubjectRole::class.java)

    }
}