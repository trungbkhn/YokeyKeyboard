package com.tapbi.spark.yokey.util;


import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.App;

import java.util.Arrays;
import java.util.List;

public class Constant {

    public static final int KEY_CHANGE_THEME = 1;
    public static final int ID_STORE = 11;
    public static final int ID_GALLERY = 111;
    public static final String HEIGHT_VIEW_VERTICAL = "HEIGHT_VIEW_VERTICAL";
    public static final String HEIGHT_VIEW_HORIZONTAL = "HEIGHT_VIEW_HORIZONTAL";
    public static final int KEY_CHANGE_THEME_NOT_SHOW_PREVIEW = 2;
    public static final int CONNECT_INTERNET = 4;
    public static final int DISCONNECT_INTERNET = 5;
    public static final String CODE_LANGUAGE_OUT_PUT = "CODE_LANGUAGE_OUT_PUT";
    public static final String CODE_LANGUAGE_IN_PUT = "CODE_LANGUAGE_IN_PUT";
    public static final int SEND_DATA_FONT = 6;
    public static final int TYPE_LANGUAGE_IN_PUT = 404;
    public static final int TYPE_LANGUAGE_OUT_PUT = 402;
    public static final int KEY_CHANGE_FONT = 7;

    public static final int EVENT_LOAD_LANGUAGE_DB = 10;

    public static final int EVENT_CHANGE_LANGUAGE = 53;

    public static final String LANGUAGE_CURRENT_KEY_BOARD ="LANGUAGE_CURRENT";

    public static final int MESSAGE_INIT_CONFIG = 1;


    //EventBus
    public static final int ACTION_CHANGE_BACKGROUND_CUSTOMZIE = 1;
    public static final int ACTION_CHANGE_BLUR_BACKGROUND_CUSTOMZIE = 2;
    public static final int ACTION_CHANGE_EFFECT_CUSTOMZIE = 3;
    public static final int ACTION_CHANGE_SOUND_CUSTOMZIE = 4;
    public static final int ACTION_CHANGE_COLOR_ICON_MENU_CUSTOMZIE = 5;
    public static final int ACTION_CHANGE_COLOR_ICON_TEXT_CUSTOMZIE = 6;
    public static final int ACTION_CHANGE_TYPE_KEY_CUSTOMZIE = 7;
    public static final int ACTION_CHANGE_STATE_ACTIVATE_KEYBOARD = 8;

    public static final int EVENT_DATA_SYMBOLS = 9;
    public static final int EVENT_DATA_STICKER_RECENT = 10;
    public static final int EVENT_CHANGE_LAYOUT_EMOJI = 11;

    public static final int EVENT_SHOW_CLIPBOARD_VIEW_KEYBOARD = 12;
    public static final int EVENT_CHANGE_LIST_FONT = 13;

    public static final int EVENT_SEND_STICKER = 14;
    public static final int EVENT_UPDATE_THEME_KEYBOARD = 15;
    public static final int EVENT_ADD_FONT = 17;

    public static final int EVENT_CANCEL_SEARCH_GIF = 20;


    public static final int KEY_SCREEN_THEME = 18;
    public static final int KEY_SCREEN_FONT = 19;
    public static final int KEY_SCREEN_LANGUAGE = 21;
    public static final int KEY_SCREEN_MORE = 22;
    public static final int KEY_CHANGE_STICKER = 23;
    public static final int KEY_SCREEN_STICKER = 24;
    public static final int KEY_SCREEN_EMOJIS = 25;
    public static final int KEY_CHANGE_STICKER_NOT_SHOW_PREVIEW = 26;
    public static final int KEY_CHECK_SUPPORT_FONT = 27;
    public static final int KEY_MAX_LENGTH_TRANSLATE = 28;
    public static final int KEY_SCREEN_MYTHEME = 29;
    public static final int REQUEST_PERMISSION_GALLERY = 30;

    public static final int EVENT_GOTO_MAIN = 31;
    public static final int EVENT_EMOJI = 32;
    public static final int REQUEST_PERMISSION_SAVE = 33;

    public static final int EVENT_TRANSLATE = 101;
    public static final int EVENT_SHOW_MENU = 102;
    public static final int EVENT_SHOW_POLICY = 103;

    public static final int EVENT_STICKER = 111;
    public static final int EVENT_SELECTION = 121;
    public static final int CHECK_EDIT_TEXT = 200;



