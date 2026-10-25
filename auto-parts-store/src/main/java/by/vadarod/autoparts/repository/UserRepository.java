package by.vadarod.autoparts.repository;

import by.vadarod.autoparts.entity.Role;
import by.vadarod.autoparts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByOrderByIdAsc();

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    Optional<User> findByEmail(String email);
}
