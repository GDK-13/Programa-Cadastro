package uespi.trabcons;

import java.text.ParseException; // Necessário para a exceção lançada no construtor (embora a lógica de parsing esteja fora)
import java.text.SimpleDateFormat; // Necessário para formatar a data para o CSV/Exibição
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

@Entity
@Table(name = "alunos")
public class Aluno implements Serializable {
    
    // --- Atributos de Estado do Aluno ---
    
    @Id
    @Column(name = "matricula")
    private String matricula; // Chave primária (única)
    
    @Column(name = "nome")
    private String nome;
    
    @Column(name = "idade")
    private int idade;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "data_nascimento")
    private Date dataNascimento; // Usado para armazenar a data internamente
    
    @Column(name = "telefone")
    private String telefone;
    
    @Column(name = "cpf")
    private String cpf;
    
    @Transient
    private int index; // Usado para controle de posição na lista (em tempo de execução)
    
    /**
     * Construtor padrão (vazio) exigido pelo Hibernate.
     */
    public Aluno() {
    }

    /**
     * Construtor para criar um novo Aluno.
     * @param matricula
     * @param nome
     * @param idade
     * @param dataNascimentoDat
     * @param telefone
     * @param cpf
     * @param index
     * @throws java.text.ParseException
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
    
    // Sobrescrita essencial para verificar Matrículas Duplicadas na listaAlunos (usado por List.contains())
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Aluno aluno = (Aluno) obj;
        // Duas matrículas são iguais se a string da matrícula for a mesma.
        return matricula.equals(aluno.matricula);
    }
    
    // Sobrescrita essencial para manter a consistência com equals (usado por estruturas hash)
    @Override
    public int hashCode() {
        return matricula.hashCode(); // Baseia o hash code na matrícula, garantindo unicidade.
    }
    
    // Formatador estático (único) para garantir que o CSV use o formato "dd/MM/yyyy"
    @Transient
    private static final SimpleDateFormat CSV_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    // Sobrescrita padrão para representação em String (útil para debug ou JList)
    @Override
    public String toString() {
        // Chama o novo método de formatação para exibir a data de forma amigável
        return String.format("Matrícula: %s | Nome: %s | Idade: %d | Data Nasc.: %s",
                matricula, nome, idade, getDataNascimentoFormatadaCsv());
    }
    
    // Método auxiliar que retorna a data no formato Date.toString()
    public String Data(){
        return String.format("%s", dataNascimento);
    }

    
    // Métodos Getters e Setters
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

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

    // Getter para fins de persistência/caminho de código antigo que usa Date.toString()
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
    
    //Getter final para uso no salvamento/exibição do CSV (garante formato dd/MM/yyyy)
    @Transient
    public String getDataNascimentoFormatadaCsv() {
        // Tenta formatar a data
        try {
            return CSV_DATE_FORMAT.format(this.dataNascimento);
        } catch (Exception e) {
            // Retorna uma string de erro se a data for nula ou inválida
            return "Data Inválida";
        }
    }
}