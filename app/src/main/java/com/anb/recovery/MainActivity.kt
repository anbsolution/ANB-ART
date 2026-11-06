package com.anb.recovery

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * ANB Mobile Repair Studio Shop Edition.
 * Visual flow: Home -> USB diagnostics -> Diagnostic report.
 * USB Host visibility is not equivalent to ADB access or lock-state access.
 * No PIN/pattern/password/FRP bypass is implemented.
 */
class MainActivity : Activity() {
    private val navy = Color.rgb(5, 18, 48)
    private val navy2 = Color.rgb(9, 39, 91)
    private val blue = Color.rgb(0, 116, 255)
    private val cyan = Color.rgb(0, 210, 255)
    private val pale = Color.rgb(235, 245, 255)
    private val ink = Color.rgb(12, 31, 65)
    private val muted = Color.rgb(83, 105, 137)
    private val green = Color.rgb(0, 160, 75)
    private val orange = Color.rgb(242, 139, 0)
    private val purple = Color.rgb(115, 30, 210)
    private val white = Color.WHITE

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private var screen = "Home"
    private var connectedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = navy
        window.navigationBarColor = navy
        render()
    }

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(239, 246, 255))
        }
        setContentView(root)

        when (screen) {
            "Diagnostics" -> diagnosticsScreen()
            "Report" -> reportScreen()
            else -> homeScreen()
        }
    }

    private fun homeScreen() {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(17))
            background = gradient(navy, Color.rgb(0, 75, 170), 0)
        }
        val top = row()
        top.addView(text("☰", 25, white, true).apply { gravity = Gravity.CENTER }, lp(42, 48))
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(text("🔧  ANB", 25, white, true))
        brand.addView(text("MOBILE REPAIR STUDIO", 13, white, true))
        brand.addView(text("SHOP EDITION  •  PRO", 11, Color.rgb(255, 220, 0), true))
        top.addView(brand, LinearParams(0, -2, 1f))
        top.addView(text("⚙", 25, white, true).apply {
            gravity = Gravity.CENTER
            setOnClickListener { info("Settings", "Shop Edition • Local-first diagnostics • Customer consent required.") }
        }, lp(42, 48))
        header.addView(top)
        header.addView(text("Repair  |  Diagnostics  |  Official Recovery", 12, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(2))
        })
        header.addView(text("USB CONNECT TO DIAGNOSE", 20, white, true).apply {
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(0, 76, 163), 15, cyan)
            setPadding(dp(12), dp(13), dp(12), dp(13))
            setOnClickListener { screen = "Diagnostics"; render() }
        }, match().apply { topMargin = dp(14) })
        header.addView(text("Detect compatible USB devices • View available diagnostics", 12, white, false).apply {
            gravity = Gravity.CENTER
        })
        root.addView(header, match())

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        scroll.addView(content)
        root.addView(scroll, LinearParams(-1, 0, 1f))

        sectionTitle("SHOP WORKSPACE", "Choose an authorized service task")
        val row1 = row()
        row1.addView(serviceTile("🔧", "Repair", "System & boot guidance", blue) {
            info("Repair", "Run basic diagnostics, record symptoms and follow official manufacturer repair guidance.")
        }, LinearParams(0, dp(112), 1f))
        row1.addView(space(8))
        row1.addView(serviceTile("🛡", "Official Recovery", "Owner-assisted recovery", purple) {
            info("Official Recovery", "Use the device owner's credentials and official account recovery. This app cannot bypass a screen lock or FRP.")
        }, LinearParams(0, dp(112), 1f))
        content.addView(row1, match())

        val row2 = row()
        row2.addView(serviceTile("🧹", "Wipe Guidance", "Data-loss warning", orange) {
            info("Wipe / Factory Reset", "A reset can permanently erase user data and may require the previously synced account. Proceed only through manufacturer-supported steps with explicit customer consent.")
        }, LinearParams(0, dp(112), 1f))
        row2.addView(space(8))
        row2.addView(serviceTile("↻", "Factory Reset", "Official steps only", green) {
            info("Factory Reset", "This app provides guidance only; it does not trigger a reset or bypass account verification.")
        }, LinearParams(0, dp(112), 1f))
        content.addView(row2, match().apply { topMargin = dp(9) })

        sectionTitle("QUICK TOOLS", "Repair studio utilities")
        val tools = row()
        listOf("Device Info", "Diagnostics", "Service Logs", "Settings").forEachIndexed { i, name ->
            tools.addView(text(
                listOf("ⓘ", "∿", "▤", "⚙")[i] + "\n" + name,
                11, blue, true
            ).apply {
                gravity = Gravity.CENTER
                background = rounded(white, 12, Color.rgb(210, 225, 245))
                setPadding(dp(3), dp(9), dp(3), dp(9))
                setOnClickListener {
                    if (name == "Diagnostics" || name == "Device Info") { screen = "Diagnostics"; render() }
                    else info(name, "This section is part of the Shop Edition workspace.")
                }
            }, LinearParams(0, dp(66), 1f))
            if (i < 3) tools.addView(space(6))
        }
        content.addView(tools, match().apply { topMargin = dp(4) })

        val note = text("SAFE & GUIDED PROCESS\nUSB detection does not unlock a phone. Diagnostics depend on what Android and the connected device expose.", 12, ink, true).apply {
            background = rounded(white, 12, Color.rgb(205, 222, 244))
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        content.addView(note, match().apply { topMargin = dp(12) })
        bottomNav()
    }

    private fun diagnosticsScreen() {
        topBar("Automatic Diagnostics", "‹") { screen = "Home"; render() }
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(18))
        }
        scroll.addView(content)
        root.addView(scroll, LinearParams(-1, 0, 1f))

        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val devices = manager.deviceList.values.toList()
        connectedCount = devices.size

        val scan = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(20), dp(16), dp(18))
            background = gradient(navy, navy2, 22)
        }
        scan.addView(text("◎", 72, cyan, true).apply { gravity = Gravity.CENTER })
        scan.addView(text(if (devices.isEmpty()) "USB DEVICE NOT DETECTED" else "USB DEVICE DETECTED", 17, white, true).apply {
            gravity = Gravity.CENTER
        })
        scan.addView(text(if (devices.isEmpty()) "Connect a compatible phone using USB OTG" else "${devices.size} USB device(s) visible to Android", 12, Color.rgb(206, 226, 255), false).apply {
            gravity = Gravity.CENTER
        })
        content.addView(scan, match())

        sectionTitle("DIAGNOSTIC STEPS", "Live USB Host visibility check")
        val steps = card()
        addStatus(steps, "USB Host device list", if (devices.isEmpty()) "No device exposed" else "Detected", devices.isNotEmpty())
        addStatus(steps, "Device descriptors", if (devices.isEmpty()) "Waiting for connection" else "Available", devices.isNotEmpty())
        addStatus(steps, "ADB authorization", "Not checked by this app", false)
        addStatus(steps, "Screen lock state", "Not accessible / not inspected", false)
        addStatus(steps, "Private data access", "Not performed", true)
        content.addView(steps, match())

        if (devices.isNotEmpty()) {
            sectionTitle("CONNECTED DEVICE DETAILS", "USB descriptors only")
            devices.forEach { device ->
                val d = card()
                d.addView(text(device.productName ?: "USB device", 16, ink, true))
                addInfo(d, "Vendor ID", device.vendorId.toString())
                addInfo(d, "Product ID", device.productId.toString())
                addInfo(d, "Interfaces", device.interfaceCount.toString())
                addInfo(d, "Device class", device.deviceClass.toString())
                content.addView(d, match())
            }
        }

        val caution = text("IMPORTANT: A locked phone may charge or appear as a USB device while exposing no diagnostic interface. Full diagnostics may require owner-authorized USB debugging or an official recovery mode. This screen never claims to support locked-screen bypass.", 12, ink, false).apply {
            background = rounded(white, 13, Color.rgb(205, 222, 244))
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        content.addView(caution, match().apply { topMargin = dp(12) })
        content.addView(primaryButton("Generate Diagnostic Report") {
            screen = "Report"; render()
        }, match().apply { topMargin = dp(13) })
        content.addView(secondaryButton("Refresh USB Scan") { render() }, match().apply { topMargin = dp(8) })
    }

    private fun reportScreen() {
        topBar("Diagnostic Report", "‹") { screen = "Diagnostics"; render() }
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(13), dp(13), dp(18))
        }
        scroll.addView(content)
        root.addView(scroll, LinearParams(-1, 0, 1f))

        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val devices = manager.deviceList.values.toList()
        val report = DeviceDiagnostics.collect()

        val device = card()
        device.addView(text("${report.manufacturer} ${report.model}", 20, ink, true))
        addInfo(device, "Android Version", report.androidVersion)
        addInfo(device, "Model / Device Code", report.deviceCode)
        addInfo(device, "Android SDK", report.sdk.toString())
        addInfo(device, "USB OTG", if (devices.isEmpty()) "No USB device detected" else "USB device visible")
        addInfo(device, "Screen Lock Status", "Not inspected")
        content.addView(device, match())

        sectionTitle("DIAGNOSIS RESULT", "Completed checks only")
        val results = card()
        addStatus(results, "USB Host connection", if (devices.isEmpty()) "Not detected" else "Detected", devices.isNotEmpty())
        addStatus(results, "Basic device information", "Available", true)
        addStatus(results, "Repair assessment", "Technician review required", false)
        addStatus(results, "Lock removal", "Not supported", false)
        addStatus(results, "Wipe / Factory reset", "Official owner-authorized process only", false)
        content.addView(results, match())

        sectionTitle("RECOMMENDED ACTION", "Choose a safe next step")
        content.addView(serviceTile("🔧", "Repair", "Record symptoms and inspect", blue) {
            info("Repair", "Use the technician checklist and manufacturer service documentation.")
        }, match())
        content.addView(serviceTile("🛡", "Official Recovery", "Owner-assisted recovery", purple) {
            info("Recovery", "The owner must complete credential and account verification.")
        }, match().apply { topMargin = dp(8) })
        content.addView(serviceTile("🧹", "Reset Guidance", "Warn about permanent data loss", orange) {
            info("Reset warning", "Factory reset erases data and may require the previously synced account. Obtain explicit consent.")
        }, match().apply { topMargin = dp(8) })

        content.addView(secondaryButton("Share basic report") {
            val message = buildString {
                appendLine("ANB Mobile Repair Studio — Diagnostic Report")
                appendLine("Device: ${report.manufacturer} ${report.model}")
                appendLine("Android: ${report.androidVersion} (SDK ${report.sdk})")
                appendLine("USB visible: ${if (devices.isEmpty()) "No" else "Yes"}")
                appendLine("Screen lock: Not inspected")
                appendLine("No private data or credentials accessed.")
            }
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(Intent.createChooser(send, "Share diagnostic summary"))
        }, match().apply { topMargin = dp(12) })
    }

    private fun topBar(title: String, back: String, action: () -> Unit) {
        val bar = row().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setBackgroundColor(navy)
        }
        bar.addView(text(back, 30, white, false).apply {
            gravity = Gravity.CENTER
            setOnClickListener { action() }
        }, lp(42, 48))
        bar.addView(text(title, 18, white, true), LinearParams(0, -2, 1f))
        bar.addView(text("ⓘ", 21, white, true).apply {
            gravity = Gravity.CENTER
            setOnClickListener { info("Diagnostics help", "This report only contains information available to this app. USB visibility is not proof of phone unlock or full diagnostic access.") }
        }, lp(36, 48))
        root.addView(bar, match())
    }

    private fun bottomNav() {
        val nav = row().apply {
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(6), dp(4), dp(6))
            setBackgroundColor(white)
        }
        listOf("⌂\nHome", "▣\nDevices", "⎓\nConnect", "◷\nHistory", "•••\nMore").forEachIndexed { index, label ->
            nav.addView(text(label, 11, if (index == 0 || index == 2) blue else muted, true).apply {
                gravity = Gravity.CENTER
                setOnClickListener {
                    if (index == 1 || index == 2) { screen = "Diagnostics"; render() }
                    else if (index == 0) { screen = "Home"; render() }
                    else info(if (index == 3) "History" else "More", "Repair records and official support guidance are available in the Shop Edition.")
                }
            }, LinearParams(0, dp(52), 1f))
        }
        root.addView(nav, match())
    }

    private fun sectionTitle(title: String, subtitle: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(14), dp(2), dp(8))
        }
        box.addView(text(title, 15, ink, true))
        box.addView(text(subtitle, 11, muted, false))
        content.addView(box, match())
    }

    private fun serviceTile(icon: String, title: String, sub: String, color: Int, action: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(9), dp(8), dp(9))
            background = gradient(color, Color.rgb(
                (Color.red(color) * 0.65).toInt().coerceIn(0,255),
                (Color.green(color) * 0.65).toInt().coerceIn(0,255),
                (Color.blue(color) * 0.65).toInt().coerceIn(0,255)
            ), 15)
            addView(text(icon, 25, white, true).apply { gravity = Gravity.CENTER })
            addView(text(title, 14, white, true).apply { gravity = Gravity.CENTER })
            addView(text(sub, 10, white, false).apply { gravity = Gravity.CENTER })
            setOnClickListener { action() }
        }
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(13), dp(12), dp(13), dp(12))
        background = rounded(white, 14, Color.rgb(207, 223, 244))
        elevation = dp(2).toFloat()
    }

    private fun addStatus(parent: LinearLayout, title: String, status: String, ok: Boolean) {
        val r = row().apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(4), 0, dp(4)) }
        r.addView(text(if (ok) "●" else "○", 16, if (ok) green else orange, true), lp(27, -2))
        r.addView(text(title, 12, ink, true), LinearParams(0, -2, 1f))
        r.addView(text(status, 11, if (ok) green else muted, true), wrap())
        parent.addView(r, match())
        parent.addView(View(this).apply { setBackgroundColor(Color.rgb(230, 237, 247)) }, lp(-1, 1))
    }

    private fun addInfo(parent: LinearLayout, key: String, value: String) {
        val r = row().apply { setPadding(0, dp(3), 0, dp(3)) }
        r.addView(text(key, 11, muted, false), LinearParams(0, -2, 1f))
        r.addView(text(value, 11, ink, true), wrap())
        parent.addView(r, match())
    }

    private fun primaryButton(title: String, action: () -> Unit) = Button(this).apply {
        text = title; isAllCaps = false; textSize = 14f; setTextColor(white)
        background = rounded(blue, 13, blue); minHeight = dp(48)
        setOnClickListener { action() }
    }

    private fun secondaryButton(title: String, action: () -> Unit) = Button(this).apply {
        text = title; isAllCaps = false; textSize = 13f; setTextColor(blue)
        background = rounded(pale, 13, Color.rgb(197, 218, 246)); minHeight = dp(46)
        setOnClickListener { action() }
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setLineSpacing(dp(2).toFloat(), 1f)
    }

    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
    private fun space(width: Int) = View(this).apply { layoutParams = lp(width, 1) }
    private fun lp(width: Int, height: Int) = LinearLayout.LayoutParams(dp(width), if (height < 0) height else dp(height))
    private fun wrap() = LinearLayout.LayoutParams(-2, -2)
    private fun match() = LinearLayout.LayoutParams(-1, -2)
    private fun LinearParams(width: Int, height: Int, weight: Float) = LinearLayout.LayoutParams(width, height, weight)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int, stroke: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat(); setStroke(dp(1), stroke)
    }

    private fun gradient(start: Int, end: Int, radius: Int) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, intArrayOf(start, end)
    ).apply { cornerRadius = dp(radius).toFloat() }

    private fun info(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
