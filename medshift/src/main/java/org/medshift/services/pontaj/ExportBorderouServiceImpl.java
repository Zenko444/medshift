package org.medshift.services.pontaj;

import org.medshift.domain.pontaj.BorderouSalarial;
import org.medshift.domain.pontaj.FormatExport;
import org.medshift.domain.pontaj.LinieBorderou;
import org.medshift.domain.pontaj.TotalOre;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Locale;

@Service
public class ExportBorderouServiceImpl implements IExportBorderouService {

	public static final String ANTET_CSV = "id_angajat;id_fisa;ore_zi;ore_noapte;ore_weekend;ore_garda;ore_sarbatoare;total";

	@Override
	public String exporta(BorderouSalarial b) {
		return b.getFormat() == FormatExport.CSV ? csv(b) : xml(b);
	}

	@Override
	public String numeFisier(BorderouSalarial b) {
		return "borderou_sectia" + b.getIdSectie() + "_" + b.getLuna() + "." + b.getFormat().name().toLowerCase();
	}

	private String csv(BorderouSalarial b) {
		StringBuilder sb = new StringBuilder(ANTET_CSV).append('\n');
		for (LinieBorderou l : liniiSortate(b)) {
			TotalOre t = l.totaluri();
			sb.append(l.idAngajat()).append(';').append(l.idFisa()).append(';')
					.append(n(t.oreZi())).append(';').append(n(t.oreNoapte())).append(';')
					.append(n(t.oreWeekend())).append(';').append(n(t.oreGarda())).append(';')
					.append(n(t.oreSarbatoare())).append(';').append(n(t.total())).append('\n');
		}
		return sb.toString();
	}

	private String xml(BorderouSalarial b) {
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<borderou sectia=\"").append(b.getIdSectie()).append("\" luna=\"").append(b.getLuna()).append("\">\n");
		for (LinieBorderou l : liniiSortate(b)) {
			TotalOre t = l.totaluri();
			sb.append("  <angajat id=\"").append(l.idAngajat()).append("\" fisa=\"").append(l.idFisa())
					.append("\" zi=\"").append(n(t.oreZi())).append("\" noapte=\"").append(n(t.oreNoapte()))
					.append("\" weekend=\"").append(n(t.oreWeekend())).append("\" garda=\"").append(n(t.oreGarda()))
					.append("\" sarbatoare=\"").append(n(t.oreSarbatoare())).append("\" total=\"").append(n(t.total()))
					.append("\"/>\n");
		}
		return sb.append("</borderou>\n").toString();
	}

	private static java.util.List<LinieBorderou> liniiSortate(BorderouSalarial b) {
		return b.getLinii().stream().sorted(Comparator.comparing(LinieBorderou::idAngajat)).toList();
	}

	/** Numar cu 2 zecimale si punct zecimal, indiferent de setarile regionale ale calculatorului. */
	private static String n(double x) {
		return String.format(Locale.ROOT, "%.2f", x);
	}
}
