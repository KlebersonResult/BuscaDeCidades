package clientegeodb;

/**
 * Erro ao falar com a API. A mensagem já vem em português e pronta para ser
 * mostrada ao usuário.
 */
public class ErroApi extends Exception {

    public ErroApi(String mensagem) {
        super(mensagem);
    }
}
