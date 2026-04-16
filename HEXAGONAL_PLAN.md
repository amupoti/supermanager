# Hexagonal Architecture Refactoring Plan

## Philosophy

Following **"Get Your Hands Dirty on Clean Architecture"** (Tom Homberger), the key principles are:

1. **Domain at the center** — no framework dependencies, pure Java
2. **Use cases as first-class citizens** — one class per use case (application layer)
3. **Ports** — interfaces owned by the domain/application layer, defining what it needs
4. **Adapters** — implementations that plug into ports (web controllers = driving/in adapters, API clients = driven/out adapters)
5. **Dependency rule** — adapters depend on ports, never the reverse
6. **No "service" grab-bags** — each use case class does one thing

## Current Problems

- `SmContentProvider` mixes authentication, team CRUD, market reads, and player actions (god class)
- `SMUserTeamService` is a service grab-bag: fetches data, computes stats, finds candidates — all in one class
- `UserController` contains business logic (building player rows, position quota enforcement)
- Domain beans (`SmTeam`, `SmPlayer`) are mutable data holders with no behavior; `SmTeam` even builds URLs (infrastructure concern)
- DTOs and domain beans live in the same module with no separation
- `SmContentParser` mixes JSON deserialization with business computations (last-4-average, team stats)
- `RdmSmTeamService` mixes domain mapping with infrastructure (RDM API calls)
- No ports/interfaces — everything is wired by concrete class

## Target Package Structure (per module)

```
org.amupoti.supermanager.{module}
├── domain/                    # Pure domain model — no Spring, no framework
│   └── model/                 # Entities, value objects, enums
├── application/               # Use cases + port interfaces
│   ├── port/
│   │   ├── in/                # Driving ports (interfaces called by adapters-in)
│   │   └── out/               # Driven ports (interfaces implemented by adapters-out)
│   └── service/               # Use case implementations
└── adapter/
    ├── in/                    # Driving adapters (web controllers, schedulers)
    │   └── web/
    └── out/                   # Driven adapters (HTTP clients, scrapers, persistence)
        └── acbapi/  / rdm/
```

---

## Module 1: supermanager-acb-parser

### Current structure
```
org.amupoti.supermanager.parser.acb
├── SmContentProvider        ← god class: auth + teams + market + stats + buy/sell
├── SmContentParser          ← JSON parsing + business computations mixed
├── beans/
│   ├── SmTeam               ← mutable, builds URLs
│   ├── SmPlayer             ← mutable data holder
│   ├── SmPlayerStatus
│   ├── PlayerPosition
│   └── market/
│       ├── PlayerMarketData ← has domain logic (findMostExpensiveFit...)
│       └── MarketCategory
├── teams/
│   └── SMUserTeamService    ← grab-bag: fetch + stats + candidates
├── market/
│   └── SmMarketService      ← unused interface
├── dto/                     ← API DTOs mixed with domain
├── utils/
│   └── DataUtils
├── exception/
└── privateleague/
    └── PrivateLeagueCategory
```

### Target structure
```
org.amupoti.supermanager.acb
├── domain/
│   └── model/
│       ├── Player                   (from SmPlayer — immutable, richer)
│       ├── PlayerStatus             (from SmPlayerStatus)
│       ├── PlayerPosition           (enum, stays)
│       ├── Team                     (from SmTeam — no URL building)
│       ├── MarketData               (from PlayerMarketData — domain logic stays)
│       ├── MarketCategory           (enum, stays)
│       └── PrivateLeagueCategory    (enum, stays)
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── LoadUserTeamsUseCase
│   │   │   ├── BuyPlayerUseCase
│   │   │   ├── SellPlayerUseCase
│   │   │   ├── UndoChangeUseCase
│   │   │   └── CancelAllChangesUseCase
│   │   └── out/
│   │       ├── AuthenticationPort
│   │       ├── TeamDataPort
│   │       ├── MarketDataPort
│   │       ├── PlayerStatsPort
│   │       └── PlayerChangePort
│   └── service/
│       ├── LoadUserTeamsService      (orchestration from SMUserTeamService)
│       ├── ComputeTeamStatsService   (extracted stats computation)
│       ├── FindCandidateService      (extracted candidate selection)
│       ├── BuyPlayerService
│       ├── SellPlayerService
│       ├── UndoChangeService
│       └── CancelAllChangesService
├── adapter/
│   └── out/
│       └── acbapi/
│           ├── AcbAuthenticationAdapter
│           ├── AcbTeamDataAdapter
│           ├── AcbMarketDataAdapter
│           ├── AcbPlayerStatsAdapter
│           ├── AcbPlayerChangeAdapter
│           └── dto/                  (all current DTOs — adapter internals)
│               ├── LoginRequest / LoginResponse
│               ├── SigninRequest / SigninResponse
│               ├── TeamsDescriptionResponse
│               ├── TeamsDetailsResponse
│               ├── MarketPlayerResponse
│               ├── PlayerStatsResponse
│               ├── TeamPlayerDetailResponse
│               └── PendingChangeResponse
├── exception/
│   ├── SmException
│   ├── ErrorCode
│   ├── ErrorResolver
│   └── InfrastructureException
└── utils/
    └── DataUtils
```

