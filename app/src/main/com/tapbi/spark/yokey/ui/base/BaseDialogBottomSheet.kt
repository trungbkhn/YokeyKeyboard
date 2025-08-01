package com.tapbi.spark.yokey.ui.base

import android.content.SharedPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tapbi.spark.yokey.data.repository.ThemeRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseDialogBottomSheet:  BottomSheetDialogFragment() {



    @JvmField
    @Inject
    var mPrefs: SharedPreferences? = null

    @JvmField
    @Inject
    var themeRepository: ThemeRepository? = null

}