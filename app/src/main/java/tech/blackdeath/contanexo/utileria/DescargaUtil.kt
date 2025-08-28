package tech.blackdeath.contanexo.utileria

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.text.Normalizer

private val httpClient by lazy { OkHttpClient() }

/**
 * Abre una URL en un navegador personalizado.
 */
fun openInCustomTabs(context: Context, url: String) {
    val tabs = CustomTabsIntent.Builder().build()
    tabs.launchUrl(context, Uri.parse(url))
}

// Compartir ENLACE (correo, WhatsApp, etc.)
fun shareLink(context: Context, url: String, subject: String? = null) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
    }
    context.startActivity(Intent.createChooser(send, "Compartir"))
}

fun enqueueDownload(context: Context, url: String, filename: String, mimeType: String?) {
    val dm = context.getSystemService<DownloadManager>()
    if (dm == null) {
        Toast.makeText(context, "No se pudo acceder al gestor de descargas", Toast.LENGTH_SHORT).show()
        return
    }

    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle(filename)
        .setDescription("Descargando documento")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    // /Download/ContaNexo/filename
    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        "ContaNexo/$filename"
    )

    if (!mimeType.isNullOrBlank()) request.setMimeType(mimeType)
    dm.enqueue(request)
}

private fun sanitizeFileName(name: String, defaultExt: String = ".pdf"): String {
    val nf = Normalizer.normalize(name, Normalizer.Form.NFD)
        .replace("[^\\p{ASCII}]".toRegex(), "")           // quita acentos
        .replace("[\\\\/:*?\"<>|]".toRegex(), "_")        // caracteres inválidos
        .trim()
    val withExt = if (nf.lowercase().endsWith(defaultExt)) nf else nf + defaultExt
    return withExt.ifEmpty { "document$defaultExt" }
}

private suspend fun downloadToCache(
    context: Context,
    url: String,
    subdir: String,
    fileName: String
): Uri = withContext(Dispatchers.IO) {
    val req = Request.Builder().url(url).build()
    httpClient.newCall(req).execute().use { resp ->
        if (!resp.isSuccessful) error("HTTP ${resp.code}")
        val dir = File(context.cacheDir, subdir).apply { mkdirs() }
        val file = File(dir, fileName)
        resp.body?.byteStream().use { input ->
            file.outputStream().use { output -> input?.copyTo(output) }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}

/** Comparte un URI local como archivo */
private fun shareFileUri(context: Context, uri: Uri, mimeType: String, subject: String? = null) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Compartir PDF"))
}

/** API pública: descarga el PDF a caché y lo comparte como adjunto */
suspend fun sharePdfAsFile(context: Context, url: String, displayName: String) {
    val safeName = sanitizeFileName(displayName, ".pdf")
    val uri = downloadToCache(context, url, "pdfs", safeName)
    shareFileUri(context, uri, "application/pdf", subject = displayName)
}

// Descarga URL a cache y abre como PDF local (content://) con permisos
fun openLocalPdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}

suspend fun viewPdfFromUrl(context: Context, url: String, displayName: String) {
    val safeName = sanitizeFileName(displayName, ".pdf")
    val uri = downloadToCache(context, url, "pdfs/view", safeName)
    openLocalPdf(context, uri)
}