package ru.betry;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import ru.betry.database.DatabaseConnector;
import ru.betry.entity.Post;
import ru.betry.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class App {

	public static void main(String[] args) throws IOException {

		DatabaseConnector connector = new DatabaseConnector("instabot");

		// выгрузка настроек
		Properties properties = new Properties();
		properties.load(new FileInputStream("app.properties"));
		TelegramBot bot = new TelegramBot(properties.getProperty("telegram_token"));

		bot.setUpdatesListener(updates -> {
			updates.forEach(System.out::println);

			updates.forEach(update -> {
				Integer userId = update.message().from().id();
				connector.startTransaction();
				User user = connector.getUserService().findById(userId);
				if (user == null) { // проверка наличия пользователя в MongoDB>
					bot.execute(new SendMessage(update.message().chat().id(),
							"Вам необходимо прислать логин и пароль в одном предложении через пробел"));
					connector.getUserService().save(new User(update.message().from().id(), null, null));
				} else if (user.getLogin() == null) { // запись логина и пароля

					String[] loginAndPassword = update.message().text().split(" ");
					user.setLogin(loginAndPassword[0]);
					user.setPassword(loginAndPassword[1]);

					connector.getUserService().save(user);


					bot.execute(new SendMessage(update.message().chat().id(),
							"Все работает! Теперь вы можете присылать нам текст/изображение для " +
									"Instagram (в одном сообщении)"));
				} else if (user.getLogin() != null && update.message().photo().length > 0){

					GetFileResponse fileResponse = bot.execute(new GetFile(update.message().photo()[0].fileId()));
					String fullPath = bot.getFullFilePath(fileResponse.file());
					try {
						HttpDownload.downloadFile(fullPath, "./images", update.message().photo()[0].fileId() + ".jpg");
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}

					Post post = new Post();
					post.setTitle(update.message().caption());
					post.setPhoto(new File("./images/" + update.message().photo()[0].fileId() + ".jpg").getPath());
					user.addPost(post);

					connector.getPostService().save(post);
					connector.getUserService().save(user);
				}

				connector.endTransaction();
			});

			return UpdatesListener.CONFIRMED_UPDATES_ALL;
		});
	}

}
