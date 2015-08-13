package uk.org.stevebuckley.modernartui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * A placeholder fragment containing a simple view.
 */
public class RightFragment extends Fragment {

    private static final String TAG="RightFragment";

    public RightFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_right,
                container,
                false);

        return layout;

    }

}
