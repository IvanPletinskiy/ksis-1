package com.handen.ksis

import java.net.InetAddress
import java.net.NetworkInterface

@ExperimentalUnsignedTypes
fun main() {
    val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
    val filteredNetforkInterfaces = networkInterfaces.filter {
        it.hardwareAddress != null && it.inetAddresses.toList().isNotEmpty()
    }
    println("Total network interfaces:${filteredNetforkInterfaces.size}")
    filteredNetforkInterfaces.forEach {
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
}