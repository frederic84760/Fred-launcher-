package fr.fred.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import fr.fred.launcher.model.InstalledApp
import java.text.Collator
import java.util.Locale

class AppRepository(private val context: Context) {
    private val pm = context.packageManager

    fun loadApps(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .map { info ->
                InstalledApp(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    activityName = info.activityInfo.name,
                    icon = info.loadIcon(pm),
                )
            }
            .distinctBy { "${it.packageName}/${it.activityName}" }
            .toMutableList()

        val collator = Collator.getInstance(Locale.FRENCH).apply { strength = Collator.PRIMARY }
        apps.sortWith { a, b -> collator.compare(a.label, b.label) }
        return apps
    }

    fun launch(app: InstalledApp) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                pm.getLaunchIntentForPackage(app.packageName)?.let { fallback ->
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallback)
                }
            }
    }

    fun launchFallback(kind: FavoriteKind) {
        val intent = when (kind) {
            FavoriteKind.PHONE -> Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:"))
            FavoriteKind.MESSAGES -> Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:"))
            FavoriteKind.CAMERA -> Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            FavoriteKind.PHOTOS -> Intent(Intent.ACTION_VIEW).apply { type = "image/*" }
            else -> null
        } ?: return

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}

enum class FavoriteKind(val displayName: String, val keywords: List<String>) {
    PHONE("Téléphone", listOf("téléphone", "telephone", "phone", "dialer")),
    MESSAGES("Messages", listOf("messages", "message", "messaging")),
    WHATSAPP("WhatsApp", listOf("whatsapp")),
    GMAIL("Gmail", listOf("gmail")),
    CHROME("Chrome", listOf("chrome")),
    CAMERA("Appareil photo", listOf("appareil photo", "camera", "caméra")),
    PHOTOS("Photos", listOf("photos", "galerie", "gallery")),
}

fun List<InstalledApp>.resolveFavorite(kind: FavoriteKind): InstalledApp? {
    val normalized = map { app -> app to normalize(app.label) }
    return normalized.firstOrNull { (_, label) ->
        kind.keywords.any { keyword -> label == normalize(keyword) }
    }?.first ?: normalized.firstOrNull { (_, label) ->
        kind.keywords.any { keyword -> label.contains(normalize(keyword)) }
    }?.first
}

private fun normalize(value: String): String = java.text.Normalizer
    .normalize(value.lowercase(Locale.FRENCH), java.text.Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
