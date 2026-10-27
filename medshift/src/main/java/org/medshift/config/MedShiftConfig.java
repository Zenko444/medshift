package org.medshift.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Spring App Configuration Class (livrabil Sprint 2).
 * Bean-urile care nu sunt clase proprii adnotate cu @Component se declara aici cu @Bean.
 */
@Configuration
public class MedShiftConfig {

	public static final ZoneId FUS_ORAR = ZoneId.of("Europe/Bucharest");

	/**
	 * Ceasul aplicatiei. Toate serviciile iau "acum" de aici, nu din LocalDate.now(),
	 * ca testele sa poata fixa data (termene de doleante, fereastra de 3 zile, sfarsit de luna).
	 */
	@Bean
	public Clock clock() {
		return Clock.system(FUS_ORAR);
	}
}
