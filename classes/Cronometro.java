package classes;

//Classe lambda que cronometra o tempo de execução de uma operação
public class Cronometro {
    private long tempoInicio;//Armazena o tempo de início em nanossegundos
    private long tempoFim;//Armazena o tempo de fim em nanossegundos
    private boolean rodando;//Verifica se o cronômetro está rodando(Expecifico para debug)

    //inicializa o cronômetro em nanossegundos
    public void iniciar() {
        this.tempoInicio = System.nanoTime();
        this.rodando = true;
    }

    //Para o cronômetro em nanossegundos
    public void parar() {
        this.tempoFim = System.nanoTime();
        this.rodando = false;
    }

    //Calcula a duração em nanossegundos
    public long getDuracaoNanos() {
        if (rodando) {
            return System.nanoTime() - tempoInicio;
        }
        return tempoFim - tempoInicio;
    }

    //Calcula a duração em Segundos
    public double getDuracaoSegundos() {
        return getDuracaoNanos() / 1_000_000_000.0;
    }

    //Mostra o tempo de execução da operação em segundos
    public void mostrarTempo(String operacao) {
        parar();
        System.out.println(operacao + " concluída em:" + String.format("%.3f", getDuracaoSegundos()) + " segundos");
        System.out.println();
    }

    //Método estático que recebe o nome da operação e uma função Runnable para ser executada
    public static double cronometrar(String nome, Runnable operacao) {
        Cronometro cronometro = new Cronometro();
        cronometro.iniciar();
        operacao.run(); // Executa a operação passada como parâmetro
        cronometro.mostrarTempo(nome);
        return cronometro.getDuracaoSegundos();
    }
}
