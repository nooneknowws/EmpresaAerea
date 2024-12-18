package br.ufpr.dac.MSReserva.dto;

import br.ufpr.dac.MSReserva.model.Aeroporto;

public class AeroportoDTO {
    private String codigo;
    private String nome;
    private String cidade;
    private String estado;
    private String pais;

    // Default constructor
    public AeroportoDTO() {}

    // Constructor from entity
    public AeroportoDTO(Aeroporto aeroporto) {
        if (aeroporto != null) {
            this.codigo = aeroporto.getCodigo();
            this.nome = aeroporto.getNome();
            this.cidade = aeroporto.getCidade();
            this.estado = aeroporto.getEstado();
            this.pais = aeroporto.getPais();
        }
    }

    // Constructor with all fields
    public AeroportoDTO(String codigo, String nome, String cidade, String estado, String pais) {
        this.codigo = codigo;
        this.nome = nome;
        this.cidade = cidade;
        this.estado = estado;
        this.pais = pais;
    }

    // Convert DTO to Entity
    public Aeroporto toEntity() {
        Aeroporto aeroporto = new Aeroporto();
        aeroporto.setCodigo(this.codigo);
        aeroporto.setNome(this.nome);
        aeroporto.setCidade(this.cidade);
        aeroporto.setEstado(this.estado);
        aeroporto.setPais(this.pais);
        return aeroporto;
    }

    // Getters and Setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }
}