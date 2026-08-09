package br.edu.ifba.atletas.servidor.operacoes;

/*
 * Interface das operações do servidor - VERSÃO 1.
 *
 * Define o contrato de armazenamento e processamento de leituras.
 * Na V1, detectarParesSimilares() é executado no SERVIDOR em O(N²).
 * Todos os métodos têm complexidade definida em OperacoesImpl.
 */
public interface Operacoes<Monitorado, Leitura> {

    /*
     * Grava uma leitura para o monitorado.
     * Complexidade esperada: O(1) amortizado — ver OperacoesImpl.
     */
    public void gravar(Monitorado monitorado, Leitura leitura);

    /*
     * Grava o total de pares para o monitorado.
     * Complexidade esperada: O(1) — ver OperacoesImpl.
     */
    public void gravar(Monitorado monitorado, int pares);

    /*
     * Detecta pares de leituras com desempenho similar.
     * Complexidade: O(N²) — executado no SERVIDOR na V1.
     * Principal gargalo de complexidade desta versão.
     */
    public int detectarParesSimilares();

}
