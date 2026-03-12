package com.organiza.application.usecase;

import com.organiza.application.dto.MesaDTO;
import com.organiza.domain.entity.Mesa;
import com.organiza.domain.repository.MesaRepositoryPort;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso para listagem e consulta de mesas.
 */
public class ListTablesUseCase {

    private final MesaRepositoryPort mesaRepository;

    public ListTablesUseCase(MesaRepositoryPort mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    /**
     * Lista todas as mesas como DTOs.
     */
    public List<MesaDTO> listAll() {
        return mesaRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Busca uma mesa por ID.
     */
    public Optional<MesaDTO> findById(int id) {
        return mesaRepository.findById(id).map(this::toDTO);
    }

    private MesaDTO toDTO(Mesa mesa) {
        return new MesaDTO(
                mesa.getId(),
                mesa.getNumero(),
                mesa.getStatus().name(),
                mesa.getStatus().getLabel()
        );
    }
}
