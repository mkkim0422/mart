package com.rldjrgo.grocerynote.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.rldjrgo.grocerynote.BuildConfig
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdBannerViewModel @Inject constructor(
    settings: SettingsDataStore,
) : androidx.lifecycle.ViewModel() {
    val isAdRemoved = settings.isAdRemoved
}

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    viewModel: AdBannerViewModel = hiltViewModel(),
) {
    val isAdRemoved by viewModel.isAdRemoved.collectAsStateWithLifecycle(initialValue = false)
    if (isAdRemoved) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration: Configuration = LocalConfiguration.current
    val adWidth = configuration.screenWidthDp

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                adUnitId = BuildConfig.AD_UNIT_BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
