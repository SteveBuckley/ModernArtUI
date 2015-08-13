package uk.org.stevebuckley.modernartui;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * A placeholder fragment containing a simple view.
 */
public class LeftFragment extends Fragment {

    private static final String TAG="LeftFragment";

    public LeftFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_left,
                container,
                false);

        return layout;

    }

}
