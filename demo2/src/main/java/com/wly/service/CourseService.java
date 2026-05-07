package com.wly.service;

import com.wly.entity.CourseSelection;
import com.wly.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    private static final Map<String, String> COURSE_TYPE_KEYWORDS = new HashMap<>();

    static {
        COURSE_TYPE_KEYWORDS.put("公共课", "英语,数学,体育,思想政治,马克思主义,毛泽东,邓小平,毛概,思修,高数,大学英语,高等数学");
        COURSE_TYPE_KEYWORDS.put("专业课", "Java,Python,C++,数据结构,算法,数据库,操作系统,计算机网络,软件工程,编译原理,计算机组成");
        COURSE_TYPE_KEYWORDS.put("选修课", "音乐,美术,摄影,心理学,经济学,文学,历史,哲学,电影,艺术,文化,创业");
    }

    /**
     * 批量导入选课数据
     */
    @Transactional
    public List<CourseSelection> importBatchData(List<String> csvLines) {
        List<CourseSelection> records = new ArrayList<>();

        for (String line : csvLines) {
            if (StringUtils.hasText(line)) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String studentId = parts[0].trim();
                    String courseId = parts[1].trim();
                    String courseName = parts[2].trim();
                    String courseType = parts.length >= 4 ? parts[3].trim() : "";

                    // 自动识别课程类型
                    if (!StringUtils.hasText(courseType)) {
                        courseType = autoDetectCourseType(courseName);
                    }

                    records.add(new CourseSelection(studentId, courseId, courseName, courseType));
                }
            }
        }

        // 去重（基于studentId + courseId）
        Map<String, CourseSelection> uniqueMap = new HashMap<>();
        for (CourseSelection record : records) {
            String key = record.getStudentId() + "_" + record.getCourseId();
            uniqueMap.put(key, record);
        }

        List<CourseSelection> uniqueRecords = new ArrayList<>(uniqueMap.values());

        // 排序（先按学生ID，再按课程ID）
        uniqueRecords.sort(Comparator
                .comparing(CourseSelection::getStudentId)
                .thenComparing(CourseSelection::getCourseId));

        // 批量保存到数据库
        return courseRepository.saveAll(uniqueRecords);
    }

    /**
     * 自动识别课程类型
     */
    private String autoDetectCourseType(String courseName) {
        for (Map.Entry<String, String> entry : COURSE_TYPE_KEYWORDS.entrySet()) {
            String[] keywords = entry.getValue().split(",");
            for (String keyword : keywords) {
                if (courseName.contains(keyword.trim())) {
                    return entry.getKey();
                }
            }
        }
        return "选修课"; // 默认
    }

    /**
     * 搜索选课记录
     */
    public Map<String, Object> searchCourses(String keyword, String searchType) {
        Map<String, Object> result = new HashMap<>();
        List<CourseSelection> searchResults = new ArrayList<>();

        if (!StringUtils.hasText(keyword)) {
            // 如果关键词为空，返回所有记录
            searchResults = courseRepository.findAll();
        } else {
            switch (searchType) {
                case "studentId":
                    searchResults = courseRepository.findByStudentId(keyword);
                    break;
                case "courseId":
                    searchResults = courseRepository.findByCourseId(keyword);
                    break;
                case "courseName":
                    searchResults = courseRepository.findByCourseNameContaining(keyword);
                    break;
                case "courseType":
                    searchResults = courseRepository.findByCourseType(keyword);
                    break;
            }
        }

        // 排序
        searchResults.sort(Comparator
                .comparing(CourseSelection::getStudentId)
                .thenComparing(CourseSelection::getCourseId));

        result.put("records", searchResults);
        result.put("count", searchResults.size());
        result.put("message", searchResults.isEmpty() ? "无匹配选课记录" : "找到" + searchResults.size() + "条记录");

        return result;
    }

    /**
     * 获取所有选课记录（按课程类型分组）
     */
    public Map<String, List<CourseSelection>> getAllGroupedByType() {
        List<CourseSelection> allRecords = courseRepository.findAll();

        // 按课程类型分组
        Map<String, List<CourseSelection>> grouped = allRecords.stream()
                .collect(Collectors.groupingBy(CourseSelection::getCourseType));

        // 对每组进行排序
        grouped.forEach((type, records) ->
                records.sort(Comparator
                        .comparing(CourseSelection::getStudentId)
                        .thenComparing(CourseSelection::getCourseId))
        );

        return grouped;
    }

    /**
     * 获取课程类型统计
     */
    public Map<String, Long> getCourseTypeStats() {
        List<Object[]> stats = courseRepository.countByCourseType();
        Map<String, Long> result = new HashMap<>();

        for (Object[] stat : stats) {
            String type = (String) stat[0];
            Long count = (Long) stat[1];
            result.put(type, count);
        }

        return result;
    }

    /**
     * 初始化示例数据
     */
    public void initSampleData() {
        List<CourseSelection> samples = Arrays.asList(
                new CourseSelection("S000001", "C000001", "Java程序设计", "专业课"),
                new CourseSelection("S000002", "C000002", "大学英语", "公共课"),
                new CourseSelection("S000003", "C000003", "数据结构", "专业课"),
                new CourseSelection("S000004", "C000004", "高等数学", "公共课"),
                new CourseSelection("S000005", "C000005", "音乐欣赏", "选修课"),
                new CourseSelection("S000001", "C000006", "计算机网络", "专业课"),
                new CourseSelection("S000002", "C000007", "马克思主义原理", "公共课"),
                new CourseSelection("S000006", "C000008", "Python编程", "专业课"),
                new CourseSelection("S000007", "C000009", "体育", "公共课"),
                new CourseSelection("S000008", "C000010", "摄影基础", "选修课")
        );

        courseRepository.saveAll(samples);
    }

    /**
     * 清除所有数据
     */
    @Transactional
    public void clearAllData() {
        courseRepository.deleteAll();
    }
}