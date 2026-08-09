package br.edu.ifba.atletas.clientes.comunicacao;

import br.edu.ifba.atletas.clientes.sensoriamento.Sensoriamento;

/*
 * Interface do cliente — define o contrato de comunicação com o servidor.
 *
 * Todos os métodos têm complexidade O(1) enquanto contrato (assinatura),
 * mas a implementação pode variar — ver ClienteImpl para as complexidades reais.
 */
public interface Cliente<Monitorado, Leitura> {

    /*
     * Configura o cliente com o atleta e o sensoriamento associado.
     * Complexidade esperada: O(1) — atribuição de referências.
     */
    public void configurar(Monitorado monitorado, Sensoriamento<Leitura> sensoriamento)
            throws Exception;

    /*
     * Verifica se a variação entre duas leituras ultrapassa limiares.
     * Complexidade esperada: O(1) — operações aritméticas simples.
     */
    public boolean ocorreuAltaOscilacao(Leitura leituraAtual, Leitura ultimaLeitura,
            int limiarOscilacaoBatimentos, int limiarOscilacaoPassos);

    /*
     * Envia uma leitura ao servidor.
     * Complexidade esperada: O(1) por chamada — uma requisição HTTP.
     */
    public Resultado enviar(Leitura leitura) throws Exception;

    /*
     * Envia o total de pares similares ao servidor.
     * Complexidade esperada: O(1) — uma requisição HTTP com um inteiro.
     */
    public Resultado enviar(int pares) throws Exception;

}
