
package ir.reza.taskmanager.repository.task;

import ir.reza.taskmanager.entity.Task;
import ir.reza.taskmanager.repository.base.BaseRepository;
import ir.reza.taskmanager.util.HibernateUtil;

import java.util.List;

public class TaskRepositoryImpl extends BaseRepository<Task>
        implements TaskRepository {

    public TaskRepositoryImpl() {
        super(Task.class);
    }

    @Override
    public List<Task> findAll() {
        return HibernateUtil.read(em ->
                em.createQuery("""
                        SELECT t
                        FROM Task t
                        LEFT JOIN FETCH t.assignee
                        LEFT JOIN FETCH t.project
                        ORDER BY t.id
                        """, Task.class)
                        .getResultList()
        );
    }

    @Override
    public Task findById(Long id) {
        return HibernateUtil.read(em ->
                em.createQuery("""
                        SELECT t
                        FROM Task t
                        LEFT JOIN FETCH t.assignee
                        LEFT JOIN FETCH t.project
                        WHERE t.id = :id
                        """, Task.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null)
        );
    }

    @Override
    public List<Task> findByAssignee(Long assigneeId) {
        return HibernateUtil.read(em ->
                em.createQuery("""
                        SELECT t
                        FROM Task t
                        LEFT JOIN FETCH t.assignee
                        LEFT JOIN FETCH t.project
                        WHERE t.assignee.id = :assigneeId
                        ORDER BY t.id
                        """, Task.class)
                        .setParameter("assigneeId", assigneeId)
                        .getResultList()
        );
    }

    @Override
    protected void copyFields(Task source, Task target) {
        target.setTitle(source.getTitle());
        target.setEstimatedMinutes(source.getEstimatedMinutes());
        target.setAssignee(source.getAssignee());
        target.setProject(source.getProject());
    }
}
