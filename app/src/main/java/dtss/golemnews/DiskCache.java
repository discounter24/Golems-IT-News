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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

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
        new ReadTask(context,link,CacheRequestType.Text,handler).execute();
    }

    public void requestImage(String link, ICacheAnswerHandler handler){
        new ReadTask(context,link,CacheRequestType.GolemImageList,handler).execute();
    }

    @Override
    public void onTextReceived(GolemArticlePage sender, String text) {
        File pageCache = new File(cacheDir,getStorageID(sender.getLink()));
        int chapterNum = 0;

        File pageChapterCacheFile;
        for(String chapter : sender.getChapers()){
             pageChapterCacheFile = new File(pageCache,"chapter" + chapterNum);
            new WriteTask(context,pageChapterCacheFile,chapter).execute();
            chapterNum++;
        }

        pageChapterCacheFile = new File(pageCache,"chapterCount");
        new WriteTask(context,pageChapterCacheFile,String.valueOf(chapterNum)).execute();

    }

    @Override
    public void onImagesReceived(GolemArticlePage sender, LinkedList<GolemImage> images) {

        File pageCacheDir = new File(cacheDir,getStorageID(sender.getLink()));
        File imageLinksCacheFile = new File(pageCacheDir,"imageLinks");
        String imageLinksCacheFileContent = "";

        for(GolemImage image : images){

            File imageCacheDir = new File(cacheDir,getStorageID(image.getLink()));

            if (!imageCacheDir.exists()){
                imageCacheDir.mkdirs();
            }

            File dataFile = new File(imageCacheDir,"image.bmp");
            File authorFile = new File(imageCacheDir,"author.txt");
            File descriptionFile = new File(imageCacheDir, "description.txt");

            new WriteTask(context,dataFile,image.getImage()).execute();
            new WriteTask(context,authorFile,image.getAuthor()).execute();
            new WriteTask(context,descriptionFile,image.getDescription()).execute();
            imageLinksCacheFileContent += image.getLink() + "\n";
        }

        new WriteTask(context, imageLinksCacheFile, imageLinksCacheFileContent).execute();

    }

    @Override
    public void onVideosReceived(GolemArticlePage sender, LinkedList<String> videos) {

    }


    private String getStorageID(String s){
        MessageDigest m = null;

        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        m.update(s.getBytes(),0,s.length());
        String hash = new BigInteger(1, m.digest()).toString(16);
        return hash;
    }




    private class WriteTask extends AsyncTask<Void,Void,Void>{

        Context context;
        File file;
        String strContent;
        Bitmap imgContent;

        public WriteTask(Context context, File file, String content){
            this.context = context;
            this.file = file;
            this.strContent = content;
        }

        public WriteTask(Context context,  File file, Bitmap content){
            this.context = context;
            this.file=file;
            this.imgContent = content;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            try {

                if (!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }


                if (file.exists()){
                    file.delete();
                    file.createNewFile();
                }

                FileOutputStream stream = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(stream);

                if (strContent != null){
                    writer.write(strContent);
                    writer.flush();
                } else if (imgContent != null){
                    imgContent.compress(Bitmap.CompressFormat.PNG,100,stream);
                    stream.flush();
                }

                stream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    private class ReadTask extends AsyncTask<Void,Void,Void>{

        private final Context context;
        private final String identifier;
        private final ICacheAnswerHandler handler;
        private final CacheRequestType requestType;

        private GolemImage resultGImage;
        private LinkedList<GolemImage> resultGImageList;
        private LinkedList<String> resultChapters;


        private boolean found;

        public ReadTask(Context context, String identifier, CacheRequestType type, ICacheAnswerHandler handler){
            this.context = context;
            this.identifier = identifier;
            this.handler = handler;
            this.requestType = type;
            this.found = false;
            this.resultChapters = new LinkedList<>();
            this.resultGImageList = new LinkedList<>();
            this.resultGImage = null;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File pageCacheDir;

            switch (requestType){
                case Text:
                    pageCacheDir = new File(cacheDir,getStorageID(identifier));
                    File pageTextCacheFile = new File(pageCacheDir,"chapterCount");
                    if (pageTextCacheFile.exists()){
                        try {
                            int chapterCount = Integer.parseInt(simpleRead(pageTextCacheFile).replace("\n",""));
                            for(int i=0; i<chapterCount; i++){
                                resultChapters.add(simpleRead(new File(pageCacheDir,"chapter" + i)));
                            }
                            found = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case GolemImageList:
                    pageCacheDir = new File(cacheDir,getStorageID(identifier));
                    File imageLinksCacheFile = new File(pageCacheDir,"imageLinks");
                    try {
                        String[] imageLinks = simpleRead(imageLinksCacheFile).split("\n");
                        for(String imageLink : imageLinks){
                            resultGImageList.add(readGolemImage(imageLink));
                        }

                        found = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case GolemImage:
                    resultGImage = readGolemImage(identifier);
                    found = (resultGImage != null);

                    break;
            }


            return null;
        }

        private GolemImage readGolemImage(String link){
            File imageCacheDir = new File(context.getCacheDir(),getStorageID(link));
            if (imageCacheDir.exists()){
                File dataFile = new File(imageCacheDir,"image.bmp");
                File authorFile = new File(imageCacheDir,"author.txt");
                File descriptionFile = new File(imageCacheDir, "description.txt");

                try {
                    Bitmap map = BitmapFactory.decodeStream(new FileInputStream(dataFile));
                    String description = simpleRead(descriptionFile);
                    String author = simpleRead(authorFile);

                    return new GolemImage(link, author, description, map);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
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

        @Override
        protected void onPostExecute(Void aVoid) {
            switch (requestType){
                case Text:
                    handler.onCacheChaptersAnswer(identifier,resultChapters,found);
                    break;
                case GolemImageList:
                    handler.onCacheGImageListAnswer(identifier,resultGImageList,found);
                    break;
                case GolemImage:
                    handler.onCacheGImageAnswer(identifier,resultGImage,found);
                    break;
            }
        }
    }

    public interface ICacheAnswerHandler{
        void onCacheGImageAnswer(String identifier, GolemImage image, boolean found);
        void onCacheGImageListAnswer(String identifier, LinkedList<GolemImage> image, boolean found);
        void onCacheChaptersAnswer(String identifier, LinkedList<String> chapters, boolean found);
    }

    public enum CacheRequestType{
        Text, GolemImage, GolemImageList
    }


}
