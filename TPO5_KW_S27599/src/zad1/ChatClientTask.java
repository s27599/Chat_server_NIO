/**
 * @author Kaczor Wiktor S27599
 */

package zad1;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<String> {

    private ChatClient client;

    public ChatClientTask(ChatClient c, List<String> msgs, int wait) {
        super(() -> {
            try {
                c.login();
                if (wait > 0) {
                    Thread.sleep(wait);
                }
                for (String req : msgs) {
                    c.send(req);
                    if (wait > 0) {
                        Thread.sleep(wait);
                    }
                }
                c.logout();
                if (wait > 0) {
                    Thread.sleep(wait);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        this.client = c;
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c, msgs, wait);
    }

    public ChatClient getClient() {
        return client;
    }
}
