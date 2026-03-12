package com.organiza.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa um pedido associado a uma mesa.
 */
public class Pedido {
    private int id;
    private int mesaId;
    private String nomeCliente;
    private StatusPedido status;
    private LocalDateTime dataHora;
    private List<ItemPedido> itens;

    public Pedido() {
        this.status = StatusPedido.PENDENTE;
        this.dataHora = LocalDateTime.now();
        this.itens = new ArrayList<>();
    }

    public Pedido(int mesaId, String nomeCliente) {
        this.mesaId = mesaId;
        this.nomeCliente = nomeCliente;
        this.status = StatusPedido.PENDENTE;
        this.dataHora = LocalDateTime.now();
        this.itens = new ArrayList<>();
    }

    public Pedido(int id, int mesaId, String nomeCliente, StatusPedido status, LocalDateTime dataHora) {
        this.id = id;
        this.mesaId = mesaId;
        this.nomeCliente = nomeCliente;
        this.status = status;
        this.dataHora = dataHora;
        this.itens = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMesaId() {
        return mesaId;
    }

    public void setMesaId(int mesaId) {
        this.mesaId = mesaId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public double getTotal() {
        return itens.stream().mapToDouble(ItemPedido::getSubtotal).sum();
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public void addItem(ItemPedido item) {
        this.itens.add(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return id == pedido.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido #" + id + " - Mesa " + mesaId + " (" + nomeCliente + ") - " + status.getLabel();
    }
}
