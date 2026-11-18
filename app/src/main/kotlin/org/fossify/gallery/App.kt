package org.fossify.gallery

import android.graphics.Color
import com.github.ajalt.reprint.core.Reprint
import com.squareup.picasso.Downloader
import com.squareup.picasso.Picasso
import okhttp3.Request
import okhttp3.Response
import org.fossify.commons.FossifyApp
import org.fossify.gallery.extensions.config
import org.fossify.gallery.helpers.AdManager
import org.fossify.gallery.helpers.FOLDER_STYLE_ROUNDED_CORNERS

class App : FossifyApp() {

    override val isAppLockFeatureAvailable = true

    override fun onCreate() {
        applyLuminaBrandDefaults()
        super.onCreate()
        Reprint.initialize(this)
        Picasso.setSingletonInstance(Picasso.Builder(this).downloader(object : Downloader {
            override fun load(request: Request) = Response.Builder().build()

            override fun shutdown() {}
        }).build())
        AdManager.init(this)
    }

    private fun applyLuminaBrandDefaults() {
        if (!config.wasLuminaThemeApplied) {
            val obsidianBg = Color.parseColor("#0B0D14")
            val primaryText = Color.parseColor("#F4F6FF")
            val primaryViolet = Color.parseColor("#7C5CFF")
            val accentLavender = Color.parseColor("#A78BFA")

            config.isSystemThemeEnabled = false
            config.isGlobalThemeEnabled = false
            config.backgroundColor = obsidianBg
            config.textColor = primaryText
            config.primaryColor = primaryViolet
            config.accentColor = accentLavender
            config.customBackgroundColor = obsidianBg
            config.customTextColor = primaryText
            config.customPrimaryColor = primaryViolet
            config.customAccentColor = accentLavender
            config.folderStyle = FOLDER_STYLE_ROUNDED_CORNERS
            config.fileRoundedCorners = true
            config.thumbnailSpacing = 6
            config.blackBackground = true
            config.wasLuminaThemeApplied = true
        }
    }
}
