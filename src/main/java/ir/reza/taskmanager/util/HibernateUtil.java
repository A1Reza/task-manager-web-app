
package ir.reza.taskmanager.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;
import java.util.function.Function;

public final class HibernateUtil {

    private static final String PERSISTENCE_UNIT = "taskManagerPU";

    private static EntityManagerFactory emf;

    private HibernateUtil() {
    }

    private static synchronized EntityManagerFactory getEntityManagerFactory() {

        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }

        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static <T> T executeInTransaction(
            Function<EntityManager, T> action) {

        try (EntityManager em = getEntityManager()) {

            EntityTransaction transaction = em.getTransaction();

            try {
                transaction.begin();

                T result = action.apply(em);

                transaction.commit();

                return result;

            } catch (RuntimeException e) {

                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw e;
            }
        }
    }

    public static void inTx(Consumer<EntityManager> action) {
        executeInTransaction(em -> {
            action.accept(em);
            return null;
        });
    }

    public static <T> T inTxReturn(
            Function<EntityManager, T> action) {
        return executeInTransaction(action);
    }

    public static <T> T read(Function<EntityManager, T> operation) {

        try (EntityManager em = getEntityManager()) {
            return operation.apply(em);
        }
    }

    public static synchronized void shutdown() {

        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
