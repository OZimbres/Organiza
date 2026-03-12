package com.organiza.ui;

import com.organiza.domain.entity.Cliente;
import com.organiza.application.usecase.ClienteService;
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
 * Tela de gerenciamento de clientes (CRUD completo).
 */
public class ClienteScreen {

    private static final String BG      = "#1C1C2E";
    private static final String BG2     = "#16213E";
    private static final String ACCENT  = "#E8A838";
    private static final String DIM     = "#8888AA";

    private final ClienteService clienteService;
    private final ObservableList<Cliente> itens = FXCollections.observableArrayList();
    private TableView<Cliente> tabela;

    // Form fields
    private TextField txtNome;
    private TextField txtTelefone;
    private Cliente selecionado;

    public ClienteScreen(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /** Abre a janela de gerenciamento de clientes. */
    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.setTitle("Gerenciar Clientes");
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);

        stage.setScene(buildScene());
        stage.setWidth(640);
        stage.setHeight(500);
        stage.show();
    }

    private Scene buildScene() {
        Label titulo = new Label("👤  Clientes");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web(ACCENT));

        // ---- Table ----
        tabela = new TableView<>(itens);
        tabela.setStyle("-fx-background-color: #0D1220; -fx-text-fill: white;");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        TableColumn<Cliente, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colNome.setStyle("-fx-text-fill: white;");

        TableColumn<Cliente, String> colTel = new TableColumn<>("Telefone");
        colTel.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTelefone() != null ? c.getValue().getTelefone() : "—"));

        tabela.getColumns().addAll(colNome, colTel);
        styleTable(tabela);

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            selecionado = sel;
            if (sel != null) {
                txtNome.setText(sel.getNome());
                txtTelefone.setText(sel.getTelefone() != null ? sel.getTelefone() : "");
            }
        });

        // ---- Form ----
        txtNome = darkField("Ex: João da Silva");
        txtTelefone = darkField("Ex: (11) 99999-0000");

        Button btnSalvar  = actionButton("💾  Salvar",  "#27AE60", "#1E8449");
        Button btnDeletar = actionButton("🗑  Deletar", "#C0392B", "#922B21");
        Button btnNovo    = actionButton("＋  Novo",    "#3498DB", "#2271A8");

        btnNovo.setOnAction(e -> {
            tabela.getSelectionModel().clearSelection();
            selecionado = null;
            txtNome.clear();
            txtTelefone.clear();
            txtNome.requestFocus();
        });

        btnSalvar.setOnAction(e -> {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) { warn("Informe o nome do cliente."); return; }
            String tel = txtTelefone.getText().trim();
            try {
                if (selecionado == null) {
                    clienteService.salvar(new Cliente(nome, tel.isEmpty() ? null : tel));
                } else {
                    selecionado.setNome(nome);
                    selecionado.setTelefone(tel.isEmpty() ? null : tel);
                    clienteService.atualizar(selecionado);
                }
                recarregar();
                txtNome.clear(); txtTelefone.clear(); selecionado = null;
            } catch (Exception ex) { warn(ex.getMessage()); }
        });

        btnDeletar.setOnAction(e -> {
            if (selecionado == null) { warn("Selecione um cliente para deletar."); return; }
            clienteService.deletar(selecionado.getId());
            recarregar();
            txtNome.clear(); txtTelefone.clear(); selecionado = null;
        });

        HBox formRow = new HBox(12,
                new VBox(4, fieldLabel("Nome *"), txtNome),
                new VBox(4, fieldLabel("Telefone"), txtTelefone),
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
            String css = ClienteScreen.class.getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        return scene;
    }

    private void recarregar() {
        itens.setAll(clienteService.listar());
    }

    // ---- helpers ----

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
        for (TableColumn<T, ?> col : tv.getColumns()) {
            col.setStyle("-fx-background-color: #16213E; -fx-text-fill: #E0E0F0;");
        }
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
