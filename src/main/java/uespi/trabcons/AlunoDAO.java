package uespi.trabcons;

import java.util.List;

/**
 * Interface (Contrato) para operações de acesso a dados (DAO)
 * relacionadas à entidade Aluno.
 */
public interface AlunoDAO {

    /**
     * Remove um objeto Aluno de uma lista de alunos.
     *
     * @param alunos A lista da qual o aluno será removido.
     * @param a O objeto Aluno a ser removido.
     * @return A lista modificada após a remoção.
     * Sera chamada pela classe RemocaoAlunoDAO como pedido pelo professor
     */
    public List<Aluno> removerAluno(List<Aluno> alunos, Aluno a);
    
}