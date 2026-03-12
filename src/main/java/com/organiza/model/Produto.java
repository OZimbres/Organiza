package com.organiza.model;

/**
 * Representa um produto do cardápio.
 */
public class Produto {

    private int id;
    private String nome;
    private double preco;
    private String categoria;

    public Produto() {}

    public Produto(int id, String nome, double preco, String categoria) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
    }

    public Produto(String nome, double preco, String categoria) {
        this(0, nome, preco, categoria);
    }

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getNome()               { return nome; }
    public void setNome(String nome)      { this.nome = nome; }

    public double getPreco()              { return preco; }
    public void setPreco(double preco)    { this.preco = preco; }

    public String getCategoria()                  { return categoria; }
    public void setCategoria(String categoria)    { this.categoria = categoria; }

    /** Label de exibição nos dropdowns — inclui preço. */
    @Override
    public String toString() {
        return String.format("%s  —  R$ %.2f", nome, preco);
    }
}
