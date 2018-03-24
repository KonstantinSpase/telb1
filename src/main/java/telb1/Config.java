package telb1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by User on 10.03.2018.
 */
public class Config {
    public static final Config INSTANCE = new Config();
    public final String KEYSTORE_PATH;
    public final String BOSS_CHAT_ID;
    public final String CAR_PASSWORD_EXPIRED;
    public final String BOT_NAME;
    public final String BOT_TOKEN;
    public final String DATABASE_URL;
    public final String ADMIN_CHAT_ID;
    public final String WASHING_PRICE;
    public final String KEYSTORE_PASSWORD;
    public final String WEBHOOK_EXTERNAL_URL;
    public final String WEBHOOK_INTERNAL_URL;
    public final String CERT_PATH;
    public final String LOG_PATH;

    private Config() {
        String WORK_DIR = ".";
        try {
            WORK_DIR = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
                    .getParentFile().getPath() + "/";
            System.out.println(WORK_DIR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Properties props = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(
                new File(WORK_DIR + "config.properties"))) {
            props.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        BOT_NAME = props.getProperty("bot.name");
        BOT_TOKEN = props.getProperty("bot.token");
        DATABASE_URL = "jdbc:sqlite:" + WORK_DIR + props.getProperty("database.url");
        ADMIN_CHAT_ID = props.getProperty("admin.chat.id");
        BOSS_CHAT_ID = props.getProperty("boss.chat.id");
        CAR_PASSWORD_EXPIRED = props.getProperty("car.password.expiration.time.minutes");
        WASHING_PRICE = props.getProperty("washing.price");
        KEYSTORE_PATH = WORK_DIR + props.getProperty("keystore");
        KEYSTORE_PASSWORD = props.getProperty("keystore.password");
        WEBHOOK_EXTERNAL_URL = props.getProperty("webhook.external.url");
        WEBHOOK_INTERNAL_URL = props.getProperty("webhook.internal.url");
        CERT_PATH = WORK_DIR + props.getProperty("cert");
        LOG_PATH = WORK_DIR + props.getProperty("log");
    }

}
