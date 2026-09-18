package com.example.colorosfontprobe;
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
        }catch(Throwable t){out("resolve ERROR "+shortErr(t));}

        for(String path:PATHS){
            Uri u=Uri.parse("content://"+authority+(path.length()==0?"":"/"+path));
            query(u);
        }
    }

    void query(Uri u){
        Cursor c=null;
        try{
            c=getContentResolver().query(u,null,null,null,null);
            if(c==null){out("NULL "+u); return;}
            String[] cols=c.getColumnNames();
            out("SUCCESS "+u+" rows="+c.getCount()+" columns="+Arrays.toString(cols));
            int shown=0;
            while(c.moveToNext() && shown<3){
                StringBuilder sb=new StringBuilder(" row"+shown+": ");
                for(int i=0;i<cols.length;i++){
                    if(i>0)sb.append(" | ");
                    sb.append(cols[i]).append("=");
                    try{
                        String v=c.getString(i);
                        if(v!=null && v.length()>160)v=v.substring(0,160)+"...";
                        sb.append(v);
                    }catch(Throwable x){sb.append("<").append(c.getType(i)).append(">");}
                }
                out(sb.toString()); shown++;
            }
        }catch(Throwable t){out("FAIL "+u+" -> "+shortErr(t));}
        finally{if(c!=null)c.close();}
    }

    String shortErr(Throwable t){
        String s=t.getClass().getSimpleName()+": "+t.getMessage();
        return s.replace('\n',' ');
    }

    void copy(){
        ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("ColorOS Font Provider Probe",log.getText()));
        Toast.makeText(this,"로그 복사됨",Toast.LENGTH_SHORT).show();
    }
}
