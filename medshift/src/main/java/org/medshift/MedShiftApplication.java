package org.medshift;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.logging.Logger;

// Clasa principala sta direct in org.medshift, ca scanarea sa gaseasca bean-urile din toate sub-pachetele.
@SpringBootApplication
public class MedShiftApplication {
	private static final Logger logger = Logger.getLogger(MedShiftApplication.class.getName());

	public static void main(String[] args) {
		logger.info("Loading: MedShift - Medical Staff Scheduling and Timesheet Management ...");
		ApplicationContext ctx = SpringApplication.run(MedShiftApplication.class, args);
		System.out.println("ApplicationContext initialized...");
		System.out.println("Initialized bean: " + ctx.getBean("planificareWorkflowServiceImpl"));
	}
}
