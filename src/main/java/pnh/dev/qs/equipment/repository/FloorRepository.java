package pnh.dev.qs.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.equipment.entity.Floor;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    List<Floor> findAllByOrderByNameAsc();

    Optional<Floor> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
