package com.organiza.model;

/**
 * Representa um cliente cadastrado.
 */
public class Cliente {

    private int id;
    private String nome;
    private String telefone;

    public Cliente() {}

    public Cliente(int id, String nome, String telefone) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
    }

    public Cliente(String nome, String telefone) {
        this(0, nome, telefone);
    }

    public int getId()             { return id; }
    public void setId(int id)      { this.id = id; }

    public String getNome()             { return nome; }
    public void setNome(String nome)    { this.nome = nome; }

    public String getTelefone()              { return telefone; }
    public void setTelefone(String tel)      { this.telefone = tel; }

    /** Label de exibição nos dropdowns. */
    @Override
    public String toString() {
        if (telefone != null && !telefone.isBlank()) {
            return nome + "  (" + telefone + ")";
        }
        return nome;
    }
}
