package com.tapbi.spark.yokey.ui.custom.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.keyboard.internal.KeyDrawParams;
import com.android.inputmethod.keyboard.internal.KeyVisualAttributes;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.utils.TypefaceUtils;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.interfaces.IEventReturnViewKbDemo;
import com.tapbi.spark.yokey.interfaces.IResultDownBackground;
import com.tapbi.spark.yokey.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ViewBgKeyKb extends RelativeLayout {
    public ViewBgKeyKb(Context context) {
        super(context);
    }

    public ViewBgKeyKb(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewBgKeyKb(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    List<Key> list = new ArrayList<>();
    Paint paint = new Paint();
    private KeyDrawParams mKeyDrawParams = new KeyDrawParams();
    private Settings mSettings;
    private Drawable bgKeyText;
    private Drawable bgKeyLanguage;
    private Drawable bgKeyShift;
    private Drawable bgKeyDelete;
    private Drawable bgKeySymbol;
    private Drawable bgKeyEnter;
    private Drawable bgKeySpace;

    private ThemeModel themeModel;


    private Keyboard keyboard;
    private IEventReturnViewKbDemo iEventReturnViewKbDemo;
    private Bitmap bmBackground;
    private String name;
    private String spaceLable;

    public void setListKey(Keyboard keyboard, KeyDrawParams keyDrawParams, ThemeModel themeModel, Bitmap bmBackground, String name, String spaceLable) {
        this.keyboard = keyboard;
        this.themeModel = themeModel;
        this.bmBackground = bmBackground;
        this.name = name;
        this.spaceLable = spaceLable;



        mSettings = Settings.getInstance();
        paint.setAntiAlias(true);
        paint.setColor(CommonUtil.hex2decimal(themeModel.getKey().getText().getTextColor()));

        this.mKeyDrawParams = keyDrawParams;
        list.clear();
        list.addAll(keyboard.getSortedKeys());
        Key keyText = null;
        Key keyShift = null;
        Key keyLanguage = null;
        Key keyEnter = null;
        Key keyDelete = null;
        Key keySpace = null;
        for (Key key : list) {
            if (key.getCode() == Constants.CODE_DELETE) {
                keyDelete = key;
            } else if (key.getCode() == Constants.CODE_ENTER) {
                keyEnter = key;
            } else if (key.getCode() == Constants.CODE_SHIFT) {
                keyShift = key;
            } else if (key.getCode() == Constants.CODE_SPACE) {
                keySpace = key;
            } else if (key.getCode() == Constants.CODE_LANGUAGE_SWITCH) {
                keyLanguage = key;
            }
            if (keyText == null && key.getLabel() != null) {
                keyText = key;
            }
        }

        this.bgKeyText = CommonUtil.getDrawable(getContext(), themeModel.getTypeKey(), "btn_key_text.png", keyText);
        this.bgKeyLanguage = CommonUtil.getDrawable(getContext(), themeModel.getTypeKey(), "btn_key_language.png", keyLanguage);
        this.bgKeyShift = CommonUtil.getDrawable(getContext(), themeModel.getTypeKey(), "btn_key_shift.png", keyShift);
        this.bgKeyDelete = CommonUtil.getDrawable(getContext(), themeModel.getTypeKey(), "btn_key_delete.png", keyDelete);
        this.bgKeySymbol = CommonUtil.getDrawable(getContext(), themeModel.getTypeKey(), "btn_key_symbol.png", keyEnter);
        this.bgKeyEnter = CommonUtil.getDrawable(getContext(), themeModel.getTypeKey(), "btn_key_enter.png", keyEnter);
        this.bgKeySpace = CommonUtil.getDrawable(getContext(), themeModel.getTypeKey(), "btn_key_special.png", keySpace);


        setLayerType(LAYER_TYPE_SOFTWARE, paint);
        setWillNotDraw(false);
//        invalidate();
        savePreview();
    }

    public void setBackground() {
//        if (drawable != null) {
//            setBackground(drawable);
//        } else {
//            int color = CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor());
//            setBackgroundColor(color);
//        }
    }


    private Drawable getDrawableIcon(String strPathToImage, String icon) {
        if (icon != null && !icon.equals("")) {
            return CommonUtil.getImageFromAsset(getContext(), strPathToImage.concat(icon));
        } else {
            return null;
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        for (Key key : list) {
//            final int keyDrawX = key.getDrawX() + getPaddingLeft();
//            final int keyDrawY = key.getY() + getPaddingTop();
//            canvas.translate(keyDrawX, keyDrawY);
//            drawKey(key, canvas);
//            canvas.translate(-keyDrawX, -keyDrawY);
//        }
//        savePreview();
    }

    private void savePreview() {
        Timber.e("Duongcv " );
        if (getWidth() > 0 && getHeight() > 0) {
            Timber.e("Duongcv start save");
            Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if (bmBackground != null) {
                bmBackground = Bitmap.createScaledBitmap(bmBackground, getWidth(), getHeight(), false);
                canvas.drawBitmap(bmBackground, 0, 0, null);
            } else {
                int color = CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor());
                canvas.drawColor(color);
            }
            for (Key key : list) {
                final int keyDrawX = key.getDrawX() + getPaddingLeft();
                final int keyDrawY = key.getY() + getPaddingTop();
                canvas.translate(keyDrawX, keyDrawY);
                drawKey(key, canvas);
                canvas.translate(-keyDrawX, -keyDrawY);
            }
            CommonUtil.saveImage(getContext(), bitmap, name, new IResultDownBackground() {
                @Override
                public void onDownBackgroundError() {
                    Timber.e("Duongcv save error" );
                }

                @Override
                public void onDownBackgroundSuccess() {
                    Timber.e("Duongcv save done");
                }
            });
        }
    }


    private Drawable getIconKeyDefault(Key key, KeyDrawParams params) {
        return key.getIcon(keyboard.mIconsSet, params.mAnimAlpha);
    }


    private void drawKey(Key key, Canvas canvas) {
        final KeyVisualAttributes attr = key.getVisualAttributes();
        final KeyDrawParams params = mKeyDrawParams.mayCloneAndUpdateParams(key.getHeight(), attr);

        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final float centerX = keyWidth * 0.5f;
        final float centerY = keyHeight * 0.5f;
        String labelCurrent = key.getLabel();
        if (key.getCode() == Constants.CODE_SPACE && labelCurrent == null) {
            labelCurrent = keyboard.mId.mSubtype.getFullDisplayName();
            if (labelCurrent == null) {
                labelCurrent = spaceLable;
            }
        }

        Drawable bgKey;
        Drawable icon = null;
        if (key.getCode() == Constants.CODE_LANGUAGE_SWITCH) {
            bgKey = bgKeyLanguage;
            icon = getIconKeyDefault(key, params);
        } else if (key.getCode() == Constants.CODE_SHIFT) {
            bgKey = bgKeyShift;
            icon = getIconKeyDefault(key, params);
        } else if (key.getCode() == Constants.CODE_DELETE) {
            bgKey = bgKeyDelete;
            icon = getIconKeyDefault(key, params);
        } else if (key.getCode() == Constants.CODE_SWITCH_ALPHA_SYMBOL) {
            bgKey = bgKeySymbol;
//            icon = iconSymbol;
        } else if (key.getCode() == Constants.CODE_ENTER) {
            bgKey = bgKeyEnter;
            icon = getIconKeyDefault(key, params);
        } else if (key.getCode() == Constants.CODE_SPACE) {
            bgKey = bgKeySpace;
//            icon = iconSpace;
        } else {
            bgKey = bgKeyText;
        }

        drawIcon(canvas, bgKey, 0, 0, keyWidth, keyHeight);
        if (icon != null) {
            drawIcon(canvas, icon, 0, 0, keyWidth, keyHeight);
        }

        if (labelCurrent != null) {
            params.mAnimAlpha = Constants.Color.ALPHA_OPAQUE;

            String label = labelCurrent;
            float labelX = centerX;
            float labelBaseline = centerY;
            final float labelCharHeight = TypefaceUtils.getReferenceCharHeight(paint);
            final float labelCharWidth = TypefaceUtils.getReferenceCharWidth(paint);

            // Vertical label text alignment.
            labelBaseline = centerY + labelCharHeight / 2.0f;

            // Horizontal label text alignment
//            if (key.isAlignLabelOffCenter() && mSettings.getCurrent().mShowSpecialKey) {
//                // The label is placed off center of the key. Used mainly on "phone number" layout.
//                labelX = centerX + params.mLabelOffCenterRatio * labelCharWidth;
//                paint.setTextAlign(Paint.Align.LEFT);
//            } else {
            labelX = centerX;
            paint.setTextAlign(Paint.Align.CENTER);
//            }
            if (key.needsAutoXScale()) {
                final float ratio = Math.min(1.0f, (keyWidth * KeyboardView.MAX_LABEL_RATIO) / TypefaceUtils.getStringWidth(label, paint));
                if (key.needsAutoScale()) {
                    final float autoSize = paint.getTextSize() * ratio;
                    paint.setTextSize(autoSize);
                } else {
                    paint.setTextScaleX(ratio);
                }
            }

//            paint.setShader(CommonUtil.getShaderGradientTextCustomizeTheme(getContext(), keyWidth, keyHeight, true));
            paint.setTextSize(key.selectTextSize(params));
            canvas.drawText(label, 0, label.length(), labelX, labelBaseline, paint);
        }


    }


    protected void drawIcon(final Canvas canvas, final Drawable icon,
                            final int x, final int y, final int width, final int height) {
        if (icon == null) {
            return;
        }
        canvas.translate(x, y);
        icon.setBounds(0, 0, width, height);
        if (icon instanceof BitmapDrawable) {
            Bitmap bm = ((BitmapDrawable) icon).getBitmap();
            if (bm != null) {
                canvas.drawBitmap(bm, (width - bm.getWidth()) / 2f, (height - bm.getHeight()) / 2f, null);
            } else {
                icon.draw(canvas);
            }
        } else {
            icon.draw(canvas);
        }
        canvas.translate(-x, -y);
    }
}
