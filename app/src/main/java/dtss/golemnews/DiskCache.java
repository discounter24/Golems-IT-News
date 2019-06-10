package dtss.golemnews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Objects;

public class DiskCache implements IPageHandler {
    private static DiskCache ourInstance;

    public static DiskCache getInstance() {
        return ourInstance;
    }


    public static void openCache(Context context){
        if (ourInstance==null){
            ourInstance = new DiskCache(context);
        }
    }


    private Context context;
    private File cacheDir;

    private DiskCache(Context context) {
        this.context = context;
        this.cacheDir = context.getCacheDir();
    }


    public void save(GolemArticle article){
        for(GolemArticlePage page : article.getPages()){
            save(page);
        }
    }

    public void save(GolemArticlePage page){
        page.requestImages(this);
        page.requestVideos(this);
        page.requestText(this);
    }


    public void requestText(String link, ICacheAnswerHandler handler){
        new ReadTask(getPageCache(link),"chapters",String.class,true,handler).execute();
    }

    public void requestImage(String link, ICacheAnswerHandler handler){
        new ReadTask(getPageCache(link),"images",GolemImage.class,true,handler).execute();
    }

    public void requestVideo(String link, ICacheAnswerHandler handler){
        new ReadTask(getPageCache(link),"videoLinks",String.class,true,handler).execute();
    }

    public void requestHtml(String link, ICacheAnswerHandler handler){
        new ReadTask(getPageCache(link),"html",String.class,false,handler).execute();
    }

    @Override
    public void onTextReceived(GolemArticlePage sender, String text) {
        new WriteTask<LinkedList>(getPageCache(sender),"chapters",sender.getChapers()).execute();
    }

    @Override
    public void onImagesReceived(GolemArticlePage sender, LinkedList<GolemImage> images) {
        new WriteTask<LinkedList>(getPageCache(sender),"images",images).execute();
    }

    @Override
    public void onVideosReceived(GolemArticlePage sender, LinkedList<String> videos) {
        new WriteTask<LinkedList>(getPageCache(sender),"videoLinks",videos).execute();
    }


    public File getPageCache(GolemArticlePage page){
        return getPageCache(page.getLink());
    }

    public File getPageCache(String link){
        return new File(cacheDir,getStorageID(link));
    }




    public class WriteTask<T> extends AsyncTask<Void,Void,Void>{

        //Context context;
        File cacheFolder;
        String id;
        T content;

