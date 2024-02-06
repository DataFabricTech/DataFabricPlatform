package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.generateKey;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class JpaService<T, ID> implements JpaServiceInterface<T, ID> {
    private final JpaRepository<T, ID> repository;

    public JpaService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public void save(T entity) throws IllegalStateException, NullPointerException, DataAccessException {
        if (repository.findById((ID) ((generateKey) entity).generateKey()).isPresent())
            throw new IllegalStateException("Entity Already exist with id: " + ((generateKey) entity).generateKey());
        else
            repository.save(entity);
        // todo openSearch save function
    }

    @Override
    public Optional<T> findById(ID id) throws IllegalArgumentException, NoSuchElementException, DataAccessException {
        return repository.findById(id);
    }

    @Override
    public List<T> findAll() throws DataAccessException {
        return repository.findAll();
    }

    @Override
    public void update(T entity) throws NullPointerException, DataAccessException {
        if (repository.findById((ID) ((generateKey) entity).generateKey()).isPresent())
            repository.save(entity);
        else
            throw new IllegalStateException("Entity doesn't exist with id: " + ((generateKey) entity).generateKey());
    }


    @Override
    public void deleteById(ID id) throws NullPointerException, IllegalArgumentException, DataAccessException {
        repository.deleteById(id);
    }

    @Override
    public void delete(T entity) throws NullPointerException, DataAccessException {
        repository.delete(entity);
    }
}
