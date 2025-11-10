package uespi.trabcons;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/**
 * Classe utilitaria que contém métodos para interface gráfica (Swing) e
 * gravação/leitura de dados em arquivo CSV.
 */
public class SwingUtils {

    // 1. UTILITÁRIO DE INTERFACE (SWING)

    /**
     * Implementa o comportamento de placeholder (texto de dica) em um campo de texto.
     * @param campo
     * @param placeholder
     */
    public static void configurarPlaceholder(JTextField campo, String placeholder) {

        // 1. O texto inicial é definido como o placeholder se o campo estiver vazio.
        // Garante que a dica apareça ao iniciar.
        if (campo.getText() == null || campo.getText().isEmpty() || campo.getText().equals(placeholder)) {
             campo.setText(placeholder);
        }

        // 2. Um 'ouvinte de foco' é adicionado para gerenciar o comportamento do campo.
        campo.addFocusListener(new FocusAdapter() {

            // Ação quando o campo recebe foco (o usuário clica).
            @Override
            public void focusGained(FocusEvent evt) {
                // Se o texto atual for o placeholder, ele é removido para que a digitação comece.
                if (campo.getText().equals(placeholder)) {
                    campo.setText("");
                }
            }

            // Ação quando o campo perde foco (o usuário clica fora).
            @Override
            public void focusLost(FocusEvent evt) {
                // Se o campo permanecer vazio, o placeholder é restaurado.
                if (campo.getText().isEmpty()) {
                    campo.setText(placeholder);
                }
            }
        });
    }

    // 2. PERSISTÊNCIA DE DADOS (CSV)

    // Constante que define o nome do arquivo para gravação.
    private static final String CSV_FILE = "ListagemAlunos.csv";

    // Array com os cabeçalhos das colunas do arquivo CSV.
    private static final String[] HEADERS = { "Matricula", "Nome", "Idade", "DataNascimento", "Telefone", "CPF", "Index" };

    // Formatador de data padronizado para "dd/MM/yyyy".
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");


    /**
     * Grava o conteúdo completo da lista de alunos no arquivo CSV.
     * @param listaAlunos
     * @throws java.io.IOException
     */
    public static void salvarAlunosEmCsv(List<Aluno> listaAlunos) throws IOException {

        // 1. Configuração do formato CSV, incluindo os cabeçalhos.
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        // 2. Abertura dos fluxos de escrita de arquivo e do printer CSV.
        // O 'try-with-resources' garante que os recursos sejam fechados automaticamente.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

            // 3. Iteração sobre cada aluno na lista.
            for (Aluno aluno : listaAlunos) {
                // Gravação dos atributos do aluno como um novo registro (linha) no CSV.
                csvPrinter.printRecord(
                    aluno.getMatricula(),
                    aluno.getNome(),
                    aluno.getIdade(),
                    aluno.getDataNascimentoFormatadaCsv(), // Uso do formato "dd/MM/yyyy"
                    aluno.getTelefone(),
                    aluno.getCpf(),
                    aluno.getIndex() // Gravação do campo 'Index' para manutenção de ordem
                );
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados no CSV: " + e.getMessage());
            // Relança a exceção para que o chamador trate o problema de I/O.
            throw e;
        }
    }

    /**
     * Efetua a leitura do arquivo CSV e constrói uma lista de objetos Aluno.
     * @return 
     */
    public static List<Aluno> carregarAlunosDoCsv() {
        // Inicialização da lista que receberá os dados.
        List<Aluno> listaAlunos = new LinkedList<>();

        // 1. Configuração do formato CSV para leitura: usa cabeçalhos e ignora a linha inicial.
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .build();

        // 2. Abertura dos fluxos de leitura de arquivo e do parser CSV.
        try (Reader reader = new FileReader(CSV_FILE);
             CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            // 3. Iteração sobre cada registro (linha de dado) lido pelo parser.
            for (CSVRecord csvRecord : csvParser) {

                // Leitura dos campos pelo nome do cabeçalho.
                String matricula = csvRecord.get("Matricula");
                String nome = csvRecord.get("Nome");
                int idade = Integer.parseInt(csvRecord.get("Idade")); // Conversão explícita para inteiro
                String telefone = csvRecord.get("Telefone");
                String cpf = csvRecord.get("CPF");
                int index = Integer.parseInt(csvRecord.get("Index"));

                // Conversão da string de data lida para um objeto Date.
                String dataStr = csvRecord.get("DataNascimento");
                Date dataNascimento = DATE_FORMAT.parse(dataStr);

                // 4. Criação do novo objeto Aluno e adição à lista.
                Aluno aluno = new Aluno(matricula, nome, idade, dataNascimento, telefone, cpf, index);
                listaAlunos.add(aluno);
            }
        // Tratamento de exceções na leitura e conversão.
        } catch (IOException | ParseException | NumberFormatException e) {
             System.err.println("Erro ao carregar dados do CSV: " + e.getMessage());
             // Se houver erros graves, retorna uma lista vazia, evitando a falha total do sistema.
             return new LinkedList<>();
        } catch (Exception e) {
             // Captura de outras exceções, como erro na construção do objeto Aluno.
             System.err.println("Erro ao criar objeto Aluno durante o carregamento: " + e.getMessage());
             return new LinkedList<>();
        }

        // Retorna a lista de alunos populada.
        return listaAlunos;
    }
}