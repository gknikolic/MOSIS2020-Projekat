package rs.elfak.findpet.Helpers;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Helpers {

    public static String formatDate(@Nullable Date date) {
        String pattern = "MM/dd/yyyy HH:mm:ss";

        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date according to the chosen pattern
        DateFormat df = new SimpleDateFormat(pattern);

        Date dateForFormat;
        if(date == null) {
            // Get the today date using Calendar object.
            dateForFormat = Calendar.getInstance().getTime();
        }
        else {
            dateForFormat = date;
        }

        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        return df.format(dateForFormat);
    }
}
