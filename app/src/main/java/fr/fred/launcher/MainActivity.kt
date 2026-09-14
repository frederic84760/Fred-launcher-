package fr.fred.launcher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import fr.fred.launcher.data.AppRepository
import fr.fred.launcher.data.FavoriteKind
import fr.fred.launcher.data.WeatherRepository
import fr.fred.launcher.data.resolveFavorite
import fr.fred.launcher.model.InstalledApp
import fr.fred.launcher.model.WeatherState
import fr.fred.launcher.ui.theme.FredBlack
import fr.fred.launcher.ui.theme.FredBlue
import fr.fred.launcher.ui.theme.FredBlueSoft
import fr.fred.launcher.ui.theme.FredBorder
import fr.fred.launcher.ui.theme.FredLauncherTheme
import fr.fred.launcher.ui.theme.FredMuted
import fr.fred.launcher.ui.theme.FredSurface
import fr.fred.launcher.ui.theme.FredWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FredLauncherTheme {
                FredLauncherApp()
            }
        }
    }
}

private enum class Screen { HOME, DRAWER, SETTINGS }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FredLauncherApp() {
    val context = LocalContext.current
    val appRepository = remember { AppRepository(context.applicationContext) }
    val weatherRepository = remember { WeatherRepository() }

    var screen by remember { mutableStateOf(Screen.HOME) }
    var apps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    var weather by remember { mutableStateOf(WeatherState()) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { appRepository.loadApps() }
        weather = withContext(Dispatchers.IO) { weatherRepository.fetch() }
    }

    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    Surface(modifier = Modifier.fillMaxSize(), color = FredBlack) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = { fadeIn(tween(170)) togetherWith fadeOut(tween(140)) },
            label = "screen",
        ) { current ->
            when (current) {
                Screen.HOME -> HomeScreen(
                    apps = apps,
                    weather = weather,
                    appRepository = appRepository,
                    onOpenDrawer = { screen = Screen.DRAWER },
                    onOpenSettings = { screen = Screen.SETTINGS },
                )
                Screen.DRAWER -> AppDrawer(
                    apps = apps,
                    onBack = { screen = Screen.HOME },
                    onLaunch = appRepository::launch,
                )
                Screen.SETTINGS -> SettingsScreen(onBack = { screen = Screen.HOME })
            }
        }
    }
}

@Composable
private fun HomeScreen(
    apps: List<InstalledApp>,
    weather: WeatherState,
    appRepository: AppRepository,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000)
        }
    }

    val timeText = remember(now.minute) { now.format(DateTimeFormatter.ofPattern("HH:mm")) }
    val dateText = remember(now.dayOfYear) {
        now.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
            .removeSuffix(".")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FredBlack)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, amount -> totalDrag += amount },
                    onDragEnd = {
                        if (totalDrag < -90f) onOpenDrawer()
                        totalDrag = 0f
                    },
                )
            }
            .combinedClickable(onClick = {}, onLongClick = onOpenSettings),
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.fred_wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.55f,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.48f), Color.Black.copy(alpha = 0.82f)),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 18.dp),
        ) {
            Text(timeText, color = FredWhite, fontSize = 62.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
            Text(dateText, color = FredWhite.copy(alpha = 0.84f), fontSize = 20.sp)

            Spacer(Modifier.height(24.dp))
            WeatherLine(weather)
            Spacer(Modifier.height(34.dp))

            FavoriteKind.entries.forEach { kind ->
                val app = remember(apps, kind) { apps.resolveFavorite(kind) }
                FavoriteRow(
                    kind = kind,
                    app = app,
                    onClick = {
                        if (app != null) appRepository.launch(app) else appRepository.launchFallback(kind)
                    },
                )
                Spacer(Modifier.height(17.dp))
            }

            Spacer(Modifier.weight(1f))
            SearchPill(onClick = onOpenDrawer)
        }
    }
}

