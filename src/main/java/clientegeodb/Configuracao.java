package clientegeodb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lê a chave de acesso da API no arquivo config.properties.
 * A chave fica fora do código (e fora do Git) para não ser exposta.
 */
public class Configuracao {

    private static final String ARQUIVO = "config.properties";
    private static final String CHAVE_PADRAO = "COLE_SUA_CHAVE_AQUI";

    private final String chaveApi;

    private Configuracao(String chaveApi) {
        this.chaveApi = chaveApi;
    }

    public String getChaveApi() {
        return chaveApi;
    }

    /**
     * Carrega o arquivo config.properties da pasta onde o programa foi iniciado.
     *
     * @throws IOException se o arquivo não existir ou a chave não estiver preenchida
     */
    public static Configuracao carregar() throws IOException {
        Path caminho = Path.of(ARQUIVO).toAbsolutePath();
        if (!Files.exists(caminho)) {
            throw new IOException("O arquivo " + ARQUIVO + " não foi encontrado em:\n"
                    + caminho.getParent() + "\n\n"
                    + "Copie o arquivo config.exemplo.properties para " + ARQUIVO
                    + " e coloque sua chave do RapidAPI.");
        }

        Properties propriedades = new Properties();
        try (InputStream entrada = Files.newInputStream(caminho)) {
            propriedades.load(entrada);
        }

        String chave = propriedades.getProperty("chave.api", "").trim();
        if (chave.isEmpty() || chave.equals(CHAVE_PADRAO)) {
            throw new IOException("A chave da API ainda não foi preenchida.\n\n"
                    + "Abra o arquivo " + caminho + " e informe sua chave em 'chave.api'.");
        }
        return new Configuracao(chave);
    }
}
