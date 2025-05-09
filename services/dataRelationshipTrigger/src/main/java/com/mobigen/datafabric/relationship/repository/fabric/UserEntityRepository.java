package com.mobigen.datafabric.relationship.repository.fabric;

import com.mobigen.datafabric.relationship.dto.fabric.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<UserEntity, String> {
}