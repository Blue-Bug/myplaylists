package com.myplaylists.web.mail;


import lombok.Data;

@Data
public class EmailContent {
    private String to;
    private String subject;
    private String body;
}
