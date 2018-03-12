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
    public   final String CAR_PASSWORD_EXPIRED ;
    public final String BOT_NAME;
    public final String BOT_TOKEN;
   public final String DATABASE_URL;
public final  String ADMIN_CHAT_ID;
    private Config() {
        Properties props = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(
                new File("./config.properties"))) {
            props.load(fileInputStream);
            BOT_NAME = props.getProperty("bot.name");
            BOT_TOKEN = props.getProperty("bot.token");
            DATABASE_URL=props.getProperty("database.url");
            ADMIN_CHAT_ID=props.getProperty("admin.chat.id");
            CAR_PASSWORD_EXPIRED=props.getProperty("car.password.expiration.time.minutes");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
