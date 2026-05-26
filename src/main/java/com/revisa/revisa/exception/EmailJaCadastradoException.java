package com.revisa.revisa.exception;

public class EmailJaCadastradoException extends RuntimeException {

    private final String email;

    public EmailJaCadastradoException(String email) {
        super("Email já cadastrado: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}