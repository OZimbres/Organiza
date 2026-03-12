package com.organiza.application.dto;

/**
 * DTO imutável para transferência de dados de mesa.
 */
public record MesaDTO(int id, int numero, String status, String statusLabel) {
}
