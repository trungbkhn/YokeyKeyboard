package com.tapbi.spark.yokey.util;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.view.inputmethod.InputMethodManager;

import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils;
import com.tapbi.spark.yokey.App;

public class UpdateAppManager {
    public static boolean needCheckInterval = false;

//    public static void callApiCheckUpdate() {
//        if (needCheckInterval) {
//            if (!checkTimeIntervalUpdate()) {
//                return;
//            }
//        }
//        needCheckInterval = false;
//        ThemesService themesService = RetrofitClient.getCommonRetrofitBuilder(ApiUtils.BASE_URL_CHECK_UPDATE).build().create(ThemesService.class);
//        themesService.getAppConfig(new ConfigAppRequest(App.getInstance().themeRepository.getLastThemeVersionUpdated())).doOnSuccess(checkUpdate -> {
//            Timber.e("getCheckUpdate result" + checkUpdate.getConfigUpdate());
//            if (checkUpdate.getConfigUpdate() != null && checkUpdate.getConfigUpdate().getThemeVersion() != null) {
//                App.getInstance().mPrefs.edit().putLong(Constant.PREF_LAST_TIME_CHECK_UPDATE, System.currentTimeMillis()).apply();
//                ThemeVersion themeVersion = checkUpdate.getConfigUpdate().getThemeVersion();
//                List<String> storeCategories = checkUpdate.getConfigUpdate().getStoreCategories();
//                App.versionUpdate = new int[]{themeVersion.getHotVersion(), themeVersion.getLedVersion(), themeVersion.getGradientVersion()
//                        , themeVersion.getColorVersion(), themeVersion.getWallpaperVersion()};
//
//                int serverVersion = checkUpdate.getConfigUpdate().getVersionUpdate();
//                if (App.getInstance().themeRepository.getLastThemeVersionUpdated() < serverVersion) {
//                    App.getInstance().mPrefs.edit().putBoolean(Constant.PREF_CLEAR_DATA_WHEN_UPDATE, false).apply();
//                    App.getInstance().themeRepository.updateThemesChanged(checkUpdate.getThemeObjects(), serverVersion);
//                }
//                // Timber.d("ducNQstoreCategories "+storeCategories.size());
//                if (storeCategories != null && storeCategories.size() > 0) {
//
//                    App.listCategoryBackground = storeCategories;
//                    App.getInstance().sendBroadcast(new Intent(Constant.ACTION_WAITING_CHECK_UPDATE));
//                }
//                int versionStickerLocal = App.getInstance().mPrefs.getInt(Constant.PREF_VERSION_STICKER, 0);
//                if (versionStickerLocal < checkUpdate.getConfigUpdate().getVersionSticker()) {
//                    App.getInstance().mPrefs.edit().putInt(Constant.PREF_VERSION_STICKER, checkUpdate.getConfigUpdate().getVersionSticker()).apply();
//                    App.getInstance().mPrefs.edit().putBoolean(
//                            Constant.PREFIX_STICKER_CATEGORY.concat(String.valueOf(1000).concat(Constant.PREFIX_STATE_LOAD)), false).apply();
//
//                }
//
//                //Todo: Duongcv check update noti on keyboard
//                processRequestVersionNotiOnKeyboard(checkUpdate);
//
//            }
//
////            checkUpdate.mConfigUpdate.saleOff = null;
////            if (checkUpdate.mConfigUpdate.saleOff == null) {
////                checkUpdate.mConfigUpdate.saleOff = new ConfigAppResponse.SaleOff();
////                checkUpdate.mConfigUpdate.saleOff.setSubscriptionId("tapbi_subscription_month");
////                checkUpdate.mConfigUpdate.saleOff.setOfferTag("sub-y-50-off");
////                checkUpdate.mConfigUpdate.saleOff.setPercentOff(30);
////                checkUpdate.mConfigUpdate.saleOff.setStartTime(1655798447);
////                checkUpdate.mConfigUpdate.saleOff.setEndTime(1755802047);
////                checkUpdate.mConfigUpdate.saleOff.setUpdatedTime(System.currentTimeMillis() / 1000);
////            }
//            if (checkUpdate.mConfigUpdate.saleOff != null) {
//                Gson gson = new Gson();
//                String saleData = App.getInstance().mPrefs.getString(Constant.PREF_LAST_SALE_OFF, "");
//                ConfigAppResponse.SaleOff oldSaleOff = gson.fromJson(saleData, ConfigAppResponse.SaleOff.class);
//                if (oldSaleOff == null) {
//                    App.getInstance().mPrefs.edit().putString(Constant.PREF_LAST_SALE_OFF, gson.toJson(checkUpdate.mConfigUpdate.saleOff)).apply();
//                    EventBus.getDefault().post(new MessageEvent(Constant.EVENT_NEW_SALE_OFF));
//                } else if (oldSaleOff.getOfferTag() != null) {
//                    if (!oldSaleOff.getOfferTag().equals(checkUpdate.mConfigUpdate.saleOff.getOfferTag()) || checkUpdate.mConfigUpdate.saleOff.getUpdatedTime() > oldSaleOff.getUpdatedTime()) {
//                        saleData = gson.toJson(checkUpdate.mConfigUpdate.saleOff);
//                        App.getInstance().mPrefs.edit().putString(Constant.PREF_LAST_SALE_OFF, saleData).apply();
//                        EventBus.getDefault().post(new MessageEvent(Constant.EVENT_NEW_SALE_OFF));
//                    }
//                }
//
//            }else{
//                App.getInstance().mPrefs.edit().putString(Constant.PREF_LAST_SALE_OFF,"");
//            }
//        }).doOnError(throwable -> throwable.printStackTrace()).subscribe();
//    }

//    private static void processRequestVersionNotiOnKeyboard(ConfigAppResponse checkUpdate) {
//        int lastVersionNotiOnKeyboard = App.getInstance().mPrefs.getInt(Constant.PREF_LAST_VERSION_NOTI_ON_KEYBOARD, 0);
//        if (lastVersionNotiOnKeyboard < checkUpdate.getConfigUpdate().getVesionNotiOnKeyboard()) {
//            ThemesService themesService = RetrofitClient.getCommonRetrofitBuilder(ApiUtils.BASE_URL_GET_VERSION_NOTI_ON_KEYBOARD).build().create(
//                    ThemesService.class);
//            themesService.getDataNotiOnKeyboard(new RequestNotiOnKeyboard(checkUpdate.getConfigUpdate().getVesionNotiOnKeyboard())).doOnSuccess(
//                    dataNotiOnKeyboard -> {
//                        App.getInstance().mPrefs.edit().putInt(Constant.PREF_LAST_VERSION_NOTI_ON_KEYBOARD,
//                                checkUpdate.getConfigUpdate().getVesionNotiOnKeyboard()).apply();
//                        Timber.e("getCheckUpdate Noti " + dataNotiOnKeyboard);
//                        if (dataNotiOnKeyboard != null) {
//                            Log.d("duongcv", "processRequestVersionNotiOnKeyboard: push noti");
//                            DataFromPushNoti dataFromPushNoti = new DataFromPushNoti(dataNotiOnKeyboard.getItem().get(0).getType(),
//                                    dataNotiOnKeyboard.getItem().get(0).getTitle()
//                                    , dataNotiOnKeyboard.getItem().get(0).getContent(), dataNotiOnKeyboard.getItem().get(0).getUrl(),
//                                    dataNotiOnKeyboard.getItem().get(0).getIcon(),
//                                    dataNotiOnKeyboard.getItem().get(0).getCover());
//                            App.getInstance().mPrefs.edit().putBoolean(Constant.IS_OLD_DATA_NOTI_ON_KEYBOARD, true).apply();
//                            App.getInstance().mPrefs.edit().putString(Constant.DATA_NOTI_ON_KEYBOARD, new Gson().toJson(dataFromPushNoti)).apply();
//                            App.getInstance().mPrefs.edit().putString(Constant.DATA_NOTI_ON_KEYBOARD_URL, dataFromPushNoti.getUrl()).apply();
//                        }
//                    }).doOnError(throwable -> {
//                throwable.printStackTrace();
//            }).subscribe();
//        }
//    }

