package com.fiap.estoque.infra.gateway.repository;

import com.fiap.estoque.infra.gateway.entity.InsumoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsumoRepository extends JpaRepository<InsumoEntity, Long> {
}
