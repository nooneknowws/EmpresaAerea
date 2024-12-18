package com.funcionarios.funcionarios.models.dto;

import java.util.List;

public class FuncionarioDTO {

    public FuncionarioDTO(Long id, String cpf, String nome, String email, EnderecoDTO endereco, String perfil,
			String telefone, String senha, String funcStatus) {
		super();
		this.id = id;
		this.cpf = cpf;
		this.nome = nome;
		this.email = email;
		this.endereco = endereco;
		this.perfil = perfil;
		this.telefone = telefone;
		this.senha = senha;
		this.funcStatus = funcStatus;
	}

	private Long id;
    private String cpf;
    private String nome;
    private String email;
    private EnderecoDTO endereco;
    private String perfil;
    private String telefone;
    private String senha;
    private String funcStatus;

    public FuncionarioDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EnderecoDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoDTO endereco) {
        this.endereco = endereco;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getfuncStatus() {
		return funcStatus;
	}

	public void setfuncStatus(String funcStatus) {
		this.funcStatus = funcStatus;
	}
}
