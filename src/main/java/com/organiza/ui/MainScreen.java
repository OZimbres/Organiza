package com.organiza.ui;

import com.organiza.model.*;
import com.organiza.service.PedidoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Dashboard de mesas — exibido como aba na janela principal.
 */
public class MainScreen {

    private static final String BG_APP     = "#1C1C2E";
    private static final String BG_HEADER  = "#16213E";
    private static final String ACCENT     = "#E8A838";
    private static final String TEXT_MUTED = "#8888AA";

    private final PedidoService pedidoService;
    private final Runnable onRefreshAll;
    private FlowPane mesasContainer;

    public MainScreen(PedidoService pedidoService, Runnable onRefreshAll) {
        this.pedidoService = pedidoService;
        this.onRefreshAll = onRefreshAll;
    }

    public Node createPane() {
        Label lblTitle = new Label("🍽  Organiza");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web(ACCENT));

        Label lblSub = new Label("Gestão de Mesas");
        lblSub.setFont(Font.font("Arial", 13));
        lblSub.setTextFill(Color.web(TEXT_MUTED));

        VBox titleBox = new VBox(2, lblTitle, lblSub);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Button btnAtualizar = styledButton("↺  Atualizar", "#3498DB", "#2271A8");
        btnAtualizar.setOnAction(e -> onRefreshAll.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(titleBox, spacer, btnAtualizar);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: " + BG_HEADER + ";");

        mesasContainer = new FlowPane();
        mesasContainer.setHgap(18);
        mesasContainer.setVgap(18);
        mesasContainer.setPadding(new Insets(24));
        mesasContainer.setPrefWrapLength(800);

        ScrollPane scroll = new ScrollPane(mesasContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + BG_APP + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(header, scroll);
        root.setStyle("-fx-background-color: " + BG_APP + ";");

        atualizarMesas();
        return root;
    }

    public void refresh() {
        atualizarMesas();
    }

    private void atualizarMesas() {
        if (mesasContainer == null) return;
        mesasContainer.getChildren().clear();
        for (Mesa mesa : pedidoService.listarMesas()) {
            mesasContainer.getChildren().add(criarCardMesa(mesa));
        }
    }

    private VBox criarCardMesa(Mesa mesa) {
        List<Pedido> pedidos = pedidoService.listarPedidosMesa(mesa.getId());
        Pedido pedidoAtual = pedidos.isEmpty() ? null : pedidos.getFirst();

        String[] colors = statusColors(mesa, pedidoAtual);
        String cardBg     = colors[0];
        String stripColor = colors[1];
        String statusText = colors[2];

        Pane strip = new Pane();
        strip.setPrefHeight(6);
        strip.setStyle("-fx-background-color: " + stripColor + "; -fx-background-radius: 12 12 0 0;");

        Label lblNum = new Label("Mesa " + mesa.getNumero());
        lblNum.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblNum.setTextFill(Color.WHITE);

        Label lblStatus = new Label(statusText);
        lblStatus.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblStatus.setTextFill(Color.web(stripColor));
        lblStatus.setPadding(new Insets(3, 10, 3, 10));
        lblStatus.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 20;");

        VBox body = new VBox(8, lblNum, lblStatus);
        body.setAlignment(Pos.CENTER);

        if (pedidoAtual != null && pedidoAtual.getStatus() != StatusPedido.PAGO) {
            String nome = pedidoAtual.getNomeCliente();
            if (nome != null && !nome.isBlank()) {
                Label lblNome = new Label("👤 " + nome);
                lblNome.setFont(Font.font("Arial", 13));
                lblNome.setTextFill(Color.web("#C8C8E0"));
                body.getChildren().add(lblNome);
            }
            double total = pedidos.stream().mapToDouble(Pedido::getTotal).sum();
            if (total > 0) {
                Label lblTotal = new Label(String.format("R$ %.2f", total));
                lblTotal.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                lblTotal.setTextFill(Color.web(ACCENT));
                body.getChildren().add(lblTotal);
            }
        }

        body.setPadding(new Insets(14, 12, 12, 12));

        VBox card = new VBox(strip, body);
        card.setPrefSize(170, 190);
        card.setMaxSize(170, 190);
        card.setStyle(
                "-fx-background-color: " + cardBg + ";"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: " + stripColor + "44;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 1.5;"
                + "-fx-cursor: hand;"
        );

        DropShadow shadow = new DropShadow(12, Color.BLACK);
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        card.setOnMouseClicked(e -> verPedidosMesa(mesa));
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("-fx-cursor: hand;",
                "-fx-cursor: hand; -fx-opacity: 0.88;")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-opacity: 0.88;", "")));

        return card;
    }

    private String[] statusColors(Mesa mesa, Pedido pedido) {
        if (pedido == null || mesa.getStatus() == StatusMesa.LIVRE) {
            return new String[]{"#1A2E22", "#27AE60", "✓ Livre"};
        }
        return switch (pedido.getStatus()) {
            case PENDENTE   -> new String[]{"#2E2510", "#F39C12", "● Pendente"};
            case EM_PREPARO -> new String[]{"#2E1C08", "#E67E22", "◆ Em preparo"};
            case PRONTO     -> new String[]{"#0D1E2E", "#3498DB", "▲ Pronto"};
            case ENTREGUE   -> new String[]{"#1E0D2E", "#9B59B6", "★ Entregue"};
            case PAGO       -> new String[]{"#1A2E22", "#2ECC71", "✓ Pago"};
        };
    }

    private void verPedidosMesa(Mesa mesa) {
        List<Pedido> pedidos = pedidoService.listarPedidosMesa(mesa.getId());

        if (pedidos.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Mesa " + mesa.getNumero() + " está livre.");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        Stage dialog = new Stage();
        dialog.setTitle("Mesa " + mesa.getNumero());

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + BG_APP + ";");

        Label titulo = new Label("Mesa " + mesa.getNumero());
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web(ACCENT));
        content.getChildren().add(titulo);

        double grandTotal = 0;

        for (Pedido pedido : pedidos) {
            VBox pedidoBox = new VBox(8);
            pedidoBox.setPadding(new Insets(14));

            String[] colors = statusColors(mesa, pedido);
            pedidoBox.setStyle(
                    "-fx-background-color: " + colors[0] + ";"
                    + "-fx-background-radius: 10;"
                    + "-fx-border-color: " + colors[1] + "66;"
                    + "-fx-border-radius: 10;"
                    + "-fx-border-width: 1;"
            );

            String nome = pedido.getNomeCliente() != null ? pedido.getNomeCliente() : "—";
            Label lblHeader = new Label("👤 " + nome + "  ·  " + colors[2]
                    + "  ·  " + pedido.getDataHora().toLocalTime().withNano(0));
            lblHeader.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            lblHeader.setTextFill(Color.web(colors[1]));
            pedidoBox.getChildren().add(lblHeader);

            for (ItemPedido item : pedido.getItens()) {
                String sub = item.getPreco() > 0
                        ? String.format("  • %dx %s  —  R$ %.2f", item.getQuantidade(), item.getProduto(), item.getSubtotal())
                        : String.format("  • %dx %s", item.getQuantidade(), item.getProduto());
                Label lblItem = new Label(sub);
                lblItem.setFont(Font.font("Arial", 14));
                lblItem.setTextFill(Color.web("#C8C8E0"));
                pedidoBox.getChildren().add(lblItem);
            }

            if (pedido.getTotal() > 0) {
                Label lblTotal = new Label(String.format("Total: R$ %.2f", pedido.getTotal()));
                lblTotal.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                lblTotal.setTextFill(Color.web(ACCENT));
                pedidoBox.getChildren().add(lblTotal);
                grandTotal += pedido.getTotal();
            }

            if (pedido.getStatus() != StatusPedido.PAGO) {
                Button btnAvancar = new Button("Avançar → " + proximoStatus(pedido.getStatus()));
                btnAvancar.setFont(Font.font("Arial", 13));
                btnAvancar.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;"
                        + "-fx-background-radius: 6; -fx-padding: 7 16;");
                btnAvancar.setOnAction(e -> {
                    pedidoService.avancarStatus(pedido.getId());
                    dialog.close();
                    onRefreshAll.run();
                    verPedidosMesa(mesa);
                });
                pedidoBox.getChildren().add(btnAvancar);
            }

            content.getChildren().add(pedidoBox);
        }

        if (grandTotal > 0) {
            Label lblGrand = new Label(String.format("💳  Total da Mesa: R$ %.2f", grandTotal));
            lblGrand.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            lblGrand.setTextFill(Color.web(ACCENT));
            lblGrand.setPadding(new Insets(8, 0, 0, 0));
            content.getChildren().add(lblGrand);
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: " + BG_APP + "; -fx-background: " + BG_APP + ";");

        Scene scene = new Scene(sp, 520, 480);
        scene.setFill(Color.web(BG_APP));
        dialog.setScene(scene);
        dialog.show();
    }

    private String proximoStatus(StatusPedido status) {
        return switch (status) {
            case PENDENTE   -> "Em preparo";
            case EM_PREPARO -> "Pronto";
            case PRONTO     -> "Entregue";
            case ENTREGUE   -> "Pago";
            case PAGO       -> "";
        };
    }

    private Button styledButton(String text, String bg, String hover) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-padding: 10 20;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hover + "; -fx-background-radius: 8; -fx-padding: 10 20;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-padding: 10 20;"));
        return btn;
    }
}
