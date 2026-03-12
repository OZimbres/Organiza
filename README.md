# Organiza

Organiza é um OMS (Order Management System) desktop completo com foco no ciclo de vida de pedidos, com cadastro de itens e mesas. Sistema leve e focado que resolve uma dor imediata de gestão de pedidos em estabelecimentos como padarias e lanchonetes.

## Funcionalidades

- **Cadastro de mesas** — mesas numeradas com status visual (🟢 Livre, 🟡 Pendente/Em preparo, 🔵 Pronto)
- **Criação de pedidos** — pedidos associados a mesas com múltiplos itens
- **Fluxo de status** — PENDENTE → EM PREPARO → PRONTO → ENTREGUE → PAGO
- **Dashboard de mesas** — visão geral de todas as mesas com indicação de status
- **Tela da cozinha** — fila de pedidos pendentes e em preparo com ações rápidas
- **Liberação automática** — mesa volta a ficar livre quando todos os pedidos são pagos
- **Gestão de clientes** — CRUD completo de clientes
- **Gestão de produtos** — CRUD de itens do cardápio com categoria e preço
- **Relatórios** — receita total, pedidos por status, itens mais vendidos

## Tecnologias

- **Java 21** (LTS)
- **JavaFX 21** — interface gráfica desktop
- **SQLite** — banco de dados local, leve, sem instalação
- **Gradle** — build e gerenciamento de dependências
- **JUnit 5** — testes automatizados
- **Mockito 5** — mocks para testes de casos de uso

## Arquitetura

O projeto segue **Clean Architecture** com **Hexagonal Architecture (Ports & Adapters)**:

```
src/main/java/com/organiza/
├── App.java                                    # Bootstrap JavaFX
├── domain/                                     # Camada de Domínio (regras de negócio puras)
│   ├── entity/
│   │   ├── Mesa.java                           # Entidade Mesa
│   │   ├── Pedido.java                         # Entidade Pedido
│   │   ├── ItemPedido.java                     # Entidade Item do Pedido
│   │   ├── Cliente.java                        # Entidade Cliente
│   │   └── Produto.java                        # Entidade Produto
│   ├── enums/
│   │   ├── StatusMesa.java                     # Enum (LIVRE, OCUPADA)
│   │   ├── StatusPedido.java                   # Enum com State Pattern (transições validadas)
│   │   └── PaymentMethod.java                  # Métodos de pagamento
│   └── repository/                             # Ports (interfaces)
│       ├── MesaRepositoryPort.java
│       ├── PedidoRepositoryPort.java
│       ├── ClienteRepositoryPort.java
│       └── ProdutoRepositoryPort.java
├── application/                                # Camada de Aplicação (casos de uso)
│   ├── dto/
│   │   ├── MesaDTO.java                        # Record imutável
│   │   ├── OrderDTO.java                       # Record imutável
│   │   └── OrderItemDTO.java                   # Record imutável
│   └── usecase/
│       ├── CreateOrderUseCase.java             # Criação de pedidos
│       ├── UpdateOrderStatusUseCase.java       # Avanço de status
│       ├── ListTablesUseCase.java              # Consulta de mesas
│       ├── GenerateReportsUseCase.java         # Relatórios e analytics
│       ├── PedidoService.java                  # Serviço de pedidos (orquestrador)
│       ├── ClienteService.java                 # Serviço de clientes
│       └── ProdutoService.java                 # Serviço de produtos
├── infrastructure/                             # Camada de Infraestrutura (adapters)
│   ├── persistence/sqlite/
│   │   ├── SQLiteConnection.java               # Conexão e schema do SQLite
│   │   ├── SQLiteMesaRepository.java           # Implementação SQLite de MesaRepositoryPort
│   │   ├── SQLitePedidoRepository.java         # Implementação SQLite de PedidoRepositoryPort
│   │   ├── SQLiteClienteRepository.java        # Implementação SQLite de ClienteRepositoryPort
│   │   └── SQLiteProdutoRepository.java        # Implementação SQLite de ProdutoRepositoryPort
│   ├── config/
│   │   ├── RepositoryFactory.java              # Factory de repositórios
│   │   └── UseCaseFactory.java                 # Factory de casos de uso
│   └── exception/
│       ├── DatabaseException.java              # Erros de persistência
│       ├── BusinessException.java              # Violações de regra de negócio
│       └── ValidationException.java            # Erros de validação
└── ui/                                         # Camada de Interface
    ├── AppShell.java                           # Shell principal com navegação
    ├── MainScreen.java                         # Dashboard de mesas
    ├── PedidoScreen.java                       # Criação de pedidos
    ├── CozinhaScreen.java                      # Tela da cozinha
    ├── ClienteScreen.java                      # Gestão de clientes
    └── ProdutoScreen.java                      # Gestão de produtos
```

### Design System

O sistema inclui temas CSS baseados na paleta do figma-ai-OMS:

| Cor | Hex | Uso |
|-----|-----|-----|
| Primary | `#2563EB` | Botões, seleção, destaques |
| Success | `#10B981` | Status livre, pronto |
| Danger | `#EF4444` | Alertas, mesa ocupada |
| Warning | `#F59E0B` | Pendente, atenção |
| Dark | `#1F2937` | Fundo escuro |
| Light | `#F9FAFB` | Fundo claro |

Temas disponíveis em `src/main/resources/css/`:
- **Dark.css** — tema escuro (padrão)
- **Light.css** — tema claro

### Padrões de Design Aplicados

- **Clean Architecture** — separação clara de camadas (domain, application, infrastructure, ui)
- **Hexagonal Architecture (Ports & Adapters)** — interfaces de repositório no domínio, implementações na infraestrutura
- **State Pattern** — transições de status validadas em `StatusPedido`
- **Factory Pattern** — `RepositoryFactory` e `UseCaseFactory` centralizam criação de objetos
- **DTO Pattern** — Java 21 Records para transferência de dados imutáveis
- **SOLID Principles** — Single Responsibility, Interface Segregation, Dependency Inversion

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
- **pedidos** — id, mesa_id, nome_cliente, status, data_hora
- **itens_pedido** — id, pedido_id, produto, quantidade, preco
- **clientes** — id, nome, telefone
- **produtos** — id, nome, preco, categoria

## Fluxo de Uso

1. O garçom anota o pedido da mesa
2. O caixa registra o pedido no sistema (seleciona mesa + adiciona itens)
3. A cozinha visualiza os pedidos pendentes e marca como "em preparo" ou "pronto"
4. O garçom verifica no dashboard que a mesa está com pedido pronto e entrega
5. Após pagamento, o pedido é marcado como pago e a mesa é liberada

## Running in IntelliJ
If IntelliJ reports 'JavaFX runtime components are missing', use the 'Run via Gradle' run configuration (File: .idea/runConfigurations/Run via Gradle.xml) or enable 'Delegate IDE build/run actions to Gradle' in Settings > Build Tools > Gradle. Running via Gradle ensures JavaFX dependencies and the correct toolchain are used.

