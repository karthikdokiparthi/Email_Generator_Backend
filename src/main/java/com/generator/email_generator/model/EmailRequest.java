package com.generator.email_generator.model;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailContent;
    private String tone;
}
