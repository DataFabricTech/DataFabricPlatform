package com.mobigen.datafabric.dataLayer.repository;

import jakarta.persistence.PersistenceException;

import java.util.List;

public interface JpaFunction<T, ID> {
    T findByKey(Object key) throws IllegalArgumentException, IllegalStateException;


    List<T> findAll() throws IllegalArgumentException, IllegalStateException, PersistenceException;

    List<T> findWhere(String columnName, String value) throws IllegalArgumentException, IllegalStateException, PersistenceException;

    List<T> findLike(String columnName, String value) throws IllegalArgumentException, IllegalStateException, PersistenceException;

    void insert(T entity) throws IllegalArgumentException, IllegalStateException;

    void delete(T entity) throws IllegalArgumentException, IllegalStateException;

    void deleteByKey(Object key) throws IllegalArgumentException, IllegalStateException;

    void update(T entity) throws IllegalArgumentException, IllegalStateException;


    List<T> executeJQuery(String jQuery) throws IllegalArgumentException, IllegalStateException, PersistenceException;
}
