package com.cachezheng.ai.springaidemo.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

public class WeatherTools {
    @Tool(description = "获取给定城市当前天气")
    String getCurrentWeather() {
        return "今天天气多云转晴";
    }
}
