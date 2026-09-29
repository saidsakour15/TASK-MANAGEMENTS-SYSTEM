package com.stms.repository;

import com.stms.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.courseName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Course> findBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.courseLecturers cl WHERE cl.lecturer.id = :lecturerId")
    Page<Course> findByLecturerId(@Param("lecturerId") Long lecturerId, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.student.id = :studentId")
    Page<Course> findEnrolledCoursesByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    long countByStatus(String status);
}
