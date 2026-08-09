package br.edu.ifba.atletas.servidor.operacoes;

/*
 * Interface das operações do servidor - VERSÃO 2.
 *
 * Diferença fundamental em relação à V1:
 *   - detectarAltasOscilacoes() apenas SOMA os inteiros recebidos dos clientes.
 *   - O cálculo O(N²) foi eliminado do servidor — roda distribuído nos clientes.
 * Complexidades reais definidas em OperacoesImpl.
 */
public interface Operacoes<Monitorado, Leitura> {

    /*
     * Grava uma leitura desencriptada para o monitorado, com fila rotativa.
     * Complexidade esperada: O(1) amortizado — ver OperacoesImpl.
     */
    public void gravar(Monitorado monitorado, Leitura leitura);

    /*
     * Registra o total de pares enviado pelo cliente (não calcula — apenas acumula).
     * Complexidade esperada: O(1) — ver OperacoesImpl.
     */
    public void gravar(Monitorado monitorado, int pares);

    /*
     * Retorna a soma total de pares registrados pelos clientes.
     * Complexidade: O(N) com N=10 fixo → O(1) na prática.
     * Diferença da V1: não executa O(N²) — apenas soma inteiros já calculados.
     */
    public int detectarAltasOscilacoes();

}
