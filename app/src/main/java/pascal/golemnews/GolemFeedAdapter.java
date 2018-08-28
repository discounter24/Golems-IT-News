package pascal.golemnews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GolemFeedAdapter extends BaseAdapter {


    Context context;
    GolemFeedItem[] data;
    private  static LayoutInflater inflater = null;

    public GolemFeedAdapter(Context context, GolemFeedItem[] data){
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
        return  position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null) vi = inflater.inflate(R.layout.feedviewitem, null);

        TextView title = (TextView) vi.findViewById(R.id.itemtitle);
        TextView description = (TextView) vi.findViewById(R.id.itemdescription);
        ImageView preview = (ImageView) vi.findViewById(R.id.preview);

        title.setText(data[position].getTitle());
        description.setText(data[position].getDescription());
        preview.setImageBitmap(data[position].getImage());

        return vi;
    }
}
