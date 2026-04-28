package app.dearobjet.backend.domain.contract.repository;

import app.dearobjet.backend.domain.contract.entity.ContractProductStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractProductStockMovementRepository extends JpaRepository<ContractProductStockMovement, Long> {
}