        public WriteTask(File cacheFolder, String id, T content){
            //this.context = context;
            this.cacheFolder = cacheFolder;
            this.id = id;
            this.content = content;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (cacheFolder.exists() && cacheFolder.isFile()){
                    cacheFolder.delete();
                }

                if (!cacheFolder.exists()){
                    cacheFolder.mkdirs();
                }


                if (content instanceof GolemImage){

                    //File file = new File(cacheFolder,id);
                    simpleWrite(cacheFolder,id,(GolemImage)content);

                } else if (content instanceof String){
                    File file = new File(cacheFolder,id);
                    simpleWrite(file,(String)content);

                } else if (content instanceof LinkedList){
                    LinkedList list = (LinkedList) content;
                    File listTypeFile = new File(cacheFolder,id + ".listtype");
                    String listTypeFileContent = "";


                    for(int i = 0; i < list.size(); i++){
                        Object item = list.get(i);
                        if (item instanceof String){
                            listTypeFileContent = "string";
                            String s = (String)item;
                            File file = new File(cacheFolder,id + "." + i);
                            simpleWrite(file,s);
                        } else if (item instanceof GolemImage){
                            listTypeFileContent = "gimage";
                            GolemImage image = (GolemImage)item;
                            simpleWrite(cacheFolder,id + "." + i,image);
                        }
                    }

                    simpleWrite(listTypeFile,listTypeFileContent);
                    simpleWrite(new File(cacheFolder,id + ".listlength"), String.valueOf(list.size()));
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public class ReadTask extends AsyncTask<Void,Void,Void>{

        private final File cacheFolder;

        private final String id;
        private final ICacheAnswerHandler handler;

        private Object result;

        private boolean list;
        private Class<?> type;

        private boolean found;

        public ReadTask(File cacheFolder, String id, Class<?> type, boolean list, ICacheAnswerHandler handler){
            this.cacheFolder = cacheFolder;
            this.id = id;
            this.handler = handler;
            this.result = null;
            this.found = false;
            this.list = list;
            this.type = type;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                if (list){
                    File listTypeFile = new File(cacheFolder,id + ".listtype");
                    File listLengthFile = new File(cacheFolder, id + ".listlength");

                    String strTypeFound = simpleRead(listTypeFile);
                    String strLength = simpleRead(listLengthFile);

                    int length = Integer.parseInt(strLength);

                    LinkedList list = new LinkedList();

                    /*
                    if (type==GolemImage.class){
                        list = new LinkedList<GolemImage>();
                    } else if (type==String.class) {
                        list = new LinkedList<String>();
                    }
                    */

                    for(int i=0;i<length;i++){
                        if (type == GolemImage.class && strTypeFound.equalsIgnoreCase("gimage")){
                            list.add(readGolemImage(cacheFolder,id + "." + i));
                            found = true;
                        } else if (type == String.class && strTypeFound.equalsIgnoreCase("string")){
                            String s = simpleRead(new File(cacheFolder,id + "." + i));
                            list.add(s);
                            found = true;
                        }

                    }

                    result = list;

                } else {
                    if (type == GolemImage.class){
                        result = readGolemImage(cacheFolder,id);
                        found = true;
                    } else if (type == String.class){
                        result = simpleRead(new File(cacheFolder,id));
                        found = true;
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            handler.onCacheAnswer(result,found);
        }
    }




    public interface ICacheAnswerHandler<T>{
        void onCacheAnswer(T cacheObject, boolean found);
    }



    private GolemImage readGolemImage(File cacheFolder, String id) throws IOException {
        File dataFile = new File(cacheFolder,id + ".bmp");
        File linkFile = new File(cacheFolder,id + ".link");
        File authorFile = new File(cacheFolder,id + ".author");
        File descriptionFile = new File(cacheFolder, id + ".desc");

        Bitmap map = BitmapFactory.decodeStream(new FileInputStream(dataFile));
        String description = simpleRead(descriptionFile);
        String author = simpleRead(authorFile);
        String link = simpleRead(linkFile);

        return new GolemImage(link, author, description, map);
    }

    private void simpleWrite(File cacheFolder, String id, GolemImage image) throws IOException {
        File dataFile = new File(cacheFolder,id + ".bmp");
        File linkFile = new File(cacheFolder,id + ".link");
        File authorFile = new File(cacheFolder,id + ".author");
        File descriptionFile = new File(cacheFolder, id + ".desc");
        simpleWrite(linkFile,image.getLink());
        simpleWrite(authorFile,image.getAuthor());
        simpleWrite(descriptionFile,image.getDescription());
        simpleWrite(dataFile,image.getImage());
    }


    private void simpleWrite(File file, String content) throws IOException {
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


    private void simpleWrite(File file, Bitmap content) throws IOException {
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        if (file.exists()){
            file.delete();
        }

        file.createNewFile();

        FileOutputStream stream = new FileOutputStream(file);
        content.compress(Bitmap.CompressFormat.PNG,100,stream);
        stream.flush();
        stream.close();
    }

    private String simpleRead(File file) throws IOException{
        return simpleRead(file,false);
    }

    private String simpleRead(File file, boolean withLineBraks) throws IOException{
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



    private String getStorageID(String s){
        MessageDigest m = null;

        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(m).update(s.getBytes(),0,s.length());
        return new BigInteger(1, m.digest()).toString(16);
    }

}
