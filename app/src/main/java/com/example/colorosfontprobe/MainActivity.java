package com.example.colorosfontprobe;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.*;

import java.io.*;

public class MainActivity extends Activity {

    private TextView log;
    private TextView preview;
    private File fontFile;

    private static final int PICK_FONT = 100;

    private static final String AUTHORITY =
            "com.example.colorosfontprobe.fonts";

    private static final String ACTION_SET_FONT =
            "com.nearme.themespace.SET_FONT";

    private static final String OPLUS_ACTION_SET_FONT =
            "com.oplus.themestore.basic.action.SET_FONT";

    private static final String ACTION_INDIVIDUATION =
            "com.oplus.themestore.action.SET_FONT_INDIVIDUATION";

    private static final String HEYTAP_ACTION_INDIVIDUATION =
            "com.heytap.themestore.action.SET_FONT_INDIVIDUATION";

    // ThemeStore APK에서 확인된 DIY 폰트 관련 key
    private static final String DIY_TTF_KEY =
            "key_diy_font_color_ttf_file_path";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        fontFile = new File(
                getFilesDir(),
                "probe-font.ttf"
        );

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        int p = dp(18);

        box.setPadding(
                p, p, p, p
        );

        TextView title =
                new TextView(this);

        title.setText(
                "ColorOS Font Probe 0.3"
        );

        title.setTextSize(24);

        box.addView(title);

        TextView info =
                new TextView(this);

        info.setText(
                "OPPO ThemeStore 17.11.7의 SET_FONT / DIY Font 경로를 테스트합니다.\n\n" +
                "각 테스트는 한 번씩 실행하고 My fonts에 선택한 폰트가 나타나는지 확인하세요."
        );

        info.setPadding(
                0,
                dp(8),
                0,
                dp(12)
        );

        box.addView(info);


        /* =========================
           FONT PICK
           ========================= */

        Button pick =
                button("1. TTF / OTF 선택");

        pick.setOnClickListener(
                v -> pickFont()
        );

        box.addView(pick);


        preview =
                new TextView(this);

        preview.setText(
                "가나다라마바사 아자차카타파하\n" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" +
                "abcdefghijklmnopqrstuvwxyz\n" +
                "0123456789"
        );

        preview.setTextSize(23);

        preview.setPadding(
                0,
                dp(15),
                0,
                dp(15)
        );

        box.addView(preview);


        /* =========================
           INFO
           ========================= */

        Button status =
                button("2. 현재 상태 확인");

        status.setOnClickListener(
                v -> showStatus()
        );

        box.addView(status);


        /* =========================
           TEST A
           ========================= */

        Button a =
                button(
                        "3A. SET_FONT + 일반 path extras"
                );

        a.setOnClickListener(
                v -> testPathExtras()
        );

        box.addView(a);


        /* =========================
           TEST B
           ========================= */

        Button b =
                button(
                        "3B. SET_FONT + Content URI"
                );

        b.setOnClickListener(
                v -> testContentUri()
        );

        box.addView(b);


        /* =========================
           TEST C
           ========================= */

        Button c =
                button(
                        "3C. DIY TTF key 테스트"
                );

        c.setOnClickListener(
                v -> testDiyKey()
        );

        box.addView(c);


        /* =========================
           TEST D
           ========================= */

        Button d =
                button(
                        "3D. SET_FONT_INDIVIDUATION 테스트"
                );

        d.setOnClickListener(
                v -> testIndividuation()
        );

        box.addView(d);


        /* =========================
           TEST E
           ========================= */

        Button e =
                button(
                        "3E. URI + DIY key 전부 조합"
                );

        e.setOnClickListener(
                v -> testEverything()
        );

        box.addView(e);


        /* =========================
           LOG
           ========================= */

        Button clear =
                button("로그 지우기");

        clear.setOnClickListener(
                v -> log.setText("")
        );

        box.addView(clear);


        Button copy =
                button("로그 클립보드 복사");

        copy.setOnClickListener(
                v -> copyLog()
        );

        box.addView(copy);


        log =
                new TextView(this);

        log.setTextSize(11);

        log.setTextIsSelectable(true);

        log.setPadding(
                0,
                dp(15),
                0,
                dp(40)
        );

        box.addView(log);


        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(box);

        setContentView(scroll);

