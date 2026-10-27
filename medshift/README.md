# MedShift – Sprint 2: Business Model Implementation

Proiect Spring Boot construit pe structura `msd.t2_c3.springboot_di` (T2.C3).
Toate cele trei bounded contexts (Planificare, Personal, Pontaj) sunt implementate.

## Deschidere în IntelliJ
1. File → Open → selectați folderul `medshift` (sau `pom.xml`) → Open as Project.
2. Așteptați ca Maven să descarce dependențele (bara din dreapta jos).
3. File → Project Structure → SDK: JDK 25 (sau puneți `<java.version>21</java.version>` în `pom.xml` dacă aveți JDK 21).
4. Click dreapta pe `src/test/java` → Run 'All Tests'.

## Structura
```
org.medshift
├── MedShiftApplication              @SpringBootApplication
├── config/MedShiftConfig            @Configuration (bean-ul Clock)
├── domain/
│   ├── shared/                      IntervalOrar, LunaCalendaristica, Perioada, GradMedical, TipTura,
│   │                                CalendarSarbatoriLegale, DomainException
│   ├── planificare/                 GraficLunar (root), Tura, Asignare, MatriceAcoperire, CerintaAcoperire, PersonalDisponibil
│   ├── personal/                    Angajat (root, +Contract, NumeComplet, Email, Competenta), DoleanteLunare (+PreferintaZi),
│   │                                Concediu, CerereSchimbTura + enum-uri
│   └── pontaj/                      FisaPontaj (root, +LiniePontaj, SegmentOre, TotalOre, Neconformitate),
│                                    BorderouSalarial (root, +LinieBorderou) + enum-uri
├── services/
│   ├── shared/                      IEntityRepository<T>, RepositoryInMemorie<T> (query by example)
│   ├── audit/                       Audit Service
│   ├── planificare/                 Repository, Factory, Strategii, Validare, Calcul
│   ├── personal/                    4 Repository, Factory, Validare, Calcul, Disponibilitate (-> Planificare)
│   └── pontaj/                      2 Repository, Factory, Segmentare ore, Conformitate, Calcul, Export borderou
└── application/
    ├── planificare/                 IPlanificareWorkflowService  (UC1.1 – UC1.6)
    ├── personal/                    IPersonalWorkflowService     (UC2.1 – UC2.4)
    └── pontaj/                      IPontajWorkflowService       (UC3.1 – UC3.5)
```

## Livrabile Sprint 2
| Livrabil | Planificare | Personal | Pontaj |
|---|---|---|---|
| Entity classes | `domain.planificare` | `domain.personal` | `domain.pontaj` |
| Repository (câte unul pe agregat) | `GraficLunarRepositoryImpl` | `Angajat…`, `DoleanteLunare…`, `Concediu…`, `CerereSchimbTura…RepositoryImpl` | `FisaPontaj…`, `BorderouSalarial…RepositoryImpl` |
| Aggregate Entity Factories | `GraficLunarFactoryImpl` | `AngajatFactoryImpl` | `FisaPontajFactoryImpl` |
| Workflow Services | `PlanificareWorkflowServiceImpl` | `PersonalWorkflowServiceImpl` | `PontajWorkflowServiceImpl` |
| Validation Service | `ValidareGraficServiceImpl` | `ValidarePersonalServiceImpl` | `ConformitateServiceImpl` |
| Computation Services | `CalculPlanificareServiceImpl` | `CalculPersonalServiceImpl`, `DisponibilitateServiceImpl` | `SegmentareOreServiceImpl`, `CalculPontajServiceImpl` |
| Audit Services | `AuditServiceImpl` (comun) | | |
| Configuration class | `MedShiftConfig` (comun) | | |
| Teste validare | `TestJupiterValidareGraficService` | `TestJupiterValidarePersonalService` | `TestJupiterConformitateService` |
| Teste calcul | `TestJupiterCalculPlanificareService` | `TestJupiterCalculPersonalService` | `TestJupiterCalculPontajService` |
| Teste workflow | `TestJupiterPlanificareWorkflowService` | `TestJupiterPersonalWorkflowService` | `TestJupiterPontajWorkflowService` |

Alte teste: `TestJupiterGraficLunar`, `TestJupiterEntitatiPersonal`, `TestJupiterEntitatiPontaj`, `TestJupiterRepositories`.

## Cum se leagă contextele
- **Personal → Planificare:** `IPersonalWorkflowService.pregatesteDisponibilitati(...)` dă lista `PersonalDisponibil`
  (concedii aprobate + doleanțe) pentru `IPlanificareWorkflowService.genereazaGrafic(...)`.
  Un concediu aprobat și un schimb de tură aprobat se aplică automat în graficul publicat.
- **Planificare → Pontaj:** din graficul `FINALIZAT`, `IPontajWorkflowService.genereazaFise(...)` face câte o fișă
  pe angajat, cu orele împărțite pe categorii (zi, noapte 22–06, weekend, gardă, sărbătoare legală).

## Reguli propuse de echipă (de confirmat)
- Prioritatea categoriilor de ore: sărbătoare > weekend > noapte > gardă > zi (`SegmentareOreServiceImpl`).
- Media de maxim 48 h/săptămână de program normal, calculată pe lună, fără orele de gardă (`ConformitateServiceImpl`).
- 20 de zile lucrătoare de concediu de odihnă pe an, minimul legal (`CalculPersonalServiceImpl`).
