package uespi.trabcons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.Serializable;

// Importações do Jakarta (Hibernate)
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

/**
 * Esta é a nossa classe principal: Aluno.
 *
 * O "@Entity" diz ao Hibernate (o cara que salva no banco) que esta classe
 * é uma tabela no banco de dados.
 * O "@Table" especifica o nome dessa tabela, que é "alunos".
 */
@Entity
@Table(name = "alunos")
public class Aluno implements Serializable {

    // --- Atributos de Estado do Aluno (Colunas da Tabela) ---

    /**
     * @Id: Marca este campo como a chave primária (o identificador único)
     * da tabela.
     * @Column: Especifica que o nome da coluna no banco é "matricula".
     */
    @Id
    @Column(name = "matricula")
    private String matricula; // A matrícula é a chave primária.

    @Column(name = "nome")
    private String nome;

    @Column(name = "idade")
    private int idade;

    /**
     * @Temporal(TemporalType.DATE): Diz ao Hibernate para guardar só a data,
     * ignorando a hora (tipo DATE no SQL).
     * @Column: Coluna no banco chamada "data_nascimento".
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "data_nascimento")
    private Date dataNascimento; // Usamos Date para guardar a data internamente.

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "cpf")
    private String cpf;

    /**
     * @Transient: Esta anotação diz ao Hibernate para **ignorar** este campo.
     * Ele não vai para o banco de dados. É só para uso em tempo de execução
     * (por exemplo, para controlar a posição em uma lista).
     */
    @Transient
    private int index; // Usado para controle de posição na lista em tempo de execução.

    /**
     * Construtor padrão (vazio). O Hibernate precisa dele para criar objetos
     * quando lê os dados do banco.
     */
    public Aluno() {
    }

    /**
     * Construtor completo para criar um Aluno do zero.
     * Ele recebe todos os dados e joga nas váriaveis da classe.
     */
    public Aluno(String matricula, String nome, int idade, Date dataNascimentoDat, String telefone, String cpf, int index) throws ParseException {
        this.matricula = matricula;
        this.nome = nome;
        this.idade = idade;
        this.dataNascimento = dataNascimentoDat;
        this.telefone = telefone;
        this.cpf = cpf;
        this.index = index;
    }

    /**
     * Sobrescrita do 'equals()'. É crucial!
     *
     * Permite que a gente compare dois objetos Aluno. Eles são considerados
     * iguais se tiverem a **mesma matrícula**. Isso é muito útil para verificar
     * se você já tem um aluno com aquela matrícula em uma lista.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Aluno aluno = (Aluno) obj;
        // A comparação se baseia só na string da matrícula.
        return matricula.equals(aluno.matricula);
    }

    /**
     * Sobrescrita do 'hashCode()'.
     *
     * Tem que ser feita junto com o 'equals()' para que o java saiba onde
     * guardar e como buscar este objeto em estruturas de dados mais rápidas
     * (tipo HashMaps). Ele usa a matrícula para gerar um código único.
     */
    @Override
    public int hashCode() {
        return matricula.hashCode(); // Usa o código da matrícula para o hash.
    }

    /**
     * O 'SimpleDateFormat' é o formato de data que a gente vai usar para
     * exportar para CSV ou mostrar na tela. Ele é estático e 'transient'
     * (não vai para o banco) para ser compartilhado por todos os alunos.
     * O formato escolhido é "dd/mm/yyyy".
     */
    @Transient
    private static final SimpleDateFormat CSV_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Sobrescrita do 'toString()'.
     *
     * Define como o objeto Aluno deve ser exibido como uma string,
     * o que é útil para debug ou para colocar o objeto diretamente em
     * um componente de lista (como um JList).
     */
    @Override
    public String toString() {
        return String.format("Matrícula: %s | Nome: %s | Idade: %d | Data Nasc.: %s",
                matricula, nome, idade, getDataNascimentoFormatadaCsv());
    }

    // Método auxiliar (parece redundânte, mas talvez tenha sido criado pra algum propósito específico)
    public String Data(){
        return String.format("%s", dataNascimento);
    }


    // --- Métodos Getters e Setters (para acessar e alterar os atributos) ---

    // Este é um dos poucos setters que existe
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    // Getters e Setters para o restante das propriedades
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public Date getDataNascimento() {
        return dataNascimento;
    }

    /**
     * Getter extra para a data. Ele retorna a data formatada
     * usando o 'Date.toString()' padrão do java, o que normalmente
     * não é o ideal, mas tá aqui pra compatibilidade.
     */
    @Transient
    public String getDataNascimentoFormatada() {
        return dataNascimento.toString();
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getMatricula() {
        return matricula;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    /**
     * Getter mais importante para data!
     *
     * Retorna a data de nascimento no formato **dd/MM/yyyy**.
     * É o formato certo para salvar em arquivos CSV ou para
     * exibir ao úsuario.
     */
    @Transient
    public String getDataNascimentoFormatadaCsv() {
        // Tenta formatar. Se a data for nula ou algo der erro,
        // retorna a mensagem de erro.
        try {
            return CSV_DATE_FORMAT.format(this.dataNascimento);
        } catch (Exception e) {
            return "Data Inválida"; // Mensagem de erro.
        }
    }
}