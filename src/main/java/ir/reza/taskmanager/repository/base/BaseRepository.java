
package ir.reza.taskmanager.repository.base;

import ir.reza.taskmanager.util.HibernateUtil;

import java.util.List;

public abstract class BaseRepository<T> implements Repository<T> {

    private final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public List<T> findAll() {
        return HibernateUtil.read(em ->
                em.createQuery(
                        "SELECT e FROM " + entityClass.getSimpleName() + " e",
                        entityClass
                ).getResultList()
        );
    }

    @Override
    public T findById(Long id) {
        return HibernateUtil.read(em ->
                em.find(entityClass, id)
        );
    }

    @Override
    public T add(T entity) {
        return HibernateUtil.executeInTransaction(em -> {
            em.persist(entity);
            return entity;
        });
    }

    @Override
    public T update(Long id, T entity) {
        return HibernateUtil.executeInTransaction(em -> {
            T existing = em.find(entityClass, id);

            if (existing == null) {
                return null;
            }

            copyFields(entity, existing);

            return existing;
        });
    }

    @Override
    public boolean delete(Long id) {
        return HibernateUtil.executeInTransaction(em -> {
            T entity = em.find(entityClass, id);

            if (entity == null) {
                return false;
            }

            em.remove(entity);
            return true;
        });
    }

    protected abstract void copyFields(T source, T target);
}
