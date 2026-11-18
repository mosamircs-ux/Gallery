package org.fossify.gallery.helpers

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.fossify.gallery.BuildConfig

object AdManager {
    private const val TAG = "LuminaAdManager"
    private const val INTERSTITIAL_COOLDOWN_MS = 90_000L
    private const val ACTIONS_PER_INTERSTITIAL = 3

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isLoadingInterstitial = false

    private var interstitialAd: InterstitialAd? = null
    private var lastInterstitialShownAt = 0L
    private var navigationCounter = 0

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        try {
            MobileAds.initialize(context.applicationContext) {
                preloadInterstitial(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.w(TAG, "MobileAds initialization failed: ${e.message}")
        }
    }

    fun attachAdaptiveBanner(activity: Activity, container: FrameLayout): AdView? {
        return try {
            val adView = AdView(activity)
            adView.adUnitId = BuildConfig.ADMOB_BANNER_ID
            adView.setAdSize(getAdaptiveBannerSize(activity, container))
            container.removeAllViews()
            container.addView(adView)

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    container.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Banner failed to load: ${adError.message}")
                }
            }

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView
        } catch (e: Exception) {
            Log.w(TAG, "Unable to attach adaptive banner: ${e.message}")
            container.visibility = View.GONE
            null
        }
    }

    fun preloadInterstitial(context: Context) {
        if (isLoadingInterstitial || interstitialAd != null) return
        isLoadingInterstitial = true
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                BuildConfig.ADMOB_INTERSTITIAL_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        Log.d(TAG, "Interstitial failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isLoadingInterstitial = false
            Log.w(TAG, "Interstitial load error: ${e.message}")
        }
    }

    fun maybeShowInterstitial(activity: Activity, onContinue: () -> Unit) {
        navigationCounter++
        val now = SystemClock.elapsedRealtime()
        val cooldownPassed = lastInterstitialShownAt == 0L || (now - lastInterstitialShownAt) >= INTERSTITIAL_COOLDOWN_MS
        val readyAd = interstitialAd

        if (readyAd != null && cooldownPassed && navigationCounter % ACTIONS_PER_INTERSTITIAL == 0 && !activity.isFinishing && !activity.isDestroyed) {
            interstitialAd = null
            lastInterstitialShownAt = now
            var continued = false
            val safeContinue = {
                if (!continued) {
                    continued = true
                    onContinue()
                }
            }

            readyAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    preloadInterstitial(activity.applicationContext)
                    safeContinue()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    preloadInterstitial(activity.applicationContext)
                    safeContinue()
                }
            }

            try {
                readyAd.show(activity)
            } catch (e: Exception) {
                preloadInterstitial(activity.applicationContext)
                safeContinue()
            }
        } else {
            if (readyAd == null) {
                preloadInterstitial(activity.applicationContext)
            }
            onContinue()
        }
    }

    private fun getAdaptiveBannerSize(activity: Activity, container: FrameLayout): AdSize {
        val density = activity.resources.displayMetrics.density
        var adWidthPixels = container.width.toFloat()
        if (adWidthPixels <= 0f) {
            adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.windowManager.currentWindowMetrics.bounds.width().toFloat()
            } else {
                @Suppress("DEPRECATION")
                val metrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay.getMetrics(metrics)
                metrics.widthPixels.toFloat()
            }
        }
        val adWidthDp = (adWidthPixels / density).toInt().coerceAtLeast(320)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidthDp)
    }
}
