package com.stms.repository;

import com.stms.entity.CourseLecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseLecturerRepository extends JpaRepository<CourseLecturer, Long> {

    Optional<CourseLecturer> findByCourseIdAndLecturerId(Long courseId, Long lecturerId);

    boolean existsByCourseIdAndLecturerId(Long courseId, Long lecturerId);

    List<CourseLecturer> findByCourseId(Long courseId);

    List<CourseLecturer> findByLecturerId(Long lecturerId);

    void deleteByCourseIdAndLecturerId(Long courseId, Long lecturerId);

    @Query("SELECT cl FROM CourseLecturer cl WHERE cl.course.id = :courseId")
    List<CourseLecturer> findLecturersByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(cl) FROM CourseLecturer cl WHERE cl.lecturer.id = :lecturerId")
    long countByLecturerId(@Param("lecturerId") Long lecturerId);
}
