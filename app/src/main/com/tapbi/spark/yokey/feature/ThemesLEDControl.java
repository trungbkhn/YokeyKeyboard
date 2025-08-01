package com.tapbi.spark.yokey.feature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import com.tapbi.spark.yokey.R;
import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.keyboard.emoji.EmojiPageKeyboardView;
import com.android.inputmethod.keyboard.internal.KeyDrawParams;
import com.android.inputmethod.keyboard.internal.MoreKeySpec;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.utils.TypefaceUtils;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.data.model.Font;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;

import java.util.HashMap;

import static com.tapbi.spark.yokey.common.CommonVariable.VALUE_DEFAULT;

public class ThemesLEDControl {
    private final int CACHE_SIZE = 4 * 1024 * 1024;
    // The maximum key label width in the proportion to the key width.
    private static final float MAX_LABEL_RATIO = 0.90f;
    private final LruCache<String, Typeface> typefaceLruCache = new LruCache<>(CACHE_SIZE);
    HashMap<Integer, Bitmap> bitmapIcons = new HashMap<>();
    public PorterDuffXfermode mXfermodeDst;
    public PorterDuffXfermode mXfermodeSrcAtop;
    public int sizeEmoji = 0;

    public Typeface getTypeface(Context context, String linkFont) {

        synchronized (typefaceLruCache) {
            Typeface typeface = typefaceLruCache.get(linkFont);
            if (typeface != null) {
                return typeface;
            }
            try {
                if (linkFont.contains("system")) {
                    typeface = Typeface.createFromFile(linkFont);
                } else {
                    typeface = Typeface.createFromAsset(context.getAssets(), linkFont);
                }
                typefaceLruCache.put(linkFont, typeface);
            } catch (Exception exception) {
                exception.printStackTrace();
            } catch (OutOfMemoryError ignored) {
            }

            return typeface;
        }

    }

    public ThemesLEDControl() {
        mXfermodeDst = new PorterDuffXfermode(PorterDuff.Mode.DST);
        mXfermodeSrcAtop = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
    }

    public void drawKeyLabel(KeyboardView keyboardView, Keyboard keyboard, Key key, Canvas canvas, Paint paint, KeyDrawParams params, Typeface typeface, boolean isUsingLanguageKeyboardOtherQwerty) {
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final float centerX = keyWidth * 0.5f;
        final float centerY = keyHeight * 0.5f;
        String label = key.getLabel();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        if (keyboard == null) {
            return;
        }
        Shader shader;
        if (key.getCode() == Constants.CODE_SPACE) {
            label = keyboard.mId.mSubtype.getFullDisplayName();
            if (label == null || label.isEmpty()) label = "English";
            if (label.contains("(")) {
                int wText = CommonUtil.getWidthString(label, paint);
                if (wText > keyWidth * 0.7) {
                    for (int i = 2; i < label.length(); i++) {
                        String text = label.substring(0, label.length() - i);
                        if (text.contains("(")) {
                            text += "..)";
                        } else {
                            text += "(..)";
                        }
                        if (CommonUtil.getWidthString(text, paint) < keyWidth * 0.7) {
                            label = text;
                            break;
                        }
                    }
                }
            }
        }

        // Draw key label.
        float labelX;
        float labelBaseline;
        if (label != null) {
            if (key.getCode()!=Constants.CODE_SHIFT&&key.getCode()!=Constants.CODE_SWITCH_ALPHA_SYMBOL&&key.getCode() != Constants.CODE_SPACE && !key.getClass().toString().contains("GridKey") && App.getInstance().fontRepository != null) {
                label = CommonUtil.replaceTextFontOUTPUT(isUsingLanguageKeyboardOtherQwerty, App.getInstance().fontRepository.charSequences, label, label, App.getInstance().fontRepository.font, App.getInstance().fontRepository.key_Font);
            }
          // Timber.d("ducNQ : drawKeyLabeled: "+key.getLabel());
            paint.setTypeface(key.selectTypeface(params));
            paint.setTypeface(typeface);
            int sizeText = key.selectTextSize(params);
//            if(!key.ismPressed()){
//                App.getInstance().textSize = sizeText;
//            }else{
//                if(App.getInstance().textSize!=0 && sizeText>App.getInstance().textSize)sizeText =App.getInstance().textSize;
//            }
            paint.setTextSize(sizeText);
            if (keyboardView instanceof EmojiPageKeyboardView) {
                if (sizeEmoji == 0) {
                    sizeEmoji = sizeText;
                }
                paint.setTextSize(sizeEmoji);
            }
            final float labelCharHeight = TypefaceUtils.getReferenceCharHeight(paint);
            final float labelCharWidth = TypefaceUtils.getReferenceCharWidth(paint);

            // Vertical label text alignment.
            labelBaseline = centerY + labelCharHeight / 2.0f;
            // Horizontal label text alignment
            if (key.isAlignLabelOffCenter()) {
                // The label is placed off center of the key. Used mainly on "phone number" layout.
                labelX = centerX + params.mLabelOffCenterRatio * labelCharWidth;
                paint.setTextAlign(Paint.Align.LEFT);
            } else {
                labelX = centerX;
                paint.setTextAlign(Paint.Align.CENTER);
            }
            if (key.needsAutoXScale()) {
                final float ratio = Math.min(1.0f, (keyWidth * MAX_LABEL_RATIO) / TypefaceUtils.getStringWidth(label, paint));
                if (key.needsAutoScale()) {
                    final float autoSize = paint.getTextSize() * ratio;
                    paint.setTextSize(autoSize);
                } else {
                    paint.setTextScaleX(ratio);
                }
            }

            shader = paint.getShader();

            paint.setAlpha(255);
            int color = paint.getColor();
            paint.setColor(Color.GRAY);
//            if(App.getInstance().fontRepository!=null && App.getInstance().fontRepository.key_Font.equals(Constant.FONT_SQUARE_DASHED)) paint.setShader(null);
            canvas.drawText(label, 0, label.length(), key.getX() + labelX, key.getY() + labelBaseline, paint);
            paint.setColor(color);
            paint.setShader(shader);

            paint.setTextScaleX(1.0f);
        }

    }

