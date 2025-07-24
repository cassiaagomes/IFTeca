# IFTeca

O IFTeca é um aplicativo Android desenvolvido para facilitar a **reserva de salas de estudo** dentro de um ambiente institucional. Ele oferece aos usuários a capacidade de visualizar salas disponíveis por turno e, em seguida, selecionar horários específicos de 60 minutos para suas reservas, além de gerenciar suas próprias reservas. A interface é moderna e intuitiva, construída utilizando o Jetpack Compose.

---

## Funcionalidades

-   **Autenticação de Usuário:**
    Permite o cadastro e login do usuário utilizando um número de matrícula e senha, com dados de autenticação gerenciados pelo Firebase Authentication. 

-   **Visualização de Salas por Turno:**
    Exibe uma lista de salas de estudo disponíveis, permitindo ao usuário filtrar por turnos (`Manhã`, `Tarde`, `Noite`).

-   **Disponibilidade de Horários por Sala:**
    Para cada sala, o aplicativo mostra blocos de horários de 60 minutos disponíveis e ocupados dentro do turno selecionado. A disponibilidade é verificada em tempo real através das reservas existentes no Firebase Realtime Database.

-   **Reserva de Horários Específicos:**
    Capacidade de selecionar um bloco de horário de 60 minutos disponível em uma sala específica para iniciar o processo de reserva, utilizando transações atômicas no Firebase para garantir a integridade dos dados e evitar conflitos.

-   **Minhas Reservas:**
    Permite ao usuário visualizar uma lista de todas as suas reservas ativas, com detalhes como nome da sala, data e horário específico. É possível cancelar reservas diretamente por esta tela.

-   **Interface Moderna e Responsiva:**
    Desenvolvida com Jetpack Compose, o aplicativo possui um design limpo e adaptável a diferentes tamanhos de tela.

---

## Arquitetura do Aplicativo

O aplicativo segue o padrão arquitetural **MVVM (Model-View-ViewModel)**, que promove a separação de responsabilidades e facilita a manutenção e testabilidade do código:

-   **Model:**
    Define as estruturas de dados principais do aplicativo. Inclui `SalaInfo` e `Sala` para representar as informações e disponibilidade das salas e `MinhaReserva` para as reservas do usuário.
-   **View:**
    Implementa a interface do usuário utilizando o Jetpack Compose. As telas são funções `@Composable` que observam o estado fornecido pelos ViewModels. Exemplos incluem `LoginScreen`, `MenuScreen`, `SalasScreen` e `ReservasScreen`.

-   **ViewModel:**
    Contém a lógica de negócio e a comunicação com o Firebase. `SalasViewModel` gerencia a busca de salas e a lógica de reserva, enquanto `ReservasViewModel` lida com a recuperação e cancelamento das reservas do usuário. Ambos utilizam `StateFlow` para expor dados reativos à UI.

---

## Tecnologias Utilizadas

-   **Kotlin:** Linguagem de programação principal para o desenvolvimento Android.
-   **Jetpack Compose:** Framework moderno para construção de interfaces de usuário declarativas no Android.
-   **Firebase Authentication:** Serviço de autenticação em nuvem para gerenciamento de usuários.
-   **Firebase Realtime Database:** Banco de dados NoSQL em tempo real para armazenamento e sincronização dos dados das salas e reservas.
-   **Jetpack Navigation Compose:** Componente para gerenciar a navegação dentro do aplicativo Android.
-   **Gradle Kotlin DSL:** Sistema de automação de build para o projeto.

---

## Uso do Aplicativo

-   **Login e Cadastro:**
    Novos usuários podem se cadastrar fornecendo matrícula e senha. Usuários existentes podem fazer login para acessar o aplicativo.

-   **Visualizar e Reservar Salas:**
    No menu principal, acesse "Visualizar Salas". Filtre por turno e clique em uma sala para ver os horários de 60 minutos disponíveis. Selecione um slot para confirmar sua reserva.

-   **Minhas Reservas:**
    Acesse "Minhas Reservas" no menu principal para ver uma lista de todas as suas reservas ativas. Você pode cancelar qualquer reserva diretamente desta tela.

---

## Contribuições

Este projeto está sendo desenvolvido pela equipe **IFTeca**.

 - Cássia Gomes
 - Danillo Coelho
 - Pedro Henrique
