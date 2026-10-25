# 2차 요구사항 추적표

교수님 요구사항 2의 필수 조건을 칸반 카드, 제품 코드, 자동 검사와 최종 증빙에 연결한다.

- 기준 자료: `TeamProject_Req2.pdf` 12쪽(표지 포함)
- 1차 요구 유지 기준: [1차 요구사항 추적표](requirements-1-traceability.md)
- 평가 절차: [요구사항 1·2 공통 평가 체크리스트](evaluation-checklist.md)
- 최종 실행 결과·PR·CI·패키지 원장: [R2-07 최종 검증 기록](req2-final-verification.md)
- `Q2-01` 같은 번호는 평가 체크리스트 ID이고, `R2-01` 같은 번호만 칸반 카드 ID다.

## 상태 판정

| 표시 | 뜻 |
|---|---|
| 구현·로컬 검증 | 제품 코드와 전용 자동 검사가 있고 현재 누적 브랜치에서 로컬 검증 기록이 존재한다. main 병합이나 원격 CI 성공을 뜻하지 않는다. |
| 최종 증빙 대기 | R2-07에서 확정 commit의 clean build, 양 OS CI, 패키지와 보고서 버전을 다시 연결해야 한다. PR #20의 소스 CI 성공과 이후 최종 브랜치 검증은 구분한다. |
| 수동 검증 대기 | 자동화만으로 판정하지 않는 실제 Windows 11 물리 키·패키지 실행·저장 후 종료/재실행 또는 사람의 색 구분 검사다. |
| 병합 대기 | 마지막 확인 기준 PR #18·#19·#20이 열려 있고 main에 병합되지 않았다. 최신 상태는 최종 검증 기록에서 다시 확인한다. |

현재 누적 브랜치는 PR #18의 기존 기능 수용 검사, PR #19의 모드별 기록, PR #20의 아이템·공급 변경을 순서대로 포함한다. 아이템 commit `15e46ec`은 origin CI `40185986802`와 fork CI `40185944683`이 모두 완료·성공했다. 보고서·아티팩트 세부 대조는 최종 검증 기록에 적는다. 이후 최종 브랜치에 추가되는 DesktopSmoke 보강은 아직 원격 CI에 게시되지 않았으므로 이 성공 결과에 포함하지 않는다. Done 카드 수는 이 문서에서 추정하지 않는다.

## 카드 책임

| 원본 카드 | 원래 범위 | 보완·완성 카드 |
|---|---|---|
| R2-01 | 난이도 선택·확률·속도·난이도 기록·삭제 애니메이션 | R2-09 난이도 UI·속도·실제 생성기 분포 |
| R2-02 | Item 모드와 10줄 공급 | R2-10 공급 경계·미리보기·대기 권리·초기화 |
| R2-03 | L 아이템과 행 삭제 | R2-11 L 표식·점수·누계·삭제 애니메이션 |
| R2-04 | 무게추 | R2-12 접촉 잠금·삭제 범위·점수·렌더링 |
| R2-05 | 자체 아이템 최소 3종 | R2-08 세 번째 자체 아이템, R2-13 C/B/P 명세·효과·검사 |
| R2-06 | 모드별 점수 | R2-14 저장 형식·필터·순위 진입·호환 |
| R2-07 | 전체 통합 검증·평가 증빙 | 이 추적표와 `req2-final-verification.md`, 원격·수동 최종 확인 |

## 난이도·일반 모드 변경 (원문 3~5쪽)

