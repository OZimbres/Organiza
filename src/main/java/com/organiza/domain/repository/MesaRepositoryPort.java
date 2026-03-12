package com.organiza.domain.repository;

import com.organiza.domain.entity.Mesa;
import com.organiza.domain.enums.StatusMesa;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) para operações de persistência de mesas.
 * Segue o princípio da Arquitetura Hexagonal — o domínio define o contrato,
 * e a infraestrutura fornece a implementação.
 */
public interface MesaRepositoryPort {

    Mesa save(Mesa mesa);

    Optional<Mesa> findById(int id);

    Optional<Mesa> findByNumero(int numero);

    List<Mesa> findAll();

    void updateStatus(int id, StatusMesa status);

    void deleteById(int id);
}
