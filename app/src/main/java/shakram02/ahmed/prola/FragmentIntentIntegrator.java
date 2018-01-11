package shakram02.ahmed.prola;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;

/**
 * Makes fragments handle {@link Activity#onActivityResult} not their parent {@link Activity}
 */

public class FragmentIntentIntegrator extends IntentIntegrator {
    private final Fragment fragment;

    /**
     * @param fragment {@link Fragment} invoking the integration
     */
    public FragmentIntentIntegrator(Fragment fragment) {
        super(fragment.getActivity());
        this.fragment = fragment;
    }

    @Override
    protected void startActivityForResult(Intent intent, int code) {
        fragment.startActivityForResult(intent, code);
    }
}