    public static final String EVENT_SHOW_HIDE_KEYBOARD = "event_show_hide_keyboard";
    public static final String CHECK_SELECT_THEME_DEFAULT = "CHECK_SELECT_THEME_DEFAULT";

    public static final int MIN_DURATION_BETWEEN_CLICK = 800;


    public static final String LOCALE_CURRENT_SUBTYPE_ID = "LOCALE_CURRENT_SUBTYPE_ID";
    public static final String IS_UPGRADE_LANGUAGE = "IS_UPGRADE_LANGUAGE_6_1_16";
    public static final String IS_FIRST_INIT_LANGUAGE = "is_first_init_language";
    public static final String LOCALE_LANGUAGE_DEFAULT = "en_US";
    public static final String LOCALE_DEFAULT = "en";
    public static final String TYPE_KEY = "TYPE_KEY";
    public static final String BLUR_KILLAPP = "BLUR_KILLAPP";
    public static final String TYPE_EDITING = "TYPE_EDITING";
    public static final String COLOR_TEXT_KILLAPP = "COLOR_TEXT_KILLAPP";
    public static final String COLOR_ICON_MENU_BAR_KILLAPP = "COLOR_ICON_MENU_BAR_KILLAPP";
    public static final String EFFECT_KEY_KILLAPP = "EFFECT_KEY_KILLAPP";
    public static final String SOUND_KEY_KILLAPP = "SOUND_KEY_KILLAPP";
    public static final String CHECK_TYPE_KEY_CURRENT = "CHECK_TYPE_KEY_CURRENT";
    public static final String CHECK_FOCUS_KEY_TEXT_COLOR = "CHECK_FOCUS_KEY_TEXT_COLOR";
    public static final String CHECK_FIRST_TIME_SETBG = "CHECK_FIRST_TIME_SETBG";
    public static final String KEY_BOARD = "key_board";
    public static final String ROOT_URL_TRANSLATE = "https://www.google.com";

    public static final String ID_SCREEN_CURRENT = "id_screen_current";
    public static final String DATA_STICKER_ONKEYBOARD = "data_sticker_onkeyboard";
    public static final String FIX_BUG_DUPLICATED_FONT_DOUBLE = "fix_bug_duplicated_font_double";
    public static final String FIX_BUG_DUPLICATED_FONT_DOUBLE_V2 = "FIX_BUG_DUPLICATED_FONT_DOUBLE_V2";
    public static final String IS_CHECK_SUPPORT_FONT = "is_check_support_font";

    public static final String PREF_UPDATE_PRE_SUBTYPE_FROM_6_0_15 = "update_pre_subtype_from_6_0_15";
    public static final String IS_USE_SYSTEM_LANGUAGE = "IS_USE_SYSTEM_LANGUAGE";

    public static final String LOCALE_CURRENT_LANGUAGE = "locale_current_language";
    public static final String PREF_SYSTEM_LOCALE = "system_locale_strings";

    public static final String INPUT_TYPE_VIETNAMESE = "input_type_vietnamese";
    public static final String INPUT_TYPE_QWERTY_VIETNAMESE = "input_type_qwerty_vietnamese";
    public static final String INPUT_TYPE_VIETNAMESE_VNI = "input_type_vietnamese_vni";
    public static final String INPUT_TYPE_VIETNAMESE_TELEX = "input_type_vietnamese_telex";
    public static final String INPUT_TYPE_VIETNAMESE_TELEX_SIMPLE = "input_type_vietnamese_telex_simple";
    public static final String INPUT_TYPE_VIETNAMESE_DEFAULT = INPUT_TYPE_VIETNAMESE_TELEX;

    //update new font
    public static final String FONT_BLOCKS = "Font blocks";
    public static final String FONT_GREEK = "Font greek";
    public static final String FONT_ANCHOR = "Font anchor";
    public static final String FONT_COUNTRY_CODE = "Font country code";
    public static final String FONT_WIGGLY = "Font wiggly";
    public static final String FONT_CRISS_CROSS = "Font criss cross";
    public static final String FONT_GLITCH = "Font glitch";
    public static final String FONT_SOVIET = "Font soviet";
    public static final String FONT_CURVY_1 = "Font curvy 1";
    public static final String FONT_ROCK_DOTS = "Font rock dots";
    public static final String FONT_STROKED_2 = "Font stroked 2";
    public static final String FONT_SUBSCRIPT = "Font subscript";
    public static final String FONT_CENSORED = "Font censored";
    public static final String FONT_DELTA = "Font delta";
    public static final String FONT_LEFT_HANDED = "Font left handed";
    public static final String FONT_SHAKY = "Font shaky";
    public static final String FONT_STINGY = "Font stingy";
    public static final String FONT_HIEROGLYPH = "Font hieroglyph";
    public static final String FONT_ORIENTAL = "Font oriental";
    public static final String FONT_FANCEE = "Font fancee";
    public static final String FONT_CHESS = "Font chess";
    public static final String FONT_FAHRENHEIT = "Font fahrenheit";
    public static final String FONT_HOURGLASS = "Font hourglass";

