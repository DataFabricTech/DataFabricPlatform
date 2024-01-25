package com.mobigen.datafabric.dataLayer.repository;

import dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.List;
import java.util.UUID;

public class JPARepository<T> implements JpaFunction<T, UUID> {
    private final EntityManager em;
    private final Class<T> entityClass;

    public JPARepository(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
    }

    @Override
    public T findByKey(Object key) throws IllegalArgumentException, IllegalStateException {
        return em.find(entityClass, key);
    }

    @Override
    public List<T> findAll() throws IllegalArgumentException, IllegalStateException, PersistenceException {
        return em.createQuery(String.format("select e from %s e", entityClass.getSimpleName()), entityClass).getResultList();
    }

    @Override
    public List<T> findWhere(String columnName, String value) throws IllegalArgumentException, IllegalStateException, PersistenceException {
        var sb = new StringBuilder();
        sb.append("select e from ").append(entityClass.getSimpleName()).append(" e where e.").append(columnName).append(" = :value");
        return em.createQuery(sb.toString(), entityClass)
                .setParameter("value", value)
                .getResultList();
    }

    @Override
    public List<T> findLike(String columnName, String value) throws IllegalArgumentException, IllegalStateException, PersistenceException {
        var sb = new StringBuilder();
        sb.append("select e from ").append(entityClass.getSimpleName()).append(" e where e.").append(columnName).append(" like :value");
        return em.createQuery(sb.toString(), entityClass)
                .setParameter("value", value)
                .getResultList();
    }

    @Override
    public void insert(T entity) throws IllegalArgumentException, IllegalStateException {
        em.persist(entity);
    }

    @Override
    public void delete(T entity) throws IllegalArgumentException, IllegalStateException {
        em.remove(entity);
    }

    @Override
    public void deleteByKey(Object key) throws IllegalArgumentException, IllegalStateException {
        var entity = em.find(entityClass, key);
        if (entity != null)
            em.remove(em.find(entityClass, key));
    }

    @Override
    public void update(T entity) throws IllegalArgumentException, IllegalStateException {
        if (findByKey(((generateKey) entity).generateKey()) != null) {
            em.merge(entity);
        } else {
            throw new IllegalArgumentException("Not Exist Row Error");
        }

    }


    @Override
    public List<T> executeJQuery(String jQuery) throws IllegalArgumentException, IllegalStateException, PersistenceException {
        return em.createQuery(jQuery, entityClass)
                .getResultList();
    }
}
