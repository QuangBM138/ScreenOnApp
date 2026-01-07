package com.example.screenonapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.screenonapp.ui.theme.ScreenOnAppTheme
import java.util.*

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (canWriteSettings(this)) {
            Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("lang", "vi") ?: "vi"
        updateResources(this, lang)

        setContent {
            var darkTheme by remember { 
                mutableStateOf(prefs.getBoolean("dark_mode", false)) 
            }
            var currentLanguage by remember { mutableStateOf(lang) }

            ScreenOnAppTheme(darkTheme = darkTheme) {
                MainScreen(
                    isDarkTheme = darkTheme,
                    onThemeChange = { isDark ->
                        darkTheme = isDark
                        prefs.edit().putBoolean("dark_mode", isDark).apply()
                    },
                    currentLanguage = currentLanguage,
                    onLanguageChange = { newLang ->
                        currentLanguage = newLang
                        prefs.edit().putString("lang", newLang).apply()
                        updateResources(this, newLang)
                        recreate()
                    },
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                            intent.data = "package:$packageName".toUri()
                            requestPermissionLauncher.launch(intent)
                        } else {
                            Toast.makeText(this, getString(R.string.permission_already_granted), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    private fun updateResources(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

fun canWriteSettings(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.System.canWrite(context)
    } else {
        true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(canWriteSettings(context)) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(context) {
        hasPermission = canWriteSettings(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Cài đặt Giao diện
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.theme_mode)) },
                            leadingIcon = {
                                Icon(
                                    if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                Switch(
                                    checked = isDarkTheme,
                                    onCheckedChange = null,
                                    modifier = Modifier.scale(0.8f)
                                )
                            },
                            onClick = { onThemeChange(!isDarkTheme) }
                        )
                        
                        HorizontalDivider()
                        
                        // Cài đặt Ngôn ngữ - Tiếng Việt
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.lang_vi)) },
                            leadingIcon = {
                                if (currentLanguage == "vi") Icon(Icons.Default.Check, contentDescription = null)
                                else Spacer(Modifier.size(24.dp))
                            },
                            onClick = {
                                onLanguageChange("vi")
                                showMenu = false
                            }
                        )
                        
                        // Cài đặt Ngôn ngữ - English
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.lang_en)) },
                            leadingIcon = {
                                if (currentLanguage == "en") Icon(Icons.Default.Check, contentDescription = null)
                                else Spacer(Modifier.size(24.dp))
                            },
                            onClick = {
                                onLanguageChange("en")
                                showMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hasPermission) {
                PermissionWarningCard(onGrantClick = {
                    onRequestPermission()
                    hasPermission = canWriteSettings(context)
                })
            }

            Text(
                stringResource(R.string.select_timeout),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            PresetTimeSection(context, hasPermission, onRequestPermission)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                stringResource(R.string.custom_timeout),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            CustomTimeSection(context, hasPermission, onRequestPermission)
        }
    }
}

@Composable
fun PermissionWarningCard(onGrantClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.permission_warning_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.permission_warning_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

@Composable
fun PresetTimeSection(
    context: Context,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    var currentTimeout by remember {
        mutableLongStateOf(
            try {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT).toLong()
            } catch (_: Exception) { 30000L }
        )
    }

    val presets = listOf(
        30000L to stringResource(R.string.time_30s),
        60000L to stringResource(R.string.time_1m),
        120000L to stringResource(R.string.time_2m),
        300000L to stringResource(R.string.time_5m),
        600000L to stringResource(R.string.time_10m),
        1800000L to stringResource(R.string.time_30m),
        -1L to stringResource(R.string.time_never)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(220.dp)
    ) {
        items(presets) { (value, label) ->
            val isSelected = currentTimeout == value
            val realValue = if (value == -1L) Int.MAX_VALUE.toLong() else value
            val isNeverSelected = (value == -1L && currentTimeout > 1800000L)

            PresetCard(
                label = label,
                isSelected = isSelected || isNeverSelected,
                onClick = {
                    if (hasPermission) {
                        setTimeout(context, realValue)
                        currentTimeout = realValue
                    } else {
                        onRequestPermission()
                    }
                }
            )
        }
    }
}

@Composable
fun PresetCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CustomTimeSection(
    context: Context,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeInputParams(value = hours, onValueChange = { hours = it }, label = stringResource(R.string.hour), modifier = Modifier.weight(1f))
                Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 4.dp))
                TimeInputParams(value = minutes, onValueChange = { minutes = it }, label = stringResource(R.string.minute), modifier = Modifier.weight(1f))
                Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 4.dp))
                TimeInputParams(value = seconds, onValueChange = { seconds = it }, label = stringResource(R.string.second), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (hasPermission) {
                        val h = hours.toLongOrNull() ?: 0
                        val m = minutes.toLongOrNull() ?: 0
                        val s = seconds.toLongOrNull() ?: 0

                        val totalMillis = (h * 3600 + m * 60 + s) * 1000

                        if (totalMillis > 0) {
                            setTimeout(context, totalMillis)
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_invalid_time), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        onRequestPermission()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.apply_custom))
            }
        }
    }
}

@Composable
fun TimeInputParams(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.all { char -> char.isDigit() }) {
                onValueChange(it)
            }
        },
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 18.sp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

fun setTimeout(context: Context, millis: Long) {
    try {
        val finalMillis = if (millis > Int.MAX_VALUE.toLong()) Int.MAX_VALUE else millis.toInt()

        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            finalMillis
        )

        val message = if (millis >= Int.MAX_VALUE.toLong())
            context.getString(R.string.timeout_disabled)
        else
            context.getString(R.string.timeout_set, formatTime(context, millis))

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.error_message, e.message), Toast.LENGTH_SHORT).show()
    }
}

fun formatTime(context: Context, millis: Long): String {
    val totalSeconds = millis / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60

    val parts = mutableListOf<String>()
    if (h > 0) parts.add(context.getString(R.string.format_hour, h.toInt()))
    if (m > 0) parts.add(context.getString(R.string.format_minute, m.toInt()))
    if (s > 0) parts.add(context.getString(R.string.format_second, s.toInt()))

    return if (parts.isEmpty()) context.getString(R.string.format_zero) else parts.joinToString(" ")
}
