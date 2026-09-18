package com.example.colorosfontprobe;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class MainActivity extends Activity {

    private TextView log, preview;
    private File fontFile;

    private final String[] ACTIONS = {
            "com.nearme.themespace.SET_FONT",
            "com.oplus.themestore.basic.action.SET_FONT",
            "com.oplus.themespace.partner.basic.action.SET_FONT",
            "com.oplus.themestore.action.SET_FONT_INDIVIDUATION",
            "com.heytap.themestore.action.SET_FONT_INDIVIDUATION"
    };

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        fontFile = new File(getFilesDir(), "probe-font.ttf");

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        int pad = dp(18);
        box.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("ColorOS Font Probe 0.2");
        title.setTextSize(24);
        box.addView(title);

        TextView desc = new TextView(this);
        desc.setText(
                "ColorOS/Android FontManager의 실제 폰트 업데이트 인터페이스를 검사합니다.\n" +
                "루트/Shizuku 없이 먼저 직접 호출 가능 여부를 확인합니다."
        );
        desc.setPadding(0, dp(8), 0, dp(12));
        box.addView(desc);

        Button pick = button("1. TTF / OTF 선택");
        pick.setOnClickListener(v -> pickFont());
        box.addView(pick);

        preview = new TextView(this);
        preview.setText(
                "가나다라마바사 아자차카타파하\n" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" +
                "abcdefghijklmnopqrstuvwxyz 0123456789"
        );
        preview.setTextSize(23);
        preview.setPadding(0, dp(15), 0, dp(15));
        box.addView(preview);

        Button scan = button("2. ColorOS 환경 검사");
        scan.setOnClickListener(v -> scan());
        box.addView(scan);

        Button inspect = button("3. FontManager API 상세 검사");
        inspect.setOnClickListener(v -> inspectFontApi());
        box.addView(inspect);

        Button apply = button("4. FontManager 직접 적용 테스트");
        apply.setOnClickListener(v -> testFontManagerApply());
        box.addView(apply);

        Button theme = button("5. ThemeStore SET_FONT 테스트");
        theme.setOnClickListener(v -> testThemeStore());
        box.addView(theme);

        Button copy = button("로그 클립보드 복사");
        copy.setOnClickListener(v -> {
            android.content.ClipboardManager cm =
                    (android.content.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);

            cm.setPrimaryClip(
                    ClipData.newPlainText(
                            "ColorOS Font Probe",
                            log.getText()
                    )
            );

            Toast.makeText(
                    this,
                    "로그 복사됨",
                    Toast.LENGTH_SHORT
            ).show();
        });

        box.addView(copy);

        log = new TextView(this);
        log.setTextSize(11);
        log.setTextIsSelectable(true);
        log.setPadding(0, dp(15), 0, dp(40));

        box.addView(log);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);

        setContentView(scroll);

        scan();
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        return b;
    }

    private int dp(int value) {
        return (int)
                (value *
                        getResources()
                                .getDisplayMetrics()
                                .density + 0.5f);
    }

    private void out(String text) {
        log.append(text + "\n");
    }

    /* ============================================================
       FONT PICKER
       ============================================================ */

    private void pickFont() {

        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        i.setType("*/*");

        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        i.putExtra(
                Intent.EXTRA_MIME_TYPES,
                new String[]{
                        "font/ttf",
                        "font/otf",
                        "application/x-font-ttf",
                        "application/x-font-opentype",
                        "application/octet-stream"
                }
        );

        startActivityForResult(i, 100);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != 100 ||
                resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {
            return;
        }

        try {

            InputStream in =
                    getContentResolver()
                            .openInputStream(
                                    data.getData()
                            );

            OutputStream os =
                    new FileOutputStream(
                            fontFile
                    );

            byte[] buffer =
                    new byte[65536];

            int n;

            while ((n = in.read(buffer)) > 0) {
                os.write(buffer, 0, n);
            }

            in.close();
            os.close();

            Typeface tf =
                    Typeface.createFromFile(
                            fontFile
                    );

            preview.setTypeface(tf);

            out("");
            out("=== FONT SELECTED ===");
            out("path=" +
                    fontFile.getAbsolutePath());

            out("size=" +
                    fontFile.length());

            out("readable=" +
                    fontFile.canRead());

        } catch (Throwable t) {

            out(
                    "[FONT ERROR]\n" +
                            stack(t)
            );
        }
    }

    /* ============================================================
       BASIC SCAN
       ============================================================ */

    private void scan() {

        if (log != null)
            log.setText("");

        out("=== DEVICE ===");

        out("manufacturer=" +
                Build.MANUFACTURER);

        out("brand=" +
                Build.BRAND);

        out("model=" +
                Build.MODEL);

        out("sdk=" +
                Build.VERSION.SDK_INT);

        out("release=" +
                Build.VERSION.RELEASE);

        out("font_variation_settings=" +
                Settings.System.getString(
                        getContentResolver(),
                        "font_variation_settings"
                ));

        out("");

        out("=== FONT FILE ===");

        out("selected=" +
                fontFile.exists());

        out("path=" +
                fontFile.getAbsolutePath());

        out("size=" +
                (fontFile.exists()
                        ? fontFile.length()
                        : 0));

        out("");

        out("=== PACKAGES ===");

        checkPackage(
                "com.oplus.themestore"
        );

        checkPackage(
                "com.heytap.themestore"
        );

        out("");

        out("=== SYSTEM SERVICES ===");

        try {

            Object font =
                    getSystemService("font");

            out("font=" +
                    String.valueOf(font));

            if (font != null) {
                out("font.class=" +
                        font.getClass()
                                .getName());
            }

        } catch (Throwable t) {

            out(
                    "font ERROR=" +
                            root(t)
            );
        }

        probeBinder("font");
        probeBinder("opluscustomize");
        probeBinder("theme");
        probeBinder("oplustheme");
    }

    private void checkPackage(String name) {

        try {

            PackageInfo p =
                    getPackageManager()
                            .getPackageInfo(
                                    name,
                                    0
                            );

            out(name +
                    " = INSTALLED " +
                    p.versionName);

        } catch (Throwable t) {

            out(name +
                    " = missing");
        }
    }

    private void probeBinder(String name) {

        try {

            Class<?> sm =
                    Class.forName(
                            "android.os.ServiceManager"
                    );

            Method get =
                    sm.getDeclaredMethod(
                            "getService",
                            String.class
                    );

            get.setAccessible(true);

            Object binder =
                    get.invoke(
                            null,
                            name
                    );

            out(
                    "ServiceManager[" +
                            name +
                            "]=" +
                            String.valueOf(binder)
            );

        } catch (Throwable t) {

            out(
                    "ServiceManager[" +
                            name +
                            "] ERROR=" +
                            root(t)
            );
        }
    }

    /* ============================================================
       FONT API INSPECTION
       ============================================================ */

    private void inspectFontApi() {

        out("");
        out("================================");
        out("FONT API INSPECTION");
        out("================================");

        inspectClass(
                "android.graphics.fonts.FontManager"
        );

        inspectClass(
                "android.graphics.fonts.FontFamilyUpdateRequest"
        );

        inspectClass(
                "android.graphics.fonts.FontFamilyUpdateRequest$Builder"
        );

        inspectClass(
                "android.graphics.fonts.FontFamilyUpdateRequest$FontFamily"
        );

        inspectClass(
                "android.graphics.fonts.FontFamilyUpdateRequest$FontFamily$Builder"
        );

        inspectClass(
                "android.graphics.fonts.FontUpdateRequest"
        );

        inspectClass(
                "android.graphics.fonts.FontUpdateRequest$Family"
        );

        inspectClass(
                "android.graphics.fonts.FontUpdateRequest$Font"
        );

        inspectClass(
                "android.graphics.fonts.Font"
        );

        inspectClass(
                "android.graphics.fonts.Font$Builder"
        );
    }

    private void inspectClass(String name) {

        out("");
        out("CLASS " + name);

        try {

            Class<?> c =
                    Class.forName(name);

            out(
                    "FOUND modifiers=" +
                            Modifier.toString(
                                    c.getModifiers()
                            )
            );

            out("-- constructors --");

            Constructor<?>[] constructors =
                    c.getDeclaredConstructors();

            for (Constructor<?> constructor :
                    constructors) {

                out(
                        "  " +
                                constructor.toGenericString()
                );
            }

            out("-- methods --");

            Method[] methods =
                    c.getDeclaredMethods();

            for (Method method :
                    methods) {

                out(
                        "  " +
                                method.toGenericString()
                );
            }

            out("-- fields --");

            Field[] fields =
                    c.getDeclaredFields();

            for (Field field :
                    fields) {

                out(
                        "  " +
                                Modifier.toString(
                                        field.getModifiers()
                                ) +
                                " " +
                                field.getType().getName() +
                                " " +
                                field.getName()
                );

                if (Modifier.isStatic(
                        field.getModifiers())) {

                    try {

                        field.setAccessible(true);

                        Object value =
                                field.get(null);

                        out(
                                "      value=" +
                                        String.valueOf(value)
                        );

                    } catch (Throwable ignored) {
                    }
                }
            }

        } catch (Throwable t) {

            out(
                    "NOT FOUND / ERROR: " +
                            root(t)
            );
        }
    }

    /* ============================================================
       REAL FONT MANAGER CALL
       ============================================================ */

    private void testFontManagerApply() {

        out("");
        out("================================");
        out("FONT MANAGER DIRECT TEST");
        out("================================");

        if (!fontFile.exists()) {

            out(
                    "ERROR: 먼저 TTF/OTF를 선택하세요."
            );

            return;
        }

        try {

            Object manager =
                    getSystemService("font");

            if (manager == null) {

                out(
                        "ERROR: font service unavailable"
                );

                return;
            }

            out(
                    "manager=" +
                            manager.getClass()
                                    .getName()
            );

            Method target = null;

            for (Method method :
                    manager.getClass()
                            .getMethods()) {

                if (method.getName()
                        .equals(
                                "updateFontFamily"
                        )) {

                    target = method;

                    break;
                }
            }

            if (target == null) {

                out(
                        "updateFontFamily NOT FOUND"
                );

                return;
            }

            out(
                    "TARGET METHOD:"
            );

            out(
                    target.toGenericString()
            );

            Class<?>[] params =
                    target.getParameterTypes();

            if (params.length != 2) {

                out(
                        "Unexpected parameter count=" +
                                params.length
                );

                return;
            }

            Class<?> requestClass =
                    params[0];

            out(
                    "requestClass=" +
                            requestClass.getName()
            );

            Object request =
                    tryCreateRequest(
                            requestClass
                    );

            if (request == null) {

                out("");
                out(
                        "REQUEST CREATION FAILED"
                );

                out(
                        "위의 'FontManager API 상세 검사'를 먼저 눌러"
                );

                out(
                        "FontFamilyUpdateRequest 생성자/Builder 구조를 확인해야 합니다."
                );

                return;
            }

            out(
                    "request=" +
                            request
            );

            int[] candidates =
                    new int[]{
                            0,
                            -1,
                            1
                    };

            for (int baseVersion :
                    candidates) {

                out("");
                out(
                        "CALL updateFontFamily(request, " +
                                baseVersion +
                                ")"
                );

                try {

                    Object result =
                            target.invoke(
                                    manager,
                                    request,
                                    baseVersion
                            );

                    out(
                            "RESULT=" +
                                    String.valueOf(
                                            result
                                    )
                    );

                    out(
                            "*** CALL ACCEPTED ***"
                    );

                    break;

                } catch (Throwable t) {

                    out(
                            "CALL FAILED:"
                    );

                    out(
                            stack(
                                    unwrap(t)
                            )
                    );
                }
            }

        } catch (Throwable t) {

            out(
                    "DIRECT TEST FATAL:"
            );

            out(
                    stack(
                            unwrap(t)
                    )
            );
        }
    }

    /*
     * 0.2에서는 우선 빈 요청 또는 Builder 기반 요청을 만들어
     * 서비스까지 호출 가능한지 검사한다.
     *
     * 상세 검사 결과에 따라 다음 버전에서 실제 TTF Font 객체를
     * 요청에 삽입한다.
     */
    private Object tryCreateRequest(
            Class<?> requestClass) {

        out("");
        out(
                "Trying request construction..."
        );

        try {

            Constructor<?>[] constructors =
                    requestClass
                            .getDeclaredConstructors();

            for (Constructor<?> constructor :
                    constructors) {

                out(
                        "constructor: " +
                                constructor
                                        .toGenericString()
                );

                Class<?>[] types =
                        constructor
                                .getParameterTypes();

                try {

                    Object[] args =
                            createDummyArgs(types);

                    constructor
                            .setAccessible(true);

                    Object request =
                            constructor
                                    .newInstance(args);

                    out(
                            "constructor SUCCESS"
                    );

                    return request;

                } catch (Throwable t) {

                    out(
                            "constructor failed: " +
                                    root(
                                            unwrap(t)
                                    )
                    );
                }
            }

        } catch (Throwable t) {

            out(
                    "constructor scan error=" +
                            root(t)
            );
        }

        try {

            String builderName =
                    requestClass.getName() +
                            "$Builder";

            Class<?> builderClass =
                    Class.forName(
                            builderName
                    );

            out(
                    "Builder FOUND: " +
                            builderName
            );

            for (Constructor<?> constructor :
                    builderClass
                            .getDeclaredConstructors()) {

                try {

                    Object[] args =
                            createDummyArgs(
                                    constructor
                                            .getParameterTypes()
                            );

                    constructor
                            .setAccessible(true);

                    Object builder =
                            constructor
                                    .newInstance(args);

                    out(
                            "Builder created"
                    );

                    for (Method method :
                            builderClass
                                    .getDeclaredMethods()) {

                        if (method.getName()
                                .equals("build") &&
                                method.getParameterCount()
                                        == 0) {

                            method.setAccessible(true);

                            Object request =
                                    method.invoke(
                                            builder
                                    );

                            out(
                                    "Builder.build SUCCESS"
                            );

                            return request;
                        }
                    }

                } catch (Throwable t) {

                    out(
                            "Builder attempt failed: " +
                                    root(
                                            unwrap(t)
                                    )
                    );
                }
            }

        } catch (Throwable t) {

            out(
                    "Builder unavailable: " +
                            root(t)
            );
        }

        return null;
    }

    private Object[] createDummyArgs(
            Class<?>[] types) {

        Object[] args =
                new Object[
                        types.length
                        ];

        for (int i = 0;
             i < types.length;
             i++) {

            Class<?> t =
                    types[i];

            if (t == int.class)
                args[i] = 0;

            else if (t == long.class)
                args[i] = 0L;

            else if (t == boolean.class)
                args[i] = false;

            else if (t == String.class)
                args[i] = "ColorOSFontProbe";

            else if (List.class
                    .isAssignableFrom(t))
                args[i] =
                        new ArrayList<>();

            else if (Set.class
                    .isAssignableFrom(t))
                args[i] =
                        new HashSet<>();

            else if (Map.class
                    .isAssignableFrom(t))
                args[i] =
                        new HashMap<>();

            else
                args[i] = null;
        }

        return args;
    }

    /* ============================================================
       THEME STORE TEST
       ============================================================ */

    private void testThemeStore() {

        out("");
        out("================================");
        out("THEME STORE TEST");
        out("================================");

        for (String action :
                ACTIONS) {

            try {

                Intent probe =
                        new Intent(action);

                ResolveInfo r =
                        getPackageManager()
                                .resolveActivity(
                                        probe,
                                        0
                                );

                if (r == null) {

                    out(
                            action +
                                    " -> NONE"
                    );

                    continue;
                }

                out(
                        action +
                                " -> " +
                                r.activityInfo.packageName +
                                "/" +
                                r.activityInfo.name
                );

                Intent launch =
                        new Intent(action);

                launch.setClassName(
                        r.activityInfo.packageName,
                        r.activityInfo.name
                );

                launch.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                if (fontFile.exists()) {

                    launch.putExtra(
                            "font_path",
                            fontFile.getAbsolutePath()
                    );

                    launch.putExtra(
                            "fontPath",
                            fontFile.getAbsolutePath()
                    );

                    launch.putExtra(
                            "file_path",
                            fontFile.getAbsolutePath()
                    );

                    launch.putExtra(
                            "ttf_file_path",
                            fontFile.getAbsolutePath()
                    );

                    launch.putExtra(
                            "key_diy_font_color_ttf_file_path",
                            fontFile.getAbsolutePath()
                    );
                }

                out(
                        "Starting explicit activity..."
                );

                startActivity(launch);

                out(
                        "startActivity accepted"
                );

                return;

            } catch (Throwable t) {

                out(
                        action +
                                " ERROR:"
                );

                out(
                        stack(
                                unwrap(t)
                        )
                );
            }
        }
    }

    /* ============================================================
       UTILS
       ============================================================ */

    private Throwable unwrap(
            Throwable t) {

        while (t instanceof
                InvocationTargetException) {

            Throwable target =
                    ((InvocationTargetException) t)
                            .getTargetException();

            if (target == null)
                break;

            t = target;
        }

        return t;
    }

    private String root(
            Throwable t) {

        t = unwrap(t);

        return t.getClass()
                .getName() +
                ": " +
                t.getMessage();
    }

    private String stack(
            Throwable t) {

        StringWriter sw =
                new StringWriter();

        t.printStackTrace(
                new PrintWriter(sw)
        );

        return sw.toString();
    }
}
