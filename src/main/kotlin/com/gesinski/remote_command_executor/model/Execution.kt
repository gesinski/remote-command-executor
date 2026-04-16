package com.gesinski.remote_command_executor.model

import java.time.LocalDateTime
import java.util.UUID
import jakarta.persistence.*

@Entity
@Table(name = "executions")
data class Execution(
    @Id
    val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val timeoutSeconds: Long,
    @Enumerated(EnumType.STRING)
    var status: ExecutionStatus = ExecutionStatus.QUEUED,
    var output: String? = null
)

enum class ExecutionStatus {
    QUEUED,
    IN_PROGRESS,
    FINISHED,
    FAILED
}