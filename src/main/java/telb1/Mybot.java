package telb1;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Mybot extends TelegramLongPollingBot {
    public long autorityStatus = 0;

    public static void main(String[] args)  {


        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            telegramBotsApi.registerBot(new Mybot());
            //String fileName = bufferedReader.readLine();
            //autority.greateFile(fileName);

            //fileName = bufferedReader.readLine();
            //System.out.println(autority.getText(fileName));
            //String Snum = bufferedReader.readLine();
            //int num = Integer.parseInt(Snum);
            //System.out.println(fileName);
            //System.out.println(Snum);
            //autority.setText(fileName, Snum);
            //System.out.println(autority.getText(fileName));


        } catch (TelegramApiException e) {
            e.printStackTrace();
        } //catch (IOException e){

        //  System.out.println("err");
        //}
    }

    @Override
    public String getBotUsername() {
        return "my_h2o_bot";
    }

    @Override
    public String getBotToken() {
        return "502797087:AAFg_VfpUGfEqoLwJiuuuMiO4NuuFI8Umzs";
    }

    @Override
    public void onUpdateReceived(Update update) {

        System.out.println("update");
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            long chatId = message.getChatId();
            String strchatId = Long.toString(chatId);

            //if (autority.checkFile(message.getText()).equals("exist") ) {}

            //if(autority.checkFile(strchatId).equals("exist")){ sendMsg(message, "Вы вошли как " + strchatId);}else{sendMsg(message, "Авторизуйтесь");}

            //if (autority.greateFile(strchatId) != 0){sendMsg("Введите пароль"); }
            //if (message.getText().equals("Пароль")){sendMsg(message,"Введите пароль");}

            if (chatId == 458108952) {
                sendMsg(message, "Привет Ярослав");
                DbManager dbManager=new DbManager();
                try {
                    sendMsg(message,dbManager.connect());
                } catch (ClassNotFoundException e) {
                    sendMsg(message, e.getMessage());
                } catch (SQLException e) {
                    sendMsg(message, e.getMessage());
                }
            }
            if (chatId == 539108508) {
                sendMsg(message, "Привет Константин");
            }

            //if (message.getText().equals("/help"))
            //  sendMsg(message, "Привет, я робот");
            //else
            //  sendMsg(message, "Я не знаю что ответить на это");

            System.out.println(message.getChatId());
        }
    }


    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add("/help");
        keyboardFirstRow.add("Комманда 2");

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add("Комманда 3");
        keyboardSecondRow.add("Комманда 4");

        // Добавляем все строчки клавиатуры в список

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}