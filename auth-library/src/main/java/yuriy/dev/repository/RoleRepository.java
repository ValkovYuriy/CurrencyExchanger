package yuriy.dev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yuriy.dev.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    @Query("SELECT r from Role r where r.role = :role")
    Optional<Role> findByName(String role);

    @Query("SELECT r from Role r where r.role in :roles")
    List<Role> findByRoles(@Param("roles") List<String> roles);
}
