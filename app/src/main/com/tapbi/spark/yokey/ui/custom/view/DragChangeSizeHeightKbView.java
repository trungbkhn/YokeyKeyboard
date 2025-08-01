package com.tapbi.spark.yokey.ui.custom.view;

import static com.tapbi.spark.yokey.util.Constant.HEIGHT_VIEW_HORIZONTAL;
import static com.tapbi.spark.yokey.util.Constant.HEIGHT_VIEW_VERTICAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.tapbi.spark.yokey.databinding.ViewChangeSizeHeightKbBinding;
import com.android.inputmethod.keyboard.KeyboardSwitcher;
import com.android.inputmethod.latin.LatinIME;
import com.android.inputmethod.latin.settings.SettingsValues;
import com.tapbi.spark.yokey.common.Constant;
import com.tapbi.spark.yokey.util.DisplayUtils;
import com.tapbi.spark.yokey.util.LocaleUtils;

import org.jetbrains.annotations.NotNull;

public class DragChangeSizeHeightKbView extends ConstraintLayout {
    private Context context;

    private ViewChangeSizeHeightKbBinding binding;
    private LatinIME latinIME;
    private SettingsValues settingsValues;
    private SharedPreferences mPrefs;
    public int heightDrag = 100;
    private float heightViewOld;
    private float heightViewMax;
    private float heightViewMin;

    private float yOld;
    private float ySpace;
    private float scale;

    public DragChangeSizeHeightKbView(@NonNull @NotNull Context context) {
        super(context);
        init(context);
    }

