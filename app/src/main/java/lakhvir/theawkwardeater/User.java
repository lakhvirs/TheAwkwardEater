package lakhvir.theawkwardeater;

import android.util.Log;

/**
 * Created by LakhvirSingh on 26/01/15.
 */
public class User {

    public static String username = null;
    public static int userID = 0;

    public static int get_userid() {
        Log.d("User.get_userid", Integer.toString(userID));
        return userID;
    }

    public static void set_userid(int userid) {
        Log.d("User.set_userid", Integer.toString(userid));
        User.userID = userid;
    }


}
