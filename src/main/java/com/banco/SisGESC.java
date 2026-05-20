package com.banco;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.ImageIcon;

public class SisGESC extends JFrame {

    // ── Conexão ──────────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/sisgesc_publico_nota?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "NovaSenha123";   // ← altere se necessário

    private Connection conn;

    // ── Cores ────────────────────────────────────────────────────
    private static final Color C_BG       = new Color(18, 18, 24);
    private static final Color C_PANEL    = new Color(28, 28, 38);
    private static final Color C_CARD     = new Color(36, 36, 50);
    private static final Color C_ACCENT   = new Color(99, 102, 241);
    private static final Color C_ACCENT2  = new Color(139, 92, 246);
    private static final Color C_SUCCESS  = new Color(34, 197, 94);
    private static final Color C_DANGER   = new Color(239, 68, 68);
    private static final Color C_WARNING  = new Color(251, 191, 36);
    private static final Color C_TEXT     = new Color(226, 232, 240);
    private static final Color C_MUTED    = new Color(100, 116, 139);
    private static final Color C_BORDER   = new Color(51, 51, 72);
    private static final Color C_TABLE_H  = new Color(45, 45, 65);
    private static final Color C_TABLE_R1 = new Color(32, 32, 46);
    private static final Color C_TABLE_R2 = new Color(28, 28, 40);
    private static final Color C_SEL      = new Color(99, 102, 241, 60);

    private static final Font  F_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font  F_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font  F_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  F_MONO     = new Font("Consolas",  Font.PLAIN, 12);

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
                // Tenta carregar do diretório "ICON" (ajuste o caminho se necessário)
                java.net.URL imgURL = getClass().getResource("/ICON/" + name + ".png");
                if (imgURL != null) {
                    ImageIcon originalIcon = new ImageIcon(imgURL);
                    Image image = originalIcon.getImage();
                    Image scaledImage = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    icons.put(name, new ImageIcon(scaledImage));
                } else {
                    // Fallback: tenta carregar do sistema de arquivos (src/ICON)
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
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(6,4,6,4);

        JLabel title = styledLabel("Configuração de Banco de Dados", F_SUBTITLE, C_TEXT);
        gc.gridx=0; gc.gridy=0; gc.gridwidth=2; form.add(title, gc);
        gc.gridwidth=1;

        JTextField fHost = styledField("localhost");
        JTextField fPort = styledField("3306");
        JTextField fDb   = styledField("sisgesc_publico_nota");
        JTextField fUser = styledField("root");
        JPasswordField fPass = new JPasswordField("");
        styleField(fPass);

        String[] labels = {"Host:","Porta:","Database:","Usuário:","Senha:"};
        JComponent[] fields = {fHost, fPort, fDb, fUser, fPass};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx=0; gc.gridy=i+1; gc.weightx=0.3;
            form.add(styledLabel(labels[i], F_BODY, C_MUTED), gc);
            gc.gridx=1; gc.weightx=0.7;
            form.add(fields[i], gc);
        }

        JButton btnConn = accentButton("  Conectar", getIcon("connect"));
        JLabel  status  = styledLabel("", F_BODY, C_DANGER);
        btnConn.addActionListener(e -> {
            try {
                String url = "jdbc:mysql://"+fHost.getText()+":"+fPort.getText()
                        +"/"+fDb.getText()+"?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url, fUser.getText(), new String(fPass.getPassword()));
                d.dispose();
            } catch (Exception ex) {
                status.setText("✗ " + ex.getMessage().split("\n")[0]);
            }
        });

        JPanel bot = new JPanel(new BorderLayout(8,0));
        bot.setBackground(C_PANEL);
        bot.setBorder(BorderFactory.createEmptyBorder(8,32,20,32));
        bot.add(status,  BorderLayout.CENTER);
        bot.add(btnConn, BorderLayout.EAST);

