package com.mobigen.datafabric.dataLayer.service.jpaService;

import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface JpaServiceInterface<T, ID> {
    void save(T entity) throws NullPointerException, DataAccessException;
    Optional<T> findById(ID id) throws IllegalArgumentException, NoSuchElementException, DataAccessException;
    List<T> findAll() throws DataAccessException;
    void update(T entity) throws NullPointerException, DataAccessException;
    void deleteById(ID id)throws NullPointerException, IllegalArgumentException, DataAccessException;
    void delete(T entity)throws NullPointerException, DataAccessException;
}
