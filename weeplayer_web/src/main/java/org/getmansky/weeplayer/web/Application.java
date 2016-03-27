package org.getmansky.weeplayer.web;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;

import java.io.File;

@ComponentScan
@EnableAutoConfiguration
public class Application implements CommandLineRunner{

   public static final String STORAGE_PATH_KEY = "weeplayer.storage.path";
   private static final Logger log = LoggerFactory.getLogger(Application.class);

   @Bean
   public EmbeddedServletContainerCustomizer containerCustomizer() {
      return container -> {
         ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/src/main/static/index.html");
         ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/src/main/static/index.html");
         ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/src/main/static/index.html");

         container.addErrorPages(error401Page, error404Page, error500Page);
      };
   }

   @Override
   public void run(String... args) throws Exception {
      if(!checkStoragePath()) {
         throw new RuntimeException("Property " + STORAGE_PATH_KEY + " is not specified or path does not exist");
      }
      log.info("Using WeePlayer storage: " + System.getProperty(STORAGE_PATH_KEY));
   }

   private boolean checkStoragePath() {
      String storagePath = System.getProperty(STORAGE_PATH_KEY);
      if(StringUtils.isBlank(storagePath)) return false;
      File file = new File(storagePath);
      if(!file.exists()) return false;

      return true;
   }

   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }
}
