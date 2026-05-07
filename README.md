# WangLiYe-Java
1.高校选课管理系统 - 学生选课基础处理工具
package com.wly.main;

import com.wly.pojo.EnrollRecord;

import java.util.*;
import java.util.stream.Collectors;



public class CourseSelectionProcessor {

   
    public List<EnrollRecord> processEnrollRecords(List<EnrollRecord> records) {
        Set<EnrollRecord> uniqueSet = new LinkedHashSet<>(records);
        List<EnrollRecord> sortedList = uniqueSet.stream()
                .sorted(Comparator.comparing(EnrollRecord::getStudentId)
                        .thenComparing(EnrollRecord::getCourseId))
                .collect(Collectors.toList());
        
        System.out.println("处理后的选课记录：");
        sortedList.forEach(record ->
                System.out.println(record.toString())
        );

        return sortedList;
    }
    
    public static void main(String[] args) {
        List<EnrollRecord> records = Arrays.asList(
                new EnrollRecord("S000001", "C000001", "Java程序设计"),
                new EnrollRecord("S000002", "C000003", "计算机网络"),
                new EnrollRecord("S000001", "C000001", "Java程序设计"), // 重复记录
                new EnrollRecord("S000001", "C000002", "数据结构"),
                new EnrollRecord("S000002", "C000001", "Java程序设计")
        );

        CourseSelectionProcessor processor = new CourseSelectionProcessor();
        List<EnrollRecord> processed = processor.processEnrollRecords(records);

        System.out.println("\n处理完成，共处理 " + records.size() + " 条记录，去重后剩余 " +
                processed.size() + " 条记录");
    }
}



2.SQL 编程题目
SELECT 
    c.course_id AS 课程ID,
    c.course_name AS 课程名称,
    COUNT(e.student_id) AS enroll_count
FROM courses c
LEFT JOIN enrollments e ON c.course_id = e.course_id
GROUP BY c.course_id, c.course_name
ORDER BY enroll_count DESC;

SELECT 
    c.course_id AS 课程ID,
    c.course_name AS 课程名称,
    COUNT(e.student_id) AS 选课人数
FROM courses c
INNER JOIN enrollments e ON c.course_id = e.course_id
WHERE c.course_type = '专业课'
GROUP BY c.course_id, c.course_name
HAVING COUNT(e.student_id) > 50
ORDER BY COUNT(e.student_id) ASC;



3.编程实战
ai:deepseek
提示词：请使用SpringBoot 3.x框架，编写一个学生选课管理系统的简单功能。具体要求如下：

1. 后端功能：
   基础功能：对选课记录进行去重（学生ID和课程ID都相同视为重复）和按课程名称排序。
   增加“选课分类”功能：课程类型分为公共课、专业课、选修课。支持在导入数据时手动标注（CSV中第四列即为课程类型）或自动识别（如果CSV中没有课程类型，则根据课程名称关键字自动识别，比如名称包含“公共”则为公共课，包含“专业”则为专业课，否则为选修课）。要求按课程类型分类存储。
   增加“选课检索”功能：支持按学生ID、课程ID、课程名称、课程类型进行关键词检索，如果检索不到则返回提示信息“无匹配选课记录”。
   性能要求：处理1000条以上记录时，检索和排序的响应时间不超过1秒，并支持单次至少500条的批量导入。

2. 页面设计：一个简单的HTML页面，包含两个核心功能：
    数据批量导入：提供一个文本区域（textarea），让用户输入CSV格式的选课数据，每行一条，格式为：学生ID,课程ID,课程名称,课程类型（示例：S000001,C000001,Java程序设计,专业课）。提供一个“导入”按钮，点击后将数据提交到后端。
   数据展示：展示导入后经过处理的选课数据，或者如果还没有导入数据，则展示后端提供的样例数据（样例数据由后端写死，包含几条选课记录）。要求按课程类型分类展示，清晰即可，无需复杂样式。

3. 前后端衔接：
   前端通过POST请求将CSV文本发送到后端的导入接口，后端处理（去重、排序、分类）后，将处理后的数据返回给前端展示。
   页面加载时，前端通过GET请求获取后端的样例数据并展示。

