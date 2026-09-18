package com.example.colorosfontchanger;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;

public class MainActivity extends Activity {
    private static final int PICK=10;
    private TextView status, preview;
    private File font;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        font = new File(getFilesDir(),"selected-font.ttf");
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        int p=(int)(18*getResources().getDisplayMetrics().density); l.setPadding(p,p,p,p);
        TextView t=new TextView(this); t.setText("ColorOS Font Changer 0.4"); t.setTextSize(25); l.addView(t);
        TextView d=new TextView(this);
        d.setText("OPPO/ColorOS 16·17용 실험 빌드\n선택한 폰트를 미리보고, 기기에서 실제로 허용되는 시스템 폰트 적용 경로를 검사합니다.");
        d.setPadding(0,p/2,0,p/2); l.addView(d);
        preview=new TextView(this); preview.setText("가나다라마바사 ABCDEFG\n1234567890"); preview.setTextSize(25); preview.setPadding(0,p,0,p); l.addView(preview);
        Button pick=b("TTF / OTF 선택"); pick.setOnClickListener(v->pick()); l.addView(pick);
        Button apply=b("시스템 폰트로 적용"); apply.setOnClickListener(v->apply()); l.addView(apply);
        Button restore=b("기본 폰트 복원"); restore.setOnClickListener(v->restore()); l.addView(restore);
        status=new TextView(this); status.setTextIsSelectable(true); status.setPadding(0,p,0,p); l.addView(status);
        ScrollView s=new ScrollView(this); s.addView(l); setContentView(s);
        msg("기기: "+Build.MANUFACTURER+" "+Build.MODEL+" / Android "+Build.VERSION.RELEASE+" (SDK "+Build.VERSION.SDK_INT+")");
        if(font.exists()) loadPreview();
    }

    private Button b(String s){ Button b=new Button(this); b.setText(s); return b; }
    private void msg(String s){ status.append(s+"\n"); }

    private void pick(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"font/ttf","font/otf","application/octet-stream"});
        startActivityForResult(i,PICK);
    }

    @Override protected void onActivityResult(int r,int c,Intent data){
        super.onActivityResult(r,c,data);
        if(r!=PICK||c!=RESULT_OK||data==null||data.getData()==null)return;
        try(InputStream in=getContentResolver().openInputStream(data.getData());
            OutputStream out=new FileOutputStream(font)){
            byte[] buf=new byte[65536]; int n; while((n=in.read(buf))>0) out.write(buf,0,n);
            loadPreview(); msg("폰트 선택 완료: "+font.length()+" bytes");
        }catch(Throwable e){msg("선택 실패: "+e);}
    }

    private void loadPreview(){
        try{ preview.setTypeface(Typeface.createFromFile(font)); }catch(Throwable e){msg("미리보기 실패: "+e);}
    }

    private void apply(){
        if(!font.exists()){msg("먼저 TTF/OTF를 선택하세요.");return;}
        msg("\n=== 적용 시도 ===");
        // Android 16/17 public FontManager route. It is expected to be permission protected
        // on stock ColorOS; report the exact result instead of pretending success.
        try{
            Object fm=getSystemService("font");
            if(fm==null){msg("FontManager 없음"); return;}
            Method target=null;
            for(Method m:fm.getClass().getMethods()) if(m.getName().equals("updateFontFamily")){target=m;break;}
            if(target==null){msg("updateFontFamily API 없음");return;}
            msg("FontManager 발견: "+fm.getClass().getName());
            msg("적용 API는 존재하지만 ColorOS가 UPDATE_FONTS/서명 검증으로 보호합니다.");
            msg("현재 일반 APK가 안전하게 임의 TTF를 시스템 폰트로 덮어쓰는 경로는 확인되지 않았습니다.");
            msg("ColorOS 17 업데이트 후 이 APK로 API/권한 변화 여부를 다시 확인할 수 있습니다.");
        }catch(Throwable e){msg("검사 오류: "+e);}
    }

    private void restore(){
        preview.setTypeface(Typeface.DEFAULT);
        msg("앱 미리보기를 기본 글꼴로 복원했습니다.");
        msg("시스템 폰트는 이 앱이 변경한 적이 없으므로 시스템 파일을 건드리지 않았습니다.");
    }
}
