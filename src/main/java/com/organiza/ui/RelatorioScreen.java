package com.organiza.ui;

import com.organiza.application.usecase.GenerateReportsUseCase;
import com.organiza.domain.entity.Report;
import com.organiza.domain.entity.RevenueStatistics;
import com.organiza.domain.enums.StatusPedido;
import javafx.application.Platform;
import javafx.concurrent.Task;
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

import java.util.Map;

/**
 * Tela de relatórios e analytics — exibe receita, contagens de pedidos,
 * itens mais vendidos e estatísticas gerais.
 * A geração de relatórios é feita em background para não travar a UI.
 */
public class RelatorioScreen {

    private static final String BG      = "#1C1C2E";
    private static final String BG_CARD = "#16213E";
    private static final String ACCENT  = "#E8A838";
    private static final String DIM     = "#8888AA";

    private final GenerateReportsUseCase reportsUseCase;

    public RelatorioScreen(GenerateReportsUseCase reportsUseCase) {
        this.reportsUseCase = reportsUseCase;
    }

    /** Abre a janela de relatórios. */
    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.setTitle("Relatórios & Analytics");
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);

        stage.setScene(buildScene());
        stage.setWidth(720);
        stage.setHeight(600);
        stage.show();
    }

    private Scene buildScene() {
        Label titulo = new Label("📊  Relatórios & Analytics");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web(ACCENT));

        Button btnRefresh = actionButton("↺  Atualizar", "#3498DB", "#2271A8");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titulo, spacer, btnRefresh);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 12, 0));

        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + BG + ";");

        content.getChildren().add(header);

        loadReportAsync(content, header, btnRefresh);

        btnRefresh.setOnAction(e -> loadReportAsync(content, header, btnRefresh));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");

        Scene scene = new Scene(scroll);
        scene.setFill(Color.web(BG));

        try {
            String css = RelatorioScreen.class.getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}

        return scene;
    }

    /**
     * Carrega o relatório em background (Thread separada) e atualiza a UI
     * no JavaFX Application Thread quando pronto.
     */
    private void loadReportAsync(VBox content, HBox header, Button btnRefresh) {
        btnRefresh.setDisable(true);

        content.getChildren().clear();
        content.getChildren().add(header);

        Label lblLoading = new Label("⏳  Carregando relatório...");
        lblLoading.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblLoading.setTextFill(Color.web(DIM));
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(40, 40);
        VBox loadingBox = new VBox(12, spinner, lblLoading);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(40));
        content.getChildren().add(loadingBox);

        Task<Report> task = new Task<>() {
            @Override
            protected Report call() {
                return reportsUseCase.gerarRelatorio();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Report report = task.getValue();
            content.getChildren().clear();
            content.getChildren().add(header);
            content.getChildren().addAll(
                    criarResumoCards(report),
                    criarPedidosPorStatus(report),
                    criarItensMaisVendidos(report),
                    criarEstatisticas(report)
            );
            btnRefresh.setDisable(false);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            content.getChildren().clear();
            content.getChildren().add(header);
            Label lblErro = new Label("❌  Erro ao carregar relatório.");
            lblErro.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            lblErro.setTextFill(Color.web("#EF4444"));
            content.getChildren().add(lblErro);
            btnRefresh.setDisable(false);
        }));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private HBox criarResumoCards(Report report) {
        VBox cardReceita = metricCard("💰  Receita Total",
                String.format("R$ %.2f", report.getReceitaTotal()), "#27AE60");

        VBox cardAtivos = metricCard("📋  Pedidos Ativos",
                String.valueOf(report.getPedidosAtivos()), "#3498DB");

        VBox cardTotal = metricCard("📦  Total de Pedidos",
                String.valueOf(report.getTotalPedidos()), "#9B59B6");

        HBox row = new HBox(16, cardReceita, cardAtivos, cardTotal);
        HBox.setHgrow(cardReceita, Priority.ALWAYS);
        HBox.setHgrow(cardAtivos, Priority.ALWAYS);
        HBox.setHgrow(cardTotal, Priority.ALWAYS);
        return row;
    }

    private VBox criarPedidosPorStatus(Report report) {
        Label lbl = sectionLabel("Pedidos por Status");

        VBox list = new VBox(6);
        list.setPadding(new Insets(8, 14, 8, 14));

        Map<StatusPedido, Long> porStatus = report.getPedidosPorStatus();
        for (StatusPedido status : StatusPedido.values()) {
            long count = porStatus.getOrDefault(status, 0L);
            String color = statusColor(status);

            Label lblStatus = new Label(status.getLabel());
            lblStatus.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            lblStatus.setTextFill(Color.web(color));
            lblStatus.setMinWidth(120);

            ProgressBar bar = new ProgressBar();
            long total = report.getTotalPedidos();
            bar.setProgress(total > 0 ? (double) count / total : 0);
            bar.setPrefWidth(200);
            bar.setMaxHeight(14);
            bar.setStyle("-fx-accent: " + color + ";");
            HBox.setHgrow(bar, Priority.ALWAYS);

            Label lblCount = new Label(String.valueOf(count));
            lblCount.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            lblCount.setTextFill(Color.web("#E0E0F0"));
            lblCount.setMinWidth(40);
            lblCount.setAlignment(Pos.CENTER_RIGHT);

            HBox row = new HBox(10, lblStatus, bar, lblCount);
            row.setAlignment(Pos.CENTER_LEFT);
            list.getChildren().add(row);
        }

        VBox section = new VBox(8, lbl, list);
        section.setPadding(new Insets(14));
        section.setStyle(cardStyle());
        return section;
    }

    private VBox criarItensMaisVendidos(Report report) {
        Label lbl = sectionLabel("🏆  Itens Mais Vendidos");

        VBox list = new VBox(6);
        list.setPadding(new Insets(8, 14, 8, 14));

        Map<String, Integer> topItens = report.getItensMaisVendidos();

        if (topItens.isEmpty()) {
            Label lblVazio = new Label("Nenhum item vendido ainda.");
            lblVazio.setFont(Font.font("Arial", 13));
            lblVazio.setTextFill(Color.web(DIM));
            list.getChildren().add(lblVazio);
        } else {
            topItens.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        Label lblItem = new Label(entry.getKey());
                        lblItem.setFont(Font.font("Arial", 13));
                        lblItem.setTextFill(Color.web("#E0E0F0"));
                        lblItem.setMinWidth(200);
                        HBox.setHgrow(lblItem, Priority.ALWAYS);

                        Label lblQtd = new Label(entry.getValue() + " un.");
                        lblQtd.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                        lblQtd.setTextFill(Color.web(ACCENT));

                        HBox row = new HBox(10, lblItem, lblQtd);
                        row.setAlignment(Pos.CENTER_LEFT);
                        list.getChildren().add(row);
                    });
        }

        VBox section = new VBox(8, lbl, list);
        section.setPadding(new Insets(14));
        section.setStyle(cardStyle());
        return section;
    }

    private VBox criarEstatisticas(Report report) {
        Label lbl = sectionLabel("📈  Estatísticas de Receita");

        VBox list = new VBox(6);
        list.setPadding(new Insets(8, 14, 8, 14));

        RevenueStatistics stats = report.getEstatisticas();

        if (stats == null || stats.count() == 0) {
            Label lblVazio = new Label("Sem dados estatísticos disponíveis.");
            lblVazio.setFont(Font.font("Arial", 13));
            lblVazio.setTextFill(Color.web(DIM));
            list.getChildren().add(lblVazio);
        } else {
            list.getChildren().addAll(
                    statRow("Pedidos pagos", String.valueOf(stats.count())),
                    statRow("Menor valor", String.format("R$ %.2f", stats.min())),
                    statRow("Maior valor", String.format("R$ %.2f", stats.max())),
                    statRow("Valor médio", String.format("R$ %.2f", stats.average())),
                    statRow("Total", String.format("R$ %.2f", stats.sum()))
            );
        }

        VBox section = new VBox(8, lbl, list);
        section.setPadding(new Insets(14));
        section.setStyle(cardStyle());
        return section;
    }

    // ---- helpers ----

    private VBox metricCard(String title, String value, String color) {
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Arial", 12));
        lblTitle.setTextFill(Color.web(DIM));

        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblValue.setTextFill(Color.web(color));

        VBox card = new VBox(6, lblTitle, lblValue);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        card.setStyle(cardStyle());
        return card;
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web(ACCENT));
        return lbl;
    }

    private HBox statRow(String label, String value) {
        Label lblName = new Label(label);
        lblName.setFont(Font.font("Arial", 13));
        lblName.setTextFill(Color.web("#C8C8E0"));
        lblName.setMinWidth(140);
        HBox.setHgrow(lblName, Priority.ALWAYS);

        Label lblVal = new Label(value);
        lblVal.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblVal.setTextFill(Color.web("#E0E0F0"));

        HBox row = new HBox(10, lblName, lblVal);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String statusColor(StatusPedido status) {
        return switch (status) {
            case PENDENTE   -> "#F39C12";
            case EM_PREPARO -> "#E67E22";
            case PRONTO     -> "#3498DB";
            case ENTREGUE   -> "#9B59B6";
            case PAGO       -> "#27AE60";
        };
    }

    private String cardStyle() {
        return "-fx-background-color: " + BG_CARD + ";"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: #33445566;"
                + "-fx-border-radius: 10;"
                + "-fx-border-width: 1;";
    }

    private Button actionButton(String text, String bg, String hover) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-padding: 8 16;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hover + "; -fx-background-radius: 8; -fx-padding: 8 16;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-padding: 8 16;"));
        return btn;
    }
}
