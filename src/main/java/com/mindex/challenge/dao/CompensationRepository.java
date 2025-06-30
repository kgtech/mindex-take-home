package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompensationRepository extends MongoRepository<Compensation, String> {

    List<Compensation> findByEmployeeId(String employeeId);
    Compensation findByEmployeeIdAndIsActive(String employeeId, boolean isActive);
    List<Compensation> findByEmployeeIdOrderByEffectiveDateDesc(String employeeId);
}