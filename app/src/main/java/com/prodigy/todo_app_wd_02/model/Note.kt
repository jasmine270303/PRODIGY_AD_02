package com.prodigy.todo_app_wd_02.model

data class Note(
    val docId: String = "",
    val noteText: String,
    val completed: Boolean = false
)
