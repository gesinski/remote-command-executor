package com.gesinski.remote_command_executor

import com.gesinski.remote_command_executor.model.Execution
import com.gesinski.remote_command_executor.model.ExecutionStatus
import com.gesinski.remote_command_executor.service.ExecutionService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.Scanner

@SpringBootApplication
class ConsoleApp

fun main() {
    val context = runApplication<ConsoleApp>(*arrayOf()) {
        setAdditionalProfiles("cli")
    }
    val executionService = context.getBean(ExecutionService::class.java)

    val scanner = Scanner(System.`in`)
    println("=== Remote Command Executor ===")

    while (true) {
        print("Write command to execute (or 'q' to quit): ")
        val command = scanner.nextLine()
        if (command.lowercase() == "q") break

        val execution: Execution = executionService.createExecution(command, cpuCount = 1)
        println("Task created. ID: ${execution.id}, status: ${execution.status}")

        while (true) {
            val updated = executionService.getExecution(execution.id)!!
            print("\rStatus: ${updated.status}   ")
            if (updated.status == ExecutionStatus.FINISHED || updated.status == ExecutionStatus.FAILED) {
                println("\nCommand output:\n${updated.output ?: "<None>"}")
                break
            }
            Thread.sleep(1000)
        }
    }

    println("Exiting ConsoleApp. Goodbye!")
}