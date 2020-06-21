package ru.betry.database;

import ru.betry.entity.Post;

public class PostService implements DatabaseService<Post> {
    private DatabaseConnector parentConnector;

    public PostService(DatabaseConnector parentConnector) {
        this.parentConnector = parentConnector;
    }

    @Override
    public Post findById(Object primaryKey) {
        return this.parentConnector.getManager().find(Post.class, primaryKey);
    }

    @Override
    public void save(Post object) {
        this.parentConnector.getManager().persist(object);
    }
}
