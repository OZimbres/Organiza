package com.organiza;

import com.organiza.database.DatabaseConnection;
import com.organiza.repository.MesaRepository;
import com.organiza.repository.PedidoRepository;
import com.organiza.service.PedidoService;
import com.organiza.ui.MainScreen;
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

        // Monta a tela principal
        MainScreen mainScreen = new MainScreen(pedidoService, primaryStage);
        primaryStage.setTitle("Organiza - Gestão de Pedidos");
        primaryStage.setScene(mainScreen.createScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
