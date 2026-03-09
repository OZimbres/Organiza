package com.organiza.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerencia a conexão com o banco de dados SQLite e inicializa o schema.
 * <p>
 * Mantém uma única conexão reutilizável, adequado para aplicações desktop
 * com acesso local ao SQLite.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:sqlite:padaria.db";
    private final String url;
    private Connection connection;

    public DatabaseConnection() {
        this(DEFAULT_URL);
    }

    public DatabaseConnection(String url) {
        this.url = url;
    }

    /**
     * Obtém a conexão com o banco de dados (reutiliza a mesma conexão).
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url);
        }
        return connection;
    }

    /**
     * Inicializa as tabelas do banco de dados caso não existam.
     */
    public void initializeDatabase() {
        String createMesas = """
                CREATE TABLE IF NOT EXISTS mesas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero INTEGER NOT NULL UNIQUE,
                    status TEXT NOT NULL DEFAULT 'LIVRE'
                )
                """;

        String createPedidos = """
                CREATE TABLE IF NOT EXISTS pedidos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    mesa_id INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDENTE',
                    data_hora TEXT NOT NULL,
                    FOREIGN KEY (mesa_id) REFERENCES mesas(id)
                )
                """;

        String createItensPedido = """
                CREATE TABLE IF NOT EXISTS itens_pedido (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pedido_id INTEGER NOT NULL,
                    produto TEXT NOT NULL,
                    quantidade INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
                )
                """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createMesas);
            stmt.execute(createPedidos);
            stmt.execute(createItensPedido);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar banco de dados", e);
        }
    }

    /**
     * Fecha a conexão com o banco de dados.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignora erro ao fechar
            }
            connection = null;
        }
    }
}
