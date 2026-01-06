package com.example.screenonapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val requestPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Settings.System.canWrite(this)) {
            Toast.makeText(this, "Quyền được cấp", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Quyền bị từ chối", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeoutApp {
                if (!Settings.System.canWrite(this)) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    requestPermission.launch(intent)
                }
            }
        }
    }
}

@Composable
fun ScreenTimeoutApp(onRequestPermission: () -> Unit) {
    val context = LocalContext.current
    var selectedTimeout by remember { mutableStateOf(30000L) }
    val timeoutOptions = listOf(
        30000L to "30 giây",
        60000L to "1 phút",
        120000L to "2 phút",
        300000L to "5 phút",
        600000L to "10 phút",
        900000L to "15 phút",
        1200000L to "20 phút",
        1500000L to "25 phút",
        1800000L to "30 phút"
    )
    var customTimeout by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Chọn thời gian giữ màn hình sáng", style = MaterialTheme.typography.headlineSmall)

        timeoutOptions.forEach { (value, label) ->
            Button(
                onClick = {
                    if (Settings.System.canWrite(context)) {
                        selectedTimeout = value
                        Settings.System.putInt(
                            context.contentResolver,
                            Settings.System.SCREEN_OFF_TIMEOUT,
                            value.toInt()
                        )
                        Toast.makeText(context, "Đặt thời gian: $label", Toast.LENGTH_SHORT).show()
                    } else {
                        onRequestPermission()
                        Toast.makeText(context, "Cần cấp quyền", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(label)
            }
        }

        OutlinedTextField(
            value = customTimeout,
            onValueChange = { customTimeout = it },
            label = { Text("Tùy chỉnh (giây)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (Settings.System.canWrite(context)) {
                    val seconds = customTimeout.toLongOrNull()
                    if (seconds != null && seconds > 0) {
                        val millis = seconds * 1000
                        Settings.System.putInt(
                            context.contentResolver,
                            Settings.System.SCREEN_OFF_TIMEOUT,
                            millis.toInt()
                        )
                        Toast.makeText(context, "Đặt thời gian: $seconds giây", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Nhập số giây hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    onRequestPermission()
                    Toast.makeText(context, "Cần cấp quyền", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Áp dụng tùy chỉnh")
        }
    }
}