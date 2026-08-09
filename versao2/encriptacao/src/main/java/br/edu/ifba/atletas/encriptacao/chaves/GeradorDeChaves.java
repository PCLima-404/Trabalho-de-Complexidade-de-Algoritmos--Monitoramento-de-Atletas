package br.edu.ifba.atletas.encriptacao.chaves;

import java.security.KeyPair;
import java.security.SecureRandom;

import br.edu.ifba.atletas.encriptacao.excecoes.FalhaGeracaoDeChaves;

/*
 * Interface do gerador de chaves criptográficas.
 *
 * Parametrizada com GeradorDeAleatoriedade — permite injetar qualquer fonte
 * de entropia que estenda SecureRandom. Na V2, usa GeradorDeAleatoriedadeReal
 * (frames do vídeo do aquário) para garantir chaves irreproduzíveis.
 *
 * Complexidades reais definidas em GeradorDeChavesImpl.
 */
public interface GeradorDeChaves<GeradorDeAleatoriedade extends SecureRandom> {

    /*
     * Inicializa o gerador com a fonte de entropia e o algoritmo escolhido.
     * Complexidade esperada: O(1) — apenas atribuições.
     */
    public void inicializar(GeradorDeAleatoriedade geradorDeAleatoriedade,
            String algoritmoDeEncriptacao);

    /*
     * Gera um par de chaves usando a fonte de aleatoriedade real.
     * Complexidade: O(K) onde K = custo de geração RSA (fixo → O(1) prático).
     * Intratabilidade: chave privada não pode ser derivada da pública sem
     * fatorar o módulo RSA — problema computacionalmente intratável.
     */
    public KeyPair gerarChaves() throws FalhaGeracaoDeChaves;

    /*
     * Gera e persiste o par de chaves nos arquivos indicados.
     * Complexidade: O(K) geração + O(B) gravação (B = bytes das chaves, fixo).
     */
    public KeyPair gerarChaves(String caminhoChavePrivada, String caminhoChavePublica)
            throws FalhaGeracaoDeChaves;

    /*
     * Encerra o gerador de aleatoriedade e libera recursos (ex: fechar vídeo).
     * Complexidade esperada: O(1).
     */
    public void finalizar() throws FalhaGeracaoDeChaves;

}
