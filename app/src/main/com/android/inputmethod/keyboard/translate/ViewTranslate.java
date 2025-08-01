package com.android.inputmethod.keyboard.translate;

import static com.tapbi.spark.yokey.util.Constant.CODE_LANGUAGE_IN_PUT;
import static com.tapbi.spark.yokey.util.Constant.CODE_LANGUAGE_OUT_PUT;
import static com.tapbi.spark.yokey.util.Constant.KEY_MAX_LENGTH_TRANSLATE;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.databinding.ViewTranslateKbBinding;
import com.android.inputmethod.latin.settings.Settings;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.data.model.LanguageTranslate;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;
import com.tapbi.spark.yokey.util.DisplayUtils;
import com.tapbi.spark.yokey.util.LocaleUtils;
import com.tapbi.spark.yokey.util.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class ViewTranslate extends ConstraintLayout {

    private ViewTranslateKbBinding binding;
    private Context context;
    private boolean translateText = true;

    private boolean checkMaxCharacter = false;

    private Handler handler = new Handler();
    private String textSearch = "";
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            translate(textSearch);
        }
    };


    public ViewTranslate(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ViewTranslate(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewTranslate(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = DataBindingUtil.inflate(inflater, R.layout.view_translate_kb, this, true);
        eventClick();
    }

    private void eventClick() {
        binding.imgBack.setOnClickListener(v -> {
            if (iListenerTranslate != null) {
                doneCharacterTranslate();
                iListenerTranslate.closeTranslate();
            }
        });
        binding.imgClose.setOnClickListener(v -> binding.edtInput.setText(""));

        binding.tvInputLanguage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iListenerTranslate != null) {
                    iListenerTranslate.showLanguageInput();
                }
            }
        });

        binding.tvOutputLanguage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iListenerTranslate != null) {
                    iListenerTranslate.showLanguageOutput();
                }
            }
        });

        binding.imgChangeLanguage.setOnClickListener(v -> {
            String codeInputOld = binding.tvInputLanguage.getContentDescription().toString();
            String nameInputOld = binding.tvInputLanguage.getText().toString();
            String codeOutputOld = binding.tvOutputLanguage.getContentDescription().toString();
            String nameOutputOld = binding.tvOutputLanguage.getText().toString();

            LanguageTranslate input = new LanguageTranslate(codeOutputOld, nameOutputOld);
            LanguageTranslate output = new LanguageTranslate(codeInputOld, nameInputOld);
            App.getInstance().mPrefs.edit().putString(CODE_LANGUAGE_IN_PUT,codeOutputOld).apply();
            App.getInstance().mPrefs.edit().putString(CODE_LANGUAGE_OUT_PUT,codeInputOld).apply();
            setInputLanguage(input);
            setOutputLanguage(output);
            if (iListenerTranslate != null) {
                iListenerTranslate.reverserLanguage();
            }
        });

        binding.edtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Timber.e("ducNQ : onTextChanged: "+s.length());
               /* if(s.length()>1500){
                    binding.edtInput.setText(s.toString().substring(0,1500));
                    binding.edtInput.requestFocus();
                    binding.edtInput.setSelection(1500);
                }*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 1501) {
                    EventBus.getDefault().post(new MessageEvent(KEY_MAX_LENGTH_TRANSLATE));
                }
                if (doNotListenerEdtChange) {
                    doNotListenerEdtChange = false;
                    return;
                }
                textSearch = s.toString();
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 500);
            }
        });
    }


    public void appendTextToEditText(String t) {
        int startSelection = binding.edtInput.getSelectionStart();
        int endSelection = binding.edtInput.getSelectionEnd();

        String currentText = binding.edtInput.getText().toString();

        if (currentText.length() == 0 || (startSelection == endSelection) && (endSelection == currentText.length())) {
            if (binding.edtInput.getText().length() + t.length() > 1500) {
                EventBus.getDefault().post(new MessageEvent(KEY_MAX_LENGTH_TRANSLATE));
            }
            binding.edtInput.append(t);
        } else {
            String afterDelete = currentText.substring(0, startSelection) + t + currentText.substring(endSelection);
            binding.edtInput.setText(afterDelete);
            binding.edtInput.setSelection(startSelection + t.length());
        }
    }

    public void deleteText() {
        int startSelection = binding.edtInput.getSelectionStart();
        int endSelection = binding.edtInput.getSelectionEnd();
        if (endSelection > 0) {
            String currentText = binding.edtInput.getText().toString();
            String afterDelete;
            if (startSelection != endSelection) {
                afterDelete = currentText.substring(0, startSelection) + currentText.substring(endSelection);
                binding.edtInput.setText(afterDelete);
                binding.edtInput.setSelection(Math.min(startSelection, afterDelete.length()));
            } else {
                afterDelete = currentText.substring(0, endSelection - 1) + currentText.substring(endSelection);
                binding.edtInput.setText(afterDelete);
                binding.edtInput.setSelection(Math.min(endSelection - 1, afterDelete.length()));
            }
        }
//        String content = binding.edtInput.getText().toString();
//        if(content!=null && content.isEmpty() && iListenerTranslate!=null){
//            iListenerTranslate.resultTranslate("");
//        }
    }

    private boolean doNotListenerEdtChange = false;

    public void doneCharacterTranslate() {
        Timber.d("ducNQ : doneCharacterTranslate: ");
        doNotListenerEdtChange = true;
        binding.edtInput.setText("");
    }


    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private IListenerTranslate iListenerTranslate;

    public void setListenerTranslate(IListenerTranslate iListenerTranslate) {
        this.iListenerTranslate = iListenerTranslate;
    }

    private void translate(String content) {
        mDisposable.clear();
        String codeLangInput = binding.tvInputLanguage.getContentDescription().toString();
        String codeLangOutput = binding.tvOutputLanguage.getContentDescription().toString();
        translateText = true;
        if (content == null || content.equals("")) {
            if (iListenerTranslate != null) {
                translateText = false;
                iListenerTranslate.resultTranslate("");
            }
            hideLottie();
            return;
        }
        Objects.requireNonNull(App.getInstance().translateRepository).getTranslate(codeLangInput, codeLangOutput, content).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()).subscribe(
                new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        if (isTranslatable()) {
                            showLottie();
                        }
                        mDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull String s) {
                        hideLottie();
                        int indexFind = s.indexOf(matcherStart);
                        int posStart = indexFind + matcherStart.length();
                        textSearch = "";
                        if (indexFind == -1) {
                            if (iListenerTranslate != null) {
                                iListenerTranslate.resultTranslate(content);
                            }
                            return;
                        }
                        String subStart = s.substring(posStart);
                        int posEnd = subStart.indexOf(matcherEnd);
                        String result = subStart.substring(0, posEnd);
                        if (iListenerTranslate != null && translateText) {
                            iListenerTranslate.resultTranslate(result);
                        }

                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        textSearch = "";
                        binding.groupShowTranslate.setVisibility(GONE);
                        binding.groupTurnOnInternet.setVisibility(VISIBLE);
                        hideLottie();
                        doneCharacterTranslate();
                        checkErrorTrans();
                        Timber.e(e);
                    }
                });
    }

    private final String matcherStart = "<span id=\"tw-answ-target-text\">";
    private final String matcherEnd = "</span>";

//    public void showErrorNetwork() {
//        hideLottie();
//        setVisibility(GONE);
//        doneCharacterTranslate();
//        //if(getVisibility()==GONE){
//        Toast toast = Toast.makeText(App.getInstance(), "Please check the internet", Toast.LENGTH_SHORT);
//        toast.show();
//        //}
//    }

    public void closeTranslate() {
        hideLottie();
        setVisibility(GONE);
        doneCharacterTranslate();
    }

    public void setEnabled() {
    }

    public void startShowTranslate() {
        LocaleUtils.INSTANCE.applyLocale(context);
        setVisibility(VISIBLE);
        setView();
        binding.edtInput.requestFocus();
        binding.edtInput.setHint(R.string.type_to_translate);
        for (String[] item : CommonUtil.getListLanguage()) {
          /*  if (item[0].equals(binding.tvInputLanguage.getContentDescription().toString())) {
                binding.tvInputLanguage.setText(item[1]);
                break;
            }*/
            if (item[0].equals(App.getInstance().mPrefs.getString(CODE_LANGUAGE_IN_PUT,binding.tvInputLanguage.getContentDescription().toString()))) {
                binding.tvInputLanguage.setText(item[1]);
                break;
            }
        }
        for (String[] item : CommonUtil.getListLanguage()) {
           /* if (item[0].equals(binding.tvOutputLanguage.getContentDescription().toString())) {
                binding.tvOutputLanguage.setText(item[1]);
                break;
            }*/
            if (item[0].equals(App.getInstance().mPrefs.getString(CODE_LANGUAGE_OUT_PUT,binding.tvOutputLanguage.getContentDescription().toString()))) {
                binding.tvOutputLanguage.setText(item[1]);
                break;
            }
        }
        binding.tvNoInternet.setText(R.string.pleaseTurnOnInternet);
    }

    public void requestFocusEdittext() {
        binding.edtInput.requestFocus();
    }

    private void setView() {
        if (isSupportLanguageTranslate()) {
            if (isKeyboardNumber) {
                binding.groupShowTranslate.setVisibility(GONE);
                binding.groupTurnOnInternet.setVisibility(GONE);
                binding.groupNoSupport.setVisibility(VISIBLE);
                binding.tvNoSupportThisLanguage.setText(R.string.no_support_number_keyboard);
            } else if (Utils.isOnline(getContext())) {
                binding.edtInput.requestFocus();
                if (DisplayUtils.getScreenWidth() < DisplayUtils.getScreenHeight()) {
                } else {
                    ConstraintLayout.LayoutParams paramsInput = (ConstraintLayout.LayoutParams) binding.tvInputLanguage.getLayoutParams();
                    paramsInput.width = LayoutParams.WRAP_CONTENT;
                    paramsInput.bottomToBottom = R.id.vBackgroundInput;
                    paramsInput.endToStart = R.id.imgChangeLanguage;
                    paramsInput.startToStart = R.id.gdLine;
                    paramsInput.topToTop = R.id.vBackgroundInput;

                    ConstraintLayout.LayoutParams paramsVLine = (ConstraintLayout.LayoutParams) binding.vLineCenter.getLayoutParams();
                    paramsVLine.topToTop = LayoutParams.PARENT_ID;
                    paramsVLine.topToBottom = LayoutParams.UNSET;

                    ConstraintLayout.LayoutParams paramsChange = (ConstraintLayout.LayoutParams) binding.imgChangeLanguage.getLayoutParams();
                    paramsChange.bottomToBottom = R.id.vBackgroundInput;
                    paramsChange.topToTop = R.id.vBackgroundInput;
                    paramsChange.endToEnd = LayoutParams.PARENT_ID;
                    paramsChange.startToStart = R.id.gdLine;

                    ConstraintLayout.LayoutParams paramsClose = (ConstraintLayout.LayoutParams) binding.imgClose.getLayoutParams();
                    paramsClose.bottomToBottom = R.id.vBackgroundInput;
                    paramsClose.endToEnd = R.id.vBackgroundInput;
                    paramsClose.topToTop = R.id.vBackgroundInput;

                    ConstraintLayout.LayoutParams paramsOut = (ConstraintLayout.LayoutParams) binding.tvOutputLanguage.getLayoutParams();
                    paramsOut.width = LayoutParams.WRAP_CONTENT;
                    paramsOut.bottomToBottom = R.id.vBackgroundInput;
                    paramsOut.endToEnd = LayoutParams.PARENT_ID;
                    paramsOut.startToEnd = R.id.imgChangeLanguage;
                    paramsOut.topToTop = R.id.vBackgroundInput;
                    paramsOut.endToStart = LayoutParams.UNSET;
                    paramsOut.startToStart = LayoutParams.UNSET;

                    ConstraintLayout.LayoutParams paramsEdit = (ConstraintLayout.LayoutParams) binding.edtInput.getLayoutParams();
                    paramsEdit.bottomToBottom = R.id.vBackgroundInput;
                    paramsEdit.endToEnd = R.id.gdLine;
                    paramsEdit.startToStart = R.id.vBackgroundInput;
                    paramsEdit.startToEnd = LayoutParams.UNSET;
                    paramsEdit.topToTop = R.id.vBackgroundInput;
                    paramsEdit.topToBottom = LayoutParams.UNSET;

                    ConstraintLayout.LayoutParams paramsBackground = (ConstraintLayout.LayoutParams) binding.vBackgroundInput.getLayoutParams();
                    paramsBackground.bottomToBottom = LayoutParams.PARENT_ID;
                    paramsBackground.endToEnd = R.id.gdLine;
                    paramsBackground.startToEnd = R.id.imgBack;
                    paramsBackground.topToBottom = R.id.vLineCenter;

                    binding.edtInput.setLayoutParams(paramsEdit);
                    binding.edtInput.requestLayout();
                    binding.tvInputLanguage.setLayoutParams(paramsInput);
                    binding.tvInputLanguage.requestLayout();
                    binding.imgChangeLanguage.setLayoutParams(paramsChange);
                    binding.imgChangeLanguage.requestLayout();
                    binding.tvOutputLanguage.setLayoutParams(paramsOut);
                    binding.tvOutputLanguage.requestLayout();
                    binding.vLineCenter.setLayoutParams(paramsVLine);
                    binding.vLineCenter.requestLayout();
                    binding.vBackgroundInput.setLayoutParams(paramsBackground);
                    binding.vBackgroundInput.requestLayout();
                    binding.imgClose.setLayoutParams(paramsClose);
                    binding.imgClose.requestLayout();
                }
                binding.groupShowTranslate.setVisibility(VISIBLE);
                binding.groupTurnOnInternet.setVisibility(GONE);
                binding.groupNoSupport.setVisibility(GONE);
            } else {
                binding.groupShowTranslate.setVisibility(GONE);
                binding.groupTurnOnInternet.setVisibility(VISIBLE);
                binding.groupNoSupport.setVisibility(GONE);
            }
        } else {
            binding.groupShowTranslate.setVisibility(GONE);
            binding.groupTurnOnInternet.setVisibility(GONE);
            binding.groupNoSupport.setVisibility(VISIBLE);
            binding.tvNoSupportThisLanguage.setText(R.string.no_support_this_language_keyboard);
        }
    }

    boolean isKeyboardNumber = false;

    public void setKeyboardNumber(boolean isKeyboardNumber) {
        this.isKeyboardNumber = isKeyboardNumber;
    }

    public boolean isKeyboardNumber() {
        return isKeyboardNumber;
    }

    private void checkErrorTrans() {
        if (!Utils.isOnline(getContext())) {
            binding.groupShowTranslate.setVisibility(GONE);
            binding.groupTurnOnInternet.setVisibility(VISIBLE);
            hideLottie();
            if (iListenerTranslate != null) {
                iListenerTranslate.onChangeLayoutViewTranslate();
            }
        }
    }

    public boolean isTranslatable() {
        return binding.groupShowTranslate.getVisibility() == View.VISIBLE;
    }

    public void setInputLanguage(LanguageTranslate inputLanguage) {
        binding.tvInputLanguage.setContentDescription(App.getInstance().mPrefs.getString(CODE_LANGUAGE_IN_PUT,inputLanguage.getCodeLanguage()));
        //binding.tvInputLanguage.setContentDescription(inputLanguage.getCodeLanguage());
        // App.getInstance().mPrefs.edit().putString("demod", inputLanguage.getNameLanguage()).apply();
        binding.tvInputLanguage.setText(inputLanguage.getNameLanguage());
        translate(binding.edtInput.getText().toString());
        setAllowReverseLanguage();
    }

    public void setAllowReverseLanguage() {
        boolean allowClickReverse = false;
        if (!binding.tvInputLanguage.getContentDescription().equals("")) {
            allowClickReverse = true;
        }
        binding.imgChangeLanguage.setClickable(allowClickReverse);
        binding.imgChangeLanguage.setImageTintList(ContextCompat.getColorStateList(context, allowClickReverse ? R.color.color_28293D : R.color.color_DFDFDF));
    }

    public void setOutputLanguage(LanguageTranslate outputLanguage) {
        binding.tvOutputLanguage.setContentDescription(App.getInstance().mPrefs.getString(CODE_LANGUAGE_OUT_PUT,outputLanguage.getCodeLanguage()));
        binding.tvOutputLanguage.setText(outputLanguage.getNameLanguage());
        translate(binding.edtInput.getText().toString());
        // TranslateOptions.newBuilder().setApiKey()
    }

    private void showLottie() {
        if (!binding.lottie.isAnimating() && binding.lottie.getVisibility() != VISIBLE) {
            binding.lottie.playAnimation();
            binding.lottie.setVisibility(VISIBLE);
        }
    }

    private void hideLottie() {
        binding.lottie.cancelAnimation();
        binding.lottie.setVisibility(GONE);
    }


    //check support
    //vietnamese don't support
    private boolean isSupportLanguageTranslate() {
        Timber.e("");
//        Timber.d("ducNQ : isSupportLanguageTranslate: "+Settings.getLanguageKeyBoardCurrent());
        return !Settings.getLanguageKeyBoardCurrent().equals(Constant.KEYBOARD_LANGUAGE_VN);
    }

    public interface IListenerTranslate {
        void resultTranslate(String output);

        void closeTranslate();

        void showLanguageInput();

        void showLanguageOutput();

        void reverserLanguage();

        void onChangeLayoutViewTranslate();
    }

}