    public void drawKeyLabel(Keyboard keyboard, Key key, Canvas canvas, Paint paint, KeyDrawParams params, Typeface typeface
            , boolean isUsingLanguageKeyboardOtherQwerty, CharSequence[] CharSequenceFont, Font itemFont, String keyFont) {
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final float centerX = keyWidth * 0.5f;
        final float centerY = keyHeight * 0.5f;
        String label = key.getLabel();


        if (keyboard == null) {
            return;
        }
        Shader shader;
        if (key.getCode() == Constants.CODE_SPACE) {
            label = keyboard.mId.mSubtype.getFullDisplayName();
        }
        // Draw key label.
        float labelX;
        float labelBaseline;
        if (label != null) {
            label = CommonUtil.replaceTextFontOUTPUT(isUsingLanguageKeyboardOtherQwerty, CharSequenceFont, label, label, itemFont, keyFont);
            paint.setTypeface(key.selectTypeface(params));
            paint.setTypeface(typeface);
            paint.setTextSize(key.selectTextSize(params));
            final float labelCharHeight = TypefaceUtils.getReferenceCharHeight(paint);
            final float labelCharWidth = TypefaceUtils.getReferenceCharWidth(paint);

            // Vertical label text alignment.
            labelBaseline = centerY + labelCharHeight / 2.0f;
            // Horizontal label text alignment
            if (key.isAlignLabelOffCenter()) {
                // The label is placed off center of the key. Used mainly on "phone number" layout.
                labelX = centerX + params.mLabelOffCenterRatio * labelCharWidth;
                paint.setTextAlign(Paint.Align.LEFT);
            } else {
                labelX = centerX;
                paint.setTextAlign(Paint.Align.CENTER);
            }
            if (key.needsAutoXScale()) {
                final float ratio = Math.min(1.0f, (keyWidth * MAX_LABEL_RATIO) / TypefaceUtils.getStringWidth(label, paint));
                if (key.needsAutoScale()) {
                    final float autoSize = paint.getTextSize() * ratio;
                    paint.setTextSize(autoSize);
                } else {
                    paint.setTextScaleX(ratio);
                }
            }

            shader = paint.getShader();

            paint.setAlpha(255);

            canvas.drawText(label, 0, label.length(), key.getX() + labelX, key.getY() + labelBaseline, paint);

            paint.setShader(shader);

            paint.setTextScaleX(1.0f);
        }
    }

