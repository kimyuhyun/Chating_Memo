package com.hongslab.chating_memo.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R


/**
 * Manifests.xml 의
 * android:launchMode="singleTask"
 * 삭제 해야 한다!!
 *
 * android:launchMode="singleTop"
 * 도 된다!
 */

class AdUtils(private val context: Context) {


    /*
    companion object {
        var isInterstitialAdOpen = false
    }

    private var mInterstitialAd: InterstitialAd? = null
    private var mInterstitialListener: OnAdListener? = null


    interface OnAdListener {
        fun onAdDismissed()
        fun onNotInForeground()
    }

    interface OnNativeAdListener {
        fun onNativeAdLoaded(nativeAd: NativeAd)
    }

        fun setInterstitialListener(onAdListener: OnAdListener) {
            mInterstitialListener = onAdListener
        }

        fun loadInterstitial() {
            val adRequest = AdRequest.Builder().build()

            val adUnit = if (MyApplication.DEBUG) {
                "ca-app-pub-3940256099942544/8691691433"
            } else {
                activity.getString(R.string.ad_inters)
            }

            InterstitialAd.load(activity, adUnit,
                adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Dlog.d("@@@@ onAdDismissedFullScreenContent")
                                mInterstitialAd = null
                                isInterstitialAdOpen = false

                                // 광고가 종료되면 콜백 호출
                                mInterstitialListener?.onAdDismissed()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Dlog.d("@@@@ onAdFailedToShowFullScreenContent: ${adError.code}, ${adError.message}")
                                mInterstitialAd = null
                                isInterstitialAdOpen = false
                                // 광고 표시 실패시에도 콜백 호출
                                if (adError.code == 3) {
                                    mInterstitialListener?.onNotInForeground()
                                } else {
                                    mInterstitialListener?.onAdDismissed()
                                }
                            }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mInterstitialAd = null
                        isInterstitialAdOpen = false

                        // 광고 로드 실패시에도 콜백 호출
                        mInterstitialListener?.onAdDismissed()
                    }
                })
        }

        fun showInterstitial() {
            isInterstitialAdOpen = true
            mInterstitialAd?.show(activity)
        }
    */

    fun getExitBanner(): AdView? {
        Dlog.d("getExitBanner")

        if (MyApplication.DEBUG) {
            return null
        }

        val adRequest = AdRequest.Builder().build()
        val adView = AdView(context)
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE)
        adView.adUnitId = if (MyApplication.DEBUG) "ca-app-pub-3940256099942544/6300978111" else context.getString(R.string.ad_exit)
        adView.loadAd(adRequest)
        return adView
    }

    fun getBanner(): AdView? {
        Dlog.d("getBanner")

        if (MyApplication.DEBUG) {
            return null
        }

        MyApplication.AD_SIZE?.let {
            val adRequest = AdRequest.Builder().build()
            val adView = AdView(context)
            adView.setAdSize(it)
            adView.adUnitId = if (MyApplication.DEBUG) "ca-app-pub-3940256099942544/6300978111" else context.getString(R.string.ad_banner)
            adView.loadAd(adRequest)
            return adView
        }
        return null
    }

    /*fun loadNativeBanner(listener: OnNativeAdListener) {
        Dlog.d("loadNativeBanner")
        val adLoader = AdLoader.Builder(activity, if (MyApplication.DEBUG) "ca-app-pub-3940256099942544/2247696110" else activity.getString(R.string.ad_native))
            .forNativeAd { nativeAd ->
                Dlog.d("${nativeAd?.headline}")
                nativeAd.let {
                    if (it != null) {
                        listener.onNativeAdLoaded(it)
                    }
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Code to be executed when an ad request fails.
                    Log.d("####", "Ad failed to load: " + adError.message)
                }
            })
            .build()
        adLoader.loadAds(AdRequest.Builder().build(), 4)
    }*/


    /*fun getNativeAdView(ad: NativeAd): NativeAdView {
        // 레이아웃 인플레이트
        val nativeAdView = LayoutInflater.from(activity)
            .inflate(R.layout.native_ad_item2, null) as NativeAdView

        // Set other ad assets.
        val iconView = nativeAdView.findViewById<ImageView>(R.id.icon)
        val primaryView = nativeAdView.findViewById<TextView>(R.id.primary)
        val bodyView = nativeAdView.findViewById<TextView>(R.id.body)
        val secondaryView = nativeAdView.findViewById<TextView>(R.id.secondary)
        val callToActionView = nativeAdView.findViewById<TextView>(R.id.call_to_action)

        val store = ad.store
        val advertiser = ad.advertiser
        val headline = ad.headline
        val body = ad.body
        val cta = ad.callToAction
        val starRating = ad.starRating
        val icon = ad.icon

        if (icon != null) {
            iconView.visibility = View.VISIBLE
            iconView.setImageDrawable(icon.drawable)
        } else {
            iconView.visibility = View.GONE
        }
        primaryView.text = "$headline"
        bodyView.text = body


        nativeAdView.iconView = iconView
        nativeAdView.headlineView = primaryView
        nativeAdView.bodyView = bodyView
        if (!TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)) {
            secondaryView.text = store
            nativeAdView.storeView = secondaryView
        } else if (!TextUtils.isEmpty(advertiser)) {
            secondaryView.text = advertiser
            nativeAdView.advertiserView = secondaryView
        }

        if (cta != null) {
            callToActionView.visibility = View.VISIBLE
            callToActionView.text = cta
            callToActionView.setOnClickListener {
                ad.performClick(Bundle())
            }
        } else {
            callToActionView.visibility = View.GONE
        }
        nativeAdView.callToActionView = callToActionView

        nativeAdView.setNativeAd(ad)

        return nativeAdView
    }*/

}