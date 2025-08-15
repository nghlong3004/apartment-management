package vn.io.nghlong3004.apartment_management.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("vn.io.nghlong3004.apartment_management.repository")
public class MyBatisConfig {
	@Bean
	public org.apache.ibatis.session.Configuration myBatisConfiguration() {
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		return configuration;
	}
}
