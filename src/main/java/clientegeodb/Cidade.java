package clientegeodb;

/**
 * Dados de uma cidade devolvidos pela API GeoDB Cities.
 */
public record Cidade(
        long id,
        String idWikidata,
        String nome,
        String pais,
        String regiao,
        double latitude,
        double longitude,
        long populacao) {
}
