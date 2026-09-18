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
    private final String AUTH = "com.example.colorosfontprobe.fonts";
    private final String[] ACTIONS = {
        "com.nearme.themespace.SET_FONT",
        "com.oplus.themestore.basic.action.SET_FONT",
        "com.oplus.themespace.partner.basic.action.SET_FONT",
        "com.oplus.themestore.action.SET_FONT_INDIVIDUATION",
        "com.heytap.themestore.action.SET_FONT_INDIVIDUATION"
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        fontFile = new File(getFilesDir(), "probe-font.ttf");

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18); box.setPadding(pad,pad,pad,pad);

        TextView title = new TextView(this);
        title.setText("ColorOS Font Probe 0.1");
        title.setTextSize(24);
        box.addView(title);

        TextView note = new TextView(this);
        note.setText("루트/Shizuku 없이 ColorOS의 공개 폰트 진입점과 시스템 인터페이스를 탐색하는 테스트 앱입니다. 실제 적용이 보장되는 버전은 아닙니다.");
        note.setPadding(0,dp(8),0,dp(12));
        box.addView(note);

        Button pick = button("1. TTF/OTF 선택");
        pick.setOnClickListener(v -> pickFont());
        box.addView(pick);

        preview = new TextView(this);
        preview.setText("가나다라마바사 ABC abc 123\n선택한 폰트 미리보기");
        preview.setTextSize(24);
        preview.setPadding(0,dp(14),0,dp(14));
        box.addView(preview);

        Button scan = button("2. ColorOS 환경 검사");
        scan.setOnClickListener(v -> scan());
        box.addView(scan);

        Button tryApply = button("3. Theme/Font Intent 적용 시도");
        tryApply.setOnClickListener(v -> tryIntents());
        box.addView(tryApply);

        Button std = button("4. Android FontManager 반사 검사");
        std.setOnClickListener(v -> inspectFontManager());
        box.addView(std);

        Button copy = button("로그 클립보드 복사");
        copy.setOnClickListener(v -> {
            ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("ColorOSFontProbe", log.getText()));
            Toast.makeText(this,"복사됨",Toast.LENGTH_SHORT).show();
        });
        box.addView(copy);

        log = new TextView(this);
        log.setTextIsSelectable(true);
        log.setTextSize(12);
        log.setPadding(0,dp(12),0,dp(24));
        box.addView(log);

        ScrollView sv = new ScrollView(this);
        sv.addView(box);
        setContentView(sv);
        scan();
    }

    private Button button(String s) {
        Button b = new Button(this); b.setText(s); return b;
    }
    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density+.5f); }
    private void out(String s){ log.append(s+"\n"); }

    private void pickFont() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"font/ttf","font/otf","application/x-font-ttf","application/octet-stream"});
        startActivityForResult(i, 100);
    }

    @Override protected void onActivityResult(int req,int res,Intent data) {
        super.onActivityResult(req,res,data);
        if(req!=100 || res!=RESULT_OK || data==null || data.getData()==null) return;
        try(InputStream in=getContentResolver().openInputStream(data.getData());
            OutputStream os=new FileOutputStream(fontFile)) {
            byte[] buf=new byte[65536]; int n;
            while((n=in.read(buf))>0) os.write(buf,0,n);
            Typeface tf=Typeface.createFromFile(fontFile);
            preview.setTypeface(tf);
            out("[FONT] copied: "+fontFile+" ("+fontFile.length()+" bytes)");
            out("[URI] content://"+AUTH+"/probe-font.ttf");
        } catch(Throwable t){ out("[FONT ERROR] "+stack(t)); }
    }

    private void scan() {
        if(log!=null) log.setText("");
        out("=== DEVICE ===");
        out("manufacturer="+Build.MANUFACTURER);
        out("brand="+Build.BRAND);
        out("model="+Build.MODEL);
        out("sdk="+Build.VERSION.SDK_INT+" release="+Build.VERSION.RELEASE);
        out("font_variation_settings="+Settings.System.getString(getContentResolver(),"font_variation_settings"));
        out("selectedFont="+fontFile.exists()+" size="+(fontFile.exists()?fontFile.length():0));
        out("\n=== PACKAGES ===");
        for(String p:new String[]{"com.oplus.themestore","com.heytap.themestore","com.android.settings"}) {
            try { getPackageManager().getPackageInfo(p,0); out(p+" = INSTALLED"); }
            catch(Throwable e){ out(p+" = missing"); }
        }
        out("\n=== INTENT RESOLUTION ===");
        for(String a:ACTIONS) {
            Intent i=new Intent(a);
            ResolveInfo r=getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY);
            out(a+" -> "+(r==null?"NONE":r.activityInfo.packageName+"/"+r.activityInfo.name+
                " exported="+r.activityInfo.exported+" perm="+r.activityInfo.permission));
        }
        out("\n=== SERVICE MANAGER PROBE ===");
        for(String s:new String[]{"font","opluscustomize","theme","oplustheme","themes"}) {
            try {
                Class<?> sm=Class.forName("android.os.ServiceManager");
                Method m=sm.getDeclaredMethod("getService",String.class);
                m.setAccessible(true);
                Object b=m.invoke(null,s);
                out("ServiceManager["+s+"]="+(b==null?"null":b.toString()));
            } catch(Throwable t){ out("ServiceManager["+s+"] ERROR: "+root(t)); }
        }
    }

    private void tryIntents() {
        if(!fontFile.exists()){ out("[APPLY] 먼저 폰트를 선택하세요."); return; }
        Uri u=Uri.parse("content://"+AUTH+"/probe-font.ttf");
        out("\n=== APPLY INTENT TEST ===");
        String[] keys={"font_path","fontPath","path","file_path","font_file_path","ttf_file_path",
                "key_diy_font_color_ttf_file_path","uri","font_uri"};
        for(String action:ACTIONS) {
            try {
                Intent i=new Intent(action);
                i.setDataAndType(u,"font/ttf");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
                for(String k:keys) {
                    i.putExtra(k, k.contains("uri") ? u.toString() : fontFile.getAbsolutePath());
                }
                i.setClipData(ClipData.newRawUri("font",u));
                ResolveInfo r=getPackageManager().resolveActivity(i,0);
                if(r==null){ out(action+" -> no activity"); continue; }
                grantUriPermission(r.activityInfo.packageName,u,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                out(action+" -> starting "+r.activityInfo.packageName+"/"+r.activityInfo.name);
                startActivity(i);
                return; // only one UI launch per press
            } catch(Throwable t){ out(action+" ERROR: "+stack(t)); }
        }
        out("No usable SET_FONT activity. Theme Store 삭제 상태라면 정상일 수 있음.");
    }

    private void inspectFontManager() {
        out("\n=== IFontManager / FontManager reflection ===");
        String[] classes={
            "android.graphics.fonts.FontManager",
            "android.graphics.fonts.FontManager$FontManagerService",
            "com.android.server.graphics.fonts.FontManagerService"
        };
        for(String n:classes) {
            try {
                Class<?> c=Class.forName(n);
                out("CLASS "+n);
                for(Method m:c.getDeclaredMethods()) out("  "+m.toGenericString());
            } catch(Throwable t){ out(n+" -> "+root(t)); }
        }
        try {
            Object svc=getSystemService("font");
            out("getSystemService(\"font\")="+svc);
            if(svc!=null) for(Method m:svc.getClass().getMethods())
                if(m.getName().toLowerCase(Locale.ROOT).contains("font") ||
                   m.getName().toLowerCase(Locale.ROOT).contains("update"))
                    out("  public: "+m.toGenericString());
        } catch(Throwable t){ out("font service inspect ERROR: "+stack(t)); }
    }

    private String root(Throwable t) {
        while(t instanceof InvocationTargetException && ((InvocationTargetException)t).getTargetException()!=null)
            t=((InvocationTargetException)t).getTargetException();
        return t.getClass().getName()+": "+t.getMessage();
    }
    private String stack(Throwable t) {
        StringWriter sw=new StringWriter(); t.printStackTrace(new PrintWriter(sw)); return sw.toString();
    }
}
