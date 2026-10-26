package com.anb.recovery

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val navy = Color.rgb(23, 37, 65)
    private val cyan = Color.rgb(0, 158, 180)
    private val ink = Color.rgb(36, 48, 67)
    private val muted = Color.rgb(105, 119, 139)
    private val page = Color.rgb(244, 247, 252)
    private val white = Color.WHITE

    private lateinit var content: LinearLayout
    private lateinit var consent: CheckBox
    private lateinit var notes: EditText
    private lateinit var summary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = navy
        window.navigationBarColor = navy

        val report = DeviceDiagnostics.collect()

        val scroll = ScrollView(this).apply {
            setBackgroundColor(page)
            clipToPadding = false
            isFillViewport = true
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(28))
        }

        scroll.addView(content)
        setContentView(scroll)

        addText(content, "ANB  •  RECOVERY TOOLS", 12, cyan, true)
        addText(content, "Android Recovery", 29, navy, true)
        addText(
            content,
            "Authorized repair  •  Device diagnostics  •  Official recovery",
            14, muted, false
        )
        addSpace(18)

        val deviceCard = addCard(
            "Device diagnostics",
            "Basic information collected locally on this device."
        )
        addPill(deviceCard, "DEVICE INFORMATION")
        addText(deviceCard, "Manufacturer", 12, muted, true)
        addText(deviceCard, report.manufacturer, 17, ink, true)
        addText(deviceCard, "Model", 12, muted, true)
        addText(deviceCard, report.model, 16, ink, false)
        addText(deviceCard, report.androidVersion, 15, ink, false)
        addText(deviceCard, "Device code: ${report.deviceCode}", 14, muted, false)

        addSpace(12)

        val repairCard = addCard(
            "Repair case",
            "Never enter customer PINs, passwords or account recovery codes."
        )

        consent = CheckBox(this).apply {
            text = "Customer has authorized this diagnostic / repair"
            textSize = 14f
            setTextColor(ink)
            buttonTintList = android.content.res.ColorStateList.valueOf(cyan)
        }
        repairCard.addView(consent, matchWrap())

        notes = EditText(this).apply {
            hint = "Technician notes (optional)"
            textSize = 14f
            setTextColor(ink)
            setHintTextColor(muted)
            gravity = android.view.Gravity.TOP
            minLines = 3
            maxLines = 6
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(Color.rgb(248, 250, 253), 12, Color.rgb(222, 229, 239))
        }
        val noteParams = matchWrap()
        noteParams.topMargin = dp(12)
        repairCard.addView(notes, noteParams)

        val reportButton = makeButton("CREATE LOCAL REPAIR SUMMARY")
        repairCard.addView(reportButton, matchWrap())

        summary = TextView(this).apply {
            text = "Your repair summary will appear here after you confirm authorization."
            textSize = 14f
            setTextColor(ink)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(white, 12, Color.rgb(226, 232, 241))
            visibility = View.GONE
        }
        val summaryParams = matchWrap()
        summaryParams.topMargin = dp(12)
        repairCard.addView(summary, summaryParams)

        reportButton.setOnClickListener {
            if (!consent.isChecked) {
                Toast.makeText(
                    this,
                    "Confirm customer authorization first.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            try {
                summary.text = RepairCaseManager.createLocalSummary(
                    consent.isChecked,
                    report,
                    notes.text.toString()
                )
                summary.visibility = View.VISIBLE
            } catch (e: IllegalArgumentException) {
                Toast.makeText(
                    this,
                    e.message ?: "Unable to create repair summary.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        addSpace(12)

        val guideCard = addCard(
            "Official recovery checklist",
            "This app does not bypass screen locks, encryption or FRP."
        )

        RecoveryGuide.steps.forEachIndexed { index, step ->
            addText(guideCard, "${index + 1}. $step", 14, ink, false)
        }

        val googleButton = makeButton("OPEN GOOGLE ACCOUNT RECOVERY")
        guideCard.addView(googleButton, matchWrap())
        googleButton.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://accounts.google.com/signin/recovery")
                )
            )
        }

        addSpace(18)
        addText(content, "ANB Android Recovery  •  Version 0.2.0", 12, muted, false)
        addText(content, "Local-first diagnostics  •  No automatic upload", 12, muted, false)
    }

    private fun addCard(title: String, subtitle: String): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(white, 18, Color.rgb(232, 237, 245))
            elevation = dp(2).toFloat()
        }

        val params = matchWrap()
        params.bottomMargin = dp(8)
        content.addView(card, params)

        addText(card, title, 19, navy, true)
        addText(card, subtitle, 13, muted, false)
        addSpaceIn(card, 12)

        return card
    }

    private fun addPill(parent: LinearLayout, label: String) {
        val pill = TextView(this).apply {
            text = label
            textSize = 11f
            setTextColor(cyan)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = rounded(Color.rgb(231, 249, 251), 30, Color.TRANSPARENT)
        }
        val params = wrapWrap()
        params.bottomMargin = dp(10)
        parent.addView(pill, params)
    }

    private fun addText(
        parent: LinearLayout,
        value: String,
        size: Int,
        color: Int,
        bold: Boolean
    ) {
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

    private fun makeButton(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 13f
            isAllCaps = false
            setTextColor(white)
            background = rounded(navy, 12, Color.TRANSPARENT)
            elevation = dp(2).toFloat()
            val p = dp(8)
            setPadding(p, p, p, p)
        }
    }

    private fun rounded(
        color: Int,
        radius: Int,
        stroke: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != Color.TRANSPARENT) {
                setStroke(dp(1), stroke)
            }
        }
    }

    private fun addSpace(height: Int) {
        content.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun addSpaceIn(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun matchWrap() =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun wrapWrap() =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
