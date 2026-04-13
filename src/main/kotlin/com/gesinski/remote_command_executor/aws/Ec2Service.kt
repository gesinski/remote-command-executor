package com.gesinski.remote_command_executor.aws

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest
import software.amazon.awssdk.services.ec2.model.InstanceStateName

@Service
class Ec2Service(
    private val ec2Client: Ec2Client,

    @Value("\${ec2.ami-id}")
    private val amiId: String,

    @Value("\${ec2.instance-type}")
    private val instanceType: String,

    @Value("\${ec2.key-name}")
    private val keyName: String
) {

    private val logger = LoggerFactory.getLogger(Ec2Service::class.java)

    fun createInstance(): String {
        require(keyName.isNotBlank()) { "EC2 key-name is blank!" }

        val request = RunInstancesRequest.builder()
            .imageId(amiId)
            .instanceType(instanceType)
            .maxCount(1)
            .minCount(1)
            .keyName(keyName)
            .securityGroupIds("sg-0c03524d791de6717")
            .build()

        val response = ec2Client.runInstances(request)

        val instanceId = response.instances().first().instanceId()
        logger.info("EC2 created: $instanceId")

        return instanceId
    }

    fun waitUntilRunning(instanceId: String) {
        logger.info("Waiting for EC2 to reach RUNNING state: $instanceId")

        repeat(60) { attempt ->
            val state = ec2Client.describeInstances {
                it.instanceIds(instanceId)
            }.reservations()
                .first()
                .instances()
                .first()
                .state()
                .name()

            logger.info("EC2 state [$attempt]: $state")

            if (state == InstanceStateName.RUNNING) {
                logger.info("EC2 is RUNNING: $instanceId")
                return
            }

            Thread.sleep(2000)
        }

        throw RuntimeException("EC2 did not reach RUNNING state in time: $instanceId")
    }

    fun waitForPublicIp(instanceId: String): String {
        logger.info("Waiting for public IP: $instanceId")

        repeat(60) { attempt ->
            val instance = ec2Client.describeInstances {
                it.instanceIds(instanceId)
            }.reservations()
                .first()
                .instances()
                .first()

            val ip = instance.publicIpAddress()

            if (!ip.isNullOrBlank()) {
                logger.info("EC2 public IP ready: $ip")
                return ip
            }

            logger.info("IP not ready yet [$attempt]...")
            Thread.sleep(2000)
        }

        throw RuntimeException("Public IP not assigned in time for $instanceId")
    }

    fun terminate(instanceId: String) {
        logger.info("Terminating EC2: $instanceId")

        ec2Client.terminateInstances {
            it.instanceIds(instanceId)
        }
    }
}