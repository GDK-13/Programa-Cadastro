package uespi.trabcons;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    // Declaração e inicialização da SessionFactory.
    // Esta instância é a coiza central do hibernate e deve ser única (Singleton).
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // Método responsável pela criação da SessionFactory.
    // A visibilidade 'private' restringe seu acesso apenas a esta classe.
    private static SessionFactory buildSessionFactory() {
        try {
            // A configuração do hibernate é carregada (geralmente via hibernate.cfg.xml),
            // e a SessionFactory é construída a parti desta configuração.
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Em caso de falha durante o processo de inicialização,
            // o erro é registrado no fluxo de saída de erro
            System.err.println("Falha ao criar a SessionFactory inicial." + ex);
            // e a execução é interrompida com uma exceção fatal de inicialização.
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Método estático de acesso à instância única da SessionFactory.
    // É o ponto de entrada para obtenção de sessões de banco de dados.
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // Método para encerramento de recursos da SessionFactory.
    // Deve ser invocado ao finalizar a aplicação para liberar caches e conexões.
    public static void shutdown() {
        // Encerramento da SessionFactory.
        getSessionFactory().close();
    }
}