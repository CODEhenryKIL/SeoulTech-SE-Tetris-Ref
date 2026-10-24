# 1차 요구사항 Todo 24개 처리 계획

- 확인일: 2026-10-24 (Asia/Seoul)
- 저장소: `HoroyoiPeach/SeoulTech-SE-Tetris-Ref`
- 계획 브랜치: `docs/req1-nondev-tasks`
- 코드 기준: `a54d5dd6d7b8434fb96c3e8880aba5c13921864a` — 로컬 HEAD·origin/main과 GitHub API의 main 일치, PR #14 병합 상태
- 원본: [GitHub 보드 View 1](https://github.com/users/HoroyoiPeach/projects/3/views/1)
- 최초 계획 범위: 최신 카드 전수 확인 및 실행 계획 작성. 이후 사용자 승인에 따른 SETUP-04·TEST-01 실행 결과는 8절에 기록. 후속 사용자 요청으로 두 카드의 상태만 In Progress로 변경(8절).

## 1. 확인 결과와 범위

브라우저에서 필터가 비어 있는 View 1의 **Todo 24 / In Progress 3 / Review 0 / Done 40**을 확인했다. 이번 “남은 24개”는 Todo 열 전체다. 전체 미완료는 In Progress를 포함하면 27개이므로 두 수를 혼용하지 않는다.

Todo 24개를 각각 열어 제목·본문·Status·Assignees·사용자 정의 `담당자` 필드를 확인했다. 모두 Draft item이고 Status는 Todo이며, 본문은 모두 `No description provided`다. GitHub Assignees는 모두 미지정이고, 사용자 정의 담당자는 SETUP-03만 `팀 공동 논의 (담당 확정 전)`, 나머지 23개는 공란이다. 따라서 아래의 산출물·완료 기준·역할은 **카드에 이미 합의된 조건이 아니라, 카드 제목과 요구사항 추적표에 근거한 계획 제안**이다. 실제 배정은 PM-01에서 기록한다.

| 분류 | 확인된 카드 | 이번 계획에서의 취급 |
|---|---|---|
| Todo 24 | SETUP 4 + INPUT 1 + TEST 9 + RELEASE 5 + PM 5 | 3절의 24행 원장으로 전수 관리 |
| In Progress 3 | CORE-15 난이도 별 블록 등장 확률 조정 / RANK-05 스코어보드 내 난이도 표기 / CORE-16 라인 제거 애니메이션 구현 | 모두 담당자 최승빈. 24개 작업에 합산하지 않고 파일 충돌·최종 검증 버전만 조율 |
| Done 중 관련 카드 | CORE-13 레벨·속도 / SCORE-01 칸 낙하 점수 / SCORE-02 속도 추가 점수 | 새 Todo로 편성하지 않음. TEST-04에서 실제 요구 충족 여부 검증 |

기존 초안의 “최신 24개 미확인”과 “INPUT-02 누락”을 이번 확인으로 해소했다. `docs/req1-nondev-tasks.md`는 과거 접근 실패 당시의 점검 기록이며, 현재 카드 범위·상태는 이 문서의 확인 결과를 따른다. 이번에는 원본 카드의 빈 본문을 편집하거나 Issue로 전환하지 않았다.

## 2. 계획의 근거와 우선순위

| 근거 | 현재 확인 내용 | 계획에 미치는 영향 |
|---|---|---|
| E1: [요구사항 추적표](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/docs/requirements-1-traceability.md#L23-L90) | R01~R53, 특히 R18 반복 입력·R40~R52 환경/검증/배포와 카드 연결 | 카드 제목만으로 끝내지 않고 요구사항별 증빙 연결 |
| E2: [해석·팀 결정](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/docs/requirements-1-traceability.md#L94-L119) | 두 OS 지원은 팀 선택. coverage 원문은 “50-70% 이상”, 70% 이상은 제안. Gradle/JUnit/JaCoCo 등은 팀 구현 방안 | OS 두 종류 검증 계획 유지, coverage 기준 구분, 도구 선택을 교수님 지정으로 표현하지 않음 |
| E3: [기존 회귀 검사](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/tests/RefactoringRegressionTest.java#L31-L114) | 52개 독립 실행 검사, 기존 동작 보존 목적. 실제 창·Windows·coverage는 범위 밖 | “테스트가 없다”에서 시작하지 않고 기존 검사를 보존·매핑한 뒤 공백 보강 |
| E4: [자식 JVM 실행 구조](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/tests/RefactoringRegressionTest.java#L147-L184) | 각 검사를 임시 폴더/별도 JVM에서 실행 | TEST-01 때 계측 방법 설계, TEST-07에서 제품 코드 실행의 보고서 반영 확인 |
| E5: [REF-09·10 검증 기록](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/docs/refactoring-discussion.md#L377-L407) | 이전 52/52 및 macOS 일부 실제 조작 기록 존재, Windows는 미검증 | 이전 결과 재사용 범위를 명시하고 최종 commit/배포물을 다시 검증 |
| E6: [반복 입력 경로](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/src/seoultech/se/tetris/component/Gamepanel.java#L204-L251) | keyPressed에서 이동·회전·드롭·정지 처리 | INPUT-02를 독립 카드로 검증하고 TEST-06·08과 결과 공유 |
| E7: [설정 경로](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/src/seoultech/se/tetris/component/Maincontainer.java#L236-L300), [점수 저장 경로](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/src/seoultech/se/tetris/score/ScoreStore.java#L12-L57) | setting.txt·tetris_score.txt는 상대 경로 사용 | RELEASE-01에서 쓰기 가능한 저장 위치 결정, RELEASE-04에서 재실행/권한 확인 |
| E8: [완료 절차](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/blob/a54d5dd6d7b8434fb96c3e8880aba5c13921864a/CONTRIBUTING.md#L41-L50) | 직접 검증 → PR → Review → 다른 팀원 확인 → main 병합 → Done | 파일이 존재하는 것과 카드 완료를 구분 |
| E9: [기준 commit의 저장소 파일 목록](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/tree/a54d5dd6d7b8434fb96c3e8880aba5c13921864a) | Gradle/Maven 설정·Wrapper·.github/workflows·패키징 스크립트·아이콘·사용자 안내서·coverage 보고서 없음. .github에는 Issue/PR 양식 존재 | SETUP-04·TEST-01·SETUP-08이 검증/배포의 선행 작업 |

우선순위는 **검증을 가능하게 만드는 기반 → 요구 충족 확인 → 배포·제출**이다. PM-01~04는 첫 단계에 착수해 계속 갱신한다. RELEASE-01과 아이콘 시안·안내서 초안은 테스트 보강과 병행할 수 있다. 아래 순번은 권장 착수 순서이며, 선행 조건 열이 실제 의존관계다. 지속 관리 카드의 최종 종료는 후반에 이루어진다.

## 3. 카드별 처리 원장 — 24개, 권장 착수 순서

최초 전수 확인 당시 전 행의 상태는 **Todo**, 본문은 **없음**이었다. 실행 후 상태는 8절을 따른다. 담당자는 SETUP-03의 공동 논의 표시를 제외하면 미지정이다. 역할명은 실제 담당자 배정이 아닌 제안이다. 파일 경로는 향후 산출물 후보이며 이번에 생성하지 않는다.

| 순번 | 카드·원본 링크 | 선행 조건 / 제안 역할 | 산출물과 검증 가능한 완료 기준 |
|---|---|---|---|
| 1 | [SETUP-01 팀원 저장소 접근 및 로컬 개발환경 구성](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251443431) | 없음 / 각 팀원 확인·취합 | `docs/development.md`에 팀원별 접근·OS·Java 21+ 버전·checkout·현재 실행 명령의 실제 결과 기록. 다른 사람 환경을 추정하지 않음. Gradle 명령은 SETUP-04 후 보완. R42, E1·E3 |
| 2 | [PM-01 팀 역할과 1차 작업 분담 기록](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469804) | SETUP-01과 병행 / 관리 취합 | `docs/team-roles.md`에 24개별 작업자·검토자·OS 확인자·담당 범위 기록. 현재 23개 공란을 완료 배정으로 간주하지 않음. 최종 실제 기여도 갱신. R44, E1·E8 |
| 3 | [SETUP-03 참고 코드 활용 범위 및 재구성 전략 결정](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251468637) | 초기 현황 확인 / 팀 공동 논의 | `docs/reference-strategy.md`에 유지·수정·분리한 참고 코드와 REF 기록, 후속 확장 방향·제외 범위·팀 검토 기록. 현재 구현을 전부 새로 만들기로 단정하지 않음. R02, E2·E5 |
| 4 | [PM-02 주요 설계 의사결정 ADR 관리](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469821) | SETUP-03부터 지속 / 결정 취합 | `docs/adr/`에 확인된 기존 결정과 신규 결정의 이유·대안·영향·검토자 연결. 우선 디렉터리 이동·테스트 실행/계측·coverage 범위·배포/저장 정책 결정. 추적표의 과거 “미결정” 목록을 실제 코드·논의와 대조. R02·R26·R42·R53, E1·E2·E4·E7 |
| 5 | [PM-03 이슈·PR·테스트 증빙 연결](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469850) | 초기부터 지속 / 증빙 취합 | `docs/req1-evidence.md`에 카드↔R번호↔PR/commit↔산출물↔검증 링크 24행 유지. Draft item은 원본 project 링크로 식별. 실행 때 각 PR마다 갱신하고 최종 누락 0건 확인. R42·R43, E1·E8 |
| 6 | [PM-04 중간 점검용 요구사항 충족 현황 작성](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469875) | 최초 코드/증빙 대조 후 반복 / 요구사항 취합 | `docs/req1-status.md`에 R01~R53의 충족·부분·미충족·미검증과 근거·조치 기록. 첫 현황을 먼저 작성하고 테스트/배포 이후 최종 갱신. Done 카드도 증빙 없으면 요구 검증 완료로 단정하지 않음. R45, E1·E5 |
| 7 | [SETUP-04 Gradle 프로젝트 및 표준 디렉터리 구성](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251468662) | 초기 경로 정책·충돌 조율 / 빌드 | `build.gradle` 또는 `.kts`, settings·Wrapper·표준 source/test 배치·개발 명령. 새 checkout에서 Java 21 compile/run 재현, 기존 52검사 실행 경로 보존. 이동 전후 패키지·리소스·실행법 대조. R49, E3·E9 |
| 8 | [TEST-01 JUnit 5 테스트 기반 구성](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469492) | SETUP-04 / 테스트 기반 | JUnit 5 발견·실행·실패 전파·결과 보고 구성. 기존 52검사를 유지한 채 Gradle 검증에 연결하고 신규 JUnit 표본도 실행. 실패 표본에서 명령 실패 확인 후 표본 제거. 자식 JVM 계측 방식을 정해 TEST-07에 전달. R46·R49, E3·E4 |
| 9 | [SETUP-08 기본 CI 빌드·테스트 워크플로 구축](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251468748) | SETUP-04·TEST-01 / CI | `.github/workflows/`에서 Windows/macOS, Java 21의 PR/push 빌드·테스트와 로그/결과 보관. 정상 성공·실패 표본의 CI 실패를 확인하고 정상화. 이후 coverage도 추가. 호스팅 runner의 통과는 대상 OS 배포 검증과 별도. R49, E2·E8·E9 |
| 10 | [RELEASE-01 배포 방식 결정 (jpackage 권장)](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469689) | PM-02·SETUP-04 / 배포 설계 | `docs/release-design.md`에 Windows 11/macOS 26+ 형식·CPU 아키텍처·런타임 포함 여부·Java 전제·저장 위치·OS별 생성/검증 환경 명시. jpackage를 우선 검토하되 채택 결과와 이유 기록. R50·R52, E2·E7 |
| 11 | [TEST-02 블록·회전·충돌 단위 테스트](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469522) | TEST-01 / 게임 규칙 검증 | 기존 block/rotation/collision 계열과 R15·R16·R21·R46 매핑. 7종·4회전·벽·바닥·고정 블록·막힌 회전의 정상/경계/불변 조건 보강. 예상값 근거와 반복 성공 결과 보관. E1·E3 |
| 12 | [TEST-03 고정·줄 삭제·게임 종료 단위 테스트](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469540) | TEST-01, TEST-02 결과 활용 / 게임 규칙 검증 | 고정→다음 블록·한 줄/여러 줄/최상단 삭제·보충·생성 불가 종료·타이머 정지 검증. 기존 lock/line/game-over 검사를 활용하고 공백만 추가. R09·R21·R32·R46, E1·E3 |
| 13 | [TEST-04 점수·속도 단위 테스트](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469555) | TEST-01·실제 점수/속도 결정 대조 / 게임 규칙 검증 | 1초 초기 낙하·속도 증가 경계·자동/수동/하드드롭 거리별 점수·속도 가산·팀 추가 규칙 검증. 기존 기대값과 R13·R14·R24~26의 불일치는 결함으로 분리하고 기대값만 변경해 숨기지 않음. E1·E3 |
| 14 | [TEST-05 설정·스코어보드 저장 테스트](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469577) | TEST-01 / 저장 검증 | 임시 경로에서 변경→저장→새 실행 복구, 기본값/기록 초기화, 손상·누락·I/O 오류·내림차순·10개 이상·동점 정책 검증. 저장 형식과 원본 파일 보존. R29~31·R37~39·R46, E1·E3·E7 |
| 15 | [INPUT-02 반복 키 입력 즉시 처리 검증](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469178) | TEST-01·실제 UI 실행 / 입력 검증 | `docs/input-validation.md`에 반복 누름·길게 누름·서로 다른 키 연속·정지/재개·키 재설정·벽 도달 시나리오. 입력 수/간격·처리 결과·포커스·환경 기록. 이벤트 전달 수 대비 누락/중복 검증과 OS 실제 입력 확인을 구분. R18, E1·E6 |
| 16 | [TEST-06 UI 입력·화면 전환 통합 테스트 또는 수동 체크리스트](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469595) | TEST-01·INPUT-02 결과 활용 / OS별 UI 검증 | `docs/ui-checklist.md`에 시작/설정/점수판 왕복·세 크기·키 변경·반복 입력·정지/재개·진행/정지 중 종료·게임오버→이름/강조→메뉴 흐름. 각 OS의 절차·기대/실제·버전·캡처 기록. R05·R18~20·R27~28·R33~36, E1·E3·E5 |
| 17 | [TEST-08 비기능 성능 테스트 작성](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469653) | 실행 기반·INPUT-02 측정 시나리오 / 성능 검증 | `docs/performance-results.md`에 최소 1개 비기능 테스트. 권장: 일정 시간 플레이의 CPU/메모리와 반복 입력 응답 분포. 실행 전 시나리오·시간·횟수·합격 기준을 결정하고 사양/원자료/결과 기록. R18·R41·R48, E1·E2 |
| 18 | [TEST-07 JaCoCo Line Coverage 50~70% 이상 달성](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469626) | TEST-01에서 조기 계측 시작, TEST-02~05 보강 후 목표 검증 / coverage | Gradle/CI의 HTML·XML 보고서와 대상 commit·제품 코드 범위·제외 근거. 자식 JVM 실행까지 반영하고 0개 테스트/부모만 계측되는 거짓 통과 차단. 직접 작성한 코드의 line coverage 70% 이상을 목표로 제안, 최종 기준은 PM-02에 확정. R47·R49, E2·E4 |
| 19 | [RELEASE-02 애플리케이션 아이콘 제작·적용](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469711) | RELEASE-01, 시안은 조기 병행 / 시각 자산·배포 | `assets/`에 원본·OS별 아이콘·제작/사용 근거, 패키징 설정에 적용. 최종 아이콘 표시는 RELEASE-03의 양 OS 패키지에서 확인. R51, E1·E2 |
| 20 | [TEST-09 전체 회귀 테스트와 결함 정리](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469669) | TEST-02~08·INPUT-02 및 발견 결함 처리 / 통합 검증 | `docs/regression-results.md`에 기존 52검사+추가 검사의 최종 commit별 결과와 UI/성능/coverage 링크. 결함의 재현·심각도·원인·수정 PR·재검증·잔여 한계 기록. 배포/동료 변경 후 다시 검증. R49, E3·E5 |
| 21 | [RELEASE-03 더블클릭 실행 패키지 자동 생성](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469743) | SETUP-04·RELEASE-01·02, 최종본은 TEST-09 통과 commit / 패키징 | `scripts/` 또는 Gradle task/CI에서 동일 commit의 OS별 패키지 자동 생성. 버전·아키텍처·Java/런타임·아이콘·SHA-256·재생성 명령 기록. 양 OS 생성 환경 확보. R50·R51, E1·E7·E9 |
| 22 | [RELEASE-04 깨끗한 대상 환경에서 설치·실행 점검](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469761) | RELEASE-03 / Windows·macOS 각 확인자 | `docs/release-validation.md`에 Windows 11/macOS 26+ 각각 설치·더블클릭·플레이·설정/점수 저장·재실행·초기화·쓰기 권한 검증. 배포물 checksum·OS/Java/사양·추가 명령/설정 여부와 캡처 기록. R40·R41·R51·R52, E1·E2·E7 |
| 23 | [RELEASE-05 사용자 안내서와 릴리스 노트 작성](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469782) | 초안은 RELEASE-01, 확정은 RELEASE-04 / 사용자 문서 | `docs/user-guide.md`·릴리스 노트에 검증한 배포물 버전·전제·실행·키/메뉴·설정·저장/초기화·제약·알려진 문제. 문서만 보고 새 사용자가 같은 배포물을 실행하는 절차 검토. R50·R52, E1·E2 |
| 24 | [PM-05 1차 결과물 증빙 패키지 정리](https://github.com/users/HoroyoiPeach/projects/3/views/1?pane=issue&itemId=251469905) | 나머지 23개 증빙 및 리뷰/병합 확인 / 제출 취합 | `docs/req1-submission.md`에 확정 commit·코드/배포물·실행법·역할·ADR·결함/해결·전체 테스트·coverage·OS별 결과·R01~53 충족표·24개 원장 링크. 링크 접근/누락/버전 일치 확인과 다른 팀원 검토 후 최종 제출 묶음 확정. R45, E1·E8 |

## 4. 단계별 진행과 병렬 가능 범위

| 단계 | 착수·마무리할 카드 | 다음 단계로 넘길 증거 |
|---|---|---|
| A. 기준·역할·추적 시작 | SETUP-01·03, PM-01~04 착수 | 팀원 환경 표, 기준 commit, 실제 배정·미배정 구분, 최초 요구 충족 현황, 결정 목록 |
| B. 재현 가능한 검사 기반 | SETUP-04 → TEST-01 → SETUP-08 | 새 checkout 빌드/기존 52검사·JUnit 실행, 정상/실패를 판별하는 CI. 이 단계부터 coverage 계측 점검 |
| C. 요구사항 검증 | TEST-02~05, INPUT-02 → TEST-06·08, TEST-07 보강 | 요구별 정상/경계/오류 결과, 실제 UI/반복 입력, 최소 1개 성능 결과, 범위가 명확한 coverage |
| D. 배포 준비 병행 | RELEASE-01 → RELEASE-02, RELEASE-03 초안, RELEASE-05 초안 | 배포·저장 정책, 아이콘, OS별 패키징 재현 경로. B/C와 병행 가능 |
| E. 통합·배포 검증 | TEST-09 → RELEASE-03 최종 → RELEASE-04 → RELEASE-05 확정 | 동일 commit의 회귀·coverage·패키지와 양 OS 실사용 결과. 배포 중 소스 변경 시 TEST-09부터 재검증 |
| F. 관리 종료·제출 | PM-01~04 최종 갱신 → PM-05 | 24개 카드의 산출물/리뷰/병합 증빙, R01~53 충족표, 제출 목차 |

- **주요 의존 경로**: SETUP-04 → TEST-01 → 요구별 검사/coverage → TEST-09 → RELEASE-03 → RELEASE-04 → RELEASE-05 → PM-05. SETUP-08은 TEST-01 직후 연결하고 이후 결과를 지속 관리한다.
- **독립 작업**: TEST-02·03·04·05는 기반 완료 후 테스트 파일을 나눠 병행 가능. INPUT-02·TEST-06·08은 공통 시나리오/측정 결과를 공유하되 각 카드의 판정을 따로 남긴다. RELEASE-01·아이콘 시안·문서 초안도 병행 가능.
- **공유 파일 제약**: Gradle/공통 fixture/회귀 실행기·Gamepanel·Board·Maincontainer는 변경 창구를 정하고 순차 통합한다. 경로 이동은 짧은 별도 변경으로 처리하고 In Progress 3개의 작성자와 통합 시점을 맞춘다. 팀 작업 규칙의 파일 충돌 절차 적용.
- **버전 경계**: In Progress 3개는 시작 조건이 아니다. 최종 제출에 포함하면 병합 후 TEST-03·04·05·06·08·09와 관련 배포 검증을 다시 수행한다. 제외하면 검증한 1차 제출 commit을 고정하고 릴리스 노트에 범위를 기록한다.
- **일정 기준**: 인원별 가용 시간·제출 마감이 제공되지 않았으므로 날짜/소요일을 임의 확정하지 않는다. PM-01에서 담당·자원 확보 후 각 단계의 완료 기준으로 일정을 정한다.

## 5. 위험과 대응

| 위험·현재 근거 | 대응 및 종료 조건 |
|---|---|
| 모든 카드의 본문·대부분 담당자 공란 | 원장의 완료 기준은 제안으로 표시. 실행 시작 시 담당/검토자와 조건을 기록하고 실제 결과로 판정 |
| 표준 디렉터리 이동과 동료 기능 브랜치 충돌 (E9, In Progress 3개) | SETUP-04에서 경로 매핑·영향 확인, 기능 변경과 분리. 이동 전후 기존 검사를 보존. 당장 이동이 곤란하면 sourceSets 임시 유지 이유와 후속 이동을 남기고 SETUP-04 완료로 오인하지 않음 |
| 기존 52/52를 요구사항 충족으로 오인 (E1·E3·E5) | 기존 보존 검사와 요구 검사를 분리. 점수/속도/게임 중 종료 등 불일치는 재현·결함으로 기록. 제품 수정은 별도 범위/PR로 연결 후 재검증 |
| 부모 JVM만 계측하거나 보고서 누락으로 거짓 통과 (E4) | TEST-01에서 계측 실행 방식 설계. TEST-07에서 알려진 제품 메서드가 실제 보고서에 잡히는지 확인하고 누락·분모 0은 실패 처리 |
| coverage 비율을 높이기 위한 임의 제외 (E2) | 직접 작성/참고 코드와 측정 분모·제외 근거 기록. 가능하면 전체 제품 코드 수치도 함께 제시. 원문과 팀 목표·확정 기준을 구분 |
| 반복 입력·성능의 “즉시”가 정량화되지 않음 (E1·E6) | 측정 전 입력 간격/횟수·응답 지표·허용 기준을 PM-02에 기록. 임의 ms 값을 교수님 요구로 만들지 않음. 전달 이벤트 대비 처리 정확도와 실제 OS 반복 입력을 모두 확인 |
| 새 Mac 결과만으로 최소 사양/Windows 지원 주장 (E1·E2·E5) | Windows 11/macOS 26+ 확인자·환경을 초기에 확보. 최소 사양 실기/제한 환경이 없으면 고사양 결과와 그 한계를 분리하며 해당 조건은 미검증 유지 |
| 더블클릭 작업 디렉터리·설치 위치 쓰기 권한 문제 (E7) | RELEASE-01에서 데이터 위치·기본 파일·기존 기록 정책 결정. RELEASE-04에서 설치 후 재실행·다른 실행 경로·저장 실패를 검증 |
| OS별 패키징 환경/CPU 아키텍처 부족 | RELEASE-01에서 양 OS 생성·검증 자원과 배포 아키텍처 확정. 생성 성공만으로 설치/실행 성공을 대체하지 않음 |
| 제출 직전 병합으로 테스트·배포물 버전 불일치 | 최종 소스 commit·로그·coverage·배포물 checksum 연결. 변경이 생기면 영향을 받는 검증을 갱신한 후 PM-05 확정 |

## 6. 실행 시 PR 분할과 카드 종료

이번 계획 문서는 현재 `docs/req1-nondev-tasks`에서 검토한다. 이후 구현은 최신 main에서 목적별 브랜치로 나누는 안이다. 다음 항목은 **향후 실행 제안**이며 이번에는 PR 생성·push·카드 이동을 수행하지 않는다.

| PR 단위 제안 | 주요 카드 | 검토 포인트 |
|---|---|---|
| 기준·역할·결정/추적 문서 | SETUP-01·03, PM-01~04 초기 | 실제 확인/제안 구분, 원본 요구 연결, 담당자 추정 없음 |
| Gradle/디렉터리·테스트 기반 | SETUP-04·TEST-01 | 경로 이동·패키지·기존 52검사 보존·실패 전파·계측 준비 |
| 최소 CI | SETUP-08 | 로컬과 같은 명령, OS별 결과·실패 검출. 기반 PR 직후 진행 |
| 요구별 테스트·UI/성능·coverage | TEST-02~08·INPUT-02 | 독립된 검사 묶음별 PR 분리, 요구 기대값·실제 입력·측정 범위 |
| 배포 설계·아이콘·자동 생성 | RELEASE-01~03 | 데이터 경로·런타임·아키텍처·OS별 재현, 최종 TEST-09 통과 버전 |
| 최종 회귀·배포 검증·사용자 문서 | TEST-09·RELEASE-04·05 | 동일 버전·두 OS 증빙·잔여 결함, 수정 발생 시 재검증 |
| 제출 증빙 | PM-01~04 최종·PM-05 | 전체 24개와 R01~53의 결과·링크·리뷰/병합 연결 |

카드마다 `카드 ID / 요구사항 / 작업자·검토자 / 기준 commit / 산출물 / 검증 절차·결과 / PR / 병합 commit / 잔여 제한`을 남긴다. E8에 따라 직접 검증 → PR → Review → 다른 팀원 1명 이상 확인 → main 병합 → Done을 적용한다. 공동 PR이어도 카드별 완료 기준은 각각 판정한다. PM-01~04는 초기 산출물 이후에도 제출 버전까지 기록을 갱신한다.

## 7. 계획 수용 기준과 실행 검증 순서

### 이번 계획 작성의 수용 기준

- [x] 실제 View 1의 Todo 24개와 24행 원장을 1:1 대조, 고유 itemId·원본 링크 포함
- [x] In Progress 3개·Done 카드와 Todo 범위를 구분
- [x] 카드 본문·상태·두 담당자 필드 확인, 빈 조건을 합의로 표현하지 않음
- [x] 24개 모두 선행 조건·역할 제안·산출물·검증 가능한 완료 기준 포함
- [x] 기존 52검사·자식 JVM·미검증 Windows/coverage·파일 경로 위험 반영
- [x] 최초 계획 단계에서 구현 및 GitHub 변경 없이 지정 계획 문서만 갱신

### 향후 실행의 검증 순서

1. 기준 commit·환경 기록 → 새 checkout의 빌드/실행·기존 검사·JUnit 발견/실패 전파 확인.
2. 최소 CI의 정상 성공/의도된 실패 확인 → 정상 상태로 복원.
3. 요구사항별 자동 검사 → 실제 UI/반복 입력 → 성능 최소 1개 → 제품 코드 계측과 line coverage 기준 확인.
4. 결함 수정/재검증 → 최종 commit의 전체 회귀 및 요구 충족표 갱신.
5. 같은 commit의 양 OS 패키지·checksum 생성 → 깨끗한 Windows 11/macOS 26+ 설치·더블클릭·저장/재실행 검증 → 사용자 안내서 대조.
6. 24개 카드의 완료 기준·검토·main 병합·증빙 링크 전수 확인 → 제출 묶음 확정. 미검증/실패 항목은 관련 카드를 Done 처리하지 않음.

최초 계획 단계에서는 게임 테스트·성능 측정·패키징을 실행하지 않았다. 당시 52/52는 E5의 이전 검증 기록이었다. 이후 이번 묶음의 새 검증 결과는 8절에 기록한다. 후속 작업에서 남는 결정은 실제 담당/검토자·일정, 디렉터리 이동 시점, 최종 coverage 기준/범위, 반복 입력/성능 합격 기준, 배포 형식/런타임/아키텍처·데이터 정책이다.

계획 자체 검증: 24개 카드·24개 고유 itemId·순번 1~24·표 열수 대조 통과, 누락/중복 0건. 별도 읽기 전용 검토에서 의존관계·완료 기준·계획 범위의 차단 문제 없음.


## 8. SETUP-04·TEST-01 실행·검증 기록 (2026-10-24)

- 사용자 승인 범위: 첫 묶음인 Gradle·JUnit 기반 구성, 검증 및 현재 브랜치 푸시까지. PR 생성·병합·후속 CI 작업은 포함하지 않음.
- 구현 commit: `ef3239e0c8a3a14dc24f179a626b3226993958ef` (`build: Gradle과 JUnit 5 테스트 기반 구성`). 이후 이 계획/증빙 문서만 추가.
- 실행 환경: macOS 26.6.2 / arm64 / Temurin JDK 21.0.12.1+1.
- 보드 후속 요청 반영: SETUP-04(`251468662`)·TEST-01(`251469492`)을 Todo → In Progress로 변경하고 저장 결과 확인. 현재 Todo 22 / In Progress 5 / Review 0 / Done 40. 담당자·본문은 변경하지 않음.

### 변경 산출물

- `build.gradle`·`settings.gradle`: Java toolchain/release 21, UTF-8, application 실행, JUnit 5 BOM 5.14.1·Jupiter·Platform Launcher, 테스트 미발견 실패 처리.
- Gradle Wrapper 9.8.2: `gradlew`·`gradlew.bat`·Wrapper JAR/properties. 공식 ZIP SHA-256 고정, JAR도 공식 SHA-256과 일치 확인.
- 제품 소스 20개를 `src/main/java`로 이동. 기존 파일과 20/20 byte-identical, 패키지·게임 규칙 변경 없음.
- 기존 52검사를 `src/test/java`로 이동. 독립 실행 명령 주석의 경로 외에는 기존 검사와 기대값 유지.
- `LegacyRegressionTest`: JUnit 어댑터 1개가 기존 52검사를 별도 JVM에서 실행. 로그·실패 종료 코드·5분 제한과 자식 프로세스 종료 완료를 JUnit 결과에 반영.
- `.gitignore`·`.gitattributes`, README 및 [개발 실행 안내](development.md): 생성 파일 제외, Wrapper 줄바꿈·명령·설치 조건·테스트 보고서·후속 coverage 계측 주의점 기록.

### 새 검증 결과

| 검증 | 결과 |
|---|---|
| 변경 전 Java 21 직접 컴파일·회귀 | 컴파일 오류 0, 기존 52/52 통과 |
| Wrapper `./gradlew --no-daemon --console=plain clean build` | 성공, JUnit 어댑터 1개 통과, XML 내부 로그의 `RESULT 52/52 passed` 확인 |
| 의도적 회귀 실패 | 임시 복사본의 첫 검사만 실패 주입 → 기존 실행기 실패 → JUnit 실패 → Gradle exit 1 확인. 원본 수정 없음 |
| JUnit 검사 미발견 | 임시 복사본에서 어댑터만 제외 → `did not discover any tests`로 Gradle exit 1 확인 |
| 시간 제한·프로세스 정리 | 임시 복사본에서 제한만 1초로 단축 → 제한 초과 실패, 종료 도우미 오류 없음, 검사 JVM 잔류 없음 확인 |
| 깨끗한 commit 산출물 재현 | 로컬 전체 이력 clone이 지연돼 해당 복제만 중단. 구현 commit을 `git archive`로 새 폴더에 추출한 뒤 `./gradlew --offline --no-daemon --console=plain clean build` 성공·52/52 재확인. 기존 작업 폴더의 build/.gradle 결과는 사용하지 않고 다운로드한 의존성 캐시만 재사용 |
| 실행 진입점 | 임시 복사본에서 `./gradlew run`으로 Tetris 프로세스 기동 확인 후 종료. 전체 UI 동작 검증은 별도 |
| 원본 보존·정적 확인 | 제품 소스 20개 및 setting.txt/tetris_score.txt 해시 동일, 기존 검사는 실행 경로 주석만 변경. 컴파일 오류 0, 기존 경고 유지, staged diff 공백 검사 통과 |
| 독립 읽기 전용 검토 | 시간 초과 시 자식 종료 완료 대기를 보강한 뒤 재검토에서 차단 문제/잔여 이슈 0건 |

로그는 작업 공간 밖 임시 검증 폴더에 보관하며, 반복 실행법과 보고서 위치는 개발 안내에 기록했다.
Windows 실제 실행·두 OS 배포·요구사항별 신규 검사·성능·JaCoCo·GitHub CI는 이번 검증에 포함하지 않았다.
두 카드는 구현·로컬 검증을 마쳤지만 사용자 요청대로 In Progress를 유지한다. 종료 지점은
`fork/docs/req1-nondev-tasks` 푸시까지이며, PR/팀 검토·main 병합·Done 전환은 후속 작업이다.


## 9. SETUP-08 CI 실행·검증 기록 (2026-10-24)

- 구현: `1005babe0f07331af9f93b3879cf732087a90b89` — Windows/macOS Java 21 빌드·테스트 자동화.
- 로그 처리 보완: `bc79a607255c170aea555893a6b61327142b31f5` — Windows에서 발견한 한글 표준 출력 인코딩 차이를 회귀 실행기 진입점의 UTF-8 PrintStream 설정으로 해결. 부모/내부 검사 JVM 모두 적용하며 제품 코드·검사 기대값은 변경하지 않음.
- 대상: `windows-2025`(Windows Server 2025/PowerShell)·`macos-26`(macOS 26/Bash), Temurin Java 21, 저장소의 Gradle Wrapper 9.8.2.
- 동작: 모든 브랜치 push와 PR 생성/갱신 → Wrapper 검증 → OS별 `build` → 성공/실패 모두 테스트 XML·HTML 및 생성된 Gradle 문제 보고서를 OS별 아티팩트로 7일 보관.
- 외부 Actions: checkout v7.0.1, setup-java v6.1.0, setup-gradle v6.4.0, upload-artifact v7.0.2의 공식 릴리스 full commit SHA 고정. `contents: read`, checkout credential 비보존.

| 검증 | 결과·근거 |
|---|---|
| 설정 정적 검사 | 공식 actionlint 1.7.12 바이너리의 배포 SHA-256 확인 후 문법·식·실행 환경·Action 검사 통과. `git diff --check` 통과 |
| 정상 push 실행 | [Java CI 40081502628](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40081502628), 기준 commit `bc79a60`: Windows·macOS 두 job 모두 success |
| 실제 검사 수 | 양 OS의 보관 ZIP을 내려받아 각 SHA-256 대조 후 JUnit XML 확인. 각 JUnit 어댑터 1개 성공, 내부 PASS 52행과 `RESULT 52/52 passed` 확인 |
| 정상 보고서 | [Windows 보고서](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40081502628/artifacts/12555501826), [macOS 보고서](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40081502628/artifacts/12555516770) 업로드 성공 |
| 의도적 실패 실행 | [Java CI 40081524304](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40081524304), 임시 commit `1e7d2e55c41be09140045c028ee748ee15ed1ae0`: 첫 검사에만 AssertionError 주입 → Windows·macOS 두 job 모두 failure |
| 실패 원인·보고서 | [Windows 실패 보고서](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40081524304/artifacts/12555187934), [macOS 실패 보고서](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40081524304/artifacts/12555412106) 각각 무결성 대조·XML 확인. 주입한 메시지와 `RESULT 51/52 passed` 확인, 인코딩 예외 없음. `always()` 보고서 업로드 success |
| 임시 변경 정리 | 원본 작업 브랜치/작업 폴더에는 실패 주입 없음. 검증용 원격 브랜치 `ci/setup-08-failure-check-f8700b`는 검증 후 예상 commit을 조건으로 삭제. 실행 기록·보고서는 보관 기간 동안 위 링크에서 확인 가능 |
| 독립 검토 | CI 설정 및 UTF-8 수정 각각 별도 읽기 전용 검토에서 차단 문제·잔여 이슈 0건 |

이 검증은 fork의 실제 **push 이벤트**로 수행했다. PR 이벤트는 같은 job 구성과 정적 검사를 확인했으며,
사용자가 요청한 푸시 범위에 맞춰 PR을 새로 생성하지 않았다. `SETUP-04`와 `TEST-01`은 이전 사용자 요청으로
Review로 이동했고, `SETUP-08`은 착수 시 In Progress로 이동했다. main 병합·Done 전환은 하지 않는다.
Windows Server의 headless 회귀 통과는 Windows 11의 실제 게임 창·키 입력·설치 검증을 대체하지 않는다.
성능·JaCoCo·최종 배포는 후속 카드의 범위다. 아티팩트가 만료돼도 버전·실행 명령과 실행 기록으로 재검증할 수 있다.
