package com.handen.ksis

import java.net.NetworkInterface
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val executor: ExecutorService = Executors.newFixedThreadPool(5)
var ipAddressToPing: String? = null

@ExperimentalUnsignedTypes
fun main() {
    printHostName()
    val networkInterfaces = getFilteredNetworkInterfaces()

    networkInterfaces.forEach {
        val inetAddresses = it.inetAddresses.toList()
        inetAddresses.forEach { inetAddress ->
            ipAddressToPing = inetAddress.hostAddress
        }

        printNetworkInterface(it)
    }

    pingIpAddress()

    val lines = readArpTable()
    val dynamicTableEntries = lines.filter {
        it.contains("динамический")
    }

    val callables = dynamicTableEntries.map {
        val array = it.trim().split("\\s+".toRegex())
        val ipAddress = array[0]
        val macAddress = array[1]
        Callable {
            try {
                val nodeName = sendNsLookupRequest(ipAddress)
                println("Network node IP address:$ipAddress, MacAddress:$macAddress, hostname:$nodeName")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }.toList()

    println("\n\n\nNetwork nodes:")

    executor.invokeAll(callables)
    awaitTerminationAfterShutdown(executor)
}

fun sendNsLookupRequest(ipAddress: String): String {
    val nsLoopupProcessFuture =
        Runtime.getRuntime().exec("nslookup $ipAddress").onExit()
    val nsLookupProcess = nsLoopupProcessFuture.get()
    val hostnameLine = nsLookupProcess.inputStream.bufferedReader(charset("cp866")).lineSequence().first()
    val hostname = hostnameLine.substringAfterLast(":").trim()
    nsLookupProcess.destroy()
    return hostname
}

fun pingIpAddress() {
    val pingFuture = Runtime.getRuntime().exec("ping $ipAddressToPing").onExit()
    pingFuture.get()
}

@ExperimentalUnsignedTypes
fun printNetworkInterface(networkInterface: NetworkInterface) {
    val macAddressString = networkInterface.hardwareAddress.joinToString(".") { byte ->
        byte.toUByte().toString(16).toUpperCase()
    }
    println("MacAddress:$macAddressString")
    println("Name:${networkInterface.displayName}")

    println("=========================")
}

fun getFilteredNetworkInterfaces(): List<NetworkInterface> {
    val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
    val filteredNetworkInterfaces = networkInterfaces.filter {
        it.hardwareAddress != null && it.inetAddresses.toList().isNotEmpty()
    }
    println("Total network interfaces:${filteredNetworkInterfaces.size}")

    return filteredNetworkInterfaces
}

fun printHostName() {
    val hostName =
        Runtime.getRuntime().exec("hostname").inputStream.bufferedReader(charset("cp866"))
            .readLine()
    println("Hostname:$hostName")
    println()
}

fun awaitTerminationAfterShutdown(threadPool: ExecutorService) {
    threadPool.shutdown()
    try {
        if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            threadPool.shutdownNow()
        }
    } catch (ex: InterruptedException) {
        threadPool.shutdownNow()
        Thread.currentThread().interrupt()
    }
}

fun readArpTable(): List<String> {
    val arpProcess = Runtime.getRuntime().exec("arp -a")
    val lines = arpProcess.inputStream.bufferedReader(charset("cp866")).readLines()
    arpProcess.destroy()
    return lines
}