@Composable
private fun WeatherLine(weather: WeatherState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (weather.weatherCode) {
                0, 1 -> Icons.Outlined.WbSunny
                else -> Icons.Outlined.Cloud
            },
            contentDescription = null,
            tint = if (weather.error) FredMuted else FredBlueSoft,
            modifier = Modifier.size(31.dp),
        )
        Spacer(Modifier.size(14.dp))
        Column {
            Text(
                text = weather.temperatureC?.let { "$it°  ${weather.location}" } ?: weather.location,
                color = FredWhite,
                fontSize = 18.sp,
            )
            Text(weather.description, color = FredMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FavoriteRow(kind: FavoriteKind, app: InstalledApp?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = {})
            .padding(vertical = 7.dp, horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (app != null) {
            AppIcon(app.icon, 34)
        } else {
            Icon(
                imageVector = fallbackIcon(kind),
                contentDescription = null,
                tint = FredBlueSoft,
                modifier = Modifier.size(31.dp),
            )
        }
        Spacer(Modifier.size(18.dp))
        Text(kind.displayName, color = FredWhite, fontSize = 18.sp, fontWeight = FontWeight.Normal)
    }
}

private fun fallbackIcon(kind: FavoriteKind): ImageVector = when (kind) {
    FavoriteKind.PHONE -> Icons.Outlined.Phone
    FavoriteKind.MESSAGES, FavoriteKind.WHATSAPP -> Icons.Outlined.ChatBubbleOutline
    FavoriteKind.GMAIL -> Icons.Outlined.MailOutline
    FavoriteKind.CHROME -> Icons.Outlined.Language
    FavoriteKind.CAMERA -> Icons.Outlined.CameraAlt
    FavoriteKind.PHOTOS -> Icons.Outlined.Image
}

@Composable
private fun SearchPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(FredSurface.copy(alpha = 0.90f))
            .combinedClickable(onClick = onClick, onLongClick = {})
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, null, tint = FredBlueSoft, modifier = Modifier.size(23.dp))
        Spacer(Modifier.size(12.dp))
        Text("Rechercher des applications", color = FredMuted, fontSize = 15.sp)
    }
}

@Composable
private fun AppDrawer(
    apps: List<InstalledApp>,
    onBack: () -> Unit,
    onLaunch: (InstalledApp) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(apps, query) {
        if (query.isBlank()) apps else apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FredBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, "Retour", tint = FredWhite)
            }
            Text("Applications", color = FredWhite, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.Apps, null, tint = FredBlue)
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher…", color = FredMuted) },
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = FredBlueSoft) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = FredWhite,
                unfocusedTextColor = FredWhite,
                focusedBorderColor = FredBlue,
                unfocusedBorderColor = FredBorder,
                cursorColor = FredBlue,
            ),
            shape = RoundedCornerShape(22.dp),
        )

        Spacer(Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { "${it.packageName}/${it.activityName}" }) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .combinedClickable(onClick = { onLaunch(app) }, onLongClick = {})
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(app.icon, 42)
                    Spacer(Modifier.size(15.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.label, color = FredWhite, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(app.packageName, color = FredMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FredBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Retour", tint = FredWhite) }
            Text("Fred Launcher", color = FredWhite, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.Settings, null, tint = FredBlue)
        }

        Spacer(Modifier.height(24.dp))
        Text("ÉCRAN D’ACCUEIL", color = FredBlueSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        SettingsCard(
            title = "Définir Fred par défaut",
            description = "Ouvre les réglages Android pour choisir Fred comme écran d’accueil.",
        ) {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        Spacer(Modifier.height(22.dp))
        Text("VERSION 0.1", color = FredBlueSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        InfoRow("Fond", "Noir OLED + bleu électrique")
        InfoRow("Météo", "Gardanne • Open-Meteo")
        InfoRow("Favoris", "7 applications principales")
        InfoRow("Gestes", "Glisser vers le haut : toutes les apps")
        InfoRow("Réglages", "Appui long sur l’accueil")

        Spacer(Modifier.height(22.dp))
        HorizontalDivider(color = FredBorder)
        Spacer(Modifier.height(18.dp))
        Text(
            "Fred Phone est préparé dans le projet mais volontairement non activé dans cette V1 : le remplacement de l’application Téléphone nécessite une validation complète des appels entrants, sortants et d’urgence.",
            color = FredMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun SettingsCard(title: String, description: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(FredSurface)
            .padding(18.dp),
    ) {
        Text(title, color = FredWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(description, color = FredMuted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = FredBlue),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Ouvrir les réglages")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = FredWhite, fontSize = 15.sp, modifier = Modifier.weight(0.33f))
        Text(value, color = FredMuted, fontSize = 15.sp, modifier = Modifier.weight(0.67f))
    }
}

@Composable
private fun AppIcon(drawable: Drawable, sizeDp: Int) {
    val bitmap = remember(drawable) { drawable.toBitmap(width = 128, height = 128).asImageBitmap() }
    Image(
        bitmap = bitmap,
        contentDescription = null,
        modifier = Modifier.size(sizeDp.dp).clip(CircleShape),
        contentScale = ContentScale.Fit,
    )
}
