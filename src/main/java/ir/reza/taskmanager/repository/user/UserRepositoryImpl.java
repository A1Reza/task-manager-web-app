package ir.reza.taskmanager.repository.user;

import ir.reza.taskmanager.entity.User;
import ir.reza.taskmanager.repository.base.BaseRepository;

public class UserRepositoryImpl extends BaseRepository<User>
        implements UserRepository {

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    protected void copyFields(User source, User target) {
        target.setName(source.getName());
        target.setEmail(source.getEmail());
    }
}