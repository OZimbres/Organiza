package com.organiza.infrastructure.config;

import com.organiza.application.usecase.CreateOrderUseCase;
import com.organiza.application.usecase.GenerateReportsUseCase;
import com.organiza.application.usecase.ListTablesUseCase;
import com.organiza.application.usecase.UpdateOrderStatusUseCase;
import com.organiza.domain.repository.MesaRepositoryPort;
import com.organiza.domain.repository.PedidoRepositoryPort;

/**
 * Factory para criação de casos de uso.
 * Centraliza a injeção de dependências dos use cases.
 */
public class UseCaseFactory {

    private final RepositoryFactory repositoryFactory;

    public UseCaseFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public CreateOrderUseCase createOrderUseCase() {
        return new CreateOrderUseCase(
                repositoryFactory.createMesaRepository(),
                repositoryFactory.createPedidoRepository()
        );
    }

    public UpdateOrderStatusUseCase updateOrderStatusUseCase() {
        return new UpdateOrderStatusUseCase(
                repositoryFactory.createMesaRepository(),
                repositoryFactory.createPedidoRepository()
        );
    }

    public ListTablesUseCase listTablesUseCase() {
        return new ListTablesUseCase(repositoryFactory.createMesaRepository());
    }

    public GenerateReportsUseCase generateReportsUseCase() {
        return new GenerateReportsUseCase(repositoryFactory.createPedidoRepository());
    }
}
