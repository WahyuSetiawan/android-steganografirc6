package siapasaja.steganografirc6.ProgressDialog;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by SiapaSaja on 12/16/2015.
 */
public class ProgressClass extends ProgressDialog {

    public ProgressClass(Context context) {
        super(context);
        setProgressStyle(STYLE_HORIZONTAL);
    }
}
