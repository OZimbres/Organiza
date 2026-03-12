package com.organiza.ui;

import com.organiza.domain.entity.ItemPedido;
import com.organiza.domain.entity.Pedido;
import com.organiza.domain.enums.StatusPedido;
import com.organiza.application.usecase.PedidoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Visão da cozinha — exibida como aba na janela principal.
 * Mostra pedidos pendentes e em preparo com indicadores de prioridade por tempo de espera.
 */
public class CozinhaScreen {

    private static final String BG_APP             = "#1C1C2E";
    private static final String BG_CARD_PENDENTE   = "#2E2510";
    private static final String BG_CARD_EM_PREPARO = "#2E1C08";
    private static final String ACCENT_PENDENTE    = "#F39C12";
    private static final String ACCENT_EM_PREPARO  = "#E67E22";
    private static final String ACCENT_TEXT        = "#E8A838";

    private final PedidoService pedidoService;
    private final Runnable onStatusChanged;
    private FlowPane pedidosContainer;

    public CozinhaScreen(PedidoService pedidoService, Runnable onStatusChanged) {
        this.pedidoService = pedidoService;
        this.onStatusChanged = onStatusChanged;
    }

    public Node createPane() {
        pedidosContainer = new FlowPane();
        pedidosContainer.setHgap(18);
        pedidosContainer.setVgap(18);
        pedidosContainer.setPadding(new Insets(24));
        pedidosContainer.setPrefWrapLength(800);

        ScrollPane scroll = new ScrollPane(pedidosContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + BG_APP + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(scroll);
        root.setStyle("-fx-background-color: " + BG_APP + ";");

        atualizarPedidos();
        return root;
    }

    public void refresh() {
        atualizarPedidos();
    }

    public int getPendingCount() {
        return pedidoService.listarPedidosCozinha().size();
    }

    private void atualizarPedidos() {
        if (pedidosContainer == null) return;
        pedidosContainer.getChildren().clear();
        List<Pedido> pedidos = pedidoService.listarPedidosCozinha();

        if (pedidos.isEmpty()) {
            Label lblVazio = new Label("✓  Nenhum pedido na fila");
            lblVazio.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            lblVazio.setTextFill(Color.web("#44AA66"));
            lblVazio.setPadding(new Insets(40));
            pedidosContainer.getChildren().add(lblVazio);
            return;
        }

        for (Pedido pedido : pedidos) {
            pedidosContainer.getChildren().add(criarCardPedido(pedido));
        }
    }

    private VBox criarCardPedido(Pedido pedido) {
        boolean isPendente = pedido.getStatus() == StatusPedido.PENDENTE;
        String cardBg = isPendente ? BG_CARD_PENDENTE : BG_CARD_EM_PREPARO;
        String accent  = isPendente ? ACCENT_PENDENTE  : ACCENT_EM_PREPARO;

        String mesaLabel = pedidoService.buscarMesa(pedido.getMesaId())
                .map(m -> "Mesa " + m.getNumero())
                .orElse("Mesa ?");

        String nome = pedido.getNomeCliente() != null && !pedido.getNomeCliente().isBlank()
                ? pedido.getNomeCliente() : "—";

        long minutos = Duration.between(pedido.getDataHora(), LocalDateTime.now()).toMinutes();

        // Priority tier
        String priorityBadge;
        String priorityColor;
        String borderColor;
        if (minutos >= 15) {
            priorityBadge = "🚨  " + minutos + " min";
            priorityColor = "#E74C3C";
            borderColor   = "#E74C3C88";
            cardBg        = isPendente ? "#3E1008" : "#3E1008";
        } else if (minutos >= 10) {
            priorityBadge = "🔥  " + minutos + " min";
            priorityColor = "#E67E22";
            borderColor   = accent + "88";
        } else if (minutos >= 5) {
            priorityBadge = "⚡  " + minutos + " min";
            priorityColor = "#F39C12";
            borderColor   = accent + "66";
        } else {
            String tempo = minutos < 1 ? "agora" : minutos + " min";
            priorityBadge = "⏱  " + tempo;
            priorityColor = "#8888AA";
            borderColor   = accent + "44";
        }

        // Colour strip
        Pane strip = new Pane();
        strip.setPrefHeight(6);
        strip.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 12 12 0 0;");

        // Header: mesa name
        Label lblMesa = new Label(mesaLabel);
        lblMesa.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblMesa.setTextFill(Color.web(accent));

        // Priority badge
        Label lblPriority = new Label(priorityBadge);
        lblPriority.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblPriority.setTextFill(Color.web(priorityColor));
        lblPriority.setPadding(new Insets(2, 8, 2, 8));
        lblPriority.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 12;");

        HBox headerRow = new HBox(8, lblMesa, new Region(), lblPriority);
        HBox.setHgrow(headerRow.getChildren().get(1), Priority.ALWAYS);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblNome = new Label("👤 " + nome);
        lblNome.setFont(Font.font("Arial", 13));
        lblNome.setTextFill(Color.web("#C8C8E0"));

        VBox body = new VBox(6, headerRow, lblNome);
        body.setPadding(new Insets(12, 14, 4, 14));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + accent + "44;");
        sep.setPadding(new Insets(2, 14, 2, 14));

        // Items
        VBox itemsBox = new VBox(4);
        itemsBox.setPadding(new Insets(4, 14, 4, 14));
        for (ItemPedido item : pedido.getItens()) {
            String priceStr = item.getPreco() > 0
                    ? String.format("  R$ %.2f", item.getSubtotal()) : "";
            Label lblItem = new Label(item.getQuantidade() + "×  " + item.getProduto() + priceStr);
            lblItem.setFont(Font.font("Arial", 14));
            lblItem.setTextFill(Color.web("#D0D0E8"));
            itemsBox.getChildren().add(lblItem);
        }

        // Total
        VBox totalBox = new VBox();
        totalBox.setPadding(new Insets(4, 14, 8, 14));
        if (pedido.getTotal() > 0) {
            Label lblTotal = new Label(String.format("Total: R$ %.2f", pedido.getTotal()));
            lblTotal.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            lblTotal.setTextFill(Color.web(ACCENT_TEXT));
            totalBox.getChildren().add(lblTotal);
        }

        // Action button
        String btnText = isPendente ? "▶  Iniciar Preparo" : "✓  Marcar como Pronto";
        Button btnAvancar = new Button(btnText);
        btnAvancar.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnAvancar.setTextFill(Color.WHITE);
        btnAvancar.setMaxWidth(Double.MAX_VALUE);
        btnAvancar.setStyle(
                "-fx-background-color: " + accent + "; -fx-background-radius: 0 0 10 10; -fx-padding: 10 0;");
        btnAvancar.setOnAction(e -> {
            pedidoService.avancarStatus(pedido.getId());
            onStatusChanged.run();
        });

        VBox card = new VBox(strip, body, sep, itemsBox, totalBox, btnAvancar);
        card.setPrefWidth(240);
        card.setStyle(
                "-fx-background-color: " + cardBg + ";"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: " + (minutos >= 15 ? "2" : "1.5") + ";"
        );

        DropShadow shadow = new DropShadow(minutos >= 15 ? 20 : 14,
                minutos >= 15 ? Color.web("#E74C3C44") : Color.BLACK);
        shadow.setOffsetY(4);
        card.setEffect(shadow);

        return card;
    }
}
