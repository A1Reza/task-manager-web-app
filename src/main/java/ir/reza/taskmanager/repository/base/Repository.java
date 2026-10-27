package ir.reza.taskmanager.repository.base;

import java.util.List;

public interface Repository<T> {

    List<T> findAll();

    T findById(Long id);

    T add(T entity);

    T update(Long id, T entity);

    boolean delete(Long id);
}
