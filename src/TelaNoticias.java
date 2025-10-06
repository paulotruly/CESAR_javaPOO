import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;

public class TelaNoticias {

    private GerenciadorNoticias gerenciador = new GerenciadorNoticias();
    private org.eclipse.swt.widgets.List listNoticias;
    private Label lblTotal;
    private Text txtBusca;
    private Combo comboBuscaCategoria;

    public TelaNoticias(Display display) {
        Shell shell = new Shell(display);
        shell.setText("Gerenciador de notícias");
        shell.setSize(650, 450);
        shell.setLayout(new GridLayout(1, false));

        // Painel de busca
        Composite searchPanel = new Composite(shell, SWT.NONE);
        searchPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        searchPanel.setLayout(new GridLayout(3, false));

        txtBusca = new Text(searchPanel, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
        txtBusca.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        comboBuscaCategoria = new Combo(searchPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboBuscaCategoria.setItems(new String[]{"Todas", "Tecnologia", "Esporte", "Política", "Saúde", "Economia"});
        comboBuscaCategoria.select(0); // "Todas" selecionada por padrão

        Button btnBuscar = new Button(searchPanel, SWT.PUSH);
        btnBuscar.setText("Buscar");

        // Lista de notícias
        listNoticias = new org.eclipse.swt.widgets.List(shell, SWT.BORDER | SWT.V_SCROLL);
        listNoticias.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Painel de botões
        Composite panelButtons = new Composite(shell, SWT.NONE);
        panelButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        panelButtons.setLayout(new GridLayout(4, true));

        Button btnAdicionar = new Button(panelButtons, SWT.PUSH);
        btnAdicionar.setText("Adicionar");

        Button btnEditar = new Button(panelButtons, SWT.PUSH);
        btnEditar.setText("Editar");

        Button btnExcluir = new Button(panelButtons, SWT.PUSH);
        btnExcluir.setText("Excluir");

        Button btnLimpar = new Button(panelButtons, SWT.PUSH);
        btnLimpar.setText("Limpar");

        // Label de total
        lblTotal = new Label(shell, SWT.BORDER);
        lblTotal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        atualizarLista();

        // Eventos
        btnAdicionar.addListener(SWT.Selection, e -> abrirDialog(shell, null));
        btnEditar.addListener(SWT.Selection, e -> {
            int index = listNoticias.getSelectionIndex();
            if (index >= 0) abrirDialog(shell, gerenciador.getNoticias().get(index));
            else alerta(shell, "Selecione uma notícia para editar.");
        });
        btnExcluir.addListener(SWT.Selection, e -> {
            int index = listNoticias.getSelectionIndex();
            if (index >= 0) {
                Noticia n = gerenciador.getNoticias().get(index);
                if (n.isFixa()) alerta(shell, "Notícia fixa não pode ser excluída.");
                else {
                    gerenciador.remover(index);
                    atualizarLista();
                }
            } else alerta(shell, "Selecione uma notícia para excluir.");
        });
        btnLimpar.addListener(SWT.Selection, e -> {
            gerenciador.removerNaoFixas();
            atualizarLista();
        });

        btnBuscar.addListener(SWT.Selection, e -> {
            String termo = txtBusca.getText().trim();
            String categoriaFiltro = comboBuscaCategoria.getText();
            atualizarListaFiltrada(termo, categoriaFiltro);
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    // Dialog para adicionar/editar
    private void abrirDialog(Shell parent, Noticia noticia) {
        Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText(noticia == null ? "Adicionar notícia" : "Editar notícia");
        dialog.setLayout(new GridLayout(2, false));
        dialog.setSize(400, 220);

        // Combo de categoria
        new Label(dialog, SWT.NONE).setText("Categoria:");
        Combo comboCategoria = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboCategoria.setItems(new String[]{"Tecnologia", "Esporte", "Política", "Saúde", "Economia"});
        comboCategoria.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Campo notícia
        new Label(dialog, SWT.NONE).setText("Notícia:");
        Text txtNoticia = new Text(dialog, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData gdNoticia = new GridData(GridData.FILL_BOTH);
        gdNoticia.horizontalSpan = 2;
        gdNoticia.heightHint = 80;
        txtNoticia.setLayoutData(gdNoticia);

        if (noticia != null) {
            comboCategoria.setText(noticia.getCategoria());
            txtNoticia.setText(noticia.getConteudo());
        }

        Button btnSalvar = new Button(dialog, SWT.PUSH);
        btnSalvar.setText("Salvar");
        GridData gdBtn = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1);
        btnSalvar.setLayoutData(gdBtn);

        btnSalvar.addListener(SWT.Selection, e -> {
            String categoria = comboCategoria.getText().trim();
            String conteudo = txtNoticia.getText().trim();

            if (!categoria.isEmpty() && !conteudo.isEmpty()) {
                if (noticia == null) gerenciador.adicionar(new Noticia(categoria, conteudo, false));
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

    private void atualizarLista() {
        listNoticias.removeAll();
        for (Noticia n : gerenciador.getNoticias()) {
            listNoticias.add("[" + n.getCategoria() + "] " + n.getConteudo() + (n.isFixa() ? " (fixa)" : ""));
        }
        lblTotal.setText("Total de notícias: " + gerenciador.getNoticias().size());
    }

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

    private void alerta(Shell parent, String msg) {
        MessageBox box = new MessageBox(parent, SWT.ICON_WARNING | SWT.OK);
        box.setMessage(msg);
        box.open();
    }

    public static void main(String[] args) {
        Display display = new Display();
        new TelaNoticias(display);
        display.dispose();
    }
}

// Classe Noticia
class Noticia {
    private String categoria;
    private String conteudo;
    private boolean fixa;

    public Noticia(String categoria, String conteudo, boolean fixa) {
        this.categoria = categoria;
        this.conteudo = conteudo;
        this.fixa = fixa;
    }

    public String getCategoria() { return categoria; }
    public String getConteudo() { return conteudo; }
    public boolean isFixa() { return fixa; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
}

// GerenciadorNoticias
class GerenciadorNoticias {
    private java.util.List<Noticia> noticias = new ArrayList<>();

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