package telb1;

import org.sqlite.core.DB;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import telb1.dbl.DbManager;
import telb1.dbl.PasswordModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;

public class Mybot extends TelegramLongPollingBot {
    public long autorityStatus = 0;

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
       // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            telegramBotsApi.registerBot(new Mybot());

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
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();
        if (message == null || !message.hasText()) return;
        String messageText = message.getText();
        Long chatId = message.getChatId();
        String messageType = getMessageType(messageText);
        switch (messageType) {
            case "carNumber":
                if (!isAdmin(chatId)) {
                    sendMessage("admin permissions need", chatId);
                    break;
                }
                Integer carId = DbManager.INSTANCE.getCarId(messageText);
                if (carId == null) {
                    sendMessage("wrong car number", chatId);
                    break;
                }
                DbManager.INSTANCE.cleanExpiredPasswords();
                String carPass = null;
                try {
                    carPass = DbManager.INSTANCE.createCarPass(carId);
                } catch (SQLException e) {
                    sendMessage(e.getMessage(), chatId);
                    break;
                }
                sendMessage(carPass, chatId);
                break;
            case "carPass":
                Integer washerId=DbManager.INSTANCE.getWasherId(chatId);
                if (washerId == null) {
                    sendMessage("you not washer", chatId);
                    break;
                }
                DbManager.INSTANCE.cleanExpiredPasswords();
                PasswordModel passwordModel = DbManager.INSTANCE.getPasswordModel(messageText);

                if (passwordModel == null) {
                    sendMessage("wrong or expired password", chatId);
                    break;
                }
                Integer washingId= null;
                try {
                    washingId = DbManager.INSTANCE.washing(passwordModel,chatId,washerId);
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage());
                }
                sendMessage("washing accepted " + washingId, chatId);
                break;
            case "washerPass":
                if (!DbManager.INSTANCE.checkWasherPassword(messageText)) {
                    sendMessage("wrong pass" + messageText, chatId);
                    break;
                }
                try {
                    DbManager.INSTANCE.washerRegistration(messageText, chatId);
                    sendMessage("you in" + messageText, chatId);
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage());
                }
                break;

            default:
                sendMessage("wrong command " + messageType, chatId);
        }

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
        return message;
    }

    private boolean isAdmin(Long chatId) {
        return Config.INSTANCE.ADMIN_CHAT_ID.contains(chatId.toString());
    }

}