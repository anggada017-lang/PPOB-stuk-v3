package com.angga.cetakstruk

data class Transaction(
    var type: String = "TRANSAKSI",
    var status: String = "BERHASIL",
    var date: String = "",
    var time: String = "",
    var customer: String = "",
    var phone: String = "",
    var bank: String = "",
    var account: String = "",
    var nominal: Long = 0,
    var fee: Long = 0,
    var total: Long = 0,
    var token: String = "",
    var kwh: String = "",
    var service: String = "",
    var reference: String = "",
    var transactionId: String = "",
    var merchantOrderId: String = "",
    var paymentMethod: String = ""
)
