package com.organiza;

import com.organiza.database.DatabaseConnection;
import com.organiza.repository.MesaRepository;
import com.organiza.repository.PedidoRepository;
import com.organiza.service.PedidoService;
import com.organiza.ui.AppShell;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Classe principal da aplicação JavaFX.
 */
public class App extends Application {

    private static final int TOTAL_MESAS = 10;

    @Override
    public void start(Stage primaryStage) {
        // Inicializa banco de dados
        DatabaseConnection db = new DatabaseConnection();
        db.initializeDatabase();

        // Inicializa repositórios e serviço
        MesaRepository mesaRepository = new MesaRepository(db);
        PedidoRepository pedidoRepository = new PedidoRepository(db);
        PedidoService pedidoService = new PedidoService(mesaRepository, pedidoRepository);

        // Cria mesas iniciais
        pedidoService.criarMesas(TOTAL_MESAS);

        // Monta a shell com abas unificadas
        AppShell appShell = new AppShell(pedidoService);
        primaryStage.setTitle("Organiza - Gestão de Pedidos");
        primaryStage.setScene(appShell.createScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
