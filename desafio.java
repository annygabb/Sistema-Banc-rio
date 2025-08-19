import java.time.LocalDateTime;
import java.util.*;

// Simulação de Lombok
class Cliente {
    private String nome;
    private String cpf;

    public Cliente(String nome, String cpf) {
        this.nome = nome;
        this.cpf = cpf;
    }

    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
}

enum TipoInvestimento {
    RENDA_FIXA, RENDA_VARIAVEL
}

record Transacao(String tipo, double valor, LocalDateTime dataHora) {
    public Transacao(String tipo, double valor) {
        this(tipo, valor, LocalDateTime.now());
    }
}

abstract class Conta {
    protected String numero;
    protected Cliente titular;
    protected double saldo;
    protected List<Transacao> historico = new ArrayList<>();

    public Conta(String numero, Cliente titular) {
        this.numero = numero;
        this.titular = titular;
        this.saldo = 0.0;
    }

    public String getNumero() { return numero; }
    public Cliente getTitular() { return titular; }
    public double getSaldo() { return saldo; }
    public List<Transacao> getHistorico() { return historico; }

    public void depositar(double valor) {
        saldo += valor;
        historico.add(new Transacao("Depósito", valor));
    }

    public void sacar(double valor) {
        if (valor <= saldo) {
            saldo -= valor;
            historico.add(new Transacao("Saque", valor));
        } else {
            System.out.println("Saldo insuficiente.");
        }
    }

    public void transferir(Conta destino, double valor) {
        if (valor <= saldo) {
            this.sacar(valor);
            destino.depositar(valor);
            historico.add(new Transacao("Transferência PIX para " + destino.getNumero(), valor));
        } else {
            System.out.println("Saldo insuficiente para transferência.");
        }
    }

    public abstract void aplicarInvestimento(TipoInvestimento tipo, double valor);
}

class ContaCorrente extends Conta {
    public ContaCorrente(String numero, Cliente titular) {
        super(numero, titular);
    }

    @Override
    public void aplicarInvestimento(TipoInvestimento tipo, double valor) {
        if (valor <= saldo) {
            saldo -= valor;
            historico.add(new Transacao("Investimento em " + tipo, valor));
        } else {
            System.out.println("Saldo insuficiente para investimento.");
        }
    }
}

class ContaRepository {
    private Map<String, Conta> contas = new HashMap<>();

    public void salvar(Conta conta) {
        contas.put(conta.getNumero(), conta);
    }

    public Conta buscar(String numero) {
        return contas.get(numero);
    }

    public Collection<Conta> listarTodas() {
        return contas.values();
    }
}

public class SistemaBancario {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ContaRepository repo = new ContaRepository();

        while (true) {
            System.out.println("\n=== MENU BANCÁRIO ===");
            System.out.println("1 - Criar Conta");
            System.out.println("2 - Depositar");
            System.out.println("3 - Sacar");
            System.out.println("4 - Transferir via PIX");
            System.out.println("5 - Investir");
            System.out.println("6 - Ver Histórico");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1 -> {
                    System.out.print("Nome do cliente: ");
                    String nome = scanner.nextLine();
                    System.out.print("CPF: ");
                    String cpf = scanner.nextLine();
                    System.out.print("Número da conta: ");
                    String numero = scanner.nextLine();

                    Cliente cliente = new Cliente(nome, cpf);
                    Conta conta = new ContaCorrente(numero, cliente);
                    repo.salvar(conta);
                    System.out.println("Conta criada com sucesso!");
                }
                case 2 -> {
                    System.out.print("Número da conta: ");
                    String numero = scanner.nextLine();
                    Conta conta = repo.buscar(numero);
                    if (conta != null) {
                        System.out.print("Valor do depósito: ");
                        double valor = scanner.nextDouble();
                        conta.depositar(valor);
                        System.out.println("Depósito realizado.");
                    } else {
                        System.out.println("Conta não encontrada.");
                    }
                }
                case 3 -> {
                    System.out.print("Número da conta: ");
                    String numero = scanner.nextLine();
                    Conta conta = repo.buscar(numero);
                    if (conta != null) {
                        System.out.print("Valor do saque: ");
                        double valor = scanner.nextDouble();
                        conta.sacar(valor);
                    } else {
                        System.out.println("Conta não encontrada.");
                    }
                }
                case 4 -> {
                    System.out.print("Conta origem: ");
                    String origem = scanner.nextLine();
                    System.out.print("Conta destino: ");
                    String destino = scanner.nextLine();
                    Conta contaOrigem = repo.buscar(origem);
                    Conta contaDestino = repo.buscar(destino);
                    if (contaOrigem != null && contaDestino != null) {
                        System.out.print("Valor da transferência: ");
                        double valor = scanner.nextDouble();
                        contaOrigem.transferir(contaDestino, valor);
                    } else {
                        System.out.println("Conta(s) não encontrada(s).");
                    }
                }
                case 5 -> {
                    System.out.print("Número da conta: ");
                    String numero = scanner.nextLine();
                    Conta conta = repo.buscar(numero);
                    if (conta != null) {
                        System.out.print("Tipo de investimento (1 - RENDA_FIXA, 2 - RENDA_VARIAVEL): ");
                        int tipo = scanner.nextInt();
                        TipoInvestimento investimento = (tipo == 1) ? TipoInvestimento.RENDA_FIXA : TipoInvestimento.RENDA_VARIAVEL;
                        System.out.print("Valor do investimento: ");
                        double valor = scanner.nextDouble();
                        conta.aplicarInvestimento(investimento, valor);
                    } else {
                        System.out.println("Conta não encontrada.");
                    }
                }
                case 6 -> {
                    System.out.print("Número da conta: ");
                    String numero = scanner.nextLine();
                    Conta conta = repo.buscar(numero);
                    if (conta != null) {
                        System.out.println("Histórico de transações:");
                        for (Transacao t : conta.getHistorico()) {
                            System.out.println(t.tipo() + " de R$" + t.valor() + " em " + t.dataHora());
                        }
                    } else {
                        System.out.println("Conta não encontrada.");
                    }
                }
                case 0 -> {
                    System.out.println("Encerrando...");
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }
}

