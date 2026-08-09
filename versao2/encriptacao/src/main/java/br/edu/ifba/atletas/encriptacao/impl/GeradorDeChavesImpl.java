package br.edu.ifba.atletas.encriptacao.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import br.edu.ifba.atletas.encriptacao.aleatoriedade.GeradorDeAleatoriedadeReal;
import br.edu.ifba.atletas.encriptacao.chaves.GeradorDeChaves;
import br.edu.ifba.atletas.encriptacao.excecoes.FalhaGeracaoDeChaves;

/*
 * Gera um par de chaves RSA usando como fonte de aleatoriedade o
 * GeradorDeAleatoriedadeReal (frames do vídeo do aquário).
 *
 * Intratabilidade: a chave privada RSA de 1024 bits gerada a partir de entropia
 * real não pode ser reproduzida sem o vídeo e o deslocamento exatos usados.
 * Fatorar a chave pública RSA para obter a privada é computacionalmente intratável.
 */
public class GeradorDeChavesImpl implements GeradorDeChaves<GeradorDeAleatoriedadeReal> {

    private static final int TAMANHO_CHAVES_ENCRIPTACAO = 1024;

    private GeradorDeAleatoriedadeReal geradorDeAleatoriedade = null;
    private String algoritmoDeEncriptacao = null;

    /*
     * Complexidade: O(1) — apenas atribuições.
     */
    @Override
    public void inicializar(GeradorDeAleatoriedadeReal geradorDeAleatoriedade,
            String algoritmoDeEncriptacao) {
        this.geradorDeAleatoriedade = geradorDeAleatoriedade;
        this.algoritmoDeEncriptacao = algoritmoDeEncriptacao;
    }

    /*
     * Gera o par de chaves RSA usando a aleatoriedade real do vídeo.
     *
     * Complexidade: O(K) onde K = custo de geração de chave RSA de 1024 bits.
     * K é considerado constante (independe do volume de dados do sistema).
     * Consequências: operação cara mas executada apenas uma vez na inicialização.
     */
    @Override
    public KeyPair gerarChaves() throws FalhaGeracaoDeChaves {
        try {
            KeyPairGenerator geradorDePares =
                KeyPairGenerator.getInstance(algoritmoDeEncriptacao);
            geradorDePares.initialize(TAMANHO_CHAVES_ENCRIPTACAO, geradorDeAleatoriedade);
            return geradorDePares.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new FalhaGeracaoDeChaves("falha gerando chave: " + e.getMessage());
        }
    }

    /*
     * Gera o par de chaves e persiste cada chave no arquivo indicado.
     *
     * Complexidade: O(K) geração + O(B) gravação onde B = bytes das chaves.
     * B é fixo (chaves RSA 1024 bits = 128 bytes), portanto O(1) efetivo.
     */
    @Override
    public KeyPair gerarChaves(String caminhoChavePrivada, String caminhoChavePublica)
            throws FalhaGeracaoDeChaves {
        KeyPair chaves = gerarChaves();
        gravar(caminhoChavePublica,  chaves.getPublic().getEncoded());
        gravar(caminhoChavePrivada, chaves.getPrivate().getEncoded());
        return chaves;
    }

    /*
     * Grava bytes em arquivo, recriando-o se já existir.
     *
     * Complexidade: O(B) onde B = tamanho dos bytes. B fixo → O(1) efetivo.
     */
    private void gravar(String caminho, byte[] bytes) throws FalhaGeracaoDeChaves {
        File f = new File(caminho);
        if (f.exists()) f.delete();

        try (FileOutputStream stream = new FileOutputStream(f)) {
            stream.write(bytes);
        } catch (IOException e) {
            throw new FalhaGeracaoDeChaves("erro gravando chave em arquivo");
        }
    }

    /*
     * Complexidade: O(1) — encerra o gerador de aleatoriedade.
     */
    @Override
    public void finalizar() throws FalhaGeracaoDeChaves {
        geradorDeAleatoriedade.finalizar();
    }

}
