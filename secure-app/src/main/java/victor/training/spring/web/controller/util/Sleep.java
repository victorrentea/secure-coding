package victor.training.spring.web.controller.util;

public class Sleep {
	
	public static void millis(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
	}
	
}
