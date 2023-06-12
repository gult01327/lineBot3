package test.com.linebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RequestMapping("/robot")
@RestController
@ComponentScan(basePackages = {"test.com.service"})
public class LineBot3Application {

	public static void main(String[] args) {
		SpringApplication.run(LineBot3Application.class, args);
	}
	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return new ResponseEntity<String>("Hello FARMERTEST", HttpStatus.OK);
	}

}
