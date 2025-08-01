package com.tapbi.spark.yokey.feature;

import static com.tapbi.spark.yokey.util.CommonUtil.getDrawableNew;
import static com.tapbi.spark.yokey.util.CommonUtil.getDrawableThemeFeatured;
import static com.tapbi.spark.yokey.util.CommonUtil.keyThemeKeyIconCommaKeyBoard;
import static com.tapbi.spark.yokey.util.CommonUtil.keyThemeKeyIconLanguageKeyBoard;
import static com.tapbi.spark.yokey.util.CommonUtil.keyThemeKeyNoIconShiftEnterKeyBoard;
import static com.tapbi.spark.yokey.util.CommonUtil.keyThemeKeyTextCommaKeyBoard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextUtils;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.keyboard.MainKeyboardView;
import com.android.inputmethod.keyboard.emoji.DynamicGridKeyboard;
import com.android.inputmethod.keyboard.emoji.EmojiPageKeyboardView;
import com.android.inputmethod.keyboard.internal.KeyDrawParams;
import com.android.inputmethod.keyboard.internal.MoreKeySpec;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.utils.TypefaceUtils;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;
import com.tapbi.spark.yokey.util.DisplayUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

public class ThemeGradientControl {
    // The maximum key label width in the proportion to the key width.
    private static final float MAX_LABEL_RATIO = 0.90f;
    public int sizeEmoji = 0;
    private Map<String, Bitmap> bmKey = new HashMap<>();
    private Map<String, Drawable> drawableIconMap = new HashMap<>();
    private Bitmap bmTextTheme6001 = null;
    private Bitmap bmLanguageTheme6001 = null;

