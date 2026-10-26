package com.anb.recovery

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.content.Intent
import android.net.Uri

class MainActivity : Activity() {
    private val navy = Color.rgb(13, 24, 44)
    private val cyan = Color.rgb(0, 188, 212)
    private val ink = Color.rgb(30, 41, 59)
    private val muted = Color.rgb(91, 105, 125)
    private lateinit var content: LinearLayout
    private lateinit var consent: CheckBox
    private lateinit var notes: EditText
    private lateinit var summary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply { setBackgroundColor(Color.rgb(245, 248, 252)) }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }
        scroll.addView(content)
        setContentView(scroll)

        addText("ANB", 14, cyan, true)
        addText("Android Recovery", 28, navy, true)
        addText("Authorized repair • Device diagnostics • Official recovery", 14, muted, false)
        addCard("Device diagnostics", "Basic device information is read locally.")
        val report = DeviceDiagnostics.collect()
        addText("Manufacturer: ${report.manufacturer}", 15, ink, true)
        addText("Model: ${report.model}", 15, ink, false)
        addText("Android: ${report.androidVersion} (SDK ${report.sdk})", 15, ink, false)
        addText("Device code: ${report.deviceCode}", 15, ink, false)

        addCard("Repair case", "Do not enter customer PINs or passwords.")
        consent = CheckBox(this).apply {
            text = "Customer has authorized this diagnostic / repair"
            setTextColor(ink)
        }
        content.addView(consent, matchWrap())
        notes = EditText(this).apply {
            hint = "Technician notes"
            minLines = 3
            gravity = Gravity.TOP
            setTextColor(ink)
        }
        content.addView(notes, matchWrap())
        content.addView(Button(this).apply {
            text = "Create local repair summary"
            setOnClickListener {
                try {
                    summary.text = RepairCaseManager.createLocalSummary(
                        consent.isChecked, report, notes.text.toString()
                    )
                    summary.visibility = TextView.VISIBLE
                } catch (_: IllegalArgumentException) {
                    Toast.makeText(this@MainActivity, "Confirm customer authorization first", Toast.LENGTH_LONG).show()
                }
            }
        }, matchWrap())
        summary = TextView(this).apply {
            visibility = TextView.GONE
            setTextColor(ink)
            textSize = 13f
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.WHITE)
        }
        content.addView(summary, matchWrap())

        addCard("Official recovery checklist", "This app does not bypass screen locks, encryption, or FRP.")
        RecoveryGuide.steps.forEachIndexed { index, step -> addText("${index + 1}. $step", 14, ink, false) }
        content.addView(Button(this).apply {
            text = "Open Google Account Recovery"
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/signin/recovery")))
            }
        }, matchWrap())
        addText("Version 0.1.0 • anbsolutioner@gmail.com", 12, muted, false)
    }

    private fun addCard(title: String, subtitle: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setBackgroundColor(Color.WHITE)
        }
        box.addView(TextView(this).apply {
            text = title; textSize = 18f; setTypeface(null, Typeface.BOLD); setTextColor(navy)
        })
        box.addView(TextView(this).apply { text = subtitle; textSize = 13f; setTextColor(muted) })
        content.addView(box, matchWrap().apply { topMargin = dp(18); bottomMargin = dp(10) })
    }

    private fun addText(value: String, size: Int, color: Int, bold: Boolean) {
        content.addView(TextView(this).apply {
            text = value; textSize = size.toFloat(); setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4))
        }, matchWrap())
    }
    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
