package com.pgmmers.radar.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "sys.conf")
@Data
public class SystemProperties {

    private String mongoRestoreDays;

    private String machineLearning;

    private String workdir;

}
