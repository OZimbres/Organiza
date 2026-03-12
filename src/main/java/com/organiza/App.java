package com.organiza;

import com.organiza.database.DatabaseConnection;
import com.organiza.repository.ClienteRepository;
import com.organiza.repository.MesaRepository;
import com.organiza.repository.PedidoRepository;
import com.organiza.repository.ProdutoRepository;
import com.organiza.service.ClienteService;
import com.organiza.service.PedidoService;
import com.organiza.service.ProdutoService;
import com.organiza.ui.AppShell;
import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.GraphicsEnvironment;

/**
 * Classe principal da aplicação JavaFX.
 */
public class App extends Application {

    private static final int TOTAL_MESAS = 10;

    @Override
    public void start(Stage primaryStage) {
        DatabaseConnection db = new DatabaseConnection();
        try {
            db.initializeDatabase();
        } catch (RuntimeException e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText("Falha ao inicializar o banco de dados");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                javafx.application.Platform.exit();
                System.exit(1);
            });
            return;
        }

        MesaRepository    mesaRepo    = new MesaRepository(db);
        PedidoRepository  pedidoRepo  = new PedidoRepository(db);
        ClienteRepository clienteRepo = new ClienteRepository(db);
        ProdutoRepository produtoRepo = new ProdutoRepository(db);

        PedidoService  pedidoService  = new PedidoService(mesaRepo, pedidoRepo);
        ClienteService clienteService = new ClienteService(clienteRepo);
        ProdutoService produtoService = new ProdutoService(produtoRepo);

        pedidoService.criarMesas(TOTAL_MESAS);

        AppShell appShell = new AppShell(pedidoService, clienteService, produtoService);
        primaryStage.setTitle("Organiza - Gestão de Pedidos");
        primaryStage.setScene(appShell.createScene(primaryStage));
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Check Java version (require >= 21)
        String javaVersion = System.getProperty("java.version");
        int major = parseMajorVersion(javaVersion);
        if (major < 21) {
            System.err.println("ERROR: Organiza requires Java 21 or newer. Detected: " + javaVersion);
            System.exit(2);
        }

        // Check for headless environment (JavaFX requires a display)
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("ERROR: No graphical display detected. JavaFX applications require a desktop session.");
            System.exit(3);
        }

        launch(args);
    }

    private static int parseMajorVersion(String version) {
        if (version == null || version.isEmpty()) return 0;
        // Examples: "21.0.10", "1.8.0_211"
        try {
            if (version.startsWith("1.")) {
                String[] parts = version.split("\\.");
                if (parts.length >= 2) return Integer.parseInt(parts[1]);
            } else {
                String[] parts = version.split("\\.");
                return Integer.parseInt(parts[0]);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }
}
