import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Dialog;
import com.vk.api.sdk.objects.messages.Message;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class DemoClass {

    //private static final Logger LOG = LoggerFactory.getLogger(DemoClass.class);

    private static Properties properties = loadConfiguration();
    private static final int APP_ID = Integer.parseInt(properties.getProperty("app.id"));
    private static final String CLIENT_SECRET = properties.getProperty("app.secret");
    private static final String REDIRECT_URI = properties.getProperty("redirect.uri");
    private static final int GROUP_ID = Integer.parseInt(properties.getProperty("group.id"));
    private static final String GROUP_KEY = properties.getProperty("group.access.token");


    public static void main(String[] args) {
        GroupBot bot = new GroupBot(GROUP_ID, GROUP_KEY);
        bot.connect();
        boolean isWork = true;
        Integer userId;
        List<Message> requests;
        String request;
        try {
            do {
                for (Dialog dialog : bot.getUnreadDialogs()) {
                    userId = bot.getUserIdByDialog(dialog);
                    requests = bot.getUnreadMessagesFromUser(userId, true);
                    for (Message message : requests) {
                        request = message.getBody();
                        if (request.equals("купил") || request.equals("купила") || request.equals("Купил") || request.equals("Купила")) {
                            bot.sendMessage(userId, "Молодец! :)");
                            isWork = false;
                            break;
                        } else {
                            bot.sendMessage(userId, String.format("Все говорят: \"%s\", а ты купи слона!", request));
                        }
                        sleep(500);
                    }
                }
                sleep(500);
            }
            while (isWork);
        } catch (InterruptedException | ClientException | ApiException e) {
            e.printStackTrace();
        }
    }

    private static Properties loadConfiguration() {
        Properties properties = new Properties();
        try (InputStream is = DemoClass.class.getResourceAsStream("/config.properties")) {
            properties.load(is);
        } catch (IOException e) {
            //LOG.error("Can't load properties file", e);
            throw new IllegalStateException(e);
        }

        return properties;
    }
}