| 평가 ID·필수 조건 | 카드 | 구현 위치 | 자동 검사·증빙 | 상태·남은 일 |
|---|---|---|---|---|
| Q2-01 Easy/Normal/Hard 선택·새 게임 적용 | R2-01, R2-09 | `Difficulty`; `Settingpanel`의 Difficulty 옵션; `Maincontainer#getDifficulty`; `Gamepanel`의 게임별 고정 난이도 | `DifficultyUiTest#settingsMenuCyclesSavesResetsAndAppliesDifficultyToNewGame`, `DifficultyUiTest#runningGamesKeepIndependentDifficultyAndBoardState` | 구현·로컬 검증; 최종 commit UI 증빙 대기 |
| Q2-02 I 상대 가중치 Easy +20%, Normal 동일, Hard -20% | R2-01, R2-09 | `BlockGenerator#nextBlock(Difficulty)` | `BlockGeneratorDifficultyTest#difficultyWeightsMatchTheApprovedDistribution`; [정책·기대 확률](req2-existing-features.md#실제-생성기의-분포-판정) | 구현·로컬 검증 |
| Q2-03 실제 게임 생성 코드로 1,000회 이상, ±5% 분포 검사 | R2-01, R2-09 | 위 실제 `BlockGenerator`를 직접 호출 | `BlockDistributionTest#productionGeneratorMatchesEveryConfiguredProbability`; 고정 seed·100,000회·상대오차 5%, CSV `build/reports/req2/block-distribution.csv` | 구현·로컬 검증; `15e46ec` 양쪽 CI 성공, CSV 세부 대조·최종 SHA 재검증 대기 |
| Q2-04 속도 증가량 Easy 0.8배·Normal 1.0배·Hard 1.2배 | R2-01, R2-09 | `Difficulty#dropIntervalMillis`; `Board#levelUp` | `DifficultyAcceptanceTest`, `Pr15BoardRegressionTest#actualLevelUpUsesDifficultySpeedFactor`; [계산 기준](test-requirements.md#난이도별-속도-계산-기준) | 구현·로컬 검증 |
| Q2-05 점수에서 난이도 식별 | R2-01, R2-06, R2-09, R2-14 | `ScoreStore.Score#difficulty`; `ScoreStore#append`; `Scoreboardpanel#drawScore` | `ScoreStoreDifficultyTest`, `DifficultyUiTest#scoreboardLabelsOnlyRecordsThatContainDifficultyMetadata`, `ModeScoreUiTest` | 구현·로컬 검증 |
| Q2-06 삭제 행 애니메이션 | R2-01, R2-03, R2-11 | `Board#removeRows`, `Board#eraseRowAnimation`과 삭제 overlay timer | `LineAnimationTest`, `LineItemAcceptanceTest`, `Pr15BoardRegressionTest#normalClearOverlayDoesNotCancelImmediatelyFollowingRowItemAnimation` | 구현·로컬 검증; 사람 화면 확인은 최종 증빙 대기 |

분포의 실제 기대값은 Easy `I=12/72`, 기타 각 `10/72`; Normal 모두 `10/70=1/7`; Hard `I=8/68`, 기타 각 `10/68`이다. 원문의 최소 표본은 1,000회이고, 우리 검사는 우연한 실패를 줄이기 위해 100,000회를 사용한다.

## Item 모드·공급·모드별 기록 (원문 6~7쪽)

| 평가 ID·필수 조건 | 카드 | 구현 위치 | 자동 검사·증빙 | 상태·남은 일 |
|---|---|---|---|---|
| Q2-07 시작 메뉴에서 Classic/Item 선택, 실제 규칙 분리 | R2-02, R2-10 | `GameMode`; `Startpanel`의 CLASSIC/ITEM; `Gamepanel#getGameMode`; `Board`의 모드 조건 | `ItemSupplyAcceptanceTest#classicModeNeverSuppliesAnItemAtThreshold`, `DesktopSmoke` | 구현·로컬 검증 |
| Q2-08 Classic/Item 점수 구분 조회 | R2-06, R2-14 | `ScoreStore.Score#mode`, `ScoreStore#readRanked(GameMode)`; `Scoreboardpanel#showMode`, `Scoreboardpanel#cycleMode` | `ScoreStoreModeTest`, `ModeScoreUiTest`, [저장·순위 정책](req2-mode-scores.md) | 구현·로컬 검증 |
| Q2-09 Item에서 누계 10줄마다 아이템 권리 1개 | R2-02, R2-10 | `Board#lockAndAdvanceBlock`, `erasedLineNum`, `pendingItems` | `ItemSupplyAcceptanceTest#crossingEachTenLineBoundarySuppliesOneNextItem`, `ItemSupplyAcceptanceTest#multipleEntitlementsAreQueuedAndConsumedOnePerLock` | 구현·로컬 검증 |
| Q2-10 경계 뒤 새 미리보기는 아이템/포함 블록 | R2-02, R2-10 | `Board#lockAndAdvanceBlock`; `BlockGenerator#nextItemBlock` | `ItemSupplyAcceptanceTest#movementTickDoesNotReplaceAwardedPreview`와 preview 일치 검사 | 구현·로컬 검증 |
| Q2-11 기본 블록과 식별 가능, 아이템마다 다른 문자 | R2-02, R2-03~05, R2-11~13 | L=`RowBlock`, 무게추=`AnchorBlock`, C=`ColumnBlock`, B=`BombBlock`, P=`CrossBlock` | `RowBlockTest`, `AnchorAcceptanceTest`, `ColumnBlockTest`, `ColumnBombAcceptanceTest`, `CrossBlockTest`; palette PNG | 구현·로컬 검증; 실제 사용자의 색·문자 식별은 수동 검증 대기 |
| Q2-12 제공 2종 + 자체 최소 3종이 공급·미리보기·효과에 연결 | R2-02~05, R2-08, R2-10, R2-13 | `BlockGenerator#nextItemBlock`의 L/무게추/C/B/P; `Board#useItem`과 각 효과 분기 | `ItemDistributionTest`(5종 각 20%, 100,000회), `ItemSupplyAcceptanceTest`, [다섯 아이템 명세](req2-items.md#다섯-아이템) | 구현·로컬 검증; `15e46ec` 양쪽 CI 성공, PR #20 병합·최종 SHA 증빙 대기 |

여러 10줄 경계를 한 번에 넘으면 권리를 모두 보존하고 고정마다 한 개씩 새 미리보기에 공급한다. 새 게임·초기화·게임오버·메뉴 종료에서는 `pendingItems`를 비운다.

## 제공 아이템 (원문 8~9쪽)

| 평가 ID·필수 조건 | 카드 | 구현 위치 | 자동 검사·증빙 | 상태·남은 일 |
|---|---|---|---|---|
| Q2-13 L이 일반 블록의 실제 점유 칸 한 곳에 무작위 부착 | R2-03, R2-11 | `RowBlock` 생성·표식 좌표 | `RowBlockTest#itemMarkerRemainsUniqueAndTracksItsOccupiedCellAcrossAllShapesAndRotations`(7종×4칸×4회전) | 구현·로컬 검증 |
| Q2-14 바닥/다른 블록 때문에 더 내려갈 수 없을 때 한 번 발동 | R2-03, R2-11 | `Board#lockAndAdvanceBlock`, `Board#useItem`, `Board#eraseRowLine` | `LineItemAcceptanceTest`, `Pr15BoardRegressionTest`의 실제 고정·승계·효과 입력 검사 | 구현·로컬 검증 |
| Q2-15 L의 미완성 행 전체 삭제·기존 행 점수 | R2-03, R2-11 | `Board#eraseRowLine`, `Board#removeRows`의 일반/L 행 합집합·100+200+… 점수 | `LineItemAcceptanceTest`, `Pr15BoardRegressionTest#rowItemAndDifferentFullRowUseOneClearUnion`, `Pr15BoardRegressionTest#overlappingRowItemCountsOnceAndTriggersBoundaryItem` | 구현·로컬 검증 |
| Q2-16 이동·회전 후 화면 L과 효과 행 일치 | R2-03, R2-11 | `RowBlock#rotate`, `RowBlock#getItemX`, `RowBlock#getItemY`; Board 좌표 변환 | `RowBlockTest`, `LineItemAcceptanceTest` | 구현·로컬 검증 |
| Q2-17 무게추 너비 4칸 | R2-04, R2-12 | `AnchorBlock`의 4칸 너비·6칸 형상 | `AnchorAcceptanceTest`의 형상·렌더링 검사 | 구현·로컬 검증 |
| Q2-18 폭 아래 블록을 바닥까지 제거하고 무게추 하강, 폭 밖/위 보존 | R2-04, R2-12 | `Board#useItem`의 `AnchorBlock` 분기 | `AnchorAcceptanceTest`의 양 벽·높이 차·영역 안/밖·위쪽 보존 검사 | 구현·로컬 검증 |
| Q2-19 최초 접촉 전 좌우 가능, 접촉 후 좌우 불가 | R2-04, R2-12 | `Board#anchorMovementLocked`, `anchorTouched` latch와 이동/승계 초기화 | `Pr15BoardRegressionTest#anchorCanMoveBeforeContact`, `Pr15BoardRegressionTest#anchorCannotMoveOrRotateAfterFloorContact`, `Pr15BoardRegressionTest#anchorContactLatchPersistsAfterObstacleRemovalAndClearsOnAdvance` | 구현·로컬 검증 |
| Q2-20 효과·제거 블록 기본 0점, 팀 추가 규칙은 명세와 일치 | R2-04, R2-12 | 무게추 효과 보너스 0; 실제 낙하 점수와 일반 완성 행 점수만 공통 적용 | `AnchorAcceptanceTest#emptyBoardDropKeepsNormalDropPointsWithoutAnEffectBonus`; [점수 정책](req2-existing-features.md#무게추-r2-12) | 구현·로컬 검증 |

## 자체 아이템·확장성·1차 요구 유지 (원문 7·10쪽)

| 평가 ID·필수 조건 | 카드 | 구현 위치 | 자동 검사·증빙 | 상태·남은 일 |
|---|---|---|---|---|
| Q2-21 자체 A: 열 삭제 C | R2-05, R2-13 | `ColumnBlock`; `Board#eraseColumnLine`, `Board#eraseColumnAnimation` | `ColumnBlockTest`, `ColumnBombAcceptanceTest#columnClearsOnlyItsOriginalMarkedColumn`, `ColumnBombAcceptanceTest#columnHardDropAppliesEffectAndAdvancesOnce` | 구현·로컬 검증 |
| Q2-22 자체 B: 폭탄 B | R2-05, R2-13 | `BombBlock`; `Board#useItem`의 폭탄 범위 삭제 | `ColumnBombAcceptanceTest`의 모양·경계 clipping·점수·승계·회전·palette 검사 | 구현·로컬 검증 |
| Q2-23 자체 C: 교차 삭제 P | R2-05, R2-08, R2-13 | `CrossBlock`; `Board#eraseCrossLines`, `Board#eraseCrossAnimation` | `CrossBlockTest`, `CrossItemAcceptanceTest`의 행·열·벽·상단·점수·승계·palette 검사 | 구현·로컬 검증 |
| Q2-24 색각 모드·창 크기·변경 키·난이도 저장 등 1차 기능과 충돌 없음 | R2-01~07, R2-09~14 | 공통 `Maincontainer`, `Settingpanel`, `Board`, 저장소·렌더링 코드 | `LegacyRegressionTest`의 기존 55개, `DifficultyUiTest`, 아이템별 4 palette 렌더 검사, `DesktopSmoke` | 자동 범위 구현·로컬 검증; 사람의 색 구분·실제 키·재실행은 수동 검증 대기 |
| Q2-25 신규 아이템을 공통 흐름에 추가할 수 있는 구조·설명 | R2-02, R2-05, R2-10, R2-13 | `ItemBlock` 하위 클래스, `BlockGenerator#nextItemBlock`, `Board#useItem`, 공통 애니메이션·승계 | [아이템 규칙·추가 지점](req2-items.md), 다섯 타입의 동일 공급/preview/고정 수용 검사 | 구현·로컬 검증; 최종 리뷰에서 설명 재현 대기 |

C와 B의 효과 점수는 0점이다. P는 원래 점유한 가로 2행과 세로 2열을 삭제하며 가로 2행에 기존 행 점수 `100+200=300`과 삭제 누계 `+2`를 적용하고 세로 효과에는 별도 점수를 주지 않는다. 세 아이템의 형태·범위·조합 정책은 [아이템 명세](req2-items.md)에 고정한다.

## 관리·품질·최종 제출 (원문 11~12쪽)

| 필수 조건 | 카드 | 구현·자료 | 검증 상태·남은 일 |
|---|---|---|---|
| 할 일·배정·진척을 보드에서 관리하고 생산성 확인 가능 | R2-01~07, R2-08~14, R2-07 | GitHub Project 카드의 담당자·상태·연결 PR | 최종 증빙 기록에서 카드별 담당·상태·PR 링크를 확정해야 함 |
| 코드·resource를 Git/GitHub로 버전 관리 | 전체, R2-07 | 저장소 commit 이력, `src/main`, `src/test`, `docs`와 `src/main/resources` | 사용 중; 최종 제출 SHA 고정 대기 |
| branch·Pull Request 정책 | 전체, R2-07 | `CONTRIBUTING.md`, `.github/PULL_REQUEST_TEMPLATE.md` | 정책 존재; PR #18→#19→#20은 마지막 확인 기준 병합 대기 |
| 발생 Issue와 해결 내역 추적 | 전체, R2-07 | `.github/ISSUE_TEMPLATE`, 카드/PR↔재검증, [등록 대기 본문](req2-issue-trace-draft.md) | `has_issues=false`, 현재 계정 `admin=false`로 등록 보류. 원본 저장소 관리자 활성화 필요 |
| 기능·비기능 요구를 함께 검증 | R2-07 | JUnit, `PerformanceRegressionTest`, `DesktopSmoke`, 패키징·OS 검사 | 로컬 자동 검증 있음; 최소 사양 실제 환경과 Windows 11 수동 조건은 별도 기록 필요 |
| Line Coverage: 중간 50%, 기말 70% | R2-07 | Gradle `check`, JaCoCo 전체 제품 코드 gate 70%; `.github/workflows/ci.yml` | 로컬 `0412df9`와 `15e46ec` 양쪽 CI의 실제 보고서 대조 완료: 1499/1668=89.87%. 이후 최종 main에서 변경이 생기면 재검증 |

## 요구사항 1 유지 연결

요구사항 2 원문 10쪽은 1차 요구와 어긋나지 않도록 요구한다. 아래 표는 [1차 추적표의 R01~R53](requirements-1-traceability.md)을 빠짐없이 범주별로 연결한다. 상세 카드·확인 절차는 그 문서를 기준으로 하고, 요구사항 2 통합 버전에서 다시 회귀한다.

| 1차 범위 | 관련 R 번호 | 요구사항 2에서의 영향·구현 | 증빙·현재 상태 |
|---|---|---|---|
| 게임·메뉴·보드·입력 | R01~R21 | CLASSIC 동작 유지, ITEM만 공급 규칙 추가; 공통 이동·회전·충돌·고정·preview·정지·종료 사용 | `LegacyRegressionTest`, `RequirementsRegressionTest`, `DesktopSmoke`; 실제 반복 물리 키는 수동 대기 |
| 색상·점수·설정 | R22~R31 | 네 palette, 화면 크기·변경 키·설정 저장 유지; 난이도 설정 추가; 공통 낙하/행 점수 유지 | 기존 55개 회귀, `DifficultyUiTest`, 아이템 palette tests; 사람 색 구분·완전 종료/재실행 수동 대기 |
| 게임오버·스코어보드 | R32~R39 | 게임별 난이도·모드를 저장하고 All/Classic/Item 상위 10개 및 모드별 순위 진입 판정 | `ScoreStoreDifficultyTest`, `ScoreStoreModeTest`, `ModeScoreUiTest`, `DesktopSmoke`; 구현·로컬 검증 |
| 실행 환경·관리·테스트·배포 | R40~R53 | Java 21, Gradle/JUnit/JaCoCo/CI/jpackage 체계를 유지하고 2차 기능까지 같은 분모·패키지로 검사 | `build desktopSmoke packageRelease`, `PerformanceRegressionTest`, CI·package reports; 최종 SHA CI와 실제 Windows 11은 대기 |

기존 55개 독립 회귀는 요구사항 1 유지의 주요 자동 근거지만, 그 개수만으로 R01~R53 전체 수동 조건까지 통과했다고 판정하지 않는다. 실제 OS, 물리 키, 사람의 색 구분, 깨끗한 환경의 저장·재실행은 별도 결과가 필요하다.

## R2-07 종료 조건

다음 항목이 모두 같은 최종 commit에 연결되어야 R2-07을 완료로 판정한다.

- [ ] PR #18→#19→#20의 최신 상태와 main 반영 순서를 확인하고 병합 commit을 기록한다.
- [x] 로컬 검사 SHA `0412df9`에서 `clean build desktopSmoke packageRelease --rerun-tasks`를 재실행하고 JUnit·독립 회귀·JaCoCo·분포 CSV·렌더링 PNG·패키지 checksum을 보관했다.
- [x] `15e46ec`의 origin CI `40185986802`·fork CI `40185944683` 성공을 확인하고 원본 보고서·배포 아티팩트를 대조했다.
- [ ] 이후 DesktopSmoke 보강까지 포함한 최종 SHA의 Windows/macOS CI를 새로 연결한다. 원격 CI는 실제 Windows 11 사용자 검증을 대신하지 않는다.
- [ ] 실제 Windows 11에서 설치/압축 해제, 더블클릭 실행, 물리 키·반복 입력, Classic/Item, 저장·완전 종료·재실행을 확인한다.
- [ ] 사람이 Normal/Protanopia/Deuteranopia/Tritanopia에서 기본 블록과 L/O/C/B/P를 구분할 수 있는지 확인한다.
- [ ] 보드 카드의 담당·상태와 Issue/PR/재검증 링크를 대조하고, 미확인 항목은 Done 대신 보류 사유를 남긴다.
- [x] 현재 자동/자료 확인 결과와 미완료 조건을 [R2-07 최종 검증 기록](req2-final-verification.md)에 검사 SHA·환경·명령·산출물 위치와 함께 기록했다. 최종 main과 수동 결과는 추가 인수 대기다.

## 현재 발견된 미완료 조건

제품 요구에서 새로 누락된 필수 기능은 이 대조에서 발견되지 않았다. 다만 다음은 아직 완료 증빙이 아니다.

1. PR #18·#19·#20은 마지막 확인 기준 main 병합 전이다.
2. `15e46ec`의 origin/fork CI 성공과 실제 보고서·배포물 대조는 완료했다. 이후 DesktopSmoke 보강을 포함한 최종 브랜치의 양 OS CI는 별도로 필요하다.
3. 실제 Windows 11 환경이 없어 패키지 실행·물리 키·저장 후 종료/재실행은 미검증이다.
4. 자동 픽셀 대비 검사는 사람의 색각·문자 식별 판정을 대신하지 않는다.
5. 최소 사양 1.2GHz CPU·RAM 1GB·디스크 500MB의 실제 Swing 플레이 성능은 기존 핵심 연산 성능 검사만으로 증명되지 않는다.
6. 원본 저장소 Issues 기능이 꺼져 있어 실제 Issue 등록은 관리자 활성화 후 진행해야 한다. 등록 대기 본문과 이 사유를 최종 원장에 남겼다.
