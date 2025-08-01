package com.android.inputmethod.keyboard.translate;

import static com.tapbi.spark.yokey.util.Constant.CODE_LANGUAGE_IN_PUT;
import static com.tapbi.spark.yokey.util.Constant.CODE_LANGUAGE_OUT_PUT;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.databinding.ViewChooseLanguageBinding;
//import com.google.cloud.translate.v3beta1.Translation;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.common.Constant;
import com.tapbi.spark.yokey.data.model.LanguageTranslate;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.LocaleUtils;
import com.orhanobut.hawk.Hawk;


import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

public class ViewChooseLanguage extends ConstraintLayout {


    private ViewChooseLanguageBinding binding;

    private Context context;
    private ArrayList<LanguageTranslate> listInput = new ArrayList<>();
    private ArrayList<LanguageTranslate> listOutput = new ArrayList<>();
    private ArrayList<LanguageTranslate> listInputRecent = new ArrayList<>();
    private ArrayList<LanguageTranslate> listOutputRecent = new ArrayList<>();

    private LanguageTranslateAdapter inputAdapter;
    private LanguageTranslateAdapter outputAdapter;

    private IListenerSetLanguageTranslate mListenerSetLanguageTranslate;
    private ArrayList<String> listCodeOutputDefault = new ArrayList<>(Collections.singletonList("en"));

    public void setListener(IListenerSetLanguageTranslate mListenerSetLanguageTranslate) {
        this.mListenerSetLanguageTranslate = mListenerSetLanguageTranslate;
        if (mListenerSetLanguageTranslate != null && listInput.size() > 0) {
            mListenerSetLanguageTranslate.onChooseLanguageInput(listInput.get(0));
        }

        if (mListenerSetLanguageTranslate != null && listOutput.size() > 0) {
            mListenerSetLanguageTranslate.onChooseLanguageOutput(listOutput.get(0));
        }
    }

