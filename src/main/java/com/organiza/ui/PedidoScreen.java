package com.organiza.ui;

import com.organiza.model.Cliente;
import com.organiza.model.ItemPedido;
import com.organiza.model.Mesa;
import com.organiza.model.Produto;
import com.organiza.service.ClienteService;
import com.organiza.service.PedidoService;
import com.organiza.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Formulário de novo pedido — exibido como painel central na janela principal.
 */
public class PedidoScreen {

    private static final String BG_APP   = "#1C1C2E";
    private static final String ACCENT   = "#E8A838";
    private static final String TEXT_DIM = "#8888AA";

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final Runnable onSaved;
    private final ObservableList<ItemPedido> itensObservable = FXCollections.observableArrayList();

    // Form fields kept as instance vars so resetForm() can clear them
    private ComboBox<Cliente>  comboCliente;
    private ComboBox<Produto>  comboProduto;
    private Spinner<Integer>   spinnerQtd;
    private Label              lblTotal;

    public PedidoScreen(PedidoService pedidoService,
                        ClienteService clienteService,
                        ProdutoService produtoService,
                        Runnable onSaved) {
        this.pedidoService  = pedidoService;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.onSaved = onSaved;
    }

    public Node createPane() {
        Label titulo = new Label("＋  Novo Pedido");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web(ACCENT));

        // ---- Mesa ----
        ComboBox<String> comboMesa = new ComboBox<>();
        List<Mesa> mesas = pedidoService.listarMesas();
        for (Mesa mesa : mesas) comboMesa.getItems().add("Mesa " + mesa.getNumero());
        if (!comboMesa.getItems().isEmpty()) comboMesa.getSelectionModel().selectFirst();
        styleCombo(comboMesa);

        // ---- Cliente picker ----
        comboCliente = new ComboBox<>();
        comboCliente.setMaxWidth(Double.MAX_VALUE);
        styleCombo(comboCliente);
        recarregarClientes();

