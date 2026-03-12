package com.organiza.repository;

import com.organiza.database.DatabaseConnection;
import com.organiza.model.ItemPedido;
import com.organiza.model.Pedido;
import com.organiza.model.StatusPedido;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de CRUD da entidade Pedido e seus itens.
 */
public class PedidoRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DatabaseConnection databaseConnection;

    public PedidoRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Salva um pedido e seus itens no banco de dados.
     */
    public Pedido save(Pedido pedido) {
        String sqlPedido = "INSERT INTO pedidos (mesa_id, nome_cliente, status, data_hora) VALUES (?, ?, ?, ?)";
        String sqlItem = "INSERT INTO itens_pedido (pedido_id, produto, quantidade, preco) VALUES (?, ?, ?, ?)";

        try {
            Connection conn = databaseConnection.getConnection();
            conn.setAutoCommit(false);
            try {
                // Insere o pedido
                try (PreparedStatement pstmt = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, pedido.getMesaId());
                    pstmt.setString(2, pedido.getNomeCliente() != null ? pedido.getNomeCliente() : "Cliente");
                    pstmt.setString(3, pedido.getStatus().name());
                    pstmt.setString(4, pedido.getDataHora().format(FORMATTER));
                    pstmt.executeUpdate();

                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            pedido.setId(keys.getInt(1));
                        }
                    }
                }

                // Insere os itens do pedido
                try (PreparedStatement pstmt = conn.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {
                    for (ItemPedido item : pedido.getItens()) {
                        item.setPedidoId(pedido.getId());
                        pstmt.setInt(1, pedido.getId());
                        pstmt.setString(2, item.getProduto());
                        pstmt.setInt(3, item.getQuantidade());
                        pstmt.setDouble(4, item.getPreco());
                        pstmt.executeUpdate();

                        try (ResultSet keys = pstmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                item.setId(keys.getInt(1));
                            }
                        }
                    }
                }

                conn.commit();
                return pedido;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar pedido", e);
        }
    }

    /**
     * Busca um pedido pelo ID, incluindo seus itens.
     */
    public Optional<Pedido> findById(int id) {
        String sql = "SELECT id, mesa_id, nome_cliente, status, data_hora FROM pedidos WHERE id = ?";
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Pedido pedido = mapRow(rs);
                        pedido.setItens(findItensByPedidoId(pedido.getId()));
                        return Optional.of(pedido);
                    }
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedido", e);
        }
    }

    /**
     * Lista todos os pedidos de uma mesa, incluindo itens.
     */
    public List<Pedido> findByMesaId(int mesaId) {
        String sql = "SELECT id, mesa_id, nome_cliente, status, data_hora FROM pedidos WHERE mesa_id = ? ORDER BY data_hora DESC";
        return findPedidos(sql, mesaId);
    }

    /**
     * Lista pedidos por status, incluindo itens.
     */
    public List<Pedido> findByStatus(StatusPedido status) {
        String sql = "SELECT id, mesa_id, nome_cliente, status, data_hora FROM pedidos WHERE status = ? ORDER BY data_hora";
        return findPedidos(sql, status.name());
    }

    /**
     * Lista todos os pedidos ativos (não pagos), incluindo itens.
     */
    public List<Pedido> findAllActive() {
        String sql = "SELECT id, mesa_id, nome_cliente, status, data_hora FROM pedidos WHERE status != 'PAGO' ORDER BY data_hora";
        List<Pedido> pedidos = new ArrayList<>();
        try {
            Connection conn = databaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Pedido pedido = mapRow(rs);
                    pedido.setItens(findItensByPedidoId(pedido.getId()));
                    pedidos.add(pedido);
                }
                return pedidos;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos ativos", e);
        }
    }

    /**
     * Atualiza o status de um pedido.
     */
    public void updateStatus(int id, StatusPedido status) {
        String sql = "UPDATE pedidos SET status = ? WHERE id = ?";
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status.name());
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do pedido", e);
        }
    }

    /**
     * Remove um pedido e seus itens.
     */
    public void deleteById(int id) {
        String sqlItens = "DELETE FROM itens_pedido WHERE pedido_id = ?";
        String sqlPedido = "DELETE FROM pedidos WHERE id = ?";

        try {
            Connection conn = databaseConnection.getConnection();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlItens)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
                try (PreparedStatement pstmt = conn.prepareStatement(sqlPedido)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover pedido", e);
        }
    }

    /**
     * Busca os itens de um pedido pelo ID do pedido.
     */
    public List<ItemPedido> findItensByPedidoId(int pedidoId) {
        String sql = "SELECT id, pedido_id, produto, quantidade, preco FROM itens_pedido WHERE pedido_id = ?";
        List<ItemPedido> itens = new ArrayList<>();
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, pedidoId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        itens.add(new ItemPedido(
                                rs.getInt("id"),
                                rs.getInt("pedido_id"),
                                rs.getString("produto"),
                                rs.getInt("quantidade"),
                                rs.getDouble("preco")
                        ));
                    }
                }
                return itens;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar itens do pedido", e);
        }
    }

    private List<Pedido> findPedidos(String sql, Object param) {
        List<Pedido> pedidos = new ArrayList<>();
        try {
            Connection conn = databaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (param instanceof Integer intVal) {
                    pstmt.setInt(1, intVal);
                } else {
                    pstmt.setString(1, param.toString());
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Pedido pedido = mapRow(rs);
                        pedido.setItens(findItensByPedidoId(pedido.getId()));
                        pedidos.add(pedido);
                    }
                }
                return pedidos;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos", e);
        }
    }

    private Pedido mapRow(ResultSet rs) throws SQLException {
        return new Pedido(
                rs.getInt("id"),
                rs.getInt("mesa_id"),
                rs.getString("nome_cliente"),
                StatusPedido.valueOf(rs.getString("status")),
                LocalDateTime.parse(rs.getString("data_hora"), FORMATTER)
        );
    }
}
