package ru.betry.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    private Integer id;

    private String login;
    private String password;
    @OneToMany
    private List<Post> postList;

    public User() { }

    public User(Integer id, String login, String password) {
        postList = new ArrayList<>();
        this.id = id;
        this.login  = login;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Post> getPostList() {
        return postList;
    }

    public void addPost(Post post) {
        this.postList.add(post);
    }

}
