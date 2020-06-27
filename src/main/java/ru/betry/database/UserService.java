package ru.betry.database;

import ru.betry.entity.User;

public class UserService implements DatabaseService<User> {
    private DatabaseConnector parentConnector;

    public UserService(DatabaseConnector parentConnector) {
        this.parentConnector = parentConnector;
    }

    @Override
    public User findById(Object primaryKey) {
        return this.parentConnector.getManager().find(User.class, primaryKey);
    }

    @Override
    public void save(User object) {
        this.parentConnector.getManager().persist(object);
    }
}
