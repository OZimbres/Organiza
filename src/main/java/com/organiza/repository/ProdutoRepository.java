package com.organiza.repository;

import com.organiza.database.DatabaseConnection;
import com.organiza.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados da tabela {@code produtos}.
 */
public class ProdutoRepository {

    private final DatabaseConnection db;

    public ProdutoRepository(DatabaseConnection db) {
        this.db = db;
    }

    public Produto save(Produto produto) {
        String sql = "INSERT INTO produtos (nome, preco, categoria) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produto.getNome());
            ps.setDouble(2, produto.getPreco());
            ps.setString(3, produto.getCategoria());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) produto.setId(keys.getInt(1));
            }
            return produto;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar produto", e);
        }
    }

    public Produto update(Produto produto) {
        String sql = "UPDATE produtos SET nome = ?, preco = ?, categoria = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, produto.getNome());
            ps.setDouble(2, produto.getPreco());
            ps.setString(3, produto.getCategoria());
            ps.setInt(4, produto.getId());
            ps.executeUpdate();
            return produto;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = db.getConnection()
                .prepareStatement("DELETE FROM produtos WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar produto", e);
        }
    }

    public List<Produto> findAll() {
        List<Produto> list = new ArrayList<>();
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM produtos ORDER BY categoria, nome")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos", e);
        }
        return list;
    }

    public Optional<Produto> findById(int id) {
        try (PreparedStatement ps = db.getConnection()
                .prepareStatement("SELECT * FROM produtos WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto", e);
        }
        return Optional.empty();
    }

    private Produto mapRow(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getDouble("preco"),
                rs.getString("categoria")
        );
    }
}
