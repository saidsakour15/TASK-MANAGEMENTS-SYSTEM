package com.stms.repository;

import com.stms.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    List<CourseEnrollment> findByCourseId(Long courseId);

    List<CourseEnrollment> findByStudentId(Long studentId);

    @Query("SELECT COUNT(e) FROM CourseEnrollment e WHERE e.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(e) FROM CourseEnrollment e WHERE e.student.id = :studentId")
    long countByStudentId(@Param("studentId") Long studentId);
}
