package com.angga.cetakstruk

import java.text.NumberFormat
import java.util.Locale

object Parser {
    private val rupiah = Regex("""(?i)(?:Rp|IDR)\s*([0-9][0-9.,]*)""")
    private fun clean(s: String) = s.replace("\u00a0", " ").trim()

    private fun amount(s: String): Long? {
        val raw = s.replace(".", "").replace(",", "").replace(" ", "")
        return raw.toLongOrNull()
    }

    private fun firstAmountAfter(text: String, labels: List<String>): Long {
        for (line in text.lines()) {
            if (labels.any { line.contains(it, ignoreCase = true) }) {
                val m = rupiah.find(line) ?: Regex("""([0-9][0-9.,]{2,})""").find(line)
                if (m != null) return amount(m.groupValues[1]) ?: 0
            }
        }
        return 0
    }

    private fun findAfter(text: String, label: Regex): String {
        val line = text.lines().firstOrNull { label.containsMatchIn(it) } ?: return ""
        return line.substringAfter(":").trim().ifEmpty {
            line.replace(label, "").trim()
        }
    }

    fun parse(input: String): Transaction {
        val t = clean(input)
        val x = Transaction()
        x.status = if (Regex("berhasil|success|sukses", RegexOption.IGNORE_CASE).containsMatchIn(t)) "BERHASIL" else "DIPROSES"

        x.date = Regex("""(\d{2}[ /-](?:Jan|Feb|Mar|Apr|Mei|Jun|Jul|Agu|Sep|Okt|Nov|Des|\d{2})[ /-]\d{4}|\d{2}/\d{2}/\d{4})""", RegexOption.IGNORE_CASE)
            .find(t)?.value ?: ""
        x.time = Regex("""\b\d{2}:\d{2}(?::\d{2})?\b""").find(t)?.value ?: ""

        // DANA Token PLN
        if (Regex("""token listrik|token pln""", RegexOption.IGNORE_CASE).containsMatchIn(t)) {
            x.type = "TOKEN LISTRIK PLN"
            x.customer = Regex("""Token Listrik.*?\b\d+\s*Rb\s+(.+?)(?:\r?\n|$)""", RegexOption.IGNORE_CASE).find(t)?.groupValues?.getOrNull(1)?.trim() ?: ""
            x.token = Regex("""\b\d{4}(?:\s+\d{4}){4}\b""").find(t)?.value ?: ""
            x.kwh = Regex("""Jumlah KWh\s*:\s*([0-9.,]+)""", RegexOption.IGNORE_CASE).find(t)?.groupValues?.getOrNull(1) ?: ""
            x.total = firstAmountAfter(t, listOf("Total Bayar"))
            x.nominal = firstAmountAfter(t, listOf("Token Listrik", "NOMINAL"))
            if (x.nominal == 0 && x.total > 0) x.nominal = x.total
            x.paymentMethod = findAfter(t, Regex("""Metode Pembayaran""", RegexOption.IGNORE_CASE))
        }
        // DANA transfer / kirim uang
        else if (Regex("""Kirim Uang|Detail Penerima""", RegexOption.IGNORE_CASE).containsMatchIn(t)) {
            x.type = "KIRIM UANG DANA"
            x.customer = Regex("""(?:Kirim Uang.*?ke|Nama)\s*:?\s*([A-Za-z][A-Za-z .'-]+)""", RegexOption.IGNORE_CASE).find(t)?.groupValues?.getOrNull(1)?.trim() ?: ""
            x.phone = Regex("""(?:Akun DANA|No\.?\s*DANA)\s*:?\s*(\d{8,15})""", RegexOption.IGNORE_CASE).find(t)?.groupValues?.getOrNull(1) ?: ""
            x.total = firstAmountAfter(t, listOf("Total Bayar"))
            val firstRp = rupiah.find(t)?.groupValues?.getOrNull(1)
            x.nominal = firstRp?.let(::amount) ?: x.total
            x.fee = (x.total - x.nominal).coerceAtLeast(0)
            x.paymentMethod = findAfter(t, Regex("""Metode Pembayaran""", RegexOption.IGNORE_CASE))
        }
        // BCA transfer
        else if (Regex("""m-Transfer|BI FAST|BANK MANDIRI|NOMINAL TRANSFER""", RegexOption.IGNORE_CASE).containsMatchIn(t)) {
            x.type = "TRANSFER BANK"
            x.bank = Regex("""Ke Rekening Tujuan\s+([A-Z ]+)""", RegexOption.IGNORE_CASE).find(t)?.groupValues?.getOrNull(1)?.trim() ?: ""
            x.account = Regex("""(?:Ke Rekening Tujuan\s+[A-Z ]+\s+)?(\d{8,20})\s+\w+""", RegexOption.IGNORE_CASE).find(t)?.groupValues?.getOrNull(1) ?: ""
            x.customer = Regex("""\b\d{8,20}\s+([A-Z][A-Z .'-]+)""").find(t)?.groupValues?.getOrNull(1)?.trim() ?: ""
            x.nominal = firstAmountAfter(t, listOf("NOMINAL TRANSFER"))
            x.fee = firstAmountAfter(t, listOf("BIAYA"))
            x.total = x.nominal + x.fee
            x.service = Regex("""Layanan\s+([^\r\n]+)|LAYANAN\s+([^\r\n]+)""", RegexOption.IGNORE_CASE).find(t)?.groupValues?.getOrNull(1)?.trim()
                ?: if (t.contains("BI FAST", true)) "BI FAST" else ""
            x.reference = Regex("""Ref\s+([A-Za-z0-9]+)""", RegexOption.IGNORE_CASE).find(t)?.groupValues?.getOrNull(1) ?: ""
        }
        // generic
        else {
            x.type = "TRANSAKSI"
            x.total = firstAmountAfter(t, listOf("Total Bayar", "TOTAL", "NOMINAL"))
            x.nominal = x.total
        }

        if (x.transactionId.isEmpty()) {
            x.transactionId = Regex("""ID Transaksi\s*:?\s*([0-9A-Za-z]+(?:\s+[0-9A-Za-z]+)?)""", RegexOption.IGNORE_CASE)
                .find(t)?.groupValues?.getOrNull(1)?.trim() ?: ""
        }
        if (x.merchantOrderId.isEmpty()) {
            x.merchantOrderId = Regex("""ID Order Merchant\s*:?\s*([0-9A-Za-z]+(?:\s+[0-9A-Za-z]+)?)""", RegexOption.IGNORE_CASE)
                .find(t)?.groupValues?.getOrNull(1)?.trim() ?: ""
        }
        return x
    }

    fun money(v: Long): String =
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }.format(v).replace("Rp", "Rp")
}
