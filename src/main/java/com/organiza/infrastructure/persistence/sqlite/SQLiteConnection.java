package com.organiza.infrastructure.persistence.sqlite;

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
public class SQLiteConnection {

    private static final String DEFAULT_URL = "jdbc:sqlite:padaria.db";
    private final String url;
    private Connection connection;

    public SQLiteConnection() {
        this(DEFAULT_URL);
    }

    public SQLiteConnection(String url) {
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
                    nome_cliente TEXT NOT NULL DEFAULT 'Cliente',
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
                    preco REAL NOT NULL DEFAULT 0.0,
                    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
                )
                """;

        String createClientes = """
                CREATE TABLE IF NOT EXISTS clientes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    telefone TEXT
                )
                """;

        String createProdutos = """
                CREATE TABLE IF NOT EXISTS produtos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    preco REAL NOT NULL DEFAULT 0.0,
                    categoria TEXT
                )
                """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createMesas);
            stmt.execute(createPedidos);
            stmt.execute(createItensPedido);
            stmt.execute(createClientes);
            stmt.execute(createProdutos);
            runMigrations(stmt);
        } catch (SQLException e) {
            // Log a clear error for users and rethrow with actionable guidance
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
            throw new RuntimeException("Erro ao inicializar banco de dados. Verifique permissões, caminho do arquivo padaria.db e se outro processo não o está bloqueando.", e);
        }
    }

    private void runMigrations(Statement stmt) {
        tryAlter(stmt, "ALTER TABLE pedidos ADD COLUMN nome_cliente TEXT NOT NULL DEFAULT 'Cliente'");
        tryAlter(stmt, "ALTER TABLE itens_pedido ADD COLUMN preco REAL NOT NULL DEFAULT 0.0");
    }

    private void tryAlter(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException ignored) {
            // Column already exists — safe to ignore
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
