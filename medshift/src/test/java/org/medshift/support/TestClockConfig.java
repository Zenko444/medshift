package org.medshift.support;

import org.medshift.config.MedShiftConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;

/** In teste, ceasul aplicatiei este fixat pe 21.10.2026 10:00 (Bucuresti) si poate fi mutat. */
@TestConfiguration
public class TestClockConfig {

	public static final LocalDateTime START = LocalDateTime.of(2026, 10, 21, 10, 0);

	@Bean
	@Primary
	public CeasControlabil ceasTest() {
		return new CeasControlabil(START, MedShiftConfig.FUS_ORAR);
	}
}
