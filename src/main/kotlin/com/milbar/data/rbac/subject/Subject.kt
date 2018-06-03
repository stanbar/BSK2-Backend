package com.milbar.data.rbac.subject

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.ForeignCollectionField
import com.j256.ormlite.table.DatabaseTable
import com.milbar.data.rbac.subject_role.SubjectRole
import javax.persistence.Column

@DatabaseTable(tableName = "Subject", daoClass = SubjectDaoImpl::class)
class Subject {

    @DatabaseField(generatedId = true)
    var id: Long = -1

    @DatabaseField(unique = true)
    lateinit var login: String

    @Column
    @Transient
    lateinit var password: String


    @ForeignCollectionField(eager = true, maxEagerLevel = 3)
    lateinit var subjectRoles: Collection<SubjectRole>
}
