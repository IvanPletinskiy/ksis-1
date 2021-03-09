package com.handen.ksis

import java.net.NetworkInterface

@ExperimentalUnsignedTypes
fun main() {
    val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
    val filteredNetworkInterfaces = networkInterfaces.filter {
        it.hardwareAddress != null && it.inetAddresses.toList().isNotEmpty()
    }
    println("Total network interfaces:${filteredNetworkInterfaces.size}")
    filteredNetworkInterfaces.forEach {
        println("=========================")
        val macAddressString = it.hardwareAddress.joinToString(".") { byte ->
            byte.toUByte().toString(16).toUpperCase()
        }
        println("MacAddress:$macAddressString")
        println("Name:${it.displayName}")

        val inetAddresses = it.inetAddresses.toList()
        inetAddresses.forEach { inetAddress ->
            println("IP address:${inetAddress.hostAddress}")
        }
    }

    println()
    println()
    println()
    println("Network nodes:")

    val process = Runtime.getRuntime().exec("arp -a")
    val lines = process.inputStream.bufferedReader(charset("cp866")).lineSequence()
    lines.forEach { line ->
        if (line.contains("динамический")) {
            val array = line.trim().split("\\s+".toRegex())
            println("Network node IP address:${array[0]}, MacAddress:${array[1]}")
        }
    }
}