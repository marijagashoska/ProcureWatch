package com.procurewatchbackend.repository;

import com.procurewatchbackend.model.entity.RealizedContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealizedContractRepository extends JpaRepository<RealizedContract, Long> {
}
