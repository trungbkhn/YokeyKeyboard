package com.android.inputmethod.keyboard.translate

import android.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import timber.log.Timber


class CustomEditTextTranslate : EditText {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == R.id.paste) {
            val clipboard: ClipboardManager? =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData: ClipData? = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val item = clipData.getItemAt(0)
                val pasteData = if (item != null && item.text != null) item.text.toString() else ""
                Timber.e("ducNQ onTextContextMenuItem: "+pasteData.length);
                if (pasteData.length > 1500) {
                    setText(pasteData.substring(0, 1500))
                    setSelection(1500)
                } else {
                    setText(pasteData)
                    setSelection(pasteData.length)
                }
            }
        }
        return super.onTextContextMenuItem(id)
    }
}