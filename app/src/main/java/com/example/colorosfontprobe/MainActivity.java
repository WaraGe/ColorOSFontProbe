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
 static final int PICK=7;
 TextView log,preview; File font;
 final String[] A={"theme","themestore","theme_individuation","theme_individuation_panel",
 "com.oplus.themestore.basic.feature.individuationprovider","com.oplus.themestore.epona"};
 final String[] P={"","font","fonts","theme","themes","current","local","download","individuation","setting","settings"};

 public void onCreate(Bundle b){
  super.onCreate(b); font=new File(getFilesDir(),"probe-font.ttf");
  LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(24,24,24,24);
  TextView t=new TextView(this);t.setText("ColorOS Font Probe 0.6 FINAL");t.setTextSize(24);l.addView(t);
  preview=new TextView(this);preview.setText("가나다라마바사 ABC 123");preview.setTextSize(26);preview.setPadding(0,24,0,24);l.addView(preview);
  Button p=btn("1. TTF / OTF 선택");p.setOnClickListener(v->pick());l.addView(p);
  Button s=btn("2. ColorOS Provider 자동 검사");s.setOnClickListener(v->scan());l.addView(s);
  Button c=btn("3. 전체 로그 복사");c.setOnClickListener(v->copy());l.addView(c);
  log=new TextView(this);log.setTextIsSelectable(true);log.setTextSize(11);l.addView(log);
  ScrollView sv=new ScrollView(this);sv.addView(l);setContentView(sv);
  o("Device="+Build.MANUFACTURER+" "+Build.MODEL+" Android="+Build.VERSION.RELEASE+" SDK="+Build.VERSION.SDK_INT);
  o("이 빌드는 실제 ColorOS ThemeStore Provider 경로를 자동 탐색합니다.");
 }
 Button btn(String x){Button b=new Button(this);b.setText(x);return b;}
 void o(String x){log.append(x+"\n");}
 void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,PICK);}
 protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r!=PICK||c!=RESULT_OK||d==null)return;
  try(InputStream in=getContentResolver().openInputStream(d.getData());OutputStream out=new FileOutputStream(font)){
   byte[] q=new byte[65536];int n;while((n=in.read(q))>0)out.write(q,0,n);
   preview.setTypeface(Typeface.createFromFile(font));o("FONT OK size="+font.length());
  }catch(Throwable e){o("FONT FAIL "+e);}
 }
 void scan(){
  o("\n========== SCAN ==========");
  for(String a:A){
   o("\nAUTHORITY "+a);
   try{
    ProviderInfo pi=getPackageManager().resolveContentProvider(a,0);
    if(pi==null)o("RESOLVE NONE");
    else o("PROVIDER "+pi.packageName+"/"+pi.name+" exported="+pi.exported+" read="+pi.readPermission+" write="+pi.writePermission);
   }catch(Throwable e){o("RESOLVE FAIL "+err(e));}
   for(String p:P)q(Uri.parse("content://"+a+(p.isEmpty()?"":"/"+p)));
  }
  o("\n========== END ==========");
  o("SUCCESS 항목이 있으면 이 로그 전체를 ChatGPT에 보내세요.");
 }
 void q(Uri u){
  Cursor c=null;
  try{
   c=getContentResolver().query(u,null,null,null,null);
   if(c==null){o("NULL "+u);return;}
   o("SUCCESS "+u+" rows="+c.getCount()+" columns="+Arrays.toString(c.getColumnNames()));
   int z=0;while(c.moveToNext()&&z<2){
    StringBuilder b=new StringBuilder("ROW"+z+" ");
    for(int i=0;i<c.getColumnCount();i++){
     if(i>0)b.append(" | ");b.append(c.getColumnName(i)).append("=");
     try{String v=c.getString(i);if(v!=null&&v.length()>120)v=v.substring(0,120)+"...";b.append(v);}catch(Throwable x){b.append("<binary>");}
    }o(b.toString());z++;
   }
  }catch(Throwable e){o("FAIL "+u+" -> "+err(e));}
  finally{if(c!=null)c.close();}
 }
 String err(Throwable e){String s=e.getClass().getSimpleName()+": "+e.getMessage();return s==null?"":s.replace('\n',' ');}
 void copy(){((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("probe",log.getText()));Toast.makeText(this,"로그 복사 완료",Toast.LENGTH_SHORT).show();}
}