package telb1;

import org.joda.time.DateTime;

import org.joda.time.YearMonth;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import telb1.dbl.DbManager;
import telb1.dbl.WasherModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Mybot extends TelegramLongPollingBot {
    public long autorityStatus = 0;

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
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
                WasherModel washer = DbManager.INSTANCE.getWasher(chatId);
                if (washer == null) {
                    sendMessage("you not washer", chatId);
                    break;
                }
                DateTime checkDateTime = DateTime.now();
                DbManager.INSTANCE.cleanExpiredPasswords(checkDateTime);
                carId = DbManager.INSTANCE.getCarIdByPassword(messageText);

                if (carId == null) {
                    sendMessage("wrong or expired password", chatId);
                    break;
                }

                try {
                    DbManager.INSTANCE.washing(carId,  washer.getWasherId(), checkDateTime);
                } catch (SQLException e) {
                    sendMessage("ошибка сервера " , chatId);
                }
                int userWashingsMonth = DbManager.INSTANCE.getUserWashingsMonth(washer.getWasherId(),checkDateTime);
                int pointWashingsMonth = DbManager.INSTANCE.getPointWashingsMonth(washer.getPoint(),checkDateTime);
                sendMessage("мойка подтвержена. итого за месяц: \nперсонально - " +userWashingsMonth+
                        "\nвсего - "+pointWashingsMonth, chatId);
                break;
            case "washerPass":

                try {
                    String washerName=DbManager.INSTANCE.washerRegistration(messageText, chatId);
                    sendMessage("пользователь "+ washerName +" активирован" , chatId);
                } catch (RuntimeException e) {
                    sendMessage(e.getMessage() , chatId);
                }
                break;

            case "date":
                if (!isAdmin(chatId)) {
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
            case "total":
                if (!(isAdmin(chatId)||isBoss(chatId))) {
                    sendMessage("admin permissions need", chatId);
                    break;
                }

              int total= DbManager.INSTANCE.getTotalCurrentMonth();
               // sendMessage("всего за месяц: " + total, chatId);
               List<String> totalPerPoint = DbManager.INSTANCE.getMonthReport1(new YearMonth(DateTime.now().getMillis()));
                StringBuilder sb=new StringBuilder();
                sb.append("всего за месяц:    " + total+
                                "\nвсего за месяц($): " + total*250+
                        "\n-----------------------------\n");
                for (String s:totalPerPoint){
                    sb.append(s+"\n");
                }
                sendMessage(sb.toString(), chatId);
                break;
            default:
                sendMessage("команда " + messageType+" не существует", chatId);
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