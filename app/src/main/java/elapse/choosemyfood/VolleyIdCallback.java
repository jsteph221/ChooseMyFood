package elapse.choosemyfood;

import java.util.ArrayList;

/**
 * Created by Joshua on 9/8/2017.
 */

public interface VolleyIdCallback {
    void onSuccess(ArrayList<String> ids);
    void onFailure();
}