    public static void checkUpdateVersion() {
        InputMethodManager mImm = (InputMethodManager) App.getInstance().getSystemService(INPUT_METHOD_SERVICE);
        if (UncachedInputMethodManagerUtils.isThisImeCurrent(App.getInstance(), mImm)) {
            try {
                App.getInstance().keyboardLanguageRepository.getAllLanguages("null", true);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

//    public static boolean checkTimeIntervalUpdate() {
//        long interval = App.getInstance().mPrefs.getLong(Constant.PREF_INTERVAL_CHECK_UPDATE_WITH_NOTI, Constant.MIN_INTERVAL_CHECK_UPDATE);
//        long lastTimeCheckUpdate = App.getInstance().mPrefs.getLong(Constant.PREF_LAST_TIME_CHECK_UPDATE, 0);
//        return System.currentTimeMillis() - lastTimeCheckUpdate >= interval;
//    }


    public static void loadCheckPreviousVersion() {
//        if (App.versionUpdate == null) {
//            int versionHot = App.getInstance().mPrefs.getInt(CommonUtil.getKeyUpdateVersion(Constant.TYPE_THEME_HOT), -1);
//            int versionLed = App.getInstance().mPrefs.getInt(CommonUtil.getKeyUpdateVersion(Constant.TYPE_THEME_LED), -1);
//            int versionGradient = App.getInstance().mPrefs.getInt(CommonUtil.getKeyUpdateVersion(Constant.TYPE_THEME_GRADIENT), -1);
//            int versionColor = App.getInstance().mPrefs.getInt(CommonUtil.getKeyUpdateVersion(Constant.TYPE_THEME_COLOR), -1);
//            int versionWallpaper = App.getInstance().mPrefs.getInt(CommonUtil.getKeyUpdateVersion(Constant.TYPE_THEME_WALLPAPER), -1);
//            App.versionUpdate = new int[]{versionHot, versionLed, versionGradient
//                    , versionColor, versionWallpaper};
//        }

    }
}
