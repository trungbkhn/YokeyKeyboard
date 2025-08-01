package com.tapbi.spark.yokey.ui.custom.view;

import static com.android.inputmethod.latin.common.Constants.NOT_A_COORDINATE;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.tapbi.spark.yokey.R;
import com.android.inputmethod.keyboard.KeyboardActionListener;
import com.android.inputmethod.latin.LatinIME;
import com.android.inputmethod.latin.RichInputConnection;
import com.android.inputmethod.latin.common.Constants;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.LocaleUtils;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;


public class CopyPasteSelectionView extends RelativeLayout {

    private static long MAX_REPEAT_COUNT_TIME = TimeUnit.SECONDS.toMillis(30);
    private static long mKeyRepeatStartTimeout;
    private static long mKeyRepeatInterval;

    private ImageView imgBack;
    private ImageView imgLeft;
    private ImageView imgUp;
    private TextView tvSelect;
    private ImageView imgDown;
    private ImageView imgRight;
    private ImageView imgHome;
    private ImageView imgEnd;
    private TextView tvSelectAll;
    private TextView tvCopy;
    private TextView tvPaste;
    private ImageView imgDelete;
    private int colors = Color.WHITE;
    private RichInputConnection richInputConnection;
    private LatinIME latinIME;

    private boolean isSelect;
    private DeleteKeyOnTouchListener deleteKeyOnTouchListener;
   private Context context;
    private CountDownTimer timer;

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;

    private View viewTouch = null;
    private boolean isRepeat;
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            action();
            handler.postDelayed(this, 50);
            isRepeat = true;
        }
    };

    public CopyPasteSelectionView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CopyPasteSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CopyPasteSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_edit_selection, this, true);
        deleteKeyOnTouchListener = new DeleteKeyOnTouchListener(getContext(), this);
        handler = new Handler();
        findViews();
        mKeyRepeatStartTimeout = getResources().getInteger(R.integer.config_key_repeat_start_timeout);
        mKeyRepeatInterval = getResources().getInteger(R.integer.config_key_repeat_interval);
        timer = new CountDownTimer(MAX_REPEAT_COUNT_TIME, mKeyRepeatInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                final long elapsed = MAX_REPEAT_COUNT_TIME - millisUntilFinished;
                if (elapsed < mKeyRepeatStartTimeout) {
                    return;
                }
                checkSelection();
            }

            @Override
            public void onFinish() {
                checkSelection();
                if (getVisibility() == VISIBLE) {
                    timer.start();
                }
            }
        };
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void findViews() {
        imgBack = findViewById(R.id.img_back);
        imgLeft = findViewById(R.id.img_left);
        imgUp = findViewById(R.id.img_up);
        tvSelect = findViewById(R.id.tv_select);
        imgDown = findViewById(R.id.img_down);
        imgRight = findViewById(R.id.img_right);
        imgHome = findViewById(R.id.img_home);
        imgEnd = findViewById(R.id.img_end);
        tvSelectAll = findViewById(R.id.tv_select_all);
        tvCopy = findViewById(R.id.tv_copy);
        tvPaste = findViewById(R.id.tv_paste);
        imgDelete = findViewById(R.id.img_delete);

        imgBack.setOnClickListener(onClickListener);
//        imgLeft.setOnClickListener(onClickListener);
//        imgUp.setOnClickListener(onClickListener);
        tvSelect.setOnClickListener(onClickListener);
//        imgDown.setOnClickListener(onClickListener);
//        imgRight.setOnClickListener(onClickListener);
        imgHome.setOnClickListener(onClickListener);
        imgEnd.setOnClickListener(onClickListener);
        tvSelectAll.setOnClickListener(onClickListener);
        tvCopy.setOnClickListener(onClickListener);
        tvPaste.setOnClickListener(onClickListener);

        imgDelete.setOnTouchListener(deleteKeyOnTouchListener);
        imgLeft.setOnTouchListener(onTouchListener);
        imgRight.setOnTouchListener(onTouchListener);
        imgUp.setOnTouchListener(onTouchListener);
        imgDown.setOnTouchListener(onTouchListener);

        // setColorFilter(App.getInstance().colorIconDefault);
    }

    public void setKeyboardActionListener(KeyboardActionListener listener) {
        deleteKeyOnTouchListener.setKeyboardActionListener(listener);
    }

    public void showEditSelection() {
        LocaleUtils.INSTANCE.applyLocale(context);
        checkSelection();
        setVisibility(VISIBLE);
        tvSelect.setText(R.string.select);
        tvSelectAll.setText(R.string.select_all);
        tvPaste.setText(R.string.paste);
        tvCopy.setText(R.string.copy);
        timer.start();
    }

    public void hideEditSelection() {
        setVisibility(GONE);
        latinIME.showMenuHeader();
        latinIME.showInputView(true);
        timer.cancel();
        if (isSelect) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
            int end = richInputConnection.getExpectedSelectionEnd();
            richInputConnection.setSelection(end, end);
            isSelect = false;
            tvSelect.setBackgroundColor(getResources().getColor(R.color.colorBgButtonSelection));
        }
    }

    public void setRichInputConnection(LatinIME latinIME, RichInputConnection richInputConnection) {
        this.latinIME = latinIME;
        this.richInputConnection = richInputConnection;
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    viewTouch = v;
                    handler.postDelayed(runnable, 200);
                    v.setBackgroundColor(getContext().getResources().getColor(R.color.colorBgButton));
                    break;
                case MotionEvent.ACTION_MOVE:
                    final float x = event.getX();
                    final float y = event.getY();
                    if (x < 0.0f || v.getWidth() < x || y < 0.0f || v.getHeight() < y) {
                        handler.removeCallbacks(runnable);
                        isRepeat = false;
                        viewTouch = null;
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(getContext().getResources().getColor(R.color.colorBgButtonSelection));
                    handler.removeCallbacks(runnable);
                    if (!isRepeat) {
                        action();
                    }
                    viewTouch = null;
                    isRepeat = false;
                    break;
            }

            return true;
        }
    };

    private void action() {
        if (viewTouch == null) {
            return;
        }
        if (viewTouch == imgLeft) {
            clickLeft();
        } else if (viewTouch == imgRight) {
            clickRight();
        } else if (viewTouch == imgUp) {
            clickUp();
        } else if ((viewTouch == imgDown)) {
            clickDown();
        }
    }

    public void setColorFilter(int colorFilter) {
        colors = colorFilter;
        imgBack.setColorFilter(colorFilter);
        imgDelete.setColorFilter(colorFilter);
        imgLeft.setColorFilter(colorFilter);
        imgDown.setColorFilter(colorFilter);
        imgEnd.setColorFilter(colorFilter);
        imgHome.setColorFilter(colorFilter);
        imgRight.setColorFilter(colorFilter);
        imgUp.setColorFilter(colorFilter);
        tvSelect.setTextColor(colorFilter);
        if (hasSelection()) {
            tvCopy.setTextColor(colorFilter);
        } else {
            tvCopy.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextSelectionDisable));
        }