4. 分层设计：
   严格遵循Controller、Service、实体层（Entity）架构，业务逻辑写在Service层，Controller只负责请求转发和响应。
   实体类设计：选课记录（CourseSelection）包含属性：学生ID（studentId）、课程ID（courseId）、课程名称（courseName）、课程类型（courseType）。
   使用内存存储（如使用List）即可，无需连接数据库。

请生成完整的代码，包括：
  SpringBoot后端：Controller、Service、实体类。
  前端页面：HTML、CSS、JavaScript（使用原生，不要复杂框架）。

  SpringBoot 3.x 选课管理系统功能升级

基于您提供的现有代码，我为您实现完整的SpringBoot 3.x选课管理系统功能升级：

1. 项目结构


src/main/java/com/wly/
├── entity/
│   └── CourseSelection.java
├── repository/
│   └── CourseRepository.java
├── service/
│   └── CourseService.java
├── controller/
│   └── CourseController.java
├── dto/
│   └── ImportRequest.java
└── CourseManagementApplication.java


2. 实体类 (CourseSelection.java)

package com.wly.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_selection", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"studentId", "courseId"}))
public class CourseSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String studentId;
    
    @Column(nullable = false)
    private String courseId;
    
    @Column(nullable = false)
    private String courseName;
    
    @Column(nullable = false)
    private String courseType; // 公共课、专业课、选修课
    
    private LocalDateTime createTime;
    
    // 构造方法
    public CourseSelection() {
        this.createTime = LocalDateTime.now();
    }
    
    public CourseSelection(String studentId, String courseId, String courseName, String courseType) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseType = courseType;
        this.createTime = LocalDateTime.now();
    }
    
    // Getters 和 Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    @Override
    public String toString() {
        return String.format("学生ID：%s，课程ID：%s，课程名称：%s，课程类型：%s", 
                studentId, courseId, courseName, courseType);
    }
}


3. Repository接口 (CourseRepository.java)

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


4. Service类 (CourseService.java)

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
    
    // 课程类型识别关键字
    private static final Map<String, String> COURSE_TYPE_KEYWORDS = Map.of(
        "公共课", "英语,数学,体育,思想政治,马克思主义,毛泽东,邓小平,毛概,思修,高数,大学英语,高等数学",
        "专业课", "Java,Python,C++,数据结构,算法,数据库,操作系统,计算机网络,软件工程,编译原理,计算机组成",
        "选修课", "音乐,美术,摄影,心理学,经济学,文学,历史,哲学,电影,艺术,文化,创业"
    );
    
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


5. Controller类 (CourseController.java)

package com.wly.controller;

import com.wly.entity.CourseSelection;
import com.wly.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    /**
     * 首页 - 显示所有选课记录
     */
    @GetMapping
    public String index(Model model) {
        // 按课程类型分组展示
        Map<String, List<CourseSelection>> groupedCourses = courseService.getAllGroupedByType();
        
        // 获取统计信息
        Map<String, Long> stats = courseService.getCourseTypeStats();
        
        model.addAttribute("groupedCourses", groupedCourses);
        model.addAttribute("stats", stats);
        model.addAttribute("totalCount", groupedCourses.values().stream()
            .mapToInt(List::size)
            .sum());
        
        return "index";
    }
    
    /**
     * 批量导入CSV数据
     */
    @PostMapping("/import")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importCsv(@RequestBody Map<String, String> request) {
        String csvData = request.get("csvData");
        String[] lines = csvData.split("\n");
        
        List<String> csvLines = List.of(lines);
        List<CourseSelection> importedRecords = courseService.importBatchData(csvLines);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "成功导入 " + importedRecords.size() + " 条记录",
            "importedCount", importedRecords.size()
        ));
    }
    
    /**
     * 搜索选课记录
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "studentId") String searchType) {
        
        Map<String, Object> result = courseService.searchCourses(keyword, searchType);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 初始化示例数据
     */
    @PostMapping("/init-sample")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> initSampleData() {
        courseService.initSampleData();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "示例数据初始化成功"
        ));
    }
    
    /**
     * 清空所有数据
     */
    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllData() {
        courseService.clearAllData();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "所有数据已清空"
        ));
    }
    
    /**
     * 获取所有选课记录
     */
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllCourses() {
        Map<String, List<CourseSelection>> groupedCourses = courseService.getAllGroupedByType();
        Map<String, Long> stats = courseService.getCourseTypeStats();
        
        return ResponseEntity.ok(Map.of(
            "groupedCourses", groupedCourses,
            "stats", stats,
            "totalCount", groupedCourses.values().stream()
                .mapToInt(List::size)
                .sum()
        ));
    }
}


