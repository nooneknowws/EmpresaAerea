package br.ufpr.dac.MSClientes.models.dto;

import java.io.Serializable;

public class LoginDTO implements Serializable {
    private Long id; 
    private String email;
    private String senha;
    private String perfil; 

    public LoginDTO() {
        super();
    }
    public LoginDTO(String email, String senha, Long id, String perfil) {
        this.email = email;
        this.senha = senha;
        this.id = id;
        this.perfil = perfil;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
}
