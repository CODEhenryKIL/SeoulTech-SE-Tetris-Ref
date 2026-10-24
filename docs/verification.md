# 검증 기록

## TEST-07 Line Coverage

- 측정 대상: `src/main/java`의 모든 제품 클래스. 생성 코드나 제품 패키지를 제외하지 않는다.
- 측정 방법: Gradle `test`가 실행하는 JUnit 검사와, `LegacyRegressionTest`가 별도 JVM으로 실행하는 독립 회귀 검사를 같은 JaCoCo 실행 데이터에 합산한다.
- 거짓 통과 방지: 자식 JVM마다 JaCoCo Java agent를 명시하며 `seoultech.se.tetris.*`만 계측한다. JUnit 어댑터는 자식 실행 전후 coverage 파일 크기가 실제로 증가했는지도 확인한다. 부모 JUnit 어댑터만 측정한 결과로 기준을 통과할 수 없다.
- 시간 제한: 개별 독립 검사는 기존 20초 제한을 유지한다. 55개 자식 JVM 계측을 포함한 전체 어댑터는 느린 CI를 고려해 10분 안에 끝나야 한다.
- 합격 기준: 전체 제품 코드의 line covered ratio 70% 이상. `check`가 `jacocoTestReport`와 `jacocoTestCoverageVerification`을 모두 실행한다.
- 결과 위치: `build/reports/jacoco/test/html/index.html`, `build/reports/jacoco/test/jacocoTestReport.xml`.
- 1차 격리 검증: 기존 회귀 55/55와 성능 검사 통과, 제품 line 934 covered / 211 missed = 81.57%. 최종 통합 commit에서는 추가된 제품 코드까지 포함해 다시 측정한다.

## TEST-08 비기능 성능 검사

- 시나리오: 실제 게임 입력마다 호출되는 아래·좌·우 이동 및 회전 충돌 판정을 빈 20×10 보드에서 400,000회 수행한다.
- 기능 조건: 모든 판정을 수행하고 중앙 생성 위치의 결과가 전부 성공이어야 한다. 시간만 재고 동작을 생략하는 검사가 아니다.
- 자동 합격 기준: 준비 반복 10,000회 후 핵심 판정 400,000회를 5초 안에 완료. JUnit 자체 제한은 10초다.
- 실행 환경 기록: JUnit XML의 `PERFORMANCE core-decisions` 로그에서 실제 작업 수·시간·예산을 확인한다.
- 1차 격리 결과: macOS 26.6.2, Temurin 21.0.12.1에서 400,000회 26ms. `clean test jacocoTestReport jacocoTestCoverageVerification` 전체 실행 1분 45초.
- 한계: 이 검사는 핵심 규칙의 심각한 성능 퇴행을 잡는 재현 가능한 CI 기준이다. 1.2GHz CPU·RAM 1GB·디스크 500MB 환경에서 전체 Swing 화면의 체감 플레이 성능을 증명하지 않는다. 해당 최소 사양의 패키지 실행은 RELEASE-04에서 별도로 확인한다.

## CORE-16 줄 삭제 애니메이션

- 완성 행은 210ms 동안 반투명하게 강조하되 게임 격자에서는 즉시 삭제하고 위쪽 행을 바로 내린다.
- headless EDT 검사에서 강조 상태와 애니메이션 Timer 시작, 210ms 뒤 자연 종료, 명시적 `stopAnimations()` 정리를 각각 확인한다.
- 강조 시작·종료 과정에서 게임 낙하 Timer의 객체·간격·실행 상태, 레벨 및 일시정지 상태가 바뀌지 않는지 확인한다.


## 최종 로컬 통합 검증 (2026-10-24)

- Java 21 `./gradlew --offline --no-daemon --console=plain desktopSmoke build packageRelease`: 성공(3분 26초).
- 독립 회귀 55/55 + 추가 JUnit 16개 성공(JUnit XML 합계 17개 중 1개는 55검사 어댑터). 실패·오류 0.
- 모든 `src/main/java` 제품 클래스의 line coverage: 1,061 covered / 240 missed = **81.55%**, 제외 패키지 없음. 70% gate 통과.
- 핵심 판정 400,000회 49ms / 5,000ms 예산. 전체 GUI의 최소 사양 성능을 증명하는 값은 아니다.
- 실제 JFrame 통합 검사: 메뉴 순환, 반복 아래 입력, 정지·재개, 진행 중 종료, 창 크기 적용·설정 재로드, 순위 조회·초기화, 게임오버의 순위 진입/미진입 흐름 통과. 입력은 합성 KeyListener 이벤트다.
- 새로운 줄 삭제 효과는 격자를 즉시 갱신하고 원래 삭제 위치를 210ms 표시한다. 게임 타이머·키 입력을 기다리게 하는 중첩 이벤트 루프를 사용하지 않는다.
- 읽기 전용 독립 코드 검토에서 순위 미진입 이름 입력 생략·동일 기록의 최신 행 강조·안내서 문구를 보완한 뒤 코드 차단 이슈 없음. 실제 OS 확인의 완료 조건은 유지한다.

