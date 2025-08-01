package com.android.inputmethod.keyboard.viewGif

import com.giphy.sdk.core.models.enums.MediaType
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.views.GiphyGridView

object GifConfig {
    var spanCount = 3
    var cellPadding = 15
    var mediaType = MediaType.gif
    var contentType = GPHContentType.gif
    var direction = GiphyGridView.VERTICAL
    var fixedSizeCells = false
    var showCheckeredBackground = true
   // const val YOUR_API_KEY = "3dxcf6TRJ2Z1hxN7qhxRSiODc3OJaCvT"
    const val YOUR_API_KEY_GIPHY = "K3LKN8wemYSxhi1zIqfVYfcPQDVt25WF"
}
