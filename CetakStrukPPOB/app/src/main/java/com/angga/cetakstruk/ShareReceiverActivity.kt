package com.angga.cetakstruk

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Receives shared transaction text or screenshots from other apps. */
class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receive(intent)
    }

    private fun receive(source: Intent?) {
        if (source == null) {
            openMain("")
            return
        }

        val action = source.action
        val mime = source.type.orEmpty()
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) {
            openMain("")
            return
        }

        val sharedText = buildString {
            source.getStringExtra(Intent.EXTRA_SUBJECT)?.takeIf { it.isNotBlank() }?.let { append(it).append("\n") }
            source.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }?.let { append(it) }
        }.trim()

        val uris = collectUris(source)
        if (uris.isEmpty() || !mime.startsWith("image/")) {
            openMain(sharedText)
            return
        }

        lifecycleScope.launch {
            val recognized = withContext(Dispatchers.IO) {
                uris.joinToString("\n\n") { uri ->
                    try { Ocr.recognize(this@ShareReceiverActivity, uri) }
                    catch (_: Exception) { "" }
                }.trim()
            }
            val combined = listOf(sharedText, recognized).filter { it.isNotBlank() }.joinToString("\n\n")
            openMain(combined)
        }
    }

    private fun collectUris(source: Intent): List<Uri> {
        val result = mutableListOf<Uri>()
        if (Build.VERSION.SDK_INT >= 33) {
            source.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { result.add(it) }
            source.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { result.addAll(it) }
        } else {
            @Suppress("DEPRECATION")
            (source.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))?.let { result.add(it) }
            @Suppress("DEPRECATION")
            (source.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM))?.let { result.addAll(it) }
        }
        source.clipData?.let { clip ->
            for (index in 0 until clip.itemCount) clip.getItemAt(index).uri?.let { result.add(it) }
        }
        return result.distinct()
    }

    private fun openMain(text: String) {
        val launch = Intent(this, MainActivity::class.java)
            .putExtra("shared_text", text)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(launch)
        finish()
    }
}