    public static final String CHECK_UPDATE_FONT_DATA = "check_update_font_data";
    public static final String CHECK_UPDATE_NEW_FONT_DATA = "check_update_new_font_data";
    public static final String KEY_OPEN_SCREEN = "key_open_screen";
    public static final String FOLDER_STICKER = "folderSticker";
    public static final String CHECK_LOAD_ADS = "CHECK_LOAD_ADS";

    public static final String HIDE_RATE_APPS = "hide_rate_app";
    public static final String FONT_UNDERLINE = "Font underline";
    public static final String FONT_TYPE_WRITE = "Font type write";
    public static final String FONT_TINY = "Font tiny";
    public static final String FONT_STICKE_THROUGH = "Font sticke through";
    public static final String FONT_STOP = "Font stop";
    public static final String FONT_STINKY = "Font stinky";
    public static final String FONT_SQUARES_OUTLINE = "Font squares outline";
    public static final String FONT_SQUARES_FILLED = "Font squares filled";
    public static final String FONT_SQUARE_DASHED = "Font square dashed";
    public static final String FONT_SLASH = "Font slash";
    public static final String FONT_SKY_LINE = "Font sky line";
    public static final String FONT_SERIF_ITALIC = "Font serif italic";
    public static final String FONT_SERIF_BOLD_ITALIC = "Font serif bold italic";
    public static final String FONT_SERIF_BOLD = "Font serif bold";
    public static final String FONT_SCRIPT_BOLD = "Font script bold";
    public static final String FONT_SCRIPT = "Font script";
    public static final String FONT_SAD = "Font sad";
    public static final String FONT_RAY = "Font ray";
    public static final String FONT_OUTLINE = "Font outline";
    public static final String FONT_NORMAL = "Font normal";
    public static final String FONT_MANGA = "Font manga";
    public static final String FONT_HAPPY = "Font happy";
    public static final String FONT_GOTHIC_BOLD = "Font gothic bold";
    public static final String FONT_GOTHIC = "Font gothic";
    public static final String FONT_CLOUDS = "Font clouds";
    public static final String FONT_CIRCLES_OUTLINE = "Font circle outline";
    public static final String FONT_CIRCLES_FILLED = "Font circle filled";
    public static final String FONT_BUBBLES = "Font bubbles";
    public static final String FONT_BIRDS = "Font birds";
    public static final String FONT_ARROWS = "Font arrows";
    public static final String FONT_COMIC = "Font comic";
    public static final String FONT_EMOJI1 = "Font emoji";
    public static final String FONT_EMOJI2 = "Font emojii";
    public static final String FONT_EMOJI3 = "Font emojiji";
    public static final String FONT_EMOJI4 = "Font emojijii";
    public static final String FONT_SANS = "Font sans";
    public static final String FONT_SANS_BOLD = "Font sans bold";
    public static final String FONT_SANS_BOLD_ITALIC = "Font sans bold italic";
    public static final String FONT_SANS_ITALIC = "Font sans italic";
    public static final String FONT_UPSIDE_DOWN = "Font upside down";

    public static final String FONT_RUNS = "Font runs";
    public static final String STYLE_KEY_BG_FILL = "style_fill";
    public static final float THRESHOLD_COLOR_CHANGE = 0.35f;

