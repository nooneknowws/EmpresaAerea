package br.ufpr.dac.MSClientes.models;

import jakarta.persistence.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "usuarios")  
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String salt;

    @Embedded
    private Endereco endereco;

    @Column(nullable = false)
    private String perfil;

    private String telefone;

    @Column(name = "saldo_milhas")
    private Double saldoMilhas;
    
    @OneToMany(mappedBy = "cliente",fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Milhas> milhas = new HashSet<>();

    // Constructors
    public Usuario() {}

    public Usuario(String cpf, String nome, String email, String senha, Endereco endereco, String perfil, String telefone, Double saldoMilhas) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.salt = gerarSalt();
        this.senha = hashSenha(senha, this.salt);
        this.endereco = endereco;
        this.perfil = perfil;
        this.telefone = telefone;
        this.saldoMilhas = saldoMilhas;
    }

    // Password handling methods
    private String gerarSalt() {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashSenha(String senha, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(senha.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criptografar a senha", e);
        }
    }

    public boolean verificarSenha(String senhaEntrada) {
        String senhaEntradaHash = hashSenha(senhaEntrada, this.salt);
        return senhaEntradaHash.equals(this.senha);
    }

    // Getters and Setters
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

    public String getSenha() {
        return senha;
    }
    
    public void setInitialPassword(String senha) {
        this.salt = gerarSalt();
        this.senha = hashSenha(senha, this.salt);
    }
    
    public void setSenha(String senha) {
        if (this.salt == null) {
            setInitialPassword(senha);
        } else {
            this.senha = hashSenha(senha, this.salt);
        }
    }
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
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

    public Double getSaldoMilhas() {
        return saldoMilhas;
    }

    public void setSaldoMilhas(Double saldoMilhas) {
        this.saldoMilhas = saldoMilhas;
    }

    public Set<Milhas> getMilhas() {
        return milhas;
    }

    public void setMilhas(Set<Milhas> milhas) {
        this.milhas = milhas;
    }
}