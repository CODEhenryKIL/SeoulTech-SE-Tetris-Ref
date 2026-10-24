# TEST-04·05 요구사항 검증

기준은 이 문서를 포함한 `docs/req1-nondev-tasks` HEAD다. 기존 55개 독립 회귀 검사의 이름과 기대값은 제품 요구에 맞춰 갱신하며, 중복되지 않는 시간·속도·점수 경계 검사 3개를 JUnit에 추가한다.

## TEST-04 점수·속도

| 요구 | 검사 | 판정 |
|---|---|---|
| R13 초기 1초 자동 낙하 | 신규 `initialDropIntervalIsOneSecond` | 충족: 초기·반복 간격 1,000ms, 게임 시작 전 미실행 |
| R14 속도 증가 | 기존 `level-up`, `lock-line-level`; 신규 `speedIncreasesThroughLevelNineteenAndThenStops` | 충족: 누적 10줄에서 레벨 2·793ms, 이후 레벨 19까지 간격이 증가하지 않고 상한 유지 |
| R24 입력 방식과 무관한 칸당 1점 | 기존 `drop-input-score`, `hard-drop`; 신규 `everyDropInputUsesTheSamePerCellScoreAtEachSpeed` | 충족: 자동·수동·하드드롭에 같은 칸당 점수 적용 |
| R25 초기보다 빠른 속도의 추가 점수 | 신규 `everyDropInputUsesTheSamePerCellScoreAtEachSpeed` | 충족: 레벨 1·2·19에서 칸당 1·2·19점 적용 |
| R26 별도 가산 규칙 | 기존 `line-clear`, `lock-line-level` | 충족: 한 번의 고정에서 여러 행 삭제 시 100, 200, 300… 순서의 combo 가산 |

## TEST-05 설정·스코어보드

| 요구 | 재사용 검사 | 판정 |
|---|---|---|
| R29 점수 기록 초기화 | `score-reset`, `score-refresh`, `score-store-direct` | 충족: 설정 메뉴·직접 저장소 초기화와 이후 재조회·재저장 |
| R30 전체 설정 기본값 복원 | `reset-settings`, `settings-isolation` | 충족: 창 크기·키·색각 모드 복원 및 인스턴스 독립 |
| R31 설정 저장·복구 | `key-change`, `key-file-round-trip`, `key-edit-staging`, `setting-option-cycle`, `setting-file-boundaries` | 충족: 변경·저장 시점·재로드·손상/누락 파일 경계 |
| R37 점수 내림차순 | `score-ranking`, `score-parse-boundaries`, `score-store-direct` | 충족: 내림차순, 동점 입력 순서, 정수·손상 행 경계 |
| R38 상위 10개 표시 | `score-ranking`, `score-store-direct` | 충족: 저장소 전체 기록과 화면 상위 10개 표시 분리 |
| R39 종료 후 순위 유지 | `score-save`, `score-append-bytes`, `score-refresh`, `score-file-boundaries` | 충족: 파일 저장·재조회·초기화 전 유지 |

TEST-05는 요구별 정상·경계·오류 사례가 이미 있어 새 검사를 추가하지 않는다. 실제 프로그램을 종료하고 다시 실행하는 사용자 흐름은 TEST-06과 RELEASE-04에서 같은 저장 파일로 재확인한다.

## CORE-15·RANK-05 호환 검사

난이도 가중치는 기존 `feature/CORE-15` 규칙을 선별 적용한다. 기본·Normal은 기존 `nextInt(7)` 순서를 유지하고 Easy는 I 블록 12/72, Hard는 I 블록 8/68, 나머지는 각각 10의 가중치를 사용한다. 시작 메뉴의 기존 4개 동작, 난이도 좌우 순환, 레거시 `이름:점수` 파일 형식, 유효한 3필드 난이도 기록과 화면 표시는 신규 JUnit 검사로 고정한다.


## 통합 중 발견·수정한 결함과 명시적 기대값 변경

- R24: 자동·수동·하드드롭 기본 낙하 점수를 칸당 1로 통일. R25: 같은 거리에서 현재 레벨만큼 가산(기본 1 + 레벨−1). 기존 `hard-drop`·`drop-input-score` 기대값은 이 요구 결함 수정에 맞춰 변경했으며 동작 차이를 숨기기 위한 변경이 아니다.
- RANK-05: 새 게임 기록에 `이름:점수:난이도`를 저장하고 난이도를 표시한다. 기존 2필드 기록은 그대로 읽고 2인자 저장 API도 유지한다. `score-append-bytes`·`score-refresh` 기대값에 신규 기록의 Normal 메타데이터/표시를 반영한다.
- TEST-05: 마지막 줄바꿈이 없는 기존 파일에 새 점수를 붙이면 두 기록이 합쳐져 사라지던 결함을 수정했다. 기존 바이트를 지우지 않고 구분 줄바꿈을 보충하며 기존·신규 기록이 모두 남는지 검사한다.
- TEST-06: 게임 진행 중 Esc로 메뉴 복귀, 이름 입력 후 실제 점수판 표시와 새 점수 강조를 연결한다. 합성 KeyListener 이벤트의 실제 JFrame 검사와 OS의 물리 입력 검증은 구분한다.
