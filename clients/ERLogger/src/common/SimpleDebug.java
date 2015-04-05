package common;

import gui.ToastMessage;

/**
 * Created by max on 2014-09-19.
 */
public class SimpleDebug {

    public static void err(String msg) {
        ToastMessage.getInstance().addMessage(msg);
    }
    public static void out(String msg) {
        System.out.println(msg);
    }
}
