<img width="1215" height="619" alt="image" src="https://github.com/user-attachments/assets/154afa58-11f9-4f76-9e1b-e385838aa0bd" /># WangLiYe-Java
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

代码：demo2

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
4.分析及设计:

核心数据模型：补充教师表（teachers），完善学生表、课程表、选课记录表的字段，说明表间关联关系，不局限于以上实体；能给出ER图。
图中由er图

并发风险：分析选课高峰期的核心并发问题，给出1个简单可行的解决方案；
  当多个学生同时选择同一门尚未满员的课程时，可能导致选课人数超过课程容量限制
  数据库乐观锁 + 条件更新

索引设计：针对选课记录表、课程表，设计合理的数据库索引，说明索引类型及设计理由。
 索引设计
选课记录表索引设计
主索引：(student_id, course_id)复合主键索引，保证学生选课唯一性，支持按学生查询。辅助索引：course_id索引支持按课程查询选课情况，enroll_time索引支持时间范围查询。复合索引：(student_id, enroll_time)支持查询学生选课历史，(course_id, enroll_time)支持课程选课趋势分析。
课程表索引设计
主键索引：course_id主键索引确保课程唯一标识。业务索引：course_name索引支持课程名称搜索，course_type索引支持按课程类型筛选。性能索引：(capacity, current_enrollment)复合索引支持查询剩余名额，current_enrollment单字段索引支持热门课程排序。

设计原则：高频查询字段优先建索引，复合索引遵循最左前缀原则，平衡读写性能，避免过度索引。
