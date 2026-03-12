package com.organiza.ui;

import com.organiza.model.ItemPedido;
import com.organiza.model.Mesa;
import com.organiza.service.PedidoService;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Formulário de novo pedido — exibido como aba na janela principal.
 */
public class PedidoScreen {

    private static final String BG_APP   = "#1C1C2E";
    private static final String ACCENT   = "#E8A838";
    private static final String TEXT_DIM = "#8888AA";

    private final PedidoService pedidoService;
    private final Runnable onSaved;
    private final ObservableList<ItemPedido> itensObservable = FXCollections.observableArrayList();

    // Form fields kept as instance vars so resetForm() can clear them
    private TextField txtNome;
    private TextField txtProduto;
    private TextField txtPreco;
    private Spinner<Integer> spinnerQtd;
    private Label lblTotal;

    public PedidoScreen(PedidoService pedidoService, Runnable onSaved) {
        this.pedidoService = pedidoService;
        this.onSaved = onSaved;
    }

    public Node createPane() {
        Label titulo = new Label("＋  Novo Pedido");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.web(ACCENT));

        // ---- Mesa ----
        ComboBox<String> comboMesa = new ComboBox<>();
        List<Mesa> mesas = pedidoService.listarMesas();
        for (Mesa mesa : mesas) comboMesa.getItems().add("Mesa " + mesa.getNumero());
        if (!comboMesa.getItems().isEmpty()) comboMesa.getSelectionModel().selectFirst();
        styleCombo(comboMesa);

        // ---- Nome do cliente ----
        txtNome = darkField("Ex: João da Silva");

        VBox clienteBox = new VBox(5, fieldLabel("Nome do Cliente"), txtNome);
        HBox.setHgrow(clienteBox, Priority.ALWAYS);
        txtNome.setMaxWidth(Double.MAX_VALUE);

        HBox topRow = new HBox(20, new VBox(5, fieldLabel("Mesa"), comboMesa), clienteBox);
        topRow.setAlignment(Pos.BOTTOM_LEFT);

        // ---- Item entry ----
        txtProduto = darkField("Ex: Pão na chapa");
        txtProduto.setPrefWidth(220);

        spinnerQtd = new Spinner<>(1, 99, 1);
        spinnerQtd.setPrefWidth(75);
        spinnerQtd.setStyle("-fx-font-size: 14; -fx-background-color: #0D1220; -fx-text-fill: white;");

        txtPreco = darkField("0,00");
        txtPreco.setPrefWidth(90);

        Button btnAdicionar = actionButton("＋ Adicionar", "#27AE60", "#1E8449");

        HBox itemRow = new HBox(12,
                new VBox(5, fieldLabel("Produto"), txtProduto),
                new VBox(5, fieldLabel("Qtd"), spinnerQtd),
                new VBox(5, fieldLabel("Preço Unit. (R$)"), txtPreco),
                new VBox(5, new Label(" "), btnAdicionar)
        );
        itemRow.setAlignment(Pos.BOTTOM_LEFT);

        // ---- Item list ----
        ListView<ItemPedido> listItens = new ListView<>(itensObservable);
        listItens.setPrefHeight(180);
        listItens.setStyle("-fx-background-color: #0D1220; -fx-background-radius: 8; -fx-border-color: #334; -fx-border-radius: 8;");
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
                    setText(String.format("%dx  %s%s", item.getQuantidade(), item.getProduto(), priceStr));
                    setFont(Font.font("Arial", 14));
                }
            }
        });

        lblTotal = new Label("Total: R$ 0,00");
        lblTotal.setFont(Font.font("Arial", FontWeight.BOLD, 16));
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

        HBox listActions = new HBox(12, btnRemover, new Region(), lblTotal);
        HBox.setHgrow(listActions.getChildren().get(1), Priority.ALWAYS);
        listActions.setAlignment(Pos.CENTER_LEFT);

        // ---- Add item action ----
        btnAdicionar.setOnAction(e -> {
            String produto = txtProduto.getText().trim();
            if (produto.isEmpty()) return;
            double preco = 0;
            try { preco = Double.parseDouble(txtPreco.getText().replace(",", ".").trim()); }
            catch (NumberFormatException ignored) {}
            itensObservable.add(new ItemPedido(produto, spinnerQtd.getValue(), preco));
            txtProduto.clear();
            txtPreco.setText("0,00");
            spinnerQtd.getValueFactory().setValue(1);
            txtProduto.requestFocus();
            updateTotal.run();
        });
        txtProduto.setOnAction(e -> btnAdicionar.fire());

        // ---- Save ----
        Button btnSalvar = actionButton("💾  Salvar Pedido", "#3498DB", "#2271A8");
        btnSalvar.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btnSalvar.setPrefWidth(Double.MAX_VALUE);
        btnSalvar.setOnAction(e -> {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) { warn("Informe o nome do cliente."); return; }
            if (itensObservable.isEmpty()) { warn("Adicione ao menos um item."); return; }
            int idx = comboMesa.getSelectionModel().getSelectedIndex();
            if (idx < 0) { warn("Selecione uma mesa."); return; }

            Mesa mesaSelecionada = mesas.get(idx);
            try {
                pedidoService.criarPedido(mesaSelecionada.getId(), nome, new ArrayList<>(itensObservable));
                resetForm();
                onSaved.run();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erro: " + ex.getMessage());
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        VBox root = new VBox(16,
                titulo,
                separator(),
                topRow,
                separator(),
                sectionLabel("Itens do Pedido"),
                itemRow,
                listItens,
                listActions,
                separator(),
                btnSalvar
        );
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: " + BG_APP + ";");
        return root;
    }

    public void resetForm() {
        itensObservable.clear();
        if (txtNome != null)     txtNome.clear();
        if (txtProduto != null)  txtProduto.clear();
        if (txtPreco != null)    txtPreco.setText("0,00");
        if (spinnerQtd != null)  spinnerQtd.getValueFactory().setValue(1);
        if (lblTotal != null)    lblTotal.setText("Total: R$ 0,00");
    }

    // ---- Helpers ----

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 12));
        l.setTextFill(Color.web(TEXT_DIM));
        return l;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        l.setTextFill(Color.web(ACCENT));
        return l;
    }

    private TextField darkField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Arial", 14));
        tf.setStyle("-fx-background-color: #0D1220; -fx-text-fill: white;"
                + "-fx-prompt-text-fill: #555577; -fx-background-radius: 6;"
                + "-fx-border-color: #334; -fx-border-radius: 6; -fx-padding: 7 10;");
        return tf;
    }

    private void styleCombo(ComboBox<String> cb) {
        cb.setStyle("-fx-background-color: #0D1220; -fx-text-fill: white;"
                + "-fx-font-size: 14; -fx-background-radius: 6;");
    }

    private Button actionButton(String text, String bg, String hover) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 7; -fx-padding: 9 18;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hover + "; -fx-background-radius: 7; -fx-padding: 9 18;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-background-radius: 7; -fx-padding: 9 18;"));
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
