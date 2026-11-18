package org.fossify.gallery.activities

import android.os.Bundle
import org.fossify.commons.extensions.viewBinding
import org.fossify.gallery.BuildConfig
import org.fossify.gallery.R
import org.fossify.gallery.databinding.ActivityLuminaAboutBinding

class LuminaAboutActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityLuminaAboutBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupEdgeToEdge(
            padTopSystem = listOf(binding.aboutToolbar),
            padBottomSystem = listOf(binding.aboutRoot)
        )

        binding.aboutToolbar.setNavigationOnClickListener { finish() }
        binding.aboutVersion.text = getString(R.string.lumina_version_label, BuildConfig.VERSION_NAME)
    }
}
