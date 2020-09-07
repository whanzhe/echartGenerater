package com.ccr.echarts;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("")
public class ChartController {

    private static String temDir = "/usr";

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping(value = "/getChart")
    public String getChart(HttpServletRequest request) throws IOException {
        initFile();
        WebDriver driver = null;
        try {
            driver = getWebDriver();
            String htmlFile = getClassPath("/templates/pie.html");
            log.info(htmlFile);
            driver.get("file://" + htmlFile);
            log.info(driver.getCurrentUrl());
            Thread.sleep(2000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("title", "123");
            String i = (String) js.executeScript("return getCharts("+ JSONObject.toJSONString(paramMap) +");");
            return i;
        } catch (Exception e) {
            log.error("生成图标异常异常：", e);
        } finally {
            driver.close();
            driver.quit();
        }
        return "";
    }

    public WebDriver getWebDriver() throws IOException {
        System.setProperty("webdriver.chrome.driver", getClassPath("/driver/chromedriver"));
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);
        //设置 chrome 的无头模式
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--start-maximized");
        //因为报表页面必须滚动才能全部展示，这里直接给个很大的高度
        chromeOptions.addArguments("--window-size=1280,4300");
        return new ChromeDriver(chromeOptions);
    }

    public  String getClassPath(String fileName) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        return copyFileToDir(fileName);
    }

    public  String copyFileToDir(String fileName) throws IOException {
        return temDir + fileName;
    }

    public void initFile() throws IOException {
        String[] fileDirs ={"driver", "templates"};
        for (String fileDir : fileDirs) {
            log.info("开始复制文件");
            // ClassPathResource classPathResource = new ClassPathResource("driver/chromedriver.exe");
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(ResourceUtils.CLASSPATH_URL_PREFIX + fileDir + "/*");
            log.info("文件个数：" + resources.length);
            for (Resource resource : resources) {
               // log.info("文件个数：{}", resource.getFilename());
                InputStream inputStream = resource.getInputStream();
                File newFile = new File(temDir + File.separator + fileDir + File.separator + resource.getFilename());
                log.info(newFile.getPath());
                if (!newFile.exists()) {
                    newFile.getParentFile().mkdirs();
                    newFile.createNewFile();
                }
                OutputStream outputStream = new FileOutputStream(newFile);
                try {
                    FileCopyUtils.copy(inputStream, outputStream);
                } catch (Exception e) {

                }finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        }
    }

}
