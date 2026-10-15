package com.angga.cetakstruk

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val printer = PrinterManager()
    private var sharedText by mutableStateOf("")
    private var transaction by mutableStateOf(Transaction())
    private var storeName by mutableStateOf("TOKO ANGGA")
    private var address by mutableStateOf("PPOB & PEMBAYARAN")
    private var template by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent { AppUi() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(i: Intent?) {
        val text = i?.getStringExtra("shared_text").orEmpty()
        if (text.isNotBlank()) {
            sharedText = text
            transaction = Parser.parse(text)
            template = Template.render(transaction, storeName, address)
        }
    }

    private fun requestBtAndThen(action: () -> Unit) {
        val perms = if (Build.VERSION.SDK_INT >= 31)
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        else emptyArray()
        if (perms.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            action()
        } else {
            btLauncher.launch(perms)
        }
    }

    private val btLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { ok -> ok }) { /* user can press printer again */ }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AppUi() {
        MaterialTheme {
            var tab by remember { mutableIntStateOf(0) }
            Scaffold(
                topBar = { TopAppBar(title = { Text("Cetak Struk PPOB") }) },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(tab == 0, { tab = 0 }, icon = {}, label = { Text("Transaksi") })
                        NavigationBarItem(tab == 1, { tab = 1 }, icon = {}, label = { Text("Printer") })
                        NavigationBarItem(tab == 2, { tab = 2 }, icon = {}, label = { Text("Template") })
                    }
                }
            ) { pad ->
                when(tab) {
                    0 -> TransactionScreen(Modifier.padding(pad))
                    1 -> PrinterScreen(Modifier.padding(pad))
                    else -> TemplateScreen(Modifier.padding(pad))
                }
            }
        }
    }

    @Composable
    private fun TransactionScreen(modifier: Modifier) {
        Column(
            modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Terima dari Share", style = MaterialTheme.typography.titleMedium)
            Text(
                if (sharedText.isBlank()) "Belum ada transaksi. Dari DANA/BCA tekan Bagikan lalu pilih Cetak Struk PPOB."
                else "Transaksi berhasil dibaca.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(transaction.type, { transaction = transaction.copy(type = it) }, label = { Text("Jenis transaksi") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(transaction.customer, { transaction = transaction.copy(customer = it) }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(transaction.phone, { transaction = transaction.copy(phone = it) }, label = { Text("Nomor HP/DANA") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(Parser.money(transaction.nominal), { transaction = transaction.copy(nominal = it.filter(Char::isDigit).toLongOrNull() ?: 0) }, label = { Text("Nominal") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(Parser.money(transaction.fee), { transaction = transaction.copy(fee = it.filter(Char::isDigit).toLongOrNull() ?: 0) }, label = { Text("Admin") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(Parser.money(transaction.total), { transaction = transaction.copy(total = it.filter(Char::isDigit).toLongOrNull() ?: 0) }, label = { Text("Total") }, modifier = Modifier.fillMaxWidth())
            if (transaction.token.isNotBlank()) OutlinedTextField(transaction.token, { transaction = transaction.copy(token = it) }, label = { Text("Token PLN") }, modifier = Modifier.fillMaxWidth())
            if (transaction.reference.isNotBlank()) OutlinedTextField(transaction.reference, { transaction = transaction.copy(reference = it) }, label = { Text("Referensi") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = { template = Template.render(transaction, storeName, address) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Buat Preview Struk") }

            if (template.isNotBlank()) {
                HorizontalDivider()
                Text("Preview", style = MaterialTheme.typography.titleMedium)
                Text(template, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = { requestBtAndThen { printer.print(template) } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (printer.isConnected()) "CETAK SEKARANG" else "Hubungkan printer dulu") }
            }
        }
    }

    @Composable
    private fun PrinterScreen(modifier: Modifier) {
        var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
        var msg by remember { mutableStateOf("Belum terhubung") }
        Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Printer Bluetooth MP-58ECO", style = MaterialTheme.typography.titleLarge)
            Button(onClick = {
                requestBtAndThen {
                    devices = printer.pairedDevices()
                    msg = "Pilih printer yang sudah dipairing di Android."
                }
            }) { Text("Tampilkan Printer Pairing") }
            Text(msg)
            devices.forEach { d ->
                OutlinedButton(onClick = {
                    requestBtAndThen {
                        msg = if (printer.connect(d)) "Terhubung: ${d.name}" else "Gagal terhubung"
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("${d.name ?: "Bluetooth"}\n${d.address}")
                }
            }
            Button(onClick = { if (printer.print("TEST PRINT\nMP-58ECO\nCetak Struk PPOB\n")) msg = "Test print dikirim." else msg = "Belum terhubung." }) {
                Text("Test Print")
            }
        }
    }

    @Composable
    private fun TemplateScreen(modifier: Modifier) {
        Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Pengaturan Struk", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(storeName, { storeName = it }, label = { Text("Nama toko") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(address, { address = it }, label = { Text("Alamat/keterangan") }, modifier = Modifier.fillMaxWidth())
            Text("Template aktif dibuat otomatis dari jenis transaksi. Versi berikutnya dapat menambah editor blok/field dan logo.")
        }
    }
}
