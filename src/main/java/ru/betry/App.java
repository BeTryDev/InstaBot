package ru.betry;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class App {
    public static void main(String[] args) throws IOException {
        // выгрузка настроек
        Properties properties = new Properties();
        properties.load(new FileInputStream("app.properties"));
        String telegramToken = properties.getProperty("telegram_token");
        BetryBot betryBot;

        if (telegramToken != null) {
            betryBot = new BetryBot(telegramToken, "instabot");
        } else {
            throw new RuntimeException("Telegram token is required");
        }

        betryBot.listenUpdates();
    }
}
