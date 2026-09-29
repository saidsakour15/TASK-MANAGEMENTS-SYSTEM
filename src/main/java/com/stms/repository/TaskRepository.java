package com.stms.repository;

import com.stms.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByCourseId(Long courseId, Pageable pageable);

    Page<Task> findByLecturerId(Long lecturerId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.course.id IN " +
           "(SELECT e.course.id FROM CourseEnrollment e WHERE e.student.id = :studentId)")
    Page<Task> findTasksByEnrolledStudent(@Param("studentId") Long studentId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.course.id IN " +
           "(SELECT cl.course.id FROM CourseLecturer cl WHERE cl.lecturer.id = :lecturerId)")
    Page<Task> findTasksByLecturerId(@Param("lecturerId") Long lecturerId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.deadline > :now AND t.course.id IN " +
           "(SELECT e.course.id FROM CourseEnrollment e WHERE e.student.id = :studentId) " +
           "ORDER BY t.deadline ASC")
    List<Task> findUpcomingTasksForStudent(@Param("studentId") Long studentId, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.deadline > :now AND t.course.id IN " +
           "(SELECT cl.course.id FROM CourseLecturer cl WHERE cl.lecturer.id = :lecturerId) " +
           "ORDER BY t.deadline ASC")
    List<Task> findUpcomingTasksForLecturer(@Param("lecturerId") Long lecturerId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Task t")
    long countAllTasks();

    @Query("SELECT t.course.id, COUNT(t) FROM Task t GROUP BY t.course.id")
    List<Object[]> countTasksPerCourse();

    @Query("SELECT t FROM Task t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Task> findBySearch(@Param("search") String search, Pageable pageable);
}
