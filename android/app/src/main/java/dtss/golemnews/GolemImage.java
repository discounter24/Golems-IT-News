package dtss.golemnews;

import android.graphics.Bitmap;

public class GolemImage {
    private final String link;
    private final String author;
    private final String description;
    private Bitmap image;


    public GolemImage(String link, String author, String description, Bitmap image){
        this.link = link;
        this.author = author;
        this.description = description;
        this.image = image;
    }


    public void setImage(Bitmap image){
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
         return description;
    }

    public Bitmap getImage() { return image; }



}
