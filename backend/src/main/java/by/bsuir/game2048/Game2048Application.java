package by.bsuir.game2048;

import by.bsuir.game2048.config.AccessControlProperties;
import by.bsuir.game2048.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AccessControlProperties.class, StorageProperties.class})
public class Game2048Application {

    public static void main(String[] args) {
        SpringApplication.run(Game2048Application.class, args);
    }
}