    public DragChangeSizeHeightKbView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragChangeSizeHeightKbView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context) {
//        setMaxWidth(App.getInstance().widthScreen);
        this.context = context;
        heightDrag = DisplayUtils.dp2px(20);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        binding = ViewChangeSizeHeightKbBinding.inflate(LayoutInflater.from(context), this, true);
        binding.vDragTop.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int[] xy = new int[2];
                    binding.vDragTop.getLocationOnScreen(xy);
                    ySpace = event.getRawY() - xy[1];
                    yOld = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float yCurrent = event.getY();
                    translateDrag(yCurrent, yOld, ySpace);
                    yOld = yCurrent - (yCurrent - yOld);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });

        binding.btnOk.setOnClickListener(v -> {
            if (heightViewMax == 0) {
                binding.viewGroup.setVisibility(INVISIBLE);
                KeyboardSwitcher.getInstance().setShowViewBgDragInKbView(false);
                return;
            }
            mPrefs.edit().putBoolean(Constant.KEYBOARD_CHANGE_LISTENER, true).apply();
            float heightKbNew = getHeight() - (float) (heightDrag / 2);
            scale = heightKbNew * scale / heightViewOld;
            mPrefs.edit().putFloat(Constant.HEIGHT_ROW_KEY, scale).apply();
            heightKbNew = Math.min(heightKbNew, heightViewMax);
            if(DisplayUtils.getScreenWidth()<DisplayUtils.getScreenHeight()){
                mPrefs.edit().putFloat(HEIGHT_VIEW_VERTICAL, heightKbNew).apply();
            }else{
                mPrefs.edit().putFloat(HEIGHT_VIEW_HORIZONTAL, heightKbNew).apply();
            }
            if (DisplayUtils.getScreenWidth() < DisplayUtils.getScreenHeight()) {
                mPrefs.edit().putFloat(Constant.HEIGHT_KEYBOARD_NEW, heightKbNew).apply();
            } else {
                mPrefs.edit().putFloat(Constant.HEIGHT_KEYBOARD_NEW_VERTICAL, heightKbNew).apply();
            }
            latinIME.setScaleValues(scale);
//            latinIME.showDragChangeSizeKb(false);
            binding.viewGroup.setVisibility(INVISIBLE);
            KeyboardSwitcher.getInstance().setShowViewBgDragInKbView(false);
        });

    }

    public void showView(boolean isShow) {
        LocaleUtils.INSTANCE.applyLocale(context);
        if (isShow) {
//            heightViewOld = KeyboardSwitcher.getInstance().getHeightKeyboard();
            binding.viewGroup.setVisibility(VISIBLE);
            KeyboardSwitcher.getInstance().setShowViewBgDragInKbView(true);
//            invalidate();
//            requestLayout();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
            if(DisplayUtils.getScreenWidth()<DisplayUtils.getScreenHeight()){
                heightView = mPrefs.getFloat(HEIGHT_VIEW_VERTICAL,0);
            }else{
                heightView = mPrefs.getFloat(HEIGHT_VIEW_HORIZONTAL,0);
            }

            if (heightView == 0) {
                heightView = KeyboardSwitcher.getInstance().getHeightKeyboard();
            }
            params.height = (int) heightView;//KeyboardSwitcher.getInstance().getHeightKeyboard();
            setLayoutParams(params);
            heightViewMax = 0;
            requestLayoutView();
        } else {
            binding.viewGroup.setVisibility(INVISIBLE);
            KeyboardSwitcher.getInstance().setShowViewBgDragInKbView(false);
        }
    }

    /* @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Timber.d("ducNQ : onMeasurddd: getScreenWidth: " + getWidth());
         setMeasuredDimension(KeyboardSwitcher.getInstance().getWidthKeyboard(), KeyboardSwitcher.getInstance().getHeightKeyboard());
     }*/
    float heightView = 0;

    private void translateDrag(float yCurrent, float yOld, float ySpace) {
        float heightSet = (int) (getHeight() - (yCurrent - yOld) - ySpace);
        if (heightViewMin == 0 || heightViewMax == 0) initMinMaxHeight();
        if (heightSet < heightViewMin) {
            heightSet = heightViewMin;
        } else if (heightSet > heightViewMax) {
            heightSet = heightViewMax;
        }
//        mPrefs.edit().putFloat("heightView", heightSet).apply();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        params.height = (int) heightSet;
        setLayoutParams(params);

    }


    public void setLatinIME(LatinIME latinIME, SettingsValues settingsValues, float scale) {
        this.latinIME = latinIME;
        this.settingsValues = settingsValues;
        this.scale = scale;
    }

    public void requestLayoutView() {
        if (binding != null) {
            binding.getRoot().requestLayout();
            invalidate();
            Log.d("duongcv", "requestLayoutView: " + binding.getRoot().getWidth());
        }
    }

    private int widthKeyboard = DisplayUtils.getScreenWidth();

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (getVisibility() == View.VISIBLE) {
//            if(mPrefs.getInt(Constant.HEIGHT_KEYBOARD_ORIGIN, 0)0)
            heightViewOld = KeyboardSwitcher.getInstance().getHeightKeyboard();
//            heightViewMax = (float) (heightViewOld / scale * Constant.VALUE_HEIGHT_MAX_KEYBOARD) + (float) heightDrag / 2;
//            heightViewMin = (float) (heightViewOld / scale * Constant.VALUE_HEIGHT_MIN_KEYBOARD) + (float) heightDrag / 2;
//            KeyboardSwitcher.getInstance().setShowViewBgDragInKbView(true);
        } else {
            KeyboardSwitcher.getInstance().setShowViewBgDragInKbView(false);
        }
    }

    private void initMinMaxHeight() {
        if (DisplayUtils.getScreenWidth() < DisplayUtils.getScreenHeight()) {
            heightViewMax = DisplayUtils.getScreenHeight() / 2.5f;
            heightViewMin = DisplayUtils.getScreenHeight() / 5f;
        } else {
            heightViewMax = DisplayUtils.getScreenHeight() / 2f + binding.vDragTop.getHeight() / 2f;
            heightViewMin = DisplayUtils.getScreenHeight() / 3f;
        }
        //  mPrefs.edit().putFloat("heightViewMax", heightViewMax).apply();
    }
}
