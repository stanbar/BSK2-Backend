package com.milbar.service.exception

class SubjectAlreadyExist(login: String) : Throwable("Subject with login: \"$login\" already exists")
