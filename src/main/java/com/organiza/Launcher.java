package com.organiza;

/**
 * Ponto de entrada para o JAR executável.
 * Esta classe não estende javafx.application.Application, o que é necessário
 * para que o fat JAR funcione corretamente sem o módulo JavaFX no module-path.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
