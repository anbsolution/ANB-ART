package com.anb.recovery

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 16, 35)
    private val panel = Color.rgb(13, 29, 58)
    private val panel2 = Color.rgb(18, 39, 72)
    private val blue = Color.rgb(22, 191, 255)
    private val mint = Color.rgb(91, 235, 180)
    private val ink = Color.rgb(238, 246, 255)
    private val muted = Color.rgb(163, 184, 211)
    private val line = Color.rgb(37, 65, 102)

    private lateinit var content: LinearLayout
    private lateinit var consent: CheckBox
    private lateinit var notes: EditText
    private lateinit var summary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0

        val report = DeviceDiagnostics.collect()
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            clipToPadding = false
            isFillViewport = true
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(28))
        }
        scroll.addView(content)
        setContentView(scroll)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = rounded(panel2, 22, line)
        }
        content.addView(hero, matchWrap())
        addText(hero, "ANB  /  RECOVERY PRO", 12, blue, true)
        addText(hero, "Android Recovery", 29, ink, true)
        addText(hero, "Authorized diagnostics. Clear repair workflow.", 14, muted, false)
        addSpaceIn(hero, 14)
        val status = TextView(this).apply {
            text = "●  LOCAL-FIRST  ·  NO AUTOMATIC UPLOAD"
            textSize = 11f
            setTextColor(mint)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(11), dp(8), dp(11), dp(8))
            background = rounded(bg, 30, line)
        }
        hero.addView(status, wrapWrap())

        addSpace(14)
        val deviceCard = addCard("01  ·  Device diagnostics", "Basic device details stay on this device.")
        addPill(deviceCard, "DEVICE INFO")
        addPair(deviceCard, "Manufacturer", report.manufacturer)
        addPair(deviceCard, "Model", report.model)
        addPair(deviceCard, "Android version", "${report.androidVersion}  ·  SDK ${report.sdk}")
        addPair(deviceCard, "Device code", report.deviceCode)

        addSpace(12)
        val repairCard = addCard("02  ·  Repair case", "Confirm customer authorization before creating a local report.")
        addText(repairCard, "Never enter customer PINs, passwords or account recovery codes.", 12, muted, false)
        consent = CheckBox(this).apply {
            text = "Customer has authorized this diagnostic / repair"
            textSize = 14f
            setTextColor(ink)
            buttonTintList = ColorStateList.valueOf(blue)
            setPadding(0, dp(7), 0, dp(7))
        }
        repairCard.addView(consent, matchWrap())
        notes = EditText(this).apply {
            hint = "Technician notes (optional)"
            textSize = 14f
            setTextColor(ink)
            setHintTextColor(muted)
            gravity = Gravity.TOP
            minLines = 3
            maxLines = 6
            setPadding(dp(13), dp(12), dp(13), dp(12))
            background = rounded(bg, 14, line)
        }
        val noteParams = matchWrap().apply { topMargin = dp(8) }
        repairCard.addView(notes, noteParams)
        val reportButton = makeButton("Create local repair summary", true)
        val buttonParams = matchWrap().apply { topMargin = dp(12) }
        repairCard.addView(reportButton, buttonParams)
        summary = TextView(this).apply {
            text = "The repair summary will appear here after authorization is confirmed."
            textSize = 14f
            setTextColor(ink)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(bg, 14, line)
            visibility = View.GONE
            setTextIsSelectable(true)
        }
        val summaryParams = matchWrap().apply { topMargin = dp(12) }
        repairCard.addView(summary, summaryParams)

        // Explicit, consent-gated sharing; nothing is sent automatically.
        val shareButton = makeButton("Share local repair summary", false).apply {
            isEnabled = false
            alpha = 0.55f
            contentDescription = "Share the generated local repair summary"
        }
        val shareParams = matchWrap().apply { topMargin = dp(8) }
        repairCard.addView(shareButton, shareParams)
        var summaryNotesSnapshot: String? = null
        // Invalidate any stale report when consent or technician notes change.
        fun invalidateSummary() {
            summaryNotesSnapshot = null
            summary.visibility = View.GONE
            summary.text = "The repair summary will appear here after authorization is confirmed."
            shareButton.isEnabled = false
            shareButton.alpha = 0.55f
        }

        consent.setOnCheckedChangeListener { _, _ -> invalidateSummary() }
        notes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { invalidateSummary() }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        shareButton.setOnClickListener {
            if (summary.visibility != View.VISIBLE || !RepairCaseManager.canShareSummary(summaryNotesSnapshot, notes.text.toString(), consent.isChecked)) {
                Toast.makeText(this, "Create a summary after confirming customer authorization first.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Review before sharing")
                .setMessage(RepairCaseManager.createShareReviewMessage(summary.text.toString()))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue to share") { _, _ ->
                    if (summary.visibility != View.VISIBLE ||
                        !RepairCaseManager.canShareSummary(
                            summaryNotesSnapshot, notes.text.toString(), consent.isChecked
                        )
                    ) {
                        Toast.makeText(
                            this,
                            "The report changed. Create a new summary before sharing.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setPositiveButton
                    }
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "ANB Android Recovery - Repair Summary")
                        putExtra(Intent.EXTRA_TEXT, summary.text.toString())
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share repair summary"))
                }
                .show()
        }

        reportButton.setOnClickListener {
            if (!consent.isChecked) {
                Toast.makeText(this, "Confirm customer authorization first.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            try {
                summary.text = RepairCaseManager.createLocalSummary(consent.isChecked, report, notes.text.toString())
                summaryNotesSnapshot = notes.text.toString()
                summary.visibility = View.VISIBLE
                shareButton.isEnabled = true
                shareButton.alpha = 1f
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, e.message ?: "Unable to create repair summary.", Toast.LENGTH_LONG).show()
            }
        }

        addSpace(12)
        val guideCard = addCard("03  ·  Official recovery checklist", "Use supported recovery options only.")
        addText(guideCard, "This app does not bypass screen locks, encryption or Factory Reset Protection (FRP).", 13, muted, false)
        RecoveryGuide.steps.forEachIndexed { index, step ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
            }
            val number = TextView(this).apply {
                text = (index + 1).toString().padStart(2, '0')
                textSize = 12f
                setTextColor(blue)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(7), dp(10), dp(7))
            }
            val body = TextView(this).apply {
                text = step
                textSize = 14f
                setTextColor(ink)
                setPadding(0, dp(6), 0, dp(8))
                setLineSpacing(dp(2).toFloat(), 1f)
            }
            row.addView(number, LinearLayout.LayoutParams(dp(34), ViewGroup.LayoutParams.WRAP_CONTENT))
            row.addView(body, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            guideCard.addView(row, matchWrap())
        }
        val googleButton = makeButton("Open Google Account Recovery", false)
        val googleParams = matchWrap().apply { topMargin = dp(10) }
        guideCard.addView(googleButton, googleParams)
        googleButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/signin/recovery")))
        }

        addSpace(20)
        addText(content, "ANB Android Recovery  ·  Version 0.7.0", 12, muted, true)
        addText(content, "Local diagnostics  ·  Consent gated  ·  No auto-upload", 12, muted, false)
    }

    private fun addCard(title: String, subtitle: String): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(panel, 18, line)
            elevation = dp(2).toFloat()
        }
        val params = matchWrap().apply { bottomMargin = dp(8) }
        content.addView(card, params)
        addText(card, title, 18, ink, true)
        addText(card, subtitle, 13, muted, false)
        addSpaceIn(card, 10)
        return card
    }

    private fun addPair(parent: LinearLayout, label: String, value: String) {
        addText(parent, label.uppercase(), 10, muted, true)
        addText(parent, value, 15, ink, false)
        addSpaceIn(parent, 4)
    }

    private fun addPill(parent: LinearLayout, label: String) {
        val pill = TextView(this).apply {
            text = label
            textSize = 10f
            setTextColor(blue)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = rounded(bg, 30, line)
        }
        val params = wrapWrap().apply { bottomMargin = dp(10) }
        parent.addView(pill, params)
    }

    private fun addText(parent: LinearLayout, value: String, size: Int, color: Int, bold: Boolean) {
        val view = TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4))
            setLineSpacing(dp(2).toFloat(), 1f)
        }
        parent.addView(view, matchWrap())
    }

    private fun makeButton(label: String, primary: Boolean): Button = Button(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        setTextColor(if (primary) bg else ink)
        background = rounded(if (primary) blue else panel2, 14, if (primary) blue else line)
        minHeight = dp(48)
        elevation = dp(1).toFloat()
        val p = dp(8)
        setPadding(p, p, p, p)
    }

    private fun rounded(color: Int, radius: Int, stroke: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != Color.TRANSPARENT) setStroke(dp(1), stroke)
    }

    private fun addSpace(height: Int) {
        content.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun addSpaceIn(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun matchWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun wrapWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
