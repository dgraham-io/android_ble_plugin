package com.goshawkgames.androidbleplugin

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

class AndroidBLEPlugin (godot: Godot) : GodotPlugin(godot) {
    private val activity = godot.getActivity()
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager =
            activity?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter
    }

    @UsedByGodot
    override fun getPluginName(): String {
        return "AndroidBLEPlugin"
    }

    override fun getPluginSignals(): Set<SignalInfo> {
        return setOf(
            SignalInfo("heart_rate_raw_data", ByteArray::class.java), // New signal for raw data
            SignalInfo(
                "bike_data_updated",
                Float::class.java,
                Float::class.java,
                Integer::class.java
            ),
            SignalInfo("permission_required", String::class.java), // Emits String
            SignalInfo("device_found", String::class.java, String::class.java),
            SignalInfo("service_found", String::class.java),
            SignalInfo("scan_failed", Integer::class.java),
            SignalInfo("plugin_message", String::class.java)
        )
    }

    // Permission verification
    fun hasBluetoothScanPermission(): Boolean {
        return activity?.let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } == true
    }

    fun hasBluetoothConnectPermission(): Boolean {
        return activity?.let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } == true
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT])
    @UsedByGodot
    fun bluetoothReady(): Boolean {
        if (bluetoothAdapter == null) {
            emitSignal("plugin_message","cannot find bluetooth adapter")
            return false
        }

        if (!bluetoothAdapter!!.isEnabled) {
            emitSignal("plugin_message","bluetooth not enabled")
            return false
        }
        // Verify permissions
        if (!hasBluetoothScanPermission()) {
            emitSignal("permission_required", Manifest.permission.BLUETOOTH_SCAN)
            return false
        }

        if (!hasBluetoothConnectPermission()) {
            emitSignal("permission_required", Manifest.permission.BLUETOOTH_CONNECT)
            return false
        }

        // Bluetooth initialized
        return true
    }
}