package uespi.trabcons;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

/**
 * Gerenciador de dados de alunos.
 * As operações de persistência sao feitas no banco de dados
 * via Hibernate, com cópia de segurança (backup) em CSV.
 */
class listaAlunos {

    // A lista local de alunos. É 'final' e só pode ser carregada no construtor.
    public final java.util.List<Aluno> listaAlunos;
    // Referência para a interface gráfica, usada para atualizar a tabela após mudanças.
    private TrabConsData dataView = null;

    // Associa a interface (view) a este controlador de dados.
    public void registerView(TrabConsData view) {
        this.dataView = view;
    }

    // Remove a associação com a interface.
    public void unregisterView() {
        this.dataView = null;
    }

    // Construtor: Inicia o gerenciador carregando os dados do banco.
    public listaAlunos() {
        this.listaAlunos = carregarAlunosDoBanco();
    }

    // -- CARREGAMENTO DE DADOS ---

    /**
     * Tenta carregar todos os alunos do banco de dados MySQL via Hibernate.
     */
    private java.util.List<Aluno> carregarAlunosDoBanco() {
        // Bloco 'try-with-resources' garante que a sessão do hibernate feche.
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Criação da consulta HQL para selecionar todos os objetos Aluno.
            Query<Aluno> query = session.createQuery("FROM Aluno", Aluno.class);
            System.out.println("Carregando alunos do banco de dados...");
            // Executa a consulta e retorna a lista de alunos.
            return query.list();
        } catch (Exception e) {
            System.err.println("Erro grave ao carregar alunos do banco: " + e.getMessage());
            e.printStackTrace();
            // Fallback: se o banco falhar, tenta carregar de um arquivo CSV existente.
            if (new File("alunos.csv").exists()) {
                System.err.println("Tentando carregar do CSV como fallback...");
                return SwingUtils.carregarAlunosDoCsv();
            }
            // Retorna lista vazia se tudo falhar.
            return new java.util.LinkedList<>();
        }
    }


    // --- CADASTRO DE ALUNO --

    /**
     * Cadastra um novo aluno no banco de dados, na lista local e salva um backup no CSV.
     */
    public void cadastrar(JTextField nomes, JTextField ind, JTextField matr, JTextField idades, JFormattedTextField datan, JFormattedTextField tele, JFormattedTextField cpfs){
        try {
            // 1. CAPTURA E PARSING DOS DADOS DA GUI (Campos da tela)
            String matricula = matr.getText();
            String nome = nomes.getText();
            int idade = Integer.parseInt(idades.getText());
            int index = Integer.parseInt(ind.getText());
            Date dataNascimento = (Date) datan.getValue();
            String telefone = tele.getText();
            String cpf = cpfs.getText();

            // Validação 1: CPF obrigatório.
            if (cpf.equals("   .   .   -  ")) {
                javax.swing.JOptionPane.showMessageDialog(null, "O campo CPF é obrigatório.", "Erro de Validação", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Validação 2: Data, Matrícula e Nome obrigatórios.
            if (dataNascimento == null || matricula.trim().isEmpty() || nome.trim().isEmpty()) {
                 javax.swing.JOptionPane.showMessageDialog(null, "Matrícula, Nome e Data são obrigatórios.", "Erro de Validação", javax.swing.JOptionPane.ERROR_MESSAGE);
                 return;
            }

            // 2. CRIA O OBJETO ALUNO na memória
            Aluno novoAluno = new Aluno(matricula, nome, idade, dataNascimento, telefone, cpf, index);

            // 3. PERSISTÊNCIA COM HIBERNATE (Banco de Dados)
            Transaction transaction = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Validação 3: Checagem prévia de CPF duplicado no banco.
                Query<Aluno> queryCpf = session.createQuery("FROM Aluno WHERE cpf = :cpfDoAluno", Aluno.class);
                queryCpf.setParameter("cpfDoAluno", cpf);

                // uniqueResult() retorna o objeto se existir, ou 'null'.
                Aluno alunoComMesmoCpf = queryCpf.uniqueResult();

                if (alunoComMesmoCpf != null) {
                    // Impede o cadastro se o CPF já estiver em uso.
                    javax.swing.JOptionPane.showMessageDialog(null,
                        "Erro: O CPF '" + cpf + "' já está sendo utilizado por: " + alunoComMesmoCpf.getNome(),
                        "CPF Duplicado",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Inicia a transação, salva o objeto no banco e confirma.
                transaction = session.beginTransaction();
                session.persist(novoAluno);
                transaction.commit();

                // 4. Se o banco salvou, o objeto é adicionado à lista local (em memória)
                this.listaAlunos.add(novoAluno);
                if (this.dataView != null) {
                    this.dataView.atualizarTudo(); // Atualiza a exibição na interface
                }

                // 5. Salva também no CSV (Backup)
                try {
                    SwingUtils.salvarAlunosEmCsv(this.listaAlunos);
                    javax.swing.JOptionPane.showMessageDialog(null, "Aluno salvo com SUCESSO no MySQL e no CSV!");

                } catch (IOException ioEx) {
                    // Aviso se o banco salvou, mas o backup falhou.
                    javax.swing.JOptionPane.showMessageDialog(null,
                        "Aluno salvo no MySQL com SUCESSO.\n\nFALHA ao salvar backup no CSV: " + ioEx.getMessage(),
                        "Aviso de Backup",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                }

                // 6. Limpa os campos da interface gráfica após o sucesso.
                nomes.setText("");
                matr.setText("");
                idades.setText("");
                datan.setValue(null);
                tele.setText("");
                cpfs.setText("");
                ind.setText("");

            } catch (ConstraintViolationException e) {
                // Captura erro de chave duplicada (ex: Matrícula) do banco.
                if (transaction != null) transaction.rollback();
                javax.swing.JOptionPane.showMessageDialog(null, "Erro: Matrícula '" + matricula + "' já existe no banco de dados.", "Matrícula Duplicada", javax.swing.JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                // Captura outros erros gerais do banco de dados/hibernate.
                if (transaction != null) transaction.rollback();
                javax.swing.JOptionPane.showMessageDialog(null, "Erro ao salvar no banco de dados: " + e.getMessage(), "Erro de Banco de Dados", javax.swing.JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

        // --- Tratamento de Exceções de Formato de Entrada ---
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro de Conversão: Idade ou Index deve ser um número válido.", "Erro de Formato", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (java.text.ParseException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: A data de nascimento deve ser fornecida.", "Erro de Data", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
             // Tratamento para exceções de validação interna do construtor Aluno.
        }
        catch (HeadlessException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro de GUI ao cadastrar aluno: " + e.getMessage(), "Erro", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Retorna o número total de alunos na lista local, como uma string.
     */
    public String getTamanho(){
        return String.valueOf(listaAlunos.size());
    }

    // --- EXCLUSÃO DE ALUNO ---

    /**
     * Exclui um aluno do banco de dados, da lista local e atualiza o CSV.
     */
    public void excluirAluno(String matricula) {
        Aluno alunoParaExcluir = null;

        // Procura o aluno na lista local pela matrícula.
        for (Aluno a : listaAlunos) {
            if (a.getMatricula().equals(matricula)) {
                alunoParaExcluir = a;
                break;
            }
        }

        if (alunoParaExcluir == null) {
            javax.swing.JOptionPane.showMessageDialog(null, "Aluno com matrícula " + matricula + " não encontrado na lista.", "Erro", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Exclui do banco de dados.
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(alunoParaExcluir);
            transaction.commit();

            // 2. Se deu certo, remove da lista em memória.
            this.listaAlunos.remove(alunoParaExcluir);

            if (this.dataView != null) {
                this.dataView.atualizarTudo(); // Atualiza a interface
            }

            // 3. Tenta atualizar o CSV (backup).
            try {
                SwingUtils.salvarAlunosEmCsv(this.listaAlunos);
                javax.swing.JOptionPane.showMessageDialog(null, "Aluno " + matricula + " excluído com sucesso do MySQL e do CSV.");
            } catch (IOException ioEx) {
                // Aviso se o banco excluiu, mas o backup CSV falhou.
                javax.swing.JOptionPane.showMessageDialog(null,
                    "Aluno excluído do MySQL com SUCESSO.\n\nFALHA ao atualizar o CSV: " + ioEx.getMessage(),
                    "Aviso de Backup",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            // Reverte a transação em caso de erro no banco.
            if (transaction != null) transaction.rollback();
            javax.swing.JOptionPane.showMessageDialog(null, "Erro ao excluir aluno do banco: " + e.getMessage(), "Erro de Banco de Dados", javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Busca um aluno na lista em memória usando a matrícula como chave.
     */
    public Aluno buscarPorMatricula(String matricula) {
        // Itera sobre a lista 'listaAlunos' local.
        for (Aluno aluno : this.listaAlunos) {
            // Compara a matrícula.
            if (aluno.getMatricula().equals(matricula)) {
                return aluno; // Retorna o aluno se a matrícula for igual.
            }
        }
        return null; // Retorna nulo se o aluno não for encontrado.
    }
}