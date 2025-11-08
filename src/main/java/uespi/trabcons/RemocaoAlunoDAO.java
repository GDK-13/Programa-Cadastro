package uespi.trabcons;

import java.util.List;

/**
 * Implementação da interface AlunoDAO focada
 * na operação de remoção de alunos.
 */
public class RemocaoAlunoDAO implements AlunoDAO {

    /**
     * Remove um aluno de uma lista (modificando-a em tempo real) 
     * e retorna a mesma lista.
     *
     * A operação de remoção utiliza o método Aluno.equals()
     * (que é baseado na matrícula) para encontrar e remover
     * o item correto.
     *
     * @param alunos A lista de alunos (ex: gerenciaAlunos.listaAlunos).
     * @param a O objeto Aluno que deve ser removido.
     * @return A própria lista 'alunos', agora modificada.
     */
    @Override
    public List<Aluno> removerAluno(List<Aluno> alunos, Aluno a) {
        
        // Guard clauses: não faz nada se a lista ou o aluno forem nulos.
        if (alunos == null || a == null) {
            return alunos;
        }
        
        // Usa o método .remove(Object) da interface List.
        // O Java irá iterar pela lista e chamar 'a.equals(itemDaLista)'
        // até encontrar uma correspondência (pela matrícula).
        alunos.remove(a);
        
        // Retorna a lista original, que agora está sem o aluno.
        return alunos;
    }
    
}