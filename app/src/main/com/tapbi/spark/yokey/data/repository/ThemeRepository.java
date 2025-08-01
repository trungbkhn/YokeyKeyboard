package com.tapbi.spark.yokey.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.android.inputmethod.keyboard.KeyboardSwitcher;
import com.android.inputmethod.latin.AudioAndHapticFeedbackManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.common.CommonVariable;
import com.tapbi.spark.yokey.data.local.db.ThemeDB;
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity;
import com.tapbi.spark.yokey.data.model.PaginationUpdate;
import com.tapbi.spark.yokey.data.model.ThemeObject;
import com.tapbi.spark.yokey.data.model.ThemeObjectList;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.data.remote.ApiUtils;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ThemeRepository {
    public final HashMap<String, Drawable> mapKeyDrawablePreview = new HashMap<>();
    private final HashMap<String, Drawable> mapKeyDrawable = new HashMap<>();
    private final HashMap<String, Drawable> mapKeyDrawableDefault = new HashMap<>();
    public ArrayList<ThemeObject> listThemeBackgroundNew = new ArrayList<>();
    public ArrayList<ThemeObject> listThemeColorNew = new ArrayList<>();
    public ArrayList<ThemeObject> listThemeFeaturedNew = new ArrayList<>();
    private ThemeModel currentThemeModel;
    private ThemeEntity currentThemeEntity;
    private ThemeModel defaultThemeModel;
    private Context context;
    private SharedPreferences mPrefs;
    private Bitmap bmBackground;
    private ThemeDB themeDB = ThemeDB.getInstance(App.getInstance());
    private KeyboardSwitcher keyboardSwitcher = KeyboardSwitcher.getInstance();
    private ArrayList<ThemeObject> listThemeTryKeyboard = new ArrayList<>();

    @Inject
    public ThemeRepository() {
        this.context = App.getInstance();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void preloadData() {
        updateCurrentThemeModel();
        loadThemeDefault();
        getThemeNew();
    }

    public void getThemeNew() {
        Single.fromCallable(() -> {
                    Gson gson = new Gson();
                    loadThemeListFromJson("Background", listThemeBackgroundNew, gson);
                    loadThemeListFromJson("Color", listThemeColorNew, gson);
                    loadThemeListFromJson("Gradient", listThemeColorNew, gson);
                    loadThemeListFromJson("Featured", listThemeFeaturedNew, gson);
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull Boolean aBoolean) {
                        Timber.e("hachung onSuccess:" + aBoolean);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e("hachung getThemeBackgroundNew error: %s", e.getMessage());
                    }
                });
    }

    private void loadThemeListFromJson(String key, List<ThemeObject> targetList, Gson gson) {
        JSONArray jsonArray = CommonUtil.getDataAssetLocal(App.getInstance(), "theme_phase_12.json", key);
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    ThemeObject obj = gson.fromJson(jsonArray.getJSONObject(i).toString(), ThemeObject.class);
                    targetList.add(obj);
                } catch (JSONException e) {
                    Timber.e("Error parsing %s item at index %d: %s", key, i, e.getMessage());
                }
            }
        }
    }

    private void addThemeTryKeyboard(List<ThemeObject> themeObjects) {
        if (listThemeTryKeyboard.size() == 0 && themeObjects.size() > 0) {
            listThemeTryKeyboard.addAll(themeObjects);
        } else if (listThemeTryKeyboard.size() > 0 && themeObjects.size() > 0) {
            for (ThemeObject theme : themeObjects) {
                boolean isExist = false;
                for (ThemeObject themeOb : listThemeTryKeyboard) {
                    if (themeOb.getId() == theme.getId()) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    listThemeTryKeyboard.add(theme);
                }
            }
        }
    }

    public void addThemeTryKeyboardThread(List<ThemeObject> themeObjects) {
        Single.fromCallable(() -> {
            addThemeTryKeyboard(themeObjects);
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {

            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e("Duongcv " + e.getMessage());
            }
        });
    }

    public ArrayList<ThemeObject> getListThemeTryKeyboard() {
        return listThemeTryKeyboard;
    }

    public void loadThemeDefault() {
        loadThemeThread(Constant.ID_THEME_DEFAULT).subscribe(new SingleObserver<ThemeModel>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull ThemeModel themeModel) {
                defaultThemeModel = themeModel;
                //  App.getInstance().themeModel = themeModel;
                App.getInstance().themeModelSound = themeModel;
                mapKeyDrawableDefault.clear();
                loadDrawableKey(defaultThemeModel);
                // Timber.d("onSuccess dataed");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                // Timber.d("onError dataed");
            }
        });
    }

    public void updateCurrentThemeModel() {
        String idTheme = !mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "0").equals("6023") ? mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "0") : "0";

        Timber.d("duongcv update theme " + idTheme);
        loadThemeThread(idTheme).subscribe(new SingleObserver<ThemeModel>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                //  Timber.d("updateCurrentThemeModel loading");
            }

            @Override
            public void onSuccess(@NonNull ThemeModel themeModel) {
                //  Timber.d("updateCurrentThemeModel success");
                currentThemeModel = themeModel;
                Timber.d("duongcv update theme success");
                App.getInstance().themeModel = themeModel;
                App.getInstance().themeModelSound = themeModel;
                updateBackgroundKeyboard();
                //  App.getInstance().idTheme=themeModel.getId();
                // App.getInstance().colorCurrent=CommonUtil.hex2decimal(themeModel.getMenuBar().getIconColor());
                mapKeyDrawable.clear();
                loadDrawableKey(currentThemeModel);
                AudioAndHapticFeedbackManager.getInstance().loadSound();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d("duongcv update theme false");
                //   Timber.d("updateCurrentThemeModel fail");
            }
        });
    }

    private void updateBackgroundKeyboard() {
        if (currentThemeModel != null && currentThemeModel.getBackground() != null && currentThemeModel.getBackground().getBackgroundImage() != null && !currentThemeModel.getBackground().getBackgroundImage().equals("0")) {
            File file = App.getInstance().appDir;
            String strPathIconKeyText = file.toString() + "/" + currentThemeModel.getId() + "/" + currentThemeModel.getBackground().getBackgroundImage();
            if ((currentThemeModel.getId() != null && Long.parseLong(currentThemeModel.getId()) > 4012 && Long.parseLong(currentThemeModel.getId()) < 5000)
                    || (currentThemeModel.getId() != null && Long.parseLong(currentThemeModel.getId()) > 6010 && Long.parseLong(currentThemeModel.getId()) < 6030)
                    || (currentThemeModel.getId() != null && Long.parseLong(currentThemeModel.getId()) > 3015 && Long.parseLong(currentThemeModel.getId()) < 4000)
            ) {
                strPathIconKeyText = Constant.FOLDER_ASSET + "themes/" + currentThemeModel.getId() + "/" + currentThemeModel.getBackground().getBackgroundImage();
            } else if (currentThemeModel.getId() != null && Long.parseLong(currentThemeModel.getId()) > 6000){
                strPathIconKeyText = currentThemeModel.getBackground().getBackgroundImage(); //1000000
            }


            try {
                Timber.e("hachung strPathIconKeyText:" + strPathIconKeyText);
                Glide.with(App.getInstance()).asBitmap().load(strPathIconKeyText).override(700).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@androidx.annotation.NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bmBackground = resource;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public ThemeModel getCurrentThemeModel() {
        if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
            if (defaultThemeModel == null) loadThemeDefault();
            Timber.e("duongcv theme default " + defaultThemeModel.getId());
            return defaultThemeModel;
        } else {
            if (currentThemeModel == null) updateCurrentThemeModel();
            try {
                return currentThemeModel != null ? currentThemeModel.copy() : currentThemeModel;
            } catch (OutOfMemoryError error) {
                return currentThemeModel;
            }
        }
    }

    public ThemeModel getDefaultThemeModel() {
        if (defaultThemeModel == null) loadThemeDefault();
        return defaultThemeModel;
    }

    public Bitmap getBitmapBackground() {
        return bmBackground;
    }

    private Single<ThemeModel> loadThemeThread(String id) {
        return Single.fromCallable(new Callable<ThemeModel>() {
            @Override
            public ThemeModel call() throws Exception {
                // TODO: chungvv update local
                if (Long.parseLong(id) > 6000 && Long.parseLong(id) < 6011) {
                    return loadThemeAssetFeatured(id, "ThemeFeatured");
                } else if (Long.parseLong(id) > 4012 && Long.parseLong(id) < 5000
                        || Long.parseLong(id) > 6010 && Long.parseLong(id) < 6030
                        || Long.parseLong(id) > 3015 && Long.parseLong(id) < 4000
                        || Long.parseLong(id) > 2003 && Long.parseLong(id) < 3000
                ) {
                    return CommonUtil.getDataAssetThemeLocal(context, id);
                }
                return CommonUtil.parserJsonFromFileTheme(context, id);

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private ThemeModel loadThemeAssetFeatured(String id, String nameObject) throws JSONException {
        ThemeModel key = new ThemeModel();
        String folder = "key/" + id + "/theme.json";
        JSONArray jsonArray = CommonUtil.getDataAssetLocal(
                App.getInstance(),
                folder,
                nameObject
        );
        if (jsonArray != null) {
            ArrayList<ThemeModel> arrayList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                Gson gson = new Gson();
                key = gson.fromJson(jsonArray.get(i).toString(), ThemeModel.class);
                arrayList.add(key);
            }
        }
        return key;
    }

    private Single<ThemeModel> loadThemeFromAsset(String id) {
        return Single.fromCallable(new Callable<ThemeModel>() {
            @Override
            public ThemeModel call() throws Exception {
                ThemeModel key = new ThemeModel();
                String folder = "key/" + id + "/theme.json";
                JSONArray jsonArray = CommonUtil.getDataAssetLocal(
                        App.getInstance(),
                        folder,
                        "ThemeFeatured"
                );
                if (jsonArray != null) {
                    ArrayList<ThemeModel> arrayList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Gson gson = new Gson();
                        key = gson.fromJson(jsonArray.get(i).toString(), ThemeModel.class);
                        arrayList.add(key);
                    }
                }
                return key;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> downloadAndSaveTheme(String themeId, ResponseBody body) {
        return Single.fromCallable(() -> {
            String fileName = themeId + ".zip";
            return CommonUtil.saveThemeToInternalStorage(App.getInstance(), body, fileName, themeId, false);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void loadDrawableKey(ThemeModel themeModel) {
        if (themeModel != null && themeModel.getId() != null) {
            Completable.fromAction(() -> {
                mapKeyDrawable.clear();
                if (themeModel.getKey() != null && themeModel.getKey().getText() != null) {
                    boolean isScaleKey = themeModel.getKey().getText().getScaleKey() == null || !themeModel.getKey().getText().getScaleKey().equals("false");
                    addDrawableFromState(themeModel, themeModel.getKey().getText().getNormal(), isScaleKey);
                    addDrawableFromState(themeModel, themeModel.getKey().getText().getPressed(), isScaleKey);
                }
                if (themeModel.getKey().getSpecial() != null) {
                    boolean isScaleKeySpecial = themeModel.getKey().getSpecial().getScaleKey() == null || !themeModel.getKey().getSpecial().getScaleKey().equals("false");
                    addDrawableFromState(themeModel, themeModel.getKey().getSpecial().getNormal(), isScaleKeySpecial);
                    addDrawableFromState(themeModel, themeModel.getKey().getSpecial().getPressed(), isScaleKeySpecial);
                    addDrawableFromState(themeModel, themeModel.getKey().getSpecial().getNormal(), isScaleKeySpecial);
                }
                if (themeModel.getPopup() != null && themeModel.getPopup().getMinKeyboard() != null) {
                    addDrawableFromState(themeModel, themeModel.getPopup().getMinKeyboard().getBgImage(), false);
                }
            }).doOnError(throwable -> {
                throwable.printStackTrace();
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }


    private void addDrawableFromState(ThemeModel themeModel, String state, boolean isScale) {
        String strPathIconKeyText = CommonUtil.getPathImage(App.getInstance(), themeModel, null, state);
        Drawable drawable = CommonUtil.getImage9PathFromLocal(App.getInstance(), strPathIconKeyText, isScale);
        if (themeModel.getId().equals(Constant.ID_THEME_DEFAULT)) {
            mapKeyDrawableDefault.put(strPathIconKeyText, drawable);
        } else {
            mapKeyDrawable.put(strPathIconKeyText, drawable);
        }
    }

    public Drawable getDrawableKey(String path, boolean isScale) {
        if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
            if (defaultThemeModel != null && mapKeyDrawableDefault.get(path) != null)
                return mapKeyDrawableDefault.get(path);
            else {
                Drawable drawable = CommonUtil.getImage9PathFromLocal(App.getInstance(), path, isScale);
                mapKeyDrawableDefault.put(path, drawable);
                return drawable;
            }
        } else {
            if (currentThemeModel != null && mapKeyDrawable.get(path) != null) {
                return mapKeyDrawable.get(path);
            } else {
                Drawable drawable = CommonUtil.getImage9PathFromLocal(App.getInstance(), path, isScale);
                mapKeyDrawable.put(path, drawable);
                return drawable;
            }
        }
    }

    public void clearDrawableKeyDefault() {
        mapKeyDrawableDefault.clear();
    }

    public boolean createThemeDataFolder(ThemeModel themeModel) throws IOException {
        File file = new File(App.getInstance().appDir, themeModel.getId() + "/theme.json");
        //App.getInstance().nameFolder = Objects.requireNonNull(themeModel.getId());
        try {
            if (!Objects.requireNonNull(file.getParentFile()).exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Writer writer = new FileWriter(file.getAbsolutePath());
            Gson gson = new GsonBuilder().create();
            gson.toJson(themeModel, writer);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void checkUpdateTheme(int lastVersion) {
        ApiUtils.checkUpdateThemeService().checkUpdateTheme(new PaginationUpdate(lastVersion)).enqueue(new Callback<ThemeObjectList>() {
            @Override
            public void onResponse(Call<ThemeObjectList> call, Response<ThemeObjectList> response) {
                if (response.body() != null && response.body().getLastVersion() != null) {
                    int lastVersion = response.body().getLastVersion();
                    if (lastVersion > (App.getInstance().mPrefs != null ? App.getInstance().mPrefs.getInt(com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, 1) : 1)) {
                        if (response.body().getItems() != null && !response.body().getItems().isEmpty()) {
                            addAllThemeDB(response.body().getItems()).doOnError(throwable -> {
                                Timber.e("Duongcv " + throwable.getMessage());
                            }).subscribe();
                        }
                        App.getInstance().mPrefs.edit().putInt(com.tapbi.spark.yokey.common.Constant.THEME_LAST_VERSION, lastVersion).apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<ThemeObjectList> call, Throwable t) {

            }
        });

    }

    public Single<ThemeEntity> getThemeEntity(String idTheme) {
        return Single.fromCallable(() -> {
            return themeDB.themeDAO().fetchOneThemeByIdTheme(idTheme);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public ThemeEntity convertThemeObjectToThemeEntity(ThemeObject themeObject, int isMyTheme) {
        ThemeEntity themeEntity = new ThemeEntity();
        themeEntity.setName(themeObject.getName());
        themeEntity.setId(themeObject.getId().toString());
        themeEntity.setPurchase("1");
        themeEntity.isMyTheme = isMyTheme;
        themeEntity.setPreview(themeObject.getPreview());
        themeEntity.setUrlTheme(themeObject.getUrlTheme());
        themeEntity.setDownloadCount(themeObject.getDownloadCount());
        themeEntity.setTypeKeyboard(themeObject.getTypeKeyboard());
        themeEntity.setUrlCoverTopTheme(themeObject.getUrlCoverTopTheme());
        themeEntity.isHotTheme = themeObject.isHotTheme() == null ? "0" : themeObject.isHotTheme().toString();
        themeEntity.isTopTheme = themeObject.getIdCategory() == null ? "1000" : themeObject.getIdCategory().toString();
        return themeEntity;
    }

    public ThemeObject convertThemeEntityToThemeObject(ThemeEntity themeEntity) {
        ThemeObject themeObject = new ThemeObject();
        themeObject.setName(themeEntity.getName());
        themeObject.setId(Long.valueOf(themeEntity.getId()));
        themeObject.setPurchase(true);
        themeObject.setPreview(themeEntity.getPreview());
        themeObject.setUrlTheme(themeEntity.getUrlTheme());
        themeObject.setDownloadCount(themeEntity.getDownloadCount());
        themeObject.setTypeKeyboard(themeEntity.getTypeKeyboard());
        themeObject.setUrlCoverTopTheme(themeEntity.getUrlCoverTopTheme());
        themeObject.setHotTheme(themeEntity.isHotTheme == null ? 0 : Integer.valueOf(themeEntity.isHotTheme));
        if (themeEntity.isTopTheme == null) {
            if (themeEntity.getTypeKeyboard().equals("RGB")) {
                themeObject.setIdCategory(com.tapbi.spark.yokey.common.Constant.ID_THEME_LED);
            } else if (themeEntity.getTypeKeyboard().equals("Color")) {
                themeObject.setIdCategory(com.tapbi.spark.yokey.common.Constant.ID_THEME_COLOR);
            } else if (themeEntity.getTypeKeyboard().equals("Background")) {
                themeObject.setIdCategory(com.tapbi.spark.yokey.common.Constant.ID_THEME_BACKGROUND);
            } else {
                themeObject.setIdCategory(com.tapbi.spark.yokey.common.Constant.ID_THEME_GRADIENT);
            }
        } else {
            themeObject.setIdCategory(Integer.valueOf(themeEntity.isTopTheme));
        }

        return themeObject;
    }

    public Single<Boolean> updateThemeDB(ThemeObject themeModel, int isMyTheme) {
        return Single.fromCallable(() -> {
            if (themeDB.themeDAO().fetchOneThemeByIdTheme(themeModel.getId().toString()) != null) {
                if (themeDB.themeDAO().fetchOneThemeByIdTheme(themeModel.getId().toString()).getId().equalsIgnoreCase(themeModel.getId().toString())) {
                    themeDB.themeDAO().updateTheme(convertThemeObjectToThemeEntity(themeModel, isMyTheme));
                }
            } else {
                if (isMyTheme == 1) {
                    themeDB.themeDAO().insertTheme(convertThemeObjectToThemeEntity(themeModel, isMyTheme));
                }
            }
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> addAllThemeDB(List<ThemeObject> listThemeObject) {
        return Single.fromCallable(() -> {
            ArrayList<ThemeEntity> themeEntities = new ArrayList<>();
            for (ThemeObject themeObject : listThemeObject) {
                themeEntities.add(convertThemeObjectToThemeEntity(themeObject, 0));
            }
            themeDB.themeDAO().insertAll(themeEntities);
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> updateThemeEntity(ThemeEntity themeEntity, int isMyTheme) {
        return Single.fromCallable(() -> {
            if (themeDB.themeDAO().fetchOneThemeByIdTheme(themeEntity.getId()) != null) {
                if (themeDB.themeDAO().fetchOneThemeByIdTheme(themeEntity.getId()).getId().equalsIgnoreCase(themeEntity.getId())) {
                    ThemeEntity themeEntity1 = new ThemeEntity();
                    themeEntity1.setName(themeEntity.getName());
                    themeEntity1.setId(themeEntity.getId());
                    themeEntity1.setPurchase("1");
                    themeEntity1.isMyTheme = isMyTheme;
                    themeEntity1.setPreview(themeEntity.getPreview());
                    themeDB.themeDAO().updateTheme(themeEntity1);
                }
            } else {
                if (isMyTheme == 1) {
                    ThemeEntity themeEntity1 = new ThemeEntity();
                    themeEntity1.setId(themeEntity.getId());
                    themeEntity1.setName(themeEntity.getName());
                    themeEntity1.setPurchase("1");
                    themeEntity1.isMyTheme = isMyTheme;
                    themeEntity1.setPreview(themeEntity.getPreview());
                    themeDB.themeDAO().insertTheme(themeEntity1);
                }
            }
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<ThemeEntity>> loadAllThemeByIsMyTheme(int isMyTheme) {
        return Single.fromCallable(() -> themeDB.themeDAO().fetchListThemeByIsMyTheme(isMyTheme)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<ThemeEntity>> loadAllThemeByIsHotTheme(int isHotTheme) {
        return Single.fromCallable(() -> themeDB.themeDAO().fetchListThemeByIsHotTheme(isHotTheme)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<ThemeEntity>> loadAllThemeByTypeKeyboard(String typeKeyboard) {
        return Single.fromCallable(() -> themeDB.themeDAO().fetchListThemeByTypeKeyboard(typeKeyboard)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<ThemeEntity>> loadAllThemeByCategory(String idCategory) {
        return Single.fromCallable(() -> themeDB.themeDAO().fetchListThemeByIdCategory(idCategory)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> deleteItemMyTheme(ThemeEntity themeEntity) {
        return Single.fromCallable(() -> deleteItem(themeEntity)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private boolean deleteItem(ThemeEntity themeEntity) {
        try {
            Objects.requireNonNull(themeDB.themeDAO()).deleteTheme(themeEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @androidx.annotation.NonNull
    public Single<String> getUri(Bitmap bitmap, String folder, boolean checkType) {
        return Single.fromCallable(() -> getPath(bitmap, folder, checkType)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public String getPath(Bitmap bitmap, String folder, boolean checkType) {
        return CommonUtil.saveBackgroundKeyboard(bitmap, folder, checkType);
    }


}
