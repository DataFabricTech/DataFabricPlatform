package com.mobigen.datafabric.relationship.repository.fabric;

import com.mobigen.datafabric.relationship.dto.fabric.EntityRelationship;
import com.mobigen.datafabric.relationship.dto.fabric.EntityRelationshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntityRelationshipRepository extends JpaRepository<EntityRelationship, EntityRelationshipId> {

    @Query("SELECT entity " +
            "FROM EntityRelationship entity " +
            "WHERE entity.fromEntity = :fromEntity " +
            "and entity.id.toId = :toId " +
            "and entity.id.relation = :relation")
    List<EntityRelationship> findFollowers(@Param("fromEntity") String fromEntity,
                                           @Param("toId") String toId, @Param("relation") Integer relation);

    @Query("SELECT entity " +
            "FROM EntityRelationship entity " +
            "WHERE entity.toEntity = :toEntity " +
            "and entity.id.toId = :toId " +
            "and entity.id.relation = :relation")
    List<EntityRelationship> findFrom(@Param("toId") String toId,
                                       @Param("toEntity") String toEntity, @Param("relation") Integer relation);

    @Query("SELECT entity " +
            "FROM EntityRelationship entity " +
            "WHERE entity.id.toId = :toId " +
            "AND entity.toEntity = :toEntity " +
            "AND entity.id.relation = :relation " +
            "AND entity.fromEntity = :fromEntity")
    List<EntityRelationship> findFrom(@Param("toId") String toId,
                                      @Param("toEntity") String toEntity,
                                      @Param("relation") Integer relation, @Param("fromEntity") String fromEntity);

    @Query("SELECT entity " +
            "FROM EntityRelationship entity " +
            "WHERE entity.id.fromId = :fromId " +
            "AND entity.fromEntity = :fromEntity " +
            "AND entity.id.relation = :relation " +
            "AND entity.toEntity = :toEntity ")
    List<EntityRelationship> findTo(@Param("fromId") String fromId,
                                    @Param("fromEntity") String fromEntity,
                                      @Param("relation") Integer relation,
                                    @Param("toEntity") String toEntity);

    @Query("SELECT entity " +
            "FROM EntityRelationship entity " +
            "WHERE entity.id.fromId = :fromId " +
            "AND entity.fromEntity = :fromEntity " +
            "AND entity.id.relation = :relation")
    List<EntityRelationship> findTo(@Param("fromId") String fromId,
                                    @Param("fromEntity") String fromEntity,
                                    @Param("relation") Integer relation);
}