        d.add(form, BorderLayout.CENTER);
        d.add(bot,  BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ── UI Principal ─────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0,0,1,0,C_BORDER),
                BorderFactory.createEmptyBorder(14,24,14,24)
        ));
        JLabel logo = styledLabel(" SisGESC", F_TITLE, C_TEXT);
        logo.setIcon(getIcon("logo"));
        JLabel sub  = styledLabel("Sistema de Gestão Educacional e Social · CCA Bom Jesus do Cangaíba", F_BODY, C_MUTED);
        JPanel logoBox = new JPanel(new GridLayout(2,1,0,2));
        logoBox.setBackground(C_PANEL);
        logoBox.add(logo); logoBox.add(sub);

        JButton btnRefresh = accentButton("Atualizar", getIcon("refresh"));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        btnRefresh.addActionListener(e -> {
            refreshCurrentTab();
            JOptionPane.showMessageDialog(
                    this,
                    "Atualizado com Sucesso!",
                    "Atualizar",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnBox.setBackground(C_PANEL);
        btnBox.add(btnRefresh);

        header.add(logoBox, BorderLayout.WEST);
        header.add(btnBox,  BorderLayout.EAST);

        // Tabs
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
        add(tabs,   BorderLayout.CENTER);
    }

    // ── Dashboard ────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel p = darkPanel(new BorderLayout(0,20));
        p.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        JLabel t = styledLabel("Painel de Controle", F_TITLE, C_TEXT);
        p.add(t, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2,4,16,16));
        cards.setBackground(C_BG);
        cards.setPreferredSize(new Dimension(0, 330));

        String[][] kpis = {
                {"Alunos Ativos",        countQuery("SELECT COUNT(*) FROM tb_matricula WHERE situacao_matricula='ativa'"), "students", "#6366f1"},
                {"Funcionários Ativos",  countQuery("SELECT COUNT(*) FROM tb_funcionario WHERE status_funcionario='ativo'"), "staff", "#8b5cf6"},
                {"Turmas Ativas",        countQuery("SELECT COUNT(*) FROM tb_turma WHERE status_turma='ativa'"), "classes", "#06b6d4"},
                {"Lista de Espera",      countQuery("SELECT COUNT(*) FROM tb_lista_espera WHERE status_espera='aguardando'"), "enrollment", "#f59e0b"},
                {"Alertas Abertos",      countQuery("SELECT COUNT(*) FROM tb_alerta WHERE status_alerta='Aberto'"), "alerts", "#ef4444"},
                {"Repasses (mês atual)", countQuery("SELECT IFNULL(SUM(valor_repasse),0) FROM tb_repasse WHERE mes_referencia=DATE_FORMAT(NOW(),'%Y-%m')"), "finance", "#22c55e"},
                {"Total Gastos",         countQuery("SELECT IFNULL(SUM(valor_gasto),0) FROM tb_gasto"), "finance", "#f97316"},
                {"Faltas (este mês)",    countQuery("SELECT COUNT(*) FROM tb_frequencia WHERE presente=0 AND MONTH(data_aula)=MONTH(NOW())"), "attendance", "#94a3b8"},
        };

        for (String[] kpi : kpis) cards.add(kpiCard(kpi[0], kpi[1], kpi[2], kpi[3]));

        // Tabela de ocupação das turmas
        JPanel lower = darkPanel(new BorderLayout(0,10));
        JLabel tOcup = styledLabel("Ocupação das Turmas em Tempo Real", F_SUBTITLE, C_TEXT);
        lower.add(tOcup, BorderLayout.NORTH);

        String[] cols = {"Turma","Turno","Faixa Etária","Capacidade","Matriculados","Vagas","Em Espera"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        try {
            ResultSet rs = query("SELECT nome_turma,turno,faixa_etaria_inicio,faixa_etaria_fim,"
                    +"capacidade_max,alunos_matriculados,vagas_disponiveis,alunos_em_espera FROM vw_ocupacao_turmas");
            while (rs.next()) model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2),
                    rs.getInt(3)+" - "+rs.getInt(4)+" anos",
                    rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8)
            });
        } catch (Exception ignored) {}

        JScrollPane sp = scrollTable(model);
        JTable tabelaTurmas = (JTable) sp.getViewport().getView();
        tabelaTurmas.setRowHeight(50); // ← ajuste o valor aqui
        lower.add(sp, BorderLayout.CENTER);

        p.add(cards, BorderLayout.CENTER);
        p.add(lower, BorderLayout.SOUTH);
        return p;
    }

    // ── Alunos ───────────────────────────────────────────────────
    private JPanel buildAlunosTab() {
        String[] cols = {"ID","Código","Nome","NIS","CPF","Sexo","Nasc.","Idade","Raça/Cor","Situação"};
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

        String[] nomes = {"Nome Completo","NIS (11 dígitos)","CPF (11 dígitos)","Data de Nascimento (AAAA-MM-DD)"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{
                row[2].toString(), row[3].toString(), row[4].toString(), row[6].toString()
        } : null);

        JComboBox<String> cbSexo = new JComboBox<>(new String[]{"Masculino","Feminino"});
        JComboBox<String> cbRaca = new JComboBox<>(new String[]{"Branca","Preta","Parda","Amarela","Indigena","Nao declarada"});
        JComboBox<String> cbSit  = new JComboBox<>(new String[]{"ativo","inativo","transferido","concluido"});
        styleCombo(cbSexo); styleCombo(cbRaca); styleCombo(cbSit);
        if (edit) { cbSexo.setSelectedItem(row[5]); cbRaca.setSelectedItem(row[8]); cbSit.setSelectedItem(row[9]); }

        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Sexo:", cbSexo);
        addFormRow(form, "Raça/Cor:", cbRaca);
        if (edit) addFormRow(form, "Situação:", cbSit);

        addSaveButton(d, () -> {
            try {
                if (edit) {
                    update("UPDATE tb_aluno SET nome_aluno=?,nis_aluno=?,cpf_aluno=?,data_nascimento=?,"
                                    +"sexo=?,raca_cor=?,situacao_aluno=? WHERE pk_aluno=?",
                            fs[0].getText(), fs[1].getText(), fs[2].getText(), fs[3].getText(),
                            cbSexo.getSelectedItem(), cbRaca.getSelectedItem(), cbSit.getSelectedItem(), row[0]);
                } else {
                    update("INSERT INTO tb_aluno(nome_aluno,nis_aluno,cpf_aluno,data_nascimento,sexo,raca_cor) VALUES(?,?,?,?,?,?)",
                            fs[0].getText(), fs[1].getText(), fs[2].getText(), fs[3].getText(),
                            cbSexo.getSelectedItem(), cbRaca.getSelectedItem());
                }
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    // ── Funcionários ─────────────────────────────────────────────
    private JPanel buildFuncionariosTab() {
        String[] cols = {"ID","CPF","Nome","Cargo","Admissão","Vínculo","Salário","C.H.","Status"};
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
        Map<String,Integer> cargoMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_cargo,nome_cargo FROM tb_cargo");
            while(rs.next()) { cargoMap.put(rs.getString(2), rs.getInt(1)); cbCargo.addItem(rs.getString(2)); }
        } catch(Exception ignored){}
        styleCombo(cbCargo);

        JComboBox<String> cbVinculo = new JComboBox<>(new String[]{"CLT","Estagiario","Voluntario","Terceirizado"});
        JComboBox<String> cbStatus  = new JComboBox<>(new String[]{"ativo","inativo","ferias","afastado"});
        styleCombo(cbVinculo); styleCombo(cbStatus);

        String[] nomes = {"CPF (11 dígitos)","Nome Completo","Data Admissão (AAAA-MM-DD)","Salário","Carga Horária Semanal"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{
                row[1].toString(), row[2].toString(), row[4].toString(), row[6].toString(), row[7].toString()
        } : null);

        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Cargo:", cbCargo);
        addFormRow(form, "Vínculo:", cbVinculo);
        addFormRow(form, "Status:", cbStatus);
        if (edit) { cbCargo.setSelectedItem(row[3]); cbVinculo.setSelectedItem(row[5]); cbStatus.setSelectedItem(row[8]); }

        addSaveButton(d, () -> {
            try {
                int cargoId = cargoMap.getOrDefault(cbCargo.getSelectedItem().toString(), 1);
                if (edit) {
                    update("UPDATE tb_funcionario SET cpf_funcionario=?,nome_funcionario=?,data_admissao=?,"
                                    +"salario=?,carga_horaria_semanal=?,tipo_vinculo=?,status_funcionario=?,fk_cargo=? WHERE pk_funcionario=?",
                            fs[0].getText(), fs[1].getText(), fs[2].getText(),
                            fs[3].getText(), fs[4].getText(), cbVinculo.getSelectedItem(), cbStatus.getSelectedItem(), cargoId, row[0]);
                } else {
                    update("INSERT INTO tb_funcionario(cpf_funcionario,nome_funcionario,data_admissao,salario,carga_horaria_semanal,tipo_vinculo,status_funcionario,fk_cargo) VALUES(?,?,?,?,?,?,?,?)",
                            fs[0].getText(), fs[1].getText(), fs[2].getText(),
                            fs[3].getText(), fs[4].getText(), cbVinculo.getSelectedItem(), cbStatus.getSelectedItem(), cargoId);
                }
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    // ── Turmas ───────────────────────────────────────────────────
    private JPanel buildTurmasTab() {
        String[] cols = {"ID","Nome","Turno","Faixa Etária Início","Faixa Etária Fim","Capacidade","Status"};
        String sql = "SELECT pk_turma,nome_turma,turno,faixa_etaria_inicio,faixa_etaria_fim,capacidade_max,status_turma FROM tb_turma ORDER BY nome_turma";
        return crudPanel("Turmas", cols, sql, "tb_turma",
                () -> dialogTurma(null),
                (row) -> dialogTurma(row)
        );
    }

    private void dialogTurma(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Turma" : "Nova Turma");

        JComboBox<String> cbTurno = new JComboBox<>(new String[]{"Manha","Tarde","Noite","Integral"});
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"ativa","inativa","concluida"});
        styleCombo(cbTurno); styleCombo(cbStatus);

        String[] nomes = {"Nome da Turma","Faixa Etária Início","Faixa Etária Fim","Capacidade Máxima"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{
                row[1].toString(), row[3].toString(), row[4].toString(), row[5].toString()
        } : null);

        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Turno:", cbTurno);
        addFormRow(form, "Status:", cbStatus);
        if (edit) { cbTurno.setSelectedItem(row[2]); cbStatus.setSelectedItem(row[6]); }

        addSaveButton(d, () -> {
            try {
                if (edit) {
                    update("UPDATE tb_turma SET nome_turma=?,turno=?,faixa_etaria_inicio=?,faixa_etaria_fim=?,capacidade_max=?,status_turma=? WHERE pk_turma=?",
                            fs[0].getText(), cbTurno.getSelectedItem(), fs[1].getText(), fs[2].getText(), fs[3].getText(), cbStatus.getSelectedItem(), row[0]);
                } else {
                    update("INSERT INTO tb_turma(nome_turma,turno,faixa_etaria_inicio,faixa_etaria_fim,capacidade_max,status_turma) VALUES(?,?,?,?,?,?)",
                            fs[0].getText(), cbTurno.getSelectedItem(), fs[1].getText(), fs[2].getText(), fs[3].getText(), cbStatus.getSelectedItem());
                }
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    // ── Matrículas ───────────────────────────────────────────────
    private JPanel buildMatriculasTab() {
        String[] cols = {"ID","Aluno","Turma","Data Matrícula","Situação"};
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
        Map<String,Integer> alunoMap = new LinkedHashMap<>();
        Map<String,Integer> turmaMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_aluno,nome_aluno FROM tb_aluno ORDER BY nome_aluno");
            while(rs.next()) { alunoMap.put(rs.getString(2), rs.getInt(1)); cbAluno.addItem(rs.getString(2)); }
            rs = query("SELECT pk_turma,nome_turma FROM tb_turma ORDER BY nome_turma");
            while(rs.next()) { turmaMap.put(rs.getString(2), rs.getInt(1)); cbTurma.addItem(rs.getString(2)); }
        } catch(Exception ignored){}
        styleCombo(cbAluno); styleCombo(cbTurma);

        JComboBox<String> cbSit = new JComboBox<>(new String[]{"ativa","inativa","concluida","trancada"});
        styleCombo(cbSit);

        String[] nomes = {"Data Matrícula (AAAA-MM-DD)"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{ row[3].toString() } : null);

        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Aluno:", cbAluno);
        addFormRow(form, "Turma:", cbTurma);
        addFormRow(form, "Situação:", cbSit);
        if (edit) { cbAluno.setSelectedItem(row[1]); cbTurma.setSelectedItem(row[2]); cbSit.setSelectedItem(row[4]); }

        addSaveButton(d, () -> {
            try {
                int alunoId = alunoMap.getOrDefault(cbAluno.getSelectedItem().toString(), 0);
                int turmaId = turmaMap.getOrDefault(cbTurma.getSelectedItem().toString(), 0);
                if (edit) {
                    update("UPDATE tb_matricula SET fk_aluno=?,fk_turma=?,data_matricula=?,situacao_matricula=? WHERE pk_matricula=?",
                            alunoId, turmaId, fs[0].getText(), cbSit.getSelectedItem(), row[0]);
                } else {
                    update("INSERT INTO tb_matricula(fk_aluno,fk_turma,data_matricula,situacao_matricula) VALUES(?,?,?,?)",
                            alunoId, turmaId, fs[0].getText(), cbSit.getSelectedItem());
                }
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    // ── Frequência ───────────────────────────────────────────────
    private JPanel buildFrequenciaTab() {
        String[] cols = {"ID","Matrícula","Aluno","Data Aula","Presente","Motivo Falta"};
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
        Map<String,Integer> matMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_matricula,CONCAT(a.nome_aluno, ' (', t.nome_turma, ')') AS matricula_info FROM tb_matricula m JOIN tb_aluno a ON a.pk_aluno=m.fk_aluno JOIN tb_turma t ON t.pk_turma=m.fk_turma ORDER BY matricula_info");
            while(rs.next()) { matMap.put(rs.getString(2), rs.getInt(1)); cbMatricula.addItem(rs.getString(2)); }
        } catch(Exception ignored){}
        styleCombo(cbMatricula);

        JCheckBox cbPresente = new JCheckBox("Presente");
        cbPresente.setBackground(C_PANEL); cbPresente.setForeground(C_TEXT); cbPresente.setFont(F_BODY);
        JTextField fMotivo = styledField("");
        fMotivo.setEnabled(false);

        cbPresente.addActionListener(e -> fMotivo.setEnabled(!cbPresente.isSelected()));

        String[] nomes = {"Data Aula (AAAA-MM-DD)"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{ row[3].toString() } : null);

        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Matrícula:", cbMatricula);
        addFormRow(form, "", cbPresente);
        addFormRow(form, "Motivo Falta:", fMotivo);

        if (edit) {
            cbMatricula.setSelectedItem(row[2].toString() + " (" + row[1].toString() + ")"); // Displaying student name and enrollment ID
            boolean presente = (boolean) row[4];
            cbPresente.setSelected(presente);
            fMotivo.setText(row[5] != null ? row[5].toString() : "");
            fMotivo.setEnabled(!presente);
        }

        addSaveButton(d, () -> {
            try {
                boolean pres = cbPresente.isSelected();
                String motivo = fMotivo.getText().isBlank() ? null : fMotivo.getText();
                if (edit) {
                    update("UPDATE tb_frequencia SET data_aula=?,presente=?,motivo_falta=? WHERE pk_frequencia=?",
                            fs[0].getText(), pres, motivo, row[0]);
                } else {
                    int matId = matMap.getOrDefault(cbMatricula.getSelectedItem().toString(), 0);
                    update("INSERT INTO tb_frequencia(fk_matricula,data_aula,presente,motivo_falta) VALUES(?,?,?,?)",
                            matId, fs[0].getText(), pres, motivo);
                }
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    // ── Responsáveis ─────────────────────────────────────────────
    private JPanel buildResponsaveisTab() {
        String[] cols = {"ID","CPF","Nome","Cadastro"};
        String sql = "SELECT pk_responsavel,cpf_responsavel,nome_responsavel,data_criacao FROM tb_responsavel ORDER BY nome_responsavel";
        return crudPanel("Responsáveis", cols, sql, "tb_responsavel",
                () -> dialogResponsavel(null),
                (row) -> dialogResponsavel(row)
        );
    }

    private void dialogResponsavel(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Responsável" : "Novo Responsável");
        String[] nomes = {"Nome Completo","CPF (11 dígitos)"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{row[2].toString(), row[1].toString()} : null);
        addSaveButton(d, () -> {
            try {
                if (edit) update("UPDATE tb_responsavel SET nome_responsavel=?,cpf_responsavel=? WHERE pk_responsavel=?", fs[0].getText(), fs[1].getText(), row[0]);
                else      update("INSERT INTO tb_responsavel(nome_responsavel,cpf_responsavel) VALUES(?,?)", fs[0].getText(), fs[1].getText());
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    // ── Financeiro ───────────────────────────────────────────────
    private JPanel buildFinanceiroTab() {
        JPanel p = darkPanel(new BorderLayout(0,0));
        JTabbedPane ft = new JTabbedPane();
        styleTabs(ft);

        // Sub-tab Repasses
        String[] cR = {"ID","Programa","Data","Valor (R$)","Mês Ref.","Descrição"};
        String sqlR = "SELECT r.pk_repasse,p.nome_programa,r.data_repasse,r.valor_repasse,r.mes_referencia,r.descricao "
                + "FROM tb_repasse r JOIN tb_programa_social p ON p.pk_programa=r.fk_programa ORDER BY r.data_repasse DESC";
        ft.addTab("Repasses", getIcon("finance"), crudPanel("Repasses", cR, sqlR, "tb_repasse",
                () -> dialogRepasse(null), (row) -> dialogRepasse(row)));

        // Sub-tab Gastos
        String[] cG = {"ID","Repasse","Categoria","Data","Valor (R$)","Descrição","NF"};
        String sqlG = "SELECT g.pk_gasto,r.mes_referencia,c.nome_categoria,g.data_gasto,"
                + "g.valor_gasto,g.descricao,g.nota_fiscal "
                + "FROM tb_gasto g JOIN tb_repasse r ON r.pk_repasse=g.fk_repasse "
                + "JOIN tb_categoria_gastos c ON c.pk_categoria=g.fk_categoria ORDER BY g.data_gasto DESC";
        ft.addTab("Gastos", getIcon("finance"), crudPanel("Gastos", cG, sqlG, "tb_gasto",
                () -> dialogGasto(null), (row) -> dialogGasto(row)));

        // Sub-tab Saldo
        String[] cS = {"Repasse","Programa","Valor Repasse","Total Gastos","Total Pgtos","Saldo"};
        String sqlS = "SELECT mes_referencia,nome_programa,valor_repasse,total_gastos,total_pagamentos,saldo_disponivel FROM vw_saldo_repasse";
        DefaultTableModel mS = tableModel(cS, sqlS);
        JPanel saldoPanel = darkPanel(new BorderLayout(0,10));
        saldoPanel.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
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
        Map<String,Integer> progMap = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_programa,nome_programa FROM tb_programa_social");
            while(rs.next()) { progMap.put(rs.getString(2), rs.getInt(1)); cbProg.addItem(rs.getString(2)); }
        } catch(Exception ignored){}
        styleCombo(cbProg);

        String[] nomes = {"Data (AAAA-MM-DD)","Valor (R$)","Mês Referência (AAAA-MM)","Descrição"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{
                row[2].toString(), row[3].toString(), row[4].toString(), row[5] != null ? row[5].toString() : ""
        } : null);

        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Programa:", cbProg);
        if (edit) cbProg.setSelectedItem(row[1]);

        addSaveButton(d, () -> {
            try {
                int progId = progMap.getOrDefault(cbProg.getSelectedItem().toString(), 1);
                if (edit) {
                    update("UPDATE tb_repasse SET data_repasse=?,valor_repasse=?,mes_referencia=?,descricao=?,fk_programa=? WHERE pk_repasse=?",
                            fs[0].getText(), fs[1].getText(), fs[2].getText(), fs[3].getText(), progId, row[0]);
                } else {
                    update("INSERT INTO tb_repasse(fk_programa,data_repasse,valor_repasse,mes_referencia,descricao) VALUES(?,?,?,?,?)",
                            progId, fs[0].getText(), fs[1].getText(), fs[2].getText(), fs[3].getText());
                }
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    private void dialogGasto(Object[] row) {
        boolean edit = row != null;
        JDialog d = formDialog(edit ? "Editar Gasto" : "Novo Gasto");

        JComboBox<String> cbRepasse = new JComboBox<>();
        JComboBox<String> cbCat     = new JComboBox<>();
        Map<String,Integer> repMap  = new LinkedHashMap<>();
        Map<String,Integer> catMap  = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_repasse,mes_referencia FROM tb_repasse ORDER BY data_repasse DESC");
            while(rs.next()) { repMap.put(rs.getString(2), rs.getInt(1)); cbRepasse.addItem(rs.getString(2)); }
            rs = query("SELECT pk_categoria,nome_categoria FROM tb_categoria_gastos ORDER BY nome_categoria");
            while(rs.next()) { catMap.put(rs.getString(2), rs.getInt(1)); cbCat.addItem(rs.getString(2)); }
        } catch(Exception ignored){}
        styleCombo(cbRepasse); styleCombo(cbCat);
        if (edit) { cbRepasse.setSelectedItem(row[1]); cbCat.setSelectedItem(row[2]); }

        String[] nomes = {"Data (AAAA-MM-DD)","Valor (R$)","Descrição","Nota Fiscal"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{
                row[3].toString(), row[4].toString(), row[5] != null ? row[5].toString() : "", row[6] != null ? row[6].toString() : ""
        } : null);

        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Repasse:", cbRepasse);
        addFormRow(form, "Categoria:", cbCat);

        addSaveButton(d, () -> {
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
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    // ── Alertas ──────────────────────────────────────────────────
    private JPanel buildAlertasTab() {
        String[] cols = {"ID","Aluno","Funcionário","Tipo","Nível","Descrição","Data","Status"};
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

        JComboBox<String> cbAluno  = new JComboBox<>();
        JComboBox<String> cbFunc   = new JComboBox<>();
        Map<String,Integer> aMap   = new LinkedHashMap<>();
        Map<String,Integer> fMap   = new LinkedHashMap<>();
        try {
            ResultSet rs = query("SELECT pk_aluno,nome_aluno FROM tb_aluno ORDER BY nome_aluno");
            while(rs.next()) { aMap.put(rs.getString(2), rs.getInt(1)); cbAluno.addItem(rs.getString(2)); }
            rs = query("SELECT pk_funcionario,nome_funcionario FROM tb_funcionario ORDER BY nome_funcionario");
            while(rs.next()) { fMap.put(rs.getString(2), rs.getInt(1)); cbFunc.addItem(rs.getString(2)); }
        } catch(Exception ignored){}
        styleCombo(cbAluno); styleCombo(cbFunc);

        JComboBox<String> cbTipo   = new JComboBox<>(new String[]{"Frequencia Critica","Vulnerabilidade","Evasao Iminente"});
        JComboBox<String> cbNivel  = new JComboBox<>(new String[]{"Baixo","Medio","Alto","Critico"});
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Aberto","Em Acompanhamento","Resolvido"});
        styleCombo(cbTipo); styleCombo(cbNivel); styleCombo(cbStatus);

        String[] nomes = {"Descrição do Alerta","Data (AAAA-MM-DD)"};
        JTextField[] fs = formFields(d, nomes, edit ? new String[]{ row[5].toString(), row[6].toString() } : null);

        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        addFormRow(form, "Aluno:", cbAluno);
        addFormRow(form, "Funcionário:", cbFunc);
        addFormRow(form, "Tipo:", cbTipo);
        addFormRow(form, "Nível de Risco:", cbNivel);
        addFormRow(form, "Status:", cbStatus);
        if (edit) { cbAluno.setSelectedItem(row[1]); cbFunc.setSelectedItem(row[2]);
            cbTipo.setSelectedItem(row[3]); cbNivel.setSelectedItem(row[4]); cbStatus.setSelectedItem(row[7]); }

        addSaveButton(d, () -> {
            try {
                int aId = aMap.getOrDefault(cbAluno.getSelectedItem().toString(), 0);
                int fId = fMap.getOrDefault(cbFunc.getSelectedItem().toString(), 0);
                if (edit) {
                    update("UPDATE tb_alerta SET descricao_alerta=?,data_alerta=?,tipo_alerta=?,nivel_risco=?,status_alerta=? WHERE pk_alerta=?",
                            fs[0].getText(), fs[1].getText(), cbTipo.getSelectedItem(), cbNivel.getSelectedItem(), cbStatus.getSelectedItem(), row[0]);
                } else {
                    update("INSERT INTO tb_alerta(fk_aluno,fk_funcionario,tipo_alerta,nivel_risco,descricao_alerta,data_alerta) VALUES(?,?,?,?,?,?)",
                            aId, fId, cbTipo.getSelectedItem(), cbNivel.getSelectedItem(), fs[0].getText(), fs[1].getText());
                }
                d.dispose(); refreshCurrentTab();
            } catch (Exception ex) { showError(d, ex); }
        });
        d.setVisible(true);
    }

    // ============================================================
    // COMPONENTES UTILITÁRIOS
    // ============================================================

    @FunctionalInterface interface Action  { void run(); }
    @FunctionalInterface interface RowAct  { void run(Object[] row); }

    private JPanel crudPanel(String title, String[] cols, String sql, String table, Action onCreate, RowAct onEdit) {
        JPanel p = darkPanel(new BorderLayout(0,12));
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        // Header bar
        JPanel bar = darkPanel(new BorderLayout(12,0));
        JLabel lbl = styledLabel(title, new Font("Segoe UI", Font.BOLD, 22), C_TEXT);

        JTextField search = styledField("Buscar...");
        search.setPreferredSize(new Dimension(280,44));
        search.putClientProperty("JTextField.placeholderText", "Buscar..."); // Placeholder text

        JButton btnNew = accentButton("Novo", getIcon("plus"));
        JButton btnEdit = accentButton("Editar", getIcon("edit"));
        JButton btnDel  = accentButton("Excluir", getIcon("delete"));
        btnEdit.setEnabled(false); btnDel.setEnabled(false);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        btns.setBackground(C_BG); btns.add(search); btns.add(btnNew); btns.add(btnEdit); btns.add(btnDel);
        bar.add(lbl, BorderLayout.WEST); bar.add(btns, BorderLayout.EAST);

        // Table
        DefaultTableModel model = tableModel(cols, sql);
        JTable table2 = styledTable(model);
        JScrollPane scroll = new JScrollPane(table2);
        styleScroll(scroll);

        // Search filter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table2.setRowSorter(sorter);
        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String t = search.getText().replace("Buscar...","").trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)"+t));
            }
        });

        // Selection
        table2.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table2.getSelectedRow() != -1;
            btnEdit.setEnabled(sel); btnDel.setEnabled(sel);
        });

        btnNew.addActionListener(e -> onCreate.run());
        btnEdit.addActionListener(e -> {
            int r = table2.convertRowIndexToModel(table2.getSelectedRow());
            if (r < 0) return;
            Object[] row = new Object[model.getColumnCount()];
            for (int i=0; i<row.length; i++) row[i] = model.getValueAt(r,i);
            onEdit.run(row);
        });
        btnDel.addActionListener(e -> {
            int r = table2.convertRowIndexToModel(table2.getSelectedRow());
            if (r < 0) return;
            Object id = model.getValueAt(r,0);
            int ok = JOptionPane.showConfirmDialog(this,"Excluir registro #"+id+"?","Confirmar",JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            String pk = "pk_"+table.replace("tb_","");
            try { update("DELETE FROM "+table+" WHERE "+pk+"=?", id); refreshCurrentTab(); }
            catch (Exception ex) { showError(this, ex); }
        });

        p.add(bar,    BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        // Rodapé contagem
        JLabel count = styledLabel("", F_BODY, C_MUTED);
        count.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));
        model.addTableModelListener(ev -> count.setText(model.getRowCount()+" registros"));
        count.setText(model.getRowCount()+" registros");
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
        JLabel lLbl = styledLabel(label, F_BODY, C_MUTED);
        lVal.setOpaque(false);
        lLbl.setOpaque(false);

        card.add(lVal, BorderLayout.WEST);   // número à esquerda
        card.add(lLbl, BorderLayout.CENTER); // label à direita

        return card;
    }

    // ── Helpers de formulário ─────────────────────────────────────
    private JDialog formDialog(String title) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(520, 520); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(C_PANEL);
        d.setLayout(new BorderLayout());
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(C_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20,16,20,16));
        JScrollPane sp = new JScrollPane(form);
        sp.setBackground(C_PANEL); sp.getViewport().setBackground(C_PANEL);
        sp.setBorder(null);
        d.add(sp, BorderLayout.CENTER);
        return d;
    }

    private JTextField[] formFields(JDialog d, String[] labels, String[] vals) {
        JPanel form = (JPanel) ((JScrollPane)d.getContentPane().getComponent(0)).getViewport().getView();
        JTextField[] fields = new JTextField[labels.length];
        for (int i=0; i<labels.length; i++) {
            fields[i] = styledField(vals != null ? vals[i] : "");
            addFormRow(form, labels[i]+":", fields[i]);
        }
        return fields;
    }

    private void addFormRow(JPanel form, String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8,0));
        row.setBackground(C_PANEL);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));
        JLabel l = styledLabel(label, F_BODY, C_MUTED);
        l.setPreferredSize(new Dimension(165,24));
        row.add(l, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        form.add(row);
    }

    private void addSaveButton(JDialog d, Action action) {
        JButton btn = accentButton("Salvar", getIcon("save"));
        btn.addActionListener(e -> action.run());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT,16,12));
        bot.setBackground(C_PANEL);
        JButton cancel = iconButton("Cancelar", getIcon("cancel"));
        cancel.addActionListener(e -> d.dispose());
        bot.add(cancel); bot.add(btn);
        d.add(bot, BorderLayout.SOUTH);
    }

    // ── Helpers de BD ─────────────────────────────────────────────
    private ResultSet query(String sql, Object... params) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i=0; i<params.length; i++) ps.setObject(i+1, params[i]);
        return ps.executeQuery();
    }

    private void update(String sql, Object... params) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i=0; i<params.length; i++) ps.setObject(i+1, params[i]);
        ps.executeUpdate();
        ps.close();
    }

    private String countQuery(String sql) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if(rs.next()) return rs.getString(1);
        } catch(Exception ignored){}
        return "—";
    }

    private DefaultTableModel tableModel(String[] cols, String sql) {
        DefaultTableModel m = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int nc = meta.getColumnCount();
            while(rs.next()) {
                Object[] row = new Object[nc];
                for(int i=0;i<nc;i++) row[i] = rs.getObject(i+1);
                m.addRow(row);
            }
        } catch(Exception ignored){}
        return m;
    }

    // ── Helpers visuais ───────────────────────────────────────────
    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(C_BG);
        p.setOpaque(true);   // ← adicionar isso
        return p;
    }

    private JLabel styledLabel(String t, Font f, Color c) {
        JLabel l = new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }

    private JTextField styledField(String val) {
        JTextField f = new JTextField(val); styleField(f); return f;
    }

    private void styleField(JTextField f) {
        f.setBackground(C_CARD); f.setForeground(C_TEXT); f.setCaretColor(C_TEXT);
        f.setFont(F_BODY); f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6,10,6,10)));
        f.setPreferredSize(new Dimension(0,36));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(C_CARD); cb.setForeground(C_TEXT); cb.setFont(F_BODY);
        cb.setBorder(new LineBorder(C_BORDER,1,true));
        cb.setPreferredSize(new Dimension(0,36));
    }

    private JButton accentButton(String text, ImageIcon icon) {
        JButton b = new JButton(text, icon);
        b.setBackground(C_ACCENT); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true); b.setBorderPainted(false);
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ b.setBackground(C_ACCENT.brighter()); }
            public void mouseExited (MouseEvent e){ b.setBackground(C_ACCENT); }
        });
        return b;
    }

    private JButton iconButton(String text, ImageIcon icon) {
        JButton b = new JButton(text, icon);
        b.setBackground(C_CARD); b.setForeground(C_TEXT);
        b.setFont(F_BODY); b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true); b.setBorderPainted(false);
        return b;
    }

    private JTable styledTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setBackground(C_TABLE_R1); t.setForeground(C_TEXT);
        t.setFont(F_BODY); t.setRowHeight(34);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,1));
        t.setSelectionBackground(C_SEL); t.setSelectionForeground(C_TEXT);
        t.getTableHeader().setBackground(C_TABLE_H);
        t.getTableHeader().setForeground(C_TEXT);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBorder(new MatteBorder(0,0,1,0,C_BORDER));
        t.setFillsViewportHeight(true);
        // Zebra
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable tbl,Object v,boolean sel,boolean foc,int r,int c){
                super.getTableCellRendererComponent(tbl,v,sel,foc,r,c);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                setBackground(sel ? C_SEL : (r%2==0 ? C_TABLE_R1 : C_TABLE_R2));
                setForeground(C_TEXT);
                return this;
            }
        });
        return t;
    }

    private JScrollPane scrollTable(DefaultTableModel m) {
        JScrollPane sp = new JScrollPane(styledTable(m));
        styleScroll(sp); return sp;
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBackground(C_BG); sp.getViewport().setBackground(C_TABLE_R1);
        sp.setBorder(new LineBorder(C_BORDER,1));
        sp.getVerticalScrollBar().setBackground(C_CARD);
        sp.getHorizontalScrollBar().setBackground(C_CARD);
    }

    private void styleTabs(JTabbedPane tp) {
        UIManager.put("TabbedPane.selected",            C_CARD);
        UIManager.put("TabbedPane.selectColor",         C_CARD);
        UIManager.put("TabbedPane.tabAreaBackground",   C_PANEL);
        UIManager.put("TabbedPane.background",          C_PANEL);
        UIManager.put("TabbedPane.foreground",          C_TEXT);
        UIManager.put("TabbedPane.selectedForeground",  C_TEXT);
        UIManager.put("TabbedPane.focus",               C_ACCENT);
        UIManager.put("TabbedPane.highlight",           C_CARD);
        UIManager.put("TabbedPane.darkShadow",          C_BORDER);
        UIManager.put("TabbedPane.shadow",              C_BORDER);
        UIManager.put("TabbedPane.light",               C_CARD);

        // ← Adicionar isso: padding interno de cada aba (top, left, bottom, right)
        UIManager.put("TabbedPane.tabInsets", new Insets(25, 45, 25, 45));

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
        switch(idx) {
            case 0: novo = buildDashboard();       break;
            case 1: novo = buildAlunosTab();       break;
            case 2: novo = buildFuncionariosTab(); break;
            case 3: novo = buildTurmasTab();       break;
            case 4: novo = buildMatriculasTab();   break;
            case 5: novo = buildFrequenciaTab();   break;
            case 6: novo = buildResponsaveisTab(); break;
            case 7: novo = buildFinanceiroTab();   break;
            case 8: novo = buildAlertasTab();      break;
            default: return;
        }
        tabs.setComponentAt(idx, novo);
        tabs.revalidate(); tabs.repaint();
    }
}