package clientegeodb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Tela única do cliente: busca cidades por prefixo, lista os resultados
 * e mostra os detalhes da cidade selecionada.
 */
public class TelaPrincipal extends JFrame {

    private static final Locale LOCAL_BR = Locale.forLanguageTag("pt-BR");
    private static final Color COR_ERRO = new Color(0xB0, 0x00, 0x20);   // contraste 7:1 sobre branco
    private static final Color COR_NORMAL = Color.BLACK;
    private static final String[] ROTULOS_DETALHES = {
        "ID", "ID Wikidata", "Nome", "País", "Região", "Latitude", "Longitude", "População"
    };

    private final ClienteGeoDB cliente;

    private final JTextField campoPrefixo = new JTextField(25);
    private final JButton botaoBuscar = new JButton("Buscar");
    private final JButton botaoDetalhes = new JButton("Ver detalhes");
    private final JLabel rotuloStatus = new JLabel();
    private final JLabel[] valoresDetalhes = new JLabel[ROTULOS_DETALHES.length];

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new String[]{"Nome", "Região", "País", "População"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    private List<Cidade> cidadesEncontradas = List.of();
    private boolean ocupado = false;

    public TelaPrincipal(ClienteGeoDB cliente) {
        super("Consulta de cidades - GeoDB Cities");
        this.cliente = cliente;

        montarTela();
        configurarEventos();
        mostrarStatus("Pronto. Digite o início do nome de uma cidade e clique em Buscar.", false);
        atualizarBotoes();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 480));
        pack();
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- Montagem da tela

    private void montarTela() {
        JPanel conteudo = new JPanel(new BorderLayout(12, 12));
        conteudo.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        conteudo.add(montarPainelBusca(), BorderLayout.NORTH);
        conteudo.add(montarPainelResultados(), BorderLayout.CENTER);
        conteudo.add(montarPainelDetalhes(), BorderLayout.EAST);
        conteudo.add(montarBarraStatus(), BorderLayout.SOUTH);

        setContentPane(conteudo);
        getRootPane().setDefaultButton(botaoBuscar); // Enter aciona "Buscar"
    }

    private JPanel montarPainelBusca() {
        JLabel rotulo = new JLabel("Início do nome da cidade:");
        rotulo.setDisplayedMnemonic('N');
        rotulo.setLabelFor(campoPrefixo);

        botaoBuscar.setMnemonic('B');
        campoPrefixo.getAccessibleContext().setAccessibleDescription(
                "Digite o início do nome, por exemplo: Poços de Caldas");

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 8);
        c.gridy = 0;
        painel.add(rotulo, c);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        painel.add(campoPrefixo, c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        painel.add(botaoBuscar, c);
        return painel;
    }

    private JPanel montarPainelResultados() {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(26);
        tabela.setPreferredScrollableViewportSize(new Dimension(520, 300));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getAccessibleContext().setAccessibleName("Resultados da busca");

        botaoDetalhes.setMnemonic('D');

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        rodape.add(botaoDetalhes, BorderLayout.WEST);

        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Resultados"));
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel montarPainelDetalhes() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Detalhes da cidade"));
        painel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(4, 8, 4, 8);

        for (int i = 0; i < ROTULOS_DETALHES.length; i++) {
            JLabel nome = new JLabel(ROTULOS_DETALHES[i] + ":");
            nome.setFont(nome.getFont().deriveFont(Font.BOLD));
            valoresDetalhes[i] = new JLabel("-");

            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            painel.add(nome, c);
            c.gridx = 1;
            c.weightx = 1;
            painel.add(valoresDetalhes[i], c);
        }

        // Empurra as linhas para o topo do painel
        c.gridx = 0;
        c.gridy = ROTULOS_DETALHES.length;
        c.weightx = 0;
        c.weighty = 1;
        painel.add(new JLabel(), c);
        return painel;
    }

