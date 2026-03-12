package com.organiza.domain.entity;

import com.organiza.domain.enums.StatusMesa;

import java.util.Objects;

/**
 * Representa uma mesa do estabelecimento.
 */
public class Mesa {
    private int id;
    private int numero;
    private StatusMesa status;

    public Mesa() {
        this.status = StatusMesa.LIVRE;
    }

    public Mesa(int numero) {
        this.numero = numero;
        this.status = StatusMesa.LIVRE;
    }

    public Mesa(int id, int numero, StatusMesa status) {
        this.id = id;
        this.numero = numero;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public StatusMesa getStatus() {
        return status;
    }

    public void setStatus(StatusMesa status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mesa mesa = (Mesa) o;
        return id == mesa.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Mesa " + numero + " - " + status.getLabel();
    }
}
