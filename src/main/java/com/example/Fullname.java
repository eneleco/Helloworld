package com.example;

public class Fullname {

    private final long id;
    private final String content;

    public Fullname(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}