package com.cloudia.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CheckPropertiesRunner implements CommandLineRunner {

    @Autowired
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("プロパティ確認 ----- >>>>>");

        // .env から読み込まれ、システムプロパティとして設定された DB_PASSWORD の値
        String systemPropDbPassword = System.getProperty("DB_PASSWORD");
        System.out.println("System Property 'DB_PASSWORD': [" + systemPropDbPassword + "]");

        // Spring Environment が認識する DB_PASSWORD の値
        String springEnvDbPassword = env.getProperty("DB_PASSWORD");
        System.out.println("Spring Environment 'DB_PASSWORD': [" + springEnvDbPassword + "]");

        // Spring Environment が最終的に認識するデータソースのパスワード値
        String hikariPassword = env.getProperty("spring.datasource.hikari.password");
        System.out.println("Spring Environment 'spring.datasource.hikari.password': [" + hikariPassword + "]");

        System.out.println("<<<<< ----- プロパティ確認終了 ----- >>>>>");
    }
}