package com.organiza.infrastructure.persistence.sqlite;

import com.organiza.domain.entity.Mesa;
import com.organiza.domain.enums.StatusMesa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de CRUD da entidade Mesa.
 */
public class SQLiteMesaRepository {

    private final SQLiteConnection databaseConnection;

    public SQLiteMesaRepository(SQLiteConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Insere uma nova mesa no banco de dados.
     */
    public Mesa save(Mesa mesa) {
        String sql = "INSERT INTO mesas (numero, status) VALUES (?, ?)";
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, mesa.getNumero());
                pstmt.setString(2, mesa.getStatus().name());
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        mesa.setId(keys.getInt(1));
                    }
                }
                return mesa;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar mesa", e);
        }
    }

    /**
     * Busca uma mesa pelo ID.
     */
    public Optional<Mesa> findById(int id) {
        String sql = "SELECT id, numero, status FROM mesas WHERE id = ?";
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar mesa", e);
        }
    }

    /**
     * Busca uma mesa pelo número.
     */
    public Optional<Mesa> findByNumero(int numero) {
        String sql = "SELECT id, numero, status FROM mesas WHERE numero = ?";
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, numero);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar mesa por número", e);
        }
    }

    /**
     * Lista todas as mesas ordenadas por número.
     */
    public List<Mesa> findAll() {
        String sql = "SELECT id, numero, status FROM mesas ORDER BY numero";
        List<Mesa> mesas = new ArrayList<>();
        try {
            Connection conn = databaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    mesas.add(mapRow(rs));
                }
                return mesas;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mesas", e);
        }
    }

    /**
     * Atualiza o status de uma mesa.
     */
    public void updateStatus(int id, StatusMesa status) {
        String sql = "UPDATE mesas SET status = ? WHERE id = ?";
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status.name());
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status da mesa", e);
        }
    }

    /**
     * Remove uma mesa pelo ID.
     */
    public void deleteById(int id) {
        String sql = "DELETE FROM mesas WHERE id = ?";
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover mesa", e);
        }
    }

    private Mesa mapRow(ResultSet rs) throws SQLException {
        return new Mesa(
                rs.getInt("id"),
                rs.getInt("numero"),
                StatusMesa.valueOf(rs.getString("status"))
        );
    }
}
