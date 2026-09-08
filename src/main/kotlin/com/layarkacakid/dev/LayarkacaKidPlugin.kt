package com.layarkacakid.dev

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.layarkacakid.dev.extractors.DadadidiExtractor
import com.layarkacakid.dev.extractors.VideonodeExtractor

@CloudstreamPlugin
class LayarkacaKidPlugin : Plugin() {

    override fun load(context: Context) {
        // Register the main provider
        registerMainAPI(LayarkacaKidProvider())

        // Register custom extractors
        registerExtractorAPI(VideonodeExtractor())
        registerExtractorAPI(DadadidiExtractor())

        // Register settings dialog for custom domain configuration
        openSettings = { ctx ->
            showSettingsDialog(ctx)
        }
    }

    private fun showSettingsDialog(context: Context) {
        val currentDomain = getKey<String>(LayarkacaKidProvider.PREF_DOMAIN_KEY)
            ?: LayarkacaKidProvider.DEFAULT_DOMAIN

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val infoText = TextView(context).apply {
            text = "Enter a custom LK21 domain or choose from known working mirrors:"
            textSize = 14f
            setPadding(0, 0, 0, 24)
        }
        container.addView(infoText)

        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setText(currentDomain)
            hint = LayarkacaKidProvider.DEFAULT_DOMAIN
            setSelectAllOnFocus(true)
        }
        container.addView(input)

        // Presets container
        val presetsLabel = TextView(context).apply {
            text = "Preset Mirrors:"
            textSize = 12f
            setPadding(0, 24, 0, 8)
        }
        container.addView(presetsLabel)

        val presetsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val btnMirror1 = Button(context).apply {
            text = "tv12.lk21official.cc"
            textSize = 11f
            setOnClickListener {
                input.setText(LayarkacaKidProvider.DEFAULT_DOMAIN)
            }
        }
        val btnMirror2 = Button(context).apply {
            text = "lk21.de"
            textSize = 11f
            setOnClickListener {
                input.setText(LayarkacaKidProvider.ALT_DOMAIN)
            }
        }
        presetsLayout.addView(btnMirror1)
        presetsLayout.addView(btnMirror2)
        container.addView(presetsLayout)

        val dialog = AlertDialog.Builder(context)
            .setTitle("LK21 Domain Settings")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newUrl = input.text.toString().trim().removeSuffix("/")
                if (newUrl.isNotBlank() && (newUrl.startsWith("http://") || newUrl.startsWith("https://"))) {
                    setKey(LayarkacaKidProvider.PREF_DOMAIN_KEY, newUrl)
                    Toast.makeText(context, "Domain updated to: $newUrl", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid URL! Must start with http:// or https://", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Reset Default") { _, _ ->
                setKey(LayarkacaKidProvider.PREF_DOMAIN_KEY, LayarkacaKidProvider.DEFAULT_DOMAIN)
                Toast.makeText(context, "Domain reset to: ${LayarkacaKidProvider.DEFAULT_DOMAIN}", Toast.LENGTH_SHORT).show()
            }
            .create()

        dialog.show()
    }
}
