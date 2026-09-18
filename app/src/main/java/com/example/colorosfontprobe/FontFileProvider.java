package com.example.colorosfontprobe;

import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

public class FontFileProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }

    private File getFont() throws FileNotFoundException {
        File f = new File(requireContext().getFilesDir(), "probe-font.ttf");
        if (!f.exists()) throw new FileNotFoundException("No selected font");
        return f;
    }
    private Context requireContext() {
        Context c = getContext();
        if (c == null) throw new IllegalStateException("No context");
        return c;
    }
    @Override public String getType(Uri uri) { return "font/ttf"; }
    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(getFont(), ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override public Cursor query(Uri uri, String[] p, String s, String[] a, String sort) {
        MatrixCursor c = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
        try {
            File f = getFont();
            c.addRow(new Object[]{"probe-font.ttf", f.length()});
        } catch (Exception ignored) {}
        return c;
    }
    @Override public int delete(Uri u,String s,String[] a){return 0;}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){return 0;}
    @Override public Uri insert(Uri u,ContentValues v){return null;}
}