    public static final String FONT_ANCIENT = "Font ancient";
    public static final String FONT_NIGMATIC = "Font nigmatic";
    public static final String FONT_EMOJI5 = "Font emoji 5";
    public static final String FONT_HIGH_LIGHTS = "Font high lights";
    public static final String FONT_CURVY_TAIL = "Font curvy tail";
    public static final String FONT_MYTHOLOGY = "Font mythology";
    public static final String FONT_SHALASY = "Font salashy";
    public static final String FONT_RAILS = "Font rails";
    public static final String FONT_COMIC_FUN = "Font comic fun";
    public static final String FONT_DOTIFY = "Font dotify";
    public static final String FONT_WIDE_SPACE = "Font wide space";
    public static final String FONT_RUSCRIPT = "Font ruscript";
    public static final String FONT_RSUMNEZ = "Font rsumnez";
    public static final String FONT_STRIKE_THROUGH = "Font strike through";
    public static final String FONT_BRALMY = "Font bralmy";
    public static final String FONT_MODER_NOPHICS = "Font moder nophics";
    public static final String FONT_RETRO_TYPE = "Font retro type";
    public static final String FONT_TINY_WINGS = "Font tiny wings";
    public static final String FONT_GOLDY = "Font goldy";
    public static final String FONT_HZSOA = "Font hzsoa";
    public static final String FONT_NOT_CHIFY = "Font not chify";
    public static final String FONT_HAMP_SHIRE = "Font hamp shire";
    public static final String FONT_SOULURGE = "Font soulurge";
    public static final String FONT_BOXIFY = "Font boxify";
    public static final String FONT_RUFF_ROAD = "Font ruff road";
    public static final String FONT_BRACKETS = "Font brackets";
    public static final String FONT_SUNSHINE = "Font sunshine";
    public static final String FONT_EMPIRE = "Font empire";
    public static final String FONT_DEMONS = "Font demons";
    public static final String FONT_UNDER_COVER = "Font under cover";
    public static final String FONT_GO_RIGHT = "Font go right";
    public static final String FONT_GO_LEFT = "Font go left";
    public static final String FONT_POP_STAR = "Font pop star";
    public static final String FONT_SHINY = "Font shiny";
    public static final String FONT_SEASHORE = "Font seashore";
    public static final String FONT_TINY_MATE = "Font tiny mate";
    public static final String FONT_PONY_TAIL = "Font pony tail";
    public static final String FONT_WHEEL = "Font wheel";
    public static final String FONT_POSTER = "Font poster";


    public static final String FONT_UPDERLINE = "Font upderline";
    public static final String FONT_UPPERLINE = "Font upperline";
    public static final String FONT_HIGHT_LIGHTS2 = "Font hight lights2";
    public static final String FONT_HORROR_MUSIC = "Font horror music";
    public static final String FONT_CLOUDY = "Font greek cloudy";
    public static final String FONT_MANIAC = "Font greek maniac";
    public static final String FONT_METHODOLOGY = "Font methodology";
    public static final String FONT_FANTASY = "Font greek fantasy";
    public static final String FONT_EPICURIOUS = "Font epicurious";
    public static final String FONT_MAGICAL = "Font magical";
    public static final String FONT_FAIRY_TALES = "Font fairy tales";

    public static final String FONT_ALAFABIA = "Font alafabia";
    public static final String FONT_AVENGER = "Font avenger";
    public static final String FONT_WICHOLOGY = "Font wichology";
    public static final String FONT_CURLS = "Font curls";
    public static final String FONT_SPARKLE = "Font sparkle";
    public static final String FONT_UPSIDE = "Font upside";
    public static final String FONT_BLACK_CHODE = "Font black chode";
    public static final String FONT_HUNGARIAN = "Font hungarian";
    public static final String FONT_WAY_COOL = "Font way cool";
    public static final String FONT_SA_MUENZ = "Font sa muenz";
    public static final String FONT_RAM_TRACK = "Font ram track";

    public static final String FONT_STAMP = "Font stamp";
    public static final String FONT_PEE_WEE = "Font pee wee";
    public static final String FONT_IM_NINJA = "Font im ninja";
    public static final String FONT_BLUE_EYES = "Font blue eyes";
    public static final String FONT_MON_TEY = "Font mon tey";
    public static final String FONT_SWORD_LINER = "Font sword liner";
    public static final String FONT_THIS_HULA = "Font this hula";
    public static final String FONT_FLIPPER = "Font flipper";
    public static final String FONT_INFINITY = "Font greek bets";
    public static final String FONT_GIORGIO_LOGY = "Font giorgio logy";
    public static final String FONT_KUNG_FU = "Font kung fu";