        showStatus();
    }


    /* ==========================================================
       UI
       ========================================================== */

    private Button button(
            String text
    ) {

        Button b =
                new Button(this);

        b.setText(text);

        return b;
    }


    private int dp(
            int value
    ) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }


    private void out(
            String text
    ) {

        log.append(
                text + "\n"
        );
    }


    /* ==========================================================
       PICK FONT
       ========================================================== */

    private void pickFont() {

        Intent i =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        i.setType("*/*");

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

        startActivityForResult(
                i,
                PICK_FONT
        );
    }


    @Override
    protected void onActivityResult(
            int request,
            int result,
            Intent data
    ) {

        super.onActivityResult(
                request,
                result,
                data
        );

        if (
                request != PICK_FONT ||
                result != RESULT_OK ||
                data == null ||
                data.getData() == null
        ) {

            return;
        }


        Uri uri =
                data.getData();


        try {

            InputStream in =
                    getContentResolver()
                            .openInputStream(uri);

            FileOutputStream os =
                    new FileOutputStream(
                            fontFile
                    );

            byte[] buffer =
                    new byte[65536];

            int n;

            while (
                    (n = in.read(buffer))
                            > 0
            ) {

                os.write(
                        buffer,
                        0,
                        n
                );
            }

            in.close();

            os.flush();
            os.close();


            Typeface typeface =
                    Typeface.createFromFile(
                            fontFile
                    );

            preview.setTypeface(
                    typeface
            );


            out("");
            out("==============================");
            out("FONT SELECTED");
            out("==============================");

            out(
                    "source uri=" +
                            uri
            );

            out(
                    "private path=" +
                            fontFile.getAbsolutePath()
            );

            out(
                    "size=" +
                            fontFile.length()
            );

            out(
                    "readable=" +
                            fontFile.canRead()
            );


            out(
                    "provider uri=" +
                            getProviderUri()
            );


        } catch (
                Throwable t
        ) {

            out(
                    stack(t)
            );
        }
    }


    /* ==========================================================
       STATUS
       ========================================================== */

    private void showStatus() {

        if (log == null)
            return;


        out("");
        out("==============================");
        out("COLOROS FONT PROBE 0.3");
        out("==============================");

        out(
                "manufacturer=" +
                        android.os.Build.MANUFACTURER
        );

        out(
                "model=" +
                        android.os.Build.MODEL
        );

        out(
                "sdk=" +
                        android.os.Build.VERSION.SDK_INT
        );

        out(
                "release=" +
                        android.os.Build.VERSION.RELEASE
        );

        out(
                "font file=" +
                        fontFile.exists()
        );

        out(
                "font size=" +
                        (
                                fontFile.exists()
                                        ? fontFile.length()
                                        : 0
                        )
        );


        out(
                "font_variation_settings=" +
                        Settings.System.getString(
                                getContentResolver(),
                                "font_variation_settings"
                        )
        );


        checkAction(
                ACTION_SET_FONT
        );

        checkAction(
                OPLUS_ACTION_SET_FONT
        );

        checkAction(
                ACTION_INDIVIDUATION
        );

        checkAction(
                HEYTAP_ACTION_INDIVIDUATION
        );
    }


    private void checkAction(
            String action
    ) {

        try {

            Intent i =
                    new Intent(action);

            ResolveInfo r =
                    getPackageManager()
                            .resolveActivity(
                                    i,
                                    0
                            );


            if (r == null) {

                out(
                        action +
                                " -> NONE"
                );

                return;
            }


            out(
                    action +
                            " -> " +
                            r.activityInfo.packageName +
                            "/" +
                            r.activityInfo.name
            );


        } catch (
                Throwable t
        ) {

            out(
                    action +
                            " -> ERROR " +
                            t
            );
        }
    }


    /* ==========================================================
       URI
       ========================================================== */

    private Uri getProviderUri() {

        return Uri.parse(
                "content://" +
                        AUTHORITY +
                        "/probe-font.ttf"
        );
    }


    private void grantFontUri(
            String packageName
    ) {

        try {

            grantUriPermission(
                    packageName,
                    getProviderUri(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            out(
                    "grantUriPermission -> " +
                            packageName
            );

        } catch (
                Throwable t
        ) {

            out(
                    "grantUriPermission ERROR: " +
                            t
            );
        }
    }


    /* ==========================================================
       COMMON LAUNCH
       ========================================================== */

    private void launch(
            Intent i,
            String test
    ) {

        out("");
        out("==============================");
        out(test);
        out("==============================");

        try {

            ResolveInfo r =
                    getPackageManager()
                            .resolveActivity(
                                    i,
                                    0
                            );


            if (r == null) {

                /*
                 * Extras/Data가 붙으면서 resolve가 실패할 수도 있으므로
                 * plain action으로 한 번 더 resolve한다.
                 */

                Intent plain =
                        new Intent(
                                i.getAction()
                        );

                r =
                        getPackageManager()
                                .resolveActivity(
                                        plain,
                                        0
                                );
            }


            if (r == null) {

                out(
                        "NO ACTIVITY"
                );

                return;
            }


            String pkg =
                    r.activityInfo.packageName;

            String cls =
                    r.activityInfo.name;


            out(
                    "activity=" +
                            pkg +
                            "/" +
                            cls
            );


            i.setClassName(
                    pkg,
                    cls
            );


            i.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            i.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );


            grantFontUri(
                    pkg
            );


            out(
                    "action=" +
                            i.getAction()
            );

            out(
                    "data=" +
                            i.getData()
            );

            out(
                    "type=" +
                            i.getType()
            );

            out(
                    "private path=" +
                            fontFile.getAbsolutePath()
            );

            out(
                    "provider uri=" +
                            getProviderUri()
            );


            startActivity(i);


            out(
                    "startActivity = ACCEPTED"
            );


        } catch (
                Throwable t
        ) {

            out(
                    "START FAILED"
            );

            out(
                    stack(t)
            );
        }
    }


    /* ==========================================================
       TEST A
       ========================================================== */

    private void testPathExtras() {

        if (!checkFont())
            return;


        Intent i =
                new Intent(
                        ACTION_SET_FONT
                );


        String path =
                fontFile.getAbsolutePath();


        i.putExtra(
                "font_path",
                path
        );

        i.putExtra(
                "fontPath",
                path
        );

        i.putExtra(
                "path",
                path
        );

        i.putExtra(
                "file_path",
                path
        );

        i.putExtra(
                "font_file_path",
                path
        );

        i.putExtra(
                "ttf_file_path",
                path
        );


        launch(
                i,
                "TEST A - PATH EXTRAS"
        );
    }


    /* ==========================================================
       TEST B
       ========================================================== */

    private void testContentUri() {

        if (!checkFont())
            return;


        Uri uri =
                getProviderUri();


        Intent i =
                new Intent(
                        ACTION_SET_FONT
                );


        i.setDataAndType(
                uri,
                "font/ttf"
        );


        i.setClipData(
                ClipData.newRawUri(
                        "font",
                        uri
                )
        );


        i.putExtra(
                "uri",
                uri.toString()
        );

        i.putExtra(
                "font_uri",
                uri.toString()
        );

        i.putExtra(
                "fontUri",
                uri.toString()
        );


        launch(
                i,
                "TEST B - CONTENT URI"
        );
    }


    /* ==========================================================
       TEST C
       ========================================================== */

    private void testDiyKey() {

        if (!checkFont())
            return;


        Intent i =
                new Intent(
                        ACTION_SET_FONT
                );


        i.putExtra(
                DIY_TTF_KEY,
                fontFile.getAbsolutePath()
        );


        /*
         * path 문자열 외에도 URI 문자열을 별도 후보 key로 전달.
         */

        i.putExtra(
                DIY_TTF_KEY + "_uri",
                getProviderUri()
                        .toString()
        );


        launch(
                i,
                "TEST C - DIY TTF KEY"
        );
    }


    /* ==========================================================
       TEST D
       ========================================================== */

    private void testIndividuation() {

        if (!checkFont())
            return;


        Intent i =
                new Intent(
                        ACTION_INDIVIDUATION
                );


        addAllFontExtras(i);


        launch(
                i,
                "TEST D - SET_FONT_INDIVIDUATION"
        );
    }


    /* ==========================================================
       TEST E
       ========================================================== */

    private void testEverything() {

        if (!checkFont())
            return;


        Uri uri =
                getProviderUri();


        Intent i =
                new Intent(
                        ACTION_SET_FONT
                );


        addAllFontExtras(i);


        i.setClipData(
                ClipData.newRawUri(
                        "font",
                        uri
                )
        );


        /*
         * 여기서는 setDataAndType을 일부러 사용하지 않는다.
         * 0.1에서 MIME/data가 붙었을 때 resolve 결과가 달라졌기 때문.
         */


        launch(
                i,
                "TEST E - ALL EXTRAS"
        );
    }


    /* ==========================================================
       ALL EXTRAS
       ========================================================== */

    private void addAllFontExtras(
            Intent i
    ) {

        String path =
                fontFile.getAbsolutePath();

        String uri =
                getProviderUri()
                        .toString();


        i.putExtra(
                "font_path",
                path
        );

        i.putExtra(
                "fontPath",
                path
        );

        i.putExtra(
                "path",
                path
        );

        i.putExtra(
                "file_path",
                path
        );

        i.putExtra(
                "font_file_path",
                path
        );

        i.putExtra(
                "ttf_file_path",
                path
        );

        i.putExtra(
                DIY_TTF_KEY,
                path
        );


        i.putExtra(
                "uri",
                uri
        );

        i.putExtra(
                "font_uri",
                uri
        );

        i.putExtra(
                "fontUri",
                uri
        );


        i.putExtra(
                "font_name",
                "ColorOSFontProbe"
        );

        i.putExtra(
                "fontName",
                "ColorOSFontProbe"
        );

        i.putExtra(
                "name",
                "ColorOSFontProbe"
        );
    }


    /* ==========================================================
       CHECK
       ========================================================== */

    private boolean checkFont() {

        if (!fontFile.exists()) {

            out("");
            out(
                    "ERROR: 먼저 폰트를 선택하세요."
            );

            Toast.makeText(
                    this,
                    "먼저 TTF/OTF를 선택하세요.",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }


    /* ==========================================================
       COPY
       ========================================================== */

    private void copyLog() {

        android.content.ClipboardManager cm =
                (android.content.ClipboardManager)
                        getSystemService(
                                CLIPBOARD_SERVICE
                        );


        cm.setPrimaryClip(
                ClipData.newPlainText(
                        "ColorOS Font Probe 0.3",
                        log.getText()
                )
        );


        Toast.makeText(
                this,
                "로그 복사됨",
                Toast.LENGTH_SHORT
        ).show();
    }


    /* ==========================================================
       STACK
       ========================================================== */

    private String stack(
            Throwable t
    ) {

        StringWriter sw =
                new StringWriter();

        t.printStackTrace(
                new PrintWriter(sw)
        );

        return sw.toString();
    }
}
