package com.organiza.ui;

import com.organiza.model.Produto;
import com.organiza.service.ProdutoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Tela de gerenciamento de produtos do cardápio (CRUD completo).
 */
public class ProdutoScreen {

    private static final String BG     = "#1C1C2E";
    private static final String ACCENT = "#E8A838";
    private static final String DIM    = "#8888AA";

    private final ProdutoService produtoService;
    private final ObservableList<Produto> itens = FXCollections.observableArrayList();
    private TableView<Produto> tabela;

    private TextField txtNome;
    private TextField txtPreco;
    private TextField txtCategoria;
    private Produto selecionado;

    public ProdutoScreen(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /** Abre a janela de gerenciamento de produtos. */
    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.setTitle("Gerenciar Produtos");
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);

        stage.setScene(buildScene());
        stage.setWidth(700);
        stage.setHeight(520);
        stage.show();
    }

    private Scene buildScene() {
        Label titulo = new Label("🛒  Produtos");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web(ACCENT));

        // ---- Table ----
        tabela = new TableView<>(itens);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        TableColumn<Produto, String> colNome  = col("Produto",    p -> p.getNome());
        TableColumn<Produto, String> colPreco = col("Preço (R$)", p ->
                String.format("%.2f", p.getPreco()));
        TableColumn<Produto, String> colCat   = col("Categoria",  p ->
                p.getCategoria() != null ? p.getCategoria() : "—");

        tabela.getColumns().addAll(colNome, colPreco, colCat);
        styleTable(tabela);

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            selecionado = sel;
            if (sel != null) {
                txtNome.setText(sel.getNome());
                txtPreco.setText(String.format("%.2f", sel.getPreco()).replace('.', ','));
                txtCategoria.setText(sel.getCategoria() != null ? sel.getCategoria() : "");
            }
        });

        // ---- Form ----
        txtNome      = darkField("Ex: Pão na Chapa");
        txtPreco     = darkField("0,00");
        txtCategoria = darkField("Ex: Salgados");

        txtPreco.setPrefWidth(90);
        txtCategoria.setPrefWidth(120);

        Button btnNovo    = actionButton("＋  Novo",    "#3498DB", "#2271A8");
        Button btnSalvar  = actionButton("💾  Salvar",  "#27AE60", "#1E8449");
        Button btnDeletar = actionButton("🗑  Deletar", "#C0392B", "#922B21");

        btnNovo.setOnAction(e -> {
            tabela.getSelectionModel().clearSelection();
            selecionado = null;
            txtNome.clear(); txtPreco.setText("0,00"); txtCategoria.clear();
            txtNome.requestFocus();
        });

        btnSalvar.setOnAction(e -> {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) { warn("Informe o nome do produto."); return; }
            double preco = 0;
            try { preco = Double.parseDouble(txtPreco.getText().replace(",", ".").trim()); }
            catch (NumberFormatException ex) { warn("Preço inválido."); return; }
            String cat = txtCategoria.getText().trim();
            try {
                if (selecionado == null) {
                    produtoService.salvar(new Produto(nome, preco, cat.isEmpty() ? null : cat));
                } else {
                    selecionado.setNome(nome);
                    selecionado.setPreco(preco);
                    selecionado.setCategoria(cat.isEmpty() ? null : cat);
                    produtoService.atualizar(selecionado);
                }
                recarregar();
                txtNome.clear(); txtPreco.setText("0,00"); txtCategoria.clear(); selecionado = null;
            } catch (Exception ex) { warn(ex.getMessage()); }
        });

        btnDeletar.setOnAction(e -> {
            if (selecionado == null) { warn("Selecione um produto para deletar."); return; }
            produtoService.deletar(selecionado.getId());
            recarregar();
            txtNome.clear(); txtPreco.setText("0,00"); txtCategoria.clear(); selecionado = null;
        });

        HBox formRow = new HBox(12,
                new VBox(4, fieldLabel("Produto *"), txtNome),
                new VBox(4, fieldLabel("Preço (R$)"), txtPreco),
                new VBox(4, fieldLabel("Categoria"), txtCategoria),
                new VBox(4, new Label(" "), new HBox(8, btnNovo, btnSalvar, btnDeletar))
        );
        formRow.setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(formRow.getChildren().get(0), Priority.ALWAYS);

        VBox root = new VBox(12, titulo, tabela, new Separator(), formRow);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG + ";");

        recarregar();
        Scene scene = new Scene(root);
        scene.setFill(Color.web(BG));

        try {
            String css = ProdutoScreen.class.getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        return scene;
    }

    private void recarregar() {
        itens.setAll(produtoService.listar());
    }

    // ---- helpers ----

    private TableColumn<Produto, String> col(String header,
            java.util.function.Function<Produto, String> getter) {
        TableColumn<Produto, String> c = new TableColumn<>(header);
        c.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        return c;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 12));
        l.setTextFill(Color.web(DIM));
        return l;
    }

    private TextField darkField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Arial", 13));
        tf.setStyle("-fx-background-color: #0D1220; -fx-text-fill: white;"
                + "-fx-prompt-text-fill: #555577; -fx-background-radius: 6;"
                + "-fx-border-color: #334; -fx-border-radius: 6; -fx-padding: 6 10;");
        return tf;
    }

    private Button actionButton(String text, String bg, String hover) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 6; -fx-padding: 7 14;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hover + "; -fx-background-radius: 6; -fx-padding: 7 14;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-background-radius: 6; -fx-padding: 7 14;"));
        return btn;
    }

    private <T> void styleTable(TableView<T> tv) {
        tv.setStyle("-fx-background-color: #0D1220; -fx-background-radius: 8;"
                + "-fx-border-color: #334; -fx-border-radius: 8;");
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
