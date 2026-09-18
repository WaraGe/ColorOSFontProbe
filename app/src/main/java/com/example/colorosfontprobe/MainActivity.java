package com.example.colorosfontprobe;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    static final int PICK=100;
    TextView log, preview;
    File fontFile;

    final String[] AUTHORITIES = {
        "theme",
        "themestore",
        "theme_individuation",
        "theme_individuation_panel",
        "com.oplus.themestore.basic.feature.individuationprovider",
        "com.oplus.themestore.epona"
    };
    final String[] PATHS = {"", "font", "fonts", "theme", "themes", "current", "local", "download"};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        fontFile=new File(getFilesDir(),"probe-font.ttf");
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        int p=dp(18); box.setPadding(p,p,p,p);

        TextView title=new TextView(this); title.setText("ColorOS Font Provider Probe 0.5"); title.setTextSize(24); box.addView(title);
        TextView info=new TextView(this);
        info.setText("ColorOS ThemeStore Provider를 직접 검사하는 마지막 진단판입니다.\nDB를 쓰거나 삭제하지 않고, 접근 가능한 폰트/테마 URI와 컬럼을 찾습니다.");
        box.addView(info);

        preview=new TextView(this); preview.setText("가나다라마바사 ABCDEFG 1234567890"); preview.setTextSize(24); preview.setPadding(0,p,0,p); box.addView(preview);

        Button pick=btn("1. TTF / OTF 선택"); pick.setOnClickListener(v->pick()); box.addView(pick);
        Button scan=btn("2. Provider 전체 자동 검사"); scan.setOnClickListener(v->scanAll()); box.addView(scan);
        Button copy=btn("3. 로그 복사"); copy.setOnClickListener(v->copy()); box.addView(copy);
        Button clear=btn("로그 지우기"); clear.setOnClickListener(v->log.setText("")); box.addView(clear);

        log=new TextView(this); log.setTextSize(11); log.setTextIsSelectable(true); log.setPadding(0,p,0,p*2); box.addView(log);
        ScrollView s=new ScrollView(this); s.addView(box); setContentView(s);
        out("Device="+Build.MANUFACTURER+" "+Build.MODEL+" Android="+Build.VERSION.RELEASE+" SDK="+Build.VERSION.SDK_INT);
        if(fontFile.exists()) loadPreview();
    }

    Button btn(String s){ Button b=new Button(this); b.setText(s); return b; }
    int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+.5f); }
    void out(String s){ log.append(s+"\n"); }

    void pick(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"font/ttf","font/otf","application/x-font-ttf","application/x-font-opentype","application/octet-stream"});
        startActivityForResult(i,PICK);
    }

    @Override protected void onActivityResult(int r,int c,Intent data){
        super.onActivityResult(r,c,data);
        if(r!=PICK||c!=RESULT_OK||data==null||data.getData()==null)return;
        try(InputStream in=getContentResolver().openInputStream(data.getData()); OutputStream os=new FileOutputStream(fontFile)){
            byte[] buf=new byte[65536]; int n; while((n=in.read(buf))>0) os.write(buf,0,n);
            loadPreview(); out("FONT selected size="+fontFile.length()+" path="+fontFile.getAbsolutePath());
        }catch(Throwable t){out("FONT ERROR "+t);}
    }

    void loadPreview(){
        try{preview.setTypeface(Typeface.createFromFile(fontFile));}catch(Throwable t){out("PREVIEW ERROR "+t);}
    }

    void scanAll(){
        out("\n========== PROVIDER SCAN ==========");
        for(String authority:AUTHORITIES) inspectAuthority(authority);
        out("========== END ==========");
        out("중요: SUCCESS가 나온 URI와 columns/row 내용을 그대로 보내주세요.");
    }

    void inspectAuthority(String authority){
        out("\n--- authority: "+authority+" ---");
        try{
            ProviderInfo pi=getPackageManager().resolveContentProvider(authority,0);
            if(pi==null){ out("resolveContentProvider = NONE"); }
            else {
                out("provider="+pi.packageName+"/"+pi.name);
                out("exported="+pi.exported+" readPerm="+pi.readPermission+" writePerm="+pi.writePermission);
            }
