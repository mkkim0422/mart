package com.rldjrgo.grocerynote.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

/**
 * Bottom banner. The ad loads asynchronously over the network, so the slot is
 * given its full adaptive-banner height UP FRONT (a reserved Box) — without
 * this the AdView is 0dp until the ad arrives and then "pops in", shoving the
 * UI up. Reserving the height keeps the layout stable (no jump).
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    viewModel: AdBannerViewModel = hiltViewModel(),
) {
    val isAdRemoved by viewModel.isAdRemoved.collectAsStateWithLifecycle(initialValue = false)
    if (isAdRemoved) return

    val context = LocalContext.current
    val configuration: Configuration = LocalConfiguration.current
    val adWidth = configuration.screenWidthDp

    val adSize = remember(adWidth) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.height.dp),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(adSize)
                    adUnitId = BuildConfig.AD_UNIT_BANNER_ID
                    loadAd(AdRequest.Builder().build())
                }
            },
        )
    }
}
