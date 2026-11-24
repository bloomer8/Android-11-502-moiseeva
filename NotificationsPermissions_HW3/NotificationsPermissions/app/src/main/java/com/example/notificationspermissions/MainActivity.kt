package com.example.notificationspermissions

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var notificationManager: NotificationManager
    private var locationResultCallback: ((String) -> Unit)? = null
    private var locationListener: LocationListener? = null
    private val locationHandler = Handler(Looper.getMainLooper())

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Compose повторно проверит разрешение при возвращении на экран.
            if (!granted) {
                Toast.makeText(this, "Разрешение на уведомления не предоставлено", Toast.LENGTH_SHORT).show()
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                getCurrentCoordinates { result ->
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                    locationResultCallback?.invoke(result)
                }
            } else {
                locationResultCallback?.invoke("Пожалуйста, предоставьте разрешение на определение местоположения.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()

        // Если приложение было в фоне, при открытии через уведомление показываем его заголовок.
        val wasInBackground = getPreferences(Context.MODE_PRIVATE)
            .getBoolean(KEY_WAS_IN_BACKGROUND, false)
        val openedFromNotification = intent?.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false) == true
        if (openedFromNotification) {
            val message = if (wasInBackground) {
                intent.getStringExtra(EXTRA_TITLE).orEmpty()
            } else {
                intent.getStringExtra(EXTRA_TEXT).orEmpty()
            }
            if (message.isNotBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotificationsApp(
                        hasNotificationPermission = { hasNotificationPermission() },
                        requestNotificationPermission = { requestNotificationPermission() },
                        openAppNotificationSettings = { openAppNotificationSettings() },
                        sendNotification = { title, message, ongoing, importance ->
                            sendNotification(title, message, ongoing, importance)
                        },
                        activeIds = { getActiveNotificationIds() },
                        editNotification = { id, title, message ->
                            editNotification(id, title, message)
                        },
                        deleteAllNotifications = { deleteAllNotifications() },
                        requestLocation = { callback -> requestLocation(callback) }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        getPreferences(Context.MODE_PRIVATE).edit().putBoolean(KEY_WAS_IN_BACKGROUND, false).apply()
    }

    override fun onStop() {
        getPreferences(Context.MODE_PRIVATE).edit().putBoolean(KEY_WAS_IN_BACKGROUND, true).apply()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
            val wasInBackground = getPreferences(Context.MODE_PRIVATE)
                .getBoolean(KEY_WAS_IN_BACKGROUND, false)
            val message = if (wasInBackground) {
                intent.getStringExtra(EXTRA_TITLE).orEmpty()
            } else {
                intent.getStringExtra(EXTRA_TEXT).orEmpty()
            }
            if (message.isNotBlank()) Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(CHANNEL_MIN, "Минимальная важность", NotificationManager.IMPORTANCE_MIN),
                NotificationChannel(CHANNEL_DEFAULT, "Обычная важность", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_HIGH, "Высокая важность", NotificationManager.IMPORTANCE_HIGH)
            )
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    private fun channelFor(importance: String): String = when (importance) {
        "MIN" -> CHANNEL_MIN
        "HIGH" -> CHANNEL_HIGH
        else -> CHANNEL_DEFAULT
    }

    private fun sendNotification(title: String, message: String, ongoing: Boolean, importance: String): Int {
        val id = getPreferences(Context.MODE_PRIVATE).getInt(KEY_NEXT_ID, 1)
        getPreferences(Context.MODE_PRIVATE).edit().putInt(KEY_NEXT_ID, id + 1).apply()

        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_FROM_NOTIFICATION, true)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, message)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, id, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelFor(importance))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title.ifBlank { "Уведомление" })
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setPriority(
                when (importance) {
                    "MIN" -> NotificationCompat.PRIORITY_MIN
                    "HIGH" -> NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setOngoing(ongoing)
            .setAutoCancel(!ongoing)

        NotificationManagerCompat.from(this).notify(id, builder.build())
        return id
    }

    private fun getActiveNotificationIds(): List<Int> {
        return try {
            notificationManager.activeNotifications.map { it.id }.sorted()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun editNotification(id: Int, title: String, message: String): Boolean {
        if (id !in getActiveNotificationIds()) return false
        val active = notificationManager.activeNotifications.firstOrNull { it.id == id } ?: return false
        val oldNotification = active.notification
        val ongoing = (oldNotification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        val importance = when (oldNotification.channelId) {
            CHANNEL_MIN -> "MIN"
            CHANNEL_HIGH -> "HIGH"
            else -> "DEFAULT"
        }

        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_FROM_NOTIFICATION, true)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, message)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, id, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val updated = NotificationCompat.Builder(this, channelFor(importance))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title.ifBlank { "Уведомление" })
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setOngoing(ongoing)
            .setAutoCancel(!ongoing)
            .build()

        notificationManager.notify(id, updated)
        return true
    }

    private fun deleteAllNotifications() {
        notificationManager.cancelAll()
    }

    private fun requestLocation(callback: (String) -> Unit) {
        locationResultCallback = callback
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentCoordinates(callback)
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @Suppress("MissingPermission")
    private fun getCurrentCoordinates(callback: (String) -> Unit) {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        val provider = providers.firstOrNull { manager.isProviderEnabled(it) }
        if (provider == null) {
            callback("Включите геолокацию на устройстве и попробуйте ещё раз.")
            return
        }

        var completed = false
        fun finish(location: Location?) {
            if (completed) return
            completed = true
            locationListener?.let { manager.removeUpdates(it) }
            locationListener = null
            locationHandler.removeCallbacksAndMessages(null)
            if (location != null) {
                callback("Широта: ${location.latitude}\nДолгота: ${location.longitude}")
            } else {
                callback("Не удалось получить координаты. Проверьте, включена ли геолокация.")
            }
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) = finish(location)
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        locationListener = listener
        try {
            manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            locationHandler.postDelayed({ finish(null) }, 15_000L)
        } catch (_: Exception) {
            finish(null)
        }
    }

    companion object {
        private const val CHANNEL_MIN = "notifications_min"
        private const val CHANNEL_DEFAULT = "notifications_default"
        private const val CHANNEL_HIGH = "notifications_high"
        private const val KEY_NEXT_ID = "next_notification_id"
        private const val KEY_WAS_IN_BACKGROUND = "was_in_background"
        private const val EXTRA_FROM_NOTIFICATION = "from_notification"
        private const val EXTRA_TITLE = "notification_title"
        private const val EXTRA_TEXT = "notification_text"
    }
}

@Composable
private fun NotificationsApp(
    hasNotificationPermission: () -> Boolean,
    requestNotificationPermission: () -> Unit,
    openAppNotificationSettings: () -> Unit,
    sendNotification: (String, String, Boolean, String) -> Int,
    activeIds: () -> List<Int>,
    editNotification: (Int, String, String) -> Boolean,
    deleteAllNotifications: () -> Unit,
    requestLocation: ((String) -> Unit) -> Unit
) {
    var permissionGranted by remember { mutableStateOf(hasNotificationPermission()) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var ongoing by remember { mutableStateOf(false) }
    var importance by remember { mutableStateOf("DEFAULT") }
    var expanded by remember { mutableStateOf(false) }
    var notificationId by remember { mutableStateOf("") }
    var activeNotificationIds by remember { mutableStateOf(activeIds()) }
    var screen by remember { mutableStateOf(false) }
    var locationText by remember { mutableStateOf("Для определения координат нажмите на кнопку") }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission()) {
            requestNotificationPermission()
        }
    }

    // Перепроверяем разрешение и список активных уведомлений после возврата в приложение.
    DisposableEffect(context) {
        val activity = context as ComponentActivity
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = hasNotificationPermission()
                activeNotificationIds = activeIds()
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    if (screen) {
        LocationScreen(
            text = locationText,
            onTextChange = { locationText = it },
            onBack = { screen = false },
            onGetLocation = {
                locationText = "Определяем координаты..."
                requestLocation { locationText = it }
            }
        )
        return
    }

    if (!permissionGranted && android.os.Build.VERSION.SDK_INT >= 33) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Для нормальной работы приложения нужно предоставить разрешение на показ уведомлений.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = openAppNotificationSettings) {
                Text("Открыть настройки приложения")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { requestNotificationPermission() }) {
                Text("Запросить разрешение")
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Уведомления", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Заголовок уведомления") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        OutlinedTextField(
            value = message, onValueChange = { message = it },
            label = { Text("Текст уведомления") },
            modifier = Modifier.fillMaxWidth(), minLines = 3
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = ongoing, onCheckedChange = { ongoing = it })
            Text("Сделать уведомление несмахиваемым")
        }

        Text("Важность уведомления")
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = importance == "MIN", onClick = { importance = "MIN" })
                Text("MIN")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = importance == "DEFAULT", onClick = { importance = "DEFAULT" })
                Text("DEFAULT")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = importance == "HIGH", onClick = { importance = "HIGH" })
                Text("HIGH")
            }
        }

        Button(
            onClick = {
                val id = sendNotification(title, message, ongoing, importance)
                activeNotificationIds = activeIds()
                Toast.makeText(context, "Уведомление отправлено. ID: $id", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && message.isNotBlank()
        ) {
            Text("Показать уведомление")
        }

        OutlinedButton(onClick = { screen = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Второй экран — координаты")
        }

        Spacer(Modifier.height(8.dp))
        Text("Управление активными уведомлениями", style = MaterialTheme.typography.titleMedium)
        Text(
            if (activeNotificationIds.isEmpty()) "Активных уведомлений нет"
            else "Активные ID: ${activeNotificationIds.joinToString()}"
        )
        OutlinedTextField(
            value = notificationId,
            onValueChange = { notificationId = it },
            label = { Text("ID уведомления") },
            modifier = Modifier.fillMaxWidth(),
            enabled = activeNotificationIds.isNotEmpty(),
            singleLine = true
        )
        Button(
            onClick = {
                val id = notificationId.toIntOrNull()
                val result = id != null && editNotification(id, title, message)
                Toast.makeText(
                    context,
                    if (result) "Уведомление обновлено" else "Не найдено активное уведомление с таким ID",
                    Toast.LENGTH_SHORT
                ).show()
                activeNotificationIds = activeIds()
            },
            enabled = activeNotificationIds.isNotEmpty() &&
                notificationId.toIntOrNull() != null &&
                title.isNotBlank() && message.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Редактировать")
        }
        OutlinedButton(
            onClick = {
                deleteAllNotifications()
                activeNotificationIds = activeIds()
                notificationId = ""
            },
            enabled = activeNotificationIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Удалить все")
        }
        OutlinedButton(
            onClick = { activeNotificationIds = activeIds() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Обновить список ID")
        }
    }
}

@Composable
private fun LocationScreen(
    text: String,
    onTextChange: (String) -> Unit,
    onBack: () -> Unit,
    onGetLocation: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Определение координат", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            readOnly = true
        )
        Button(onClick = onGetLocation, modifier = Modifier.fillMaxWidth()) {
            Text("Получить координаты")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Назад")
        }
    }
}
