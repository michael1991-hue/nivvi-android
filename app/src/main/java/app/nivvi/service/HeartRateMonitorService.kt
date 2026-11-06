package app.nivvi.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.nivvi.MainActivity
import app.nivvi.R
import app.nivvi.data.AlertSettings
import app.nivvi.data.AppDatabase
import app.nivvi.data.HeartRateReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class HeartRateMonitorService : Service() {

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var bluetoothGatt: BluetoothGatt? = null
    private var scanner: BluetoothLeScanner? = null

    private var currentHr: Int? = null
    private var currentSpo2: Int? = null
    private var lastSampleTime: Long = 0
    private var lastSnapshotTime: Long = 0
    private var outsideSince: Long = 0
    private var alertActive: Boolean = false
    private var settings = AlertSettings()

    companion object {
        const val CHANNEL_ID = "nivvi_monitor"
        const val NOTIFICATION_ID = 1
        val HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val PULSE_OX_SERVICE = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")
        val PLX_CONTINUOUS = UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        var latestHr: Int? = null
        var latestSpo2: Int? = null
        var connectionState: String = "Disconnected"
    }

    inner class LocalBinder : Binder() {
        fun getService(): HeartRateMonitorService = this@HeartRateMonitorService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    fun updateSettings(newSettings: AlertSettings) {
        settings = newSettings
        outsideSince = 0
        alertActive = false
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_notification_title))
            .setContentText(getString(R.string.foreground_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Nivvi monitoring", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Keeps Bluetooth heart-rate monitoring alive while the screen is locked" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun startScan() {
        connectionState = "Scanning"
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        scanner = bluetoothManager.adapter?.bluetoothLeScanner
        scanner?.startScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val uuids = result.scanRecord?.serviceUuids
            if (uuids?.any { it.uuid == HEART_RATE_SERVICE || it.uuid == PULSE_OX_SERVICE } == true) {
                scanner?.stopScan(this)
                connectionState = "Connecting"
                result.device.connectGatt(this@HeartRateMonitorService, false, gattCallback)
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                bluetoothGatt = gatt
                connectionState = "Connected"
                gatt.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectionState = "Disconnected"
                latestHr = null
                latestSpo2 = null
                outsideSince = 0
                alertActive = false
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            enableNotifications(gatt, HEART_RATE_SERVICE, HEART_RATE_MEASUREMENT)
            enableNotifications(gatt, PULSE_OX_SERVICE, PLX_CONTINUOUS)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            when (characteristic.uuid) {
                HEART_RATE_MEASUREMENT -> handleHeartRate(characteristic)
                PLX_CONTINUOUS -> handlePulseOx(characteristic)
            }
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, serviceUuid: UUID, charUuid: UUID) {
        val characteristic = gatt.getService(serviceUuid)?.getCharacteristic(charUuid) ?: return
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }

    private fun handleHeartRate(characteristic: BluetoothGattCharacteristic) {
        val hr = parseHeartRate(characteristic)
        if (hr <= 0) return
        currentHr = hr
        latestHr = hr
        lastSampleTime = System.currentTimeMillis()
        evaluateAlerts(hr)
        maybeSnapshot(hr, currentSpo2)
        updateNotification(hr)
    }

    private fun handlePulseOx(characteristic: BluetoothGattCharacteristic) {
        val value = characteristic.value ?: return
        if (value.size >= 5) {
            val spo2 = value[4].toInt() and 0xFF
            if (spo2 in 70..100) {
                currentSpo2 = spo2
                latestSpo2 = spo2
            }
        }
    }

    private fun parseHeartRate(characteristic: BluetoothGattCharacteristic): Int {
        val flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) ?: return 0
        if ((flag and 0x02) != 0 && (flag and 0x04) == 0) return 0
        val format = if ((flag and 0x01) == 0) BluetoothGattCharacteristic.FORMAT_UINT8 else BluetoothGattCharacteristic.FORMAT_UINT16
        return characteristic.getIntValue(format, 1) ?: 0
    }

    private fun evaluateAlerts(hr: Int) {
        val now = System.currentTimeMillis()
        val highBreach = settings.highEnabled && hr > settings.highLimit
        val lowBreach = settings.lowEnabled && hr < settings.lowLimit
        val outside = highBreach || lowBreach

        if (!outside) {
            if (alertActive) alertActive = false
            outsideSince = 0
            return
        }

        if (outsideSince == 0L) outsideSince = now
        if (now - outsideSince >= settings.dwellSeconds * 1000L && !alertActive) {
            alertActive = true
        }
    }

    private fun maybeSnapshot(hr: Int, spo2: Int?) {
        val now = System.currentTimeMillis()
        if (lastSnapshotTime == 0L || now - lastSnapshotTime >= 30_000) {
            lastSnapshotTime = now
            scope.launch {
                AppDatabase.get(this@HeartRateMonitorService).heartRateDao().insert(
                    HeartRateReading(timestamp = now, heartRate = hr, oxygen = spo2)
                )
            }
        }
    }

    private fun updateNotification(hr: Int) {
        val text = if (currentSpo2 != null) "$hr bpm · ${currentSpo2}% SpO₂" else "$hr bpm"
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(text)
            .setContentText("Nivvi is monitoring")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        bluetoothGatt?.close()
        scanner?.stopScan(scanCallback)
        super.onDestroy()
    }
}
