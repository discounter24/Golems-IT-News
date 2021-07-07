package dtss.golemnews.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import dtss.golemnews.GolemImage;

public class IOUtils {

    public static String getSha1(String text)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(text.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }

    public static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }


    public static void simpleWrite(File cacheFolder, String id, GolemImage image) throws IOException {
        if (image==null) return;
        File dataFile = new File(cacheFolder,id + ".bmp");
        File linkFile = new File(cacheFolder,id + ".link");
        File authorFile = new File(cacheFolder,id + ".author");
        File descriptionFile = new File(cacheFolder, id + ".desc");

        if (image.getLink() != null){
            simpleWrite(linkFile,image.getLink());
        }
        if (image.getAuthor() != null){
            simpleWrite(authorFile,image.getAuthor());
        }
        if (image.getDescription() != null){
            simpleWrite(descriptionFile, image.getDescription());
        }

        if (image.getImage() != null){
            simpleWrite(dataFile,image.getImage());
        }

    }


    public static void simpleWrite(File file, String content) throws IOException {
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        if (file.exists()){
            file.delete();
        }

        file.createNewFile();

        FileOutputStream stream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(content);
        writer.flush();
        stream.close();
    }


    public static void simpleWrite(File file, Bitmap content) throws IOException {
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        if (file.exists()){
            file.delete();
        }

        file.createNewFile();

        FileOutputStream stream = new FileOutputStream(file);
        try{
            content.compress(Bitmap.CompressFormat.PNG,100,stream);
            stream.flush();
        } catch (Exception ex){
            Log.d("SimepleWrite_Image",ex.toString());
        }
        stream.close();
    }

    public static String simpleRead(File file) throws IOException{
        return simpleRead(file,false);
    }

    public static String simpleRead(File file, boolean withLineBraks) throws IOException{
        StringBuilder text = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            text.append(line);
            if (withLineBraks){
                text.append('\n');
            }
        }
        br.close();
        return text.toString();
    }



    public static class WriteFileTask extends AsyncTask<Void,Void,Void> {

        File folder;
        String name;
        String content;

        public WriteFileTask(File folder, String name, String content){
            this.folder = folder;
            this.name = name;
            this.content = content;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (folder.exists() && folder.isFile()){
                    folder.delete();
                }

                if (!folder.exists()){
                    folder.mkdirs();
                }

                IOUtils.simpleWrite(new File(folder,name),content);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static class ReadTask extends AsyncTask<Void,Void,Void>{

        private File folder;
        private String name;
        private String content;

        private ReadFileHandler handler;


        private boolean found = false;

        public ReadTask(File folder, String name, ReadFileHandler handler){
            this.folder = folder;
            this.name = name;
            this.handler = handler;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                content = IOUtils.simpleRead(new File(folder,name));
                found = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            handler.onFinish(content,found);
        }
    }

    public interface ReadFileHandler{
        void onFinish(String content, boolean found);
    }
}
