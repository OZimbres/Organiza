# Organiza

Organiza é um OMS (Order Management System) com foco no ciclo de vida de pedidos, com cadastro de itens e mesas. Sistema leve e focado que resolve uma dor imediata de gestão de pedidos em estabelecimentos como padarias e lanchonetes.

## Funcionalidades

- **Cadastro de mesas** — mesas numeradas com status visual (🟢 Livre, 🟡 Pendente/Em preparo, 🔵 Pronto)
- **Criação de pedidos** — pedidos associados a mesas com múltiplos itens
- **Fluxo de status** — PENDENTE → EM PREPARO → PRONTO → ENTREGUE → PAGO
- **Dashboard de mesas** — visão geral de todas as mesas com indicação de status
- **Tela da cozinha** — fila de pedidos pendentes e em preparo com ações rápidas
- **Liberação automática** — mesa volta a ficar livre quando todos os pedidos são pagos

## Tecnologias

- **Java 21** (LTS)
- **JavaFX 21** — interface gráfica desktop
- **SQLite** — banco de dados local, leve, sem instalação
- **Gradle** — build e gerenciamento de dependências
- **JUnit 5** — testes automatizados

## Estrutura do Projeto

```
src/main/java/com/organiza/
├── App.java                         # Classe principal (JavaFX Application)
├── database/
│   └── DatabaseConnection.java      # Conexão e inicialização do SQLite
├── model/
│   ├── Mesa.java                    # Entidade Mesa
│   ├── Pedido.java                  # Entidade Pedido
│   ├── ItemPedido.java              # Entidade Item do Pedido
│   ├── StatusMesa.java              # Enum de status da mesa
│   └── StatusPedido.java            # Enum de status do pedido
├── repository/
│   ├── MesaRepository.java          # CRUD de mesas
│   └── PedidoRepository.java        # CRUD de pedidos e itens
├── service/
│   └── PedidoService.java           # Lógica de negócio
└── ui/
    ├── MainScreen.java              # Dashboard principal
    ├── PedidoScreen.java            # Tela de criação de pedido
    └── CozinhaScreen.java           # Tela da cozinha
```

## Como executar

### Pré-requisitos

- Java 21 (JDK)

Windows: configure JAVA_HOME to your JDK 21 installation (e.g., C:\\Program Files\\Java\\jdk-21) and add %JAVA_HOME%\\bin to PATH. Use .\\gradlew.bat on Windows.

### Build e execução

```bash
# Compilar
./gradlew build

# Executar a aplicação (Linux/macOS)
./gradlew run
# Executar a aplicação (Windows)
./gradlew.bat run

# Executar os testes
./gradlew test
```

Note: The project uses a Gradle Java toolchain for Java 21. If gradle.properties contains org.gradle.java.home pointing to a platform-specific JDK path, remove or update it to match your system. Prefer relying on the Gradle toolchain.

## Banco de Dados

O sistema utiliza SQLite com um arquivo local `padaria.db`. As tabelas são criadas automaticamente na primeira execução:

- **mesas** — id, numero, status
- **pedidos** — id, mesa_id, status, data_hora
- **itens_pedido** — id, pedido_id, produto, quantidade

## Fluxo de Uso

1. O garçom anota o pedido da mesa
2. O caixa registra o pedido no sistema (seleciona mesa + adiciona itens)
3. A cozinha visualiza os pedidos pendentes e marca como "em preparo" ou "pronto"
4. O garçom verifica no dashboard que a mesa está com pedido pronto e entrega
5. Após pagamento, o pedido é marcado como pago e a mesa é liberada

## Running in IntelliJ
If IntelliJ reports 'JavaFX runtime components are missing', use the 'Run via Gradle' run configuration (File: .idea/runConfigurations/Run via Gradle.xml) or enable 'Delegate IDE build/run actions to Gradle' in Settings > Build Tools > Gradle. Running via Gradle ensures JavaFX dependencies and the correct toolchain are used.

