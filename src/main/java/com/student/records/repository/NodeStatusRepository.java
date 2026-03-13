package com.student.records.repository;

import com.student.records.domain.NodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeStatusRepository extends JpaRepository<NodeStatus, String> {
}
