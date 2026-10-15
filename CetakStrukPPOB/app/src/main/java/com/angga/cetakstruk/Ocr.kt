package com.angga.cetakstruk

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object Ocr {
    suspend fun recognize(context: Context, uri: Uri): String =
        suspendCancellableCoroutine { cont ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        if (cont.isActive) cont.resume(result.text)
                        recognizer.close()
                    }
                    .addOnFailureListener { e ->
                        if (cont.isActive) cont.resumeWithException(e)
                        recognizer.close()
                    }
            } catch (e: Exception) {
                if (cont.isActive) cont.resumeWithException(e)
            }
        }
}
