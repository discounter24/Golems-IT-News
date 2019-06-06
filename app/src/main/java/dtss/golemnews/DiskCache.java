package dtss.golemnews;

public class DiskCache {
    private static final DiskCache ourInstance = new DiskCache();

    public static DiskCache getInstance() {
        return ourInstance;
    }

    private DiskCache() {

    }



}
