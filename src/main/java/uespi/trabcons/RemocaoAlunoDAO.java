package uespi.trabcons;

import java.util.List;

/**
 * Esta é a classe 'RemocaoAlunoDAO'.
 * Ela é responsável por implementar o método de exclusão
 * de um aluno de uma lista.
 */
public class RemocaoAlunoDAO implements AlunoDAO {

    /**
     * Remove um objeto Aluno de uma lista de alunos.
     * A lista é modificada diretamente no local.
     *
     * @param alunos A lista que contém os alunos.
     * @param a O objeto Aluno a ser removido da lista.
     * @return A própria lista 'alunos', agora modificada.
     */
    @Override
    public List<Aluno> removerAluno(List<Aluno> alunos, Aluno a) {

        // Cláusulas de guarda: verifica se a lista ou o aluno são nulos.
        // Se um deles for nulo, a operação é encerrada
        // e a lista original é devolvida, sem alterassão.
        if (alunos == null || a == null) {
            return alunos;
        }

        // O método 'remove(Object)' é invocado na lista.
        // A remoção depende da implementação do método 'equals()' na classe Aluno,
        // garantindo que o aluno correto (pela matrícula) seja encontrado e retirado.
        alunos.remove(a);

        // A lista já está modificada. Ela é retornada para finalizar a operação.
        return alunos;
    }

}