    public static final String FONT_HAPPY_FACE = "Font happy face";
    public static final String FONT_ROMTNUM = "Font romtnum";
    public static final String FONT_SANS_SKRIT = "Font sans skrit";
    public static final String FONT_DOUBLE_LINE = "Font double line";
    public static final String FONT_ANGER = "Font anger";
    public static final String FONT_MOGLI = "Font mogli";
    public static final String FONT_DRAGON = "Font dragon";
    public static final String FONT_TINY_CAPS = "Font tiny caps";
    public static final String FONT_SAMBA_WAYS = "Font samba ways";
    public static final String FONT_JAKAS = "Font jakas";
    public static final String FONT_NWOD_EDIT_PU = "Font nmod edit pu";

    public static final String FONT_ROUND_STAMP = "Font round stamp";
    public static final String FONT_HIGHLIGHT_ME = "Font highlight me";
    public static final String FONT_QUESTION = "Font question";
    public static final String FONT_BRIDGE = "Font bridge";
    public static final String FONT_HOTSPOT = "Font hotspot";
    public static final String FONT_FROZEN = "Font frozen";
    public static final String FONT_BUDDHA = "Font buddha";
    public static final String FONT_PERSHIAN = "Font pershian";
    public static final String FONT_EMPIRE_AGC = "Font empire agc";
    public static final String FONT_SWAGO_LOGY = "Font swago logy";
    public static final String FONT_TYPEWRITER = "Font type writer";

    public static final String FONT_DOUBLER_TRUCK = "Font double truck";
    public static final String FONT_FRACTURE = "Font fracture";
    public static final String FONT_FRACTURE_BOLD = "Font fracture bold";
    public static final String FONT_RUSSIAN = "Font russian";
    public static final String FONT_RUNES = "Font runes";
    public static final String FONT_DOTS = "Font dots";
    public static final String FONT_STROKED = "Font stroked";


    //font update version 5.1
    public static final String FONT_BRAILLE = "Font braille";
    public static final String FONT_HAT = "Font hat";
    public static final String FONT_TILDE = "Font tilde";
    public static final String FONT_UNDERLINE_2 = "Font underline 2";
    public static final String FONT_FRAME = "Font frame";
    public static final String FONT_MOSAIQ = "Font mosaiq";
    public static final String FONT_IN_LOVE = "Font in love";
    public static final String FONT_LINES = "Font lines";
    public static final String FONT_SHARDS = "Font shards";
    public static final String FONT_DOUBLE_CURLY = "Font double curly";
    public static final String FONT_THIN_A = "Font thin a";
    public static final String FONT_ANGUI = "Font angui";
    public static final String FONT_DOUBLE = "Font double";
    public static final String FONT_BRACE = "Font brace";
    public static final String FONT_SEPARATION = "Font separation";
    public static final String FONT_SEPARATION_SQUIGGLY = "Font separation squilly";
    public static final String FONT_PIPE_SUP_RIGHT = "Font pipe sup right";
    public static final String FONT_PIPE_SUP_LEFT = "Font pipe sup left";
    public static final String FONT_THIN_DOWN_LEFT = "Font thin down left";
    public static final String FONT_THIN_DOWN_RIGHT = "Font thin down right";
    public static final String FONT_THIN_UP_LEFT = "Font thin up left";
    public static final String FONT_THIN_UP_RIGHT = "Font thin up right";
    public static final String FONT_ROUND = "Font round";
    public static final String FONT_LONG = "Font long";
    public static final String FONT_STARS = "Font stars";
    public static final String FONT_ANGULAR = "Font angular";
    public static final String FONT_SPECIAL = "Font special";
    public static final String FONT_DOT_RIGHT = "Font dot right";
    public static final String FONT_DOT_LEFT = "Font dot left";
    public static final String FONT_DOT = "Font dot";
    public static final String FONT_TRIANGULAR = "Font triangular";
    public static final String FONT_TRIANGULAR_RIGHT = "Font triangular right";
    public static final String FONT_TRIANGULAR_LEFT = "Font triangular left";

    public static final String FONT_SEPARATION_THIN = "Font separation thin";
    public static final String FONT_SEPARATION_THIN_LEFT = "Font separation thin left";
    public static final String FONT_SEPARATION_THIN_RIGHT = "Font separation thin right";
    public static final String FONT_DUST = "Font dust";


    public static final String KEYBOARD_LANGUAGE_ENGLISH = "en";
    public static final String KEYBOARD_LANGUAGE_RUSSIAN = "ru";

