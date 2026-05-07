package com.wly.controller;

import com.wly.entity.CourseSelection;
import com.wly.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
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

        List<String> csvLines = Arrays.asList(lines);
        List<CourseSelection> importedRecords = courseService.importBatchData(csvLines);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "成功导入 " + importedRecords.size() + " 条记录");
        response.put("importedCount", importedRecords.size());

        return ResponseEntity.ok(response);
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
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "示例数据初始化成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 清空所有数据
     */
    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllData() {
        courseService.clearAllData();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "所有数据已清空");

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有选课记录
     */
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllCourses() {
        Map<String, List<CourseSelection>> groupedCourses = courseService.getAllGroupedByType();
        Map<String, Long> stats = courseService.getCourseTypeStats();

        Map<String, Object> response = new HashMap<>();
        response.put("groupedCourses", groupedCourses);
        response.put("stats", stats);
        response.put("totalCount", groupedCourses.values().stream()
                .mapToInt(List::size)
                .sum());

        return ResponseEntity.ok(response);
    }
}