## RELEASE-04 검증 범위와 남은 조건

| 환경 | 확인한 내용 | 남은 확인 |
|---|---|---|
| macOS 26.6.2 / Apple Silicon / Java 21.0.12.1 | 런타임 포함 ZIP 생성·SHA-256 대조·새 임시 폴더 압축 해제·`codesign --verify --deep --strict` 통과, 포함 JAR/아이콘/런처 설정 확인, 패키지 앱의 실제 시작 화면 표시 | 깨끗한 대상 사용자 환경에서 물리 키 조작·저장·종료·재실행의 전체 절차 |
| Windows Server 2025 | GitHub CI에서 빌드·회귀·커버리지·실제 JFrame 합성 입력·x64 패키지 생성 성공 확인 | 실제 Windows 11 설치·더블클릭·키 반복·플레이·저장·재실행 |

TEST-06과 RELEASE-04는 합성 입력/패키지 생성만으로 완료 처리하지 않는다. 실제 사용자 환경 검증이 남으면 In Progress에 남기고 나머지 완료 카드만 Review로 이동한다. PM-01·02·05는 이번 작업 범위에서 제외한다.


## 실제 GitHub CI와 배포 산출물 확인

소스 commit [`456d876`](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/commit/456d87618b3838690baa45abedf476c2dd284c67)의 [Java CI 40085571922](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40085571922)에서 두 OS job이 모두 success다. 양 OS 보고서 ZIP의 GitHub SHA-256을 대조한 뒤 XML·실제 창 결과를 직접 확인했다.

| CI 환경 | 검사 결과 | 제품 coverage | 실제 창 검사 |
|---|---|---|---|
| Windows Server 2025 / amd64 / Java 21.0.12.1 | 독립 55/55 + 추가 JUnit 16개 성공 | 1061/1301 = 81.55% | JFrame 합성 입력 통과 |
| macOS 26.6.2 / aarch64 / Java 21.0.13 | 독립 55/55 + 추가 JUnit 16개 성공 | 1061/1301 = 81.55% | JFrame 합성 입력 통과 |

배포 아티팩트 바깥 ZIP의 GitHub digest와 안쪽 실행 ZIP의 `.sha256`을 모두 대조했다. 안쪽 JAR·아이콘·패키지 데이터 경로 설정·OS 런처·포함 JVM 라이브러리 존재도 확인했다.

| 패키지 | 실행 ZIP SHA-256 | 다운로드 |
|---|---|---|
| Windows x64 | `db21c20e24df7c112600825c00ed797bd9d73430852c9a033db82f3d4998a360` | [Windows package](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40085571922/artifacts/12556459967) |
| macOS Apple Silicon | `b56948801e977c18c484761a827eb5eb5344afc9235d1032c87882424321e093` | [macOS package](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40085571922/artifacts/12556509597) |

CI 보관 기간은 7일이다. 만료 시 동일 소스에서 개발 안내의 OS별 명령으로 다시 생성·검증한다. CI 성공은 위에서 명시한 실제 Windows 11/사용자 환경 검증의 남은 조건을 해소하지 않는다.

## PR #15 이후 main 통합 검증 (2026-10-24)

- 통합 기준: 요구사항 2 PR #15의 main merge commit `729ceaf`를 요구사항 1 브랜치에 반영.
- 시작 메뉴를 CLASSIC·ITEM·MENU·SCORE·EXIT에 맞추고, 기존 회귀 검사의 탐색 인덱스와 미리보기 여백 색 기대값을 현재 화면에 맞게 조정. 독립 검사 55개는 그대로 유지.
- Java 21 `./gradlew --offline --no-daemon --console=plain build desktopSmoke packageRelease` 성공(1분 50초). JUnit 17개(독립 회귀 55/55 어댑터 포함), 실패·오류 0.
- 모든 제품 클래스 line coverage: 1,068 covered / 436 missed = **71.01%**. 아이템 코드를 포함해 측정하며 제품 제외·70% 기준 변경 없음. 아이템 전용 시나리오는 이번 요구사항 1 검증 범위에 추가하지 않았다.
- 핵심 판정 400,000회 87ms / 5,000ms 예산.
- 일반 행 삭제는 요구사항 1의 비차단 강조 표시를 유지한다. 아이템 전용 효과의 중첩 이벤트 루프는 main 구현을 유지하여 동일한 보장을 하지 않는다.
- 요구사항 2 후속 범위: 난이도 선택 UI 누락, 아이템 애니메이션 중 일시정지·행 아이템 표시/점수·Anchor 동작, 난이도별 낙하 속도, 모드별 점수 메타데이터 및 신규 요구사항 검증. 이번 통합의 통과 결과는 요구사항 2 전체 완료를 의미하지 않는다.
- 실제 Windows 11과 깨끗한 사용자 환경의 수동 검증은 위 TEST-06·RELEASE-04 조건대로 남는다.
- 통합 후 macOS aarch64 실행 ZIP 생성과 SHA-256 대조, 런처·JAR·JVM·아이콘 포함 확인: `f81e3a206377364a36b803f3f4776cda1def49096d1d12a76d6852f986d7b06a`.
