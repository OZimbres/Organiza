package com.organiza.ui;

import com.organiza.model.ItemPedido;
import com.organiza.model.Mesa;
import com.organiza.model.Pedido;
import com.organiza.model.StatusPedido;
import com.organiza.service.PedidoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;

/**
 * Tela da cozinha — mostra pedidos pendentes e em preparo.
 */
public class CozinhaScreen {

    private final PedidoService pedidoService;
    private final Runnable onStatusChanged;
    private VBox pedidosContainer;

    public CozinhaScreen(PedidoService pedidoService, Runnable onStatusChanged) {
        this.pedidoService = pedidoService;
        this.onStatusChanged = onStatusChanged;
    }

    public Scene createScene() {
        Label titulo = new Label("🍳 Pedidos da Cozinha");
        titulo.setFont(Font.font("Arial", 26));
        titulo.setStyle("-fx-font-weight: bold;");

        pedidosContainer = new VBox(15);
        pedidosContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(pedidosContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        Button btnAtualizar = new Button("ATUALIZAR");
        btnAtualizar.setFont(Font.font("Arial", 16));
        btnAtualizar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 12 30;");
        btnAtualizar.setOnAction(e -> atualizarPedidos());

        VBox root = new VBox(15, titulo, scrollPane, btnAtualizar);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        atualizarPedidos();

        return new Scene(root, 600, 500);
    }

    private void atualizarPedidos() {
        pedidosContainer.getChildren().clear();
        List<Pedido> pedidos = pedidoService.listarPedidosCozinha();

        if (pedidos.isEmpty()) {
            Label lblVazio = new Label("Nenhum pedido na fila!");
            lblVazio.setFont(Font.font("Arial", 20));
            lblVazio.setStyle("-fx-text-fill: #666;");
            pedidosContainer.getChildren().add(lblVazio);
            return;
        }

        for (Pedido pedido : pedidos) {
            VBox card = criarCardPedido(pedido);
            pedidosContainer.getChildren().add(card);
        }
    }

    private VBox criarCardPedido(Pedido pedido) {
        // Buscar número da mesa
        String mesaLabel = pedidoService.buscarMesa(pedido.getMesaId())
                .map(m -> "Mesa " + m.getNumero())
                .orElse("Mesa ?");

        String corFundo = pedido.getStatus() == StatusPedido.PENDENTE ? "#FFF9C4" : "#FFE0B2";

        Label lblHeader = new Label(mesaLabel + " — " + pedido.getStatus().getLabel()
                + " (" + pedido.getDataHora().toLocalTime().withNano(0) + ")");
        lblHeader.setFont(Font.font("Arial", 18));
        lblHeader.setStyle("-fx-font-weight: bold;");

        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: " + corFundo + "; "
                + "-fx-border-color: #BDBDBD; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.getChildren().add(lblHeader);

        for (ItemPedido item : pedido.getItens()) {
            Label lblItem = new Label("  " + item.getQuantidade() + "x " + item.getProduto());
            lblItem.setFont(Font.font("Arial", 16));
            card.getChildren().add(lblItem);
        }

        String btnText = pedido.getStatus() == StatusPedido.PENDENTE
                ? "INICIAR PREPARO"
                : "MARCAR COMO PRONTO";

        Button btnAvancar = new Button(btnText);
        btnAvancar.setFont(Font.font("Arial", 16));
        btnAvancar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 24;");
        btnAvancar.setOnAction(e -> {
            pedidoService.avancarStatus(pedido.getId());
            atualizarPedidos();
            onStatusChanged.run();
        });

        card.getChildren().add(btnAvancar);
        return card;
    }
}
