package com.gesinski.remote_command_executor.aws

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest

@Service
class Ec2Service(
    private val ec2Client: Ec2Client,
    @Value("\${ec2.ami-id}") private val amiId: String,
    @Value("\${ec2.instance-type}") private val instanceType: String,
    @Value("\${ec2.key-name}") private val keyName: String
) {

    fun createInstance(): String {
        val request = RunInstancesRequest.builder()
            .imageId(amiId)
            .instanceType(instanceType)
            .maxCount(1)
            .minCount(1)
            .keyName(keyName)
            .build()

        val response = ec2Client.runInstances(request)
        return response.instances()[0].instanceId()
    }

    fun waitUntilRunning(instanceId: String) {
        while (true) {
            val state = ec2Client.describeInstances {
                it.instanceIds(instanceId)
            }.reservations()[0].instances()[0].state().nameAsString()

            if (state == "running") break

            Thread.sleep(500)
        }
    }

    fun getPublicIp(instanceId: String): String {
        return ec2Client.describeInstances {
            it.instanceIds(instanceId)
        }.reservations()[0].instances()[0].publicIpAddress()
    }

    fun terminate(instanceId: String) {
        ec2Client.terminateInstances {
            it.instanceIds(instanceId)
        }
    }
}