    public void drawKeyLabelLEDMoreKeyboardView(Keyboard keyboard, Key key, Canvas canvas, Paint paint, KeyDrawParams params, Typeface typeface) {
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final float centerX = keyWidth * 0.5f;
        final float centerY = keyHeight * 0.5f;
        String label = key.getLabel();


        if (keyboard == null) {
            return;
        }
        Shader shader;
        if (key.getCode() == Constants.CODE_SPACE) {
            label = keyboard.mId.mSubtype.getFullDisplayName();
        }

        // Draw key label.
        float labelX;
        float labelBaseline;
        if (label != null) {
            paint.setTypeface(key.selectTypeface(params));
            paint.setTypeface(typeface);
            paint.setTextSize(key.selectTextSize(params));
            final float labelCharHeight = TypefaceUtils.getReferenceCharHeight(paint);
            final float labelCharWidth = TypefaceUtils.getReferenceCharWidth(paint);

            // Vertical label text alignment.
            labelBaseline = centerY + labelCharHeight / 2.0f;
            // Horizontal label text alignment
            if (key.isAlignLabelOffCenter()) {
                // The label is placed off center of the key. Used mainly on "phone number" layout.
                labelX = centerX + params.mLabelOffCenterRatio * labelCharWidth;
                paint.setTextAlign(Paint.Align.LEFT);
            } else {
                labelX = centerX;
                paint.setTextAlign(Paint.Align.CENTER);
            }
            if (key.needsAutoXScale()) {
                final float ratio = Math.min(1.0f, (keyWidth * MAX_LABEL_RATIO) / TypefaceUtils.getStringWidth(label, paint));
                if (key.needsAutoScale()) {
                    final float autoSize = paint.getTextSize() * ratio;
                    paint.setTextSize(autoSize);
                } else {
                    paint.setTextScaleX(ratio);
                }
            }

            shader = paint.getShader();


            canvas.drawText(label, 0, label.length(), key.getX() + labelX, key.getY() + labelBaseline, paint);

            paint.setShader(shader);

            paint.setTextScaleX(1.0f);
        }
    }


    public void drawHintLabelKey(Key key, Canvas canvas, Paint paint, KeyDrawParams params, int mDefaultKeyLabelFlags, float mKeyShiftedLetterHintPadding, float mKeyHintLetterPadding) {
        // Draw hint label.

        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final float centerX = keyWidth * 0.5f;
        final float centerY = keyHeight * 0.5f;
        Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();
        final String hintLabel = key.getHintLabel();
        if (hintLabel != null) {
            paint.setTextSize(key.selectHintTextSize(params));
            paint.setColor(key.selectHintTextColor(params));
            // TODO: Should add a way to specify type face for hint letters
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            final float labelCharHeight = TypefaceUtils.getReferenceCharHeight(paint);
            final float labelCharWidth = TypefaceUtils.getReferenceCharWidth(paint);
            final float hintX, hintBaseline;
            final float adjustmentY = params.mHintLabelVerticalAdjustment * labelCharHeight;
            if (key.hasHintLabel()) {
                // The hint label is placed just right of the key label. Used mainly on "phone number" layout.
                hintX = centerX + params.mHintLabelOffCenterRatio * labelCharWidth;
                if (key.isAlignHintLabelToBottom(mDefaultKeyLabelFlags)) {
                    hintBaseline = centerY;
                } else {
                    hintBaseline = centerY + labelCharHeight / 2.0f;
                }
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + centerX + 5
                        , key.getY() + hintBaseline * 1.2f + adjustmentY, paint);
//                canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + hintX/1.5f
//                        , key.getY() + hintBaseline*1.2f + adjustmentY, paint);
            } else if (key.hasShiftedLetterHint()) {
                // The hint label is placed at top-right corner of the key. Used mainly on tablet.
                hintX = keyWidth - mKeyShiftedLetterHintPadding - labelCharWidth / 1.1f;
                paint.getFontMetrics(mFontMetrics);
                hintBaseline = -mFontMetrics.top;
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + hintX
                        , key.getY() + hintBaseline + adjustmentY, paint);
            } else { // key.hasHintLetter()
                // The hint letter is placed at top-right corner of the key. Used mainly on phone.
                final float hintDigitWidth = TypefaceUtils.getReferenceDigitWidth(paint);
                final float hintLabelWidth = TypefaceUtils.getStringWidth(hintLabel, paint);
                hintX = keyWidth - mKeyHintLetterPadding
                        - Math.max(hintDigitWidth, hintLabelWidth) / 1.0f;
                hintBaseline = -paint.ascent();
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + hintX
                        , key.getY() + hintBaseline + adjustmentY, paint);
            }

            // final float adjustmentY = params.mHintLabelVerticalAdjustment * labelCharHeight;
