package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.FisaPontaj;
import org.medshift.domain.pontaj.Neconformitate;

import java.util.List;

/** Validation Service (CF3.3): verificarea legala a unei fise de pontaj. Lista goala = fisa conforma. */
public interface IConformitateService {
	List<String> valideazaCampuri(FisaPontaj fisa);
	List<Neconformitate> verifica(FisaPontaj fisa);
}
