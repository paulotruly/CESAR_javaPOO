// Importa as bibliotecas necessárias do SWT (para criar interfaces gráficas)
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;

// Classe principal da interface gráfica do sistema de notícias
public class TelaNoticias {

    // Gerenciador responsável por armazenar e manipular as notícias
    private GerenciadorNoticias gerenciador = new GerenciadorNoticias();
    
    // Componentes da interface
    private org.eclipse.swt.widgets.List listNoticias; // Lista visual das notícias
    private Label lblTotal; // Mostra o total de notícias cadastradas
    private Text txtBusca; // Campo de busca de texto
    private Combo comboBuscaCategoria; // Menu suspenso para filtrar por categoria

    // Construtor: define toda a interface
    public TelaNoticias(Display display) {
        // Cria a janela principal (Shell)
        Shell shell = new Shell(display);
        shell.setText("Gerenciador de notícias");
        shell.setSize(650, 450);
        shell.setLayout(new GridLayout(1, false)); // Layout em grade, 1 coluna

        // ---------- PAINEL DE BUSCA ----------
        Composite searchPanel = new Composite(shell, SWT.NONE);
        searchPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        searchPanel.setLayout(new GridLayout(3, false)); // 3 colunas: campo, combo e botão

        // Campo de texto para busca
        txtBusca = new Text(searchPanel, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
        txtBusca.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // ComboBox com categorias disponíveis
        comboBuscaCategoria = new Combo(searchPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboBuscaCategoria.setItems(new String[]{"Todas", "Tecnologia", "Esporte", "Política", "Saúde", "Economia"});
        comboBuscaCategoria.select(0); // Seleciona “Todas” por padrão

        // Botão de buscar
        Button btnBuscar = new Button(searchPanel, SWT.PUSH);
        btnBuscar.setText("Buscar");

        // ---------- LISTA DE NOTÍCIAS ----------
        listNoticias = new org.eclipse.swt.widgets.List(shell, SWT.BORDER | SWT.V_SCROLL);
        listNoticias.setLayoutData(new GridData(GridData.FILL_BOTH)); // Ocupa todo o espaço restante

        // ---------- PAINEL DE BOTÕES ----------
        Composite panelButtons = new Composite(shell, SWT.NONE);
        panelButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        panelButtons.setLayout(new GridLayout(4, true)); // 4 botões lado a lado

        Button btnAdicionar = new Button(panelButtons, SWT.PUSH);
        btnAdicionar.setText("Adicionar");

        Button btnEditar = new Button(panelButtons, SWT.PUSH);
        btnEditar.setText("Editar");

        Button btnExcluir = new Button(panelButtons, SWT.PUSH);
        btnExcluir.setText("Excluir");

        Button btnLimpar = new Button(panelButtons, SWT.PUSH);
        btnLimpar.setText("Limpar");

        // ---------- LABEL DE TOTAL DE NOTÍCIAS ----------
        lblTotal = new Label(shell, SWT.BORDER);
        lblTotal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        atualizarLista(); // Exibe as notícias iniciais

        // ---------- EVENTOS DOS BOTÕES ----------

        // Abre diálogo para adicionar notícia
        btnAdicionar.addListener(SWT.Selection, e -> abrirDialog(shell, null));

        // Editar notícia selecionada
        btnEditar.addListener(SWT.Selection, e -> {
            int index = listNoticias.getSelectionIndex();
            if (index >= 0)
                abrirDialog(shell, gerenciador.getNoticias().get(index));
            else
                alerta(shell, "Selecione uma notícia para editar.");
        });

        // Excluir notícia selecionada (se não for fixa)
        btnExcluir.addListener(SWT.Selection, e -> {
            int index = listNoticias.getSelectionIndex();
            if (index >= 0) {
                Noticia n = gerenciador.getNoticias().get(index);
                if (n.isFixa())
                    alerta(shell, "Notícia fixa não pode ser excluída.");
                else {
                    gerenciador.remover(index);
                    atualizarLista();
                }
            } else alerta(shell, "Selecione uma notícia para excluir.");
        });

        // Limpa todas as notícias não fixas
        btnLimpar.addListener(SWT.Selection, e -> {
            gerenciador.removerNaoFixas();
            atualizarLista();
        });

        // Busca notícias pelo termo e categoria
        btnBuscar.addListener(SWT.Selection, e -> {
            String termo = txtBusca.getText().trim();
            String categoriaFiltro = comboBuscaCategoria.getText();
            atualizarListaFiltrada(termo, categoriaFiltro);
        });

        // ---------- LOOP PRINCIPAL ----------
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    // ---------- JANELA DE ADIÇÃO/EDIÇÃO DE NOTÍCIA ----------
    private void abrirDialog(Shell parent, Noticia noticia) {
        Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText(noticia == null ? "Adicionar notícia" : "Editar notícia");
        dialog.setLayout(new GridLayout(2, false));
        dialog.setSize(400, 220);

        // Campo de categoria
        new Label(dialog, SWT.NONE).setText("Categoria:");
        Combo comboCategoria = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboCategoria.setItems(new String[]{"Tecnologia", "Esporte", "Política", "Saúde", "Economia"});
        comboCategoria.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Campo de texto para o conteúdo da notícia
        new Label(dialog, SWT.NONE).setText("Notícia:");
        Text txtNoticia = new Text(dialog, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData gdNoticia = new GridData(GridData.FILL_BOTH);
        gdNoticia.horizontalSpan = 2;
        gdNoticia.heightHint = 80;
        txtNoticia.setLayoutData(gdNoticia);

        // Se for edição, preenche os campos
        if (noticia != null) {
            comboCategoria.setText(noticia.getCategoria());
            txtNoticia.setText(noticia.getConteudo());
        }

        // Botão salvar
        Button btnSalvar = new Button(dialog, SWT.PUSH);
        btnSalvar.setText("Salvar");
        GridData gdBtn = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1);
        btnSalvar.setLayoutData(gdBtn);

        // Evento do botão salvar
        btnSalvar.addListener(SWT.Selection, e -> {
            String categoria = comboCategoria.getText().trim();
            String conteudo = txtNoticia.getText().trim();

            if (!categoria.isEmpty() && !conteudo.isEmpty()) {
                if (noticia == null)
                    gerenciador.adicionar(new Noticia(categoria, conteudo, false));
                else {
                    noticia.setCategoria(categoria);
                    noticia.setConteudo(conteudo);
                }
                atualizarLista();
                dialog.close();
            } else alerta(dialog, "Preencha todos os campos!");
        });

        dialog.open();
    }

    // Atualiza a lista completa de notícias
    private void atualizarLista() {
        listNoticias.removeAll();
        for (Noticia n : gerenciador.getNoticias()) {
            listNoticias.add("[" + n.getCategoria() + "] " + n.getConteudo() + (n.isFixa() ? " (fixa)" : ""));
        }
        lblTotal.setText("Total de notícias: " + gerenciador.getNoticias().size());
    }

    // Atualiza a lista com base na busca e filtro
    private void atualizarListaFiltrada(String termo, String categoriaFiltro) {
        listNoticias.removeAll();
        for (Noticia n : gerenciador.getNoticias()) {
            boolean matchConteudo = n.getConteudo().toLowerCase().contains(termo.toLowerCase());
            boolean matchCategoria = categoriaFiltro.equals("Todas") || n.getCategoria().equals(categoriaFiltro);

            if (matchConteudo && matchCategoria) {
                listNoticias.add("[" + n.getCategoria() + "] " + n.getConteudo() + (n.isFixa() ? " (fixa)" : ""));
            }
        }
        lblTotal.setText("Total de notícias: " + listNoticias.getItemCount());
    }

    // Exibe uma caixa de alerta
    private void alerta(Shell parent, String msg) {
        MessageBox box = new MessageBox(parent, SWT.ICON_WARNING | SWT.OK);
        box.setMessage(msg);
        box.open();
    }

    // Método principal que inicia a aplicação
    public static void main(String[] args) {
        Display display = new Display();
        new TelaNoticias(display);
        display.dispose();
    }
}

// ---------- CLASSE Noticia ----------
// Representa uma notícia com categoria, conteúdo e se é fixa ou não
class Noticia {
    private String categoria;
    private String conteudo;
    private boolean fixa;

    public Noticia(String categoria, String conteudo, boolean fixa) {
        this.categoria = categoria;
        this.conteudo = conteudo;
        this.fixa = fixa;
    }

    // Getters e setters
    public String getCategoria() { return categoria; }
    public String getConteudo() { return conteudo; }
    public boolean isFixa() { return fixa; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
}

// ---------- CLASSE GerenciadorNoticias ----------
// Controla a lista de notícias (CRUD básico)
class GerenciadorNoticias {
    private java.util.List<Noticia> noticias = new ArrayList<>();

    // Adiciona algumas notícias fixas iniciais
    public GerenciadorNoticias() {
        adicionar(new Noticia("Tecnologia", "IA revoluciona mercado de tecnologia", true));
        adicionar(new Noticia("Esporte", "Brasil vence Copa do Mundo", true));
        adicionar(new Noticia("Política", "Nova lei ambiental é aprovada", true));
        adicionar(new Noticia("Saúde", "Descoberta cura para doença rara", true));
        adicionar(new Noticia("Economia", "Economia cresce 5% no trimestre", true));
    }

    public void adicionar(Noticia n) { noticias.add(n); }
    public void remover(int index) { noticias.remove(index); }
    public void removerNaoFixas() { noticias.removeIf(n -> !n.isFixa()); }
    public java.util.List<Noticia> getNoticias() { return noticias; }
}