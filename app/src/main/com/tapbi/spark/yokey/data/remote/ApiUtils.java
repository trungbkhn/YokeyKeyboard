package com.tapbi.spark.yokey.data.remote;

public class ApiUtils {
    private static final String BASE_URL_THEME = "https://m5zbt5p4zh.execute-api.eu-central-1.amazonaws.com/";
    private static final String BASE_URL_LED_THEME = "https://g4fh4czk0h.execute-api.eu-central-1.amazonaws.com/";
    private static final String BASE_URL_GET_GRADIENT_THEME = "https://xbhsmnyr5b.execute-api.eu-central-1.amazonaws.com/";
    private static final String BASE_URL_GET_COLOR_THEME = "https://3mch3g5yp2.execute-api.eu-central-1.amazonaws.com/";
    private static final String BASE_URL_GET_BACKGROUND_THEME = "https://l4c1bb2pe4.execute-api.eu-central-1.amazonaws.com/";
    private static final String BASE_URL_GET_HOT_THEME = "https://bojfpsk2ni.execute-api.eu-central-1.amazonaws.com/";
    private static final String BASE_URL_GET_HOT_STICKER = "https://zkzay3j1sb.execute-api.eu-central-1.amazonaws.com/";
    private static final String BASE_URL_CHECK_UPDATE_STICKER = "https://ddse8r4zli.execute-api.eu-central-1.amazonaws.com/";
    private static final String BASE_URL_CHECK_UPDATE_THEME = "https://vxcs6uu78h.execute-api.eu-central-1.amazonaws.com/";
    public static final String ROOT_URL_BACKGROUND = "https://207yyqxul2.execute-api.eu-central-1.amazonaws.com";

    //Todo Test
    private static final String BASE_URL_GET_HOT_THEME_TEST = "https://bq1bi7phgd.execute-api.eu-central-1.amazonaws.com";

    private static final String BASE_URL_GET_TOP_THEME = "https://eyt1euffa0.execute-api.eu-central-1.amazonaws.com/";

    public static final String BASE_URL_DOWNLOAD_ZIP_THEME = "https://ledkeythemes.s3.eu-central-1.amazonaws.com/";
    public static final String BASE_URL_DOWNLOAD_ZIP_STICKER = "https://zomj-emojikeyboard.s3.eu-central-1.amazonaws.com/";


    public static ThemesService getLEDThemesService() {
        return RetrofitClient.getLEDTheme(BASE_URL_LED_THEME).create(ThemesService.class);
    }
    public static ThemesService getThemesService() {
        return RetrofitClient.getLEDTheme(BASE_URL_THEME).create(ThemesService.class);
    }

    public static ThemesService downloadZipThemesService() {
        return RetrofitClient.downloadZIPFileTheme(BASE_URL_DOWNLOAD_ZIP_THEME).create(ThemesService.class);
    }
    public static ThemesService downloadZipNewThemesService(String baseUrl) {
        return RetrofitClient.downloadZIPFileTheme(baseUrl).create(ThemesService.class);
    }
    public static ThemesService downloadZipThemesServiceNew(String id) {
        return RetrofitClient.downloadZIPFileTheme(BASE_URL_DOWNLOAD_ZIP_THEME+id+"/").create(ThemesService.class);
    }
    public static StickerService downloadZipStickerService() {
        return RetrofitClient.getRetrofitServer(BASE_URL_DOWNLOAD_ZIP_STICKER).create(StickerService.class);
    }


    public static ThemesService getGradientThemesService() {
        return RetrofitClient.getGradientTheme(BASE_URL_GET_GRADIENT_THEME).create(ThemesService.class);
    }

    public static ThemesService getColorThemesService() {
        return RetrofitClient.getColorTheme(BASE_URL_GET_COLOR_THEME).create(ThemesService.class);
    }

    public static ThemesService getBackgroundThemesService() {
        return RetrofitClient.getBackgroundTheme(BASE_URL_GET_BACKGROUND_THEME).create(ThemesService.class);
    }
    public static ThemeService getBackground() {
        return RetrofitClient.getRetrofitServer(ROOT_URL_BACKGROUND).create(ThemeService.class);
    }

    public static ThemesService getHotThemesService() {
        return RetrofitClient.getHotTheme(BASE_URL_GET_HOT_THEME).create(ThemesService.class);

        //  return RetrofitClient.getHotTheme(BASE_URL_GET_HOT_THEME_TEST).create(ThemesService.class);
    }

    public static ThemesService getTopThemesService() {
        return RetrofitClient.getTopTheme(BASE_URL_GET_TOP_THEME).create(ThemesService.class);
    }

    public static StickerService getStickerService() {
        return RetrofitClient.getRetrofitServer(BASE_URL_GET_HOT_STICKER).create(StickerService.class);
    }

    public static StickerService checkUpdateStickerService() {
        return RetrofitClient.getRetrofitServer(BASE_URL_CHECK_UPDATE_STICKER).create(StickerService.class);
    }

    public static ThemesService checkUpdateThemeService() {
        return RetrofitClient.getRetrofitServer(BASE_URL_CHECK_UPDATE_THEME).create(ThemesService.class);
    }

}
