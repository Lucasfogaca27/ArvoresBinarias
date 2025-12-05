import java.util.Scanner;
import java.text.Normalizer;
import java.util.regex.Pattern;

// ==================================================================
// PARTE 1: ESTRUTURAS DE DADOS (NODOS)
// ==================================================================

// Nodo da Árvore de Estudantes (ABP Interna)
class NodoEstudante {
    String nome;
    NodoEstudante esquerda;
    NodoEstudante direita;

    public NodoEstudante(String nome) {
        this.nome = nome;
        this.esquerda = null;
        this.direita = null;
    }
}

// Nodo da Árvore de Turmas (ABP Principal)
class NodoTurma {
    int codigo;
    NodoTurma esquerda;
    NodoTurma direita;
    NodoEstudante raizEstudantes; // Referência para a árvore de alunos desta turma
    int qtdEstudantes; // Contador cacheado para facilitar operações

    public NodoTurma(int codigo) {
        this.codigo = codigo;
        this.esquerda = null;
        this.direita = null;
        this.raizEstudantes = null;
        this.qtdEstudantes = 0;
    }
}

// Auxiliar para a listagem global ordenada (Simulando uma lista encadeada simples)
class NodoLista {
    String nomeEstudante;
    int codigoTurma;
    NodoLista proximo;

    public NodoLista(String nome, int turma) {
        this.nomeEstudante = nome;
        this.codigoTurma = turma;
        this.proximo = null;
    }
}

// ==================================================================
// PARTE 2: LÓGICA DO SISTEMA (GERENCIADOR)
// ==================================================================

public class Main {

    private NodoTurma raizTurmas;

    public Main() {
        this.raizTurmas = null;
    }

