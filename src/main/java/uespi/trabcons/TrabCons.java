/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package uespi.trabcons;

/**
 * Ponto de entrada principal da aplicação.
 */
public class TrabCons {

    // Este é o método principal que inicia o programa.
    public static void main(String[] args) {
        // Criação de uma instância unica da classe que gerencia os alunos.
        // Isso garante que todos os dados operem sobre a mesma lista.
        final listaAlunos gerenciaAlunos = new listaAlunos();

        // Inicialização da interface grafica (a tela principal).
        // A instância de 'gerenciaAlunos' é passada para a tela
        // para que a tela possa manipular os dados.
        TrabConsFrame.Tela(gerenciaAlunos);
    }
}