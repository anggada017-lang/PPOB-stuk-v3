# Cetak Struk PPOB v3 — Share Receiver — Android project

This is a starter Android project for the Cetak Struk PPOB app.

## Current features in this prototype
- Accepts shared transaction text from other apps.
- Accepts a shared image and tries to read text with Google ML Kit OCR.
- Parses sample DANA PLN token, DANA money transfer, and BCA bank-transfer formats.
- Lets the user review/edit basic transaction fields.
- Builds a simple 58 mm-style text receipt.
- Can connect to paired Bluetooth Classic SPP thermal printers, including compatible MP-58ECO devices.

## Build on Codemagic
The repository includes `codemagic.yaml`. The workflow uses the Gradle installation available in the Codemagic build environment and runs `gradle assembleDebug`.
It can build either:
- a repository with the extracted Android project files at the root; or
- a repository containing the project ZIP at its root.

The debug APK should appear under Artifacts after a successful build.

## Important limitations
- This is a prototype and has not been tested against every DANA/BCA share format or every MP-58ECO firmware variant.
- OCR requires internet access during build to download dependencies and may need permission/format adjustments on some devices.
- The app creates a store/PPOB receipt from shared transaction details; it is not an official bank or e-wallet receipt.
- Review transaction data before printing. Never enter or share banking PINs, passwords, or OTPs.
- Test the app and printer before operational use.


## Perubahan v3 (Share Receiver)
- Menerima teks transaksi melalui menu Bagikan/Share dari aplikasi lain.
- Menerima satu screenshot atau beberapa gambar yang dibagikan sekaligus; mencoba membaca teks gambar dengan OCR.
- Menggabungkan teks bersama hasil OCR lalu mengirimkannya ke layar transaksi untuk diperiksa dan diedit.
- Versi aplikasi dinaikkan ke 3.0.

Catatan: dukungan berbagi bergantung pada aplikasi sumber dan format yang dibagikannya. OCR/parsing tidak selalu akurat; periksa dan koreksi nominal, nomor, token, serta referensi sebelum mencetak. Aplikasi ini membuat struk toko/PPOB dan bukan bukti resmi dari DANA, GoPay, bank, atau penyedia layanan.
