package clientegeodb;

import java.io.IOException;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Ponto de entrada do programa.
 */
public class Principal {

    private static final float TAMANHO_FONTE = 15f;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Principal::iniciar);
    }

    private static void iniciar() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Sem o visual do sistema, o visual padrão do Java continua funcionando
        }
        aumentarFonte();

        try {
            Configuracao configuracao = Configuracao.carregar();
            new TelaPrincipal(new ClienteGeoDB(configuracao.getChaveApi())).setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Configuração necessária", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /** Aumenta a fonte de todos os componentes para facilitar a leitura. */
    private static void aumentarFonte() {
        for (Object chave : Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(chave) instanceof FontUIResource fonte) {
                UIManager.put(chave, new FontUIResource(fonte.deriveFont(TAMANHO_FONTE)));
            }
        }
    }
}
