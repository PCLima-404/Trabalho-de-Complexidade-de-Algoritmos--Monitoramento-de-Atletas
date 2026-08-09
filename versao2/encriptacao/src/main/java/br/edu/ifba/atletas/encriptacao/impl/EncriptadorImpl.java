package br.edu.ifba.atletas.encriptacao.impl;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import br.edu.ifba.atletas.encriptacao.encriptador.Encriptador;
import br.edu.ifba.atletas.encriptacao.excecoes.FalhaEncriptacao;

/*
 * Encripta com a chave pública RSA e desencripta com a chave privada RSA.
 *
 * Intratabilidade: dado apenas o texto cifrado e a chave pública, recuperar
 * o texto original exige fatorar o módulo RSA de 1024 bits — problema
 * computacionalmente intratável com hardware atual.
 */
public class EncriptadorImpl extends Encriptador {

    public EncriptadorImpl(KeyPair chaves, String algoritmoDeEncriptacao) {
        super(chaves, algoritmoDeEncriptacao);
    }

    /*
     * Encripta os dados com a chave pública RSA.
     * Resultado codificado em Base64 URL-safe para tráfego via URL.
     *
     * Complexidade: O(M) onde M = tamanho dos dados em bytes.
     * Razão: a cifra RSA processa M bytes em blocos; para M pequeno e fixo
     * (uma leitura de sensor), o custo é O(1) na prática.
     * Consequências: RSA com chave de 1024 bits suporta até 117 bytes por bloco;
     * mensagens maiores exigiriam múltiplos blocos ou encriptação híbrida.
     */
    @Override
    public String encriptar(String dados) throws FalhaEncriptacao {
        String encriptacao = "";
        synchronized (encriptacao) {
            try {
                Cipher cifrador = Cipher.getInstance(algoritmoDeEncriptacao);
                cifrador.init(Cipher.ENCRYPT_MODE, chaves.getPublic());
                byte[] cifragem = cifrador.doFinal(dados.getBytes(StandardCharsets.UTF_8));
                encriptacao = Base64.getUrlEncoder().encodeToString(cifragem);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException
                   | InvalidKeyException | IllegalBlockSizeException
                   | BadPaddingException e) {
                throw new FalhaEncriptacao("falha encriptando dados: " + e.getMessage());
            }
        }
        return encriptacao;
    }

    /*
     * Desencripta os dados com a chave privada RSA.
     *
     * Complexidade: O(M) — mesmo raciocínio do encriptar().
     * Na prática O(1) para mensagens de tamanho fixo (leituras de sensor).
     */
    @Override
    public String desencriptar(String encriptacao) throws FalhaEncriptacao {
        try {
            Cipher cifrador = Cipher.getInstance(algoritmoDeEncriptacao);
            cifrador.init(Cipher.DECRYPT_MODE, chaves.getPrivate());
            byte[] bytes = Base64.getUrlDecoder().decode(encriptacao);
            byte[] bytesDecriptados = cifrador.doFinal(bytes);
            return new String(bytesDecriptados, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
               | InvalidKeyException | IllegalBlockSizeException
               | BadPaddingException e) {
            throw new FalhaEncriptacao("falha desencriptando dados: " + e.getMessage());
        }
    }

}