//        tvCopy.setTextColor(colorFilter);
        tvPaste.setTextColor(colorFilter);
        tvSelectAll.setTextColor(colorFilter);
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == imgBack) {
                hideEditSelection();
            } /*else if (v == imgLeft) {
                clickLeft();
            } else if (v == imgUp) {
                clickUp();
            } */ else if (v == tvSelect) {
                clickSelect();
            } /*else if (v == imgDown) {
                clickDown();
            } else if (v == imgRight) {
                clickRight();
            } */ else if (v == imgHome) {
                setCursorStart();
            } else if (v == imgEnd) {
                setCursorEnd();
            } else if (v == tvSelectAll) {
                Timber.e("hachung hasSelection:"+!hasSelection());
                if (!hasSelection()) {
                    selectAll();
                } else {
                    copyToClipboard(true);
                }
            } else if (v == tvCopy) {
                copyToClipboard(false);
            } else if (v == tvPaste) {
                pasteFromClipboard();
            }
        }
    };

    private void checkWhenDelete() {
        if (isSelect) {
            try {
                int start;
                int end;
                if (richInputConnection.getExpectedSelectionEnd() > richInputConnection.getExpectedSelectionStart()) {
                    start = richInputConnection.getExpectedSelectionStart();
                    end = richInputConnection.getExpectedSelectionEnd();
                } else if (richInputConnection.getExpectedSelectionEnd() < richInputConnection.getExpectedSelectionStart()) {
                    end = richInputConnection.getExpectedSelectionStart();
                    start = richInputConnection.getExpectedSelectionEnd();
                } else {
                    start = richInputConnection.getExpectedSelectionStart();
                    end = richInputConnection.getExpectedSelectionEnd();

                    if (richInputConnection != null && richInputConnection.getExtractedText() != null) {
                        end = richInputConnection.getExtractedText().text.toString().length() - 1;
                    }


                }
                richInputConnection.setSelection(start, end);
                richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
                isSelect = false;
//				tvSelect.setBackgroundColor(colorUp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void selectAll() {
        try {
            if (richInputConnection != null && richInputConnection.getExtractedText() != null) {
                ExtractedText extractedText = richInputConnection.getExtractedText();
                if (extractedText != null && extractedText.text != null && extractedText.text.length() > 0) {
                    int length = extractedText.text.length();
                    richInputConnection.setSelection(0, length);

//					if (!isSelect) {
//						clickSelect();
//					}
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean checkWhenClick(int type) {
        int start = richInputConnection.getExpectedSelectionStart();
        int end = richInputConnection.getExpectedSelectionEnd();
        if (type == LEFT || type == UP) {
            return start != 0;
        } else {
            try {
                if (richInputConnection != null && richInputConnection.getExtractedText() != null && richInputConnection.getExtractedText().text != null) {
                    CharSequence allText = richInputConnection.getExtractedText().text;
                    if (end >= allText.length()) {
                        return false;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }
    }

    private void clickDown() {
        if (isSelect) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN));
        } else if (checkWhenClick(DOWN)) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN));
        }
    }

    private void clickUp() {
        if (isSelect) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP));
        } else if (checkWhenClick(UP)) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP));
        }
    }

    private void clickRight() {
        Timber.e("hachung clickRight:"+isSelect);
        if (isSelect) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
        } else if (checkWhenClick(RIGHT)) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
        }
    }

    private void clickLeft() {
        Timber.e("hachung clickLeft:"+isSelect);
        if (isSelect) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
        } else if (checkWhenClick(LEFT)) {
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
        }
    }

    private void setCursorEnd() {
        richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END));
        richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_END));
    }

    private void setCursorStart() {
        richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
        richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_HOME));
    }

    private void clickSelect() {
        isSelect = !isSelect;
        if (isSelect) {
            int colorFilter = CommonUtil.manipulateColor(getResources().getColor(R.color.black), 0.5f, 220);
            //int colors = ColorUtils.
            tvSelect.setBackgroundColor(colorFilter/*getResources().getColor(R.color.black)*/);
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
        } else {
            tvSelect.setBackgroundColor(getResources().getColor(R.color.colorBgButtonSelection));
            richInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
            int end = richInputConnection.getExpectedSelectionEnd();
            richInputConnection.setSelection(end, end);
        }
    }

    private void checkSelection() {
        if (hasSelection()) {
            tvCopy.setEnabled(true);
            tvCopy.setTextColor(colors/*Color.WHITE*/);
            tvSelectAll.setText(R.string.cut);
        } else {
            tvCopy.setEnabled(false);
           // int colorFilter = CommonUtil.manipulateColor(colors, 0.5f, 220);
            tvCopy.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextSelectionDisable));
            tvSelectAll.setText(R.string.select_all);
        }
    }

    private boolean hasSelection() {
        if (richInputConnection.getSelectedText(0) != null) {
            return true;
        }
        return false;
    }

    private void copyToClipboard(boolean cut) {
        CharSequence textSelect = richInputConnection.getSelectedText(0);
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (textSelect != null && clipboard != null) {
            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.app_name), textSelect.toString());
            clipboard.setPrimaryClip(clip);
            if (!cut) {
                CommonUtil.customToast(getContext(), getResources().getString(R.string.text_copied));
                // Toast.makeText(getContext(), R.string.text_copied, Toast.LENGTH_SHORT).show();
            } else {
                richInputConnection.setComposingText("", 1);
            }
        } else {
            CommonUtil.customToast(getContext(), getResources().getString(R.string.read_external_dictionary_error));
            //Toast.makeText(getContext(), getResources().getString(R.string.read_external_dictionary_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clipData = clipboard.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence textToPaste = clipData.getItemAt(0).getText();
                if (textToPaste != null) {
                    richInputConnection.setComposingText(textToPaste, 1);
                    richInputConnection.finishComposingText();
                } else {
                    CommonUtil.customToast(getContext(), getResources().getString(R.string.read_external_dictionary_error));
                    // Toast.makeText(getContext(), getResources().getString(R.string.read_external_dictionary_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                CommonUtil.customToast(getContext(), getResources().getString(R.string.read_external_dictionary_error));
                // Toast.makeText(getContext(), getResources().getString(R.string.read_external_dictionary_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            CommonUtil.customToast(getContext(), getResources().getString(R.string.read_external_dictionary_error));
            // Toast.makeText(getContext(), getResources().getString(R.string.read_external_dictionary_error), Toast.LENGTH_SHORT).show();
        }
    }

    private static class DeleteKeyOnTouchListener implements OnTouchListener {
        private static long MAX_REPEAT_COUNT_TIME = TimeUnit.SECONDS.toMillis(30);
        private static long mKeyRepeatStartTimeout;
        private static long mKeyRepeatInterval;
        private CopyPasteSelectionView editSelectrionView;

        public DeleteKeyOnTouchListener(Context context, CopyPasteSelectionView editSelectrionView) {
            this.editSelectrionView = editSelectrionView;
            mKeyRepeatStartTimeout = context.getResources().getInteger(R.integer.config_key_repeat_start_timeout);
            mKeyRepeatInterval = context.getResources().getInteger(R.integer.config_key_repeat_interval);
            mTimer = new CountDownTimer(MAX_REPEAT_COUNT_TIME, mKeyRepeatInterval) {
                @Override
                public void onTick(long millisUntilFinished) {
                    final long elapsed = MAX_REPEAT_COUNT_TIME - millisUntilFinished;
                    if (elapsed < mKeyRepeatStartTimeout) {
                        return;
                    }
                    onKeyRepeat();
                }

                @Override
                public void onFinish() {
                    onKeyRepeat();
                }
            };
        }

        /**
         * Key-repeat state.
         */
        private static final int KEY_REPEAT_STATE_INITIALIZED = 0;
        // The key is touched but auto key-repeat is not started yet.
        private static final int KEY_REPEAT_STATE_KEY_DOWN = 1;
        // At least one key-repeat event has already been triggered and the key is not released.
        private static final int KEY_REPEAT_STATE_KEY_REPEAT = 2;

        private KeyboardActionListener mKeyboardActionListener =
                KeyboardActionListener.EMPTY_LISTENER;

        // TODO: Do the same things done in PointerTracker
        private final CountDownTimer mTimer;
        private int mState = KEY_REPEAT_STATE_INITIALIZED;
        private int mRepeatCount = 0;

        public void setKeyboardActionListener(final KeyboardActionListener listener) {
            mKeyboardActionListener = listener;
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    editSelectrionView.checkWhenDelete();
                    onTouchDown(v);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    final float x = event.getX();
                    final float y = event.getY();
                    if (x < 0.0f || v.getWidth() < x || y < 0.0f || v.getHeight() < y) {
                        // Stop generating key events once the finger moves away from the view area.
                        onTouchCanceled(v);
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    onTouchUp(v);
                    return true;
            }
            return false;
        }

        private void handleKeyDown() {
            mKeyboardActionListener.onPressKey(
                    Constants.CODE_DELETE, mRepeatCount, true /* isSinglePointer */);
        }

        private void handleKeyUp() {
            mKeyboardActionListener.onCodeInput(Constants.CODE_DELETE,
                    NOT_A_COORDINATE, NOT_A_COORDINATE, false /* isKeyRepeat */, false);
            mKeyboardActionListener.onReleaseKey(
                    Constants.CODE_DELETE, false /* withSliding */);
            ++mRepeatCount;
        }

        private void onTouchDown(final View v) {
            mTimer.cancel();
            mRepeatCount = 0;
            handleKeyDown();
            v.setPressed(true /* pressed */);
            mState = KEY_REPEAT_STATE_KEY_DOWN;
            mTimer.start();
        }

        private void onTouchUp(final View v) {
            mTimer.cancel();
            if (mState == KEY_REPEAT_STATE_KEY_DOWN) {
                handleKeyUp();
            }
            v.setPressed(false /* pressed */);
            mState = KEY_REPEAT_STATE_INITIALIZED;
        }

        private void onTouchCanceled(final View v) {
            mTimer.cancel();
            v.setBackgroundColor(Color.TRANSPARENT);
            mState = KEY_REPEAT_STATE_INITIALIZED;
        }

        // Called by {@link #mTimer} in the UI thread as an auto key-repeat signal.
        void onKeyRepeat() {
            switch (mState) {
                case KEY_REPEAT_STATE_INITIALIZED:
                    // Basically this should not happen.
                    break;
                case KEY_REPEAT_STATE_KEY_DOWN:
                    // Do not call {@link #handleKeyDown} here because it has already been called
                    // in {@link #onTouchDown}.
                    handleKeyUp();
                    mState = KEY_REPEAT_STATE_KEY_REPEAT;
                    break;
                case KEY_REPEAT_STATE_KEY_REPEAT:
                    handleKeyDown();
                    handleKeyUp();
                    break;
            }
        }
    }
}
