package uespi.trabcons;

import java.util.List;

/**
 * Interface para operações de acesso a dados (DAO)
 * relacionadas à entidade Aluno.
 */
public interface AlunoDAO {

    /**
     * Remove um objeto Aluno de uma lista de alunos.
     *
     * Este método vai ser a função principal pra tirar um aluno
     * da lista. 
     *
     * @param alunos
     * @param a
     * @return
     */
    public List<Aluno> removerAluno(List<Aluno> alunos, Aluno a);
    
}