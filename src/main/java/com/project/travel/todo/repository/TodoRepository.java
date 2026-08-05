package com.project.travel.todo.repository;

import com.project.travel.todo.entity.Todo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Integer> {
    List<Todo> findByDay_DayNoOrderByCreatedAtAsc(Integer dayNo);

    @EntityGraph(attributePaths = "writer")
    List<Todo> findByDay_Record_RecordNoAndDay_Record_IsDeletedFalseOrderByCreatedAtAsc(Integer recordNo);
}
