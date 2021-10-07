package rs.elfak.findpet.Helpers;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetArrayListFromStream<T> {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<T> get(Stream<T> stream)
    {

        // Convert the Stream to List
        List<T>
                list = stream.collect(Collectors.toList());

        // Create an ArrayList of the List
        ArrayList<T>
                arrayList = new ArrayList<T>(list);

        // Return the ArrayList
        return arrayList;
    }
}
