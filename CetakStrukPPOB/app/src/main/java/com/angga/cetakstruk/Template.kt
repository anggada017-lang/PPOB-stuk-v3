package com.angga.cetakstruk

object Template {
    fun render(t: Transaction, storeName: String, address: String): String {
        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine(storeName.padCenter(32))
        if (address.isNotBlank()) sb.appendLine(address.padCenter(32))
        sb.appendLine("================================")
        sb.appendLine(t.type.padCenter(32))
        sb.appendLine()
        if (t.date.isNotBlank() || t.time.isNotBlank()) sb.appendLine("Tanggal : ${t.date} ${t.time}".trim())
        if (t.customer.isNotBlank()) sb.appendLine("Nama     : ${t.customer}")
        if (t.phone.isNotBlank()) sb.appendLine("No. HP   : ${t.phone}")
        if (t.bank.isNotBlank()) sb.appendLine("Bank     : ${t.bank}")
        if (t.account.isNotBlank()) sb.appendLine("Rekening : ${t.account}")
        if (t.token.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("TOKEN PLN")
            sb.appendLine(t.token)
            if (t.kwh.isNotBlank()) sb.appendLine("Jumlah KWh : ${t.kwh} kWh")
        }
        if (t.nominal > 0) sb.appendLine("Nominal  : ${Parser.money(t.nominal)}")
        if (t.fee > 0) sb.appendLine("Admin    : ${Parser.money(t.fee)}")
        if (t.total > 0) {
            sb.appendLine("--------------------------------")
            sb.appendLine("TOTAL    : ${Parser.money(t.total)}")
        }
        if (t.service.isNotBlank()) sb.appendLine("Layanan  : ${t.service}")
        if (t.paymentMethod.isNotBlank()) sb.appendLine("Bayar    : ${t.paymentMethod}")
        sb.appendLine("Status   : ${t.status}")
        if (t.reference.isNotBlank()) sb.appendLine("Ref      : ${t.reference}")
        if (t.transactionId.isNotBlank()) sb.appendLine("ID Trans : ${t.transactionId}")
        if (t.merchantOrderId.isNotBlank()) sb.appendLine("ID Order : ${t.merchantOrderId}")
        sb.appendLine()
        sb.appendLine("--------------------------------")
        sb.appendLine("TERIMA KASIH".padCenter(32))
        sb.appendLine("Simpan struk ini sebagai bukti.".padCenter(32))
        sb.appendLine("================================")
        return sb.toString()
    }

    private fun String.padCenter(width: Int): String {
        if (length >= width) return take(width)
        val left = (width - length) / 2
        return " ".repeat(left) + this
    }
}