//            canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + hintX/1.5f
//                    , key.getY() + hintBaseline*1.2f + adjustmentY, paint);
        }
    }

    public void drawIconKey(Context context, Keyboard keyboard, Key key, Canvas canvas, Paint paintIconFilter, Paint paintIcon, KeyDrawParams params, boolean isSearchGif, KeyboardView keyboardView, Shader shader) {
        Drawable icon;
        if (key.getCode() == Constants.CODE_ENTER && isSearchGif) {
            icon = context.getResources().getDrawable(R.drawable.sym_keyboard_return_lxx_light, null);
        } else {
            icon = key.getIcon(keyboard.mIconsSet, params.mAnimAlpha);
        }
        if (key.getCode() == Constants.CODE_COMMA || key.getCode() == Constants.CODE_COMMA_OTHER_LANGUAGE) {
            MoreKeySpec[] moreKeySpecs = key.getMoreKeys();
            if (moreKeySpecs != null && moreKeySpecs.length > 0) {
                Drawable iconHint = keyboardView.getKeyboard().mIconsSet.getIconDrawable(key.getMoreKeys()[0].mIconId);
                if (shader != null) {
                    paintIcon.setShader(shader);
                }
                drawIcon(canvas, keyboardView, iconHint, paintIcon, key);
            }
        } else if (key.getLabel() == null && icon != null && key.getCode() != Constants.CODE_SPACE) {
//            drawIcon(canvas, icon, paintIconFilter, paintIcon, key.getX(), key.getY(), key.getWidth(), key.getHeight());
            drawIcon(canvas, keyboardView, icon, paintIcon, key);
        }
    }


    public void drawRectBorderKey(Key key, Canvas canvas, Paint paintRect, float keyPaddingX, float keyPaddingY, float radiusKey, String styleBorder, int alpha, float strokeWidth) {

        //  if (Constants.CODE_SPACE == key.getCode() ) {
        // alpha = paintRect.getAlpha();
        if (styleBorder.equals("style_fill")) {
            paintRect.setAlpha(alpha);
            paintRect.setStyle(Paint.Style.FILL);
        } else if (styleBorder.equals("style_none")) {
            paintRect.setAlpha(0);
        } else {
            paintRect.setAlpha(255);
            paintRect.setStyle(Paint.Style.STROKE);
        }
        paintRect.setAntiAlias(true);
        canvas.drawRoundRect(key.getX() + keyPaddingX, key.getY() + keyPaddingY, key.getX() + key.getWidth() - keyPaddingX, key.getY() + key.getHeight() + keyPaddingY,
                radiusKey, radiusKey, paintRect);
//todo draw again circle
        //  canvas.drawCircle(key.getX() + keyPaddingX, key.getY() + keyPaddingY,
        //         radiusKey, paintRect);


    }


    private static void drawIcon(Canvas canvas, Drawable icon, Paint paintIconFilter, Paint paintIcon,
                                 int x, int y, int width, int height) {
        if (icon instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            if (bitmap != null && paintIconFilter != null) {

                int offX = (width - bitmap.getWidth()) / 2;
                int offY = (height - bitmap.getHeight()) / 2;
                canvas.drawRect(x + offX, y + offY, x + offX + bitmap.getWidth(), y + offY + bitmap.getHeight(), paintIcon);
                canvas.drawBitmap(bitmap, x + offX, y + offY, paintIconFilter);
            }

        } else {
            icon.setBounds(x, y, x + width, y + height);
            canvas.drawRect(icon.getBounds(), paintIcon);
            icon.draw(canvas);
        }
    }

    private void drawIcon(Canvas canvas, KeyboardView keyboardView, Drawable icon, Paint paintIcon,
                          Key key) {
        if (icon instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                int left = 0;
                int top = 0;
                Bitmap bitmapResult;
                if (bitmapIcons.containsKey(key.getIconId())) {
                    bitmapResult = bitmapIcons.get(key.getIconId());
                } else {
                    float maxKeySize = Math.max(key.getWidth(), key.getHeight()) * 0.7f;
                    int maxBitmapSize = Math.max(bitmap.getWidth(), bitmap.getHeight());
                    float ratio = maxKeySize / maxBitmapSize;
                    if (ratio > 1) {
                        ratio = 1;
                    }
                    if (ratio == 1) {
                        bitmapResult = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    } else {
                        if (key.getCode() == Constants.CODE_COMMA || key.getCode() == Constants.CODE_COMMA_OTHER_LANGUAGE) {
                            ratio = ratio * 0.5f;
                        }
                        int w = (int) (bitmap.getWidth() * ratio);
                        int h = (int) (bitmap.getHeight() * ratio);
                        bitmapResult = Bitmap.createScaledBitmap(bitmap, w > 0 ? w : bitmap.getWidth(), h > 0 ? h : bitmap.getHeight(), true).copy(Bitmap.Config.ARGB_8888, true);
                    }
                    Canvas canvas1 = new Canvas(bitmapResult);
                    Paint drawPaint = new Paint();
                    drawPaint.setColor(Color.WHITE);  // the color doesn't really matter
                    drawPaint.setXfermode(mXfermodeDst);
                    canvas1.drawPaint(drawPaint);
                    drawPaint.setXfermode(null);
                    bitmapIcons.put(key.getIconId(), bitmapResult);

                }
                if (bitmapResult == null) {
                    return;
                }
                int offX = (key.getWidth() - bitmapResult.getWidth()) / 2;
                int offY = (key.getHeight() - bitmapResult.getHeight()) / 2;
                if (key.getCode() == Constants.CODE_COMMA || key.getCode() == Constants.CODE_COMMA_OTHER_LANGUAGE) {
                    offY = offY - bitmapResult.getHeight() / 4;
                }
                if (keyboardView.getThemeKeyBgStyle().equals(Constant.STYLE_KEY_BG_FILL)) {
                    if (!key.ismPressed()) {
                        float ratio = keyboardView.getThemeKeyBgAlpha() / 256;
                        if (ratio >= Constant.THRESHOLD_COLOR_CHANGE) {
                            paintIcon.setShader(null);
                            paintIcon.setColor(Color.WHITE);
                        }
                    }

                }
                left = (key.getX() + offX);
                top = (key.getY() + offY);
                Canvas canvas1 = new Canvas(bitmapResult);
                canvas1.translate(-left, -top);
                Xfermode xfermode = paintIcon.getXfermode();
                paintIcon.setXfermode(mXfermodeSrcAtop);
                canvas1.drawRect(0, 0, keyboardView.getWidth(), keyboardView.getHeightWithMargin(), paintIcon);
                canvas1.translate(left, top);
                paintIcon.setXfermode(xfermode);
                canvas.drawBitmap(bitmapResult, key.getX() + offX, key.getY() + offY, paintIcon);
            }
        } else {
            if (icon != null) {
                icon.setBounds(key.getX(), key.getY(), key.getX() + key.getWidth(), key.getHeight() + key.getHeight());
                canvas.drawRect(icon.getBounds(), paintIcon);
                icon.draw(canvas);
            }

        }
    }

    public static int setAlphaKey(float alpha, Paint paintRect, String styleBorder) {
        int VALUE_DEFAULT_MAX = 100;
        int alphaDefault = 100;
        int mAlpha = 0;
        switch (styleBorder) {
            case "style_fill":
                mAlpha = (int) (alphaDefault * alpha / VALUE_DEFAULT_MAX);
                paintRect.setAlpha(mAlpha);
                break;

            case "style_stroke":
                mAlpha = 155 + (int) (alphaDefault * alpha / VALUE_DEFAULT_MAX);
                paintRect.setAlpha(mAlpha);
                break;

            default:
                break;
        }
        return mAlpha;
    }

    public String setStyleBorder(String styleBorder, Paint paintRect, int mAlpha) {
        switch (styleBorder) {
            case "style_fill":
                paintRect.setAlpha(mAlpha);
                paintRect.setStyle(Paint.Style.FILL);
                break;

            case "style_stroke":
                paintRect.setAlpha(mAlpha);
                paintRect.setStyle(Paint.Style.STROKE);
                break;

            default:
                break;
        }
        return styleBorder;
    }

    public void setStrokeWidth(Context context, float strokeWid, Paint paintRect, Paint paintPreview, Paint paintIcon) {
        float strokeWidthDefault = context.getResources().getDisplayMetrics().density * 1;
        float strokeWidth = strokeWidthDefault * strokeWid / VALUE_DEFAULT;

        paintRect.setStrokeWidth(strokeWidth);
        paintPreview.setStrokeWidth(strokeWidth);
        paintIcon.setStrokeWidth(strokeWidth);
    }

    private static float getRadius(float centerX, float centerY, float width, float height) {
        if (centerX < width / 2) {
            if (centerY < height / 2) {
                return (float) Math.sqrt(Math.pow(width - centerX, 2) + Math.pow(height - centerY, 2));
            } else {
                return (float) Math.sqrt(Math.pow(width - centerX, 2) + Math.pow(centerY, 2));
            }
        } else {
            if (centerY < height / 2) {
                return (float) Math.sqrt(Math.pow(centerX, 2) + Math.pow(height - centerY, 2));
            } else {
                return (float) Math.sqrt(Math.pow(centerX, 2) + Math.pow(centerY, 2));
            }
        }
    }

    private static Shader onDrawKeyboardStyleLeftToRight(KeyboardView keyboardView, float mTranslate, int[] colors, ThemeModel objectTheme) {

        Matrix mMatrix = new Matrix();
        float rangeColor = objectTheme.getKey().getLed().getRange() / VALUE_DEFAULT;
        LinearGradient mLinearGradient = new LinearGradient(0, 0, keyboardView.getWidth() * rangeColor,
                -keyboardView.getHeight() / 2f * rangeColor, colors,
                null, Shader.TileMode.REPEAT);
        mMatrix.setTranslate(mTranslate, 0);
        mLinearGradient.setLocalMatrix(mMatrix);
        return mLinearGradient;
    }

    private static Shader onDrawKeyboardStyleCircle(KeyboardView keyboardView, float mTranslate, int[] colors, ThemeModel objectTheme) {

        int length2 = colors.length;
        int m = keyboardView.getWidth() / (length2 * 2);
        int n = (int) (mTranslate / m);

        float[] positions2 = new float[length2];
        int[] cls = new int[length2];
        float r = mTranslate / m - n;
        for (int i = 0; i < length2; i++) {
            cls[(i + n) % length2] = colors[i];
            positions2[i] = (i + r) * 1f / length2;
        }
        float centerX = keyboardView.getWidth() / 2f;
        float centerY = keyboardView.getHeight() / 2f;
        return new RadialGradient(centerX, centerY,
                ThemesLEDControl.getRadius(centerX, centerY, keyboardView.getWidth(), keyboardView.getHeight()), cls, positions2, Shader.TileMode.REPEAT);
    }


    private static Shader onDrawKeyboardStyleLeftToRightSlow(KeyboardView keyboardView, float mTranslate, int[] colors, ThemeModel objectTheme) {

        // mTranslate += speedColor;
        Matrix mMatrix = new Matrix();
        float rangeColor = objectTheme.getKey().getLed().getRange() / VALUE_DEFAULT;
        LinearGradient mLinearGradientLED3;
        mLinearGradientLED3 = new LinearGradient(0, 0, keyboardView.getWidth() * colors.length * rangeColor, -keyboardView.getHeight() / 2f * rangeColor, colors,
                null, Shader.TileMode.REPEAT);

        mMatrix.setTranslate(mTranslate, 0);
        mLinearGradientLED3.setLocalMatrix(mMatrix);

        return mLinearGradientLED3;
    }

    public static Shader getLEDStyle(KeyboardView keyboardView, float mTranslate, int[] colors, ThemeModel objectTheme) {
        switch (objectTheme.getKey().getLed().getStyleLed()) {

            case 1:
                return onDrawKeyboardStyleLeftToRight(keyboardView, mTranslate, colors, objectTheme);
            case 2:
                return onDrawKeyboardStyleLeftToRightSlow(keyboardView, mTranslate, colors, objectTheme);
            case 3:
                return onDrawKeyboardStyleCircle(keyboardView, mTranslate, colors, objectTheme);

        }

        return null;

    }

}
