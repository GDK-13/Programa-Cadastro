package uespi.trabcons;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
// Imports essenciais para I/O e manipulação de data
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;

// Imports do Apache Commons CSV
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/**
 * Classe utilitária para métodos de conveniência no Swing e persistência de dados (CSV).
 */
public class SwingUtils {
    
    // 1. UTILITÁRIO DE INTERFACE (SWING)

    /**
     * Configura um placeholder (texto de dica) em um JTextField.
     * @param campo
     * @param placeholder
     */
    public static void configurarPlaceholder(JTextField campo, String placeholder) {
        
        // 1. Define o texto inicial como placeholder se o campo estiver vazio
        if (campo.getText() == null || campo.getText().isEmpty() || campo.getText().equals(placeholder)) {
             campo.setText(placeholder);
        }

        // 2. Adiciona o FocusListener para gerenciar o comportamento ao ganhar/perder foco
        campo.addFocusListener(new FocusAdapter() {
            
            // Ação quando o campo GANHA foco (clica nele)
            @Override
            public void focusGained(FocusEvent evt) {
                // Limpa o campo se o texto atual for o placeholder
                if (campo.getText().equals(placeholder)) {
                    campo.setText("");
                }
            }

            // Ação quando o campo PERDE foco (clica fora)
            @Override
            public void focusLost(FocusEvent evt) {
                // Restaura o placeholder se o campo estiver vazio
                if (campo.getText().isEmpty()) {
                    campo.setText(placeholder);
                }
            }
        });
    }
    
    // 2. PERSISTÊNCIA DE DADOS (CSV)
    
    //Constante com o nome do arquivo
    private static final String CSV_FILE = "ListagemAlunos.csv";
    
    //Cabeçalhos das colunas do CSV
    private static final String[] HEADERS = { "Matricula", "Nome", "Idade", "DataNascimento", "Telefone", "CPF", "Index" }; 

    //Formatador de data para o formato "dd/MM/yyyy")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    
    /**
     * Salva a lista completa de alunos no arquivo CSV.
     * @param listaAlunos A lista de objetos Aluno para salvar.
     * @throws IOException Se ocorrer um erro de I/O (disco, permissão).
     */
    public static void salvarAlunosEmCsv(List<Aluno> listaAlunos) throws IOException {
        
        // 1. Define o formato: CSV padrão e especifica os cabeçalhos.
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS) 
                .build();

        // 2. Abre os streams de I/O dentro de um bloco try-with-resources (garante fechamento automático)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

            // 3. Itera sobre a lista e grava cada registro (linha) no CSV
            for (Aluno aluno : listaAlunos) {
                csvPrinter.printRecord(
                    aluno.getMatricula(),
                    aluno.getNome(),
                    aluno.getIdade(),
                    aluno.getDataNascimentoFormatadaCsv(), // Usa o formato "dd/MM/yyyy"
                    aluno.getTelefone(),
                    aluno.getCpf(),
                    aluno.getIndex() // Inclui o índice (posição de inserção original)
                );
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados no CSV: " + e.getMessage());
            // 🛑 Relança a exceção para que listaAlunos possa tratá-la e notificar o usuário
            throw e; 
        }
    }
    
    /**
     * Carrega a lista de alunos a partir do arquivo CSV.
     * @return Lista de Aluno carregada. Retorna lista vazia em caso de erro de I/O ou parsing.
     */
    public static List<Aluno> carregarAlunosDoCsv() {
        List<Aluno> listaAlunos = new LinkedList<>();
        
        // 1. Define o formato: CSV padrão, usa os cabeçalhos e pula a primeira linha (cabeçalho)
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .build();
        
        // 2. Abre os streams de I/O e o parser
        try (Reader reader = new FileReader(CSV_FILE);
             CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            // 3. Itera sobre cada registro lido (linha do CSV)
            for (CSVRecord csvRecord : csvParser) {
                
                // Mapeamento: Lê os campos usando os nomes do cabeçalho
                String matricula = csvRecord.get("Matricula");
                String nome = csvRecord.get("Nome");
                int idade = Integer.parseInt(csvRecord.get("Idade")); // Converte String para int
                String telefone = csvRecord.get("Telefone");
                String cpf = csvRecord.get("CPF");
                int index = Integer.parseInt(csvRecord.get("Index"));
                
                // Conversão da String de volta para Date usando o formatador "dd/MM/yyyy"
                String dataStr = csvRecord.get("DataNascimento");
                Date dataNascimento = DATE_FORMAT.parse(dataStr);
                
                // 4. Cria o objeto Aluno e o adiciona à lista
                Aluno aluno = new Aluno(matricula, nome, idade, dataNascimento, telefone, cpf, index); 
                listaAlunos.add(aluno);
            }
        //Captura de exceções: I/O, falha de parsing de data ou falha de conversão de número
        } catch (IOException | ParseException | NumberFormatException e) {
             System.err.println("Erro ao carregar dados do CSV: " + e.getMessage());
             // Se houver erro, retorna uma lista vazia para evitar falha do programa
             return new LinkedList<>();
        } catch (Exception e) {
             // Captura exceções mais gerais (ex: do construtor Aluno)
             System.err.println("Erro ao criar objeto Aluno durante o carregamento: " + e.getMessage());
             return new LinkedList<>();
        }
        
        return listaAlunos;
    }
}