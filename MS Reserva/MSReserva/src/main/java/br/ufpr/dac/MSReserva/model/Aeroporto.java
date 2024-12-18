package br.ufpr.dac.MSReserva.model;

import jakarta.persistence.*;

@Embeddable
public class Aeroporto {
    @Column(name = "nome")
    private String nome;
    
    @Column(name = "codigo")
    private String codigo;
    
    @Column(name = "cidade")
    private String cidade;
    
    @Column(name = "estado")
    private String estado;
    
    @Column(name = "pais")
    private String pais;
    
    public Aeroporto() {}
    
    public Aeroporto(String nome, String codigo, String cidade, String estado, String pais) {
        this.nome = nome;
        this.codigo = codigo;
        this.cidade = cidade;
        this.estado = estado;
        this.pais = pais;
    }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
}
