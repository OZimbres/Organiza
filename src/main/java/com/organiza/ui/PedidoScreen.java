package com.organiza.ui;

import com.organiza.model.ItemPedido;
import com.organiza.model.Mesa;
import com.organiza.service.PedidoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela para criação de novos pedidos.
 */
public class PedidoScreen {

    private final PedidoService pedidoService;
    private final Runnable onPedidoCriado;
    private final ObservableList<ItemPedido> itensObservable = FXCollections.observableArrayList();

    public PedidoScreen(PedidoService pedidoService, Runnable onPedidoCriado) {
        this.pedidoService = pedidoService;
        this.onPedidoCriado = onPedidoCriado;
    }

    public Scene createScene() {
        Label titulo = new Label("Novo Pedido");
        titulo.setFont(Font.font("Arial", 24));
        titulo.setStyle("-fx-font-weight: bold;");

        // Seleção de mesa
        Label lblMesa = new Label("Mesa:");
        lblMesa.setFont(Font.font("Arial", 18));

        ComboBox<String> comboMesa = new ComboBox<>();
        List<Mesa> mesas = pedidoService.listarMesas();
        for (Mesa mesa : mesas) {
            comboMesa.getItems().add("Mesa " + mesa.getNumero());
        }
        if (!comboMesa.getItems().isEmpty()) {
            comboMesa.getSelectionModel().selectFirst();
        }
        comboMesa.setStyle("-fx-font-size: 16;");

        HBox mesaBox = new HBox(10, lblMesa, comboMesa);
        mesaBox.setAlignment(Pos.CENTER_LEFT);

        // Entrada de itens
        Label lblProduto = new Label("Produto:");
        lblProduto.setFont(Font.font("Arial", 16));
        TextField txtProduto = new TextField();
        txtProduto.setPromptText("Ex: Pão na chapa");
        txtProduto.setFont(Font.font("Arial", 16));
        txtProduto.setPrefWidth(250);

        Label lblQtd = new Label("Qtd:");
        lblQtd.setFont(Font.font("Arial", 16));
        Spinner<Integer> spinnerQtd = new Spinner<>(1, 99, 1);
        spinnerQtd.setPrefWidth(80);
        spinnerQtd.setStyle("-fx-font-size: 16;");

        Button btnAdicionar = new Button("+ Adicionar");
        btnAdicionar.setFont(Font.font("Arial", 16));
        btnAdicionar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 16;");

        HBox itemBox = new HBox(10, lblProduto, txtProduto, lblQtd, spinnerQtd, btnAdicionar);
        itemBox.setAlignment(Pos.CENTER_LEFT);

        // Lista de itens adicionados
        ListView<ItemPedido> listItens = new ListView<>(itensObservable);
        listItens.setPrefHeight(200);
        listItens.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ItemPedido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getQuantidade() + "x " + item.getProduto());
                    setFont(Font.font("Arial", 16));
                }
            }
        });

        // Botão remover item
        Button btnRemover = new Button("Remover Selecionado");
        btnRemover.setFont(Font.font("Arial", 14));
        btnRemover.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 8 16;");
        btnRemover.setOnAction(e -> {
            ItemPedido selected = listItens.getSelectionModel().getSelectedItem();
            if (selected != null) {
                itensObservable.remove(selected);
            }
        });

        // Ação adicionar item
        btnAdicionar.setOnAction(e -> {
            String produto = txtProduto.getText().trim();
            if (!produto.isEmpty()) {
                itensObservable.add(new ItemPedido(produto, spinnerQtd.getValue()));
                txtProduto.clear();
                spinnerQtd.getValueFactory().setValue(1);
                txtProduto.requestFocus();
            }
        });

        // Ação enter no campo de produto
        txtProduto.setOnAction(e -> btnAdicionar.fire());

        // Botão salvar pedido
        Button btnSalvar = new Button("SALVAR PEDIDO");
        btnSalvar.setFont(Font.font("Arial", 20));
        btnSalvar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 40;");
        btnSalvar.setOnAction(e -> {
            if (itensObservable.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Adicione ao menos um item ao pedido.");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            int selectedIndex = comboMesa.getSelectionModel().getSelectedIndex();
            if (selectedIndex < 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Selecione uma mesa.");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            Mesa mesaSelecionada = mesas.get(selectedIndex);
            List<ItemPedido> itens = new ArrayList<>(itensObservable);

            try {
                pedidoService.criarPedido(mesaSelecionada.getId(), itens);
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Pedido criado para Mesa " + mesaSelecionada.getNumero() + "!");
                alert.setHeaderText(null);
                alert.showAndWait();
                itensObservable.clear();
                onPedidoCriado.run();
                // Fechar a janela
                btnSalvar.getScene().getWindow().hide();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao criar pedido: " + ex.getMessage());
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        VBox root = new VBox(15, titulo, mesaBox, itemBox, listItens, btnRemover, btnSalvar);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        return new Scene(root, 700, 550);
    }
}
