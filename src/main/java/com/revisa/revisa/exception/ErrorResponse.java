package com.revisa.revisa.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private int status;
    private String mensagem;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String mensagem, LocalDateTime timestamp) {
        this.status = status;
        this.mensagem = mensagem;
        this.timestamp = timestamp;
    }

    public int getStatus() { return status; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getTimestamp() { return timestamp; }
}