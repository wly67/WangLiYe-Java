package com.wly;

import com.wly.service.CourseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Demo2Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Demo2Application.class, args);

        // 初始化示例数据
        CourseService courseService = context.getBean(CourseService.class);
        if (courseService.getCourseTypeStats().isEmpty()) {
            courseService.initSampleData();
            System.out.println("示例数据初始化完成");
        }
    }

}
