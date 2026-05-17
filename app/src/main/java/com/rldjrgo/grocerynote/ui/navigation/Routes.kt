package com.rldjrgo.grocerynote.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val COMPLETED = "completed"
    const val SETTINGS = "settings"
    const val STORE_MANAGE = "store_manage"

    // Optional deep-link arguments for Home (set by widget OpenStoreAction).
    const val HOME_DEEPLINK_STORE_ARG = "selected_store_id"
    const val HOME_DEEPLINK_ITEM_ARG = "highlight_item_id"
}
