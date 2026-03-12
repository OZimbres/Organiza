package com.organiza.ui;

import com.organiza.service.PedidoService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Shell da aplicação — monta o TabPane com Mesas, Cozinha e Novo Pedido,
 * e orquestra a atualização automática a cada 5 segundos.
 */
public class AppShell {

    private final PedidoService pedidoService;

    private TabPane tabPane;
    private Tab mesasTab;
    private Tab cozinhaTab;

    private MainScreen mainScreen;
    private CozinhaScreen cozinhaScreen;
    private PedidoScreen pedidoScreen;

    public AppShell(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public Scene createScene() {
        mainScreen    = new MainScreen(pedidoService, this::refreshAll);
        cozinhaScreen = new CozinhaScreen(pedidoService, this::refreshAll);
        pedidoScreen  = new PedidoScreen(pedidoService, this::onPedidoSaved);

        mesasTab              = buildTab("🍽  Mesas",       mainScreen.createPane());
        cozinhaTab            = buildTab("🍳  Cozinha",     cozinhaScreen.createPane());
        Tab pedidoTab         = buildTab("＋  Novo Pedido", pedidoScreen.createPane());

        tabPane = new TabPane(mesasTab, cozinhaTab, pedidoTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Timeline autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> refreshAll()));
        autoRefresh.setCycleCount(Animation.INDEFINITE);
        autoRefresh.play();

        VBox root = new VBox(tabPane);
        root.setStyle("-fx-background-color: #1C1C2E;");

        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(Color.web("#1C1C2E"));

        try {
            String css = AppShell.class.getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        return scene;
    }

    private Tab buildTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private void refreshAll() {
        mainScreen.refresh();
        cozinhaScreen.refresh();
        int count = cozinhaScreen.getPendingCount();
        cozinhaTab.setText(count > 0 ? "🍳  Cozinha (" + count + ")" : "🍳  Cozinha");
    }

    private void onPedidoSaved() {
        pedidoScreen.resetForm();
        refreshAll();
        tabPane.getSelectionModel().select(mesasTab);
    }
}
