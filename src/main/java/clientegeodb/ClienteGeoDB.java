package clientegeodb;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cliente da API REST GeoDB Cities (publicada no hub RapidAPI).
 * Faz duas chamadas GET: buscar cidades por prefixo e buscar os detalhes de uma cidade.
 */
public class ClienteGeoDB {

    private static final String HOST = "wft-geo-db.p.rapidapi.com";
    private static final String URL_BASE = "https://" + HOST + "/v1/geo/cities";

    private final HttpClient clienteHttp = HttpClient.newHttpClient();
    private final String chaveApi;
    private final String urlBase;

    public ClienteGeoDB(String chaveApi) {
        this(chaveApi, URL_BASE);
    }

    /** Permite trocar a URL base (usado apenas em testes locais). */
    ClienteGeoDB(String chaveApi, String urlBase) {
        this.chaveApi = chaveApi;
        this.urlBase = urlBase;
    }

    /**
     * GET /v1/geo/cities?namePrefix=...  - lista até 10 cidades, das mais populosas para as menos.
     */
    public List<Cidade> buscarCidades(String prefixo) throws ErroApi {
        String prefixoCodificado = URLEncoder.encode(prefixo, StandardCharsets.UTF_8).replace("+", "%20");
        String url = urlBase + "?namePrefix=" + prefixoCodificado + "&limit=10&sort=-population";

        try {
            JSONObject corpo = new JSONObject(enviarGet(url));
            JSONArray dados = corpo.getJSONArray("data");
            List<Cidade> cidades = new ArrayList<>();
            for (int i = 0; i < dados.length(); i++) {
                cidades.add(converterCidade(dados.getJSONObject(i)));
            }
            return cidades;
        } catch (JSONException e) {
            throw new ErroApi("A API respondeu em um formato inesperado.");
        }
    }

    /**
     * GET /v1/geo/cities/{id}  - devolve os detalhes de uma cidade.
     */
    public Cidade buscarDetalhes(long id) throws ErroApi {
        String url = urlBase + "/" + id;

        try {
            JSONObject corpo = new JSONObject(enviarGet(url));
            return converterCidade(corpo.getJSONObject("data"));
        } catch (JSONException e) {
            throw new ErroApi("A API respondeu em um formato inesperado.");
        }
    }

    /** Envia a requisição GET com os cabeçalhos de autenticação e devolve o corpo da resposta. */
    private String enviarGet(String url) throws ErroApi {
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("X-RapidAPI-Key", chaveApi)
                .header("X-RapidAPI-Host", HOST)
                .GET()
                .build();

        HttpResponse<String> resposta;
        try {
            resposta = clienteHttp.send(requisicao, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ErroApi("Não foi possível conectar à API. Verifique sua conexão com a internet e tente novamente.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErroApi("A consulta foi interrompida.");
        }

        return switch (resposta.statusCode()) {
            case 200 -> resposta.body();
            case 401, 403 -> throw new ErroApi("Chave recusada pela API. Confira se a chave está correta e se você "
                    + "assinou a API GeoDB Cities no RapidAPI.");
            case 404 -> throw new ErroApi("Cidade não encontrada.");
            case 429 -> throw new ErroApi("Muitas consultas em pouco tempo. Aguarde um instante e tente de novo.");
            default -> throw new ErroApi("A API respondeu com erro (código " + resposta.statusCode() + ").");
        };
    }

    /** Converte o objeto JSON de uma cidade para a classe Cidade. */
    private Cidade converterCidade(JSONObject json) {
        return new Cidade(
                json.getLong("id"),
                json.optString("wikiDataId", ""),
                json.optString("name", ""),
                json.optString("country", ""),
                json.optString("region", ""),
                json.optDouble("latitude", 0),
                json.optDouble("longitude", 0),
                json.optLong("population", 0));
    }
}
