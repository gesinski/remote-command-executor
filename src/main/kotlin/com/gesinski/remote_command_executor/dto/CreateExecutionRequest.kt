package com.gesinski.remote_command_executor.dto

data class CreateExecutionRequest(
    val command: String,
    val timeoutSeconds: Long
)