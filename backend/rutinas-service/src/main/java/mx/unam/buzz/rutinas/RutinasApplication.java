package mx.unam.buzz.rutinas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("mx.unam.buzz.rutinas.modelo.entity")
public class RutinasApplication {

    public static void main(String[] args) {
        SpringApplication.run(RutinasApplication.class, args);
    }
}
