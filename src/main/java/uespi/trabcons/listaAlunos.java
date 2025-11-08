package uespi.trabcons;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException; // RE-ADICIONADO: Necessário para salvar o CSV
import java.util.Date;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

// --- NOVAS IMPORTAÇÕES DO HIBERNATE ---
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

/**
 * @author GDK13
 * * (MODIFICADO) Esta classe gerencia os alunos usando o
 * Hibernate (MySQL) E também salva um backup no arquivo CSV.
 */
class listaAlunos {
    
    public final java.util.List<Aluno> listaAlunos;
    private TrabConsData dataView = null;
    
    public void registerView(TrabConsData view) {
        this.dataView = view;
    }
    
     public void unregisterView() {
        this.dataView = null;
    }
    
    // Construtor: Carrega a lista do BANCO DE DADOS ao iniciar.
    public listaAlunos() {
        this.listaAlunos = carregarAlunosDoBanco();
    }
    
    /**
     * NOVO MÉTODO: Carrega todos os alunos do banco de dados MySQL
     * usando Hibernate.
     */
    private java.util.List<Aluno> carregarAlunosDoBanco() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Aluno> query = session.createQuery("FROM Aluno", Aluno.class);
            System.out.println("Carregando alunos do banco de dados...");
            return query.list();
        } catch (Exception e) {
            System.err.println("Erro grave ao carregar alunos do banco: " + e.getMessage());
            e.printStackTrace();
            // Tenta carregar do CSV como um fallback de emergência
            if (new File("alunos.csv").exists()) {
                System.err.println("Tentando carregar do CSV como fallback...");
                return SwingUtils.carregarAlunosDoCsv();
            }
            // Se tudo falhar, retorna uma lista vazia
            return new java.util.LinkedList<>(); 
        }
    }
    
    
    /**
     * MÉTODO MODIFICADO: Cadastra o aluno no BANCO DE DADOS e no CSV.
     */
    public void cadastrar(JTextField nomes, JTextField ind, JTextField matr, JTextField idades, JFormattedTextField datan, JFormattedTextField tele, JFormattedTextField cpfs){
        try {
            // 1. CAPTURA E PARSING (Sua lógica original)
            String matricula = matr.getText();
            String nome = nomes.getText();
            int idade = Integer.parseInt(idades.getText());
            int index = Integer.parseInt(ind.getText());
            Date dataNascimento = (Date) datan.getValue();
            String telefone = tele.getText();
            String cpf = cpfs.getText();
            
            if (cpf.equals("   .   .   -  ")) {
                javax.swing.JOptionPane.showMessageDialog(null, "O campo CPF é obrigatório.", "Erro de Validação", javax.swing.JOptionPane.ERROR_MESSAGE);
                return; // Para a execução do cadastro
            }

            
            if (dataNascimento == null) {
                throw new java.text.ParseException("Data não pode ser nula", 0);
            }
            if (matricula.trim().isEmpty() || nome.trim().isEmpty()) {
                 javax.swing.JOptionPane.showMessageDialog(null, "Matrícula e Nome são obrigatórios.", "Erro de Validação", javax.swing.JOptionPane.ERROR_MESSAGE);
                 return;
            }

            // 2. CRIA O OBJETO ALUNO
            Aluno novoAluno = new Aluno(matricula, nome, idade, dataNascimento, telefone, cpf, index); 
               
            // 3. PERSISTÊNCIA COM HIBERNATE (BANCO DE DADOS)
            Transaction transaction = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                 // VALIDAÇÃO 2: CHECA CPF DUPLICADO NO BANCO
                // Precisamos checar ANTES de tentar salvar.
                Query<Aluno> queryCpf = session.createQuery(
                    "FROM Aluno WHERE cpf = :cpfDoAluno", Aluno.class
                );
                queryCpf.setParameter("cpfDoAluno", cpf);
                
                // uniqueResult() retorna um Aluno se encontrar, ou null se não.
                Aluno alunoComMesmoCpf = queryCpf.uniqueResult(); 

                if (alunoComMesmoCpf != null) {
                    // Se encontrou um aluno, o CPF já está em uso.
                    javax.swing.JOptionPane.showMessageDialog(null, 
                        "Erro: O CPF '" + cpf + "' já está sendo utilizado pelo aluno: " + alunoComMesmoCpf.getNome(), 
                        "CPF Duplicado", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                    return; // Para a execução do cadastro
                }
                transaction = session.beginTransaction();
                session.persist(novoAluno); 
                transaction.commit();

                // 4. Se a transação foi bem-sucedida, atualiza a lista em memória
                this.listaAlunos.add(novoAluno);
                if (this.dataView != null) {
                    this.dataView.atualizarTudo();
                }
                // 5. AGORA, SALVA TAMBÉM NO CSV (COMO ANTES)
                try {
                    SwingUtils.salvarAlunosEmCsv(this.listaAlunos);
                    javax.swing.JOptionPane.showMessageDialog(null, "Aluno salvo com SUCESSO no MySQL e no CSV!");
                    
                } catch (IOException ioEx) {
                    // Se o banco salvou mas o CSV falhou, avisa o usuário
                    javax.swing.JOptionPane.showMessageDialog(null, 
                        "Aluno salvo no MySQL com SUCESSO.\n\nFALHA ao salvar backup no CSV: " + ioEx.getMessage(), 
                        "Aviso de Backup", 
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                }

                // 6. Limpa os campos da GUI
                nomes.setText("");
                matr.setText("");
                idades.setText("");
                datan.setValue(null);
                tele.setText("");
                cpfs.setText("");
                ind.setText("");

            } catch (ConstraintViolationException e) {
                if (transaction != null) transaction.rollback();
                javax.swing.JOptionPane.showMessageDialog(null, "Erro: Matrícula '" + matricula + "' já existe no banco de dados.", "Matrícula Duplicada", javax.swing.JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                javax.swing.JOptionPane.showMessageDialog(null, "Erro ao salvar no banco de dados: " + e.getMessage(), "Erro de Banco de Dados", javax.swing.JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            
        // --- Blocos de Tratamento de Exceções Originais ---
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro de Conversão: Idade ou Index deve ser um número válido.", "Erro de Formato", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (java.text.ParseException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: A data de nascimento deve ser fornecida.", "Erro de Data", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
             // Tratamento da exceção de Matrícula Duplicada (se houver)
        } 
        // Não precisamos mais do catch (IOException e) aqui fora, 
        // pois ele está sendo tratado dentro do try-catch do Hibernate.
        catch (HeadlessException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro de GUI ao cadastrar aluno: " + e.getMessage(), "Erro", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Retorna o tamanho da lista como String para exibição na GUI.
     */
    public String getTamanho(){
        return String.valueOf(listaAlunos.size());
    }
    
    // --- MÉTODOS DE EXCLUSÃO (TAMBÉM ATUALIZADO) ---

    /**
     * Exclui um aluno do banco de dados, da lista local e do CSV.
     */
    public void excluirAluno(String matricula) {
        Aluno alunoParaExcluir = null;
        
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

        // 1. Exclui do banco de dados
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(alunoParaExcluir); 
            transaction.commit();
            
            // 2. Se deu certo, remove da lista em memória
            this.listaAlunos.remove(alunoParaExcluir);
            
            if (this.dataView != null) {
                this.dataView.atualizarTudo();
            }
            
            // 3. Tenta atualizar o CSV
            try {
                SwingUtils.salvarAlunosEmCsv(this.listaAlunos);
                javax.swing.JOptionPane.showMessageDialog(null, "Aluno " + matricula + " excluído com sucesso do MySQL e do CSV.");
            } catch (IOException ioEx) {
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "Aluno excluído do MySQL com SUCESSO.\n\nFALHA ao atualizar o CSV: " + ioEx.getMessage(), 
                    "Aviso de Backup", 
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            javax.swing.JOptionPane.showMessageDialog(null, "Erro ao excluir aluno do banco: " + e.getMessage(), "Erro de Banco de Dados", javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Busca um aluno na lista em memória pela matrícula.
     *
     * @param matricula A matrícula a ser procurada.
     * @return O objeto Aluno se encontrado, ou null se não.
     */
    public Aluno buscarPorMatricula(String matricula) {
        // Itera sobre a lista 'listaAlunos'
        for (Aluno aluno : this.listaAlunos) {
            // Compara a matrícula de cada aluno com a matrícula fornecida
            if (aluno.getMatricula().equals(matricula)) {
                return aluno; // Retorna o aluno se encontrar
            }
        }
        return null; // Retorna null se o loop terminar sem encontrar
    }
}