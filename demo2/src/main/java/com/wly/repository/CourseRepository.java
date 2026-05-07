package com.wly.repository;

import com.wly.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<CourseSelection, Long> {

    // 根据学生ID查找
    List<CourseSelection> findByStudentId(String studentId);

    // 根据课程ID查找
    List<CourseSelection> findByCourseId(String courseId);

    // 根据课程名称模糊查询
    List<CourseSelection> findByCourseNameContaining(String courseName);

    // 根据课程类型查找
    List<CourseSelection> findByCourseType(String courseType);

    // 联合去重查找
    CourseSelection findByStudentIdAndCourseId(String studentId, String courseId);

    // 多条件组合查询
    @Query("SELECT c FROM CourseSelection c WHERE " +
            "(:studentId is null or c.studentId = :studentId) AND " +
            "(:courseId is null or c.courseId = :courseId) AND " +
            "(:courseName is null or c.courseName like %:courseName%) AND " +
            "(:courseType is null or c.courseType = :courseType) " +
            "ORDER BY c.studentId, c.courseId")
    List<CourseSelection> search(
            @Param("studentId") String studentId,
            @Param("courseId") String courseId,
            @Param("courseName") String courseName,
            @Param("courseType") String courseType
    );

    // 按课程类型分组统计
    @Query("SELECT c.courseType, COUNT(c) FROM CourseSelection c GROUP BY c.courseType")
    List<Object[]> countByCourseType();
}