    private static void blendAlpha(@Nonnull final Paint paint, final int alpha) {
        final int color = paint.getColor();
        paint.setARGB((paint.getAlpha() * alpha) / Constants.Color.ALPHA_OPAQUE, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static boolean isScaleKey(Key key, ThemeModel themeModel) {
        boolean isScale = false;
        if (key.getCode() == Constants.CODE_DELETE
                || key.getCode() == Constants.CODE_SWITCH_ALPHA_SYMBOL
                || key.getCode() == Constants.CODE_ENTER
                || key.getCode() == Constants.CODE_COMMA_KEY
                || key.getCode() == Constants.CODE_PERIOD_KEY
                || key.getCode() == Constants.CODE_SHIFT
        ) {
            isScale = themeModel.getBackgroundKey() == null || themeModel.getBackgroundKey().getScaleKey() == null || !themeModel.getBackgroundKey().getScaleKey().equals("false");
        } else if (key.getCode() == Constants.CODE_SPACE) {
            isScale = themeModel.getKey() == null || themeModel.getKey().getSpecial() == null || themeModel.getKey().getSpecial().getScaleKey() == null || !themeModel.getKey().getSpecial().getScaleKey().equals("false");
        } else {
            isScale = themeModel.getKey() == null || themeModel.getKey().getText() == null || themeModel.getKey().getText().getScaleKey() == null || !themeModel.getKey().getText().getScaleKey().equals("false");
        }
        return isScale;
    }

    public void drawKeyLabel(KeyboardView keyboardView, Context context, Keyboard keyboard, Key key, Canvas canvas,
                             Paint paint, KeyDrawParams params, ThemeModel themeModel, float mKeyTextShadowRadius,
                             boolean isUsingLanguageKeyboardOtherQwerty) {
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final float centerX = keyWidth * 0.5f;
        final float centerY = keyHeight * 0.5f;
        float labelX = centerX;
        float labelBaseline;
        String label = key.getLabel();
        if (keyboard == null) {
            return;
        }

        if (key.getCode() == Constants.CODE_SPACE) {
            label = keyboard.mId.mSubtype.getFullDisplayName();
            if (label == null) {
                label = ((MainKeyboardView) keyboardView).fullLanguageDisplay;
            }
        }
        if (label != null) {
            if (key.getCode() != Constants.CODE_SHIFT && key.getCode() != Constants.CODE_SWITCH_ALPHA_SYMBOL && key.getCode() != Constants.CODE_SPACE && !(key instanceof DynamicGridKeyboard.GridKey)) {
                label = CommonUtil.replaceTextFontOUTPUT(isUsingLanguageKeyboardOtherQwerty, App.getInstance().fontRepository.charSequences, label, label, App.getInstance().fontRepository.font, App.getInstance().fontRepository.key_Font);
            }
            paint.setTypeface(key.selectTypeface(params));
            App.getInstance().textSize = key.selectTextSize(params);
            paint.setTextSize(App.getInstance().textSize);
            final float labelCharHeight = TypefaceUtils.getReferenceCharHeight(paint);
            final float labelCharWidth = TypefaceUtils.getReferenceCharWidth(paint);
            if (key.getCode() == Constants.CODE_SPACE) {
                if (label.contains("(")) {
                    int wText = CommonUtil.getWidthString(label, paint);
                    boolean isChangeLable = false;
                    if (wText > keyWidth * 0.8) {
                        for (int i = 2; i < label.length(); i++) {
                            String text = label.substring(0, label.length() - i);
                            if (text.contains("(")) {
                                text += "..)";
                            } else {
                                text += "(..)";
                            }
                            if (text.contains("(..)")) text = text.replace("(..)", "");
                            if (CommonUtil.getWidthString(text, paint) < keyWidth * 0.8) {
                                isChangeLable = true;
                                label = text;
                                break;
                            }
                        }
                    }
                }

            }

            // Vertical label text alignment.
            labelBaseline = centerY + labelCharHeight / 2.0f;

            // Horizontal label text alignment
            if (key.isAlignLabelOffCenter()) {
                // The label is placed off center of the key. Used mainly on "phone number" layout.
                labelX = centerX + params.mLabelOffCenterRatio * labelCharWidth;
                paint.setTextAlign(Paint.Align.LEFT);
            } else {
                paint.setTextAlign(Paint.Align.CENTER);
            }
            if (key.needsAutoXScale()) {
                final float ratio = Math.min(1.0f, (keyWidth * MAX_LABEL_RATIO) /
                        TypefaceUtils.getStringWidth(label, paint));
                if (key.needsAutoScale()) {
                    final float autoSize = paint.getTextSize() * ratio;
                    paint.setTextSize(autoSize);
                } else {
                    paint.setTextScaleX(ratio);
                }
            }
            if (keyboardView instanceof EmojiPageKeyboardView) {
                if (sizeEmoji == 0) {
                    sizeEmoji = (int) paint.getTextSize();
                }
                paint.setTextSize(sizeEmoji);
            }
            if (key.isEnabled()) {//Change color text
                if (Objects.equals(themeModel.getId(), "6006")) {
                    int[] arrayColor = new int[]{Color.parseColor("#E63535"), Color.parseColor("#FF8800")};
                    LinearGradient linearGradient = new LinearGradient(key.getX(), key.getY(), key.getX() + keyWidth, key.getY() + keyHeight, arrayColor, null, Shader.TileMode.CLAMP);
                    paint.setShader(linearGradient);
                } else if (key.getLabel() != null && key.getLabel().equals("= \\ <") && (Objects.equals(themeModel.getId(), "6010") || Objects.equals(themeModel.getId(), "3006"))) {
                    paint.setShader(null);
                    if (Objects.equals(themeModel.getId(), "6010")) {
                        paint.setColor(CommonUtil.hex2decimal("#E81B18"));
                    } else if (Objects.equals(themeModel.getId(), "3006")) {
                        paint.setColor(Color.WHITE);
                    }
                } else {
                    paint.setShader(null);
                    if (key.getCode() == Constants.CODE_SPACE) {
                        paint.setColor(CommonUtil.hex2decimal(themeModel.getKey().getSpecial().getTextColor()));
                    } else {
                        if (key.ismPressed()) {
                            paint.setColor(CommonUtil.hex2decimal(themeModel.getKey().getText().getTextColorPressed()));
                        } else {
                            paint.setColor(CommonUtil.hex2decimal(themeModel.getKey().getText().getTextColor()));
                        }

                    }

                }
                // Set a drop shadow for the text if the shadow radius is positive value.
                if (mKeyTextShadowRadius > 0.0f) {
                    paint.setShadowLayer(mKeyTextShadowRadius, 0.0f, 0.0f, params.mTextShadowColor);
                } else {
                    paint.clearShadowLayer();
                }
            } else {
                // Make label invisible
                paint.setColor(Color.TRANSPARENT);
                paint.clearShadowLayer();
            }

            blendAlpha(paint, params.mAnimAlpha);

            try {
                String strPathIconKeyText = CommonUtil.getPathImage(context, themeModel, key, themeModel.getKey().getText().getNormal());
                if (key.ismPressed() ) {
                    strPathIconKeyText = CommonUtil.getPathImage(context, themeModel, key, themeModel.getKey().getText().getPressed());
                }
                if (key.getCode() == Constants.CODE_SHIFT) {
                    strPathIconKeyText = strPathIconKeyText.replace("btn_key_text.png", "btn_key_shift.png");
                    drawIcon(canvas, App.getInstance().themeRepository.getDrawableKey(strPathIconKeyText, isScaleKey(key, themeModel)), key.getX(), key.getY(), key.getWidth(), key.getHeight(), themeModel, key, false);
                } else {
                    if (strPathIconKeyText.contains("6001") && strPathIconKeyText.contains("btn_key_text") && !(key instanceof DynamicGridKeyboard.GridKey)) {//Long.parseLong(themeModel.getId()) > 6000
                        Bitmap bitmap = null;
                        if (bmTextTheme6001 != null) {
                            if (bmTextTheme6001.getWidth() == key.getWidth() || bmTextTheme6001.getHeight() == key.getHeight()) {
                                bitmap = bmTextTheme6001;
                            }
                        }
                        if (bitmap == null) {
                            InputStream istr = context.getAssets().open(strPathIconKeyText.substring(Constant.FOLDER_ASSET.length()));
                            bitmap = BitmapFactory.decodeStream(istr);
                            if (key.getWidth() < key.getHeight()) {
                                int height = bitmap.getHeight() * key.getWidth() / bitmap.getWidth();
                                bitmap = Bitmap.createScaledBitmap(bitmap, key.getWidth(), height, true);
                            } else {
                                int width = bitmap.getWidth() * key.getHeight() / bitmap.getHeight();
                                bitmap = Bitmap.createScaledBitmap(bitmap, width, key.getHeight(), true);
                            }

                            bmTextTheme6001 = bitmap;
                        }
                        int left = key.getX() + (key.getWidth() - bitmap.getWidth()) / 2;
                        int top = key.getY() + (key.getHeight() - bitmap.getHeight()) / 2;
                        if (DisplayUtils.getScreenHeight() < DisplayUtils.getScreenWidth() || keyboard.getSortedKeys().size() < 20) {
                            int color = paint.getColor();
                            paint.setColor(Color.parseColor("#BD9659"));
                            if (key.hasHintLabel()) {
                                canvas.drawRoundRect(key.getX(), key.getY(), key.getX() + key.getWidth(), key.getY() + key.getHeight(), 10, 10, paint);
                            }
                            paint.setColor(color);
                        }
                        if (keyboard.getSortedKeys().size() < 20 && key.getLabel() != null && !key.getLabel().equals(".") && !key.getLabel().equals("-")) {
                            left = key.getX() + key.getWidth() / 2 - bitmap.getWidth() * 3 / 4;
                            top = key.getY() + (key.getHeight() - bitmap.getHeight()) / 2;
                        }
                        canvas.drawBitmap(bitmap, left, top, null);
                    } else {
                        drawIcon(canvas, App.getInstance().themeRepository.getDrawableKey(strPathIconKeyText, isScaleKey(key, themeModel)), key.getX(), key.getY(), key.getWidth(), key.getHeight(), themeModel, key, key.getCode() != Constants.CODE_SWITCH_ALPHA_SYMBOL);
                    }
                }
                canvas.save();
                paint.setAntiAlias(true);
                if (!Objects.equals(themeModel.getId(), "6006")) {
                    paint.setShader(null);
                }
//                KeyboardId keyboardId = keyboard.mId;
//                Timber.e("hachung keyboardId:" + keyboardId.mElementId);

                if (!keyThemeKeyIconCommaKeyBoard.contains(themeModel.getId())
                        || keyThemeKeyTextCommaKeyBoard.contains(themeModel.getId())
                        || (key.getCode() != Constants.CODE_COMMA_KEY && key.getCode() != Constants.CODE_SWITCH_ALPHA_SYMBOL)
                        || (key.getCode() == Constants.CODE_SWITCH_ALPHA_SYMBOL && !keyThemeKeyNoIconShiftEnterKeyBoard.contains(themeModel.getId()))) {
                    if ("6029".equals(themeModel.getId()) && key.getCode() == Constants.CODE_SPACE) {
                        float quarterFromCenterToBottom = centerY + keyHeight * 0.2f;
                        labelBaseline = quarterFromCenterToBottom - (paint.descent() + paint.ascent()) / 2.0f;
                    }
                    canvas.drawText(label, 0, label.length(), key.getX() + labelX, key.getY() + labelBaseline, paint);

                }

                paint.setAntiAlias(false);
                canvas.restore();
                paint.setTextScaleX(1.0f);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Bitmap getBitmap(Drawable drawable) {
        Bitmap bm = Bitmap.createBitmap(1080, 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        drawable.draw(canvas);
        return bm;
    }

    public void drawKeyLabelMoreKeyboardView(Keyboard keyboard, Key key, Canvas canvas,
                                             Paint paint, KeyDrawParams params, boolean isSearchGif, ThemeModel themeModel, float mKeyTextShadowRadius
            , boolean isUsingLanguageKeyboardOtherQwerty) {

        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final float centerX = keyWidth * 0.5f;
        final float centerY = keyHeight * 0.5f;
        float labelX = centerX;
        float labelBaseline;
        String label = key.getLabel();
        if (label != null) {
            if (key.getCode() != Constants.CODE_SPACE)
                label = CommonUtil.replaceTextFontOUTPUT(isUsingLanguageKeyboardOtherQwerty, App.getInstance().fontRepository.charSequences, label, label, App.getInstance().fontRepository.font, App.getInstance().fontRepository.key_Font);
            paint.setTypeface(key.selectTypeface(params));
            int sizeT = key.selectTextSize(params);
            if (App.getInstance().textSize != 0 && sizeT > App.getInstance().textSize)
                sizeT = App.getInstance().textSize;
            paint.setTextSize(sizeT);
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
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
                paint.setTextAlign(Paint.Align.CENTER);
            }
            if (key.needsAutoXScale()) {
                final float ratio = Math.min(1.0f, (keyWidth * MAX_LABEL_RATIO) /
                        TypefaceUtils.getStringWidth(label, paint));
                if (key.needsAutoScale()) {
                    final float autoSize = paint.getTextSize() * ratio;
                    paint.setTextSize(autoSize);
                } else {
                    paint.setTextScaleX(ratio);
                }
            }

            if (key.isEnabled()) {
                //set color for more keyboard
                if (themeModel.getTypeKeyboard().equals(Constants.ID_CATEGORY_COLOR) || themeModel.getTypeKeyboard().equals(Constants.ID_CATEGORY_GRADIENT) ||
                        themeModel.getTypeKeyboard().equals(Constants.ID_CATEGORY_WALL)) {
                    paint.setColor(CommonUtil.hex2decimal(themeModel.getKey().getText().getTextColor()));
                } else {
                    paint.setColor(CommonUtil.hex2decimal(themeModel.getPopup().getMinKeyboard().getTextColorSelected()));
                }
                // paint.setColor(CommonUtil.hex2decimal(themeModel.getPopup().getMinKeyboard().getTextColorSelected()));
                // Set a drop shadow for the text if the shadow radius is positive value.
                if (mKeyTextShadowRadius > 0.0f) {
                    paint.setShadowLayer(mKeyTextShadowRadius, 0.0f, 0.0f, params.mTextShadowColor);
                } else {
                    paint.clearShadowLayer();
                }
            } else {
                // Make label invisible
                paint.setColor(Color.TRANSPARENT);
                paint.clearShadowLayer();
            }

            blendAlpha(paint, params.mAnimAlpha);

            try {
                canvas.drawText(label, 0, label.length(), key.getX() + labelX, key.getY() + labelBaseline, paint);
                // Turn off drop shadow and reset x-scale.
                paint.clearShadowLayer();

                paint.setTextScaleX(1.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        drawIconKey(App.getInstance(), keyboard, key, canvas, params, isSearchGif, themeModel, 10, paint, true);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void drawIconKey(Context context, Keyboard keyboard, Key key, Canvas canvas,
                            KeyDrawParams params, boolean isSearchGif, ThemeModel themeModel, float mSpacebarIconWidthRatio, Paint paint, boolean isMoreKeyboardView) {
        Drawable icon = (keyboard == null) ? null
                : key.getIcon(keyboard.mIconsSet, params.mAnimAlpha);
        Key keyText = null;
        Key keyShift = null;
        Key keyLanguage = null;

        Key keyEnter = null;
        Key keyDelete = null;
        Key keySpace = null;
        //for (Key key : list) {
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
        try {
            boolean enableMultiply = false;
            if (key.getCode() == Constants.CODE_ENTER) {
                if (Objects.equals(themeModel.getId(), "6001") || Objects.equals(themeModel.getId(), "6006")) {
                    icon = checkDrawable(context, themeModel.getTypeKey(), "btn_key_enter.png", keyEnter, "6006");
//                    icon = CommonUtil.getDrawable(context, themeModel.getTypeKey(), "btn_key_enter.png", keyEnter);
                } else if (Objects.equals(themeModel.getId(), "6007")) {
                    icon = checkDrawable(context, themeModel.getTypeKey(), "ic_enter.png", keyEnter, "6007");
//                    icon = CommonUtil.getDrawableThemeFeatured(context, themeModel.getTypeKey(), "ic_enter.png", keyEnter);
                } else {
                    if (isSearchGif) {
                        icon = context.getResources().getDrawable(R.drawable.sym_keyboard_search_lxx_light, null);
                    } else {
                        icon = key.getIcon(keyboard.mIconsSet, params.mAnimAlpha);
                    }
                }
                enableMultiply = true;
            } else if (key.getCode() == Constants.CODE_LANGUAGE_SWITCH) {
//                if(CommonUtil.printInputLanguages(context)>1) {
                icon = context.getResources().getDrawable(R.drawable.sym_keyboard_language_switch_dark, null);
                enableMultiply = true;
            } else if (key.getCode() == Constants.CODE_DELETE) {
                if (themeModel.getId() != null) {
                    if (Objects.equals(themeModel.getId(), "6006") || Objects.equals(themeModel.getId(), "6001")) {
                        icon = checkDrawable(context, themeModel.getTypeKey(), "btn_key_delete.png", keyDelete, "6006");
//                    icon = CommonUtil.getDrawable(context, themeModel.getTypeKey(), "btn_key_delete.png", keyDelete);
                    } else if (Objects.equals(themeModel.getId(), "6010")) {
                        icon = getDrawableThemeFeatured(context, themeModel.getTypeKey(), "ic_delete.png", keyDelete);
                    } else {
                        icon = getDrawableNew(context, Long.parseLong(themeModel.getId()), "ic_delete.png", keyDelete, Constants.CODE_DELETE);
                    }
                }
                // String path =App.getInstance().file.toString() + "/" + themeModel.getTypeKey() + checkFileHDPI(context) + strPath;

                enableMultiply = true;
            } else if (key.getCode() == Constants.CODE_SHIFT) {
               /* if (Objects.equals(themeModel.getId(), "6006") || Objects.equals(themeModel.getId(), "6001")) {
//                    icon = CommonUtil.getDrawable(context, themeModel.getTypeKey(), "btn_key_shift.png", keyShift);
                    icon = checkDrawable(context, themeModel.getTypeKey(), "btn_key_shift.png", keyShift, "6006");
                } else if ((themeModel.getTypeKeyboard().equals(Constants.ID_CATEGORY_COLOR) || themeModel.getTypeKeyboard().equals(Constants.ID_CATEGORY_WALL))
                        && themeModel.getBackground().getBackgroundImage().equals("bg_keyboard.jpg")) {
                    icon = getDrawableNew(context, Long.parseLong(themeModel.getId()), "ic_shift.png", keyShift, Constants.CODE_SHIFT);
                } else {
                    icon = context.getResources().getDrawable(R.drawable.sym_keyboard_shift_holo_dark, null);
                }*/
//                if (Objects.equals(themeModel.getId(), "6020")) {
//                    icon = context.getResources().getDrawable(R.drawable.ic_shift_1, null);
//                } else {
//                    icon = key.getIcon(keyboard.mIconsSet, params.mAnimAlpha);
//                }
                icon = key.getIcon(keyboard.mIconsSet, params.mAnimAlpha);

                enableMultiply = true;
            }
            if (!Objects.equals(themeModel.getId(), "3003") && !Objects.equals(themeModel.getId(), "3001") && !Objects.equals(themeModel.getId(), "3006")) {
                if (enableMultiply) {

                    String strColor = String.format("#%06X", 0xFFFFFF & CommonUtil.hex2decimal(themeModel.getKey().getSpecial().getTextColor()));
                    if (themeModel.getColorSymbol() != null && !themeModel.getColorSymbol().isEmpty()) {
                        strColor = String.format("#%06X", 0xFFFFFF & CommonUtil.hex2decimal(themeModel.getColorSymbol()));
                    }
                    if (icon != null) {
                        if (Objects.equals(themeModel.getId(), "6010") && key.getCode() == Constants.CODE_SHIFT) {
                            icon.setColorFilter(Color.parseColor("#E81B18"),
                                    PorterDuff.Mode.MULTIPLY);
                        } else if (Objects.equals(themeModel.getId(), "6020")) {
                            icon.setColorFilter(Color.parseColor("#3A2127"),
                                    PorterDuff.Mode.MULTIPLY);
                        } else {
                            icon.setColorFilter(Color.parseColor(strColor),
                                    PorterDuff.Mode.MULTIPLY);
                        }
                    }
                }
            } else {
                if (icon != null) {
                    icon.setColorFilter(null);
                }
            }
            String strPathIconKeyText = CommonUtil.getPathImage(context, themeModel, key, themeModel.getKey().getSpecial().getNormal());
            if ((!keyThemeKeyIconCommaKeyBoard.contains(themeModel.getId()) || keyThemeKeyTextCommaKeyBoard.contains(themeModel.getId())) && (key.getCode() == Constants.CODE_COMMA || key.getCode() == Constants.CODE_COMMA_OTHER_LANGUAGE)) {
                MoreKeySpec[] moreKeySpecs = key.getMoreKeys();
                if (moreKeySpecs != null && moreKeySpecs.length > 0 && keyboard != null) {
                    Drawable iconHint = keyboard.mIconsSet.getIconDrawable(key.getMoreKeys()[0].mIconId);
                    if (iconHint instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) iconHint).getBitmap();
                        Bitmap bm = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.6), (int) (bitmap.getHeight() * 0.6), true);
                        int x = key.getX() + (key.getWidth() - bm.getWidth()) / 2;
                        int y = key.getY() + bm.getHeight() / 4;
                        String strColor = themeModel.getKey().getSpecial().getTextColor();
                        if (themeModel.getColorSymbol() != null && !themeModel.getColorSymbol().isEmpty()) {
                            strColor = themeModel.getColorSymbol();
                        }
                        int markerColor = CommonUtil.hex2decimal(strColor);
                        iconHint.setBounds(x, y, bm.getWidth() + x, bm.getHeight() + y);
                        iconHint.setColorFilter(markerColor, PorterDuff.Mode.MULTIPLY);
                        iconHint.draw(canvas);
                    }
                }
            }

            if (key.getLabel() == null && icon != null) {
                final int iconWidth;
                if (key.getCode() == Constants.CODE_SPACE && icon instanceof NinePatchDrawable) {
                    iconWidth = (int) (key.getDrawWidth() * mSpacebarIconWidthRatio);
                } else {
                    iconWidth = Math.min(icon.getIntrinsicWidth(), key.getDrawWidth());
                }
                final int iconHeight = icon.getIntrinsicHeight();

                if (icon instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                    if (bitmap != null) {
                        int xIcon = (key.getDrawWidth() - iconWidth) / 2;
                        int yIcon = (key.getHeight() - iconHeight) / 2;
//                        icon.clearColorFilter();
                        Drawable drawable = null;
                        if (App.getInstance().themeRepository != null)
                            drawable = App.getInstance().themeRepository.getDrawableKey(strPathIconKeyText, isScaleKey(key, themeModel));
                        if (drawable != null) {
                            if (key.ismPressed()) {
//                            drawable.setColorFilter(CommonUtil.hex2decimal(themeModel.getKey().getText().getTextColor()), PorterDuff.Mode.MULTIPLY);
                                if (key.getCode() == Constants.CODE_SHIFT || key.getCode() == Constants.CODE_DELETE
                                        || key.getCode() == Constants.CODE_ENTER || key.getCode() == Constants.CODE_LANGUAGE_SWITCH
                                        || key.getCode() == Constants.CODE_SPACE) {
                                    drawIconKeyPress(context, key, canvas, themeModel);
                                } else {
//                                    drawIcon(canvas, drawable, key.getX(), key.getY(), key.getDrawWidth(), key.getHeight(), themeModel, key, false);
                                }
                            } else {
                                drawable.clearColorFilter();
                                if (key.getCode() != Constants.CODE_EMOJI && key.getCode() != Constants.CODE_SETTINGS) {
                                    if (strPathIconKeyText.contains("6001") && key.getCode() == Constants.CODE_LANGUAGE_SWITCH) {////Long.parseLong(Objects.requireNonNull(themeModel.getId())) > 6000
                                        Bitmap bm = null;
                                        if (bmLanguageTheme6001 != null) {
                                            if (bmLanguageTheme6001.getWidth() == key.getWidth() || bmLanguageTheme6001.getHeight() == key.getHeight()) {
                                                bm = bmLanguageTheme6001;
                                            }
                                        }
                                        if (bm == null) {
                                            InputStream istr = context.getAssets().open(strPathIconKeyText.substring(Constant.FOLDER_ASSET.length()));
                                            bm = BitmapFactory.decodeStream(istr);
                                            if (key.getWidth() < key.getHeight()) {
                                                int height = bm.getHeight() * key.getWidth() / bm.getWidth();
                                                bm = Bitmap.createScaledBitmap(bm, key.getWidth(), height, true);
                                            } else {
                                                int width = bm.getWidth() * key.getHeight() / bm.getHeight();
                                                bm = Bitmap.createScaledBitmap(bm, width, key.getHeight(), true);
                                            }

                                            bmLanguageTheme6001 = bm;
                                        }
                                        int left = key.getX() + (key.getWidth() - bm.getWidth()) / 2;
                                        int top = key.getY() + (key.getHeight() - bm.getHeight()) / 2;
                                        if (DisplayUtils.getScreenHeight() < DisplayUtils.getScreenWidth()) {
                                            int color = paint.getColor();
                                            paint.setColor(Color.parseColor("#BD9659"));
                                            if (key.hasHintLabel()) {
                                                canvas.drawRoundRect(key.getX(), key.getY(), key.getX() + key.getWidth(), key.getY() + key.getHeight(), 10, 10, paint);
                                            }
                                            paint.setColor(color);
                                        }
                                        canvas.drawBitmap(bm, left, top, null);
                                    } else {
                                        if (key.getCode() != Constants.CODE_SPACE || (!Objects.equals(themeModel.getId(), "100") && !Objects.equals(themeModel.getId(), "15"))) {
                                            drawIcon(canvas, drawable, key.getX(), key.getY(), key.getDrawWidth(), key.getHeight(), themeModel, key, false);
                                        }
                                    }
                                }
                            }
                        }

                        if ((key.getCode() != Constants.CODE_SPACE
                                || (!Objects.equals(themeModel.getId(), "100") && !Objects.equals(themeModel.getId(), "15")))
                                && (!CommonUtil.keyThemeKeyNoIconShiftEnterKeyBoard.contains(themeModel.getId())
                                || (key.getCode() == Constants.CODE_LANGUAGE_SWITCH && keyThemeKeyIconLanguageKeyBoard.contains(themeModel.getId()))
                                || isMoreKeyboardView
                                || key.getCode() == Constants.CODE_EMOJI

                        )) {
                            if ("6014".equals(themeModel.getId())) {
                                int offsetYEnter = key.getHeight() / 10;
                                switch (key.getCode()) {
                                    case Constants.CODE_ENTER:
                                        drawIcon(canvas, icon, key.getX() + xIcon, key.getY() + offsetYEnter, iconWidth, iconHeight, themeModel, key, true);
                                        break;
                                    case Constants.CODE_DELETE:
                                        drawIcon(canvas, icon, key.getX() + xIcon, key.getY() + yIcon + offsetYEnter, iconWidth, iconHeight, themeModel, key, true);
                                        break;
                                    case Constants.CODE_LANGUAGE_SWITCH:
                                        drawIcon(canvas, icon, key.getX() + xIcon + offsetYEnter, key.getY() + yIcon + offsetYEnter, iconWidth, iconHeight, themeModel, key, true);
                                        break;
                                    default:
                                        drawIcon(canvas, icon, key.getX() + xIcon, key.getY() + yIcon, iconWidth, iconHeight, themeModel, key, true);

                                }
                            } else {
                                drawIcon(canvas, icon, key.getX() + xIcon, key.getY() + yIcon, iconWidth, iconHeight, themeModel, key, true);
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Drawable checkDrawable(Context context, int typeKey, String nameKey, Key key, String idTheme) {
        Drawable icon;
        String path = typeKey + "/" + nameKey;
        if (drawableIconMap.containsKey(path)) {
            icon = drawableIconMap.get(path);
        } else {
            if (idTheme.equals("6007")) {
                icon = CommonUtil.getDrawableThemeFeatured(context, typeKey, nameKey, key);
            } else {
                icon = CommonUtil.getDrawable(context, typeKey, nameKey, key);
            }
            drawableIconMap.put(path, icon);
        }
        return icon;
    }

    public void drawIconKeyPress(Context context, Key key, Canvas canvas, ThemeModel themeModel) {
        Drawable icon;
        try {
            if (key.getCode() == Constants.CODE_LANGUAGE_SWITCH) {
                icon = context.getResources().getDrawable(R.drawable.sym_keyboard_language_switch_dark, null);
                String strColor = String.format("#%06X", 0xFFFFFF & CommonUtil.hex2decimal(themeModel.getKey().getSpecial().getPressed()));
                if (icon != null) {
                    icon.setColorFilter(Color.parseColor(strColor),
                            PorterDuff.Mode.MULTIPLY);
                }
                String strPathIconKeyText = CommonUtil.getPathImage(context, themeModel, key, themeModel.getKey().getSpecial().getPressed());
                drawIcon(canvas, App.getInstance().themeRepository.getDrawableKey(strPathIconKeyText, isScaleKey(key, themeModel)), key.getX(), key.getY(), key.getDrawWidth(), key.getHeight(), themeModel, key, true);
            } else if (key.getCode() == Constants.CODE_SPACE) {
                String strPathIconKeyText = CommonUtil.getPathImage(context, themeModel, key, themeModel.getKey().getSpecial().getPressed());
                drawIcon(canvas, App.getInstance().themeRepository.getDrawableKey(strPathIconKeyText, isScaleKey(key, themeModel)),
                        key.getX(), key.getY(), key.getWidth(), key.getHeight(), themeModel, key, true);
            } else {
                String strPathIconKeyText = CommonUtil.getPathImage(context, themeModel, key, themeModel.getKey().getSpecial().getPressed());
                drawIcon(canvas, App.getInstance().themeRepository.getDrawableKey(strPathIconKeyText, isScaleKey(key, themeModel)), key.getX(), key.getY(), key.getDrawWidth(), key.getHeight(), themeModel, key, false);// fix bug 526
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawHintLabel(Key key, Paint paint, KeyDrawParams params, ThemeModel themeModel, int mDefaultKeyLabelFlags,
                              float mKeyShiftedLetterHintPadding, Paint.FontMetrics mFontMetrics, float mKeyHintLetterPadding, Canvas canvas) {
        // Draw hint label.
        final float centerX = key.getWidth() * 0.5f;
        final float centerY = key.getHeight() * 0.5f;
        final String hintLabel = key.getHintLabel();
        if (hintLabel != null) {
            paint.setTextSize(key.selectHintTextSize(params));
//            paint.setColor(CommonUtil.hex2decimal(themeModel.getPopup().getPreview().getTextColor()));
            // TODO: Should add a way to specify type face for hint letters
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            blendAlpha(paint, params.mAnimAlpha);
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
                canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + centerX + 5,
                        key.getY() + hintBaseline * 1.2f + adjustmentY, paint);
//                canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + hintX / 1.5f,
//                        key.getY() + hintBaseline * 1.2f + adjustmentY, paint);
            } else if (key.hasShiftedLetterHint()) {
                // The hint label is placed at top-right corner of the key. Used mainly on tablet.
                hintX = key.getWidth() - mKeyShiftedLetterHintPadding - labelCharWidth / 2.0f;
                paint.getFontMetrics(mFontMetrics);
                hintBaseline = -mFontMetrics.top;
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + hintX,
                        key.getY() + hintBaseline + adjustmentY, paint);
            } else {
                // The hint letter is placed at top-right corner of the key. Used mainly on phone.
                final float hintDigitWidth = TypefaceUtils.getReferenceDigitWidth(paint);
                final float hintLabelWidth = TypefaceUtils.getStringWidth(hintLabel, paint);
                hintX = key.getWidth() - mKeyHintLetterPadding - Math.max(hintDigitWidth, hintLabelWidth) / 1.2f;
                hintBaseline = -paint.ascent();
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + hintX,
                        key.getY() + hintBaseline + adjustmentY, paint);
            }
            // final float adjustmentY = params.mHintLabelVerticalAdjustment * labelCharHeight;
//            canvas.drawText(hintLabel, 0, hintLabel.length(), key.getX() + hintX / 1.5f,
//                    key.getY() + hintBaseline * 1.2f + adjustmentY, paint);
        }
    }

    // Draw popup hint "..." at the bottom right corner of the key.
    public void drawKeyPopupHint(Context context, @Nonnull final Key key, @Nonnull final Canvas canvas,
                                 @Nonnull final Paint paint, @Nonnull final KeyDrawParams params,
                                 String mKeyPopupHintLetter, ThemeModel themeModel, float mKeyHintLetterPadding, float mKeyPopupHintLetterPadding) {
        if (TextUtils.isEmpty(mKeyPopupHintLetter)) {
            return;
        }
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        paint.setTypeface(params.mTypeface);
        paint.setTextSize(params.mHintLetterSize);
        paint.setColor(CommonUtil.hex2decimal(themeModel.getPopup().getMinKeyboard().getTextColor()));
        paint.setTextAlign(Paint.Align.CENTER);
        final float hintX = keyWidth - mKeyHintLetterPadding
                - TypefaceUtils.getReferenceCharWidth(paint) / 2.0f;
        final float hintY = keyHeight - mKeyPopupHintLetterPadding;

        String strPathIconKeyText = CommonUtil.getPathImage(context, themeModel, key, themeModel.getPopup().getMinKeyboard().getBgImage());
        final Drawable iconKeyText = App.getInstance().themeRepository.getDrawableKey(strPathIconKeyText, isScaleKey(key, themeModel));
        canvas.drawBitmap(CommonUtil.drawableToBitmap(iconKeyText), 0, 0, paint);
        canvas.drawText(mKeyPopupHintLetter, hintX, hintY, paint);

    }

    public void drawIcon(@Nonnull final Canvas canvas, final Drawable icon, final int x, final int y, final int width, final int height, ThemeModel themeModel, Key key, boolean isIcon) {
        if (icon != null) {
            int d_0_5 = DisplayUtils.dp2px(0.5f);
            if ((Objects.equals(themeModel.getId(), "6009") || Objects.equals(themeModel.getId(), "6010"))) {
                if (key.getIconId() == 3 || key.getIconId() == 20) {
                    icon.setBounds(x + d_0_5, y, width + d_0_5 * 10 + x, height + d_0_5 * 10 + y);
                } else {
                    if (key.getCode() == Constants.CODE_ENTER && themeModel.getId().equals("6009")) {
                        icon.setBounds(x, y, width - d_0_5 * 10 + x, height + y);
                    } else
                        icon.setBounds(x - d_0_5 * 2, y, width + d_0_5 * 2 + x, height + y);
                }
                // }
            } else {
                if (key.getIconId() == 3 || key.getIconId() == 20) {
                    icon.setBounds(x + d_0_5, y, width + d_0_5 * 10 + x, height + d_0_5 * 10 + y);
                } else icon.setBounds(x, y, width + x, height + y);
            }
            icon.draw(canvas);
        }
    }

    private Bitmap getBitmap(int min, String pathKey) {
        Bitmap bitmap;
        bitmap = CommonUtil.getBitmapFromAsset(App.getInstance().getBaseContext(), pathKey);
        bitmap = Bitmap.createScaledBitmap(bitmap, min, min, true);
        bmKey.remove(pathKey);
        bmKey.put(pathKey, bitmap);
        return bitmap;
    }

    public void drawRectBorderKey(Key key, Canvas canvas, Paint paintRect, float keyPaddingX, float keyPaddingY, float radiusKey, String styleBorder) {

        if (styleBorder.equals("style_fill")) {
            paintRect.setAlpha(255);
            paintRect.setStyle(Paint.Style.FILL);
        } else if (styleBorder.equals("style_none")) {
            paintRect.setAlpha(0);
        } else {
            paintRect.setAlpha(255);
            paintRect.setStyle(Paint.Style.STROKE);
        }

        canvas.drawRoundRect(key.getX() + keyPaddingX, key.getY() + keyPaddingY, key.getX() + key.getWidth() - keyPaddingX, key.getY() + key.getHeight() + keyPaddingY,
                radiusKey, radiusKey, paintRect);
    }
}
