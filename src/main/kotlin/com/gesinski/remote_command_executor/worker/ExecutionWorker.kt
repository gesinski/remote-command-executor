package com.gesinski.remote_command_executor.worker

import com.gesinski.remote_command_executor.model.Execution
import com.gesinski.remote_command_executor.model.ExecutionStatus
import com.gesinski.remote_command_executor.service.ExecutionService
import com.gesinski.remote_command_executor.aws.Ec2Service
import com.gesinski.remote_command_executor.aws.SshService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ExecutionWorker(
    private val executionService: ExecutionService,
    private val ec2Service: Ec2Service,
    private val sshService: SshService
) {

    private val logger = LoggerFactory.getLogger(ExecutionWorker::class.java)

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    fun execute() {
        val queuedTasks = executionService.getQueuedExecutions()
            .filter { it.status == ExecutionStatus.QUEUED }

        for (task in queuedTasks) {
            processTask(task)
        }
    }

    private fun processTask(task: Execution) {

        task.status = ExecutionStatus.IN_PROGRESS
        executionService.save(task)

        logger.info("Starting task ${task.id}")

        var instanceId: String? = null

        try {
            instanceId = ec2Service.createInstance()
            logger.info("EC2 created: $instanceId")

            ec2Service.waitUntilRunning(instanceId)

            val ip = ec2Service.waitForPublicIp(instanceId)
            logger.info("EC2 IP: $ip")

            sshService.waitForSshReady(
                ip = ip,
                user = "ubuntu"
            )

            val output = sshService.executeCommand(ip, task.command)

            task.output = output
            task.status = ExecutionStatus.FINISHED
            executionService.save(task)

            logger.info("Task ${task.id} finished successfully")

        } catch (e: Exception) {

            logger.error("Task ${task.id} failed", e)

            task.status = ExecutionStatus.FAILED
            task.output = e.message
            executionService.save(task)

        } finally {
            instanceId?.let {
                try {
                    ec2Service.terminate(it)
                    logger.info("EC2 terminated: $it")
                } catch (e: Exception) {
                    logger.warn("Failed to terminate EC2: $it", e)
                }
            }
        }
    }
}