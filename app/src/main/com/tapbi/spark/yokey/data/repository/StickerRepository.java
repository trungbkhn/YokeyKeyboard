package com.tapbi.spark.yokey.data.repository;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.common.LiveEvent;
import com.tapbi.spark.yokey.data.local.dao.StickerRecentDAO;
import com.tapbi.spark.yokey.data.local.entity.Emoji;
import com.tapbi.spark.yokey.data.local.entity.Sticker;
import com.tapbi.spark.yokey.data.local.db.ThemeDB;
import com.tapbi.spark.yokey.data.local.entity.StickerRecent;
import com.tapbi.spark.yokey.data.model.ListSticker;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.data.model.PaginationObj;
import com.tapbi.spark.yokey.data.model.PaginationUpdate;
import com.tapbi.spark.yokey.data.model.StickerOnKeyboard;
import com.tapbi.spark.yokey.data.remote.ApiUtils;
import com.tapbi.spark.yokey.data.remote.StickerService;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class StickerRepository {
    private StickerService stickerService;
    private StickerService stickerServiceDownload;
    public MutableLiveData<List<Sticker>> listMutableLiveDataTikTok = new MutableLiveData<>();
    public MutableLiveData<List<Sticker>> listMutableLiveDataOther = new MutableLiveData<>();
    public MutableLiveData<List<Sticker>> listMutableLiveDataAnimal = new MutableLiveData<>();
    public MutableLiveData<List<Sticker>> listStickerMutableLiveDataTikTok = new MutableLiveData<>();
    public MutableLiveData<List<Sticker>> listStickerMutableLiveDataAnimal = new MutableLiveData<>();
    public LiveEvent<Boolean> resultDownload = new LiveEvent<>();
    private File destinationFile;
    public ArrayList<StickerOnKeyboard> stickerOnKeyboards = new ArrayList<>();
    public int idShowResultDownload = 0;
    private int idCategoryLoad = -1;
    private int sortKeyLoad = -1;
    public ArrayList<String> arrayListCategoryGif = new ArrayList<>();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private ArrayList<Sticker> listStickerTryKeyboard =  new ArrayList<>();
    @Inject
    public StickerRepository() {
        stickerService = ApiUtils.getStickerService();
        stickerServiceDownload = ApiUtils.downloadZipStickerService();
        ContextWrapper contextWrapper = new ContextWrapper(App.getInstance());
        destinationFile = contextWrapper.getDir(App.getInstance().getFilesDir().getName(), Context.MODE_PRIVATE);
       // updateListStickerOnkeyboard();
       // loadStickerRecent();
    }

    public void updateListStickerOnkeyboard() {
        Single.fromCallable(() -> ThemeDB.getInstance(App.getInstance()).stickerDAO().getAllStickerIsDownload(1)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<List<Sticker>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull List<Sticker> stickers) {
                if (stickers.size() > 0) {
                    Timber.d("ducNQ : onSuccessed: ");
                    stickerOnKeyboards.clear();
                    stickerOnKeyboards.add(0, new StickerOnKeyboard(new ArrayList<>(), ""));
                    for (Sticker sticker : stickers) {
                        ArrayList<String> listSticker = new ArrayList<>();
                        String thumb = null;
                        File folderSticker = new File(destinationFile, Constant.FOLDER_STICKER + sticker.getId() + "/" + sticker.getId() + "/" + sticker.getId());
                        if (folderSticker.exists()) {
                            String[] paths = folderSticker.list();
                            if (paths != null) {
                                for (String path : paths) {
                                    if (path.endsWith(".png"))
                                        listSticker.add(folderSticker.getAbsolutePath() + "/" + path);
                                }
                            }
                        }
                        File fileThumb = new File(destinationFile, Constant.FOLDER_STICKER + sticker.getId() + "/" + sticker.getId() + "/thumb.png");
                        if (fileThumb.exists()) thumb = fileThumb.getAbsolutePath();
                        if (thumb != null && listSticker.size() > 0)
                            stickerOnKeyboards.add(new StickerOnKeyboard(listSticker, thumb));
                    }
                }
                Timber.d("ducNQ : preloadData: "+stickerOnKeyboards.size());
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }

    private void addStickerTryKeyboard(List<Sticker> stickers){
        if (listStickerTryKeyboard.size() == 0 && stickers.size() > 0){
            listStickerTryKeyboard.addAll(stickers);
        }else if (listStickerTryKeyboard.size() > 0 && stickers.size() > 0){
            for (Sticker sticker : stickers) {
                boolean isExist = false;
                for (Sticker st : listStickerTryKeyboard) {
                    if (sticker.getId() == st.getId()){
                        isExist = true;
                        break;
                    }
                }
                if (!isExist){
                    listStickerTryKeyboard.add(sticker);
                }
            }
        }
    }

    public void addStickerTryKeyboardThread(List<Sticker> stickers){
        Single.fromCallable(() -> {
            addStickerTryKeyboard(stickers);
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

    public ArrayList<Sticker> getListStickerTryKeyboard() {
        return listStickerTryKeyboard;
    }

    private Single<List<StickerRecent>> getStickerRecent() {
       return Single.fromCallable(() -> {
            ThemeDB themeDB = ThemeDB.getInstance(App.getInstance());
            StickerRecentDAO stickerRecentDAO = null;
            List<StickerRecent> list = null;
            if (themeDB != null) stickerRecentDAO = themeDB.stickerRecentDAO();
            return stickerRecentDAO.getAllStickerRecent();

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Sticker>> getAllStickerByCategory(int idCategory) {
       return Single.fromCallable(() -> {
            ThemeDB themeDB = ThemeDB.getInstance(App.getInstance());
            return themeDB.stickerDAO().getAllStickerByCategory(idCategory);

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void loadStickerRecent() {
        getStickerRecent().subscribe(new SingleObserver<List<StickerRecent>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull List<StickerRecent> stickerRecents) {
                if (stickerRecents.size() > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(Constant.DATA_STICKER_RECENT, (ArrayList<? extends Parcelable>) stickerRecents);
                    EventBus.getDefault().postSticky(new MessageEvent(Constant.EVENT_DATA_STICKER_RECENT, bundle));
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });

    }

    public void loadData(int sortKey, int idCategory, ArrayList<Sticker> listSticker, Context context) {
        if (this.sortKeyLoad != sortKey && idCategoryLoad != idCategory) {
            sortKeyLoad = sortKey;
            idCategoryLoad = idCategory;
            int lastVersion = 1;
            if (App.getInstance().mPrefs != null) {
                lastVersion = App.getInstance().mPrefs.getInt(com.tapbi.spark.yokey.common.Constant.STICKER_LAST_VERSION, 1);
            }
            stickerService.getListSticker(new PaginationObj(sortKey, idCategory, sortKey, lastVersion)).enqueue(new Callback<ListSticker>() {
                @Override
                public void onResponse(Call<ListSticker> call, Response<ListSticker> response) {
                    refeshLoadData();
                    if (response.body() != null && response.body().getStickerList() != null) {
                        listSticker.addAll(response.body().getStickerList());
                        switch (idCategory){
                            case 2000:
                                listMutableLiveDataTikTok.postValue(listSticker);
                                break;
                            case 1000:
                                listMutableLiveDataAnimal.postValue(listSticker);
                                break;
                            case 3000:
                                listMutableLiveDataOther.postValue(listSticker);
                        }
                        for (Sticker sticker : listSticker) {
                            if (CommonUtil.checkStickerExist(sticker)){
                                sticker.setDownload(1);
                            }else {
                                sticker.setDownload(0);
                            }
                        }
                        mCompositeDisposable.clear();
                        insertStickersDatabase(context, listSticker).subscribe(new SingleObserver<Boolean>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                mCompositeDisposable.add(d);
                            }

                            @Override
                            public void onSuccess(@NonNull Boolean aBoolean) {
                                Timber.e("Duongcv insert sticker"  + aBoolean);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                Timber.e("Duongcv insert sticker " + e.getMessage());
                            }
                        });
//                        if (idCategory == 2000) listMutableLiveDataTikTok.postValue(listSticker);
//                        else listMutableLiveDataAnimal.postValue(listSticker);
                        if (response.body().getLastEvaluatedKey() != null) {
                            if (response.body().getLastEvaluatedKey().getSortKey() > 0)
                                loadData(response.body().getLastEvaluatedKey().getSortKey(), idCategory, listSticker, context);
                        }else {
                            App.getInstance().mPrefs.edit().putBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_STICKER.concat(String.valueOf(idCategory)),true).apply();
                            try {
                                if (response.body().getLastVersion() != null) {
                                    int lastVersion = response.body().getLastVersion();
                                    if (lastVersion > (App.getInstance().mPrefs != null ? App.getInstance().mPrefs.getInt(com.tapbi.spark.yokey.common.Constant.STICKER_LAST_VERSION, 1) : 1)) {
                                        App.getInstance().mPrefs.edit().putInt(com.tapbi.spark.yokey.common.Constant.STICKER_LAST_VERSION, lastVersion).apply();
                                    }
                                }
                            }catch (NullPointerException e){}
                        }
                    }
                }

                @Override
                public void onFailure(Call<ListSticker> call, Throwable t) {
                    Log.d("duongcv", "onFailure: ");
                    refeshLoadData();
                    if (App.getInstance().getConnectivityStatus() != -1)
                        CommonUtil.customToast(context, context.getResources().getString(R.string.text_check_internet_sticker));
                }
            });
        }
    }

    public void checkUpdateSticker(int lastVersion, Context context) {
        Log.d("duongcv", "checkUpdateSticker: ");
        ApiUtils.checkUpdateStickerService().checkUpdateSticker(new PaginationUpdate(lastVersion)).enqueue(new Callback<ListSticker>() {
                @Override
                public void onResponse(Call<ListSticker> call, Response<ListSticker> response) {
                    if (response.body() != null &&  response.body().getLastVersion() != null) {
                        int lastVersion = response.body().getLastVersion();
                        if (lastVersion > (App.getInstance().mPrefs != null ? App.getInstance().mPrefs.getInt(com.tapbi.spark.yokey.common.Constant.STICKER_LAST_VERSION, 1) : 1)) {
                            if (response.body().getStickerList() != null && !response.body().getStickerList().isEmpty()) {
                                mCompositeDisposable.clear();
                                insertStickersDatabase(context, response.body().getStickerList()).subscribe(new SingleObserver<Boolean>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {
                                        mCompositeDisposable.add(d);
                                    }

                                    @Override
                                    public void onSuccess(@NonNull Boolean aBoolean) {
                                        Timber.e("Duongcv insert sticker"  + aBoolean);
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                        Timber.e("Duongcv insert sticker " + e.getMessage());
                                    }
                                });
                            }
                            App.getInstance().mPrefs.edit().putInt(com.tapbi.spark.yokey.common.Constant.STICKER_LAST_VERSION, lastVersion).apply();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ListSticker> call, Throwable t) {

                }
            });

    }

    private void refeshLoadData() {
        idCategoryLoad = -1;
        sortKeyLoad = -1;
    }

    public void addStickerRecent(StickerRecent stickerRecent) {
        Timber.d("duongcv add sticker " + stickerRecent.getLink());
        Single.fromCallable(() -> {
            ThemeDB.getInstance(App.getInstance()).stickerRecentDAO().insertStickerRecent(stickerRecent);
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                Timber.d("duongcv add sticker success " + stickerRecent.getLink());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d("duongcv add false " + stickerRecent.getLink());
            }
        });
    }

    public void downloadZipFileTheme(Sticker sticker) {
        Call<ResponseBody> call = stickerServiceDownload.downloadFileByUrl(sticker.getId() + "/" + sticker.getId() + ".zip");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    downloadAndSaveSticker(String.valueOf(sticker.getId()), response.body()).subscribe(new SingleObserver<Boolean>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            if (aBoolean) {
                                Timber.d("duongcv 1" + idShowResultDownload +":" +sticker.getId());
                                if (idShowResultDownload == sticker.getId())
                                    resultDownload.postValue(true);
                                else {
                                    sticker.setDownload(1);
                                    insertStickerDatabase(App.getInstance(), sticker).subscribe(new SingleObserver<Boolean>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@NonNull Boolean aBoolean) {
                                            updateListStickerOnkeyboard();
                                            EventBus.getDefault().post(new MessageEvent(Constant.KEY_CHANGE_STICKER_NOT_SHOW_PREVIEW));
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }
                                    });
                                }
                            } else {
                                if (idShowResultDownload == sticker.getId())
                                    resultDownload.postValue(false);
                                Toast.makeText(App.getInstance(), App.getInstance().getResources().getString(R.string.apply_sticker_fail), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            if (idShowResultDownload == sticker.getId())
                                resultDownload.postValue(false);
                            Toast.makeText(App.getInstance(), App.getInstance().getResources().getString(R.string.apply_sticker_fail), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (idShowResultDownload == sticker.getId()) resultDownload.postValue(false);
                    Toast.makeText(App.getInstance(), App.getInstance().getResources().getString(R.string.apply_sticker_fail), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                if (idShowResultDownload == sticker.getId()) resultDownload.postValue(false);
                Toast.makeText(App.getInstance(), App.getInstance().getResources().getString(R.string.apply_sticker_fail), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private Single<Boolean> downloadAndSaveSticker(String stickerId, ResponseBody body) {
        return Single.fromCallable(() -> {
            String fileName = stickerId + ".zip";
            return CommonUtil.saveThemeToInternalStorage(App.getInstance(), body, fileName, stickerId, true);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> insertStickerDatabase(Context context, Sticker sticker) {
        return Single.fromCallable(() -> {
            ThemeDB.getInstance(context).stickerDAO().insertSticker(sticker);
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    public Single<Boolean> insertStickersDatabase(Context context, List<Sticker> stickers) {
        return Single.fromCallable(() -> {
            ThemeDB.getInstance(context).stickerDAO().insertStickerList(stickers);
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    public MutableLiveData<List<Sticker>> getLiveData(int idCategory) {
        switch (idCategory){
            case 1000:
                return listMutableLiveDataAnimal;
            case 2000:
                return listMutableLiveDataTikTok;
            default:
                return listMutableLiveDataOther;
        }
//        if (idCategory == 1000) return listMutableLiveDataAnimal;
//        else return listMutableLiveDataTikTok;
    }

//    public MutableLiveData<List<Sticker>> getLiveStickerData(int idCategory) {
//        if (idCategory == 1000) return listStickerMutableLiveDataAnimal;
//        else return listStickerMutableLiveDataTikTok;
//    }

    public void loadStickerDB(int idCategory) {
        Single.fromCallable(() -> ThemeDB.getInstance(App.getInstance()).stickerDAO().getAllStickerIsDownload(1, idCategory)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<List<Sticker>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull List<Sticker> stickers) {
//                if (idCategory == 1000) listStickerMutableLiveDataAnimal.postValue(stickers);
//                else listStickerMutableLiveDataTikTok.postValue(stickers);
                getLiveData(idCategory).postValue(stickers);
            }

            @Override
            public void onError(@NonNull Throwable e) {
//                if (idCategory == 1000) listStickerMutableLiveDataAnimal.postValue(null);
//                else listStickerMutableLiveDataTikTok.postValue(null);
                getLiveData(idCategory).postValue(null);
            }
        });
    }

    public void insertEmojiDB(Emoji emoji) {
        if (ThemeDB.getInstance(App.getInstance()) != null && ThemeDB.getInstance(App.getInstance()).emojiDAO() != null) {
            ThemeDB.getInstance(App.getInstance()).emojiDAO().insertEmoji(emoji);
        }
    }

    public void deleteEmojiDB(Emoji emoji) {
        if (ThemeDB.getInstance(App.getInstance()) != null && ThemeDB.getInstance(App.getInstance()).emojiDAO() != null) {
            ThemeDB.getInstance(App.getInstance()).emojiDAO().deleteEmoji(emoji);
        }
    }

    public void updateEmojiDB(Emoji emoji) {
        if (ThemeDB.getInstance(App.getInstance()) != null && ThemeDB.getInstance(App.getInstance()).emojiDAO() != null) {
            ThemeDB.getInstance(App.getInstance()).emojiDAO().updateFavourite(emoji.getFavourite(), emoji.getContent());
        }
    }

    public Single<ArrayList<Emoji>> loadEmojiFavourite() {
        return Single.fromCallable(() -> (ArrayList<Emoji>) ThemeDB.getInstance(App.getInstance()).emojiDAO().allEmojiByFavourite(1)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<ArrayList<Emoji>> loadEmojiByType(int type) {
        return Single.fromCallable(() -> (ArrayList<Emoji>) ThemeDB.getInstance(App.getInstance()).emojiDAO().allEmojiByType(type)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<ArrayList<Emoji>> loadEmoji() {
        return Single.fromCallable(() -> (ArrayList<Emoji>) ThemeDB.getInstance(App.getInstance()).emojiDAO().getAllEmoji()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public void insertDataEmoji(ArrayList<Emoji> list) {
        ThemeDB.getInstance(App.getInstance()).emojiDAO().insertEmojiList(list);
    }
    public void loadDataGifCategory() {
        loadCategoryGif().subscribe(new SingleObserver<ArrayList<String>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull ArrayList<String> strings) {
               arrayListCategoryGif.clear();
               arrayListCategoryGif.addAll(strings);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }
    public Single<ArrayList<String>> loadCategoryGif() {
        return Single.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                return getCategoryGif();
            }
        }).subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor())).observeOn(AndroidSchedulers.mainThread());
    }
    public ArrayList<String> getCategoryGif() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("trending");
        arrayList.add("sticker");
        arrayList.add("text");
        arrayList.add("emoji");
        arrayList.add("actions");
        arrayList.add("adjectives");
        arrayList.add("animals");
        arrayList.add("anime");
        arrayList.add("art & desig");
        arrayList.add("cartoons & comic");
        arrayList.add("celebrities");
        arrayList.add("decades");
        arrayList.add("emotions");
        arrayList.add("fashion & beauty");
        arrayList.add("food & drink");
        arrayList.add("gaming");
        arrayList.add("greetings");
        arrayList.add("holiday");
        arrayList.add("identity");
        arrayList.add("interests");
        arrayList.add("memes");
        arrayList.add("movies");
        arrayList.add("music");
        arrayList.add("nature");
        arrayList.add("new & politics");
        arrayList.add("reactions");
        arrayList.add("science");
        arrayList.add("sports");
        arrayList.add("transportation");
        arrayList.add("tv");
        arrayList.add("weird");
        return arrayList;
    }
}
