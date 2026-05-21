package com.banco;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.ImageIcon;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SisGESC extends JFrame {

    // ── Conexão ──────────────────────────────────────────────────
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sisgesc_publico_nota?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "NovaSenha123";

    private Connection conn;

    // ── Cores ────────────────────────────────────────────────────
    private static final Color C_BG = new Color(18, 18, 24);
    private static final Color C_PANEL = new Color(28, 28, 38);
    private static final Color C_CARD = new Color(36, 36, 50);
    private static final Color C_ACCENT = new Color(99, 102, 241);
    private static final Color C_ACCENT2 = new Color(139, 92, 246);
    private static final Color C_SUCCESS = new Color(34, 197, 94);
    private static final Color C_DANGER = new Color(239, 68, 68);
    private static final Color C_WARNING = new Color(251, 191, 36);
    private static final Color C_TEXT = new Color(226, 232, 240);
    private static final Color C_MUTED = new Color(100, 116, 139);
    private static final Color C_BORDER = new Color(51, 51, 72);
    private static final Color C_TABLE_H = new Color(45, 45, 65);
    private static final Color C_TABLE_R1 = new Color(32, 32, 46);
    private static final Color C_TABLE_R2 = new Color(28, 28, 40);
    private static final Color C_SEL = new Color(99, 102, 241, 60);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_MONO = new Font("Consolas", Font.PLAIN, 12);

    // ── Tabs ─────────────────────────────────────────────────────
    private JTabbedPane tabs;

    // ── Ícones (opcionais) ──────────────────────────────────────
    private Map<String, ImageIcon> icons = new HashMap<>();

    // ============================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SisGESC().setVisible(true));
    }

    // ============================================================
    public SisGESC() {
        setTitle("SisGESC — Sistema de Gestão Educacional e Social");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 900);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(C_BG);

        loadIcons();

        if (!conectar()) {
            showConnDialog();
        }

        buildUI();
    }

    private void loadIcons() {
        String[] iconNames = {"logo", "dashboard", "students", "staff", "classes", "enrollment", "attendance", "guardians", "finance", "alerts", "plus", "edit", "delete", "save", "cancel", "refresh", "connect"};
        for (String name : iconNames) {
            try {
                java.net.URL imgURL = getClass().getResource("/ICON/" + name + ".png");
                if (imgURL != null) {
                    ImageIcon originalIcon = new ImageIcon(imgURL);
                    Image image = originalIcon.getImage();
                    Image scaledImage = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    icons.put(name, new ImageIcon(scaledImage));
                } else {
                    ImageIcon originalIcon = new ImageIcon("src/ICON/" + name + ".png");
                    Image image = originalIcon.getImage();
                    Image scaledImage = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    icons.put(name, new ImageIcon(scaledImage));
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar ícone " + name + ": " + e.getMessage());
                icons.put(name, null);
            }
        }
    }

    private ImageIcon getIcon(String name) {
        return icons.getOrDefault(name, null);
    }

    // ── Conexão ──────────────────────────────────────────────────
    private boolean conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showConnDialog() {
        JDialog d = new JDialog(this, "Configurar Conexão MySQL", true);
        d.setSize(420, 320);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(C_PANEL);
        d.setLayout(new BorderLayout(0, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(24, 32, 8, 32));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        JLabel title = styledLabel("Configuração de Banco de Dados", F_SUBTITLE, C_TEXT);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        form.add(title, gc);
        gc.gridwidth = 1;

        JTextField fHost = styledField("localhost");
        JTextField fPort = styledField("3306");
        JTextField fDb = styledField("sisgesc_publico_nota");
        JTextField fUser = styledField("root");
        JPasswordField fPass = new JPasswordField("");
        styleField(fPass);

        String[] labels = {"Host:", "Porta:", "Database:", "Usuário:", "Senha:"};
        JComponent[] fields = {fHost, fPort, fDb, fUser, fPass};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0;
            gc.gridy = i + 1;
            gc.weightx = 0.3;
            form.add(styledLabel(labels[i], F_BODY, C_MUTED), gc);
            gc.gridx = 1;
            gc.weightx = 0.7;
            form.add(fields[i], gc);
        }

        JButton btnConn = accentButton("  Conectar", getIcon("connect"));
        JLabel status = styledLabel("", F_BODY, C_DANGER);
        btnConn.addActionListener(e -> {
            try {
                String url = "jdbc:mysql://" + fHost.getText() + ":" + fPort.getText()
                        + "/" + fDb.getText() + "?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url, fUser.getText(), new String(fPass.getPassword()));
                d.dispose();
            } catch (Exception ex) {
                status.setText("✗ " + ex.getMessage().split("\n")[0]);
            }
        });

        JPanel bot = new JPanel(new BorderLayout(8, 0));
        bot.setBackground(C_PANEL);
        bot.setBorder(BorderFactory.createEmptyBorder(8, 32, 20, 32));
        bot.add(status, BorderLayout.CENTER);
        bot.add(btnConn, BorderLayout.EAST);

        d.add(form, BorderLayout.CENTER);
        d.add(bot, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── UI Principal ─────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDER),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));
        JLabel logo = styledLabel(" SisGESC", F_TITLE, C_TEXT);
        logo.setIcon(getIcon("logo"));
        JLabel sub = styledLabel("Sistema de Gestão Educacional e Social · CCA Bom Jesus do Cangaíba", F_BODY, C_MUTED);
        JPanel logoBox = new JPanel(new GridLayout(2, 1, 0, 2));
        logoBox.setBackground(C_PANEL);
        logoBox.add(logo);
        logoBox.add(sub);

        JButton btnRefresh = accentButton("Atualizar", getIcon("refresh"));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        btnRefresh.addActionListener(e -> {
            refreshCurrentTab();
            JOptionPane.showMessageDialog(this, "Atualizado com Sucesso!", "Atualizar", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnSalvar = accentButton("Salvar Sistema", getIcon("save"));
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalvar.setBackground(C_SUCCESS);
        btnSalvar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnSalvar.addActionListener(e -> exportarSistema());
        btnSalvar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnSalvar.setBackground(C_SUCCESS.brighter());
            }

            public void mouseExited(MouseEvent e) {
                btnSalvar.setBackground(C_SUCCESS);
            }
        });

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnBox.setBackground(C_PANEL);
        btnBox.add(btnSalvar);
        btnBox.add(btnRefresh);

        header.add(logoBox, BorderLayout.WEST);
        header.add(btnBox, BorderLayout.EAST);

        tabs = new JTabbedPane(JTabbedPane.LEFT);
        styleTabs(tabs);

        tabs.addTab("Dashboard", getIcon("dashboard"), buildDashboard());
        tabs.addTab("Alunos", getIcon("students"), buildAlunosTab());
        tabs.addTab("Funcionários", getIcon("staff"), buildFuncionariosTab());
        tabs.addTab("Turmas", getIcon("classes"), buildTurmasTab());
        tabs.addTab("Matrículas", getIcon("enrollment"), buildMatriculasTab());
        tabs.addTab("Frequência", getIcon("attendance"), buildFrequenciaTab());
        tabs.addTab("Responsáveis", getIcon("guardians"), buildResponsaveisTab());
        tabs.addTab("Financeiro", getIcon("finance"), buildFinanceiroTab());
        tabs.addTab("Alertas", getIcon("alerts"), buildAlertasTab());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // ── Dashboard ────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel p = darkPanel(new BorderLayout(0, 20));
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel t = styledLabel("Painel de Controle", F_TITLE, C_TEXT);
        p.add(t, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 4, 16, 16));
        cards.setBackground(C_BG);
        cards.setPreferredSize(new Dimension(0, 300));

        String[][] kpis = {
                {"Alunos Ativos", countQuery("SELECT COUNT(*) FROM tb_matricula WHERE situacao_matricula='ativa'"), "students", "#6366f1"},
                {"Funcionários Ativos", countQuery("SELECT COUNT(*) FROM tb_funcionario WHERE status_funcionario='ativo'"), "staff", "#8b5cf6"},
                {"Turmas Ativas", countQuery("SELECT COUNT(*) FROM tb_turma WHERE status_turma='ativa'"), "classes", "#06b6d4"},
                {"Lista de Espera", countQuery("SELECT COUNT(*) FROM tb_lista_espera WHERE status_espera='aguardando'"), "enrollment", "#f59e0b"},
                {"Alertas Abertos", countQuery("SELECT COUNT(*) FROM tb_alerta WHERE status_alerta='Aberto'"), "alerts", "#ef4444"},
                {"Repasses (mês atual)", countQuery("SELECT IFNULL(SUM(valor_repasse),0) FROM tb_repasse WHERE mes_referencia=DATE_FORMAT(NOW(),'%Y-%m')"), "finance", "#22c55e"},
                {"Total Gastos", countQuery("SELECT IFNULL(SUM(valor_gasto),0) FROM tb_gasto"), "finance", "#f97316"},
                {"Faltas (este mês)", countQuery("SELECT COUNT(*) FROM tb_frequencia WHERE presente=0 AND MONTH(data_aula)=MONTH(NOW())"), "attendance", "#94a3b8"},
        };

        for (String[] kpi : kpis) cards.add(kpiCard(kpi[0], kpi[1], kpi[2], kpi[3]));

        JPanel lower = darkPanel(new BorderLayout(0, 10));
        JLabel tOcup = styledLabel("Ocupação das Turmas em Tempo Real", F_SUBTITLE, C_TEXT);
        lower.add(tOcup, BorderLayout.NORTH);

        String[] cols = {"Turma", "Turno", "Faixa Etária", "Capacidade", "Matriculados", "Vagas", "Em Espera"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try {
            ResultSet rs = query("SELECT nome_turma,turno,faixa_etaria_inicio,faixa_etaria_fim,"
                    + "capacidade_max,alunos_matriculados,vagas_disponiveis,alunos_em_espera FROM vw_ocupacao_turmas");
            while (rs.next()) model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2),
                    rs.getInt(3) + " - " + rs.getInt(4) + " anos",
                    rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8)
            });
        } catch (Exception ignored) {
        }

        JScrollPane sp = scrollTable(model);
        JTable tabelaTurmas = (JTable) sp.getViewport().getView();
        tabelaTurmas.setRowHeight(50);
        tabelaTurmas.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tabelaTurmas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        lower.add(sp, BorderLayout.CENTER);

        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setBackground(C_BG);
        cardsWrapper.setPreferredSize(new Dimension(0, 320));
        cardsWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        cardsWrapper.add(cards, BorderLayout.CENTER);

        JPanel middle = darkPanel(new BorderLayout(0, 20));
        middle.add(cardsWrapper, BorderLayout.NORTH);
        middle.add(lower, BorderLayout.CENTER);

        p.add(middle, BorderLayout.CENTER);
        return p;
    }

    // ── Alunos ───────────────────────────────────────────────────
    private JPanel buildAlunosTab() {
        String[] cols = {"ID", "Código", "Nome", "NIS", "CPF", "Sexo", "Nasc.", "Idade", "Raça/Cor", "Situação"};
        String sql = "SELECT pk_aluno,codigo_aluno,nome_aluno,nis_aluno,cpf_aluno,sexo,"
                + "data_nascimento,idade,raca_cor,situacao_aluno FROM vw_aluno ORDER BY nome_aluno";
        return crudPanel("Alunos", cols, sql, "tb_aluno",
                () -> dialogAluno(null),
                (row) -> dialogAluno(row)
        );
    }

    private void dialogAluno(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Aluno" : "Novo Aluno");

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();

        JTextField fNome = styledField(edit ? row[2].toString() : "");
        JTextField fNis = styledField(edit ? row[3].toString() : "");
        JTextField fCpf = styledField(edit ? row[4].toString() : "");
        JFormattedTextField fNasc = dateField(edit ? row[6].toString() : "");

        addFormRow(form, "Nome Completo:", fNome);
        addFormRow(form, "NIS (11 dígitos):", fNis);
        addFormRow(form, "CPF (11 dígitos):", fCpf);
        addFormRow(form, "Data de Nascimento:", fNasc);

        JComboBox<String> cbSexo = new JComboBox<>(new String[]{"Masculino", "Feminino"});
        JComboBox<String> cbRaca = new JComboBox<>(new String[]{"Branca", "Preta", "Parda", "Amarela", "Indigena", "Nao declarada"});
        JComboBox<String> cbSit = new JComboBox<>(new String[]{"ativo", "inativo", "transferido", "concluido"});
        styleCombo(cbSexo);
        styleCombo(cbRaca);
        styleCombo(cbSit);

        addFormRow(form, "Sexo:", cbSexo);
        addFormRow(form, "Raça/Cor:", cbRaca);
        if (edit) {
            cbSexo.setSelectedItem(row[5]);
            cbRaca.setSelectedItem(row[8]);
            cbSit.setSelectedItem(row[9]);
            addFormRow(form, "Situação:", cbSit);
        }

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ──────────────────────────────────────
            if (fNome.getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O campo 'Nome Completo' é obrigatório.",
                        "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                fNome.requestFocus();
                return;
            }
            if (!fNis.getText().matches("\\d{11}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O NIS deve conter exatamente 11 dígitos numéricos.\n"
                                + "Valor informado: '" + fNis.getText() + "'",
                        "NIS inválido", JOptionPane.WARNING_MESSAGE);
                fNis.requestFocus();
                return;
            }
            if (!fCpf.getText().matches("\\d{11}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O CPF deve conter exatamente 11 dígitos numéricos (sem pontos ou traços).\n"
                                + "Valor informado: '" + fCpf.getText() + "'",
                        "CPF inválido", JOptionPane.WARNING_MESSAGE);
                fCpf.requestFocus();
                return;
            }
            if (fNasc.getText().contains("_") || fNasc.getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        "⚠ Preencha a Data de Nascimento no formato DD/MM/AAAA.\n"
                                + "O banco aceita apenas datas válidas completas.",
                        "Data inválida", JOptionPane.WARNING_MESSAGE);
                fNasc.requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ───────────────────────────────
            try {
                if (edit) {
                    // CORREÇÃO: row[0] (pk_aluno) adicionado como último parâmetro do WHERE
                    update("UPDATE tb_aluno SET nome_aluno=?,nis_aluno=?,cpf_aluno=?,data_nascimento=?,"
                                    + "sexo=?,raca_cor=?,situacao_aluno=? WHERE pk_aluno=?",
                            fNome.getText(), fNis.getText(), fCpf.getText(),
                            toDBDate(fNasc.getText()),
                            cbSexo.getSelectedItem().toString(),
                            cbRaca.getSelectedItem().toString(),
                            cbSit.getSelectedItem().toString(),
                            row[0]); // ← CORREÇÃO: estava faltando row[0]
                } else {
                    update("INSERT INTO tb_aluno(nome_aluno,nis_aluno,cpf_aluno,data_nascimento,sexo,raca_cor) VALUES(?,?,?,?,?,?)",
                            fNome.getText(), fNis.getText(), fCpf.getText(),
                            toDBDate(fNasc.getText()),
                            cbSexo.getSelectedItem(), cbRaca.getSelectedItem());
                }
                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                // Mensagens amigáveis para erros conhecidos do banco
                if (msg != null && msg.contains("uq_nis_aluno"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Já existe um aluno cadastrado com o NIS '" + fNis.getText() + "'.\nVerifique e tente novamente.",
                            "NIS duplicado", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("uq_cpf_aluno"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Já existe um aluno cadastrado com o CPF '" + fCpf.getText() + "'.\nVerifique e tente novamente.",
                            "CPF duplicado", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("CPF do aluno invalido"))
                    JOptionPane.showMessageDialog(d,
                            "✗ O CPF informado '" + fCpf.getText() + "' não passou na validação de dígito verificador.\nVerifique se o número está correto.",
                            "CPF inválido", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("8 e 14 anos"))
                    JOptionPane.showMessageDialog(d,
                            "✗ O aluno deve ter entre 8 e 14 anos para ser cadastrado.\nVerifique a data de nascimento informada.",
                            "Faixa etária inválida", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("Incorrect date value"))
                    JOptionPane.showMessageDialog(d,
                            "✗ A data de nascimento informada é inválida.\nUse o formato DD/MM/AAAA com uma data real.",
                            "Data inválida", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    // ── Funcionários ─────────────────────────────────────────────
    private JPanel buildFuncionariosTab() {
        String[] cols = {"ID", "CPF", "Nome", "Cargo", "Admissão", "Vínculo", "Salário", "C.H.", "Status"};
        String sql = "SELECT f.pk_funcionario,f.cpf_funcionario,f.nome_funcionario,c.nome_cargo,"
                + "f.data_admissao,f.tipo_vinculo,f.salario,f.carga_horaria_semanal,f.status_funcionario "
                + "FROM tb_funcionario f JOIN tb_cargo c ON c.pk_cargo=f.fk_cargo ORDER BY f.nome_funcionario";
        return crudPanel("Funcionários", cols, sql, "tb_funcionario",
                () -> dialogFuncionario(null),
                (row) -> dialogFuncionario(row)
        );
    }

    private void dialogFuncionario(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Funcionário" : "Novo Funcionário");

        JComboBox<String> cbCargo = new JComboBox<>();
        Map<String, Integer> cargoMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_cargo,nome_cargo FROM tb_cargo");
            while (rs.next()) {
                cargoMap.put(rs.getString(2), rs.getInt(1));
                cbCargo.addItem(rs.getString(2));
            }
        } catch (Exception ignored) {
        }
        styleCombo(cbCargo);

        // CORREÇÃO: valores correspondem exatamente ao ENUM('CLT','Estatutario','Voluntario') do banco
        JComboBox<String> cbVinculo = new JComboBox<>(new String[]{"CLT", "Estatutario", "Voluntario"});
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"ativo", "desligado", "afastado"});
        styleCombo(cbVinculo);
        styleCombo(cbStatus);

        JTextField fCpf = styledField(edit ? row[1].toString() : "");
        JTextField fNome = styledField(edit ? row[2].toString() : "");
        JFormattedTextField fAdm = dateField(edit ? row[4].toString() : "");
        JTextField fSalario = styledField(edit ? row[6].toString() : "");
        JTextField fCH = styledField(edit ? row[7].toString() : "");

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "CPF (11 dígitos):", fCpf);
        addFormRow(form, "Nome Completo:", fNome);
        addFormRow(form, "Data Admissão:", fAdm);
        addFormRow(form, "Salário:", fSalario);
        addFormRow(form, "Carga Horária Semanal:", fCH);
        addFormRow(form, "Cargo:", cbCargo);
        addFormRow(form, "Vínculo:", cbVinculo);
        addFormRow(form, "Status:", cbStatus);

        if (edit) {
            cbCargo.setSelectedItem(row[3]);
            cbVinculo.setSelectedItem(row[5]);
            cbStatus.setSelectedItem(row[8]);
        }

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ──────────────────────────────────────
            if (!fCpf.getText().matches("\\d{11}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O CPF deve conter exatamente 11 dígitos numéricos (sem pontos ou traços).\n"
                                + "Valor informado: '" + fCpf.getText() + "'",
                        "CPF inválido", JOptionPane.WARNING_MESSAGE);
                fCpf.requestFocus();
                return;
            }
            if (fNome.getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O campo 'Nome Completo' é obrigatório.",
                        "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                fNome.requestFocus();
                return;
            }
            if (fAdm.getText().contains("_") || fAdm.getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        "⚠ Preencha a Data de Admissão no formato DD/MM/AAAA.\n"
                                + "O banco não aceita datas incompletas.",
                        "Data inválida", JOptionPane.WARNING_MESSAGE);
                fAdm.requestFocus();
                return;
            }
            double salario = 0;
            try {
                salario = Double.parseDouble(fSalario.getText().replace(",", "."));
                if (salario <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O Salário deve ser um número positivo (ex: 3200.00).\n"
                                + "Valor informado: '" + fSalario.getText() + "'",
                        "Salário inválido", JOptionPane.WARNING_MESSAGE);
                fSalario.requestFocus();
                return;
            }
            int ch = 0;
            try {
                ch = Integer.parseInt(fCH.getText().trim());
                if (ch <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Carga Horária Semanal deve ser um número inteiro positivo (ex: 40).\n"
                                + "Valor informado: '" + fCH.getText() + "'",
                        "Carga horária inválida", JOptionPane.WARNING_MESSAGE);
                fCH.requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ───────────────────────────────
            try {
                int cargoId = cargoMap.getOrDefault(cbCargo.getSelectedItem().toString(), 1);
                if (edit) {
                    update("UPDATE tb_funcionario SET cpf_funcionario=?,nome_funcionario=?,data_admissao=?,"
                                    + "salario=?,carga_horaria_semanal=?,tipo_vinculo=?,status_funcionario=?,fk_cargo=? WHERE pk_funcionario=?",
                            fCpf.getText(), fNome.getText(), toDBDate(fAdm.getText()),
                            fSalario.getText(), fCH.getText(),
                            cbVinculo.getSelectedItem().toString(),
                            cbStatus.getSelectedItem().toString(),
                            cargoId, row[0]);
                } else {
                    update("INSERT INTO tb_funcionario(cpf_funcionario,nome_funcionario,data_admissao,salario,carga_horaria_semanal,tipo_vinculo,status_funcionario,fk_cargo) VALUES(?,?,?,?,?,?,?,?)",
                            fCpf.getText(), fNome.getText(), toDBDate(fAdm.getText()),
                            fSalario.getText(), fCH.getText(),
                            cbVinculo.getSelectedItem().toString(),
                            cbStatus.getSelectedItem().toString(),
                            cargoId);
                }
                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("uq_cpf_funcionario"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Já existe um funcionário cadastrado com o CPF '" + fCpf.getText() + "'.\nVerifique e tente novamente.",
                            "CPF duplicado", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("CPF do funcionario invalido"))
                    JOptionPane.showMessageDialog(d,
                            "✗ O CPF informado '" + fCpf.getText() + "' não passou na validação de dígito verificador.\nVerifique se o número está correto.",
                            "CPF inválido", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("Incorrect date value"))
                    JOptionPane.showMessageDialog(d,
                            "✗ A data de admissão informada é inválida.\nUse o formato DD/MM/AAAA com uma data real.",
                            "Data inválida", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    // ── Turmas ───────────────────────────────────────────────────
    private JPanel buildTurmasTab() {
        String[] cols = {"ID", "Nome", "Turno", "Faixa Etária Início", "Faixa Etária Fim", "Capacidade", "Status"};
        String sql = "SELECT pk_turma,nome_turma,turno,faixa_etaria_inicio,faixa_etaria_fim,capacidade_max,status_turma FROM tb_turma ORDER BY nome_turma";
        return crudPanel("Turmas", cols, sql, "tb_turma",
                () -> dialogTurma(null),
                (row) -> dialogTurma(row)
        );
    }

    private void dialogTurma(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Turma" : "Nova Turma");

        // CORREÇÃO: ENUM turno no banco é ('Manha','Tarde') — removidos "Noite" e "Integral"
        JComboBox<String> cbTurno = new JComboBox<>(new String[]{"Manha", "Tarde"});
        // CORREÇÃO: ENUM status_turma no banco é ('ativa','encerrada') — removidos "inativa" e "concluida"
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"ativa", "encerrada"});
        styleCombo(cbTurno);
        styleCombo(cbStatus);

        String[] nomes = {"Nome da Turma", "Faixa Etária Início", "Faixa Etária Fim", "Capacidade Máxima"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{
                row[1].toString(), row[3].toString(), row[4].toString(), row[5].toString()
        } : null);

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Turno:", cbTurno);
        addFormRow(form, "Status:", cbStatus);
        if (edit) {
            cbTurno.setSelectedItem(row[2]);
            cbStatus.setSelectedItem(row[6]);
        }

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ──────────────────────────────────────
            if (fs[0].getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O campo 'Nome da Turma' é obrigatório.",
                        "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                fs[0].requestFocus();
                return;
            }
            int faixaIni = 0, faixaFim = 0, cap = 0;
            try {
                faixaIni = Integer.parseInt(fs[1].getText().trim());
                if (faixaIni < 8 || faixaIni > 14) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Faixa Etária Início deve ser um número inteiro entre 8 e 14.\n"
                                + "Valor informado: '" + fs[1].getText() + "'",
                        "Faixa etária inválida", JOptionPane.WARNING_MESSAGE);
                fs[1].requestFocus();
                return;
            }
            try {
                faixaFim = Integer.parseInt(fs[2].getText().trim());
                if (faixaFim < 8 || faixaFim > 14 || faixaFim <= faixaIni) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Faixa Etária Fim deve ser maior que o Início e estar entre 8 e 14.\n"
                                + "Valor informado: '" + fs[2].getText() + "'",
                        "Faixa etária inválida", JOptionPane.WARNING_MESSAGE);
                fs[2].requestFocus();
                return;
            }
            try {
                cap = Integer.parseInt(fs[3].getText().trim());
                if (cap <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Capacidade Máxima deve ser um número inteiro positivo.\n"
                                + "Valor informado: '" + fs[3].getText() + "'",
                        "Capacidade inválida", JOptionPane.WARNING_MESSAGE);
                fs[3].requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ───────────────────────────────
            try {
                if (edit) {
                    update("UPDATE tb_turma SET nome_turma=?,turno=?,faixa_etaria_inicio=?,faixa_etaria_fim=?,capacidade_max=?,status_turma=? WHERE pk_turma=?",
                            fs[0].getText(), cbTurno.getSelectedItem().toString(),
                            fs[1].getText(), fs[2].getText(), fs[3].getText(),
                            cbStatus.getSelectedItem().toString(), row[0]);
                } else {
                    update("INSERT INTO tb_turma(nome_turma,turno,faixa_etaria_inicio,faixa_etaria_fim,capacidade_max,status_turma) VALUES(?,?,?,?,?,?)",
                            fs[0].getText(), cbTurno.getSelectedItem().toString(),
                            fs[1].getText(), fs[2].getText(), fs[3].getText(),
                            cbStatus.getSelectedItem().toString());
                }
                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("uq_turma_ano"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Já existe uma turma com este nome no mesmo ano letivo.\nEscolha um nome diferente.",
                            "Turma duplicada", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("chk_faixa_etaria"))
                    JOptionPane.showMessageDialog(d,
                            "✗ A faixa etária deve estar entre 8 e 14 anos, conforme regra do banco de dados.",
                            "Faixa etária inválida", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    // ── Matrículas ───────────────────────────────────────────────
    private JPanel buildMatriculasTab() {
        String[] cols = {"ID", "Aluno", "Turma", "Data Matrícula", "Situação"};
        String sql = "SELECT m.pk_matricula,a.nome_aluno,t.nome_turma,m.data_matricula,m.situacao_matricula "
                + "FROM tb_matricula m JOIN tb_aluno a ON a.pk_aluno=m.fk_aluno "
                + "JOIN tb_turma t ON t.pk_turma=m.fk_turma ORDER BY m.data_matricula DESC";
        return crudPanel("Matrículas", cols, sql, "tb_matricula",
                () -> dialogMatricula(null),
                (row) -> dialogMatricula(row)
        );
    }

    private void dialogMatricula(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Matrícula" : "Nova Matrícula");

        JComboBox<String> cbAluno = new JComboBox<>();
        JComboBox<String> cbTurma = new JComboBox<>();
        Map<String, Integer> alunoMap = new LinkedHashMap<>();
        Map<String, Integer> turmaMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_aluno,nome_aluno FROM tb_aluno ORDER BY nome_aluno");
            while (rs.next()) {
                alunoMap.put(rs.getString(2), rs.getInt(1));
                cbAluno.addItem(rs.getString(2));
            }
            rs = query("SELECT pk_turma,nome_turma FROM tb_turma ORDER BY nome_turma");
            while (rs.next()) {
                turmaMap.put(rs.getString(2), rs.getInt(1));
                cbTurma.addItem(rs.getString(2));
            }
        } catch (Exception ignored) {
        }
        styleCombo(cbAluno);
        styleCombo(cbTurma);

        // CORREÇÃO: ENUM situacao_matricula no banco é ('ativa','cancelada','concluida') — removidos "inativa" e "trancada"
        JComboBox<String> cbSit = new JComboBox<>(new String[]{"ativa", "cancelada", "concluida"});
        styleCombo(cbSit);

        String[] nomes = {"Data Matrícula (AAAA-MM-DD)"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{row[3].toString()} : null);

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Aluno:", cbAluno);
        addFormRow(form, "Turma:", cbTurma);
        addFormRow(form, "Situação:", cbSit);
        if (edit) {
            cbAluno.setSelectedItem(row[1]);
            cbTurma.setSelectedItem(row[2]);
            cbSit.setSelectedItem(row[4]);
        }

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ──────────────────────────────────────
            if (cbAluno.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(d,
                        "⚠ Nenhum aluno disponível para matrícula.",
                        "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cbTurma.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(d,
                        "⚠ Nenhuma turma disponível.",
                        "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (fs[0].getText().isBlank() || !fs[0].getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Data de Matrícula deve estar no formato AAAA-MM-DD (ex: 2025-02-01).",
                        "Data inválida", JOptionPane.WARNING_MESSAGE);
                fs[0].requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ───────────────────────────────
            try {
                int alunoId = alunoMap.getOrDefault(cbAluno.getSelectedItem().toString(), 0);
                int turmaId = turmaMap.getOrDefault(cbTurma.getSelectedItem().toString(), 0);
                if (edit) {
                    update("UPDATE tb_matricula SET fk_aluno=?,fk_turma=?,data_matricula=?,situacao_matricula=? WHERE pk_matricula=?",
                            alunoId, turmaId, fs[0].getText(),
                            cbSit.getSelectedItem().toString(), row[0]);
                } else {
                    update("INSERT INTO tb_matricula(fk_aluno,fk_turma,data_matricula,situacao_matricula) VALUES(?,?,?,?)",
                            alunoId, turmaId, fs[0].getText(),
                            cbSit.getSelectedItem().toString());
                }
                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("ja possui matricula ativa"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Este aluno já possui uma matrícula ativa.\nEncerre a matrícula atual antes de criar uma nova.",
                            "Matrícula duplicada", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("capacidade maxima"))
                    JOptionPane.showMessageDialog(d,
                            "✗ A turma selecionada atingiu sua capacidade máxima de alunos.\nAdicione o aluno à lista de espera.",
                            "Turma lotada", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("Ha alunos na lista de espera"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Há alunos aguardando na lista de espera para esta turma.\nA matrícula será processada automaticamente quando houver vaga.",
                            "Lista de espera ativa", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("nao compativel com a faixa etaria"))
                    JOptionPane.showMessageDialog(d,
                            "✗ A idade atual do aluno não é compatível com a faixa etária desta turma.\nEscolha uma turma adequada para a idade do aluno.",
                            "Faixa etária incompatível", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("limite total"))
                    JOptionPane.showMessageDialog(d,
                            "✗ O limite total da instituição (200 pessoas) foi atingido.\nNão é possível realizar novas matrículas no momento.",
                            "Limite atingido", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    // ── Frequência ───────────────────────────────────────────────
    private JPanel buildFrequenciaTab() {
        String[] cols = {"ID", "Matrícula", "Aluno", "Data Aula", "Presente", "Motivo Falta"};
        String sql = "SELECT f.pk_frequencia,m.pk_matricula,a.nome_aluno,f.data_aula,f.presente,f.motivo_falta "
                + "FROM tb_frequencia f JOIN tb_matricula m ON m.pk_matricula=f.fk_matricula "
                + "JOIN tb_aluno a ON a.pk_aluno=m.fk_aluno ORDER BY f.data_aula DESC";
        return crudPanel("Frequência", cols, sql, "tb_frequencia",
                () -> dialogFrequencia(null),
                (row) -> dialogFrequencia(row)
        );
    }

    private void dialogFrequencia(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Frequência" : "Nova Frequência");

        JComboBox<String> cbMatricula = new JComboBox<>();
        Map<String, Integer> matMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT m.pk_matricula,CONCAT(a.nome_aluno, ' (', t.nome_turma, ')') AS matricula_info FROM tb_matricula m JOIN tb_aluno a ON a.pk_aluno=m.fk_aluno JOIN tb_turma t ON t.pk_turma=m.fk_turma ORDER BY matricula_info");
            while (rs.next()) {
                matMap.put(rs.getString(2), rs.getInt(1));
                cbMatricula.addItem(rs.getString(2));
            }
        } catch (Exception ignored) {
        }
        styleCombo(cbMatricula);

        JRadioButton rbPresente = new JRadioButton("Presente");
        JRadioButton rbAusente = new JRadioButton("Ausente");
        ButtonGroup bgPresenca = new ButtonGroup();
        bgPresenca.add(rbPresente);
        bgPresenca.add(rbAusente);
        rbPresente.setSelected(true);
        for (JRadioButton rb : new JRadioButton[]{rbPresente, rbAusente}) {
            rb.setBackground(C_PANEL);
            rb.setForeground(C_TEXT);
            rb.setFont(F_BODY);
        }
        JPanel pnlPresenca = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlPresenca.setBackground(C_PANEL);
        pnlPresenca.add(rbPresente);
        pnlPresenca.add(rbAusente);

        JTextField fMotivo = styledField("");
        fMotivo.setEnabled(false);

        rbPresente.addActionListener(e -> fMotivo.setEnabled(false));
        rbAusente.addActionListener(e -> fMotivo.setEnabled(true));

        String[] nomes = {"Data Aula (AAAA-MM-DD)"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{row[3].toString()} : null);

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Matrícula:", cbMatricula);
        addFormRow(form, "Presença:", pnlPresenca);
        addFormRow(form, "Motivo Falta:", fMotivo);

        if (edit) {
            cbMatricula.setSelectedItem(row[2].toString() + " (" + row[1].toString() + ")");
            boolean presente = (boolean) row[4];
            if (presente) {
                rbPresente.setSelected(true);
                fMotivo.setEnabled(false);
            } else {
                rbAusente.setSelected(true);
                fMotivo.setEnabled(true);
            }
            fMotivo.setText(row[5] != null ? row[5].toString() : "");
        }

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ──────────────────────────────────────
            if (cbMatricula.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(d,
                        "⚠ Selecione uma matrícula.",
                        "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (fs[0].getText().isBlank() || !fs[0].getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Data da Aula deve estar no formato AAAA-MM-DD (ex: 2025-03-17).",
                        "Data inválida", JOptionPane.WARNING_MESSAGE);
                fs[0].requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ───────────────────────────────
            try {
                boolean pres = rbPresente.isSelected();
                String motivo = fMotivo.getText().isBlank() ? null : fMotivo.getText();
                if (edit) {
                    update("UPDATE tb_frequencia SET data_aula=?,presente=?,motivo_falta=? WHERE pk_frequencia=?",
                            fs[0].getText(), pres, motivo, row[0]);
                } else {
                    int matId = matMap.getOrDefault(cbMatricula.getSelectedItem().toString(), 0);
                    update("INSERT INTO tb_frequencia(fk_matricula,data_aula,presente,motivo_falta) VALUES(?,?,?,?)",
                            matId, fs[0].getText(), pres, motivo);
                }
                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("uq_frequencia"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Já existe um registro de frequência para este aluno nesta data.\nCada aluno pode ter apenas um registro por dia de aula.",
                            "Frequência duplicada", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("Incorrect date value"))
                    JOptionPane.showMessageDialog(d,
                            "✗ A data informada não é válida.\nUse o formato AAAA-MM-DD com uma data real (ex: 2025-03-17).",
                            "Data inválida", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    // ── Responsáveis ─────────────────────────────────────────────
    private JPanel buildResponsaveisTab() {
        String[] cols = {"ID", "CPF", "Nome", "Cadastro"};
        String sql = "SELECT pk_responsavel,cpf_responsavel,nome_responsavel,data_criacao FROM tb_responsavel ORDER BY nome_responsavel";
        return crudPanel("Responsáveis", cols, sql, "tb_responsavel",
                () -> dialogResponsavel(null),
                (row) -> dialogResponsavel(row)
        );
    }

    private void dialogResponsavel(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Responsável" : "Novo Responsável");
        d.setSize(520, 680); // um pouco maior para caber os campos

        String[] nomes = {"Nome Completo", "CPF (11 dígitos)"};
        JTextField[] fs = formFields(d, nomes,
                edit ? new String[]{row[2].toString(), row[1].toString()} : null);

        // ── Contatos ──────────────────────────────────────────────
        JTextField fTel = styledField("");
        JTextField fEmail = styledField("");

        // ── Endereço ──────────────────────────────────────────────
        JTextField fCep = styledField("");
        JTextField fLogradouro = styledField("");
        JTextField fNumero = styledField("");
        JTextField fComplemento = styledField("");
        JTextField fBairro = styledField("");
        JTextField fCidade = styledField("São Paulo");
        JTextField fUf = styledField("SP");
        fUf.setPreferredSize(new Dimension(50, 36));

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();

        // Separador — Contatos
        JLabel lblSepContato = styledLabel("   Contatos   ", F_BODY, C_MUTED);
        form.add(lblSepContato);

        addFormRow(form, "Telefone:", fTel);
        addFormRow(form, "E-mail:", fEmail);

        // Separador — Endereço
        JLabel lblSepEnd = styledLabel("   Endereço   ", F_BODY, C_MUTED);
        form.add(lblSepEnd);

        addFormRow(form, "CEP (8 dígitos):", fCep);
        addFormRow(form, "Logradouro:", fLogradouro);
        addFormRow(form, "Número:", fNumero);
        addFormRow(form, "Complemento:", fComplemento);
        addFormRow(form, "Bairro:", fBairro);
        addFormRow(form, "Cidade:", fCidade);
        addFormRow(form, "UF:", fUf);

        // ── Pré-preenche no modo edição ───────────────────────────
        if (edit) {
            try {
                ResultSet rs = query(
                        "SELECT tipo_contato, valor_contato FROM tb_contato_responsavel " +
                                "WHERE fk_responsavel=? ORDER BY principal DESC", row[0]);
                while (rs.next()) {
                    String tipo = rs.getString(1);
                    String val = rs.getString(2);
                    if (tipo.equals("telefone") && fTel.getText().isBlank()) fTel.setText(val);
                    if (tipo.equals("email") && fEmail.getText().isBlank()) fEmail.setText(val);
                }
            } catch (Exception ignored) {
            }

            try {
                ResultSet rs = query(
                        "SELECT logradouro, numero, complemento, bairro, cidade, uf, cep " +
                                "FROM tb_endereco_responsavel WHERE fk_responsavel=?", row[0]);
                if (rs.next()) {
                    fLogradouro.setText(rs.getString(1));
                    fNumero.setText(rs.getString(2) != null ? rs.getString(2) : "");
                    fComplemento.setText(rs.getString(3) != null ? rs.getString(3) : "");
                    fBairro.setText(rs.getString(4) != null ? rs.getString(4) : "");
                    fCidade.setText(rs.getString(5));
                    fUf.setText(rs.getString(6));
                    fCep.setText(rs.getString(7));
                }
            } catch (Exception ignored) {
            }
        }

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ────────────────────────────────────────
            if (fs[0].getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        " O campo 'Nome Completo' é obrigatório.",
                        "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                fs[0].requestFocus();
                return;
            }
            if (!fs[1].getText().matches("\\d{11}")) {
                JOptionPane.showMessageDialog(d,
                        " O CPF deve conter exatamente 11 dígitos numéricos (sem pontos ou traços).\n"
                                + "Valor informado: '" + fs[1].getText() + "'",
                        "CPF inválido", JOptionPane.WARNING_MESSAGE);
                fs[1].requestFocus();
                return;
            }
            String tel = fTel.getText().trim();
            if (!tel.isBlank() && !tel.matches("^\\(\\d{2}\\) \\d{4,5}-\\d{4}$")) {
                JOptionPane.showMessageDialog(d,
                        " Telefone inválido. Use o formato (XX) XXXXX-XXXX ou (XX) XXXX-XXXX.\n"
                                + "Valor informado: '" + tel + "'",
                        "Telefone inválido", JOptionPane.WARNING_MESSAGE);
                fTel.requestFocus();
                return;
            }
            String email = fEmail.getText().trim();
            if (!email.isBlank() && !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(d,
                        " E-mail inválido.\nValor informado: '" + email + "'",
                        "E-mail inválido", JOptionPane.WARNING_MESSAGE);
                fEmail.requestFocus();
                return;
            }
            String cep = fCep.getText().trim();
            if (!cep.isBlank() && !cep.matches("\\d{8}")) {
                JOptionPane.showMessageDialog(d,
                        " CEP deve conter exatamente 8 dígitos numéricos (sem hífen).\n"
                                + "Valor informado: '" + cep + "'",
                        "CEP inválido", JOptionPane.WARNING_MESSAGE);
                fCep.requestFocus();
                return;
            }
            if (!cep.isBlank() && fLogradouro.getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        " Se o CEP foi informado, o Logradouro é obrigatório.",
                        "Logradouro obrigatório", JOptionPane.WARNING_MESSAGE);
                fLogradouro.requestFocus();
                return;
            }
            if (!cep.isBlank() && fBairro.getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        " Se o CEP foi informado, o Bairro é obrigatório.",
                        "Bairro obrigatório", JOptionPane.WARNING_MESSAGE);
                fBairro.requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ────────────────────────────────
            try {
                int respId;
                if (edit) {
                    update("UPDATE tb_responsavel SET nome_responsavel=?,cpf_responsavel=? WHERE pk_responsavel=?",
                            fs[0].getText(), fs[1].getText(), row[0]);
                    respId = ((Number) row[0]).intValue();
                    update("DELETE FROM tb_contato_responsavel WHERE fk_responsavel=?", respId);
                    update("DELETE FROM tb_endereco_responsavel WHERE fk_responsavel=?", respId);
                } else {
                    update("INSERT INTO tb_responsavel(nome_responsavel,cpf_responsavel) VALUES(?,?)",
                            fs[0].getText(), fs[1].getText());
                    ResultSet rs = query("SELECT pk_responsavel FROM tb_responsavel WHERE cpf_responsavel=?",
                            fs[1].getText());
                    rs.next();
                    respId = rs.getInt(1);
                }

                // Contatos
                if (!tel.isBlank())
                    update("INSERT INTO tb_contato_responsavel(fk_responsavel,tipo_contato,valor_contato,principal) VALUES(?,?,?,?)",
                            respId, "telefone", tel, 1);
                if (!email.isBlank())
                    update("INSERT INTO tb_contato_responsavel(fk_responsavel,tipo_contato,valor_contato,principal) VALUES(?,?,?,?)",
                            respId, "email", email, tel.isBlank() ? 1 : 0);

                // Endereço (salva apenas se CEP foi preenchido)
                if (!cep.isBlank()) {
                    String num = fNumero.getText().trim().isEmpty() ? null : fNumero.getText().trim();
                    String comp = fComplemento.getText().trim().isEmpty() ? null : fComplemento.getText().trim();
                    update("INSERT INTO tb_endereco_responsavel " +
                                    "(fk_responsavel,logradouro,numero,complemento,bairro,cidade,uf,cep) " +
                                    "VALUES(?,?,?,?,?,?,?,?)",
                            respId,
                            fLogradouro.getText().trim(),
                            num, comp,
                            fBairro.getText().trim(),
                            fCidade.getText().trim(),
                            fUf.getText().trim().toUpperCase(),
                            cep);
                }

                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("uq_cpf_responsavel"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Já existe um responsável com o CPF '" + fs[1].getText() + "'.",
                            "CPF duplicado", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("CPF do responsavel invalido"))
                    JOptionPane.showMessageDialog(d,
                            "✗ CPF '" + fs[1].getText() + "' inválido no dígito verificador.",
                            "CPF inválido", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("Telefone do responsavel invalido"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Telefone rejeitado pelo banco. Use: (XX) XXXXX-XXXX",
                            "Telefone inválido", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("E-mail do responsavel invalido"))
                    JOptionPane.showMessageDialog(d,
                            "✗ E-mail rejeitado pelo banco.",
                            "E-mail inválido", JOptionPane.ERROR_MESSAGE);
                else if (msg != null && msg.contains("chk_cep"))
                    JOptionPane.showMessageDialog(d,
                            "✗ CEP inválido. Use apenas 8 dígitos numéricos.",
                            "CEP inválido", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    // ── Financeiro ───────────────────────────────────────────────
    private JPanel buildFinanceiroTab() {
        JPanel p = darkPanel(new BorderLayout(0, 0));
        JTabbedPane ft = new JTabbedPane();
        styleTabs(ft);

        String[] cR = {"ID", "Programa", "Data", "Valor (R$)", "Mês Ref.", "Descrição"};
        String sqlR = "SELECT r.pk_repasse,p.nome_programa,r.data_repasse,r.valor_repasse,r.mes_referencia,r.descricao "
                + "FROM tb_repasse r JOIN tb_programa_social p ON p.pk_programa=r.fk_programa ORDER BY r.data_repasse DESC";
        ft.addTab("Repasses", getIcon("finance"), crudPanel("Repasses", cR, sqlR, "tb_repasse",
                () -> dialogRepasse(null), (row) -> dialogRepasse(row)));

        String[] cG = {"ID", "Repasse", "Categoria", "Data", "Valor (R$)", "Descrição", "NF"};
        String sqlG = "SELECT g.pk_gasto,r.mes_referencia,c.nome_categoria,g.data_gasto,"
                + "g.valor_gasto,g.descricao,g.nota_fiscal "
                + "FROM tb_gasto g JOIN tb_repasse r ON r.pk_repasse=g.fk_repasse "
                + "JOIN tb_categoria_gastos c ON c.pk_categoria=g.fk_categoria ORDER BY g.data_gasto DESC";
        ft.addTab("Gastos", getIcon("finance"), crudPanel("Gastos", cG, sqlG, "tb_gasto",
                () -> dialogGasto(null), (row) -> dialogGasto(row)));

        String[] cS = {"Repasse", "Programa", "Valor Repasse", "Total Gastos", "Total Pgtos", "Saldo"};
        String sqlS = "SELECT mes_referencia,nome_programa,valor_repasse,total_gastos,total_pagamentos,saldo_disponivel FROM vw_saldo_repasse";
        DefaultTableModel mS = tableModel(cS, sqlS);
        JPanel saldoPanel = darkPanel(new BorderLayout(0, 10));
        saldoPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        saldoPanel.add(styledLabel("Saldo por Repasse", F_SUBTITLE, C_TEXT), BorderLayout.NORTH);
        saldoPanel.add(scrollTable(mS), BorderLayout.CENTER);
        ft.addTab("Saldo", getIcon("finance"), saldoPanel);

        p.add(ft);
        return p;
    }

    private void dialogRepasse(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Repasse" : "Novo Repasse");

        JComboBox<String> cbProg = new JComboBox<>();
        Map<String, Integer> progMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_programa,nome_programa FROM tb_programa_social");
            while (rs.next()) {
                progMap.put(rs.getString(2), rs.getInt(1));
                cbProg.addItem(rs.getString(2));
            }
        } catch (Exception ignored) {
        }
        styleCombo(cbProg);

        String[] nomes = {"Data (AAAA-MM-DD)", "Valor (R$)", "Mês Referência (AAAA-MM)", "Descrição"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{
                row[2].toString(), row[3].toString(), row[4].toString(), row[5] != null ? row[5].toString() : ""
        } : null);

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Programa:", cbProg);
        if (edit) cbProg.setSelectedItem(row[1]);

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ──────────────────────────────────────
            if (fs[0].getText().isBlank() || !fs[0].getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Data do repasse deve estar no formato AAAA-MM-DD (ex: 2025-03-05).",
                        "Data inválida", JOptionPane.WARNING_MESSAGE);
                fs[0].requestFocus();
                return;
            }
            try {
                double val = Double.parseDouble(fs[1].getText().replace(",", "."));
                if (val <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O Valor do repasse deve ser um número positivo (ex: 18000.00).\n"
                                + "Valor informado: '" + fs[1].getText() + "'",
                        "Valor inválido", JOptionPane.WARNING_MESSAGE);
                fs[1].requestFocus();
                return;
            }
            if (!fs[2].getText().matches("\\d{4}-\\d{2}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O Mês de Referência deve estar no formato AAAA-MM (ex: 2025-03).",
                        "Mês referência inválido", JOptionPane.WARNING_MESSAGE);
                fs[2].requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ───────────────────────────────
            try {
                int progId = progMap.getOrDefault(cbProg.getSelectedItem().toString(), 1);
                if (edit) {
                    update("UPDATE tb_repasse SET data_repasse=?,valor_repasse=?,mes_referencia=?,descricao=?,fk_programa=? WHERE pk_repasse=?",
                            fs[0].getText(), fs[1].getText(), fs[2].getText(), fs[3].getText(), progId, row[0]);
                } else {
                    update("INSERT INTO tb_repasse(fk_programa,data_repasse,valor_repasse,mes_referencia,descricao) VALUES(?,?,?,?,?)",
                            progId, fs[0].getText(), fs[1].getText(), fs[2].getText(), fs[3].getText());
                }
                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("uq_repasse"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Já existe um repasse cadastrado para este programa no mês '" + fs[2].getText() + "'.\nCada programa pode ter apenas um repasse por mês.",
                            "Repasse duplicado", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    private void dialogGasto(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Gasto" : "Novo Gasto");

        JComboBox<String> cbRepasse = new JComboBox<>();
        JComboBox<String> cbCat = new JComboBox<>();
        Map<String, Integer> repMap = new LinkedHashMap<>();
        Map<String, Integer> catMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_repasse,mes_referencia FROM tb_repasse ORDER BY data_repasse DESC");
            while (rs.next()) {
                repMap.put(rs.getString(2), rs.getInt(1));
                cbRepasse.addItem(rs.getString(2));
            }
            rs = query("SELECT pk_categoria,nome_categoria FROM tb_categoria_gastos ORDER BY nome_categoria");
            while (rs.next()) {
                catMap.put(rs.getString(2), rs.getInt(1));
                cbCat.addItem(rs.getString(2));
            }
        } catch (Exception ignored) {
        }
        styleCombo(cbRepasse);
        styleCombo(cbCat);
        if (edit) {
            cbRepasse.setSelectedItem(row[1]);
            cbCat.setSelectedItem(row[2]);
        }

        String[] nomes = {"Data (AAAA-MM-DD)", "Valor (R$)", "Descrição", "Nota Fiscal"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{
                row[3].toString(), row[4].toString(),
                row[5] != null ? row[5].toString() : "",
                row[6] != null ? row[6].toString() : ""
        } : null);

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Repasse:", cbRepasse);
        addFormRow(form, "Categoria:", cbCat);

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ──────────────────────────────────────
            if (fs[0].getText().isBlank() || !fs[0].getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Data do gasto deve estar no formato AAAA-MM-DD (ex: 2025-03-10).",
                        "Data inválida", JOptionPane.WARNING_MESSAGE);
                fs[0].requestFocus();
                return;
            }
            try {
                double val = Double.parseDouble(fs[1].getText().replace(",", "."));
                if (val <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O Valor do gasto deve ser um número positivo (ex: 1200.00).\n"
                                + "Valor informado: '" + fs[1].getText() + "'",
                        "Valor inválido", JOptionPane.WARNING_MESSAGE);
                fs[1].requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ───────────────────────────────
            try {
                int repId = repMap.getOrDefault(cbRepasse.getSelectedItem().toString(), 1);
                int catId = catMap.getOrDefault(cbCat.getSelectedItem().toString(), 1);
                String nf = fs[3].getText().isBlank() ? null : fs[3].getText();
                if (edit) {
                    update("UPDATE tb_gasto SET data_gasto=?,valor_gasto=?,descricao=?,nota_fiscal=?,fk_repasse=?,fk_categoria=? WHERE pk_gasto=?",
                            fs[0].getText(), fs[1].getText(), fs[2].getText(), nf, repId, catId, row[0]);
                } else {
                    update("INSERT INTO tb_gasto(fk_repasse,fk_categoria,data_gasto,valor_gasto,descricao,nota_fiscal) VALUES(?,?,?,?,?,?)",
                            repId, catId, fs[0].getText(), fs[1].getText(), fs[2].getText(), nf);
                }
                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("uq_gasto"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Esta Nota Fiscal já foi lançada para este repasse.\nCada nota fiscal só pode ser registrada uma vez por repasse.",
                            "Nota fiscal duplicada", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    // ── Alertas ──────────────────────────────────────────────────
    private JPanel buildAlertasTab() {
        String[] cols = {"ID", "Aluno", "Funcionário", "Tipo", "Nível", "Descrição", "Data", "Status"};
        String sql = "SELECT al.pk_alerta,a.nome_aluno,f.nome_funcionario,al.tipo_alerta,"
                + "al.nivel_risco,al.descricao_alerta,al.data_alerta,al.status_alerta "
                + "FROM tb_alerta al "
                + "JOIN tb_aluno a ON a.pk_aluno=al.fk_aluno "
                + "JOIN tb_funcionario f ON f.pk_funcionario=al.fk_funcionario ORDER BY al.data_alerta DESC";
        return crudPanel("Alertas", cols, sql, "tb_alerta",
                () -> dialogAlerta(null),
                (row) -> dialogAlerta(row)
        );
    }

    private void dialogAlerta(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Alerta" : "Novo Alerta");

        JComboBox<String> cbAluno = new JComboBox<>();
        JComboBox<String> cbFunc = new JComboBox<>();
        Map<String, Integer> aMap = new LinkedHashMap<>();
        Map<String, Integer> fMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_aluno,nome_aluno FROM tb_aluno ORDER BY nome_aluno");
            while (rs.next()) {
                aMap.put(rs.getString(2), rs.getInt(1));
                cbAluno.addItem(rs.getString(2));
            }
            rs = query("SELECT pk_funcionario,nome_funcionario FROM tb_funcionario ORDER BY nome_funcionario");
            while (rs.next()) {
                fMap.put(rs.getString(2), rs.getInt(1));
                cbFunc.addItem(rs.getString(2));
            }
        } catch (Exception ignored) {
        }
        styleCombo(cbAluno);
        styleCombo(cbFunc);

        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Frequencia Critica", "Vulnerabilidade", "Evasao Iminente"});
        JComboBox<String> cbNivel = new JComboBox<>(new String[]{"Baixo", "Medio", "Alto", "Critico"});
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Aberto", "Em Acompanhamento", "Resolvido"});
        styleCombo(cbTipo);
        styleCombo(cbNivel);
        styleCombo(cbStatus);

        String[] nomes = {"Descrição do Alerta", "Data (AAAA-MM-DD)"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{row[5].toString(), row[6].toString()} : null);

        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Aluno:", cbAluno);
        addFormRow(form, "Funcionário:", cbFunc);
        addFormRow(form, "Tipo:", cbTipo);
        addFormRow(form, "Nível de Risco:", cbNivel);
        addFormRow(form, "Status:", cbStatus);
        if (edit) {
            cbAluno.setSelectedItem(row[1]);
            cbFunc.setSelectedItem(row[2]);
            cbTipo.setSelectedItem(row[3]);
            cbNivel.setSelectedItem(row[4]);
            cbStatus.setSelectedItem(row[7]);
        }

        addSaveButton(d, () -> {
            // ── VALIDAÇÕES ──────────────────────────────────────
            if (fs[0].getText().isBlank()) {
                JOptionPane.showMessageDialog(d,
                        "⚠ O campo 'Descrição do Alerta' é obrigatório.",
                        "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                fs[0].requestFocus();
                return;
            }
            if (fs[1].getText().isBlank() || !fs[1].getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(d,
                        "⚠ A Data do alerta deve estar no formato AAAA-MM-DD (ex: 2025-03-19).",
                        "Data inválida", JOptionPane.WARNING_MESSAGE);
                fs[1].requestFocus();
                return;
            }
            // ── FIM DAS VALIDAÇÕES ───────────────────────────────
            try {
                int aId = aMap.getOrDefault(cbAluno.getSelectedItem().toString(), 0);
                int fId = fMap.getOrDefault(cbFunc.getSelectedItem().toString(), 0);
                if (edit) {
                    update("UPDATE tb_alerta SET descricao_alerta=?,data_alerta=?,tipo_alerta=?,nivel_risco=?,status_alerta=? WHERE pk_alerta=?",
                            fs[0].getText(), fs[1].getText(),
                            cbTipo.getSelectedItem().toString(),
                            cbNivel.getSelectedItem().toString(),
                            cbStatus.getSelectedItem().toString(),
                            row[0]);
                } else {
                    update("INSERT INTO tb_alerta(fk_aluno,fk_funcionario,tipo_alerta,nivel_risco,descricao_alerta,data_alerta) VALUES(?,?,?,?,?,?)",
                            aId, fId,
                            cbTipo.getSelectedItem().toString(),
                            cbNivel.getSelectedItem().toString(), // CORREÇÃO: estava sem .toString()
                            fs[0].getText(), fs[1].getText());
                }
                d.dispose();
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("uq_alerta"))
                    JOptionPane.showMessageDialog(d,
                            "✗ Já existe um alerta do tipo '" + cbTipo.getSelectedItem() + "' para este aluno nesta data.\nAltere a data ou o tipo de alerta.",
                            "Alerta duplicado", JOptionPane.ERROR_MESSAGE);
                else
                    showError(d, ex);
            }
        });
        d.setVisible(true);
    }

    // ============================================================
    // COMPONENTES UTILITÁRIOS
    // ============================================================

    @FunctionalInterface
    interface Action {
        void run();
    }

    @FunctionalInterface
    interface RowAct {
        void run(Object[] row);
    }

    private JPanel crudPanel(String title, String[] cols, String sql, String table, Action onCreate, RowAct onEdit) {
        JPanel p = darkPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel bar = darkPanel(new BorderLayout(12, 0));
        JLabel lbl = styledLabel(title, new Font("Segoe UI", Font.BOLD, 22), C_TEXT);

        JTextField search = styledField("Buscar...");
        search.setPreferredSize(new Dimension(280, 44));
        search.putClientProperty("JTextField.placeholderText", "Buscar...");

        JButton btnNew = accentButton("Novo", getIcon("plus"));
        JButton btnEdit = accentButton("Editar", getIcon("edit"));
        JButton btnDel = accentButton("Excluir", getIcon("delete"));
        btnEdit.setEnabled(false);
        btnDel.setEnabled(false);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(C_BG);
        btns.add(search);
        btns.add(btnNew);
        btns.add(btnEdit);
        btns.add(btnDel);
        bar.add(lbl, BorderLayout.WEST);
        bar.add(btns, BorderLayout.EAST);

        DefaultTableModel model = tableModel(cols, sql);
        JTable table2 = styledTable(model);
        JScrollPane scroll = new JScrollPane(table2);
        styleScroll(scroll);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table2.setRowSorter(sorter);
        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String t = search.getText().replace("Buscar...", "").trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t));
            }
        });

        table2.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table2.getSelectedRow() != -1;
            btnEdit.setEnabled(sel);
            btnDel.setEnabled(sel);
        });

        btnNew.addActionListener(e -> onCreate.run());
        btnEdit.addActionListener(e -> {
            int r = table2.convertRowIndexToModel(table2.getSelectedRow());
            if (r < 0) return;
            Object[] row = new Object[model.getColumnCount()];
            for (int i = 0; i < row.length; i++) row[i] = model.getValueAt(r, i);
            onEdit.run(row);
        });
        btnDel.addActionListener(e -> {
            int r = table2.convertRowIndexToModel(table2.getSelectedRow());
            if (r < 0) return;
            Object id = model.getValueAt(r, 0);
            int ok = JOptionPane.showConfirmDialog(this, "Excluir registro #" + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            String pk = "pk_" + table.replace("tb_", "");
            try {
                update("DELETE FROM " + table + " WHERE " + pk + "=?", id);
                refreshCurrentTab();
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.contains("foreign key constraint"))
                    JOptionPane.showMessageDialog(this,
                            "✗ Não é possível excluir este registro pois ele está vinculado a outros dados no sistema.\nRemova os vínculos antes de excluir.",
                            "Exclusão bloqueada", JOptionPane.ERROR_MESSAGE);
                else
                    showError(this, ex);
            }
        });

        p.add(bar, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        JLabel count = styledLabel("", F_BODY, C_MUTED);
        count.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        model.addTableModelListener(ev -> count.setText(model.getRowCount() + " registros"));
        count.setText(model.getRowCount() + " registros");
        p.add(count, BorderLayout.SOUTH);

        return p;
    }

    private JPanel kpiCard(String label, String value, String iconName, String colorHex) {
        Color color = Color.decode(colorHex);
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(C_CARD);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(20, 16, 20, 16)
        ));
        JLabel lVal = styledLabel(value, new Font("Segoe UI", Font.BOLD, 30), color);
        JLabel lLbl = styledLabel(label, new Font("Segoe UI", Font.BOLD, 16), C_MUTED);
        lVal.setOpaque(false);
        lLbl.setOpaque(false);
        card.add(lVal, BorderLayout.WEST);
        card.add(lLbl, BorderLayout.CENTER);
        return card;
    }

    private JFormattedTextField dateField(String valueYMD) {
        try {
            MaskFormatter mask = new MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
            JFormattedTextField f = new JFormattedTextField(mask);
            if (valueYMD != null && valueYMD.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] p = valueYMD.split("-");
                f.setText(p[2] + "/" + p[1] + "/" + p[0]);
            }
            styleField(f);
            return f;
        } catch (Exception e) {
            return new JFormattedTextField();
        }
    }

    private String toDBDate(String ddmmyyyy) {
        if (ddmmyyyy == null || ddmmyyyy.contains("_")) return "";
        String[] p = ddmmyyyy.split("/");
        if (p.length == 3) return p[2] + "-" + p[1] + "-" + p[0];
        return ddmmyyyy;
    }

    // ── Helpers de formulário ─────────────────────────────────────
    private JDialog formDialog(String title) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(520, 520);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(C_PANEL);
        d.setLayout(new BorderLayout());
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(C_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
        JScrollPane sp = new JScrollPane(form);
        sp.setBackground(C_PANEL);
        sp.getViewport().setBackground(C_PANEL);
        sp.setBorder(null);
        d.add(sp, BorderLayout.CENTER);
        return d;
    }

    private JTextField[] formFields(JDialog d, String[] labels, String[] vals) {
        JPanel form = (JPanel) ((JScrollPane) d.getContentPane().getComponent(0)).getViewport().getView();
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = styledField(vals != null ? vals[i] : "");
            addFormRow(form, labels[i] + ":", fields[i]);
        }
        return fields;
    }

    private void addFormRow(JPanel form, String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(C_PANEL);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        JLabel l = styledLabel(label, F_BODY, C_MUTED);
        l.setPreferredSize(new Dimension(165, 24));
        row.add(l, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        form.add(row);
    }

    private void addSaveButton(JDialog d, Action action) {
        JButton btn = accentButton("Salvar", getIcon("save"));
        btn.addActionListener(e -> {
            action.run();
            if (!d.isVisible()) {
                JOptionPane.showMessageDialog(this,
                        "Salvo com Sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        bot.setBackground(C_PANEL);
        JButton cancel = iconButton("Cancelar", getIcon("cancel"));
        cancel.addActionListener(e -> d.dispose());
        bot.add(cancel);
        bot.add(btn);
        d.add(bot, BorderLayout.SOUTH);
    }

    // ── Helpers de BD ─────────────────────────────────────────────
    private ResultSet query(String sql, Object... params) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
        return ps.executeQuery();
    }

    private void update(String sql, Object... params) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
        ps.executeUpdate();
        ps.close();
    }

    private String countQuery(String sql) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getString(1);
        } catch (Exception ignored) {
        }
        return "—";
    }

    private DefaultTableModel tableModel(String[] cols, String sql) {
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int nc = meta.getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[nc];
                for (int i = 0; i < nc; i++) row[i] = rs.getObject(i + 1);
                m.addRow(row);
            }
        } catch (Exception ignored) {
        }
        return m;
    }

    // ── Helpers visuais ───────────────────────────────────────────
    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(C_BG);
        p.setOpaque(true);
        return p;
    }

    private JLabel styledLabel(String t, Font f, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private JTextField styledField(String val) {
        JTextField f = new JTextField(val);
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setBackground(C_CARD);
        f.setForeground(C_TEXT);
        f.setCaretColor(C_TEXT);
        f.setFont(F_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setPreferredSize(new Dimension(0, 36));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(C_CARD);
        cb.setForeground(C_TEXT);
        cb.setFont(F_BODY);
        cb.setBorder(new LineBorder(C_BORDER, 1, true));
        cb.setPreferredSize(new Dimension(0, 36));
    }

    private JButton accentButton(String text, ImageIcon icon) {
        JButton b = new JButton(text, icon);
        b.setBackground(C_ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(C_ACCENT.brighter());
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(C_ACCENT);
            }
        });
        return b;
    }

    private JButton iconButton(String text, ImageIcon icon) {
        JButton b = new JButton(text, icon);
        b.setBackground(C_CARD);
        b.setForeground(C_TEXT);
        b.setFont(F_BODY);
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(false);
        return b;
    }

    private JTable styledTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setBackground(C_TABLE_R1);
        t.setForeground(C_TEXT);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setRowHeight(42);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(C_SEL);
        t.setSelectionForeground(C_TEXT);
        t.getTableHeader().setBackground(C_TABLE_H);
        t.getTableHeader().setForeground(C_TEXT);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, C_BORDER));
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setBackground(sel ? C_SEL : (r % 2 == 0 ? C_TABLE_R1 : C_TABLE_R2));
                setForeground(C_TEXT);
                setFont(new Font("Segoe UI", Font.BOLD, 15));
                return this;
            }
        });
        return t;
    }

    private JScrollPane scrollTable(DefaultTableModel m) {
        JScrollPane sp = new JScrollPane(styledTable(m));
        styleScroll(sp);
        return sp;
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBackground(C_BG);
        sp.getViewport().setBackground(C_TABLE_R1);
        sp.setBorder(new LineBorder(C_BORDER, 1));
        sp.getVerticalScrollBar().setBackground(C_CARD);
        sp.getHorizontalScrollBar().setBackground(C_CARD);
    }

    private void styleTabs(JTabbedPane tp) {
        UIManager.put("TabbedPane.selected", C_CARD);
        UIManager.put("TabbedPane.selectColor", C_CARD);
        UIManager.put("TabbedPane.tabAreaBackground", C_PANEL);
        UIManager.put("TabbedPane.background", C_PANEL);
        UIManager.put("TabbedPane.foreground", C_TEXT);
        UIManager.put("TabbedPane.selectedForeground", C_TEXT);
        UIManager.put("TabbedPane.focus", C_ACCENT);
        UIManager.put("TabbedPane.highlight", C_CARD);
        UIManager.put("TabbedPane.darkShadow", C_BORDER);
        UIManager.put("TabbedPane.shadow", C_BORDER);
        UIManager.put("TabbedPane.light", C_CARD);
        UIManager.put("TabbedPane.tabInsets", new Insets(26, 45, 26, 45));
        tp.setBackground(C_PANEL);
        tp.setForeground(C_TEXT);
        tp.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tp.updateUI();
    }

    private void showError(Component parent, Exception ex) {
        String msg = ex.getMessage();
        if (msg != null && msg.length() > 200) msg = msg.substring(0, 200) + "...";
        JOptionPane.showMessageDialog(parent, "Erro: " + msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void refreshCurrentTab() {
        int idx = tabs.getSelectedIndex();
        JPanel novo;
        switch (idx) {
            case 0:
                novo = buildDashboard();
                break;
            case 1:
                novo = buildAlunosTab();
                break;
            case 2:
                novo = buildFuncionariosTab();
                break;
            case 3:
                novo = buildTurmasTab();
                break;
            case 4:
                novo = buildMatriculasTab();
                break;
            case 5:
                novo = buildFrequenciaTab();
                break;
            case 6:
                novo = buildResponsaveisTab();
                break;
            case 7:
                novo = buildFinanceiroTab();
                break;
            case 8:
                novo = buildAlertasTab();
                break;
            default:
                return;
        }
        tabs.setComponentAt(idx, novo);
        tabs.revalidate();
        tabs.repaint();
    }


    private void exportarSistema() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar relatório do sistema");
        fc.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        String nomePadrao = "SisGESC_Relatorio_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".pdf";
        fc.setSelectedFile(new java.io.File(nomePadrao));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String caminho = fc.getSelectedFile().getAbsolutePath();
        if (!caminho.toLowerCase().endsWith(".pdf")) caminho += ".pdf";

        try {
            gerarPDF(caminho);
            JOptionPane.showMessageDialog(this,
                    "PDF gerado com sucesso!\n" + caminho,
                    "Salvar Sistema", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar PDF:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void gerarPDF(String caminho) throws Exception {
        Document doc = new Document(PageSize.A4, 40, 40, 60, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(caminho));
        Class<com.itextpdf.text.Font> PdfFont = com.itextpdf.text.Font.class;

        // ── Cores do sistema ──────────────────────────────────────
        BaseColor COR_ACCENT = new BaseColor(99, 102, 241);
        BaseColor COR_HEADER = new BaseColor(28, 28, 38);
        BaseColor COR_ROW1 = new BaseColor(32, 32, 46);
        BaseColor COR_ROW2 = new BaseColor(36, 36, 50);
        BaseColor COR_TEXT = new BaseColor(226, 232, 240);
        BaseColor COR_SUCCESS = new BaseColor(34, 197, 94);
        BaseColor COR_DANGER = new BaseColor(239, 68, 68);
        BaseColor COR_WARNING = new BaseColor(251, 191, 36);
        BaseColor COR_MUTED = new BaseColor(100, 116, 139);
        BaseColor COR_BORDER = new BaseColor(51, 51, 72);

        // ── Fontes ────────────────────────────────────────────────
        com.itextpdf.text.Font fTitulo    = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 26, com.itextpdf.text.Font.BOLD,  COR_TEXT);
        com.itextpdf.text.Font fSubtitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL, COR_MUTED);
        com.itextpdf.text.Font fSecao     = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD,  COR_ACCENT);
        com.itextpdf.text.Font fColHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9,  com.itextpdf.text.Font.BOLD,  COR_TEXT);
        com.itextpdf.text.Font fCell      = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8,  com.itextpdf.text.Font.NORMAL, COR_TEXT);
        com.itextpdf.text.Font fCellBold  = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8,  com.itextpdf.text.Font.BOLD,  COR_TEXT);
        com.itextpdf.text.Font fKpiVal    = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD,  COR_ACCENT);
        com.itextpdf.text.Font fKpiLabel  = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8,  com.itextpdf.text.Font.NORMAL, COR_MUTED);
        com.itextpdf.text.Font fRodape    = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 7,  com.itextpdf.text.Font.NORMAL, COR_MUTED);
        com.itextpdf.text.Font fSub2      = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 13, com.itextpdf.text.Font.NORMAL, COR_MUTED);
        com.itextpdf.text.Font fDataCapa  = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL, COR_MUTED);

        String agora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // ── Numeração de páginas ──────────────────────────────────
        PdfPageEventHelper eventos = new PdfPageEventHelper() {
            public void onEndPage(PdfWriter w, Document d) {
                PdfContentByte cb = w.getDirectContent();
                // Linha rodapé
                cb.setColorStroke(new BaseColor(77, 77, 77));
                cb.setLineWidth(0.5f);
                cb.moveTo(40, 42);
                cb.lineTo(555, 42);
                cb.stroke();
                // Texto rodapé
                Phrase rodape = new Phrase(
                        "SisGESC — CCA Bom Jesus do Cangaíba  |  Gerado em: " + agora
                                + "  |  Pág. " + w.getPageNumber(),
                        fRodape);
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, rodape, 297, 30, 0);
            }
        };
        writer.setPageEvent(eventos);

        doc.open();

        // ============================================================
        // CAPA
        // ============================================================
        PdfContentByte cbCapa = writer.getDirectContent();
        cbCapa.setColorFill(COR_HEADER);
        cbCapa.rectangle(0, PageSize.A4.getHeight() - 200, PageSize.A4.getWidth(), 200);
        cbCapa.fill();

        doc.add(new Paragraph("\n\n\n\n"));
        Paragraph titulo = new Paragraph("SisGESC", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph inst = new Paragraph("Sistema de Gestão Educacional e Social", fSub2);
        inst.setAlignment(Element.ALIGN_CENTER);
        doc.add(inst);

        Paragraph cca = new Paragraph("CCA Bom Jesus do Cangaíba", fSub2);
        cca.setAlignment(Element.ALIGN_CENTER);
        doc.add(cca);

        doc.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n\n\n"));

        Paragraph dataCapa = new Paragraph("Relatório completo gerado em: " + agora, fDataCapa);
        dataCapa.setAlignment(Element.ALIGN_CENTER);
        doc.add(dataCapa);

        doc.newPage();

        // ============================================================
        // Método auxiliar interno — cria cabeçalho de seção
        // ============================================================
        // (usaremos lambdas locais para reutilizar)

        // ============================================================
        // SEÇÃO 1 — RESUMO EXECUTIVO (KPIs)
        // ============================================================
        Paragraph secResumo = new Paragraph("Resumo Executivo", fSecao);
        secResumo.setSpacingBefore(8);
        secResumo.setSpacingAfter(10);
        doc.add(secResumo);

        String[][] kpis = {
                {"Alunos Ativos", countQuery("SELECT COUNT(*) FROM tb_matricula WHERE situacao_matricula='ativa'")},
                {"Funcionários Ativos", countQuery("SELECT COUNT(*) FROM tb_funcionario WHERE status_funcionario='ativo'")},
                {"Turmas Ativas", countQuery("SELECT COUNT(*) FROM tb_turma WHERE status_turma='ativa'")},
                {"Lista de Espera", countQuery("SELECT COUNT(*) FROM tb_lista_espera WHERE status_espera='aguardando'")},
                {"Alertas Abertos", countQuery("SELECT COUNT(*) FROM tb_alerta WHERE status_alerta='Aberto'")},
                {"Total de Responsáveis", countQuery("SELECT COUNT(*) FROM tb_responsavel")},
                {"Repasses (mês atual)", "R$ " + countQuery("SELECT IFNULL(SUM(valor_repasse),0) FROM tb_repasse WHERE mes_referencia=DATE_FORMAT(NOW(),'%Y-%m')")},
                {"Total Gastos", "R$ " + countQuery("SELECT IFNULL(SUM(valor_gasto),0) FROM tb_gasto")},
        };

        PdfPTable tKpi = new PdfPTable(4);
        tKpi.setWidthPercentage(100);
        tKpi.setSpacingAfter(16);
        for (String[] kpi : kpis) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(COR_ROW2);
            cell.setBorderColor(COR_BORDER);
            cell.setPadding(10);
            cell.addElement(new Paragraph(kpi[1], fKpiVal));
            cell.addElement(new Paragraph(kpi[0], fKpiLabel));
            tKpi.addCell(cell);
        }
        doc.add(tKpi);

        // ============================================================
        // SEÇÃO 2 — ALUNOS
        // ============================================================
        doc.add(new Paragraph("Alunos", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsAluno = {"ID", "Código", "Nome", "NIS", "CPF", "Sexo", "Nascimento", "Idade", "Raça/Cor", "Situação"};
        PdfPTable tAluno = criarTabela(colsAluno, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query("SELECT pk_aluno,codigo_aluno,nome_aluno,nis_aluno,cpf_aluno,sexo,"
                    + "data_nascimento,idade,raca_cor,situacao_aluno FROM vw_aluno ORDER BY nome_aluno");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                for (int i = 1; i <= 10; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "-", fCell));
                    c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tAluno.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tAluno);

        // ============================================================
        // SEÇÃO 3 — FUNCIONÁRIOS
        // ============================================================
        doc.newPage();
        doc.add(new Paragraph("Funcionários", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsFunc = {"ID", "CPF", "Nome", "Cargo", "Admissão", "Vínculo", "Salário", "C.H.", "Status"};
        PdfPTable tFunc = criarTabela(colsFunc, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query("SELECT f.pk_funcionario,f.cpf_funcionario,f.nome_funcionario,c.nome_cargo,"
                    + "f.data_admissao,f.tipo_vinculo,f.salario,f.carga_horaria_semanal,f.status_funcionario "
                    + "FROM tb_funcionario f JOIN tb_cargo c ON c.pk_cargo=f.fk_cargo ORDER BY f.nome_funcionario");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                for (int i = 1; i <= 9; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "-", fCell));
                    c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tFunc.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tFunc);

        // ============================================================
        // SEÇÃO 4 — TURMAS E OCUPAÇÃO
        // ============================================================
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Turmas e Ocupação", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsTurma = {"ID", "Nome", "Turno", "Faixa Etária", "Capacidade", "Matriculados", "Vagas", "Em Espera", "Status"};
        PdfPTable tTurma = criarTabela(colsTurma, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query("SELECT pk_turma,nome_turma,turno,"
                    + "CONCAT(faixa_etaria_inicio,' - ',faixa_etaria_fim,' anos'),"
                    + "capacidade_max,alunos_matriculados,vagas_disponiveis,alunos_em_espera,status_turma "
                    + "FROM vw_ocupacao_turmas ORDER BY nome_turma");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                for (int i = 1; i <= 9; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "-", fCell));
                    // Destaca turmas sem vagas
                    if (i == 7 && "0".equals(val)) c.setBackgroundColor(new BaseColor(120, 30, 30));
                    else c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tTurma.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tTurma);

        // ============================================================
        // SEÇÃO 5 — MATRÍCULAS
        // ============================================================
        doc.newPage();
        doc.add(new Paragraph("Matrículas", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsMat = {"ID", "Aluno", "Turma", "Data Matrícula", "Situação"};
        PdfPTable tMat = criarTabela(colsMat, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query("SELECT m.pk_matricula,a.nome_aluno,t.nome_turma,"
                    + "m.data_matricula,m.situacao_matricula "
                    + "FROM tb_matricula m JOIN tb_aluno a ON a.pk_aluno=m.fk_aluno "
                    + "JOIN tb_turma t ON t.pk_turma=m.fk_turma ORDER BY a.nome_aluno");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                for (int i = 1; i <= 5; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "-", fCell));
                    if (i == 5) {
                        if ("ativa".equals(val)) c.setBackgroundColor(new BaseColor(20, 80, 40));
                        else if ("cancelada".equals(val)) c.setBackgroundColor(new BaseColor(100, 20, 20));
                        else c.setBackgroundColor(bg);
                    } else c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tMat.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tMat);

        // ============================================================
        // SEÇÃO 6 — FREQUÊNCIA (resumo por aluno)
        // ============================================================
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Frequência — Resumo por Aluno", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsFreq = {"Aluno", "Turma", "Total Aulas", "Presenças", "Faltas", "% Presença"};
        PdfPTable tFreq = criarTabela(colsFreq, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query(
                    "SELECT a.nome_aluno, t.nome_turma, "
                            + "COUNT(f.pk_frequencia) AS total, "
                            + "SUM(f.presente) AS presencas, "
                            + "COUNT(f.pk_frequencia) - SUM(f.presente) AS faltas, "
                            + "ROUND(SUM(f.presente)/COUNT(f.pk_frequencia)*100,1) AS pct "
                            + "FROM tb_frequencia f "
                            + "JOIN tb_matricula m ON m.pk_matricula=f.fk_matricula "
                            + "JOIN tb_aluno a ON a.pk_aluno=m.fk_aluno "
                            + "JOIN tb_turma t ON t.pk_turma=m.fk_turma "
                            + "GROUP BY a.pk_aluno, t.pk_turma ORDER BY a.nome_aluno");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                String[] vals = {
                        rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6) + "%"
                };
                for (int i = 0; i < vals.length; i++) {
                    String val = vals[i] != null ? vals[i] : "-";
                    PdfPCell c = new PdfPCell(new Phrase(val, fCell));
                    // Destaca % abaixo de 75%
                    if (i == 5) {
                        try {
                            double pct = Double.parseDouble(val.replace("%", ""));
                            if (pct < 75) c.setBackgroundColor(new BaseColor(120, 30, 30));
                            else if (pct < 85) c.setBackgroundColor(new BaseColor(100, 80, 10));
                            else c.setBackgroundColor(new BaseColor(20, 80, 40));
                        } catch (Exception e2) {
                            c.setBackgroundColor(bg);
                        }
                    } else c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tFreq.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tFreq);

        // ============================================================
        // SEÇÃO 7 — RESPONSÁVEIS
        // ============================================================
        doc.newPage();
        doc.add(new Paragraph("Responsáveis", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsResp = {"ID", "CPF", "Nome", "Telefone", "E-mail", "Endereço", "Cidade", "UF", "CEP"};
        PdfPTable tResp = criarTabela(colsResp, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query(
                    "SELECT r.pk_responsavel, r.cpf_responsavel, r.nome_responsavel, "
                            + "MAX(CASE WHEN cr.tipo_contato='telefone' THEN cr.valor_contato END) AS telefone, "
                            + "MAX(CASE WHEN cr.tipo_contato='email'    THEN cr.valor_contato END) AS email, "
                            + "IFNULL(e.logradouro,'—') AS logradouro, "
                            + "IFNULL(e.cidade,'—') AS cidade, "
                            + "IFNULL(e.uf,'—') AS uf, "
                            + "IFNULL(e.cep,'—') AS cep "
                            + "FROM tb_responsavel r "
                            + "LEFT JOIN tb_contato_responsavel cr ON cr.fk_responsavel=r.pk_responsavel "
                            + "LEFT JOIN tb_endereco_responsavel e ON e.fk_responsavel=r.pk_responsavel "
                            + "GROUP BY r.pk_responsavel ORDER BY r.nome_responsavel");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                for (int i = 1; i <= 9; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "—", fCell));
                    c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tResp.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tResp);

        // ============================================================
        // SEÇÃO 8 — FINANCEIRO
        // ============================================================
        doc.newPage();
        doc.add(new Paragraph("Financeiro — Repasses", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsRep = {"ID", "Programa", "Data", "Valor (R$)", "Mês Ref.", "Descrição"};
        PdfPTable tRep = criarTabela(colsRep, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query("SELECT r.pk_repasse,p.nome_programa,r.data_repasse,"
                    + "r.valor_repasse,r.mes_referencia,IFNULL(r.descricao,'—') "
                    + "FROM tb_repasse r JOIN tb_programa_social p ON p.pk_programa=r.fk_programa "
                    + "ORDER BY r.data_repasse DESC");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                for (int i = 1; i <= 6; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "—", fCell));
                    c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tRep.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tRep);

        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Financeiro — Gastos", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsGasto = {"ID", "Mês Repasse", "Categoria", "Data", "Valor (R$)", "Descrição", "Nota Fiscal"};
        PdfPTable tGasto = criarTabela(colsGasto, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query("SELECT g.pk_gasto,r.mes_referencia,c.nome_categoria,"
                    + "g.data_gasto,g.valor_gasto,IFNULL(g.descricao,'—'),IFNULL(g.nota_fiscal,'—') "
                    + "FROM tb_gasto g JOIN tb_repasse r ON r.pk_repasse=g.fk_repasse "
                    + "JOIN tb_categoria_gastos c ON c.pk_categoria=g.fk_categoria "
                    + "ORDER BY g.data_gasto DESC");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                for (int i = 1; i <= 7; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "—", fCell));
                    c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tGasto.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tGasto);

        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Financeiro — Saldo por Repasse", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsSaldo = {"Mês Ref.", "Programa", "Valor Repasse", "Total Gastos", "Total Pgtos", "Saldo"};
        PdfPTable tSaldo = criarTabela(colsSaldo, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query("SELECT mes_referencia,nome_programa,valor_repasse,"
                    + "total_gastos,total_pagamentos,saldo_disponivel FROM vw_saldo_repasse");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                for (int i = 1; i <= 6; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "—", fCell));
                    // Destaca saldo negativo em vermelho
                    if (i == 6) {
                        try {
                            double saldo = Double.parseDouble(val.replace(",", "."));
                            if (saldo < 0) c.setBackgroundColor(new BaseColor(120, 30, 30));
                            else c.setBackgroundColor(new BaseColor(20, 80, 40));
                        } catch (Exception e2) {
                            c.setBackgroundColor(bg);
                        }
                    } else c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tSaldo.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tSaldo);

        // ============================================================
        // SEÇÃO 9 — ALERTAS
        // ============================================================
        doc.newPage();
        doc.add(new Paragraph("Alertas", fSecao));
        doc.add(new Paragraph(" "));

        String[] colsAlerta = {"ID", "Aluno", "Funcionário", "Tipo", "Nível", "Descrição", "Data", "Status"};
        PdfPTable tAlerta = criarTabela(colsAlerta, COR_ACCENT, COR_HEADER, COR_ROW1, COR_ROW2, COR_BORDER, fColHeader, fCell);
        try {
            ResultSet rs = query("SELECT al.pk_alerta,a.nome_aluno,f.nome_funcionario,"
                    + "al.tipo_alerta,al.nivel_risco,al.descricao_alerta,al.data_alerta,al.status_alerta "
                    + "FROM tb_alerta al "
                    + "JOIN tb_aluno a ON a.pk_aluno=al.fk_aluno "
                    + "JOIN tb_funcionario f ON f.pk_funcionario=al.fk_funcionario "
                    + "ORDER BY al.data_alerta DESC");
            boolean alt = false;
            while (rs.next()) {
                BaseColor bg = alt ? COR_ROW2 : COR_ROW1;
                String nivel = rs.getString(5);
                for (int i = 1; i <= 8; i++) {
                    String val = rs.getString(i);
                    PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "—", fCell));
                    if (i == 5) {
                        if ("Critico".equals(nivel)) c.setBackgroundColor(new BaseColor(120, 10, 10));
                        else if ("Alto".equals(nivel)) c.setBackgroundColor(new BaseColor(120, 50, 10));
                        else if ("Medio".equals(nivel)) c.setBackgroundColor(new BaseColor(90, 70, 10));
                        else c.setBackgroundColor(bg);
                    } else c.setBackgroundColor(bg);
                    c.setBorderColor(COR_BORDER);
                    c.setPadding(4);
                    tAlerta.addCell(c);
                }
                alt = !alt;
            }
        } catch (Exception ignored) {
        }
        doc.add(tAlerta);

        doc.close();
    }

    // ── Helper: cria tabela estilizada ───────────────────────────────
    private PdfPTable criarTabela(String[] colunas,
                                  BaseColor corAccent, BaseColor corHeader,
                                  BaseColor corRow1,   BaseColor corRow2,
                                  BaseColor corBorder,
                                  com.itextpdf.text.Font fColHeader,
                                  com.itextpdf.text.Font fCell) throws Exception {

        PdfPTable t = new PdfPTable(colunas.length);
        t.setWidthPercentage(100);
        t.setSpacingAfter(10);
        t.setHeaderRows(1);

        for (String col : colunas) {
            PdfPCell h = new PdfPCell(new Phrase(col, fColHeader));
            h.setBackgroundColor(corHeader);
            h.setBorderColor(corAccent);
            h.setBorderWidth(1f);
            h.setPadding(5);
            h.setHorizontalAlignment(Element.ALIGN_LEFT);
            t.addCell(h);
        }
        return t;
    }
}