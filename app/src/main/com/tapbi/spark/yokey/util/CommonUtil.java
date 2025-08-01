package com.tapbi.spark.yokey.util;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.android.inputmethod.keyboard.KeyboardView.SPECIAL_CHARACTERS_CHANGE_FONT;
import static com.android.inputmethod.keyboard.KeyboardView.SYMBOL_LANGUAGE_GREEK_CHANGE_FONT;
import static com.android.inputmethod.keyboard.KeyboardView.SYMBOL_NOT_CHANGE_FONT;
import static com.android.inputmethod.latin.common.LocaleUtils.constructLocaleFromString;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.emoji.DynamicGridKeyboard;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.utils.AdditionalSubtypeUtils;
import com.android.inputmethod.latin.utils.SubtypeLocaleUtils;
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.renderscript.Toolkit;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.common.CommonVariable;
import com.tapbi.spark.yokey.data.local.LanguageEntity;
import com.tapbi.spark.yokey.data.local.entity.Sticker;
import com.tapbi.spark.yokey.data.model.Font;
import com.tapbi.spark.yokey.data.model.LanguageTranslate;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.data.model.theme.BackgroundKey;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.interfaces.IResultDownBackground;
import com.tapbi.spark.yokey.ui.main.MainActivity;
import com.tapbi.spark.yokey.ui.welcome.WelcomeActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class CommonUtil {

    public static final List<String> keyThemeKeyIconCommaKeyBoard = Arrays.asList("6011", "6012", "6013", "6014", "6015");
    public static final List<String> keyThemeKeyTextCommaKeyBoard = Arrays.asList("6014");
    public static final List<String> keyThemeKeyNoIconShiftEnterKeyBoard = Arrays.asList("6011", "6012", "6013", "6015", "6016", "6017", "6019", "6029"); // ẩn các icon shift, enter,delete
    public static final List<String> keyThemeKeyIconLanguageKeyBoard = Arrays.asList("6016", "6017", "6019", "6029");

    private static final Map<String, Drawable> currentEffect = new HashMap<>();
    public static long lastClickTime = 0;
    private static long timeToast = 0;

    // private static final ContextWrapper contextWrapper = new ContextWrapper(App.getInstance());
    //private static final File file = contextWrapper.getDir(App.getInstance().getFilesDir().getName(), Context.MODE_PRIVATE);
    private static String loadJsonFromAsset(Context context, String strFileName) {

        String json = "";
        try {
            InputStream inputStream = context.getAssets().open(strFileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static boolean isIntentAvailable(Intent intent, Context context) {
        if (intent == null) {
            return false;
        }
        return context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    public static String[][] getListLanguage() {
        return new String[][]{
                new String[]{"", App.getInstance().getResources().getString(R.string.langauge_detection), "", "", ""},
                new String[]{"af", App.getInstance().getResources().getString(R.string.afrikaans), "", "south_africa", "🇿🇦"},
                new String[]{"sq", App.getInstance().getResources().getString(R.string.albanian), "", "albania", "🇦🇱"},
                /* new String[]{"ar", App.getInstance().getResources().getString(R.string.arabic), "ar-SA", "saudi_arabia", "🇦🇪"},*/
                new String[]{"hy", App.getInstance().getResources().getString(R.string.armenian), "", "armenia", "🇦🇲"},
                new String[]{"az", App.getInstance().getResources().getString(R.string.azerbaijani), "", "azerbaijan", "🇦🇿"},
                new String[]{"eu", App.getInstance().getResources().getString(R.string.basque), "", "spain", "🏳️‍🌈"},
                new String[]{"be", App.getInstance().getResources().getString(R.string.belarusian), "", "belarus", "🇧🇾"},
                new String[]{"bn", App.getInstance().getResources().getString(R.string.bengali), "", "benin", "🇧🇩"},
                new String[]{"bs", App.getInstance().getResources().getString(R.string.bosnian), "", "bosnia_and_herzegovina", "🇧🇦"},
                new String[]{"bg", App.getInstance().getResources().getString(R.string.bulgarian), "", "bulgaria", "🇧🇬"},
                new String[]{"ca", App.getInstance().getResources().getString(R.string.catalan), "", "spain", "🏳️‍🌈"},
                new String[]{"ceb", App.getInstance().getResources().getString(R.string.cebuano), "", "philippines", "🏳️‍🌈"},
                new String[]{"ny", App.getInstance().getResources().getString(R.string.chichewa), "", "malawi", "🇲🇼"},
                new String[]{"zh-CN", App.getInstance().getResources().getString(R.string.chinese_simplified), "zh-CN", "china", "🇨🇳"},
                new String[]{"zh-TW", App.getInstance().getResources().getString(R.string.chinese_traditional), "zh-CN", "china", "🇹🇼"},
                new String[]{"hr", App.getInstance().getResources().getString(R.string.croatian), "", "croatia", "🇭🇷"},
                new String[]{"cs", App.getInstance().getResources().getString(R.string.czech), "cs-CZ", "czech_republic", "🇨🇿"},
                new String[]{"da", App.getInstance().getResources().getString(R.string.danish), "da-DK", "denmark", "🇩🇰"},
                new String[]{"nl", App.getInstance().getResources().getString(R.string.dutch), "", "netherlands", "🇳🇱"},
                new String[]{"en", App.getInstance().getResources().getString(R.string.english), "en-US", "united_kingdom", "🏴󠁧󠁢󠁥󠁮󠁧󠁿"},
                new String[]{"eo", App.getInstance().getResources().getString(R.string.esperanto), "", "esperanto", "🏳️‍🌈"},
                new String[]{"et", App.getInstance().getResources().getString(R.string.estonian), "", "estonia", "🇪🇪"},
                new String[]{"tl", App.getInstance().getResources().getString(R.string.filipino), "", "philippines", "🇵🇭"},
                new String[]{"fi", App.getInstance().getResources().getString(R.string.finnish), "fi-FI", "finland", "🇫🇮"},
                new String[]{"fr", App.getInstance().getResources().getString(R.string.french), "fr-CA", "france", "🇫🇷"},
                new String[]{"gl", App.getInstance().getResources().getString(R.string.galician), "", "spain", "🏳️‍🌈"},
                new String[]{"ka", App.getInstance().getResources().getString(R.string.georgian), "", "georgia", "🇬🇪"},
                new String[]{"de", App.getInstance().getResources().getString(R.string.german), "de-DE", "germany", "🇩🇪"},
                new String[]{"gu", App.getInstance().getResources().getString(R.string.gujarati), "", "india", "🏳️‍🌈"},
                new String[]{"el", App.getInstance().getResources().getString(R.string.greek), "el-GR", "greece", "🇬🇷"},
                new String[]{"ht", App.getInstance().getResources().getString(R.string.haitian_creole), "", "haiti", "🇭🇹"},
                new String[]{"ha", App.getInstance().getResources().getString(R.string.hausa), "", "niger", "🏳️‍🌈"},
                new String[]{"iw", App.getInstance().getResources().getString(R.string.hebrew), "he-IL", "israel", "🇮🇱"},
                new String[]{"hi", App.getInstance().getResources().getString(R.string.hindi), "hi-IN", "india", "🇮🇳"},
                new String[]{"hmn", App.getInstance().getResources().getString(R.string.hmong), "", "china", "🏳️‍🌈"},
                new String[]{"hu", App.getInstance().getResources().getString(R.string.hungarian), "hu-HU", "hungary", "🇭🇺"},
                new String[]{"is", App.getInstance().getResources().getString(R.string.icelandic), "", "iceland", "🇮🇸"},
                new String[]{"id", App.getInstance().getResources().getString(R.string.indonesian), "id-ID", "indonesia", "🇮🇩"},
                new String[]{"ig", App.getInstance().getResources().getString(R.string.igbo), "", "nigeria", "🇿🇦"},
                new String[]{"ga", App.getInstance().getResources().getString(R.string.irish), "", "ireland", "🇮🇪"},
                new String[]{"it", App.getInstance().getResources().getString(R.string.italian), "it-IT", "italy", "🇮🇹"},
                new String[]{"ja", App.getInstance().getResources().getString(R.string.japanese), "ja-JP", "japan", "🇯🇵"},
                new String[]{"jw", App.getInstance().getResources().getString(R.string.javanese), "", "indonesia", "🇮🇩"},
                new String[]{"kk", App.getInstance().getResources().getString(R.string.kazakh), "", "kazakhstan", "🇰🇿"},
                new String[]{"km", App.getInstance().getResources().getString(R.string.khmer), "", "cambodia", "🇰🇭"},
                new String[]{"kn", App.getInstance().getResources().getString(R.string.kannada), "", "india", "🏳️‍🌈"},
                new String[]{"ko", App.getInstance().getResources().getString(R.string.korean), "ko-KR", "south_korea", "🇰🇷"},
                new String[]{"lo", App.getInstance().getResources().getString(R.string.lao), "", "laos", "🇱🇦"},
                new String[]{"lv", App.getInstance().getResources().getString(R.string.latvian), "", "latvia", "🇱🇻"},
                new String[]{"lt", App.getInstance().getResources().getString(R.string.lithuanian), "", "lithuania", "🇱🇹"},
                new String[]{"mk", App.getInstance().getResources().getString(R.string.macedonian), "", "macedonia", "🇲🇰"},
                new String[]{"mg", App.getInstance().getResources().getString(R.string.malagasy), "", "madagascar", "🇲🇬"},
                new String[]{"ms", App.getInstance().getResources().getString(R.string.malay), "", "malaysia", "🇲🇾"},
                new String[]{"ml", App.getInstance().getResources().getString(R.string.malayalam), "", "india", "🏳️‍🌈"},
                new String[]{"mi", App.getInstance().getResources().getString(R.string.maori), "", "new_zealand", "🏳️‍🌈"},
                new String[]{"mr", App.getInstance().getResources().getString(R.string.marathi), "", "india", "🏳️‍🌈"},
                new String[]{"my", App.getInstance().getResources().getString(R.string.myanmar_burmese), "", "myanmar", "🇲🇲"},
                new String[]{"mn", App.getInstance().getResources().getString(R.string.mongolian), "", "mongolia", "🇲🇳"},
                new String[]{"ne", App.getInstance().getResources().getString(R.string.nepali), "", "nepal", "🇳🇵"},
                new String[]{"no", App.getInstance().getResources().getString(R.string.norwegian), "no-NO", "norway", "🇳🇴"},
                new String[]{"fa", App.getInstance().getResources().getString(R.string.persian), "", "iran", "🇮🇷"},
                new String[]{"pl", App.getInstance().getResources().getString(R.string.polish), "pl-PL", "poland", "🇵🇱"},
                new String[]{"pt", App.getInstance().getResources().getString(R.string.portuguese), "pt-BR", "portugal", "🇵🇹"},
                new String[]{"pa", App.getInstance().getResources().getString(R.string.punjabi), "", "pakistan", "🏳️‍🌈"},
                new String[]{"ro", App.getInstance().getResources().getString(R.string.romanian), "ro-RO", "romania", "🇷🇴"},
                new String[]{"ru", App.getInstance().getResources().getString(R.string.russian), "ru-RU", "russia", "🇷🇺"},
                new String[]{"sr", App.getInstance().getResources().getString(R.string.serbian), "", "serbia", "🇷🇸"},
                new String[]{"st", App.getInstance().getResources().getString(R.string.sesotho), "", "lesotho", "🇱🇸"},
                new String[]{"si", App.getInstance().getResources().getString(R.string.sinhala), "", "sri_lanka", "🇱🇰"},
                new String[]{"sk", App.getInstance().getResources().getString(R.string.slovak), "sk-SK", "slovakia", "🇸🇰"},
                new String[]{"sl", App.getInstance().getResources().getString(R.string.slovenian), "", "slovenia", "🇸🇮"},
                new String[]{"so", App.getInstance().getResources().getString(R.string.somali), "", "somalia", "🇸🇴"},
                new String[]{"es", App.getInstance().getResources().getString(R.string.spanish), "es-ES", "spain", "🇪🇸"},
                new String[]{"su", App.getInstance().getResources().getString(R.string.sudanese), "", "sudan", "🇸🇩"},
                new String[]{"sv", App.getInstance().getResources().getString(R.string.swedish), "sv-SE", "sweden", "🇸🇪"},
                new String[]{"sw", App.getInstance().getResources().getString(R.string.swahili), "", "tanzania", "🇰🇪"},
                new String[]{"ta", App.getInstance().getResources().getString(R.string.tamil), "", "singapore", "🏳️‍🌈"},
                new String[]{"te", App.getInstance().getResources().getString(R.string.telugu), "", "india", "🇮🇪"},
                new String[]{"tg", App.getInstance().getResources().getString(R.string.tajik), "", "tajikistan", "🇹🇯"},
                new String[]{"th", App.getInstance().getResources().getString(R.string.thai), "th-TH", "thailand", "🇹🇭"},
                new String[]{"tr", App.getInstance().getResources().getString(R.string.turkish), "tr-TR", "turkey", "🇹🇷"},
                new String[]{"uk", App.getInstance().getResources().getString(R.string.ukrainian), "", "ukraine", "🇺🇦"},
                new String[]{"ur", App.getInstance().getResources().getString(R.string.urdu), "", "pakistan", "🇵🇰"},
                new String[]{"uz", App.getInstance().getResources().getString(R.string.uzbek), "", "uzbekistan", "🇺🇿"},
                new String[]{"vi", App.getInstance().getResources().getString(R.string.vietnamese), "", "vietnam", "🇻🇳"},
                new String[]{"cy", App.getInstance().getResources().getString(R.string.welsh), "", "wales", "🏴󠁧󠁢󠁷󠁬󠁳󠁿"},
                new String[]{"yi", App.getInstance().getResources().getString(R.string.yiddish), "", "sweden", "🇮🇱"},
                new String[]{"yo", App.getInstance().getResources().getString(R.string.yoruba), "", "nigeria", "🇳🇬"},
                new String[]{"zu", App.getInstance().getResources().getString(R.string.zulu), "", "south_africa", "🇿🇦"}};

    }

    public static final ArrayList<LanguageTranslate> getLanguageTranslate() {
        ArrayList<LanguageTranslate> languageTranslates = new ArrayList<>();
        /// languageTranslates.add())
        languageTranslates.add(new LanguageTranslate("", R.string.langauge_detection, "", "", ""));
        languageTranslates.add(new LanguageTranslate("af", R.string.afrikaans, "", "south_africa", "🇿🇦"));
        languageTranslates.add(new LanguageTranslate("sq", R.string.albanian, "", "albania", "🇦🇱"));
        languageTranslates.add(new LanguageTranslate("ar", R.string.arabic, "ar-SA", "saudi_arabia", "🇦🇪"));
        languageTranslates.add(new LanguageTranslate("hy", R.string.armenian, "", "armenia", "🇦🇲"));
        languageTranslates.add(new LanguageTranslate("az", R.string.azerbaijani, "", "azerbaijan", "🇦🇿"));
        languageTranslates.add(new LanguageTranslate("eu", R.string.basque, "", "spain", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("be", R.string.belarusian, "", "belarus", "🇧🇾"));
        languageTranslates.add(new LanguageTranslate("bn", R.string.bengali, "", "benin", "🇧🇩"));
        languageTranslates.add(new LanguageTranslate("bs", R.string.bosnian, "", "bosnia_and_herzegovina", "🇧🇦"));
        languageTranslates.add(new LanguageTranslate("bg", R.string.bulgarian, "", "bulgaria", "🇧🇬"));
        languageTranslates.add(new LanguageTranslate("ca", R.string.catalan, "", "spain", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("ceb", R.string.cebuano, "", "philippines", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("ny", R.string.chichewa, "", "malawi", "🇲🇼"));
        languageTranslates.add(new LanguageTranslate("zh-CN", R.string.chinese_simplified, "zh-CN", "china", "🇨🇳"));
        languageTranslates.add(new LanguageTranslate("zh-TW", R.string.chinese_traditional, "zh-CN", "china", "🇹🇼"));
        languageTranslates.add(new LanguageTranslate("hr", R.string.croatian, "", "croatia", "🇭🇷"));
        languageTranslates.add(new LanguageTranslate("cs", R.string.czech, "cs-CZ", "czech_republic", "🇨🇿"));
        languageTranslates.add(new LanguageTranslate("da", R.string.danish, "da-DK", "denmark", "🇩🇰"));
        languageTranslates.add(new LanguageTranslate("nl", R.string.dutch, "", "netherlands", "🇳🇱"));
        languageTranslates.add(new LanguageTranslate("en", R.string.english, "en-US", "united_kingdom", "🏴󠁧󠁢󠁥󠁮󠁧󠁿"));
        languageTranslates.add(new LanguageTranslate("eo", R.string.esperanto, "", "esperanto", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("et", R.string.estonian, "", "estonia", "🇪🇪"));
        languageTranslates.add(new LanguageTranslate("tl", R.string.filipino, "", "philippines", "🇵🇭"));
        languageTranslates.add(new LanguageTranslate("fi", R.string.finnish, "fi-FI", "finland", "🇫🇮"));
        languageTranslates.add(new LanguageTranslate("fr", R.string.french, "fr-CA", "france", "🇫🇷"));
        languageTranslates.add(new LanguageTranslate("gl", R.string.galician, "", "spain", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("ka", R.string.georgian, "", "georgia", "🇬🇪"));
        languageTranslates.add(new LanguageTranslate("de", R.string.german, "de-DE", "germany", "🇩🇪"));
        languageTranslates.add(new LanguageTranslate("gu", R.string.gujarati, "", "india", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("el", R.string.greek, "el-GR", "greece", "🇬🇷"));
        languageTranslates.add(new LanguageTranslate("ht", R.string.haitian_creole, "", "haiti", "🇭🇹"));
        languageTranslates.add(new LanguageTranslate("ha", R.string.hausa, "", "niger", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("iw", R.string.hebrew, "he-IL", "israel", "🇮🇱"));
        languageTranslates.add(new LanguageTranslate("hi", R.string.hindi, "hi-IN", "india", "🇮🇳"));
        languageTranslates.add(new LanguageTranslate("hmn", R.string.hmong, "", "china", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("hu", R.string.hungarian, "hu-HU", "hungary", "🇭🇺"));
        languageTranslates.add(new LanguageTranslate("is", R.string.icelandic, "", "iceland", "🇮🇸"));
        languageTranslates.add(new LanguageTranslate("id", R.string.indonesian, "id-ID", "indonesia", "🇮🇩"));
        languageTranslates.add(new LanguageTranslate("ig", R.string.igbo, "", "nigeria", "🇿🇦"));
        languageTranslates.add(new LanguageTranslate("ga", R.string.irish, "", "ireland", "🇮🇪"));
        languageTranslates.add(new LanguageTranslate("it", R.string.italian, "it-IT", "italy", "🇮🇹"));
        languageTranslates.add(new LanguageTranslate("ja", R.string.japanese, "ja-JP", "japan", "🇯🇵"));
        languageTranslates.add(new LanguageTranslate("jw", R.string.javanese, "", "indonesia", "🇮🇩"));
        languageTranslates.add(new LanguageTranslate("kk", R.string.kazakh, "", "kazakhstan", "🇰🇿"));
        languageTranslates.add(new LanguageTranslate("km", R.string.khmer, "", "cambodia", "🇰🇭"));
        languageTranslates.add(new LanguageTranslate("kn", R.string.kannada, "", "india", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("ko", R.string.korean, "ko-KR", "south_korea", "🇰🇷"));
        languageTranslates.add(new LanguageTranslate("lo", R.string.lao, "", "laos", "🇱🇦"));
        languageTranslates.add(new LanguageTranslate("lv", R.string.latvian, "", "latvia", "🇱🇻"));
        languageTranslates.add(new LanguageTranslate("lt", R.string.lithuanian, "", "lithuania", "🇱🇹"));
        languageTranslates.add(new LanguageTranslate("mk", R.string.macedonian, "", "macedonia", "🇲🇰"));
        languageTranslates.add(new LanguageTranslate("mg", R.string.malagasy, "", "madagascar", "🇲🇬"));
        languageTranslates.add(new LanguageTranslate("ms", R.string.malay, "", "malaysia", "🇲🇾"));
        languageTranslates.add(new LanguageTranslate("ml", R.string.malayalam, "", "india", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("mi", R.string.maori, "", "new_zealand", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("mr", R.string.marathi, "", "india", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("my", R.string.myanmar_burmese, "", "myanmar", "🇲🇲"));
        languageTranslates.add(new LanguageTranslate("mn", R.string.mongolian, "", "mongolia", "🇲🇳"));
        languageTranslates.add(new LanguageTranslate("ne", R.string.nepali, "", "nepal", "🇳🇵"));
        languageTranslates.add(new LanguageTranslate("no", R.string.norwegian, "no-NO", "norway", "🇳🇴"));
        languageTranslates.add(new LanguageTranslate("fa", R.string.persian, "", "iran", "🇮🇷"));
        languageTranslates.add(new LanguageTranslate("pl", R.string.polish, "pl-PL", "poland", "🇵🇱"));
        languageTranslates.add(new LanguageTranslate("pt", R.string.portuguese, "pt-BR", "portugal", "🇵🇹"));
        languageTranslates.add(new LanguageTranslate("pa", R.string.punjabi, "", "pakistan", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("ro", R.string.romanian, "ro-RO", "romania", "🇷🇴"));
        languageTranslates.add(new LanguageTranslate("ru", R.string.russian, "ru-RU", "russia", "🇷🇺"));
        languageTranslates.add(new LanguageTranslate("sr", R.string.serbian, "", "serbia", "🇷🇸"));
        languageTranslates.add(new LanguageTranslate("st", R.string.sesotho, "", "lesotho", "🇱🇸"));
        languageTranslates.add(new LanguageTranslate("si", R.string.sinhala, "", "sri_lanka", "🇱🇰"));
        languageTranslates.add(new LanguageTranslate("sk", R.string.slovak, "sk-SK", "slovakia", "🇸🇰"));
        languageTranslates.add(new LanguageTranslate("sl", R.string.slovenian, "", "slovenia", "🇸🇮"));
        languageTranslates.add(new LanguageTranslate("so", R.string.somali, "", "somalia", "🇸🇴"));
        languageTranslates.add(new LanguageTranslate("es", R.string.spanish, "es-ES", "spain", "🇪🇸"));
        languageTranslates.add(new LanguageTranslate("su", R.string.sudanese, "", "sudan", "🇸🇩"));
        languageTranslates.add(new LanguageTranslate("sv", R.string.swedish, "sv-SE", "sweden", "🇸🇪"));
        languageTranslates.add(new LanguageTranslate("sw", R.string.swahili, "", "tanzania", "🇰🇪"));
        languageTranslates.add(new LanguageTranslate("ta", R.string.tamil, "", "singapore", "🏳️‍🌈"));
        languageTranslates.add(new LanguageTranslate("te", R.string.telugu, "", "india", "🇮🇪"));
        languageTranslates.add(new LanguageTranslate("tg", R.string.tajik, "", "tajikistan", "🇹🇯"));
        languageTranslates.add(new LanguageTranslate("th", R.string.thai, "th-TH", "thailand", "🇹🇭"));
        languageTranslates.add(new LanguageTranslate("tr", R.string.turkish, "tr-TR", "turkey", "🇹🇷"));
        languageTranslates.add(new LanguageTranslate("uk", R.string.ukrainian, "", "ukraine", "🇺🇦"));
        languageTranslates.add(new LanguageTranslate("ur", R.string.urdu, "", "pakistan", "🇵🇰"));
        languageTranslates.add(new LanguageTranslate("uz", R.string.uzbek, "", "uzbekistan", "🇺🇿"));
        languageTranslates.add(new LanguageTranslate("vi", R.string.vietnamese, "", "vietnam", "🇻🇳"));
        languageTranslates.add(new LanguageTranslate("cy", R.string.welsh, "", "wales", "🏴󠁧󠁢󠁷󠁬󠁳󠁿"));
        languageTranslates.add(new LanguageTranslate("yi", R.string.yiddish, "", "sweden", "🇮🇱"));
        languageTranslates.add(new LanguageTranslate("yo", R.string.yoruba, "", "nigeria", "🇳🇬"));
        languageTranslates.add(new LanguageTranslate("zu", R.string.zulu, "", "south_africa", "🇿🇦"));
        return languageTranslates;
    }

    public static void checkCurrentAppKeyBoard(Activity activity) {
        try {
            InputMethodManager mImm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            if (!UncachedInputMethodManagerUtils.isThisImeCurrent(activity, mImm)) {
                final Intent intent = new Intent();
                intent.setClass(activity, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
                activity.finish();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static void checkKeyboardApp(Activity activity) {
        // try {
        InputMethodManager mImm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        UncachedInputMethodManagerUtils.isThisImeCurrent(activity, mImm);
//        }catch (Exception ignored){
//            Timber.e(ignored);
//        }
        //return false;
    }

    public static boolean isFirstInstall(Context context) {
        try {
            long firstInstallTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
            long lastUpdateTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
            return firstInstallTime == lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static ArrayList<String> getStringArrayPref(String key) {
        SharedPreferences prefs = android.preference.PreferenceManager
                .getDefaultSharedPreferences(App.getInstance());
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public static void setStringArrayPref(String key, ArrayList<String> values) {
        SharedPreferences prefs = android.preference.PreferenceManager
                .getDefaultSharedPreferences(App.getInstance());
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }

//    public static LanguageEntity convertIMSubtypeToLanguageEntity(InputMethodSubtype inputMethodSubtype) {
//        LanguageEntity languageEntity = new LanguageEntity();
//        languageEntity.setName(SubtypeLocaleUtils.getKeyboardLayoutSetName(inputMethodSubtype));
//        String layoutKeyboardName = SubtypeLocaleUtils.getKeyboardLayoutSetDisplayName(inputMethodSubtype);
//        if (layoutKeyboardName == null || SubtypeLocaleUtils.QWERTY.equalsIgnoreCase(layoutKeyboardName)) {
//            layoutKeyboardName = "";
//        }
////        languageEntity.displayName = SubtypeLocaleUtils.getSubtypeLocaleDisplayName(inputMethodSubtype.getLocale()) + " " + layoutKeyboardName;
//        languageEntity.setDisplayName(inputMethodSubtype.getDisplayName(App.getInstance(), App.getInstance().getPackageName(), App.getInstance()
//                .getApplicationInfo()).toString());
//        languageEntity.getDisplayName().concat(layoutKeyboardName);
//        languageEntity.setLocale(inputMethodSubtype.getLocale());
//        languageEntity.setExtraValues(inputMethodSubtype.getExtraValue());
//        languageEntity.setIconRes(inputMethodSubtype.getIconResId());
//        languageEntity.setNameRes(inputMethodSubtype.getNameResId());
//        languageEntity.setEnabled(false);
//        languageEntity.setAscii(inputMethodSubtype.isAsciiCapable());
//        languageEntity.setAuxiliary(inputMethodSubtype.isAuxiliary());
//        languageEntity.setOverrideEnable(inputMethodSubtype.overridesImplicitlyEnabledSubtype());
//        languageEntity.setPrefSubtype(AdditionalSubtypeUtils.getPrefSubtype(inputMethodSubtype));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            languageEntity.setSubtypeTag(inputMethodSubtype.getLanguageTag());
//        } else {
//            languageEntity.setSubtypeTag("");
//        }
//        languageEntity.setSubtypeId(inputMethodSubtype.hashCode());
//        languageEntity.setSubtypeMode(inputMethodSubtype.getMode());
//        return languageEntity;
//    }

    public static InputMethodInfo getInfoThisIme() {
        InputMethodInfo thisImeInfo = null;
        final InputMethodManager imm = (InputMethodManager) App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        for (final InputMethodInfo imi : imm.getInputMethodList()) {
            if (imi.getPackageName().equals(App.getInstance().getPackageName())) {
                thisImeInfo = imi;
                break;
            }
        }
        return thisImeInfo;
    }

    public static LanguageEntity convertIMSubtypeToLanguageEntity(InputMethodSubtype inputMethodSubtype) {
        LanguageEntity languageEntity = new LanguageEntity();
        languageEntity.name = SubtypeLocaleUtils.getKeyboardLayoutSetName(inputMethodSubtype);
        String layoutKeyboardName = SubtypeLocaleUtils.getKeyboardLayoutSetDisplayName(inputMethodSubtype);
        if (layoutKeyboardName == null || SubtypeLocaleUtils.QWERTY.equalsIgnoreCase(layoutKeyboardName)) {
            layoutKeyboardName = "";
        }
        languageEntity.displayName = inputMethodSubtype.getDisplayName(App.getInstance(), App.getInstance().getPackageName(), App.getInstance().getApplicationInfo()).toString();
        languageEntity.displayName.concat(layoutKeyboardName);
        languageEntity.locale = inputMethodSubtype.getLocale();
        languageEntity.extraValues = inputMethodSubtype.getExtraValue();
        languageEntity.iconRes = inputMethodSubtype.getIconResId();
        languageEntity.nameRes = inputMethodSubtype.getNameResId();
        languageEntity.isEnabled = false;
        languageEntity.isAscii = inputMethodSubtype.isAsciiCapable();
        languageEntity.isAuxiliary = inputMethodSubtype.isAuxiliary();
        languageEntity.overrideEnable = inputMethodSubtype.overridesImplicitlyEnabledSubtype();
        languageEntity.prefSubtype = AdditionalSubtypeUtils.getPrefSubtype(inputMethodSubtype);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            languageEntity.subtypeTag = inputMethodSubtype.getLanguageTag();
        } else {
            languageEntity.subtypeTag = "";
        }
        languageEntity.subtypeId = inputMethodSubtype.hashCode();
        languageEntity.subtypeMode = inputMethodSubtype.getMode();
        return languageEntity;
    }

    public static void setEnableDefaultSystem(ArrayList<LanguageEntity> languageEntities) {
        if (languageEntities == null || languageEntities.isEmpty()) {
            return;
        }
        boolean isDefault = false;
        HashMap<String, Boolean> mapStringLanguage = new HashMap<>();
        HashMap<String, Integer> mapStringLanguageContain = new HashMap<>();
        ArrayList<Locale> locales = new ArrayList<>();
        ArrayList<String> stringsLocale = App.getInstance().keyboardLanguageRepository.mLocaleSystemArray;
        boolean hasEnLocal = false;
        for (String localString : stringsLocale) {
            Locale locale = constructLocaleFromString(localString);
            locales.add(locale);
            if (localString.contains(Constant.LOCALE_DEFAULT)) {
                hasEnLocal = true;
            }
        }
        for (int i = 0; i < languageEntities.size(); i++) {
            LanguageEntity languageEntity = languageEntities.get(i);
            languageEntity.isEnabled = false;
            int indexLocale = 0;
            for (Locale locale : locales) {
                String languageFull = locale.toString();
                String language = locale.getLanguage();
                if (!languageFull.isEmpty()) {
                    if (languageEntity.locale.equals(languageFull)) {
                        Log.d("duongcv", "setEnableDefaultSystem: true" + languageFull);
                        languageEntity.isEnabled = true;
                        languageEntity.indexList = indexLocale;
                        mapStringLanguage.put(locale.getLanguage(), true);
                        languageEntities.set(i, languageEntity);
                    } else {
                        if (!language.isEmpty() && languageEntity.locale.contains(language)) {
                            mapStringLanguageContain.put(language, i);
                        }
                    }
                }
                indexLocale++;
            }
            if (!hasEnLocal && languageEntity.locale.equals(Constant.LOCALE_LANGUAGE_DEFAULT)) {
                languageEntity.isEnabled = true;
                languageEntities.set(i, languageEntity);
                mapStringLanguage.put(languageEntity.locale, true);
            }

        }
        if (mapStringLanguageContain.size() > 0) {
            Object[] arrayKey = mapStringLanguageContain.keySet().toArray();
            for (int j = 0; j < arrayKey.length; j++) {
                if (!mapStringLanguage.containsKey(arrayKey[j])) {
                    int index = mapStringLanguageContain.get(arrayKey[j]);
                    languageEntities.get(index).isEnabled = true;
                    mapStringLanguage.put(languageEntities.get(index).locale, true);
                }
            }
        }
        if (mapStringLanguage.size() > 0) {
            isDefault = true;
        }

        if (!isDefault) { // todo: no language enable
            int index = -1;
            // todo: try set locale default
            for (int i = 0; i < languageEntities.size(); i++) {
                LanguageEntity languageEntity = languageEntities.get(i);
                languageEntity.isEnabled = false;
                if (languageEntity.locale.equals(Constant.LOCALE_LANGUAGE_DEFAULT) || languageEntity.locale.contains(Constant.LOCALE_DEFAULT)) {
                    index = i;
                    languageEntity.isEnabled = true;
                    languageEntities.set(i, languageEntity);
                }
            }
            if (index == -1) {
                index = 0;
                LanguageEntity languageEntity = languageEntities.get(index);
                languageEntity.isEnabled = true;
                languageEntities.set(index, languageEntity);
            }
            // todo: no locale default found, set default 0

        }
        // todo: move all enabled to up
        int length = languageEntities.size();
        Log.d("duongcv", "setEnableDefaultSystem: ");
//        App.getInstance().keyboardLanguageRepository.setDefaultLanguageFirstTime();
        int i = length - 1;
        int d = 0;
        while (i >= d) {
            LanguageEntity languageEntity = languageEntities.get(i);
            if (languageEntity.isEnabled) {
                languageEntities.remove(i);
                languageEntities.add(0, languageEntity);
                d++;
            } else {
                i--;
            }
        }
    }

    public static InputMethodSubtype[] getAdditionalSubtypes(Context context) {
        // When we are called from the Settings application but we are not already running, some
        // singleton and utility classes may not have been initialized.  We have to call
        // initialization method of these classes here. See {@link LatinIME#onCreate()}.
        final SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);

        SubtypeLocaleUtils.init(context);

        final Resources res = context.getResources();
        String prefAdditionalSubtypes = Settings.readPrefAdditionalSubtypes(prefs, res);
        if (!prefs.getBoolean(Constant.PREF_UPDATE_PRE_SUBTYPE_FROM_6_0_15, false)) {
            final String predefinedPrefSubtypes = AdditionalSubtypeUtils.createPrefSubtypes(
                    res.getStringArray(R.array.predefined_subtypes));
            final String[] prefSubtypeArray = predefinedPrefSubtypes.split(";");
            for (String s : prefSubtypeArray) {
                if (!prefAdditionalSubtypes.contains(s)) {
                    prefAdditionalSubtypes = prefAdditionalSubtypes.concat(";");
                    prefAdditionalSubtypes = prefAdditionalSubtypes.concat(s);
                }
            }
            Settings.writePrefAdditionalSubtypes(prefs, prefAdditionalSubtypes);
            prefs.edit().putBoolean(Constant.PREF_UPDATE_PRE_SUBTYPE_FROM_6_0_15, true).apply();
        }
        return AdditionalSubtypeUtils.createAdditionalSubtypesArray(prefAdditionalSubtypes);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getWidthString(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width();
    }
//    public static Drawable getImageFromAsset(Context context, String strNameImage) {
//        Drawable imgAsset = null;
//
//        InputStream ims;
//        try {
//            if (PreferenceColorKeyboard.readString(context, PreferenceColorKeyboard.PATH_THEME_DOWNLOADED, "").equals("")) {
//                ims = context.getAssets().open(strNameImage);
//            } else {
//                File file = new File(strNameImage);
//                ims = new FileInputStream(file);
//            }
//            // load image as Drawable
//
//            imgAsset = Drawable.createFromStream(ims, null);
//        } catch (IOException e) {
//            //e.printStackTrace();
//        }
//        if (imgAsset instanceof BitmapDrawable) {
//            return imgAsset;
//        }
//        return (NinePatchDrawable) imgAsset;
//
//    }

    public static ArrayList<String> loadListImageFromFolderAsset(Context context, String folder) {
        AssetManager asset_manager = context.getAssets();
        ArrayList<String> listImage = new ArrayList<>();
        try {
            String[] files = asset_manager.list(folder);
            if (files != null && files.length > 0) {
                for (String path : files) {
                    listImage.add(Constant.FOLDER_ASSET + folder + "/" + path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listImage;
    }

    public static int manipulateColor(int color, float factor, int alpha) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(alpha,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isNetworkOnline1(Context context) {
        boolean isOnline = false;
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());  // need ACCESS_NETWORK_STATE permission
            isOnline = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOnline;
    }

    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        // It's a dark color
        return !(darkness < 0.5); // It's a light color
    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public static int getContrastColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.rgb(255 - r, 255 - g, 255 - b);
    }

    public static void appendText(Font font, CharSequence[] sp, StringBuilder c, String textNeedChange, int indexStartAppend) {
        if (textNeedChange == null) {
            return;
        }
        for (int i = indexStartAppend; i < textNeedChange.length(); i++) {
            if (String.valueOf(textNeedChange.charAt(i)).equals(" ")) {
                c.append(" ");
            } else {
                int index = font.getIndex(String.valueOf(textNeedChange.charAt(i)), false);
                if (sp != null) {
                    if (index == 100) {
                        if (sp.length == 53) {
                            c.append(textNeedChange.charAt(i)).append(sp[sp.length - 1].toString());
                        } else {
                            c.append(textNeedChange.charAt(i));
                        }
                    } else {
                        c.append(sp[index].toString());
                    }
                }
            }
        }
    }

    public static void appendText(Font font, CharSequence[] sp, StringBuilder c, String textNeedChange, int indexStartAppend, int indexEndAppend) {

        for (int i = indexStartAppend; i < Math.min(textNeedChange.length(), indexEndAppend); i++) {
            if (String.valueOf(textNeedChange.charAt(i)).equals(" ")) {
                c.append(" ");
            } else {
                int index = font.getIndex(String.valueOf(textNeedChange.charAt(i)), false);
                if (sp != null) {
                    if (index == 100) {
                        if (sp.length == 53) {
                            c.append(textNeedChange.charAt(i)).append(sp[sp.length - 1].toString());
                        } else {
                            c.append(textNeedChange.charAt(i));
                        }
                    } else {
                        c.append(sp[index].toString());
                    }
                }
            }
        }
    }

    public static String replaceTextFontOUTPUT(boolean isUsingLanguageKeyboardOtherQwerty, CharSequence[] CharSequenceFont, String label, String labelCurrent, Font itemFont, String keyFont) {
        isUsingLanguageKeyboardOtherQwerty = Constant.LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY.contains(Settings.getLanguageKeyBoardCurrent());
        if (isUsingLanguageKeyboardOtherQwerty) {
            // TEXT KEY NO qwertyMain
            if (CharSequenceFont.length == 53) {
                if (!SYMBOL_NOT_CHANGE_FONT.equals(label.toLowerCase()) || Settings.getLanguageKeyBoardCurrent().equals(Constant.KEYBOARD_LANGUAGE_GREEK) && SYMBOL_LANGUAGE_GREEK_CHANGE_FONT.contains(label.toLowerCase())) {
                    if (Constant.LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY_PLUS_CHARACTER_ON_THE_LEFT.contains(Settings.getLanguageKeyBoardCurrent()) || Constant.LIST_FONT_PLUS_CHARACTER_ON_THE_LEFT.contains(keyFont)) {
                        label = CharSequenceFont[CharSequenceFont.length - 1].toString() + labelCurrent;
                    } else {
                        label = labelCurrent + CharSequenceFont[CharSequenceFont.length - 1].toString();
                    }
                }
            } else {
                checkSupportFont(keyFont);
            }
        } else {
            int index;
            index = itemFont.getIndex(labelCurrent, false);
            if (index == 100) {
                if (CharSequenceFont.length == 53) {
                    if (SPECIAL_CHARACTERS_CHANGE_FONT.contains(label)) {
                        if (Constant.LIST_FONT_PLUS_CHARACTER_ON_THE_LEFT.contains(keyFont)) {
                            label = CharSequenceFont[CharSequenceFont.length - 1].toString() + labelCurrent;
                        } else {
                            label = labelCurrent + CharSequenceFont[CharSequenceFont.length - 1].toString();
                        }
                    } else {
//                        checkSupportFont(keyFont);
                    }
                } else {
                    //    checkSupportFont(keyFont);
                }
            } else {
                if (index >= 0 && index <= CharSequenceFont.length - 1) {
                    label = CharSequenceFont[index].toString();
                } else {
//                    checkSupportFont(keyFont);
                }
            }
        }
        return label;
    }

    public static void checkSupportFont(String keyFont) {
        if (!keyFont.equals(Constant.FONT_NORMAL)) {
            // customToast(App.getInstance(),App.getInstance().getResources().getString(R.string.not_support_font));
            //Toast.makeText(App.getInstance(), App.getInstance().getResources().getString(R.string.not_support_font), Toast.LENGTH_SHORT).show();
            App.getInstance().mPrefs.edit().putString(Constant.USING_FONT, Constant.FONT_NORMAL).apply();
            App.getInstance().fontRepository.updateCurrentFont();
            if (!App.getInstance().listFontNotUsed.containsKey(keyFont)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //   App.getInstance().listFontNotUsed.put(keyFont, keyFont);
                    }
                }, 150);
                Timber.d("duc checkSupportFont " + System.currentTimeMillis());
            }
            EventBus.getDefault().post(new MessageEvent(Constant.KEY_CHECK_SUPPORT_FONT));
        }
    }

    public static int printInputLanguages(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> ims = imm.getEnabledInputMethodList();
        for (InputMethodInfo method : ims) {
            if (method.getSettingsActivity() != null && method.getSettingsActivity().equals(Constant.PATH_SETTING_CUSTOM)) {
                List<InputMethodSubtype> submethods = imm.getEnabledInputMethodSubtypeList(method, true);
                if (submethods != null && submethods.size() > 0) return submethods.size();
            }
        }
        return 1;
    }

    public static void invokeSubtypeEnablerOfThisIme(Context context) {
        InputMethodManager mImm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        final InputMethodInfo imi =
                UncachedInputMethodManagerUtils.getInputMethodInfoOf(context.getPackageName(), mImm);
        if (imi == null) {
            return;
        }
        final Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(android.provider.Settings.EXTRA_INPUT_METHOD_ID, imi.getId());
        context.startActivity(intent);
    }

    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        int val = 0;
        if (s == null) return val;
        s = s.toUpperCase();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

    public static ThemeModel parserJsonFromFileTheme(Context context, String idTheme) {
        ThemeModel themeModel = null;
        // ContextWrapper contextWrapper = new ContextWrapper(context);
        //File file = contextWrapper.getDir(context.getFilesDir().getName(), Context.MODE_PRIVATE);

        try {
            Gson gson = new Gson();
            String strPath = App.getInstance().file.toString() + "/" + idTheme + "/theme.json";
            String textJson;
            File yourFile = new File(strPath);
            if (!yourFile.exists()) return null;
            InputStream inputStream = new FileInputStream(yourFile);
            StringBuilder stringBuilder = new StringBuilder();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String receiveString;
            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            textJson = stringBuilder.toString();
            try {
                themeModel = gson.fromJson(textJson, ThemeModel.class);
            } catch (IncompatibleClassChangeError ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return themeModel;

    }

    public static JSONArray getDataAssetLocal(Context context, String nameFolderAsset, String nameObjectJson) {
        String json;
        try {

            InputStream is = context.getAssets().open(nameFolderAsset);
            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            assert jsonObject != null;
            return jsonObject.getJSONArray(nameObjectJson);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static JSONObject getJSONObjectLocal(Context context, String nameFolderAsset) {
        String json;
        try {

            InputStream is = context.getAssets().open(nameFolderAsset);
            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static ThemeModel getDataAssetThemeLocal(Context context, String id) {
        try {
            String folder = "themes/" + id + "/theme.json";
            String json = loadJsonFromAsset(context, folder);
            return new Gson().fromJson(json, ThemeModel.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getImage9PathFromLocal(Context context, String strPath, boolean isScale) {
        Drawable existingDrawable =  ContextCompat.getDrawable(context, R.drawable.ic_transparent);
        try {
            if (isScale) {
                Bitmap bitmap = null;
                if (strPath.contains(Constant.FOLDER_ASSET)) {
                    InputStream istr = context.getAssets().open(strPath.substring(Constant.FOLDER_ASSET.length()));
                    bitmap = BitmapFactory.decodeStream(istr);
                } else {
                    bitmap = BitmapFactory.decodeFile(strPath);
                }
                existingDrawable = context.getDrawable(R.drawable.btn_key_text_pressed);
                if (strPath.contains(Constant.FOLDER_ASSET)) {
                    if (strPath.contains("special"))
                        existingDrawable = context.getDrawable(R.drawable.btn_key_special);
                    else if (strPath.contains("shift") || strPath.contains("delete") || strPath.contains("enter") || strPath.contains("symbol"))
                        existingDrawable = context.getDrawable(R.drawable.btn_key_shift);
                    else if (strPath.contains("2005") || strPath.contains("2009")) {
                        if (strPath.contains("btn_key_text") || strPath.contains("btn_key_language")) {
                            existingDrawable = context.getDrawable(R.drawable.btn_key_text_circle_pressed);
                        }
                    }
                }
                Rect padding = new Rect();
                if (existingDrawable != null) {
                    existingDrawable.getPadding(padding);
                }
                Bitmap existingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_key_text_pressed);
                if (strPath.contains(Constant.FOLDER_ASSET)) {
                    if (strPath.contains("special"))
                        existingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_key_special);
                    else if (strPath.contains("shift") || strPath.contains("delete") || strPath.contains("enter") || strPath.contains("symbol"))
                        existingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_key_shift);
                    else if (strPath.contains("2005") || strPath.contains("2009")) {
                        if (strPath.contains("btn_key_text") || strPath.contains("btn_key_language")) {
                            existingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_key_text_circle_pressed);
                        }
                    }
                }
                byte[] chunk = existingBitmap.getNinePatchChunk();
                return new NinePatchDrawable(context.getResources(), bitmap, chunk, padding, null);
            } else {
               return getImageFromAssetAndFile(context, strPath);
            }

        } catch (Exception e) {
            return existingDrawable;
        }
    }

    public static Drawable getImageFromAssetAndFile(Context context, String strNameImage) {

        Drawable imgAsset =null;
        InputStream ims = null;
        try {
            if (strNameImage.contains(Constant.FOLDER_ASSET)) {
                ims = context.getAssets().open(strNameImage.substring(Constant.FOLDER_ASSET.length()));
            } else {
                File file = new File(strNameImage);
                ims = new FileInputStream(file);
            }

            // load image as Drawable
            imgAsset = Drawable.createFromStream(ims, null);
        } catch (IOException e) {
            //Timber.d(e);
        } finally {
            if (ims != null) {
                try {
                    ims.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return imgAsset;

    }

    public static GradientDrawable getGradientDrawableBackground(String[] strColorArr) {
        int[] colors = new int[strColorArr.length];

        for (int i = 0; i < strColorArr.length; i++) {
            colors[i] = hex2decimal(strColorArr[i]);
        }
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                colors);
        gd.setCornerRadius(0f);
        return gd;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean checkTime() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastClickTime) < 800) {
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    public static String getPathImage(Context context, ThemeModel themeModel, Key key, String nameImage9path) {
        String folder = App.getInstance().file.toString();
        String idTheme = themeModel.getId();
        if (key != null && themeModel.getTypeKey() != Constant.TYPE_KEY_DEFAULT && !(key instanceof DynamicGridKeyboard.GridKey)) {
            File fileTheme = new File(App.getInstance().file, Objects.requireNonNull(idTheme));
            boolean useAsset = !fileTheme.exists() || Long.parseLong(idTheme) > 1000000 || App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE;

            if (useAsset) {
                long themeIdLong = Long.parseLong(idTheme);
                if ((themeIdLong > 6010 && themeIdLong < 6030) ||
                        (themeIdLong > 2003 && themeIdLong < 3000) ||
                        (themeIdLong > 3015 && themeIdLong < 4000) ||
                        (themeIdLong > 4012 && themeIdLong < 5000)) {
                    folder = Constant.FOLDER_ASSET + "themes";
                } else {
                    folder = Constant.FOLDER_ASSET + "key";
                }
            }

            idTheme = String.valueOf(themeModel.getTypeKey());
            BackgroundKey bgKey = themeModel.getBackgroundKey();

            switch (nameImage9path) {
                case "btn_key_text.png", "btn_key_text.9.png" -> {
                    nameImage9path = resolveTextKeyImage(bgKey, key.getCode(), "btn_key_text.png", "btn_key_special.png", "btn_key_symbol.png");
                }
                case "btn_key_text_pressed.png", "btn_key_text_pressed.9.png" -> {
                    nameImage9path = resolveTextKeyImage(bgKey, key.getCode(), "btn_key_text_pressed.png", "btn_key_special.png", "btn_key_symbol.png");
                }
                case "btn_key_special.png", "btn_key_special_pressed.png", "btn_key_special.9.png", "btn_key_special_pressed.9.png" -> {
                    nameImage9path = resolveSpecialKeyImage(bgKey, key.getCode());
                }
            }
        }

        return folder + "/" + idTheme + checkFileHDPI(context) + nameImage9path;
    }





    // chungvv Xử lý key thường (text, space, symbol, comma, period)
    private static String resolveTextKeyImage(BackgroundKey bgKey, int keyCode, String defaultImage, String spaceImage, String symbolImage) {
        return switch (keyCode) {
            case Constants.CODE_SPACE -> spaceImage;
            case Constants.CODE_SWITCH_ALPHA_SYMBOL ->
                    (bgKey != null && bgKey.getSymbol() != null && !bgKey.getSymbol().isEmpty()) ? bgKey.getSymbol() : symbolImage;
            case Constants.CODE_COMMA_KEY , Constants.CODE_EMOJI->
                    (bgKey != null && bgKey.getComma() != null && !bgKey.getComma().isEmpty()) ? bgKey.getComma() : defaultImage;
            case Constants.CODE_PERIOD_KEY ->
                    (bgKey != null && bgKey.getPeriod() != null && !bgKey.getPeriod().isEmpty()) ? bgKey.getPeriod() : defaultImage;
            default -> defaultImage;
        };
    }

    //chungvv Xử lý key đặc biệt (enter, delete, shift, language)
    private static String resolveSpecialKeyImage(BackgroundKey bgKey, int keyCode) {
        return switch (keyCode) {
            case Constants.CODE_LANGUAGE_SWITCH ->
                    (bgKey != null && bgKey.getLanguage() != null && !bgKey.getLanguage().isEmpty()) ? bgKey.getLanguage() : "btn_key_language.png";
            case Constants.CODE_DELETE ->
                    (bgKey != null && bgKey.getDelete() != null && !bgKey.getDelete().isEmpty()) ? bgKey.getDelete() : "btn_key_delete.png";
            case Constants.CODE_ENTER ->
                    (bgKey != null && bgKey.getEnter() != null && !bgKey.getEnter().isEmpty()) ? bgKey.getEnter() : "btn_key_enter.png";
            case Constants.CODE_SHIFT ->
                    (bgKey != null && bgKey.getShift() != null && !bgKey.getShift().isEmpty()) ? bgKey.getShift() : "btn_key_shift.png";
            default -> "btn_key_special.png";
        };
    }

    public static String checkFileHDPI(Context context) {
//        switch (context.getResources().getDisplayMetrics().densityDpi) {
//            case DisplayMetrics.DENSITY_LOW:
//            case DisplayMetrics.DENSITY_MEDIUM:
//                return "/mhdpi/";
//            case DisplayMetrics.DENSITY_260:
//            case DisplayMetrics.DENSITY_280:
//            case DisplayMetrics.DENSITY_300:
//            case DisplayMetrics.DENSITY_XHIGH:
//                return "/xhdpi/";
//            case DisplayMetrics.DENSITY_340:
//            case DisplayMetrics.DENSITY_360:
//            case DisplayMetrics.DENSITY_400:
//            case DisplayMetrics.DENSITY_420:
//            case DisplayMetrics.DENSITY_440:
//            case DisplayMetrics.DENSITY_450:
//            case DisplayMetrics.DENSITY_XXHIGH:
//                return "/xxhdpi/";
//            case DisplayMetrics.DENSITY_560:
//            case DisplayMetrics.DENSITY_XXXHIGH:
//                return "/xxxhdpi/";
//            default:
//                return "/hdpi/";
//        }
        String displayMetrics;
        final int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        if (densityDpi <= DisplayMetrics.DENSITY_180) {
            displayMetrics = "/mdpi/";
        } else if (densityDpi < DisplayMetrics.DENSITY_260) {
            displayMetrics = "/hdpi/";
        } else if (densityDpi < DisplayMetrics.DENSITY_340) {
            displayMetrics = "/xhdpi/";
        } else if (densityDpi < DisplayMetrics.DENSITY_560) {
            displayMetrics = "/xxhdpi/";
        } else {
            displayMetrics = "/xxxhdpi/";
        }
        return displayMetrics;
    }

    public static String getPathImageForPreviewPressKey(Context context, String idTheme, String nameImage9path) {
        // ContextWrapper contextWrapper = new ContextWrapper(context);
        //  File file = contextWrapper.getDir(context.getFilesDir().getName(), Context.MODE_PRIVATE);
        return App.getInstance().file.toString() + "/" + idTheme + "/anydpi/" + nameImage9path;
    }

    public static Drawable getImageFromAsset(Context context, String strNameImage) {
        Drawable imgAsset = null;

        InputStream ims;
        try {
            ims = context.getAssets().open(strNameImage);
            // load image as Drawable
            imgAsset = Drawable.createFromStream(ims, null);

        } catch (IOException e) {

            e.printStackTrace();
        }

        return imgAsset;

    }

    private static String copyFolderFromAssetToInternalStorage(Context context, String arg_assetDir, String arg_destinationDir) {
        try {
            String dest_dir_path = addLeadingSlash(arg_destinationDir);
            File dest_dir = new File(dest_dir_path);
            createDir(dest_dir);
            AssetManager asset_manager = context.getAssets();
            String[] files = asset_manager.list(arg_assetDir);
            if (files != null) {
                for (String file : files) {
                    String abs_asset_file_path = addTrailingSlash(arg_assetDir) + file;
                    String[] sub_files = asset_manager.list(abs_asset_file_path);
                    assert sub_files != null;
                    if (sub_files.length == 0) {
                        // It is a file
                        String dest_file_path = addTrailingSlash(dest_dir_path) + file;
                        copyAssetFile(context, abs_asset_file_path, dest_file_path);
                    } else {
                        // It is a sub directory
                        copyFolderFromAssetToInternalStorage(context, abs_asset_file_path, addTrailingSlash(arg_destinationDir) + file);
                    }
                }
                if (arg_destinationDir.contains(CommonVariable.PATH_TO_THEME_CUSTOM_DEFAULT) && App.getInstance().themeRepository != null
                        && App.getInstance().themeRepository.getDefaultThemeModel() == null) {
                    App.getInstance().themeRepository.loadThemeDefault();
                }
            }

            return dest_dir_path;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CommonVariable.COPY_FILE_FAIL;
    }

    public static void copyThemeFofFirstTimeOpenAppFromAssetToFile(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String strIDTheme = mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "");
        String strIDThemePhase5 = mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT_UPDATE_PHASE_5, "");
        String strIDThemeLEDDefault = mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT_UPDATE_LED_DEFAULT, "");

        //  ContextWrapper contextWrapper = new ContextWrapper(context);
        //  File file = contextWrapper.getDir(context.getFilesDir().getName(), Context.MODE_PRIVATE);
        if (strIDTheme.equalsIgnoreCase("")) {
            String arg_assetDir = CommonVariable.PATH_TO_THEME_DEFAULT_IN_ASSET;
            String arg_destinationDir = App.getInstance().file.toString() + CommonVariable.PATH_TO_THEME_DEFAULT;
            copyThemeDefaultThread(context, arg_assetDir, arg_destinationDir).subscribe(new SingleObserver<String>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onSuccess(@NonNull String s) {
                    if (s.equals(CommonVariable.COPY_FILE_FAIL)) {
                        Timber.d("duongcv update theme copy 0 false");
                        mPrefs.edit().putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "").apply();
                    } else {
                        Timber.d("duongcv update theme copy 0 success");
                        mPrefs.edit().putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, CommonVariable.ID_THEME_DEFAULT).apply();
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {

                }
            });
        }

        if (strIDThemePhase5.equalsIgnoreCase("")) {
            String arg_assetDir_custom = CommonVariable.PATH_TO_THEME_CUSTOM_IN_ASSET;
            String arg_destinationDirCustom = App.getInstance().file.toString() + CommonVariable.PATH_TO_THEME_CUSTOM_DEFAULT;
            copyThemeDefaultThread(context, arg_assetDir_custom, arg_destinationDirCustom).subscribe(new SingleObserver<String>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onSuccess(@NonNull String s) {
                    if (s.equals(CommonVariable.COPY_FILE_FAIL)) {
                        mPrefs.edit().putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT_UPDATE_PHASE_5, "").apply();
                        Timber.d("duongcv update theme copy 100 false");
                    } else {
                        Timber.d("duongcv update theme copy 100 success");
                        mPrefs.edit().putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT_UPDATE_PHASE_5, CommonVariable.PATH_TO_THEME_CUSTOM_IN_ASSET).apply();
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {

                }
            });
        }

        if (strIDThemeLEDDefault.equalsIgnoreCase("")) {
            String arg_assetDir_custom = "themes/3";
            String arg_destinationDirCustom = App.getInstance().file.toString() + "/3";
            copyThemeDefaultThread(context, arg_assetDir_custom, arg_destinationDirCustom).subscribe(new SingleObserver<String>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onSuccess(@NonNull String s) {
                    if (s.equals(CommonVariable.COPY_FILE_FAIL)) {
                        Timber.d("duongcv update theme copy 3 false");
                        mPrefs.edit().putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT_UPDATE_LED_DEFAULT, "").apply();
                    } else {
                        Timber.d("duongcv update theme copy 3 success");
                        mPrefs.edit().putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT_UPDATE_LED_DEFAULT, "themes/3").apply();
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {

                }
            });
        }
    }

    private static Single<String> copyThemeDefaultThread(Context context, String arg_assetDir, String arg_destinationDir) {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return CommonUtil.copyFolderFromAssetToInternalStorage(context, arg_assetDir, arg_destinationDir);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private static void copyAssetFile(Context context, String assetFilePath, String destinationFilePath) throws IOException {
        InputStream in = context.getAssets().open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    private static String addTrailingSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        return path;
    }

    private static String addLeadingSlash(String path) {
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return path;
    }

    private static void createDir(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Can't create directory, a file is in the way");
            }
        } else {
            dir.mkdirs();
            if (!dir.isDirectory()) {
                throw new IOException("Unable to create directory");
            }
        }
    }

    public static boolean saveThemeToInternalStorage(Context context, ResponseBody body, String fileName, String idTheme, boolean isSticker) {
        //ContextWrapper contextWrapper = new ContextWrapper(context);
        // File destinationFile = contextWrapper.getDir(context.getFilesDir().getName(), Context.MODE_PRIVATE);
        File file = new File(App.getInstance().file, fileName);
        String fileUnzip = App.getInstance().file.getAbsolutePath();
        if (isSticker) {
            File folderSticker = new File(App.getInstance().file, Constant.FOLDER_STICKER + idTheme);
            if (!folderSticker.exists()) {
                folderSticker.mkdir();
            }
            file = new File(folderSticker.getAbsoluteFile(), fileName);
            fileUnzip = folderSticker.getAbsolutePath();
        }

        try {
            InputStream inputStream = body.byteStream();
            OutputStream outputStream = new FileOutputStream(file);
            byte[] data = new byte[4096];
            int countSize;
            while ((countSize = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, countSize);

            }
            outputStream.flush();
            String pathFileZip = file.getAbsolutePath();
            boolean resultUnzip = superFastUnzip(pathFileZip, fileUnzip);
            if (!isSticker) deleteFileZip(pathFileZip, idTheme);
            else deleteFileZip(pathFileZip, Constant.FOLDER_STICKER + idTheme);
            return resultUnzip;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean superFastUnzip(String inputZipFile, String destinationDirectory) {
        try {
            int BUFFER = 2048;
            List<String> zipFiles = new ArrayList<String>();
            File sourceZipFile = new File(inputZipFile);
            File unzipDestinationDirectory = new File(destinationDirectory);
            unzipDestinationDirectory.mkdir();
            ZipFile zipFile;
            zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
            Enumeration zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(unzipDestinationDirectory, currentEntry);
                if (currentEntry.endsWith(".zip")) {
                    zipFiles.add(destFile.getAbsolutePath());
                }

                File destinationParent = destFile.getParentFile();

                destinationParent.mkdirs();

                try {
                    if (!entry.isDirectory()) {
                        BufferedInputStream is =
                                new BufferedInputStream(zipFile.getInputStream(entry));
                        int currentByte;
                        byte[] data = new byte[BUFFER];

                        FileOutputStream fos = new FileOutputStream(destFile);
                        BufferedOutputStream dest =
                                new BufferedOutputStream(fos, BUFFER);
                        while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            zipFile.close();

            for (Iterator<String> iter = zipFiles.iterator(); iter.hasNext(); ) {
                String zipName = (String) iter.next();
                unzip(zipName, destinationDirectory + File.separatorChar + zipName.substring(0, zipName.lastIndexOf(".zip"))
                );

            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void unzip(String _zipFile, String _targetLocation) {

        //create target location folder if not exist
        dirChecker(_targetLocation);

        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {

                try {
                    //checking if canonical paths to unzipped files are underneath an expected directory
                    String destDirCanonicalPath = (new File(_targetLocation)).getCanonicalPath();
                    String outputFileCanonicalPath = (new File(_zipFile)).getCanonicalPath();
                    if (!outputFileCanonicalPath.startsWith(destDirCanonicalPath)) {
                        throw new Exception(String.format("Found Zip Path Traversal Vulnerability with %s", destDirCanonicalPath));

                    } else {
                        //create dir if required while unzipping
                        if (ze.isDirectory()) {
                            dirChecker(ze.getName());
                        } else {
                            FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }

                            zin.closeEntry();
                            fout.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }


            }
            zin.close();
            if (fin != null) {
                fin.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    private static void deleteFileZip(String zipFile, String idTheme) {
        ContextWrapper contextWrapper = new ContextWrapper(App.getInstance());
        File file = contextWrapper.getDir(App.getInstance().getFilesDir().getName(), Context.MODE_PRIVATE);
        File fileTheme = new File(file.toString(), idTheme);
        if (fileTheme.exists()) {
            File deleteFileZipAfterUnzip = new File(zipFile);
            boolean deleted = deleteFileZipAfterUnzip.delete();
        }
    }

    public static int dpToPx(Context context, int dp) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dp * scale + 0.5f);
        }
        return dp;
    }

    public static void customToast(Context context, String name) {
        if (System.currentTimeMillis() - timeToast > 2000) {
            timeToast = System.currentTimeMillis();
            Rect bounds = new Rect();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //context.getLayoutInflater();
            @SuppressLint("InflateParams")
            View toastRoot = inflater.inflate(R.layout.custom_toast, null);
            TextView tv = toastRoot.findViewById(R.id.txt_toast);
            Paint textPaint = tv.getPaint();
            textPaint.getTextBounds(name, 0, name.length(), bounds);
            int width = bounds.width();
            //tv.setWidth(getScreenHeight() / 4);
            tv.setWidth((int) (width * 1.5));
            tv.setText(name);
            tv.setHeight((int) tv.getTextSize() * 4);
            Toast toast1 = new Toast(context);
            toast1.setGravity(Gravity.CENTER_HORIZONTAL, 0, getScreenHeight() / 4);
            toast1.setView(toastRoot);
            toast1.show();
            toast1.setDuration(Toast.LENGTH_SHORT);
        }
    }

    public static Spanned setBoldString(String textNotBoldFirst, String textToBold, String textNotBoldLast) {
        String resultant = textNotBoldFirst + " " + "<b>" + textToBold + "</b>" + " " + textNotBoldLast;// "<b>" set textBold
        return Html.fromHtml(resultant);
    }

    public static Spanned setBoldString2(String textNotBoldFirst, String textToBold, String textNotBoldLast) {
        //  String resultant = textNotBoldFirst + " " + "<b>" + textToBold + "</b>" + " " + textNotBoldLast;// "<b>" set textBold

        return Html.fromHtml(textNotBoldFirst + "<font color=\"#FFFFFF\">" + textToBold + "</font><br><br>"
                + textNotBoldLast);
    }

    public static boolean checkStickerExist(Sticker sticker) {
        ContextWrapper contextWrapper = new ContextWrapper(App.getInstance());
        File destinationFile = contextWrapper.getDir(App.getInstance().getFilesDir().getName(), Context.MODE_PRIVATE);
        File folderSticker = new File(destinationFile, Constant.FOLDER_STICKER + sticker.getId() + "/" + sticker.getId() + "/" + sticker.getId());
        File fileThumb = new File(destinationFile, Constant.FOLDER_STICKER + sticker.getId() + "/" + sticker.getId() + "/thumb.png");
        if (folderSticker.exists() && folderSticker.listFiles() != null && Objects.requireNonNull(folderSticker.listFiles()).length > 0 && fileThumb.exists()) {
            return true;
        } else {
            File folder = new File(destinationFile, Constant.FOLDER_STICKER + sticker.getId());
            if (folder.exists()) folder.delete();
            return false;
        }
    }

    public static void downloadBackgroundToStorage(Context context, String pathUrlDown, String idBackground, IResultDownBackground iResultDownBackground) {
        Glide.with(context)
                .asBitmap()
                .load(pathUrlDown)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Timber.d("ducNQ : saveImaged: 3");
                        iResultDownBackground.onDownBackgroundError();
                        App.getInstance().checkDownloadBg = true;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (App.getInstance().getConnectivityStatus() != -1) {

                            saveImage(context, resource, idBackground, iResultDownBackground);
                        }
                        return false;
                    }

                }).submit();
    }

    public static String saveBackgroundKeyboard(Bitmap bm, String folders, boolean check) {
        String dataBg = "";
        try {
            File getFilesDir = App.getInstance().getApplicationContext().getFilesDir();
            File storageDirCustomize = new File(getFilesDir, Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE);
            File storageDir = new File(getFilesDir, Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE_BACKGROUND);
            String root = App.getInstance().appDir.toString() + folders + "/";
            File folder = new File(root);
            App.getInstance().pathFolderBgCurrent = folder.getPath();
            if (!folder.exists()) folder.mkdirs();
            String bgName = "";
            if (check) {
                bgName = System.currentTimeMillis() + ".jpg";
            } else {
                bgName = App.getInstance().idPath/* App.getInstance().idBgCurrent*/ + "a.jpg";
            }
            File file = new File(folder, bgName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            dataBg = file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataBg;
    }

    public static void saveImage(Context context, Bitmap image, String idBackground, IResultDownBackground iResultDownBackground) {
        String savedImagePath = "";
        File getFilesDir = context.getFilesDir();
        File storageDirCustomize = new File(getFilesDir, Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE);
        File storageDir = new File(getFilesDir, Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE_BACKGROUND);
        boolean success = true;
        if (!storageDirCustomize.exists()) storageDirCustomize.mkdirs();
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, idBackground);
            if (imageFile.exists()) imageFile.delete();
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
                if (App.getInstance().checkDownloadBg) {
                    Timber.e("Duongcv delete preview");
                    App.getInstance().checkDownloadBg = false;
                    if (imageFile.exists()) imageFile.delete();
                }
                iResultDownBackground.onDownBackgroundSuccess();
            } catch (Exception e) {
                Timber.d("ducNQ : saveImaged: 1");
                iResultDownBackground.onDownBackgroundError();
                e.printStackTrace();
            }
        } else {
            Timber.d("ducNQ : saveImaged: 2");
            iResultDownBackground.onDownBackgroundError();
        }
    }

    public static boolean checkBackgroundThemeDownloaded(String idBackground) {
        File getFilesDir = App.getInstance().getFilesDir();
        File storageDir = new File(getFilesDir, Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE_BACKGROUND);
        File imageFile = new File(storageDir, idBackground);
        return imageFile.exists();
    }

    public static void hideSoftInput(Activity activity) {
        hideSoftInput(activity.getWindow());
    }

    public static void hideSoftInput(@androidx.annotation.NonNull final Window window) {
        View view = window.getCurrentFocus();
        if (view == null) {
            View decorView = window.getDecorView();
            View focusView = decorView.findViewWithTag("keyboardTagView");
            if (focusView == null) {
                view = new EditText(window.getContext());
                view.setTag("keyboardTagView");
                ((ViewGroup) decorView).addView(view, 0, 0);
            } else {
                view = focusView;
            }
            view.requestFocus();
        }
        hideSoftInput(view);
    }

    public static void hideSoftInput(@androidx.annotation.NonNull final View view) {
        InputMethodManager imm =
                (InputMethodManager) App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean checkPermissionStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(App.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static Drawable getDrawableEffectClick(Context context, String keyEffectPopup) {
        Drawable drawableEffectClick;
        try {
            if (keyEffectPopup.equals(Constant.ID_NONE)) {
                //drawableEffectClick = ContextCompat.getDrawable(context, R.drawable.ic_transparent);
                drawableEffectClick = null;
            } else {
                String nameFile = "effect/" + keyEffectPopup + "/thumbnail.png";
                if (currentEffect.containsKey(nameFile) && currentEffect.get(nameFile) != null) {
                    drawableEffectClick = currentEffect.get(nameFile);
                } else {
                    InputStream ims = context.getAssets().open(nameFile);
                    // set image to ImageView
                    drawableEffectClick = Drawable.createFromStream(ims, null);

                    currentEffect.clear();
                    currentEffect.put(nameFile, drawableEffectClick);
                }
            }
        } catch (Exception e) {
            drawableEffectClick = ContextCompat.getDrawable(context, R.drawable.ic_transparent);
            e.printStackTrace();
        }
        return drawableEffectClick;
    }

    public static void intentMain(Context context, int action) {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(Constant.KEY_OPEN_SCREEN, action);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }

    public static Bitmap blurBitmap(Context ctx, Bitmap image, int radius) {
        if (image == null) {
            return null;
        }
        if (radius == 0) {
            return image;
        }
        try {
//            int width = Math.round(image.getWidth());
//            int height = Math.round(image.getHeight());
//            if (width > 1080) {
//                height = height * 1080 / width;
//                width = 1080;
//            }
            Bitmap outputBitmap = Toolkit.INSTANCE.blur(image, radius);
            return outputBitmap;
        } catch (Exception | OutOfMemoryError exception) {
            return image;
        }
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);

            istr.close();
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    public static Drawable getDrawable(Context context, int typeKey, String strPath, Key key) {
        try {
            InputStream bitmapStream = null;
            bitmapStream = context.getAssets().open("key/" + typeKey + checkFileHDPI(context) + strPath);
            Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream);

            // String path = Objects.requireNonNull(App.getInstance().file).toString() + "/" + typeKey + checkFileHDPI(context) + strPath;
            if (key.getWidth() > 0 && key.getHeight() > 0)
                bitmap = Bitmap.createScaledBitmap(bitmap, key.getWidth(), key.getHeight(), true);
            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getDrawableNew(Context context, long typeKey, String strPath, Key key, int keyCode) {
        try {
            InputStream bitmapStream = null;
            // bitmapStream = context.getAssets().open("key/" + typeKey + checkFileHDPI(context) + strPath);
            String path = Objects.requireNonNull(App.getInstance().file).toString() + "/" + typeKey + checkFileHDPI(context) + strPath;
            File file = new File(path);
            if (!file.exists()) {
                if (keyCode == Constants.CODE_DELETE)
                    return getDrawableDelete(context, typeKey, strPath, key);
                else
                    return context.getResources().getDrawable(R.drawable.sym_keyboard_shift_holo_dark, null);
            } else {
                bitmapStream = new FileInputStream(path);
                Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream);
                if (key.getWidth() > 0 && key.getHeight() > 0)
                    bitmap = Bitmap.createBitmap(bitmap);//Bitmap.createScaledBitmap(bitmap, key.getWidth(), key.getHeight(), true);
                return new BitmapDrawable(context.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //file:///android_asset/key/6006/bg1.jpg
    public static Drawable getDrawableThemeFeatured(Context context, int typeKey, String strPath, Key key) {
        try {
            InputStream bitmapStream = null;
            bitmapStream = context.getAssets().open("key/" + typeKey + checkFileHDPI(context) + strPath);
            Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream);

            if (key.getWidth() > 0 && key.getHeight() > 0)
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) (key.getWidth() / 2.5), key.getHeight() / 2, true);
            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Drawable getDrawableDelete(Context context, long typeKey, String strPath, Key key) {
        try {
            InputStream bitmapStream = null;
            bitmapStream = context.getAssets().open("themes/" + typeKey + checkFileHDPI(context) + strPath);
            Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream);
            if (key.getWidth() > 0 && key.getHeight() > 0)
                bitmap = Bitmap.createBitmap(bitmap);
            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            return context.getResources().getDrawable(R.drawable.sym_keyboard_delete_holo_dark, null);
        }
    }

    public static void policy(Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(context.getResources().getString(R.string.policy_url_rgb)));
        if (browserIntent.resolveActivity(App.getInstance().getPackageManager()) != null) {
            context.startActivity(browserIntent);
        } else {
            if (System.currentTimeMillis() - timeToast > 2000) {
                Toast.makeText(App.getInstance(), context.getResources().getString(R.string.no_app), Toast.LENGTH_SHORT).show();
                timeToast = System.currentTimeMillis();
            }
        }
    }

    public static boolean delayShowAdsInterstitial() {
        long delayShowAds = FirebaseRemoteConfig.getInstance().getLong(com.tapbi.spark.yokey.common.Constant.DELAY_SHOW_ADS_INTERSTITIAL_FIRST);
        Log.e("ZomjKeyboard", "delayShowAdsInterstitial " + delayShowAds);
        if (delayShowAds != 0L) {
            return true;
        }
        return false;
    }

    public static boolean disableShowAdsOpenForeground() {
        long disableShowAds = FirebaseRemoteConfig.getInstance().getLong(com.tapbi.spark.yokey.common.Constant.DISABLE_SHOW_ADS_OPEN_FOREGROUND);
        Log.e("ZomjKeyboard", "disableShowAdsOpenForeground " + disableShowAds);
        if (disableShowAds != 0L) {
            return true;
        }
        return false;
    }

    public static boolean disableShowAdsNativeActiveKeyboard() {
        long disableShowAds = FirebaseRemoteConfig.getInstance().getLong(com.tapbi.spark.yokey.common.Constant.DISABLE_ADS_NATIVE_ACTIVE_KEYBOARD);
        Log.e("ZomjKeyboard", "disableShowAdsOpenForeground " + disableShowAds);
        if (disableShowAds != 0L) {
            return true;
        }
        return false;
    }

    public static long positionAdsNativeDetail() {
        long position = FirebaseRemoteConfig.getInstance().getLong(com.tapbi.spark.yokey.common.Constant.POSITION_ADS_NATIVE_DETAIL);
        Log.e("ZomjKeyboard", "positionAdsNativeDetail " + position);
        return position;
    }

    public static long marginAdsNativeDetail() {
        long margin = FirebaseRemoteConfig.getInstance().getLong(com.tapbi.spark.yokey.common.Constant.MARGIN_ADS_NATIVE_DETAIL);
        Log.e("ZomjKeyboard", "marginAdsNativeDetail " + margin);
        return margin;
    }

    public String loadDataJsonAssets(Context context, String inFile) {
        try {
            InputStream stream = context.getAssets().open(inFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            return new String(buffer);
        } catch (Exception e) {
            return "";
        }
    }


}
