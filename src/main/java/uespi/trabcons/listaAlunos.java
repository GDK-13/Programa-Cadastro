/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uespi.trabcons;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

/**
 *
 * @author GDK13
 */

class listaAlunos {
    // 1. Apenas declara a lista como final (será inicializada no construtor).
    public final java.util.List<Aluno> listaAlunos;
    
    // Construtor: Carrega a lista do CSV ao iniciar.
    public listaAlunos() {
        // 2. Inicializa a lista em TODOS os caminhos (obrigatório para campos 'final').
        if (new File("alunos.csv").exists()) {
             // Chama o carregamento do CSV (tenta ler dados persistidos)
             this.listaAlunos = SwingUtils.carregarAlunosDoCsv();
        } else {
             // Se o arquivo não existe, inicializa com uma lista vazia.
             this.listaAlunos = new java.util.LinkedList<>();
        }
    }
    
    public void cadastrar(JTextField nomes, JTextField ind, JTextField matr, JTextField idades, JFormattedTextField datan, JFormattedTextField tele, JFormattedTextField cpfs){
    try {
            // 1. CAPTURA DE DADOS E CONVERSÃO DE TIPOS PRIMITIVOS
            String matricula = matr.getText();
            String nome = nomes.getText();
            
            // Validação e conversão da Data de Nascimento
            Object dateValue = datan.getValue();
            if (dateValue == null) {
                 throw new java.text.ParseException("Data de Nascimento não pode ser vazia.", 0);
            }
            Date dataNascimentoDat = (Date) dateValue;
            
            // Conversão de campos numéricos (idade e índice)
            int idade = Integer.parseInt(idades.getText());
            int index = Integer.parseInt(ind.getText());

            // Captura de campos formatados
            String telefone = tele.getText();
            String cpf = cpfs.getText();

            // 2. CRIAÇÃO DO OBJETO ALUNO
            Aluno novoAluno = new Aluno(matricula, nome, idade, dataNascimentoDat, telefone, cpf, index);
            
            // Validação de Matrícula Única (usando o método equals() da classe Aluno)
            if (this.listaAlunos.contains(novoAluno)) {
                 javax.swing.JOptionPane.showMessageDialog(null, 
                        "Erro: Já existe um aluno com a matrícula " + matricula + " na lista.", 
                        "Matrícula Duplicada", javax.swing.JOptionPane.ERROR_MESSAGE);
                throw new IllegalArgumentException("Matrícula Duplicada");
            }
            
            // Inserção na Posição (mantém a lógica de inserir no final se o índice for inválido)
            if (index < 0 || index > this.listaAlunos.size()) {
                this.listaAlunos.add(novoAluno);
                javax.swing.JOptionPane.showMessageDialog(null, "Aluno adicionado no final (índice " + this.listaAlunos.size() + ").", "Sucesso", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                this.listaAlunos.add(index, novoAluno); 
                javax.swing.JOptionPane.showMessageDialog(null, "Aluno adicionado na posição " + index + ".", "Sucesso", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            
            //SALVAMENTO. Persiste a lista atualizada no CSV.
            SwingUtils.salvarAlunosEmCsv(this.listaAlunos); 

            // Atualizar a Tabela (chamada será feita externamente pela classe de GUI)
            // atualizarTabela();

        // --- Blocos de Tratamento de Exceções ---
        } catch (NumberFormatException e) {
            // Tratamento para falha na conversão de Idade ou Index
            javax.swing.JOptionPane.showMessageDialog(null, "Erro de Conversão: Idade ou Index deve ser um número válido.", "Erro de Formato", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (java.text.ParseException e) {
            // Tratamento para falha na data
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: A data de nascimento deve ser fornecida.", "Erro de Data", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
             // Tratamento da exceção de Matrícula Duplicada
        } 
        // Tratamento de erro de I/O: Captura exceções relançadas por SwingUtils.salvarAlunosEmCsv
        catch (IOException e) { 
            javax.swing.JOptionPane.showMessageDialog(null, "Erro ao salvar dados CSV: " + e.getMessage(), "Erro de I/O", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        // Tratamento de exceções relacionadas ao ambiente gráfico
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
}