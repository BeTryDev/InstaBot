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
        String persistenceName = properties.getProperty("persistence_name");
        BetryBot betryBot;

        if (telegramToken != null && persistenceName != null) {
            betryBot = new BetryBot(telegramToken, persistenceName);
        } else {
            throw new RuntimeException("Telegram token and database persistence unit name are required");
        }

        betryBot.listenUpdates();
    }
}
