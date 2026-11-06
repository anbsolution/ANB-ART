package com.anb.recovery

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Phase 1: complete professional repair-studio UI. No lock/FRP bypass functionality. */
class MainActivity : Activity() {
    private val navy = Color.rgb(9, 31, 69)
    private val blue = Color.rgb(20, 91, 220)
    private val pale = Color.rgb(229, 240, 255)
    private val canvas = Color.rgb(244, 248, 253)
    private val ink = Color.rgb(23, 38, 61)
    private val muted = Color.rgb(105, 121, 145)
    private val border = Color.rgb(220, 230, 243)
    private val white = Color.WHITE
    private lateinit var root: LinearLayout
    private lateinit var page: LinearLayout
    private var selectedTab = "Home"
    private var latestSummary: String? = null
    private var summaryNotesSnapshot: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = navy
        window.navigationBarColor = navy
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(canvas)
        }
        setContentView(root)
        render()
    }

    private fun render() {
        root.removeAllViews()
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(15), dp(18), dp(16))
            setBackgroundColor(navy)
        }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(label("ANB", 17, white, true).apply {
            gravity = Gravity.CENTER
            background = rounded(blue, 12, blue)
            setPadding(dp(12), dp(9), dp(12), dp(9))
        }, wrap())
        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        titleBox.addView(label("MOBILE RECOVERY STUDIO", 16, white, true))
        titleBox.addView(label("Professional repair toolkit", 11, Color.rgb(190, 211, 240), false))
        top.addView(titleBox, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(label("?", 19, white, true).apply {
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(29, 64, 117), 22, Color.rgb(65, 103, 158))
            setOnClickListener { showInfo("Help & safety", "Use this toolkit only with the device owner's permission. It provides diagnostics and official recovery guidance. It does not bypass screen locks, encryption or account protections.") }
        }, LinearLayout.LayoutParams(dp(40), dp(40)))
        header.addView(top)
        header.addView(label("Authorized diagnostics • Local-first workflow", 12, Color.rgb(206, 222, 245), false), wrap().apply { topMargin = dp(12) })
        root.addView(header, match())

        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(22))
        }
        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        when (selectedTab) {
            "Home" -> showHome()
            "Devices" -> showDevices()
            "Tools" -> showTools()
            "Records" -> showRecords()
            else -> showMore()
        }
        renderBottomNavigation()
    }

    private fun showHome() {
        val hero = card().apply { background = rounded(navy, 20, navy) }
        hero.addView(label("WELCOME TO YOUR REPAIR WORKSPACE", 10, Color.rgb(177, 207, 246), true))
        hero.addView(label("ANB Mobile\nRecovery Studio", 27, white, true))
        hero.addView(label("Scan connected devices, review diagnostics and follow official recovery steps.", 13, Color.rgb(219, 231, 249), false))
        hero.addView(button("Open USB Device Scanner", true).apply {
            setOnClickListener { navigate("Devices") }
        }, match().apply { topMargin = dp(13) })
        page.addView(hero, match())

        section("SUPPORTED BRANDS", "Brand-specific official guidance")
        val brands = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        listOf(
            listOf(Triple("Redmi", "MI", Color.rgb(239, 102, 35)), Triple("OPPO", "O", Color.rgb(28, 143, 95))),
            listOf(Triple("Samsung", "S", Color.rgb(28, 93, 196)), Triple("Vivo", "V", Color.rgb(31, 117, 216)))
        ).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEachIndexed { index, brand ->
                row.addView(brandTile(brand.first, brand.second, brand.third), LinearLayout.LayoutParams(0, dp(90), 1f))
                if (index == 0) row.addView(View(this), LinearLayout.LayoutParams(dp(10), 1))
            }
            brands.addView(row, match().apply { bottomMargin = dp(9) })
        }
        page.addView(brands, match())

        section("QUICK WORKSPACE", "Common repair-shop tasks")
        val actions = listOf(
            Triple("USB Device Scanner", "Detect connected USB devices", "Devices"),
            Triple("Device Diagnostics", "View this phone's basic details", "Tools"),
            Triple("Official Recovery Guides", "Brand-wise support steps", "More"),
            Triple("Service Records", "Review current repair session", "Records")
        )
        actions.chunked(2).forEach { group ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            group.forEachIndexed { index, item ->
                row.addView(actionTile(item.first, item.second) { navigate(item.third) }, LinearLayout.LayoutParams(0, dp(108), 1f))
                if (index == 0) row.addView(View(this), LinearLayout.LayoutParams(dp(10), 1))
            }
            page.addView(row, match().apply { bottomMargin = dp(9) })
        }
        val safety = card()
        safety.addView(label("IMPORTANT", 11, Color.rgb(208, 112, 24), true))
        safety.addView(label("USB connection alone cannot remove another phone's PIN or pattern. Use the owner's credentials or the manufacturer's official recovery process.", 13, ink, false))
        page.addView(safety, match())
    }

    private fun showDevices() {
        section("USB DEVICE SCANNER", "Connect a customer's phone with a compatible OTG adapter")
        val host = DeviceDiagnostics.collect()
        val hostCard = card()
        hostCard.addView(label("HOST DEVICE", 10, muted, true))
        hostCard.addView(label("${host.manufacturer} ${host.model}", 18, ink, true))
        hostCard.addView(label("Android ${host.androidVersion} • SDK ${host.sdk}", 12, muted, false))
        page.addView(hostCard, match())

        val scanner = card()
        scanner.addView(label("CONNECTED USB DEVICES", 16, ink, true))
        scanner.addView(label("Lists devices visible to Android USB Host mode. No lock data is accessed.", 12, muted, false))
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fun refresh() {
            results.removeAllViews()
            val manager = getSystemService(Context.USB_SERVICE) as UsbManager
            val devices = manager.deviceList.values.toList()
            if (devices.isEmpty()) {
                results.addView(label("No USB device detected. Check OTG adapter, cable and the connected phone's USB mode.", 13, muted, false), match().apply { topMargin = dp(12) })
            } else {
                devices.forEach { results.addView(usbDeviceCard(it), match().apply { topMargin = dp(9) }) }
            }
        }
        scanner.addView(button("Refresh USB device list", true).apply { setOnClickListener { refresh() } }, match().apply { topMargin = dp(12) })
        scanner.addView(results, match())
        refresh()
        page.addView(scanner, match())

        val note = card()
        note.addView(label("SCANNER LIMITATION", 11, Color.rgb(208, 112, 24), true))
        note.addView(label("A normal Android app can list USB devices exposed by the host. It cannot read private phone data or remove a screen lock. ADB diagnostics require USB debugging to be enabled and authorized on the connected phone.", 13, ink, false))
        page.addView(note, match().apply { topMargin = dp(12) })
    }

    private fun usbDeviceCard(device: UsbDevice): LinearLayout {
        val item = card()
        item.addView(label(device.productName ?: "USB device", 14, ink, true))
        item.addView(label("Vendor ID: ${device.vendorId} • Product ID: ${device.productId}", 12, muted, false))
        item.addView(label("Device class: ${device.deviceClass} • Interfaces: ${device.interfaceCount}", 12, muted, false))
        return item
    }

    private fun showTools() {
        section("REPAIR TOOLS", "Local diagnostics and customer-authorized documentation")
        val report = DeviceDiagnostics.collect()
        val diagnostics = card()
        diagnostics.addView(label("DEVICE DIAGNOSTICS", 15, ink, true))
        addInfoRow(diagnostics, "Manufacturer", report.manufacturer)
        addInfoRow(diagnostics, "Model", report.model)
        addInfoRow(diagnostics, "Android version", "${report.androidVersion} (SDK ${report.sdk})")
        addInfoRow(diagnostics, "Device code", report.deviceCode)
        page.addView(diagnostics, match())

        val repair = card()
        repair.addView(label("CUSTOMER REPAIR CASE", 16, ink, true))
        repair.addView(label("Generate a local report only after confirming customer authorization.", 12, muted, false))
        val consent = CheckBox(this).apply {
            text = "Customer authorization confirmed"
            textSize = 13f
            setTextColor(ink)
            buttonTintList = android.content.res.ColorStateList.valueOf(blue)
        }
        repair.addView(consent, match())
        val notes = EditText(this).apply {
            hint = "Technician notes (never enter PINs or passwords)"
            minLines = 3
            maxLines = 5
            gravity = Gravity.TOP
            textSize = 13f
            setTextColor(ink)
            setHintTextColor(muted)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(canvas, 12, border)
        }
        repair.addView(notes, match().apply { topMargin = dp(8) })
        val preview = label("", 12, ink, false).apply {
            visibility = View.GONE
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(canvas, 12, border)
            setTextIsSelectable(true)
        }
        repair.addView(button("Generate local repair summary", true).apply {
            setOnClickListener {
                if (!consent.isChecked) {
                    toast("Confirm customer authorization first.")
                } else {
                    summaryNotesSnapshot = notes.text.toString()
                    latestSummary = RepairCaseManager.createLocalSummary(true, report, notes.text.toString())
                    preview.text = latestSummary
                    preview.visibility = View.VISIBLE
                }
            }
        }, match().apply { topMargin = dp(10) })
        repair.addView(preview, match().apply { topMargin = dp(10) })
        repair.addView(button("Share reviewed, redacted summary", false).apply {
            setOnClickListener {
                val summary = latestSummary
                if (!RepairCaseManager.canShareSummary(summaryNotesSnapshot, notes.text.toString(), consent.isChecked) || summary == null) {
                    toast("Notes or authorization changed. Generate a new report before sharing.")
                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Review before sharing")
                        .setMessage(RepairCaseManager.createShareReviewMessage(summary))
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Share") { _, _ ->
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "ANB Mobile Recovery Studio - Repair Summary")
                                putExtra(Intent.EXTRA_TEXT, RepairCaseManager.redactDeviceCodeForSharing(summary))
                            }
                            startActivity(Intent.createChooser(send, "Share repair summary"))
                        }.show()
                }
            }
        }, match().apply { topMargin = dp(9) })
        page.addView(repair, match())

        val recovery = card()
        recovery.addView(label("OFFICIAL RECOVERY", 15, ink, true))
        recovery.addView(label("A factory reset may erase customer data and can require the previously synced account afterward. Explain this risk before proceeding.", 13, muted, false))
        recovery.addView(button("Open brand recovery guides", false).apply { setOnClickListener { navigate("More") } }, match().apply { topMargin = dp(10) })
        page.addView(recovery, match().apply { topMargin = dp(12) })
    }

    private fun showRecords() {
        section("SERVICE RECORDS", "Privacy-conscious repair session")
        val session = card()
        session.addView(label("CURRENT SESSION", 15, ink, true))
        session.addView(label("Phase 1 creates repair summaries locally on demand. It does not upload customer records or claim to keep a permanent database.", 13, muted, false))
        session.addView(button("Create a repair summary", true).apply { setOnClickListener { navigate("Tools") } }, match().apply { topMargin = dp(12) })
        page.addView(session, match())
        val privacy = card()
        privacy.addView(label("CUSTOMER DATA", 13, Color.rgb(20, 148, 91), true))
        privacy.addView(label("Never store PINs, passwords, Google credentials or private content in technician notes. Share only reviewed information with authorization.", 13, ink, false))
        page.addView(privacy, match().apply { topMargin = dp(12) })
    }

    private fun showMore() {
        section("GUIDES & SETTINGS", "Manufacturer resources and app information")
        listOf("Redmi / Xiaomi", "OPPO", "Samsung", "Vivo").forEach { brand ->
            page.addView(actionTile(brand, "Official support and recovery guidance") { showBrandGuide(brand) }, match().apply { bottomMargin = dp(9) })
        }
        val about = card()
        about.addView(label("ANB MOBILE RECOVERY STUDIO", 15, ink, true))
        about.addView(label("Phase 1 • Professional Blue Shop UI", 13, muted, false))
        about.addView(label("Package: com.anb.recovery", 12, muted, false))
        about.addView(label("Authorized diagnostics • USB host discovery • Official recovery guidance", 12, muted, false))
        page.addView(about, match())
    }

    private fun showBrandGuide(brand: String) {
        val url = when {
            brand.contains("Redmi", true) || brand.contains("Xiaomi", true) -> "https://www.mi.com/global/support/"
            brand.contains("OPPO", true) -> "https://support.oppo.com/"
            brand.contains("Samsung", true) -> "https://www.samsung.com/in/support/"
            else -> "https://www.vivo.com/in/support"
        }
        AlertDialog.Builder(this)
            .setTitle("$brand • Official recovery")
            .setMessage("1. Verify ownership and customer authorization.\n\n2. Have the owner use their own screen credential or official account recovery.\n\n3. Explain that a factory reset can erase data.\n\n4. The device may request the previously synced account after reset. This app does not bypass locks or account verification.")
            .setNegativeButton("Close", null)
            .setPositiveButton("Open official support") { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .show()
    }

    private fun renderBottomNavigation() {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(5), dp(4), dp(5))
            setBackgroundColor(white)
        }
        listOf("Home", "Devices", "Tools", "Records", "More").forEach { name ->
            val glyph = when (name) {
                "Home" -> "⌂"
                "Devices" -> "▣"
                "Tools" -> "⚒"
                "Records" -> "▤"
                else -> "•••"
            }
            nav.addView(label("$glyph\n$name", 10, if (selectedTab == name) blue else muted, selectedTab == name).apply {
                gravity = Gravity.CENTER
                setOnClickListener { navigate(name) }
            }, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        root.addView(nav, match())
    }

    private fun navigate(name: String) {
        selectedTab = name
        render()
    }

    private fun section(title: String, subtitle: String) {
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(17), dp(2), dp(10))
        }
        block.addView(label(title, 16, ink, true))
        block.addView(label(subtitle, 12, muted, false))
        page.addView(block, match())
    }

    private fun brandTile(name: String, symbol: String, color: Int): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(12), dp(8), dp(12))
        background = rounded(color, 15, color)
        addView(label(symbol, 22, white, true).apply { gravity = Gravity.CENTER })
        addView(label(name, 13, white, true).apply { gravity = Gravity.CENTER })
        setOnClickListener { showBrandGuide(name) }
    }

    private fun actionTile(title: String, subtitle: String, action: () -> Unit): LinearLayout = card().apply {
        addView(label(title, 14, ink, true))
        addView(label(subtitle, 11, muted, false))
        setOnClickListener { action() }
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(15), dp(15), dp(15), dp(15))
        background = rounded(white, 17, border)
        elevation = dp(1).toFloat()
    }

    private fun addInfoRow(parent: LinearLayout, key: String, value: String) {
        parent.addView(label(key.uppercase(), 10, muted, true))
        parent.addView(label(value, 14, ink, false))
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(5)))
    }

    private fun button(text: String, primary: Boolean): Button = Button(this).apply {
        this.text = text
        textSize = 13f
        isAllCaps = false
        setTextColor(if (primary) white else blue)
        background = rounded(if (primary) blue else pale, 13, if (primary) blue else border)
        minHeight = dp(47)
        setPadding(dp(12), dp(7), dp(12), dp(7))
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean): TextView = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setLineSpacing(dp(2).toFloat(), 1f)
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun rounded(color: Int, radius: Int, stroke: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(1), stroke)
    }

    private fun showInfo(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun match() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun wrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
