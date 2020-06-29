package ru.betry;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramUploadPhotoRequest;
import ru.betry.database.DatabaseConnector;
import ru.betry.entity.Post;
import ru.betry.entity.User;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BetryBot {
    private final TelegramBot bot;
    private final DatabaseConnector connector;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public BetryBot(String telegramToken, String databaseUnitName) {
        this.bot = new TelegramBot(telegramToken);
        this.connector = new DatabaseConnector(databaseUnitName);
    }

    public void listenUpdates() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(System.out::println);

            updates.forEach(update -> {
                Integer userId = update.message().from().id();
                connector.startTransaction();
                User user = connector.getUserService().findById(userId);

                if (user == null) { // проверка наличия пользователя в MongoDB>
                    processNewUser(update);
                } else if (user.getLogin() == null) { // запись логина и пароля
                    processLoginAndPassword(update, user);
                } else if (user.getLogin() != null && update.message().photo() != null) {
                    if (update.message().photo().length == 1) {
                        processNewInstagramPost(update, user);
                    } else {
                        bot.execute(new SendMessage(update.message().chat().id(),
                                "Пока что я умею работать только с одним изображением!"));
                    }
                }

                connector.endTransaction();
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void processNewUser(Update update) {
        bot.execute(new SendMessage(update.message().chat().id(),
                "Вам необходимо прислать логин и пароль в одном предложении через пробел"));
        connector.getUserService().save(new User(update.message().from().id(), null, null));
    }

    private void processLoginAndPassword(Update update, User user) {
        String[] loginAndPassword = update.message().text().split(" ");
        user.setLogin(loginAndPassword[0]);
        user.setPassword(loginAndPassword[1]);

        connector.getUserService().save(user);


        bot.execute(new SendMessage(update.message().chat().id(),
                "Все работает! Теперь вы можете присылать нам текст, изображение и дату публикации" +
                        "(формат: чч:мм дд.ММ.гггг, в начале сообщения) для Instagram (в одном сообщении)"));
    }

    private void processNewInstagramPost(Update update, User user) {
        GetFileResponse fileResponse = bot.execute(new GetFile(update.message().photo()[0].fileId()));
        String fullPath = bot.getFullFilePath(fileResponse.file());
        try {
            HttpDownload.downloadFile(fullPath, "./images", update.message().photo()[0].fileId() + ".jpg");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        SimpleDateFormat parser = new SimpleDateFormat("hh:mm dd.MM.yyyy");
        Date datePost = null;
        try {
            datePost = parser.parse(update.message().caption().split("\n")[0]);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }

        Runnable sendToInstagram = () -> {
            Instagram4j instagram4j = Instagram4j.builder()
                    .username(user.getLogin())
                    .password(user.getPassword())
                    .build();
            instagram4j.setup();
            try {
                instagram4j.login();
                instagram4j.sendRequest(new InstagramUploadPhotoRequest(
                        new File("./images/" + update.message().photo()[0].fileId() + ".jpg"),
                        update.message().caption().replace(
                                update.message().caption().split("\n")[0] + "\n",
                                "")));
            } catch (IOException e) {
                System.err.println("Instagram error: " + e.getMessage());
            }
        };

        System.out.println(datePost.getTime() - System.currentTimeMillis() + "");
        scheduler.schedule(sendToInstagram, datePost.getTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);

        Post post = new Post();
        post.setDate(datePost);
        post.setTitle(update.message().caption().replace(
                update.message().caption().split("\n")[0] + "\n",
                ""));
        post.setPhoto(new File("./images/" + update.message().photo()[0].fileId() + ".jpg").getPath());
        user.addPost(post);

        connector.getPostService().save(post);
        connector.getUserService().save(user);
    }
}
