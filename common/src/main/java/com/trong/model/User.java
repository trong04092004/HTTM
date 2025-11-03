package com.trong.model;

public class User {

    private int idUser;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String tel;
    private String role;
    public User() {
    }

    public User(int idUser, String fullName, String username, String password, String email, String tel, String role) {
        this.idUser = idUser;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.tel = tel;
        this.role = role;
    }

    // Getters and Setters
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullname(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}