    // --- UTILS ---
    // Normaliza string: minúscula e sem acentos (Regra do PDF)
    private String formatarNome(String nome) {
        if (nome == null) return "";
        String nfdNormalizedString = Normalizer.normalize(nome, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").toLowerCase().trim();
    }

    // ==============================================================
    // OPERAÇÕES DE TURMA (ABP EXTERNA)
    // ==============================================================

    public void insereTurma(int codigo) {
        raizTurmas = inserirTurmaRec(raizTurmas, codigo);
    }

    private NodoTurma inserirTurmaRec(NodoTurma raiz, int codigo) {
        if (raiz == null) {
            System.out.println("Turma " + codigo + " criada com sucesso.");
            return new NodoTurma(codigo);
        }
        if (codigo < raiz.codigo) {
            raiz.esquerda = inserirTurmaRec(raiz.esquerda, codigo);
        } else if (codigo > raiz.codigo) {
            raiz.direita = inserirTurmaRec(raiz.direita, codigo);
        } else {
            System.out.println("Erro: Turma " + codigo + " já existe.");
        }
        return raiz;
    }

    public NodoTurma buscarTurma(int codigo) {
        return buscarTurmaRec(raizTurmas, codigo);
    }

    private NodoTurma buscarTurmaRec(NodoTurma raiz, int codigo) {
        if (raiz == null || raiz.codigo == codigo) {
            return raiz;
        }
        if (codigo < raiz.codigo) {
            return buscarTurmaRec(raiz.esquerda, codigo);
        }
        return buscarTurmaRec(raiz.direita, codigo);
    }

    public void removeTurma(int codigo) {
        raizTurmas = removeTurmaRec(raizTurmas, codigo);
    }

    private NodoTurma removeTurmaRec(NodoTurma raiz, int codigo) {
        if (raiz == null) {
            System.out.println("Turma não encontrada.");
            return null;
        }

        if (codigo < raiz.codigo) {
            raiz.esquerda = removeTurmaRec(raiz.esquerda, codigo);
        } else if (codigo > raiz.codigo) {
            raiz.direita = removeTurmaRec(raiz.direita, codigo);
        } else {
            // Encontrou a turma para remover
            // Caso 1: Folha ou 1 filho
            if (raiz.esquerda == null) return raiz.direita;
            if (raiz.direita == null) return raiz.esquerda;

            // Caso 2: 2 filhos -> Pega o menor do lado direito (sucessor)
            NodoTurma sucessor = encontrarMinimoTurma(raiz.direita);
            raiz.codigo = sucessor.codigo;
            raiz.raizEstudantes = sucessor.raizEstudantes; // Importante: move os alunos
            raiz.qtdEstudantes = sucessor.qtdEstudantes;

            // Remove o sucessor antigo
            raiz.direita = removeTurmaRec(raiz.direita, sucessor.codigo);
            System.out.println("Turma removida.");
        }
        return raiz;
    }

    private NodoTurma encontrarMinimoTurma(NodoTurma raiz) {
        while (raiz.esquerda != null) {
            raiz = raiz.esquerda;
        }
        return raiz;
    }

    // ==============================================================
    // OPERAÇÕES DE ESTUDANTE (ABP INTERNA)
    // ==============================================================

    public void insereEstudante(int codTurma, String nomeCru) {
        NodoTurma turma = buscarTurma(codTurma);
        if (turma == null) {
            System.out.println("Erro: Turma " + codTurma + " não encontrada. Crie a turma primeiro.");
            return;
        }
        String nome = formatarNome(nomeCru);
        // Verifica duplicata na mesma turma antes de inserir
        if (buscarEstudanteRec(turma.raizEstudantes, nome)) {
            System.out.println("Erro: Estudante '" + nome + "' já existe na turma " + codTurma);
            return;
        }

        turma.raizEstudantes = insereEstudanteRec(turma.raizEstudantes, nome);
        turma.qtdEstudantes++;
        System.out.println("Estudante " + nome + " inserido na turma " + codTurma);
    }

    private NodoEstudante insereEstudanteRec(NodoEstudante raiz, String nome) {
        if (raiz == null) {
            return new NodoEstudante(nome);
        }
        if (nome.compareTo(raiz.nome) < 0) {
            raiz.esquerda = insereEstudanteRec(raiz.esquerda, nome);
        } else if (nome.compareTo(raiz.nome) > 0) {
            raiz.direita = insereEstudanteRec(raiz.direita, nome);
        }
        return raiz;
    }

    private boolean buscarEstudanteRec(NodoEstudante raiz, String nome) {
        if (raiz == null) return false;
        int cmp = nome.compareTo(raiz.nome);
        if (cmp == 0) return true;
        if (cmp < 0) return buscarEstudanteRec(raiz.esquerda, nome);
        return buscarEstudanteRec(raiz.direita, nome);
    }

    public void removeEstudante(int codTurma, String nomeCru) {
        NodoTurma turma = buscarTurma(codTurma);
        if (turma == null) {
            System.out.println("Turma não existe.");
            return;
        }
        String nome = formatarNome(nomeCru);

        // Verifica se existe antes de tentar remover para ajustar contador
        if (buscarEstudanteRec(turma.raizEstudantes, nome)) {
            turma.raizEstudantes = removeEstudanteRec(turma.raizEstudantes, nome);
            turma.qtdEstudantes--;
            System.out.println("Estudante removido.");
        } else {
            System.out.println("Estudante não encontrado nesta turma.");
        }
    }

    private NodoEstudante removeEstudanteRec(NodoEstudante raiz, String nome) {
        if (raiz == null) return null;

        int cmp = nome.compareTo(raiz.nome);
        if (cmp < 0) {
            raiz.esquerda = removeEstudanteRec(raiz.esquerda, nome);
        } else if (cmp > 0) {
            raiz.direita = removeEstudanteRec(raiz.direita, nome);
        } else {
            // Achou
            if (raiz.esquerda == null) return raiz.direita;
            if (raiz.direita == null) return raiz.esquerda;

            NodoEstudante sucessor = encontrarMinimoEstudante(raiz.direita);
            raiz.nome = sucessor.nome;
            raiz.direita = removeEstudanteRec(raiz.direita, sucessor.nome);
        }
        return raiz;
    }

    private NodoEstudante encontrarMinimoEstudante(NodoEstudante raiz) {
        while (raiz.esquerda != null) raiz = raiz.esquerda;
        return raiz;
    }

    // ==============================================================
    // RELATÓRIOS E EXIBIÇÕES
    // ==============================================================

    public void exibeTurma(int codTurma, String ordem) {
        NodoTurma turma = buscarTurma(codTurma);
        if (turma == null) {
            System.out.println("Turma não encontrada.");
            return;
        }
        System.out.println("--- Turma " + codTurma + " (" + ordem + ") ---");
        if (ordem.equals("AZ")) emOrdem(turma.raizEstudantes);
        else if (ordem.equals("ZA")) emOrdemInversa(turma.raizEstudantes);
        else if (ordem.equals("PRE")) preOrdem(turma.raizEstudantes);
        System.out.println();
    }

    private void emOrdem(NodoEstudante raiz) {
        if (raiz != null) {
            emOrdem(raiz.esquerda);
            System.out.println("- " + raiz.nome);
            emOrdem(raiz.direita);
        }
    }

    private void emOrdemInversa(NodoEstudante raiz) {
        if (raiz != null) {
            emOrdemInversa(raiz.direita);
            System.out.println("- " + raiz.nome);
            emOrdemInversa(raiz.esquerda);
        }
    }

    private void preOrdem(NodoEstudante raiz) {
        if (raiz != null) {
            System.out.println("- " + raiz.nome);
            preOrdem(raiz.esquerda);
            preOrdem(raiz.direita);
        }
    }

    // Exibe lista GLOBAL ordenada (O desafio da lista única)
    public void exibeTodosEstudantes() {
        System.out.println("--- Lista Global de Estudantes (A-Z) ---");
        NodoLista listaOrdenada = null;
        listaOrdenada = coletarTodos(raizTurmas, listaOrdenada);

        NodoLista atual = listaOrdenada;
        while (atual != null) {
            System.out.println(atual.nomeEstudante + " (Turma: " + atual.codigoTurma + ")");
            atual = atual.proximo;
        }
    }

    // Percorre turmas e insere na lista encadeada auxiliar de forma ordenada
    private NodoLista coletarTodos(NodoTurma raizT, NodoLista lista) {
        if (raizT != null) {
            lista = coletarTodos(raizT.esquerda, lista);
            lista = coletarEstudantesNaLista(raizT.raizEstudantes, raizT.codigo, lista);
            lista = coletarTodos(raizT.direita, lista);
        }
        return lista;
    }

    private NodoLista coletarEstudantesNaLista(NodoEstudante raizE, int turma, NodoLista lista) {
        if (raizE != null) {
            lista = coletarEstudantesNaLista(raizE.esquerda, turma, lista);
            lista = inserirNaListaOrdenada(lista, raizE.nome, turma);
            lista = coletarEstudantesNaLista(raizE.direita, turma, lista);
        }
        return lista;
    }

    // Inserção ordenada em lista encadeada simples (Manual)
    private NodoLista inserirNaListaOrdenada(NodoLista cabeca, String nome, int turma) {
        NodoLista novo = new NodoLista(nome, turma);
        if (cabeca == null || nome.compareTo(cabeca.nomeEstudante) < 0) {
            novo.proximo = cabeca;
            return novo;
        }
        NodoLista atual = cabeca;
        while (atual.proximo != null && atual.proximo.nomeEstudante.compareTo(nome) <= 0) {
            atual = atual.proximo;
        }
        novo.proximo = atual.proximo;
        atual.proximo = novo;
        return cabeca;
    }

    // Estatísticas
    public void exibirEstatisticas() {
        if (raizTurmas == null) {
            System.out.println("Nenhuma turma cadastrada.");
            return;
        }
        System.out.println("\n--- Estatísticas ---");
        // Conta estudantes por turma
        contaEstudantesRec(raizTurmas);

        // Maior e Menor
        int[] dados = {Integer.MIN_VALUE, Integer.MAX_VALUE}; // [0]=max, [1]=min
        calcularMinMax(raizTurmas, dados);

        System.out.println("Maior nº de alunos em uma turma: " + (dados[0] == Integer.MIN_VALUE ? 0 : dados[0]));
        System.out.println("Menor nº de alunos em uma turma: " + (dados[1] == Integer.MAX_VALUE ? 0 : dados[1]));

        System.out.print("Turmas com MAIOR nº: ");
        exibirTurmasComQtd(raizTurmas, dados[0]);
        System.out.println();

        System.out.print("Turmas com MENOR nº: ");
        exibirTurmasComQtd(raizTurmas, dados[1]);
        System.out.println();
    }

    private void contaEstudantesRec(NodoTurma raiz) {
        if (raiz != null) {
            contaEstudantesRec(raiz.esquerda);
            System.out.println("Turma " + raiz.codigo + ": " + raiz.qtdEstudantes + " estudantes.");
            contaEstudantesRec(raiz.direita);
        }
    }

    private void calcularMinMax(NodoTurma raiz, int[] dados) {
        if (raiz != null) {
            if (raiz.qtdEstudantes > dados[0]) dados[0] = raiz.qtdEstudantes;
            if (raiz.qtdEstudantes < dados[1]) dados[1] = raiz.qtdEstudantes;
            calcularMinMax(raiz.esquerda, dados);
            calcularMinMax(raiz.direita, dados);
        }
    }

    private void exibirTurmasComQtd(NodoTurma raiz, int alvo) {
        if (raiz != null) {
            exibirTurmasComQtd(raiz.esquerda, alvo);
            if (raiz.qtdEstudantes == alvo) System.out.print(raiz.codigo + " ");
            exibirTurmasComQtd(raiz.direita, alvo);
        }
    }

    // Nomes Repetidos (Naive approach: percorre tudo e conta)
    // Para ser eficiente sem HashMap seria complexo, vamos usar a lista ordenada já criada
    public void exibirNomesRepetidos() {
        System.out.println("\n--- Nomes Repetidos (em mais de uma turma) ---");
        NodoLista lista = null;
        lista = coletarTodos(raizTurmas, lista);

        if (lista == null) return;

        NodoLista atual = lista;
        String nomeAnterior = "";
        boolean jaImprimiu = false;

        while (atual.proximo != null) {
            if (atual.nomeEstudante.equals(atual.proximo.nomeEstudante)) {
                if (!atual.nomeEstudante.equals(nomeAnterior)) {
                    System.out.println(atual.nomeEstudante);
                    nomeAnterior = atual.nomeEstudante;
                }
            }
            atual = atual.proximo;
        }
    }

    // ==============================================================
    // MAIN
    // ==============================================================
    public static void main(String[] args) {
        Main sistema = new Main();
        Scanner scanner = new Scanner(System.in);

        // --- PRÉ-CARGA DE DADOS (Conforme PDF pede dados iniciais) ---
        sistema.insereTurma(103);
        sistema.insereTurma(201);
        sistema.insereTurma(305);
        sistema.insereTurma(202);
        sistema.insereTurma(105);

        //Turma 103
        sistema.insereEstudante(103, "Joao");
        sistema.insereEstudante(103, "Ana");
        sistema.insereEstudante(103, "Mauro");
        sistema.insereEstudante(103, "Clarice");
        sistema.insereEstudante(103,"Luiz");
        sistema.insereEstudante(103, "Samuel");
        sistema.insereEstudante(103, "Diego");

        //Turma 201
        sistema.insereEstudante(201, "Carlos");
        sistema.insereEstudante(201, "Maria");
        sistema.insereEstudante(201, "Beatriz");
        sistema.insereEstudante(201, "Lucas");
        sistema.insereEstudante(201, "Vitor");
        sistema.insereEstudante(201, "Denise");

        //Turma 305
        sistema.insereEstudante(305, "Maria");
        sistema.insereEstudante(305, "Eduardo");
        sistema.insereEstudante(305, "Paulo");
        sistema.insereEstudante(305, "Cintia");
        sistema.insereEstudante(305, "Marisa");
        sistema.insereEstudante(305, "Alvaro");
        sistema.insereEstudante(305, "Sandra");

        //Turma 202
        sistema.insereEstudante(202, "Patricia");
        sistema.insereEstudante(202, "Anelise");
        sistema.insereEstudante(202, "Douglas");
        sistema.insereEstudante(202, "Diego");
        sistema.insereEstudante(202, "Marcos");
        sistema.insereEstudante(202, "Vania");

        //Turma 105
        sistema.insereEstudante(105, "Eduardo");
        sistema.insereEstudante(105, "Lucas");
        sistema.insereEstudante(105, "Maria");
        sistema.insereEstudante(105, "Vinicius");
        sistema.insereEstudante(105, "Felipe");
        sistema.insereEstudante(105, "Clara");

        int opcao = 0;
        do {
            System.out.println("\n=== SISTEMA DE TURMAS (ABP) ===");
            System.out.println("1. Inserir Turma");
            System.out.println("2. Inserir Estudante");
            System.out.println("3. Remover Turma");
            System.out.println("4. Remover Estudante");
            System.out.println("5. Exibir Turma (A-Z)");
            System.out.println("6. Exibir Turma (Z-A)");
            System.out.println("7. Exibir Turma (Pré-Ordem)");
            System.out.println("8. Exibir TODOS Estudantes (Global)");
            System.out.println("9. Estatísticas (Contagem/Maior/Menor)");
            System.out.println("10. Nomes Repetidos");
            System.out.println("0. Sair");
            System.out.print("Opção: ");

            try {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Consumir quebra de linha
            } catch (Exception e) {
                System.out.println("Entrada inválida.");
                scanner.nextLine();
                continue;
            }

            switch (opcao) {
                case 1:
                    System.out.print("Código da Turma (3 dígitos): ");
                    sistema.insereTurma(scanner.nextInt());
                    break;
                case 2:
                    System.out.print("Código da Turma: ");
                    int t = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nome do Estudante: ");
                    sistema.insereEstudante(t, scanner.nextLine());
                    break;
                case 3:
                    System.out.print("Código da Turma a remover: ");
                    sistema.removeTurma(scanner.nextInt());
                    break;
                case 4:
                    System.out.print("Código da Turma: ");
                    int tRem = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Nome do Estudante a remover: ");
                    sistema.removeEstudante(tRem, scanner.nextLine());
                    break;
                case 5:
                    System.out.print("Código da Turma: ");
                    sistema.exibeTurma(scanner.nextInt(), "AZ");
                    break;
                case 6:
                    System.out.print("Código da Turma: ");
                    sistema.exibeTurma(scanner.nextInt(), "ZA");
                    break;
                case 7:
                    System.out.print("Código da Turma: ");
                    sistema.exibeTurma(scanner.nextInt(), "PRE");
                    break;
                case 8:
                    sistema.exibeTodosEstudantes();
                    break;
                case 9:
                    sistema.exibirEstatisticas();
                    break;
                case 10:
                    sistema.exibirNomesRepetidos();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);

        scanner.close();
    }
}