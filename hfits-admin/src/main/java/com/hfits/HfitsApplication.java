package com.hfits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author hfits
 */
@SpringBootApplication(
        exclude = { DataSourceAutoConfiguration.class },
        scanBasePackages = {"com.hfits" })
public class HfitsApplication {
    public static void main(String[] args)
    {
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(HfitsApplication.class, args);
        for (int i = 0; i < 3; i++) {
            System.out.println("============================应用启动成功============================");
        }
    }
}