6. 主启动类 (CourseManagementApplication.java)

package com.wly;

import com.wly.service.CourseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CourseManagementApplication {
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CourseManagementApplication.class, args);
        
        // 初始化示例数据
        CourseService courseService = context.getBean(CourseService.class);
        if (courseService.getCourseTypeStats().isEmpty()) {
            courseService.initSampleData();
            System.out.println("示例数据初始化完成");
        }
    }
}


7. application.properties配置

# 应用配置
server.port=8080
spring.application.name=course-management

# 数据库配置
spring.datasource.url=jdbc:h2:mem:coursedb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA配置
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# H2控制台
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# 日志配置
logging.level.com.wly=DEBUG
logging.level.org.springframework.web=INFO


8. 前端页面 (src/main/resources/templates/index.html)

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学生选课管理系统</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Arial', sans-serif;
            background-color: #f5f5f5;
            padding: 20px;
            color: #333;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        header {
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #4CAF50;
        }
        
        h1 {
            color: #2c3e50;
            margin-bottom: 10px;
        }
        
        .subtitle {
            color: #7f8c8d;
            font-size: 16px;
        }
        
        .section {
            margin-bottom: 30px;
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            border: 1px solid #e9ecef;
        }
        
        h2 {
            color: #3498db;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }
        
        .form-group {
            margin-bottom: 15px;
        }
        
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #495057;
        }
        
        textarea {
            width: 100%;
            padding: 12px;
            border: 2px solid #ddd;
            border-radius: 5px;
            font-family: 'Consolas', monospace;
            font-size: 14px;
            resize: vertical;
            min-height: 150px;
        }
        
        textarea:focus {
            border-color: #4CAF50;
            outline: none;
        }
        
        .button-group {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-top: 20px;
        }
        
        button {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-weight: bold;
            transition: all 0.3s;
        }
        
        .btn-primary {
            background-color: #4CAF50;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #45a049;
        }
        
        .btn-secondary {
            background-color: #3498db;
            color: white;
        }
        
        .btn-secondary:hover {
            background-color: #2980b9;
        }
        
        .btn-danger {
            background-color: #e74c3c;
            color: white;
        }
        
        .btn-danger:hover {
            background-color: #c0392b;
        }
        
        .btn-info {
            background-color: #17a2b8;
            color: white;
        }
        
        .btn-info:hover {
            background-color: #138496;
        }
        
        .message {
            padding: 10px;
            margin-top: 10px;
            border-radius: 5px;
            display: none;
        }
        
        .message.success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .message.error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        
        .search-box {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
        }
        
        .search-input {
            flex: 1;
            padding: 10px;
            border: 2px solid #ddd;
            border-radius: 5px;
        }
        
        .search-type {
            padding: 10px;
            border: 2px solid #ddd;
            border-radius: 5px;
            background-color: white;
        }
        
        .tab-container {
            margin-bottom: 20px;
        }
        
        .tabs {
            display: flex;
            gap: 5px;
            border-bottom: 2px solid #dee2e6;
        }
        
        .tab {
            padding: 10px 20px;
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-bottom: none;
            cursor: pointer;
            border-radius: 5px 5px 0 0;
            transition: all 0.3s;
        }
        
        .tab.active {
            background-color: #4CAF50;
            color: white;
            border-color: #4CAF50;
        }
        
        .tab-content {
            display: none;
            padding: 20px;
            background-color: white;
            border: 1px solid #dee2e6;
            border-top: none;
        }
        
        .tab-content.active {
            display: block;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        
        th, td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        
        th {
            background-color: #f2f2f2;
            font-weight: bold;
            color: #333;
        }
        
        tr:hover {
            background-color: #f5f5f5;
        }
        
        .statistics {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 20px;
        }
        
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }
        
        .stat-card h3 {
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 5px;
            opacity: 0.9;
        }
        
        .stat-card .count {
            font-size: 24px;
            font-weight: bold;
        }
        
        .example {
            background-color: #e7f3ff;
            padding: 15px;
            border-radius: 5px;
            margin-top: 10px;
            font-family: 'Consolas', monospace;
            font-size: 13px;
            color: #0066cc;
        }
        
        .example-title {
            font-weight: bold;
            margin-bottom: 5px;
            color: #004085;
        }
        
        .loading {
            display: none;
            text-align: center;
            padding: 20px;
            color: #666;
        }
        
        .loading::after {
            content: "加载中...";
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>📚 学生选课管理系统</h1>
            <p class="subtitle">SpringBoot 3.x 实现 | 批量导入 | 智能分类 | 快速检索</p>
        </header>
        
        <!-- 统计信息 -->
        <div class="section">
            <h2>📊 数据概览</h2>
            <div class="statistics" id="statistics">
                <!-- 统计信息将通过JavaScript动态加载 -->
            </div>
        </div>
        
        <!-- 数据导入区 -->
        <div class="section">
            <h2>📥 批量导入选课数据</h2>
            <div class="form-group">
                <label for="csvData">请输入CSV格式的选课数据（每行一条记录）：</label>
                <textarea id="csvData" placeholder="格式示例：
S000001,C000001,Java程序设计,专业课
S000002,C000003,大学英语,公共课
S000003,C000004,数据结构,专业课
S000001,C000005,音乐欣赏,选修课"></textarea>
            </div>
            
            <div class="example">
                <div class="example-title">📋 CSV格式说明：</div>
                学号,课程号,课程名称,课程类型<br>
                • 课程类型可填：公共课、专业课、选修课<br>
                • 如不指定课程类型，系统将自动识别<br>
                • 支持单次导入≥500条记录
            </div>
            
            <div class="button-group">
                <button class="btn-primary" onclick="importData()">导入数据</button>
                <button class="btn-secondary" onclick="initSampleData()">初始化示例数据</button>
                <button class="btn-danger" onclick="clearAllData()">清空所有数据</button>
            </div>
            
            <div id="importMessage" class="message"></div>
        </div>
        
        <!-- 搜索区 -->
        <div class="section">
            <h2>🔍 选课记录检索</h2>
            <div class="search-box">
                <input type="text" id="searchKeyword" class="search-input" placeholder="请输入搜索关键词...">
                <select id="searchType" class="search-type">
                    <option value="studentId">按学生ID</option>
                    <option value="courseId">按课程ID</option>
                    <option value="courseName">按课程名称</option>
                    <option value="courseType">按课程类型</option>
                </select>
                <button class="btn-info" onclick="searchCourses()">搜索</button>
                <button class="btn-secondary" onclick="resetSearch()">重置</button>
            </div>
            <div id="searchMessage" class="message"></div>
        </div>
        
        <!-- 数据展示区 -->
        <div class="section">
            <h2>📋 选课记录列表</h2>
            <div id="loading" class="loading"></div>
            
            <div class="tab-container">
                <div class="tabs" id="courseTypeTabs">
                    <!-- 选项卡将通过JavaScript动态生成 -->
                </div>
                <div class="tab-content active" id="allCoursesTab">
                    <table id="allCoursesTable">
                        <thead>
                            <tr>
                                <th>序号</th>
                                <th>学号</th>
                                <th>课程号</th>
                                <th>课程名称</th>
                                <th>课程类型</th>
                                <th>创建时间</th>
                            </tr>
                        </thead>
                        <tbody id="allCoursesBody">
                            <!-- 数据将通过JavaScript动态加载 -->
                        </tbody>
                    </table>
                </div>
            </div>
            
            <div id="noDataMessage" style="text-align: center; padding: 40px; color: #666; display: none;">
                <p>暂无选课记录，请先导入数据或初始化示例数据。</p>
            </div>
        </div>
    </div>

    <script>
        // 页面加载完成后初始化
        document.addEventListener('DOMContentLoaded', function() {
            loadAllCourses();
        });
        
        // 加载所有选课记录
        function loadAllCourses() {
            showLoading(true);
            
            fetch('/courses/all')
                .then(response => response.json())
                .then(data => {
                    updateStatistics(data.stats, data.totalCount);
                    updateCourseTabs(data.groupedCourses);
                    renderCoursesTable(data.groupedCourses);
                    showLoading(false);
                    
                    // 显示/隐藏无数据消息
                    document.getElementById('noDataMessage').style.display = 
                        data.totalCount === 0 ? 'block' : 'none';
                })
                .catch(error => {
                    console.error('加载数据失败:', error);
                    showMessage('error', '加载数据失败: ' + error.message, 'importMessage');
                    showLoading(false);
                });
        }
        
        // 更新统计信息
        function updateStatistics(stats, totalCount) {
            const statsContainer = document.getElementById('statistics');
            let html = '';
            
            // 总记录数卡片
            html += `
                <div class="stat-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                    <h3>总记录数</h3>
                    <div class="count">${totalCount}</div>
                </div>
            `;
            
            // 各类型统计卡片
            const colors = [
                'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
                'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
            ];
            
            let colorIndex = 0;
            for (const [type, count] of Object.entries(stats)) {
                html += `
                    <div class="stat-card" style="background: ${colors[colorIndex % colors.length]}">
                        <h3>${type}</h3>
                        <div class="count">${count}</div>
                    </div>
                `;
                colorIndex++;
            }
            
            statsContainer.innerHTML = html;
        }
        
        // 更新课程类型选项卡
        function updateCourseTabs(groupedCourses) {
            const tabsContainer = document.getElementById('courseTypeTabs');
            let html = '';
            
            // 所有课程选项卡
            html += `<div class="tab active" onclick="switchTab('all')">全部课程</div>`;
            
            // 各类型课程选项卡
            for (const type in groupedCourses) {
                const count = groupedCourses[type].length;
                html += `
                    <div class="tab" onclick="switchTab('${type}')">
                        ${type} (${count})
                    </div>
                `;
            }
            
            tabsContainer.innerHTML = html;
        }
        
        // 切换选项卡
        function switchTab(type) {
            // 更新选项卡状态
            const tabs = document.querySelectorAll('.tab');
            tabs.forEach(tab => {
                if (tab.textContent.includes(type) || (type === 'all' && tab.textContent.includes('全部课程'))) {
                    tab.classList.add('active');
                } else {
                    tab.classList.remove('active');
                }
            });
            
            // 加载对应类型的数据
            if (type === 'all') {
                loadAllCourses();
            } else {
                searchByType(type);
            }
        }
        
        // 按类型搜索
        function searchByType(courseType) {
            showLoading(true);
            
            fetch(`/courses/search?searchType=courseType&keyword=${encodeURIComponent(courseType)}`)
                .then(response => response.json())
                .then(data => {
                    renderCoursesTable({[courseType]: data.records});
                    showLoading(false);
                })
                .catch(error => {
                    console.error('搜索失败:', error);
                    showLoading(false);
                });
        }
        
        // 渲染课程表格
        function renderCoursesTable(groupedCourses) {
            const tbody = document.getElementById('allCoursesBody');
            let html = '';
            let index = 1;
            
            for (const [type, courses] of Object.entries(groupedCourses)) {
                for (const course of courses) {
                    html += `
                        <tr>
                            <td>${index++}</td>
                            <td>${course.studentId}</td>
                            <td>${course.courseId}</td>
                            <td>${course.courseName}</td>
                            <td>
                                <span style="
                                    display: inline-block;
                                    padding: 3px 8px;
                                    border-radius: 12px;
                                    font-size: 12px;
                                    ${getCourseTypeStyle(course.courseType)}
                                ">
                                    ${course.courseType}
                                </span>
                            </td>
                            <td>${formatDateTime(course.createTime)}</td>
                        </tr>
                    `;
                }
            }
            
            tbody.innerHTML = html;
        }
        
        // 获取课程类型样式
        function getCourseTypeStyle(type) {
            const styles = {
                '公共课': 'background-color: #e3f2fd; color: #1565c0;',
                '专业课': 'background-color: #e8f5e8; color: #2e7d32;',
                '选修课': 'background-color: #f3e5f5; color: #7b1fa2;'
            };
            return styles[type] || 'background-color: #f5f5f5; color: #666;';
        }
        
        // 格式化日期时间
        function formatDateTime(dateTimeStr) {
            if (!dateTimeStr) return '';
            const date = new Date(dateTimeStr);
            return date.toLocaleString('zh-CN');
        }
        
        // 导入数据
        function importData() {
            const csvData = document.getElementById('csvData').value.trim();
            
            if (!csvData) {
                showMessage('error', '请输入CSV数据！', 'importMessage');
                return;
            }
            
            showLoading(true);
            
            fetch('/courses/import', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ csvData: csvData })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showMessage('success', data.message, 'importMessage');
                    document.getElementById('csvData').value = '';
                    loadAllCourses();
                } else {
                    showMessage('error', data.message || '导入失败', 'importMessage');
                }
                showLoading(false);
            })
            .catch(error => {
                showMessage('error', '导入失败: ' + error.message, 'importMessage');
                showLoading(false);
            });
        }
        
        // 搜索课程
        function searchCourses() {
            const keyword = document.getElementById('searchKeyword').value.trim();
            const searchType = document.getElementById('searchType').value;
            
            if (!keyword) {
                showMessage('error', '请输入搜索关键词！', 'searchMessage');
                return;
            }
            
            showLoading(true);
            
            fetch(`/courses/search?keyword=${encodeURIComponent(keyword)}&searchType=${searchType}`)
                .then(response => response.json())
                .then(data => {
                    if (data.records.length === 0) {
                        showMessage('error', '无匹配选课记录', 'searchMessage');
                        renderCoursesTable({});
                    } else {
                        showMessage('success', data.message, 'searchMessage');
                        renderCoursesTable({'搜索结果': data.records});
                    }
                    showLoading(false);
                })
                .catch(error => {
                    showMessage('error', '搜索失败: ' + error.message, 'searchMessage');
                    showLoading(false);
                });
        }
        
        // 重置搜索
        function resetSearch() {
            document.getElementById('searchKeyword').value = '';
            document.getElementById('searchMessage').style.display = 'none';
            loadAllCourses();
        }
        
        // 初始化示例数据
        function initSampleData() {
            if (!confirm('确定要初始化示例数据吗？这将添加10条示例选课记录。')) {
                return;
            }
            
            showLoading(true);
            
            fetch('/courses/init-sample', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showMessage('success', data.message, 'importMessage');
                    loadAllCourses();
                } else {
                    showMessage('error', data.message || '初始化失败', 'importMessage');
                }
                showLoading(false);
            })
            .catch(error => {
                showMessage('error', '初始化失败: ' + error.message, 'importMessage');
                showLoading(false);
            });
        }
        
        // 清空所有数据
        function clearAllData() {
            if (!confirm('⚠️ 警告：确定要清空所有数据吗？此操作不可恢复！')) {
                return;
            }
            
            showLoading(true);
            
            fetch('/courses/clear', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showMessage('success', data.message, 'importMessage');
                    loadAllCourses();
                } else {
                    showMessage('error', data.message || '清空失败', 'importMessage');
                }
                showLoading(false);
            })
            .catch(error => {
                showMessage('error', '清空失败: ' + error.message, 'importMessage');
                showLoading(false);
            });
        }
        
        // 显示/隐藏加载状态
        function showLoading(show) {
            document.getElementById('loading').style.display = show ? 'block' : 'none';
        }
        
        // 显示消息
        function showMessage(type, text, containerId) {
            const messageDiv = document.getElementById(containerId);
            messageDiv.textContent = text;
            messageDiv.className = 'message ' + type;
            messageDiv.style.display = 'block';
            
            setTimeout(() => {
                messageDiv.style.display = 'none';
            }, 5000);
        }
    </script>
