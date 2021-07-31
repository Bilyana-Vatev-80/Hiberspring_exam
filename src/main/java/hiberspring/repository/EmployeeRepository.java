package hiberspring.repository;

import hiberspring.domain.entities.Employee;
import hiberspring.domain.entities.EmployeeCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    @Query(value = "select * from employees as e\n" +
            "join branch b on e.branch_id = b.id\n" +
            "join products p on b.id = p.branch_id\n" +
            "group by e.id\n" +
            "order by concat (e.first_name,' ', e.last_name) , " +
            "length(e.position) desc",nativeQuery = true)
    List<Employee> findAllByBranchWithProduct();
}