    public static final String KEYBOARD_LANGUAGE_ARABIC = "ar";
    public static final String KEYBOARD_LANGUAGE_PERSIAN = "fa";
    public static final String KEYBOARD_LANGUAGE_SERBIAN = "sr";
    public static final String KEYBOARD_LANGUAGE_TAMIL = "ta";
    public static final String KEYBOARD_LANGUAGE_TELUGU_INDIA = "te_IN";
    public static final String KEYBOARD_LANGUAGE_THAI = "th";
    public static final String KEYBOARD_LANGUAGE_UKRAINIAN = "uk";
    public static final String KEYBOARD_LANGUAGE_ARMENIAN = "hy";
    public static final String KEYBOARD_LANGUAGE_BANGLA_BANGLADESH = "bn";
    public static final String KEYBOARD_LANGUAGE_BELARUSIAN_BELARUS = "be";
    public static final String KEYBOARD_LANGUAGE_BULGARIAN = "bg";
    public static final String KEYBOARD_LANGUAGE_GEORGIAN = "ka";
    public static final String KEYBOARD_LANGUAGE_GREEK = "el";
    public static final String KEYBOARD_LANGUAGE_HEBREW = "iw";
    public static final String KEYBOARD_LANGUAGE_HINDI = "hi";
    public static final String KEYBOARD_LANGUAGE_KANNADA = "kn";
    public static final String KEYBOARD_LANGUAGE_KAZAKH = "kk";
    public static final String KEYBOARD_LANGUAGE_KHMER = "km";
    public static final String KEYBOARD_LANGUAGE_KYRGYZ = "ky";
    public static final String KEYBOARD_LANGUAGE_LAOS = "lo";
    public static final String KEYBOARD_LANGUAGE_MACEDONIAN = "mk";
    public static final String KEYBOARD_LANGUAGE_MALAYALAM = "ml";
    public static final String KEYBOARD_LANGUAGE_MARATHI = "mr";
    public static final String KEYBOARD_LANGUAGE_MONGOLIAN = "mn";
    public static final String KEYBOARD_LANGUAGE_NEPALI_TRADI = "ne";
    public static final String KEYBOARD_LANGUAGE_NEPALI = "ne_NP";
    public static final String KEYBOARD_LANGUAGE_CATALAN = "ca";
    public static final String KEYBOARD_LANGUAGE_ESTONIA = "et";
    public static final String KEYBOARD_LANGUAGE_GALICIAN = "gl";
    public static final String KEYBOARD_LANGUAGE_ITALY = "it";
    public static final String KEYBOARD_LANGUAGE_NAUY = "nb";
    public static final String KEYBOARD_LANGUAGE_PHILIPPINES = "fil";
    public static final String KEYBOARD_LANGUAGE_BASQUE = "eu";
    public static final String KEYBOARD_LANGUAGE_DANISH = "da";
    public static final String KEYBOARD_LANGUAGE_ESPERANTO = "eo";
    public static final String KEYBOARD_LANGUAGE_FINNISH = "fi";
    public static final String KEYBOARD_LANGUAGE_SWITZERLAND = "fr";
    public static final String KEYBOARD_LANGUAGE_GERMAN = "de";
    public static final String KEYBOARD_LANGUAGE_ITALIAN = "it";
    public static final String KEYBOARD_LANGUAGE_NORWEGIAN = "nb";
    public static final String KEYBOARD_LANGUAGE_SERBIAN_LATIN = "sr_zz";
    public static final String KEYBOARD_LANGUAGE_SPANISH = "es";
    public static final String KEYBOARD_LANGUAGE_SWEDISH = "sv";
    public static final String KEYBOARD_LANGUAGE_TELUGU = "te";
    public static final String KEYBOARD_LANGUAGE_UZBEK = "uz";
    public static final String KEYBOARD_LANGUAGE_TAGALOG_PHILIP = "tl";
    public static final String KEYBOARD_LANGUAGE_VN = "vi";

    public static final String KEY_TAB_ALL = "All";
    public static final String KEY_TAB_SANS = "Sans serif";
    public static final String KEY_TAB_SERIF = "Serif";
    public static final String KEY_TAB_DISPLAY = "Display";
    public static final String KEY_TAB_HAND = "Handwritten";
    public static final String KEY_TAB_SCRIPT = "Script";
    public static final String KEY_TAB_TIKTOK = "Tik Tok";
    public static final String KEY_TAB_INS = "Instagram";
    public static final String KEY_TAB_OTHER = "Other";
    public static final String DATA_FONT = "data_font";
    public static final String DATA_FONT_ADD = "data_font_add";
    public static final String DATA_BUNDLE = "data_bundle";
    public static final String USING_FONT = "using_font";
    public static final String USING_FONT_RECENT = "using_font_recent";

