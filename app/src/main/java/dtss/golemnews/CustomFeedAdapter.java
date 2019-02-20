package dtss.golemnews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.text.SimpleDateFormat;

class CustomFeedAdapter extends BaseAdapter {


    final Context context;
    GolemFeedItem[] data;
    private  static LayoutInflater inflater = null;

    public CustomFeedAdapter(Context context, GolemFeedItem[] data){
        this.context=context;
        this.data=data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public GolemFeedItem getItem(int position) {
        return  data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null) vi = inflater.inflate(R.layout.feedviewitem, null);

        TextView title = vi.findViewById(R.id.articleTitle);
        TextView description = vi.findViewById(R.id.itemdescription);
        ImageView preview = vi.findViewById(R.id.preview);
        TextView dateView = vi.findViewById(R.id.dateView);

        title.setText(data[position].getTitle());
        description.setText(data[position].getDescription());

        preview.setImageBitmap(data[position].getPreviewImage());

        SimpleDateFormat day = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");


        try{
            String datum = "Veröffentlicht am " + day.format(data[position].getPubDate()) + " um " + time.format(data[position].getPubDate()) + " Uhr";
            dateView.setText(datum);
        } catch (Exception ex){
            //ignore
        }
        return vi;
    }
}