    public ViewChooseLanguage(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ViewChooseLanguage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewChooseLanguage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        this.context = context;
        Hawk.init(context).build();
        backupHawkToSharedPreferences();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = DataBindingUtil.inflate(inflater, R.layout.view_choose_language, this, true);
//        inputAdapter = new LanguageTranslateAdapter()
        binding.container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListenerSetLanguageTranslate != null) {
                    hideView();
                    mListenerSetLanguageTranslate.onCloseChooseLanguage();
                }
            }
        });

        inputAdapter = new LanguageTranslateAdapter(context, listInput, -1, new LanguageTranslateAdapter.ILanguageChooseListener() {
            @Override
            public void onChooseLanguage(int pos, LanguageTranslate languageTranslate) {
                if (mListenerSetLanguageTranslate != null) {
                    Timber.d("ducNQ : onChooseLanguage: 1");
                    hideView();
                    App.getInstance().mPrefs.edit().putString(CODE_LANGUAGE_IN_PUT, languageTranslate.getCodeLanguage()).apply();
                    mListenerSetLanguageTranslate.onChooseLanguageInput(languageTranslate);
                    // App.getInstance().mPrefs.edit().putString("demod", languageTranslate.getNameLanguage()).apply();
                    listInputRecent.remove(languageTranslate);
                    listInputRecent.add(0, languageTranslate);

                    if (listInputRecent.size() > 6) {
                        if (listInputRecent.get(6).getCodeLanguage().equals("")) {
                            listInputRecent.remove(5);
                        } else {
                            listInputRecent.remove(4);
                        }

                    }

                    listInput.removeAll(listInputRecent);
                    listInput.addAll(0, listInputRecent);
                    inputAdapter.changeList(listInput, listInputRecent.size());

                    saveListInputRecent();
                }
            }
        }, com.tapbi.spark.yokey.util.Constant.TYPE_LANGUAGE_IN_PUT);

        outputAdapter = new LanguageTranslateAdapter(context, listOutput, -1, new LanguageTranslateAdapter.ILanguageChooseListener() {
            @Override
            public void onChooseLanguage(int pos, LanguageTranslate languageTranslate) {
                if (mListenerSetLanguageTranslate != null) {
                    hideView();
                    App.getInstance().mPrefs.edit().putString(CODE_LANGUAGE_OUT_PUT, languageTranslate.getCodeLanguage()).apply();
                    mListenerSetLanguageTranslate.onChooseLanguageOutput(languageTranslate);

                    listOutputRecent.remove(languageTranslate);
                    listOutputRecent.add(0, languageTranslate);

                    if (listOutputRecent.size() > 6) {
                        listOutputRecent.remove(6);
                    }

                    listOutput.remove(pos);
                    listOutput.add(0, languageTranslate);
                    outputAdapter.changeList(listOutput, listOutputRecent.size());


                    saveListOutputRecent();
                }
            }
        }, com.tapbi.spark.yokey.util.Constant.TYPE_LANGUAGE_OUT_PUT);
        //Timber.d("ducNQ : onChooseLanguaged: "+App.getInstance().mPrefs.getString("CODE_LANGUAGE","vn"));

        binding.rcvInput.setAdapter(inputAdapter);
        binding.rcvOutput.setAdapter(outputAdapter);

        getListInput();

        getListOutput();

    }

    private void backupHawkToSharedPreferences() {
        try {
            if (App.getInstance().mPrefs.getBoolean(Constant.BACK_UP_HAWK_TO_SHARED_PRE_LANGUAGE_RECENT, true)) {
                ArrayList<String> listCodeInput = Hawk.get(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_INPUT, new ArrayList<>());
                ArrayList<String> listCodeOutput = Hawk.get(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_OUTPUT, listCodeOutputDefault);
                App.getInstance().translateRepository.addListLanguageRecent(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_INPUT, listCodeInput);
                App.getInstance().translateRepository.addListLanguageRecent(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_OUTPUT, listCodeOutput);
                App.getInstance().mPrefs.edit().putBoolean(Constant.BACK_UP_HAWK_TO_SHARED_PRE_LANGUAGE_RECENT, false).apply();
            }
        } catch (Exception e) {
            App.getInstance().mPrefs.edit().putBoolean(Constant.BACK_UP_HAWK_TO_SHARED_PRE_LANGUAGE_RECENT, false).apply();
            Timber.e(e);
        }

    }

    public void showInput() {
        showView(true);
    }

    public void showOutput() {
        showView(false);
    }


    private void showView(boolean isTypeInput) {
        LocaleUtils.INSTANCE.applyLocale(context);
        binding.cvInput.setVisibility(isTypeInput ? VISIBLE : GONE);
        binding.cvOutput.setVisibility(isTypeInput ? GONE : VISIBLE);
        if (isTypeInput) {
            binding.rcvInput.scrollToPosition(0);
        } else {
            binding.rcvOutput.scrollToPosition(0);
        }
        setVisibility(VISIBLE);
    }

    public void hideView() {
        setVisibility(GONE);
    }


    public void getListInput() {
        listInputRecent.clear();
        listInput.clear();
        ArrayList<String> listCode = App.getInstance().translateRepository.getListLanguageRecent(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_INPUT,
                new ArrayList<>());
        for (int i = 5; i <= listCode.size() - 1; i++) {
            listCode.remove(i);
        }
        listCode.add("");
        for (String[] item : CommonUtil.getListLanguage()) {
            LanguageTranslate lg = new LanguageTranslate(item[0], item[1]);
            listInput.add(lg);
        }


        for (String code : listCode) {
            for (LanguageTranslate item : listInput) {
                if (code.equals(item.getCodeLanguage())) {
                    listInputRecent.add(item);
                    break;
                }
            }
        }

        listInput.removeAll(listInputRecent);
        listInput.addAll(0, listInputRecent);
        for (int i = 0; i < listInput.size(); i++) {
            if (listInput.get(i).getCodeLanguage().contains(App.getInstance().mPrefs.getString(CODE_LANGUAGE_IN_PUT, ""))) {
                listInput.add(0, listInput.get(i));
                if (i + 1 < listInput.size()) {
                    listInput.remove(i + 1);
                }
                break;
            }
        }
        inputAdapter.changeList(listInput, listInputRecent.size());
//        Timber.e("mListenerSetLanguageTranslate: " + mListenerSetLanguageTranslate);
    }

    public void getListOutput() {
        listOutputRecent.clear();
        listOutput.clear();
        ArrayList<String> listCode = App.getInstance().translateRepository.getListLanguageRecent(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_OUTPUT,
                listCodeOutputDefault);
        for (int i = 6; i <= listCode.size() - 1; i++) {
            listCode.remove(i);
            // break;
        }

        for (String[] item : CommonUtil.getListLanguage()) {
            if (item[0].equals("")) {
                continue;
            }
            LanguageTranslate lg = new LanguageTranslate(item[0], item[1]);
            listOutput.add(lg);
        }

        for (String code : listCode) {
            for (LanguageTranslate item : listOutput) {
                if (item.getCodeLanguage().equals("")) {
                    continue;
                }
                if (code.equals(item.getCodeLanguage())) {
                    listOutputRecent.add(item);
                }
            }
        }
        listOutput.removeAll(listOutputRecent);
        listOutput.addAll(0, listOutputRecent);
        for (int i = 0; i < listOutput.size(); i++) {
            if (listOutput.get(i).getCodeLanguage().contains(App.getInstance().mPrefs.getString(CODE_LANGUAGE_OUT_PUT, ""))) {
                listOutput.add(0, listOutput.get(i));
                if (i + 1 < listOutput.size()) {
                    listOutput.remove(i + 1);
                }
                break;
            }
        }
        outputAdapter.changeList(listOutput, listOutputRecent.size());
    }


    private void saveListInputRecent() {
        ArrayList<String> listSave = new ArrayList<>();
        for (LanguageTranslate item : listInputRecent) {
            if (item.getCodeLanguage().equals("")) {
                continue;
            }
            listSave.add(item.getCodeLanguage());
        }
        App.getInstance().translateRepository.addListLanguageRecent(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_INPUT, listSave);
    }

    private void saveListOutputRecent() {
        ArrayList<String> listSave = new ArrayList<>();
        for (LanguageTranslate item : listOutputRecent) {
            if (item.getCodeLanguage().equals("")) {
                continue;
            }
            listSave.add(item.getCodeLanguage());
        }
        App.getInstance().translateRepository.addListLanguageRecent(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_OUTPUT, listSave);
    }

    public void reverseLanguage() {
        ArrayList<String> listCodeRecentInputOld = App.getInstance().translateRepository.getListLanguageRecent(
                Constant.KEY_LANGUAGE_TRANSLATE_RECENT_INPUT, new ArrayList<>());
        ArrayList<String> listCodeRecentOutputOld = App.getInstance().translateRepository.getListLanguageRecent(
                Constant.KEY_LANGUAGE_TRANSLATE_RECENT_OUTPUT, listCodeOutputDefault);
        App.getInstance().translateRepository.addListLanguageRecent(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_INPUT, listCodeRecentOutputOld);
        App.getInstance().translateRepository.addListLanguageRecent(Constant.KEY_LANGUAGE_TRANSLATE_RECENT_OUTPUT, listCodeRecentInputOld);
        getListInput();
        getListOutput();
    }


    public interface IListenerSetLanguageTranslate {
        void onChooseLanguageInput(LanguageTranslate languageTranslate);

        void onChooseLanguageOutput(LanguageTranslate languageTranslate);

        void onCloseChooseLanguage();
    }


}