    public static final int SHOW_VIEW_CHANGE_SIZE_KB = 234;
    public static final int HIDE_VIEW_CHANGE_SIZE_KB = 222;
    public static final String CONTENT_CLIPBOARD = "Content_clipboard";
    public static final String FOLDER_ASSET = "file:///android_asset/";
    public static final int TYPE_EDIT_NONE = 1;
    public static final int TYPE_EDIT_CUSTOMIZE = 2;
    public static final String ID_THEME_DEFAULT = "100";
    public static final String ID_THEME_LED_DEFAULT = "3";

    public static final String EFFECT_FOCUS_KILLAPP = "EFFECT_FOCUS_KILLAPP";
    public static final String PATH_FILE_UN_ZIP_THEME_CUSTOMIZE_BACKGROUND = "/customize/background";
    public static final String PATH_FILE_DOWNLOADED_BACKGROUND = "/downloaded/background/";
    public static final String PATH_FILE_UN_ZIP_THEME_CUSTOMIZE = "/customize";
    public static final String DATA_CHANGE_BACKGROUND_CUSTOMZIE = "data_change_background_customize";
    public static final String DATA_CHANGE_BLUR_BACKGROUND_CUSTOMZIE = "data_change_blur_background_customize";
    public static final String DATA_CHANGE_EFFECT_CUSTOMZIE = "data_change_effect_customize";
    public static final String DATA_CHANGE_SOUND_CUSTOMZIE = "data_change_sound_customize";
    public static final String DATA_CHANGE_COLOR_ICON_MENU_CUSTOMZIE = "data_change_color_icon_menu_customize";
    public static final String DATA_CHANGE_COLOR_ICON_TEXT_CUSTOMZIE = "data_change_color_icon_text_customize";
    public static final String DATA_CHANGE_TYPE_KEY_CUSTOMZIE = "data_change_type_key_customize";
    public static final String ID_NONE = "none";
    public static final String NAME_FILE_AUDIO_ASSETS_EDIT = "name_file_audio_asset_edit";
    public static final String AUDIO_DEFAULT = "audio_default";
    public static final int TYPE_KEY_DEFAULT = 0;
    public static final int TYPE_KEY_2006 = 2006;
    public static final String DATA_THEMEMODEL = "data_thememodel";
    public static final String TYPE_SYMBOLS_DECORATIVETEXT = "type_symbols_decorativetext";
    public static final String TYPE_SYMBOLS_EMOJI = "type_symbols_emoji";
    public static final String CHECK_ADD_DATA_SYMBOLS = "check_add_data_symbols";
    public static final String DATA_SYMBOLS = "data_symbols";
    public static final String DATA_SYMBOLS_EMOJI = "data_symbols_emoji";
    public static final String DATA_SYMBOLS_DECORATIVE = "data_symbols_decorative";
    public static final String DATA_STICKER_RECENT = "data_sticker_recent";
    public static final String PATH_CURRENT_BG = "PATH_CURRENT_BG";
    public static final int CHECK_DOUBLE_CLICK = 212;

    public static final int TYPE_EMOJI = 0;
    public static final int TYPE_GIF = 1;
    public static final int TYPE_SYMBOLS = 2;
    public static final int TYPE_STICKER = 3;
    public static final String LINK_CURRENT = "LINK_CURRENT";
    public static final int KEYBOARD_VIEW_TYPE_KEY = 0;
    public static final int KEYBOARD_VIEW_TYPE_EMOJI = 3;

    public static final int HEADER_KEYBOARD_TYPE_MENU = 0;
    public static final int HEADER_KEYBOARD_TYPE_SUGGESTION = 1;
    public static final int HEADER_KEYBOARD_TYPE_SEARCH_GIF = 2;


    public static final String TITLE_INFOR_PREMIUM = "title_infor_premium";
    public static final String DES_INFOR_PREMIUM = "des_infor_premium";
    public static final String CHECK_KILLAPP_EMOJI = "CHECK_KILLAPP_EMOJI";





    public static final String PATH_SETTING_CUSTOM = "com.android.inputmethod.latin.settings.SettingsActivity";

