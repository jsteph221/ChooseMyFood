package elapse.choosemyfood;

import java.util.ArrayList;

/**
 * Created by Joshua on 9/8/2017.
 */

public interface VolleyDetailCallback {
    void onSuccess(Restaurant rest);
    void onFailure();
}
