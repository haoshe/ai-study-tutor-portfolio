package ie.tcd.scss.aichat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.ai.openai.api-key=test-key"
})
class AichatApplicationTests {

	@Test
	void contextLoads() {
		// Context loads successfully
	}

}
