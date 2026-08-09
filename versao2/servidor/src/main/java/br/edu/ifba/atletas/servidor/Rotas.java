package br.edu.ifba.atletas.servidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifba.atletas.servidor.impl.Atleta;
import br.edu.ifba.atletas.servidor.impl.Leitura;
import br.edu.ifba.atletas.servidor.impl.OperacoesImpl;
import br.edu.ifba.atletas.servidor.operacoes.Operacoes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/*
 * Versão 2: desencripta com chave privada RSA antes de processar cada requisição.
 * Recebe o total de pares calculado pelo cliente (não executa O(N²)).
 */
@Path("atletas")
public class Rotas {

    private static Operacoes<Atleta, Leitura> operacoes = null;

    private static Operacoes<Atleta, Leitura> getOperacoes() {
        if (operacoes == null) {
            operacoes = new OperacoesImpl();
        }
        return operacoes;
    }

    private static final String INFORMACOES = "serviço de atendimento a atletas, v2.0";
    private static final String ALGORITMO_DE_ENCRIPTACAO = "RSA";

    // Ajuste este caminho para onde o módulo encriptacao gravou a chave privada
    private static final String CAMINHO_CHAVE_PRIVADA =
    "chave/ch_privada.chv";

    private PrivateKey chave = null;

    /*
     * Carrega a chave privada RSA do arquivo (singleton por instância de Rotas).
     *
     * Complexidade: O(B) onde B = bytes do arquivo de chave (fixo) → O(1).
     */
    private PrivateKey getChavePrivada()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (chave == null) {
            File arquivo = new File(CAMINHO_CHAVE_PRIVADA);
            FileInputStream stream = new FileInputStream(arquivo);
            byte[] bytes = stream.readAllBytes();
            stream.close();

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(ALGORITMO_DE_ENCRIPTACAO);
            chave = kf.generatePrivate(spec);
        }
        return chave;
    }

    /*
     * Desencripta os bytes com a chave privada RSA.
     *
     * Complexidade: O(M) onde M = tamanho dos dados. M fixo por leitura → O(1).
     * Consequências: apenas quem possui a chave privada pode ler os dados —
     * garante a intratabilidade para um atacante que intercepte o tráfego.
     */
    private String desencriptar(byte[] encriptado)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
                   InvalidKeyException, InvalidKeySpecException,
                   IOException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ALGORITMO_DE_ENCRIPTACAO);
        cipher.init(Cipher.DECRYPT_MODE, getChavePrivada());
        byte[] desencriptado = cipher.doFinal(encriptado);
        return new String(desencriptado);
    }

    /*
     * Complexidade: O(1)
     */
    @GET
    @Path("/")
    public Response getInformacoes() {
        return Response.ok(INFORMACOES, MediaType.TEXT_PLAIN).build();
    }

    /*
     * Recebe leitura encriptada, desencripta e grava.
     *
     * Complexidade: O(M) desencriptação + O(1) gravação = O(1) efetivo (M fixo).
     * Consequências: apenas leituras que passaram pelo limiar chegam aqui —
     * volume de requisições muito menor que na V1.
     */
    @POST
    @Path("/leituras/{dados}")
    public Response gravarLeitura(@PathParam("dados") String dados) {
        Response resposta = Response.serverError().build();

        System.out.println("dados encriptados recebidos: " + dados);

        try {
            String json = desencriptar(Base64.getUrlDecoder().decode(dados));
            System.out.println("dados desencriptados: " + json);

            ObjectMapper mapeador = new ObjectMapper();
            JsonNode dic = mapeador.readTree(json);

            Atleta atleta = new Atleta(dic.get("id").asText(), "modelo");
            Leitura leitura = new Leitura(
                dic.get("batimentos").asInt(),
                dic.get("passos").asInt()
            );

            getOperacoes().gravar(atleta, leitura);
            resposta = Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resposta;
    }

    /*
     * Recebe o total de pares calculado pelo cliente (encriptado).
     * O servidor NÃO executa O(N²) — recebe apenas o inteiro resultante.
     *
     * Complexidade: O(M) desencriptação + O(1) registro = O(1) efetivo.
     */
    @POST
    @Path("/pares/{dados}")
    public Response gravarPares(@PathParam("dados") String dados) {
        Response resposta = Response.serverError().build();

        System.out.println("pares encriptados recebidos: " + dados);

        try {
            String json = desencriptar(Base64.getUrlDecoder().decode(dados));
            System.out.println("pares desencriptados: " + json);

            ObjectMapper mapeador = new ObjectMapper();
            JsonNode dic = mapeador.readTree(json);

            Atleta atleta = new Atleta(dic.get("id").asText(), "modelo");
            int pares = dic.get("pares").asInt();

            getOperacoes().gravar(atleta, pares);
            resposta = Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resposta;
    }

    /*
     * Retorna a soma total de pares registrados pelos clientes.
     *
     * Complexidade: O(N) com N=10 fixo → O(1) efetivo.
     */
    @GET
    @Path("/pares")
    public Response detectarPares() {
        int pares = getOperacoes().detectarAltasOscilacoes();
        return Response.ok(pares + "", MediaType.TEXT_PLAIN).build();
    }

}
