package com.langf.efence.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;

public class InternalStorage {
    private Context context;
    private static InternalStorage storage;

    private InternalStorage(Context context){
        this.context = context;
    }

    public static InternalStorage getStorage(Context context) {
        if (storage == null) {
            synchronized (InternalStorage.class){
                if (storage == null){
                    storage = new InternalStorage(context);
                }
            }
        }
        return storage;
    }

    /**
     * 保存内容到内部存储器�?
     * @param content
     *            内容
     */
    public void save(final String url, final String content) throws IOException {
        new Thread(){
            public void run() {
                try {
                    // FileOutputStream fos=context.openFileOutput(filename,
                    // Context.MODE_PRIVATE);
                    String filename=String.valueOf(url.hashCode());
                    File file = new File(context.getFilesDir(), filename);
                    FileOutputStream fos = new FileOutputStream(file);

                    fos.write(content.getBytes());
                    fos.close();
                } catch (IOException e) {
                    Logger.e("Persist json from " + url + "error.",e);
                }
            }
        }.start();
    }

    /**
     * 通过文件名获取内容
     * @return 文件内容
     */
    public String get(String url) throws IOException {
        String filename=String.valueOf(url.hashCode());
        FileInputStream fis = context.openFileInput(filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = -1;
        while ((len = fis.read(data)) != -1) {
            baos.write(data, 0, len);
        }
        String re = new String(baos.toByteArray());
        fis.close();
        baos.close();
        return re;
    }

    /**
     * 通过url 删除文件
     * @param url
     * @return
     */
    public boolean delJsonFile(String url) {
        String filename=String.valueOf(url.hashCode());
        return delete(filename);
    }

    /**
     * 以追加的方式在文件的末尾添加内容
     *
     * @param filename
     *            文件�?
     * @param content
     *            追加的内�?
     */
    public void append(String filename, String content) throws IOException {
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
        fos.write(content.getBytes());
        fos.close();
    }

    /**
     * 删除文件
     *
     * @param filename
     *            文件�?
     * @return 是否成功
     */
    public boolean delete(String filename) {
        return context.deleteFile(filename);
    }

    /**
     * 获取内部存储路径下的�?��文件�?
     *
     * @return 文件名数
     */
    public String[] queryAllFile() {
        return context.fileList();
    }

}