### Key changes
- **`SmContentProvider` (god class)** → split into 5 out-adapters, each implementing a port
- **`SmContentParser`** → parsing logic absorbed into each adapter (it's an adapter concern)
- **`SMUserTeamService`** → split into `LoadUserTeamsService` (orchestration) + `ComputeTeamStatsService` + `FindCandidateService`
- DTOs move under `adapter/out/acbapi/dto/` — they're API-specific, not domain
- Domain model classes become richer: `Player` and `Team` gain behavior
- URL building removed from `SmTeam` → moves to adapter
- Drop redundant `.parser.` from package name

---

## Module 2: supermanager-rdm-parser

### Current structure
```
org.amupoti.supermanager.parser.rdm
├── RdmContentProvider       ← HTTP with throttling
├── RdmContentParser         ← JSoup HTML parsing
├── RdmMatchService          ← caching & orchestration
├── Match                    ← domain entity
├── RdmTeam                  ← enum 18 clubs + hardcoded quality
├── RdmTeamData              ← team + schedule aggregate
├── RdmException
└── config/
    └── RdmConfiguration
```

### Target structure
```
org.amupoti.supermanager.rdm
├── domain/
│   └── model/
│       ├── LeagueTeam               (from RdmTeam — cleaner name)
│       ├── Match                    (stays, immutable)
│       └── TeamSchedule             (from RdmTeamData — richer name)
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── GetTeamScheduleUseCase
│   │   │   └── GetAllSchedulesUseCase
│   │   └── out/
│   │       └── ScheduleScrapingPort
│   └── service/
│       ├── TeamScheduleService      (from RdmMatchService — use case logic)
│       └── ScheduleCacheService     (cache warming + eviction)
├── adapter/
│   └── out/
│       └── rdm/
│           ├── RdmScrapingAdapter   (merged provider + parser)
│           └── RdmConfiguration     (Spring config)
└── exception/
    └── RdmException
```

### Key changes
- `RdmContentProvider` + `RdmContentParser` → merge into `RdmScrapingAdapter`
- `RdmMatchService` → `TeamScheduleService` with port interfaces
- Domain types get cleaner names (`LeagueTeam`, `TeamSchedule`)

---

## Module 3: supermanager-viewer

### Current structure
```
org.amupoti.sm.main
├── Application
├── config/
│   ├── ApplicationConfig    ← creates beans, caches, executors
│   ├── TemplateAutoConfiguration
│   └── SMConstants
├── controller/
│   ├── UserController       ← has business logic (row building, quota)
│   ├── TeamController
│   ├── PlayerController
│   ├── PrivateLeagueController
│   ├── CacheController
│   └── exception/GlobalExceptionHandler
├── bean/
│   └── SMUser
├── users/
│   └── UserCredentialsHolder
├── service/
│   ├── RdmSmTeamService     ← mixes domain mapping + infra
│   ├── PrivateLeagueService
│   └── ScrapingRefreshJob
└── model/
    ├── ViewerPlayer
    ├── ViewerMatch
    ├── PrivateLeagueTeamData
    ├── UserTeamViewData
    └── PlayerRow
```

### Target structure
```
org.amupoti.supermanager.viewer
├── domain/
│   └── model/
│       └── SMUser                   (login credentials — domain concept)
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── ViewUserTeamsUseCase
│   │   │   ├── ViewCalendarUseCase
│   │   │   └── ViewPrivateLeagueUseCase
│   │   └── out/
│   │       └── CredentialsStorePort
│   └── service/
│       ├── ViewUserTeamsService     (from UserController logic + RdmSmTeamService)
│       ├── ViewCalendarService      (thin — delegates to rdm module)
│       └── PrivateLeagueService     (stays, minor refactor)
├── adapter/
│   ├── in/
│   │   ├── web/
│   │   │   ├── UserTeamWebAdapter   (from UserController — thin HTTP only)
│   │   │   ├── CalendarWebAdapter   (from TeamController)
│   │   │   ├── PlayerWebAdapter     (from PlayerController)
│   │   │   ├── PrivateLeagueWebAdapter (from PrivateLeagueController)
│   │   │   ├── CacheWebAdapter      (from CacheController)
│   │   │   ├── GlobalExceptionHandler
│   │   │   └── dto/                 (view models)
│   │   │       ├── PlayerRowDto
│   │   │       ├── ViewerPlayerDto
│   │   │       ├── ViewerMatchDto
│   │   │       └── TeamViewDto
│   │   └── scheduler/
│   │       └── ScrapingRefreshJob   (driving adapter — @Scheduled trigger)
│   └── out/
│       └── session/
│           └── InMemoryCredentialsAdapter (from UserCredentialsHolder)
├── config/
│   ├── ApplicationConfig            (composition root — Spring wiring)
│   ├── TemplateAutoConfiguration
│   └── SMConstants
└── Application                      (@SpringBootApplication entry point)
```

### Key changes
- **`UserController`** → split into `UserTeamWebAdapter` (thin HTTP) + `ViewUserTeamsService` (all row-building/enrichment logic)
- **`RdmSmTeamService`** → absorbed into `ViewUserTeamsService`
- **`ScrapingRefreshJob`** → moves to `adapter/in/scheduler/` (it's a driving adapter)
- View models → `adapter/in/web/dto/` (presentation-specific)
- **`UserCredentialsHolder`** → driven adapter for session storage

---

## Execution Order

| # | ID | Module | What |
|---|-----|--------|------|
| 1 | domain-models-acb | acb-parser | Domain models: move & clean Player, Team, PlayerStatus, etc. |
| 2 | ports-acb | acb-parser | Port interfaces (in + out) |
| 3 | adapters-acb | acb-parser | Split SmContentProvider → 5 adapters, move DTOs |
| 4 | usecases-acb | acb-parser | Use case services (LoadTeams, ComputeStats, FindCandidate, Buy, Sell, Undo, CancelAll) |
| 5 | domain-models-rdm | rdm-parser | Domain models: LeagueTeam, Match, TeamSchedule |
| 6 | ports-adapters-rdm | rdm-parser | Ports + merge provider/parser → RdmScrapingAdapter |
| 7 | viewer-adapters-in | viewer | Thin web adapters, move view models to dto/ |
| 8 | viewer-usecases | viewer | ViewUserTeamsService, ViewCalendarService |
| 9 | viewer-adapters-out | viewer | InMemoryCredentialsAdapter, ScrapingRefreshJob move |
| 10 | cleanup | all | Verify build, run tests, delete old packages |

### Dependency graph
```
domain-models-acb ──→ ports-acb ──→ adapters-acb ──→ usecases-acb ──┐
                                                                      ├──→ viewer-adapters-in ──┐
domain-models-rdm ──→ ports-adapters-rdm ──────────────────────────┘   │                        │
                                                                       ├──→ viewer-usecases ──→ viewer-adapters-out ──→ cleanup
                                                                       │
```

## Notes

- **Templates (.ftl/.vm)**: Stay in `resources/templates/` — update attribute names if DTOs are renamed
- **Spring wiring**: `ApplicationConfig` stays in `config/` — it's the composition root
- **Tests**: Move alongside their targets; update imports
- **Incremental**: Each step keeps the project compilable
- **Package naming**: Drop redundant `.parser.` — modules aren't just parsers
