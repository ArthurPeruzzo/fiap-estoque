package com.fiap.estoque.infra.gateway.repository;

import com.fiap.estoque.infra.gateway.entity.PecaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PecaRepository extends JpaRepository<PecaEntity, Long> {
}
