package telb1;

import org.joda.time.DateTime;

import org.joda.time.YearMonth;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import telb1.dbl.DbManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Handler extends TelegramWebhookBot {
    public long autorityStatus = 0;
    private static Logger logger = Logger.getLogger("telb1.Handler");
    public static void main(String[] args) {
        java.util.logging.Handler fh = null;
        try {
            fh = new FileHandler(Config.INSTANCE.LOG_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.getLogger("").addHandler(fh);
        logger.setLevel(Level.ALL);
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(
                    Config.INSTANCE.KEYSTORE_PATH,
                    Config.INSTANCE.KEYSTORE_PASSWORD,
                    Config.INSTANCE.WEBHOOK_EXTERNAL_URL,
                    Config.INSTANCE.WEBHOOK_INTERNAL_URL,
                    Config.INSTANCE.CERT_PATH);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
        try {
            telegramBotsApi.registerBot(new Handler());

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getBotUsername() {
        return Config.INSTANCE.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Config.INSTANCE.BOT_TOKEN;
    }

    @Override
    public String getBotPath() {
        return "h2o";
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {

        Message message = update.getMessage();
        if (message == null || !message.hasText()) return null;
        String messageText = message.getText();
        Long chatId = message.getChatId();
        String user = message.getChat().getFirstName();
        String messageType = getMessageType(messageText);
        switch (messageType) {
            case "carNumber":
                if (!isAdmin(chatId)) {
                    sendMessage("admin permissions need", chatId);
                    break;
                }
                Integer carId = DbManager.INSTANCE.getCarId(messageText);
                if (carId == null) {
                    logger.info("wrong car number "+messageText+" by user: " +user );
                    sendMessage("wrong car number", chatId);
                    break;
                }
                DateTime requestDataTime = DateTime.now();
                Integer cw = DbManager.INSTANCE.getCarWashingsInCurrentMonth(carId, requestDataTime);
                sendMessage(cw.toString(), chatId);

                DbManager.INSTANCE.cleanExpiredPasswords(requestDataTime);
                String carPass = null;
                try {
                    carPass = DbManager.INSTANCE.createCarPass(carId, requestDataTime);
                } catch (SQLException e) {
                    sendMessage(e.getMessage(), chatId);
                    break;
                }
                sendMessage(carPass, chatId);
                break;
            case "carPass":
                Integer washerId = DbManager.INSTANCE.getWasherId(chatId);
                if (washerId == null) {
                    sendMessage("телефон не зарегистрирован", chatId);
                    break;
                }
                DateTime checkDateTime = DateTime.now();
                DbManager.INSTANCE.cleanExpiredPasswords(checkDateTime);
                carId = DbManager.INSTANCE.getCarIdByPassword(messageText);

                if (carId == null) {
                    sendMessage("неправильный или просроченый пароль", chatId);
                    break;
                }
                String carNumber = DbManager.INSTANCE.getCarNumber(carId);
                if (carNumber == null) {
                    sendMessage("автомобиль отсутствует в базе", chatId);
                    break;
                }
                try {
                    DbManager.INSTANCE.washing(carId, washerId, checkDateTime);
                } catch (SQLException e) {
                    sendMessage("ошибка сервера ", chatId);
                }
                int pointWashingsMonth = DbManager.INSTANCE.getPointWashingsMonth(washerId, checkDateTime);
                sendMessage("Мойка " + carNumber + " подтверждена. \nИтого за месяц: " + pointWashingsMonth, chatId);
                break;
            case "washerPass":

                try {
                    String point = DbManager.INSTANCE.washerRegistration(messageText, chatId, user);
                    sendMessage("администратор мойки " + point + " активирован", chatId);
                } catch (RuntimeException e) {
                    sendMessage(e.getMessage(), chatId);
                }
                break;

            case "date":
                if (!(isAdmin(chatId) || isBoss(chatId))) {
                    sendMessage("admin permissions need", chatId);
                    break;
                }
                ByteArrayOutputStream out = null;
                try {
                    out = new ReportBuilder().build(messageText);
                    SendDocument sendDocument = new SendDocument()
                            .setChatId(chatId)
                            .setNewDocument("report.xlsx",
                                    new ByteArrayInputStream(out.toByteArray()));
                    sendDocument(sendDocument);
                    out.close();
                } catch (IOException | TelegramApiException e) {
                    throw new RuntimeException(e.getMessage());
                }
                break;
            case "Total":
                if (!(isAdmin(chatId) || isBoss(chatId))) {
                    sendMessage("admin permissions need", chatId);
                    break;
                }
//
                int total = DbManager.INSTANCE.getTotalCurrentMonth();
                // sendMessage("всего за месяц: " + total, chatId);
                List<String> totalPerPoint = DbManager.INSTANCE.getMonthReport1(new YearMonth(DateTime.now().getMillis()));
                StringBuilder sb = new StringBuilder();
                sb.append("всего за месяц:    " + total +
                        "\nвсего за месяц(р): " + total * 250 +
                        "\n-----------------------------\n");
                for (String s : totalPerPoint) {
                    sb.append(s + "\n");
                }
                sendMessage(sb.toString(), chatId);
                break;
            default:
                sendMessage("команда " + messageType + " не существует", chatId);
        }
        return null;
    }

    private void sendMessage(String messageText, Long chatId) {
        SendMessage response = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chatId)
                .setText(messageText);
        try {
            execute(response); // Call method to send the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getMessageType(String message) {
        if (message.matches("\\d{4}")) return "carPass";
        if (message.matches("[А-Яа-яЁё]\\d{3}[А-Яа-яЁё]{2}\\d{2,3}")) return "carNumber";
        if (message.length() == 6) return "washerPass";
        if (message.matches("^(?:0[1-9]|1[0-2])/[0-9]{2}$")) return "date";
        return message;
    }

    private boolean isAdmin(Long chatId) {
        return Config.INSTANCE.ADMIN_CHAT_ID.contains(chatId.toString());
    }

    private boolean isBoss(Long chatId) {
        return Config.INSTANCE.BOSS_CHAT_ID.contains(chatId.toString());
    }

}