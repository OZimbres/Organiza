package com.organiza.ui;

import com.organiza.model.*;
import com.organiza.service.PedidoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

/**
 * Tela principal do sistema — dashboard de mesas.
 */
public class MainScreen {

    private final PedidoService pedidoService;
    private final Stage stage;
    private VBox mesasContainer;

    public MainScreen(PedidoService pedidoService, Stage stage) {
        this.pedidoService = pedidoService;
        this.stage = stage;
    }

    public Scene createScene() {
        // Título
        Label titulo = new Label("Organiza - Gestão de Pedidos");
        titulo.setFont(Font.font("Arial", 28));
        titulo.setStyle("-fx-font-weight: bold;");

        // Container de mesas
        mesasContainer = new VBox(10);
        mesasContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(mesasContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Botões de ação
        Button btnNovoPedido = new Button("NOVO PEDIDO");
        btnNovoPedido.setFont(Font.font("Arial", 18));
        btnNovoPedido.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 30;");
        btnNovoPedido.setOnAction(e -> abrirTelaPedido());

        Button btnCozinha = new Button("VER COZINHA");
        btnCozinha.setFont(Font.font("Arial", 18));
        btnCozinha.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 15 30;");
        btnCozinha.setOnAction(e -> abrirTelaCozinha());

        Button btnAtualizar = new Button("ATUALIZAR");
        btnAtualizar.setFont(Font.font("Arial", 16));
        btnAtualizar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 30;");
        btnAtualizar.setOnAction(e -> atualizarMesas());

        HBox botoes = new HBox(20, btnNovoPedido, btnCozinha, btnAtualizar);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(15));

        VBox root = new VBox(15, titulo, scrollPane, botoes);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        atualizarMesas();

        return new Scene(root, 800, 600);
    }

    private void atualizarMesas() {
        mesasContainer.getChildren().clear();
        List<Mesa> mesas = pedidoService.listarMesas();

        for (Mesa mesa : mesas) {
            HBox mesaBox = criarCardMesa(mesa);
            mesasContainer.getChildren().add(mesaBox);
        }
    }

    private HBox criarCardMesa(Mesa mesa) {
        Label lblMesa = new Label("Mesa " + mesa.getNumero());
        lblMesa.setFont(Font.font("Arial", 20));
        lblMesa.setStyle("-fx-font-weight: bold;");

        // Buscar status do pedido mais recente da mesa
        List<Pedido> pedidosMesa = pedidoService.listarPedidosMesa(mesa.getId());
        String statusTexto;
        String corFundo;

        if (pedidosMesa.isEmpty() || mesa.getStatus() == StatusMesa.LIVRE) {
            statusTexto = "🟢 Livre";
            corFundo = "#E8F5E9";
        } else {
            Pedido pedidoAtual = pedidosMesa.getFirst();
            statusTexto = switch (pedidoAtual.getStatus()) {
                case PENDENTE -> "🟡 Pendente";
                case EM_PREPARO -> "🟡 Em preparo";
                case PRONTO -> "🔵 Pronto";
                case ENTREGUE -> "✅ Entregue";
                case PAGO -> "🟢 Pago";
            };
            corFundo = switch (pedidoAtual.getStatus()) {
                case PENDENTE -> "#FFF9C4";
                case EM_PREPARO -> "#FFE0B2";
                case PRONTO -> "#BBDEFB";
                case ENTREGUE -> "#C8E6C9";
                case PAGO -> "#E8F5E9";
            };
        }

        Label lblStatus = new Label(statusTexto);
        lblStatus.setFont(Font.font("Arial", 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botão para ver pedidos da mesa
        Button btnVer = new Button("Ver Pedidos");
        btnVer.setFont(Font.font("Arial", 14));
        btnVer.setOnAction(e -> verPedidosMesa(mesa));

        HBox card = new HBox(15, lblMesa, spacer, lblStatus, btnVer);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + corFundo + "; "
                + "-fx-border-color: #BDBDBD; -fx-border-radius: 8; -fx-background-radius: 8;");

        return card;
    }

    private void verPedidosMesa(Mesa mesa) {
        List<Pedido> pedidos = pedidoService.listarPedidosMesa(mesa.getId());

        if (pedidos.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Nenhum pedido para Mesa " + mesa.getNumero());
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        Stage dialog = new Stage();
        dialog.setTitle("Pedidos - Mesa " + mesa.getNumero());

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        for (Pedido pedido : pedidos) {
            VBox pedidoBox = new VBox(5);
            pedidoBox.setPadding(new Insets(10));
            pedidoBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #FAFAFA;");

            Label lblPedido = new Label("Pedido #" + pedido.getId() + " - " + pedido.getStatus().getLabel()
                    + " (" + pedido.getDataHora().toLocalTime().withNano(0) + ")");
            lblPedido.setFont(Font.font("Arial", 16));
            lblPedido.setStyle("-fx-font-weight: bold;");
            pedidoBox.getChildren().add(lblPedido);

            for (ItemPedido item : pedido.getItens()) {
                Label lblItem = new Label("  • " + item.getQuantidade() + "x " + item.getProduto());
                lblItem.setFont(Font.font("Arial", 14));
                pedidoBox.getChildren().add(lblItem);
            }

            if (pedido.getStatus() != StatusPedido.PAGO) {
                Button btnAvancar = new Button("Avançar → " + proximoStatus(pedido.getStatus()));
                btnAvancar.setFont(Font.font("Arial", 14));
                btnAvancar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 16;");
                btnAvancar.setOnAction(e -> {
                    pedidoService.avancarStatus(pedido.getId());
                    dialog.close();
                    atualizarMesas();
                    verPedidosMesa(mesa);
                });
                pedidoBox.getChildren().add(btnAvancar);
            }

            content.getChildren().add(pedidoBox);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        dialog.setScene(new Scene(scrollPane, 500, 400));
        dialog.show();
    }

    private String proximoStatus(StatusPedido status) {
        return switch (status) {
            case PENDENTE -> "Em preparo";
            case EM_PREPARO -> "Pronto";
            case PRONTO -> "Entregue";
            case ENTREGUE -> "Pago";
            case PAGO -> "";
        };
    }

    private void abrirTelaPedido() {
        PedidoScreen pedidoScreen = new PedidoScreen(pedidoService, this::atualizarMesas);
        Stage pedidoStage = new Stage();
        pedidoStage.setTitle("Novo Pedido");
        pedidoStage.setScene(pedidoScreen.createScene());
        pedidoStage.show();
    }

    private void abrirTelaCozinha() {
        CozinhaScreen cozinhaScreen = new CozinhaScreen(pedidoService, this::atualizarMesas);
        Stage cozinhaStage = new Stage();
        cozinhaStage.setTitle("Cozinha - Fila de Pedidos");
        cozinhaStage.setScene(cozinhaScreen.createScene());
        cozinhaStage.show();
    }
}