    public static List<String> LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY
            = Arrays.asList(KEYBOARD_LANGUAGE_RUSSIAN, KEYBOARD_LANGUAGE_ARABIC,
            KEYBOARD_LANGUAGE_PERSIAN,
            KEYBOARD_LANGUAGE_SERBIAN,
            KEYBOARD_LANGUAGE_TAMIL,
            KEYBOARD_LANGUAGE_TELUGU_INDIA,
            KEYBOARD_LANGUAGE_THAI,
            KEYBOARD_LANGUAGE_UKRAINIAN,
            KEYBOARD_LANGUAGE_ARMENIAN,
            KEYBOARD_LANGUAGE_BANGLA_BANGLADESH,
            KEYBOARD_LANGUAGE_BELARUSIAN_BELARUS,
            KEYBOARD_LANGUAGE_BULGARIAN,
            KEYBOARD_LANGUAGE_GEORGIAN,
            KEYBOARD_LANGUAGE_GREEK,
            KEYBOARD_LANGUAGE_HEBREW,
            KEYBOARD_LANGUAGE_HINDI,
            KEYBOARD_LANGUAGE_KANNADA,
            KEYBOARD_LANGUAGE_KAZAKH,
            KEYBOARD_LANGUAGE_KHMER,
            KEYBOARD_LANGUAGE_KYRGYZ,
            KEYBOARD_LANGUAGE_LAOS,
            KEYBOARD_LANGUAGE_MACEDONIAN,
            KEYBOARD_LANGUAGE_MALAYALAM,
            KEYBOARD_LANGUAGE_MARATHI,
            KEYBOARD_LANGUAGE_MONGOLIAN,
            KEYBOARD_LANGUAGE_NEPALI_TRADI,
            KEYBOARD_LANGUAGE_NEPALI,
            KEYBOARD_LANGUAGE_CATALAN,
            KEYBOARD_LANGUAGE_ESTONIA,
            KEYBOARD_LANGUAGE_GALICIAN,
            KEYBOARD_LANGUAGE_ITALY,
            KEYBOARD_LANGUAGE_NAUY,
            KEYBOARD_LANGUAGE_PHILIPPINES,
            KEYBOARD_LANGUAGE_BASQUE,
            KEYBOARD_LANGUAGE_DANISH,
            KEYBOARD_LANGUAGE_ESPERANTO,
            KEYBOARD_LANGUAGE_FINNISH,
            KEYBOARD_LANGUAGE_SWITZERLAND,
            KEYBOARD_LANGUAGE_GERMAN,
            KEYBOARD_LANGUAGE_ITALIAN,
            KEYBOARD_LANGUAGE_NORWEGIAN,
            KEYBOARD_LANGUAGE_SERBIAN_LATIN,
            KEYBOARD_LANGUAGE_SPANISH,
            KEYBOARD_LANGUAGE_SWEDISH,
            KEYBOARD_LANGUAGE_TELUGU,
            KEYBOARD_LANGUAGE_UZBEK,
            KEYBOARD_LANGUAGE_TAGALOG_PHILIP,
            KEYBOARD_LANGUAGE_VN
    );

    public static List<String> LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY_PLUS_CHARACTER_ON_THE_LEFT = Arrays.asList(KEYBOARD_LANGUAGE_ARABIC,
            KEYBOARD_LANGUAGE_PERSIAN,
            KEYBOARD_LANGUAGE_HEBREW
    );
    public static final String LANGUAGE_ENG = "en";
    public static final String LANGUAGE_JP = "ja";
    public static final String LANGUAGE_KO = "ko";
    public static final String LANGUAGE_VIE = "vi";
    public static final String STATE = "st";
    public static final List<String> LIST_FONT_PLUS_CHARACTER_ON_THE_LEFT = Arrays.asList(
            FONT_SEPARATION_THIN_LEFT, FONT_TRIANGULAR_LEFT, FONT_DOT_LEFT);

    public static String[][] LIST_LANGUAGE = new String[][]{
            new String[]{"", App.getInstance().getResources().getString(R.string.langauge_detection), "", "", ""},
            new String[]{"af", App.getInstance().getResources().getString(R.string.afrikaans), "", "south_africa", "🇿🇦"},
            new String[]{"sq", App.getInstance().getResources().getString(R.string.albanian), "", "albania", "🇦🇱"},
            new String[]{"ar", App.getInstance().getResources().getString(R.string.arabic), "ar-SA", "saudi_arabia", "🇦🇪"},
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

    };


