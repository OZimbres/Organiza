package com.organiza.infrastructure.config;

import com.organiza.domain.repository.ClienteRepositoryPort;
import com.organiza.domain.repository.MesaRepositoryPort;
import com.organiza.domain.repository.PedidoRepositoryPort;
import com.organiza.domain.repository.ProdutoRepositoryPort;
import com.organiza.infrastructure.persistence.sqlite.SQLiteClienteRepository;
import com.organiza.infrastructure.persistence.sqlite.SQLiteConnection;
import com.organiza.infrastructure.persistence.sqlite.SQLiteMesaRepository;
import com.organiza.infrastructure.persistence.sqlite.SQLitePedidoRepository;
import com.organiza.infrastructure.persistence.sqlite.SQLiteProdutoRepository;

/**
 * Factory para criação de repositórios.
 * Centraliza a instanciação e permite trocar a implementação (ex.: SQLite → PostgreSQL)
 * sem alterar o restante do sistema.
 */
public class RepositoryFactory {

    private final SQLiteConnection connection;

    public RepositoryFactory(SQLiteConnection connection) {
        this.connection = connection;
    }

    public MesaRepositoryPort createMesaRepository() {
        return new SQLiteMesaRepository(connection);
    }

    public PedidoRepositoryPort createPedidoRepository() {
        return new SQLitePedidoRepository(connection);
    }

    public ClienteRepositoryPort createClienteRepository() {
        return new SQLiteClienteRepository(connection);
    }

    public ProdutoRepositoryPort createProdutoRepository() {
        return new SQLiteProdutoRepository(connection);
    }
}
