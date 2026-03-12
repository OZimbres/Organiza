package com.organiza.domain.entity;

import java.util.Objects;

/**
 * Representa um item dentro de um pedido.
 */
public class ItemPedido {
    private int id;
    private int pedidoId;
    private String produto;
    private int quantidade;
    private double preco;

    public ItemPedido() {
    }

    public ItemPedido(String produto, int quantidade, double preco) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public ItemPedido(int id, int pedidoId, String produto, int quantidade, double preco) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.produto = produto;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(int pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public double getSubtotal() {
        return quantidade * preco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPedido that = (ItemPedido) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return quantidade + "x " + produto;
    }
}
