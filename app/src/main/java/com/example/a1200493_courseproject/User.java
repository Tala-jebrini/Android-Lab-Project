package com.example.a1200493_courseproject;

public class User {

    private String Email;
    private String FirstName;
    private String LastName;
    private String Password;

    public User(String email, String firstName, String lastName, String password) {
        Email = email;
        FirstName = firstName;
        LastName = lastName;
        Password = password;
    }

    public String getEmail() {
        return Email;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getPassword() {
        return Password;
    }
}
