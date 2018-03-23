package telb1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by User on 10.03.2018.
 */
public class Config {
    public static final Config INSTANCE = new Config();
    public final String KEYSTORE;
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
    public final String CERT;

    private Config() {
        Properties props = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(
                new File(Mybot.WORK_DIR+"config.properties"))) {
            props.load(fileInputStream);
            BOT_NAME = props.getProperty("bot.name");
            BOT_TOKEN = props.getProperty("bot.token");
            DATABASE_URL = props.getProperty("database.url");
            ADMIN_CHAT_ID = props.getProperty("admin.chat.id");
            BOSS_CHAT_ID = props.getProperty("boss.chat.id");
            CAR_PASSWORD_EXPIRED = props.getProperty("car.password.expiration.time.minutes");
            WASHING_PRICE = props.getProperty("washing.price");
            KEYSTORE = props.getProperty("keystore");
            KEYSTORE_PASSWORD = props.getProperty("keystore.password");
            WEBHOOK_EXTERNAL_URL = props.getProperty("webhook.external.url");
            WEBHOOK_INTERNAL_URL = props.getProperty("webhook.internal.url");
            CERT = props.getProperty("cert");


        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
