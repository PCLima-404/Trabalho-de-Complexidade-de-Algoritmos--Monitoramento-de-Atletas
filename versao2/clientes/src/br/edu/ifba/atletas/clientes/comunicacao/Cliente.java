package br.edu.ifba.atletas.clientes.comunicacao;

import br.edu.ifba.atletas.clientes.sensoriamento.Sensoriamento;

/*
 * Interface do cliente - VERSÃO 2.
 *
 * Define o contrato de comunicação com o servidor otimizado.
 * Na V2, enviar(Leitura) transmite dados encriptados com RSA,
 * e enviar(int) envia o resultado do O(N²) calculado no cliente.
 * Complexidades reais definidas em ClienteImpl.
 */
public interface Cliente<Monitorado, Leitura> {

    /*
     * Configura o cliente com o atleta, sensoriamento e carrega a chave pública RSA.
     * Complexidade esperada: O(B) onde B = bytes da chave (fixo → O(1)).
     */
    public void configurar(Monitorado monitorado, Sensoriamento<Leitura> sensoriamento)
            throws Exception;

    /*
     * Verifica se a variação entre duas leituras ultrapassa os limiares.
     * Complexidade esperada: O(1) — operações aritméticas simples.
     */
    public boolean ocorreuAltaOscilacao(Leitura leituraAtual, Leitura ultimaLeitura,
            int limiarOscilacaoBatimentos, int limiarOscilacaoPassos);

    /*
     * Encripta e envia uma leitura ao servidor (só chamado após passar pelo limiar).
     * Complexidade esperada: O(M) encriptação + O(1) HTTP (M fixo → O(1)).
     */
    public Resultado enviar(Leitura leitura) throws Exception;

    /*
     * Encripta e envia o total de pares calculado pelo cliente.
     * O servidor NÃO executa O(N²) — recebe apenas este inteiro.
     * Complexidade esperada: O(1) por chamada.
     */
    public Resultado enviar(int pares) throws Exception;

}
