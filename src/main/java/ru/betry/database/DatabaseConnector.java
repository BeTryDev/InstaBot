package ru.betry.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DatabaseConnector {
    private final EntityManagerFactory managerFactory;
    private EntityManager manager;
    private final UserService userService;
    private final PostService postService;

    public DatabaseConnector(String persistenceUnitName) {
        managerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
        userService = new UserService(this);
        postService = new PostService(this);
    }

    public void startTransaction() {
        if (!isManagerOpened()) {
            manager = createEntityManager();
            manager.getTransaction().begin();
        }
    }

    public void endTransaction() throws RuntimeException {
        if (isManagerOpened()) {
            manager.getTransaction().commit();
            manager.close();
        } else {
            throw new RuntimeException("You must start the transaction first");
        }
    }

    private EntityManager createEntityManager() {
        return managerFactory.createEntityManager();
    }

    private boolean isManagerOpened() {
        return (manager != null && manager.isOpen());
    }

    public EntityManager getManager() {
        return manager;
    }

    public UserService getUserService() {
        return userService;
    }

    public PostService getPostService() {
        return postService;
    }
}