    private JPanel montarBarraStatus() {
        rotuloStatus.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 2));
        rotuloStatus.getAccessibleContext().setAccessibleName("Mensagem de status");

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(rotuloStatus, BorderLayout.CENTER);
        return painel;
    }

    // ---------------------------------------------------------------- Eventos

    private void configurarEventos() {
        botaoBuscar.addActionListener(e -> iniciarBusca());
        botaoDetalhes.addActionListener(e -> iniciarBuscaDetalhes());
        tabela.getSelectionModel().addListSelectionListener(e -> atualizarBotoes());

        // Duplo clique em uma linha também mostra os detalhes (atalho para quem usa mouse)
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !ocupado && tabela.getSelectedRow() >= 0) {
                    iniciarBuscaDetalhes();
                }
            }
        });
    }

    private void atualizarBotoes() {
        botaoBuscar.setEnabled(!ocupado);
        campoPrefixo.setEnabled(!ocupado);
        botaoDetalhes.setEnabled(!ocupado && tabela.getSelectedRow() >= 0);
    }

    private void definirOcupado(boolean valor, String mensagem) {
        ocupado = valor;
        atualizarBotoes();
        mostrarStatus(mensagem, false);
    }

    private void mostrarStatus(String mensagem, boolean erro) {
        rotuloStatus.setForeground(erro ? COR_ERRO : COR_NORMAL);
        // O prefixo "Erro:" garante que o erro não dependa apenas da cor
        rotuloStatus.setText(erro ? "Erro: " + mensagem : mensagem);
    }

    // ---------------------------------------------------------------- Chamadas à API (em segundo plano)

    private void iniciarBusca() {
        String prefixo = campoPrefixo.getText().trim();
        if (prefixo.isEmpty()) {
            mostrarStatus("digite o início do nome de uma cidade para buscar.", true);
            campoPrefixo.requestFocusInWindow();
            return;
        }

        definirOcupado(true, "Buscando cidades...");
        new SwingWorker<List<Cidade>, Void>() {
            @Override
            protected List<Cidade> doInBackground() throws Exception {
                return cliente.buscarCidades(prefixo);
            }

            @Override
            protected void done() {
                try {
                    mostrarCidades(get(), prefixo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    tratarFalha(e);
                }
                definirOcupadoSemMensagem();
            }
        }.execute();
    }

    private void iniciarBuscaDetalhes() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            mostrarStatus("selecione uma cidade na lista.", true);
            return;
        }
        long id = cidadesEncontradas.get(linha).id();

        definirOcupado(true, "Buscando detalhes...");
        new SwingWorker<Cidade, Void>() {
            @Override
            protected Cidade doInBackground() throws Exception {
                return cliente.buscarDetalhes(id);
            }

            @Override
            protected void done() {
                try {
                    mostrarDetalhes(get());
                    mostrarStatus("Detalhes carregados.", false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    tratarFalha(e);
                }
                definirOcupadoSemMensagem();
            }
        }.execute();
    }

    /** Reabilita os controles mantendo a mensagem que já está na barra de status. */
    private void definirOcupadoSemMensagem() {
        ocupado = false;
        atualizarBotoes();
    }

    private void tratarFalha(ExecutionException e) {
        if (e.getCause() instanceof ErroApi) {
            mostrarStatus(e.getCause().getMessage(), true);
        } else {
            mostrarStatus("falha inesperada (" + e.getCause() + ").", true);
        }
    }

    // ---------------------------------------------------------------- Exibição dos resultados

    private void mostrarCidades(List<Cidade> cidades, String prefixo) {
        cidadesEncontradas = cidades;
        modeloTabela.setRowCount(0);
        limparDetalhes();

        NumberFormat formato = NumberFormat.getIntegerInstance(LOCAL_BR);
        for (Cidade cidade : cidades) {
            modeloTabela.addRow(new Object[]{
                cidade.nome(), cidade.regiao(), cidade.pais(), formato.format(cidade.populacao())});
        }

        if (cidades.isEmpty()) {
            mostrarStatus("Nenhuma cidade encontrada para \"" + prefixo + "\". Confira a grafia e tente de novo.", false);
        } else {
            tabela.setRowSelectionInterval(0, 0);
            mostrarStatus(cidades.size() + " cidade(s) encontrada(s). Selecione uma e clique em Ver detalhes.", false);
        }
    }

    private void mostrarDetalhes(Cidade cidade) {
        NumberFormat inteiro = NumberFormat.getIntegerInstance(LOCAL_BR);
        String[] valores = {
            String.valueOf(cidade.id()),
            cidade.idWikidata(),
            cidade.nome(),
            cidade.pais(),
            cidade.regiao(),
            String.format(LOCAL_BR, "%.4f", cidade.latitude()),
            String.format(LOCAL_BR, "%.4f", cidade.longitude()),
            inteiro.format(cidade.populacao())
        };
        for (int i = 0; i < valores.length; i++) {
            valoresDetalhes[i].setText(valores[i].isEmpty() ? "-" : valores[i]);
        }
    }

    private void limparDetalhes() {
        for (JLabel valor : valoresDetalhes) {
            valor.setText("-");
        }
    }
}
