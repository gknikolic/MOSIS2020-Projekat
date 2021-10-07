package rs.elfak.findpet.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.R;

public class SpinnerWithPlaceholderAdapter extends ArrayAdapter {

    public SpinnerWithPlaceholderAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
    }

    private boolean firstOptionEnabled = false;

    public void setFirstOptionDisabled() {firstOptionEnabled = false;}
    public void setFirstOptionEnabled() {firstOptionEnabled = true;}

    @Override
    public boolean isEnabled(int position){
        if(firstOptionEnabled) {
            return true;
        }
        else {
            if(position == 0)
            {
                // Disable the first item from Spinner
                // First item will be use for hint
                return false;
            }
            else
            {
                return true;
            }
        }
    }
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView tv = (TextView) view;
        if(position == 0){
            // Set the hint text color gray
            tv.setTextColor(Color.GRAY);
        }
        else {
            tv.setTextColor(getContext().getResources().getColor(R.color.design_default_color_on_primary));
        }
        return view;
    }
}
