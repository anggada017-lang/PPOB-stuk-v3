package com.angga.cetakstruk

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import java.io.OutputStream
import java.util.UUID

class PrinterManager {
    private val adapter = BluetoothAdapter.getDefaultAdapter()
    private var out: OutputStream? = null
    private var socket: android.bluetooth.BluetoothSocket? = null
    private val spp = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun pairedDevices(): List<BluetoothDevice> =
        adapter?.bondedDevices?.toList()?.sortedBy { it.name ?: it.address } ?: emptyList()

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean = try {
        socket?.close()
        socket = device.createRfcommSocketToServiceRecord(spp)
        adapter?.cancelDiscovery()
        socket!!.connect()
        out = socket!!.outputStream
        true
    } catch (_: Exception) {
        false
    }

    fun isConnected() = out != null

    fun print(text: String): Boolean = try {
        val o = out ?: return false
        o.write(byteArrayOf(0x1B, 0x40)) // initialize
        o.write(byteArrayOf(0x1B, 0x61, 0x00)) // left
        o.write(text.toByteArray(Charsets.UTF_8))
        o.write("\n\n\n".toByteArray(Charsets.UTF_8))
        o.flush()
        true
    } catch (_: Exception) {
        false
    }

    fun disconnect() {
        try { out?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        out = null
        socket = null
    }
}
