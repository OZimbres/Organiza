package com.organiza.repository;

import com.organiza.database.DatabaseConnection;
import com.organiza.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados da tabela {@code clientes}.
 */
public class ClienteRepository {

    private final DatabaseConnection db;

    public ClienteRepository(DatabaseConnection db) {
        this.db = db;
    }

    public Cliente save(Cliente cliente) {
        String sql = "INSERT INTO clientes (nome, telefone) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getTelefone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cliente.setId(keys.getInt(1));
            }
            return cliente;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar cliente", e);
        }
    }

    public Cliente update(Cliente cliente) {
        String sql = "UPDATE clientes SET nome = ?, telefone = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getTelefone());
            ps.setInt(3, cliente.getId());
            ps.executeUpdate();
            return cliente;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar cliente", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = db.getConnection()
                .prepareStatement("DELETE FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar cliente", e);
        }
    }

    public List<Cliente> findAll() {
        List<Cliente> list = new ArrayList<>();
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM clientes ORDER BY nome")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes", e);
        }
        return list;
    }

    public Optional<Cliente> findById(int id) {
        try (PreparedStatement ps = db.getConnection()
                .prepareStatement("SELECT * FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente", e);
        }
        return Optional.empty();
    }

    private Cliente mapRow(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("telefone")
        );
    }
}