</body>
</html>


9. pom.xml配置

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.5</version>
        <relativePath/>
    </parent>

    <groupId>com.wly</groupId>
    <artifactId>course-management</artifactId>
    <version>1.0.0</version>
    <name>Course Management System</name>
    <description>学生选课管理系统</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- H2 数据库 -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Thymeleaf 模板引擎 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- 开发工具 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

改进：创建性能监控工具类：


@Slf4j
@Component
public class PerformanceMonitor {

    private final Map<String, OperationStats> operationStats = new ConcurrentHashMap<>();
    private final Map<String, PerformanceRequirement> requirements = new ConcurrentHashMap<>();
    
    public PerformanceMonitor() {
        // 根据题目要求设置性能指标
        requirements.put("importBatchData", new PerformanceRequirement(1000, 1000)); // 1000条记录 ≤ 1秒
        requirements.put("searchCourses", new PerformanceRequirement(100, 1000));    // 搜索响应 ≤ 100ms
        requirements.put("getAllRecords", new PerformanceRequirement(200, 1000));    // 获取所有记录 ≤ 200ms
    }

  
    public MonitorContext start(String operation) {
        return new MonitorContext(operation, System.currentTimeMillis());
    }

 
    public void record(String operation, long duration) {
        OperationStats stats = operationStats.computeIfAbsent(
                operation, k -> new OperationStats()
        );
        stats.record(duration);

        // 检查是否满足性能要求
        PerformanceRequirement requirement = requirements.get(operation);
        if (requirement != null && duration > requirement.getMaxTimeMs()) {
            log.warn("性能告警: 操作 '{}' 执行时间 {}ms 超过阈值 {}ms",
                    operation, duration, requirement.getMaxTimeMs());
        }

        if (duration > 1000) { // 超过1秒记录严重警告
            log.error("性能严重告警: 操作 '{}' 执行时间过长: {}ms", operation, duration);
        }
    }

  
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        operationStats.forEach((operation, opStats) -> {
            Map<String, Object> opMetrics = new ConcurrentHashMap<>();
            opMetrics.put("count", opStats.getCount());
            opMetrics.put("totalTime", opStats.getTotalTime());
            opMetrics.put("avgTime", opStats.getAverageTime());
            opMetrics.put("maxTime", opStats.getMaxTime());
            opMetrics.put("minTime", opStats.getMinTime());

            // 添加性能达标状态
            PerformanceRequirement requirement = requirements.get(operation);
            if (requirement != null) {
                boolean passed = opStats.getAverageTime() <= requirement.getMaxTimeMs();
                opMetrics.put("requirement", requirement.getMaxTimeMs() + "ms");
                opMetrics.put("passed", passed);
                opMetrics.put("performance", passed ? "✓ 达标" : "✗ 未达标");
            }

            stats.put(operation, opMetrics);
        });