        Button btnNovoCliente = miniButton("＋");
        btnNovoCliente.setTooltip(new Tooltip("Cadastrar novo cliente"));
        btnNovoCliente.setOnAction(e -> {
            Stage owner = (Stage) btnNovoCliente.getScene().getWindow();
            new ClienteScreen(clienteService).show(owner);
            // Refresh picker after the window is opened (user will add and come back)
            btnNovoCliente.getScene().getWindow().focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> { if (isFocused) recarregarClientes(); });
        });

        HBox clienteRow = new HBox(6, comboCliente, btnNovoCliente);
        clienteRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(comboCliente, Priority.ALWAYS);

        // ---- Produto picker ----
        comboProduto = new ComboBox<>();
        comboProduto.setMaxWidth(Double.MAX_VALUE);
        styleCombo(comboProduto);
        recarregarProdutos();

        Button btnNovoProduto = miniButton("＋");
        btnNovoProduto.setTooltip(new Tooltip("Cadastrar novo produto"));
        btnNovoProduto.setOnAction(e -> {
            Stage owner = (Stage) btnNovoProduto.getScene().getWindow();
            new ProdutoScreen(produtoService).show(owner);
            btnNovoProduto.getScene().getWindow().focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> { if (isFocused) recarregarProdutos(); });
        });

        HBox produtoRow = new HBox(6, comboProduto, btnNovoProduto);
        produtoRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(comboProduto, Priority.ALWAYS);

        // ---- Qty + Add ----
        spinnerQtd = new Spinner<>(1, 99, 1);
        spinnerQtd.setPrefWidth(70);
        spinnerQtd.setStyle("-fx-font-size: 13; -fx-background-color: #0D1220; -fx-text-fill: white;");

        Button btnAdicionar = actionButton("＋  Add", "#27AE60", "#1E8449");

        HBox itemRow = new HBox(10,
                new VBox(4, fieldLabel("Produto"), produtoRow),
                new VBox(4, fieldLabel("Qtd"), spinnerQtd),
                new VBox(4, new Label(" "), btnAdicionar)
        );
        itemRow.setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(itemRow.getChildren().get(0), Priority.ALWAYS);

        // ---- Item list ----
        ListView<ItemPedido> listItens = new ListView<>(itensObservable);
        listItens.setPrefHeight(150);
        listItens.setStyle("-fx-background-color: #0D1220; -fx-background-radius: 8;"
                + "-fx-border-color: #334; -fx-border-radius: 8;");
        listItens.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ItemPedido item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: transparent; -fx-text-fill: #E0E0F0;");
                if (empty || item == null) {
                    setText(null);
                } else {
                    String priceStr = item.getPreco() > 0
                            ? String.format("  —  R$ %.2f", item.getSubtotal()) : "";
                    setText(String.format("%dx  %s%s",
                            item.getQuantidade(), item.getProduto(), priceStr));
                    setFont(Font.font("Arial", 13));
                }
            }
        });

        lblTotal = new Label("Total: R$ 0,00");
        lblTotal.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lblTotal.setTextFill(Color.web(ACCENT));

        Runnable updateTotal = () -> {
            double total = itensObservable.stream().mapToDouble(ItemPedido::getSubtotal).sum();
            lblTotal.setText(String.format("Total: R$ %.2f", total));
        };

        Button btnRemover = actionButton("✕  Remover", "#C0392B", "#922B21");
        btnRemover.setOnAction(e -> {
            ItemPedido sel = listItens.getSelectionModel().getSelectedItem();
            if (sel != null) { itensObservable.remove(sel); updateTotal.run(); }
        });

        HBox listActions = new HBox(10, btnRemover, new Region(), lblTotal);
        HBox.setHgrow(listActions.getChildren().get(1), Priority.ALWAYS);
        listActions.setAlignment(Pos.CENTER_LEFT);

        // ---- Add item action ----
        btnAdicionar.setOnAction(e -> {
            Produto p = comboProduto.getSelectionModel().getSelectedItem();
            if (p == null) { warn("Selecione um produto."); return; }
            itensObservable.add(new ItemPedido(p.getNome(), spinnerQtd.getValue(), p.getPreco()));
            spinnerQtd.getValueFactory().setValue(1);
            updateTotal.run();
        });

        // ---- Save ----
        Button btnSalvar = actionButton("💾  Salvar Pedido", "#3498DB", "#2271A8");
        btnSalvar.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btnSalvar.setMaxWidth(Double.MAX_VALUE);
        btnSalvar.setOnAction(e -> {
            Cliente cliente = comboCliente.getSelectionModel().getSelectedItem();
            if (cliente == null) { warn("Selecione um cliente."); return; }
            if (itensObservable.isEmpty()) { warn("Adicione ao menos um item."); return; }
            int idx = comboMesa.getSelectionModel().getSelectedIndex();
            if (idx < 0) { warn("Selecione uma mesa."); return; }
            Mesa mesaSelecionada = mesas.get(idx);
            try {
                pedidoService.criarPedido(mesaSelecionada.getId(), cliente.getNome(),
                        new ArrayList<>(itensObservable));
                resetForm();
                onSaved.run();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erro: " + ex.getMessage());
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        VBox root = new VBox(12,
                titulo,
                separator(),
                new HBox(20, new VBox(4, fieldLabel("Mesa"), comboMesa),
                         new VBox(4, fieldLabel("Cliente"), clienteRow)),
                separator(),
                sectionLabel("Itens do Pedido"),
                itemRow,
                listItens,
                listActions,
                separator(),
                btnSalvar
        );
        ((HBox) root.getChildren().get(2)).setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(((HBox) root.getChildren().get(2)).getChildren().get(1), Priority.ALWAYS);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: " + BG_APP + ";");
        return root;
    }

    public void resetForm() {
        itensObservable.clear();
        if (comboCliente != null)  comboCliente.getSelectionModel().clearSelection();
        if (comboProduto != null)  comboProduto.getSelectionModel().clearSelection();
        if (spinnerQtd != null)    spinnerQtd.getValueFactory().setValue(1);
        if (lblTotal != null)      lblTotal.setText("Total: R$ 0,00");
    }

    public void refreshPickers() {
        recarregarClientes();
        recarregarProdutos();
    }

    private void recarregarClientes() {
        if (comboCliente == null) return;
        Cliente prev = comboCliente.getSelectionModel().getSelectedItem();
        comboCliente.getItems().setAll(clienteService.listar());
        if (prev != null) {
            comboCliente.getItems().stream()
                .filter(c -> c.getId() == prev.getId()).findFirst()
                .ifPresent(comboCliente.getSelectionModel()::select);
        }
    }

    private void recarregarProdutos() {
        if (comboProduto == null) return;
        Produto prev = comboProduto.getSelectionModel().getSelectedItem();
        comboProduto.getItems().setAll(produtoService.listar());
        if (prev != null) {
            comboProduto.getItems().stream()
                .filter(p -> p.getId() == prev.getId()).findFirst()
                .ifPresent(comboProduto.getSelectionModel()::select);
        }
    }

    // ---- helpers ----

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 12));
        l.setTextFill(Color.web(TEXT_DIM));
        return l;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l.setTextFill(Color.web(ACCENT));
        return l;
    }

    private void styleCombo(ComboBox<?> cb) {
        cb.setStyle("-fx-background-color: #0D1220; -fx-text-fill: white;"
                + "-fx-font-size: 13; -fx-background-radius: 6;");
    }

    private Button actionButton(String text, String bg, String hover) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 7; -fx-padding: 8 14;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hover + "; -fx-background-radius: 7; -fx-padding: 8 14;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-background-radius: 7; -fx-padding: 8 14;"));
        return btn;
    }

    private Button miniButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setTextFill(Color.web(ACCENT));
        btn.setPrefWidth(32);
        btn.setStyle("-fx-background-color: #0D1220; -fx-background-radius: 6;"
                + "-fx-border-color: " + ACCENT + "88; -fx-border-radius: 6; -fx-padding: 5 8;");
        return btn;
    }

    private Separator separator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334;");
        return sep;
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
