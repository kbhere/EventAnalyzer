package com.kartik.EventAnalyzer.repository;

import com.kartik.EventAnalyzer.model.Alert;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends CrudRepository<Alert, String> {
}
