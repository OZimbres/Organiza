package com.organiza.ui;

import com.organiza.application.usecase.ClienteService;
import com.organiza.application.usecase.GenerateReportsUseCase;
import com.organiza.application.usecase.PedidoService;
import com.organiza.application.usecase.ProdutoService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Shell da aplicação — monta a janela principal com layout de 3 painéis
 * (Mesas | Novo Pedido | Cozinha) usando SplitPane redimensionável.
 */
public class AppShell {

    private static final String BG_TOOLBAR = "#16213E";
    private static final String ACCENT     = "#E8A838";

    private final PedidoService  pedidoService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final GenerateReportsUseCase reportsUseCase;

    private Stage         primaryStage;
    private MainScreen    mainScreen;
    private CozinhaScreen cozinhaScreen;
    private PedidoScreen  pedidoScreen;
    private Label         lblKitchenBadge;

    public AppShell(PedidoService pedidoService,
                    ClienteService clienteService,
                    ProdutoService produtoService,
                    GenerateReportsUseCase reportsUseCase) {
        this.pedidoService  = pedidoService;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.reportsUseCase = reportsUseCase;
    }

    public Scene createScene(Stage stage) {
        this.primaryStage = stage;

        mainScreen    = new MainScreen(pedidoService, this::refreshAll);
        cozinhaScreen = new CozinhaScreen(pedidoService, this::refreshAll);
        pedidoScreen  = new PedidoScreen(pedidoService, clienteService, produtoService,
                                         this::onPedidoSaved);

        // ---- Toolbar ----
        Label lblLogo = new Label("🍽  Organiza");
        lblLogo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblLogo.setTextFill(Color.web(ACCENT));

        lblKitchenBadge = new Label("🍳  Cozinha");
        lblKitchenBadge.setFont(Font.font("Arial", 13));
        lblKitchenBadge.setTextFill(Color.web("#8888AA"));

        Button btnClientes    = toolbarButton("👤  Clientes");
        Button btnProdutos    = toolbarButton("🛒  Produtos");
        Button btnRelatorios  = toolbarButton("📊  Relatórios");

        btnClientes.setOnAction(e -> new ClienteScreen(clienteService).show(primaryStage));
        btnProdutos.setOnAction(e -> {
            new ProdutoScreen(produtoService).show(primaryStage);
            // Refresh pickers when focus returns to main window
            primaryStage.focusedProperty().addListener((obs, wasF, isF) -> {
                if (isF) pedidoScreen.refreshPickers();
            });
        });
        btnRelatorios.setOnAction(e -> new RelatorioScreen(reportsUseCase).show(primaryStage));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(16, lblLogo, spacer, lblKitchenBadge, btnClientes, btnProdutos, btnRelatorios);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 18, 10, 18));
        toolbar.setStyle("-fx-background-color: " + BG_TOOLBAR + ";");

        // ---- SplitPane (3 panes) ----
        SplitPane split = new SplitPane(
                mainScreen.createPane(),
                pedidoScreen.createPane(),
                cozinhaScreen.createPane()
        );
        split.setDividerPositions(0.35, 0.65);
        split.setStyle("-fx-background-color: #1C1C2E;");
        VBox.setVgrow(split, Priority.ALWAYS);

        // ---- Auto-refresh every 5 seconds ----
        Timeline autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> refreshAll()));
        autoRefresh.setCycleCount(Animation.INDEFINITE);
        autoRefresh.play();

        VBox root = new VBox(toolbar, split);
        root.setStyle("-fx-background-color: #1C1C2E;");

        Scene scene = new Scene(root, 1200, 720);
        scene.setFill(Color.web("#1C1C2E"));

        try {
            String css = AppShell.class.getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        return scene;
    }

    private void refreshAll() {
        mainScreen.refresh();
        cozinhaScreen.refresh();
        int count = cozinhaScreen.getPendingCount();
        if (lblKitchenBadge != null) {
            lblKitchenBadge.setText(count > 0
                    ? "🍳  Cozinha  (" + count + " pendente" + (count > 1 ? "s" : "") + ")"
                    : "🍳  Cozinha");
            lblKitchenBadge.setTextFill(count > 0
                    ? Color.web("#F39C12") : Color.web("#8888AA"));
        }
    }

    private void onPedidoSaved() {
        pedidoScreen.resetForm();
        refreshAll();
    }

    private Button toolbarButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: #0D1220; -fx-background-radius: 8;"
                + "-fx-border-color: #334; -fx-border-radius: 8; -fx-padding: 7 16;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #1A2840; -fx-background-radius: 8;"
                + "-fx-border-color: " + ACCENT + "66; -fx-border-radius: 8; -fx-padding: 7 16;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #0D1220; -fx-background-radius: 8;"
                + "-fx-border-color: #334; -fx-border-radius: 8; -fx-padding: 7 16;"));
        return btn;
    }
}

