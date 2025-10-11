package com.deaifish.excel2db;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.deaifish.excel2db.mapper")
@Slf4j
public class Excel2dbApplication {

	public static void main(String[] args) {
		SpringApplication.run(Excel2dbApplication.class, args);
        log.info("启动成功");
	}

}
