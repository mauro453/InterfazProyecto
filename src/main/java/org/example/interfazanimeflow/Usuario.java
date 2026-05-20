package org.example.interfazanimeflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Evita errores si tu backend envía más datos de la cuenta
public class Usuario {
    private Long id;
    private String username; // o 'email', según lo que devuelva tu backend

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}