        return stats;
    }

   
    public String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(60)).append("\n");
        report.append("性能监控报告\n");
        report.append("=".repeat(60)).append("\n");

        operationStats.forEach((operation, stats) -> {
            PerformanceRequirement requirement = requirements.get(operation);
            String status = "N/A";

            if (requirement != null) {
                boolean passed = stats.getAverageTime() <= requirement.getMaxTimeMs();
                status = passed ? "✓ 达标" : "✗ 未达标";
            }

            report.append(String.format("%-20s: 调用次数=%d, 平均耗时=%.2fms, 最大耗时=%dms, 状态=%s\n",
                    operation, stats.getCount(), stats.getAverageTime(), stats.getMaxTime(), status));
        });

        report.append("=".repeat(60));
        return report.toString();
    }
    
    
    @Data
    private static class OperationStats {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalTime = new LongAdder();
        private final AtomicLong maxTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private volatile long lastUpdate = System.currentTimeMillis();

        public void record(long duration) {
            count.increment();
            totalTime.add(duration);
            maxTime.updateAndGet(current -> Math.max(current, duration));
            minTime.updateAndGet(current -> Math.min(current, duration));
            lastUpdate = System.currentTimeMillis();
        }

        public long getCount() {
            return count.longValue();
        }

        public long getTotalTime() {
            return totalTime.longValue();
        }

        public double getAverageTime() {
            long countVal = count.longValue();
            return countVal > 0 ? (double) totalTime.longValue() / countVal : 0;
        }

        public long getMaxTime() {
            return maxTime.get();
        }

        public long getMinTime() {
            long min = minTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
    }
    
    @Data
    private static class PerformanceRequirement {
        private final int maxTimeMs;
        private final int expectedRecordCount;

        public PerformanceRequirement(int maxTimeMs, int expectedRecordCount) {
            this.maxTimeMs = maxTimeMs;
            this.expectedRecordCount = expectedRecordCount;
        }
    }
}
4.分析及设计
核心数据模型：补充教师表（teachers），完善学生表、课程表、选课记录表的字段，说明表间关联关系，不局限于以上实体；能给出ER图。

并发风险：分析选课高峰期的核心并发问题，给出1个简单可行的解决方案；
索引设计：针对选课记录表、课程表，设计合理的数据库索引，说明索引类型及设计理由。
