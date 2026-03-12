package com.organiza.service;

import com.organiza.model.*;
import com.organiza.repository.MesaRepository;
import com.organiza.repository.PedidoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Serviço de negócio para gerenciamento de pedidos e mesas.
 */
public class PedidoService {

    private final MesaRepository mesaRepository;
    private final PedidoRepository pedidoRepository;

    public PedidoService(MesaRepository mesaRepository, PedidoRepository pedidoRepository) {
        this.mesaRepository = mesaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    // ---- Mesas ----

    /**
     * Cria mesas numeradas de 1 até o total informado.
     */
    public void criarMesas(int totalMesas) {
        for (int i = 1; i <= totalMesas; i++) {
            if (mesaRepository.findByNumero(i).isEmpty()) {
                mesaRepository.save(new Mesa(i));
            }
        }
    }

    /**
     * Lista todas as mesas.
     */
    public List<Mesa> listarMesas() {
        return mesaRepository.findAll();
    }

    /**
     * Busca uma mesa pelo ID.
     */
    public Optional<Mesa> buscarMesa(int id) {
        return mesaRepository.findById(id);
    }

    // ---- Pedidos ----

    /**
     * Cria um novo pedido para uma mesa, marcando a mesa como ocupada.
     *
     * @param mesaId ID da mesa
     * @param itens  Lista de itens do pedido
     * @return O pedido criado
     * @throws IllegalArgumentException se a mesa não existir
     * @throws IllegalStateException    se a lista de itens estiver vazia
     */
    public Pedido criarPedido(int mesaId, String nomeCliente, List<ItemPedido> itens) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new IllegalArgumentException("Mesa não encontrada: " + mesaId));

        if (itens == null || itens.isEmpty()) {
            throw new IllegalStateException("Pedido deve conter ao menos um item");
        }

        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        }

        Pedido pedido = new Pedido(mesa.getId(), nomeCliente);
        itens.forEach(pedido::addItem);

        Pedido saved = pedidoRepository.save(pedido);

        // Marca a mesa como ocupada
        mesaRepository.updateStatus(mesa.getId(), StatusMesa.OCUPADA);

        return saved;
    }

    /**
     * Avança o status do pedido para o próximo estágio.
     * Fluxo: PENDENTE → EM_PREPARO → PRONTO → ENTREGUE → PAGO
     *
     * @param pedidoId ID do pedido
     * @throws IllegalArgumentException se o pedido não existir
     * @throws IllegalStateException    se o pedido já estiver pago
     */
    public Pedido avancarStatus(int pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + pedidoId));

        StatusPedido novoStatus = switch (pedido.getStatus()) {
            case PENDENTE -> StatusPedido.EM_PREPARO;
            case EM_PREPARO -> StatusPedido.PRONTO;
            case PRONTO -> StatusPedido.ENTREGUE;
            case ENTREGUE -> StatusPedido.PAGO;
            case PAGO -> throw new IllegalStateException("Pedido já está pago");
        };

        pedidoRepository.updateStatus(pedidoId, novoStatus);
        pedido.setStatus(novoStatus);

        // Se pago, verifica se todos os pedidos da mesa estão pagos para liberar
        if (novoStatus == StatusPedido.PAGO) {
            liberarMesaSeCompleta(pedido.getMesaId());
        }

        return pedido;
    }

    /**
     * Busca um pedido pelo ID.
     */
    public Optional<Pedido> buscarPedido(int id) {
        return pedidoRepository.findById(id);
    }

    /**
     * Lista pedidos de uma mesa.
     */
    public List<Pedido> listarPedidosMesa(int mesaId) {
        return pedidoRepository.findByMesaId(mesaId);
    }

    /**
     * Lista pedidos por status.
     */
    public List<Pedido> listarPedidosPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status);
    }

    /**
     * Lista todos os pedidos ativos (não pagos).
     */
    public List<Pedido> listarPedidosAtivos() {
        return pedidoRepository.findAllActive();
    }

    /**
     * Lista pedidos pendentes e em preparo para a tela da cozinha.
     */
    public List<Pedido> listarPedidosCozinha() {
        List<Pedido> pendentes = pedidoRepository.findByStatus(StatusPedido.PENDENTE);
        List<Pedido> emPreparo = pedidoRepository.findByStatus(StatusPedido.EM_PREPARO);
        pendentes.addAll(emPreparo);
        return pendentes;
    }

    /**
     * Libera a mesa se todos os seus pedidos estiverem pagos.
     */
    private void liberarMesaSeCompleta(int mesaId) {
        List<Pedido> pedidosMesa = pedidoRepository.findByMesaId(mesaId);
        boolean todosPagos = pedidosMesa.stream()
                .allMatch(p -> p.getStatus() == StatusPedido.PAGO);
        if (todosPagos) {
            mesaRepository.updateStatus(mesaId, StatusMesa.LIVRE);
        }
    }
}
