package rs.elfak.findpet.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import rs.elfak.findpet.R;
import rs.elfak.findpet.data_models.ClusterMarker;

public class ClusterSpinnerAdapter extends ArrayAdapter<ClusterMarker> {
    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private List<ClusterMarker> values;

    LayoutInflater inflator;

    public ClusterSpinnerAdapter(Context context, int textViewResourceId,
                                 List<ClusterMarker> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
        inflator = LayoutInflater.from(context);
    }

    @Override
    public int getCount(){
        return values.size();
    }

    @Override
    public ClusterMarker getItem(int position){
        return values.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = (TextView) inflator.inflate(R.layout.spinner_item, null);
//        TextView label = (TextView) super.getView(position, convertView, parent);
//        label.setTextColor(Color.WHITE);

        label.setText(values.get(position).user.username);
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        TextView label = (TextView) inflator.inflate(R.layout.spinner_item, null);
//        label.setTextColor(Color.WHITE);
        label.setText(values.get(position).user.username);

        return label;
    }
}
