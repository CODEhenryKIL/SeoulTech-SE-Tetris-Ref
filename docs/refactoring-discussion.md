# 테트리스 리팩토링 검토·논의 문서

작성일: 2026-10-22

상태: PR10 오류·REF-01~06 로컬 구현·검증 완료(macOS) / REF-07 이후 논의 진행

이 문서를 앞으로 리팩토링 논의를 이어갈 기준으로 사용한다. 기존 [tetris-improvements.md:1](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/tetris-improvements.md:1>)와 10월 20일 계획 초안은 참고 자료로 두고 원문을 변경하지 않는다. 목표는 **중복 코드 삭제와 가독성 개선을 우선하고, 1차 기능을 유지하면서 2차 요구사항에 필요한 경계를 정리하는 것**이다.

## 1. 분석 작성 당시 상태와 범위

| 항목 | 확인 내용 |
|---|---|
| 분석 대상 | 게임 소스 16개, 회귀 테스트 파일 1개, 기존 개선 메모·계획 초안 |
| 현재 브랜치 / HEAD | `refactor/code-cleanup` / `5571b43` |
| 로컬에 저장된 main 기준 | `e6f2d5c` — PR #10 병합 커밋 |
| 기존 오류 수정 후보 | `4eabe8d` — 현재 작업 브랜치에 이미 포함된 세 오류 수정 |
| 테스트 상태 | `tests/RefactoringRegressionTest.java`는 이전 요청에서 생성한 독립 실행 검사 파일. 후속 완료 커밋에 포함 |
| 이번에 작성하는 산출물 | 이 문서 한 개. 소스·테스트·설정·점수 파일은 변경하지 않음 |
| 이번에 하지 않는 일 | 코드 수정·테스트 실행·설치·브랜치 전환·커밋·push·PR 생성·원격 머지 |

`origin/main`은 마지막 fetch 때 저장된 로컬 참조다. 이번에는 fetch나 원격 조회를 하지 않았으므로, **현재 실제 원격 main의 최신 상태까지 확인했다는 뜻은 아니다.**

표기의 의미는 다음과 같다.

- **확인 사실**: 현재 소스·Git 이력·저장된 검사 로그에서 직접 확인한 내용.
- **추론**: 확인 사실로 예상되는 영향. 실제 화면이나 모든 입력 경로에서 재현했다는 뜻은 아님.
- **제안**: 사용자와 상의할 변경 후보. 구현 승인이나 완료 표시가 아님.
- **미확인**: 현재 자료만으로 판단할 수 없는 내용.

## 2. 가장 먼저 다룰 작업: 승빈님 PR #10의 조건부 오류 수정

### 2.1 재현된 세 오류 — 로컬 해결·검증 완료 (2026-10-22)

이 세 건은 과거 검토에서 재현한 오류다. 후속 요청인 “조건부 오류 해결 먼저”에 따라 현재 브랜치에 이미 들어 있는 `4eabe8d`의 수정 내용을 확인하고, 2026-10-22 Java 21 컴파일과 회귀 테스트 16개를 다시 실행해 모두 통과했다. 기존 수정으로 해결되어 불필요한 중복 코드 수정은 하지 않았다.

| 번호 | 병합된 PR 코드에서 발생했던 문제 | 원인 | 현재 로컬 후보 수정 | 이후 확인 기준 |
|---|---|---|---|---|
| PR10-01 | 키 변경 화면에서 새 키 입력 시 예외 | 생성자가 필드와 같은 이름의 지역 배열을 선언해 실제 필드가 null | 필드 `currentKey`를 초기화 | 중복 키 거부, 새 키 입력, 저장, 다시 읽기 |
| PR10-02 | 모든 설정 초기화 후에도 사용자 키가 유지 | 기본값 목록과 현재값이 같은 가변 목록을 공유 | 설정 인스턴스가 목록을 복사해 소유 | 키·크기·색각 모드 기본값 복원 및 저장, 다른 인스턴스와 독립 |
| PR10-03 | 모르는 설정 항목이 파일에 있으면 시작 중 예외 | 존재하지 않는 Map 항목을 바로 참조 | 등록된 이름인지 확인 후 모르는 항목 건너뜀 | 알 수 없는 행 뒤의 정상 설정도 계속 읽기 |

근거: 키 배열 [Settingpanel.java:196](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:196>), [Settingpanel.java:208](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:208>); 설정 파일 처리 [Maincontainer.java:204](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:204>); 목록 복사 [Maincontainer.java:285](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:285>). Git 비교 `git diff e6f2d5c..5571b43 -- src`에서도 세 수정과 파일 끝 개행만 확인됐다.

**현재 상태**: PR10-01~03 현재 작업 브랜치에서 해결·검증 완료 / 원격 게시·머지는 실행하지 않음 / 팀 main 반영 완료로 간주하지 않음.

**처리 기록**: 기존 `4eabe8d`의 세 수정만 유지했다. 소스 16개·회귀 테스트·실제 설정/점수 파일의 내용이 검증 전후 동일함도 확인했다. 새 브랜치나 커밋은 만들지 않았으며, 나머지 리팩토링은 착수하지 않았다. 이후 게시가 필요하면 순수 리팩토링과 분리해 해당 오류 수정만 다룬다.

**새 검증 근거**: 아래 7절의 2026-10-22 컴파일·테스트 로그. 오류에 직접 해당하는 설정 검사 4개와 나머지 게임 회귀 검사 12개가 모두 통과했다.

### 2.2 PR10-04: 화면 크기 변경 후 하단 흰 여백 — 로컬 수정·검증 완료 (2026-10-22)

- **수정 계획·범위**: 실제 컴포넌트 크기와 설정 크기가 달라도 `getWidth()/getHeight()`가 실제 크기를 반환하는 검사를 먼저 추가한 뒤, 설정 크기 조회 메서드와 네 화면의 호출부만 변경한다. 화면 복귀 순서 변경은 포함하지 않는다.
- **확인된 원인**: Maincontainer가 JFrame의 `getWidth()/getHeight()`를 설정 크기로 재정의했다. 실제 GUI에서 프레임 높이 628, 반환 높이 600, 제목 표시줄 높이 28이 관찰됐다. Swing이 부모 크기로 다시 그릴 범위를 제한하면서 하단 영역이 누락될 수 있는 잘못된 크기 계약이다. 실제 확대 후 흰 띠도 28(2배율 캡처에서 56픽셀)로 일치했다.
- **비교 검증**: 같은 메뉴 처리 코드를 호출한 임시 GUI에서 원본과 화면 복귀 순서만 변경한 버전은 흰 띠가 발생했다. 크기 메서드만 변경한 버전과 두 수정을 적용한 버전은 발생하지 않았다. 크기 수정본은 최대 크기 확대와 최소 크기 축소에서도 재발하지 않았다. 따라서 기존의 복귀 순서 원인 추정은 이번 흰 띠의 직접 원인으로 채택하지 않는다.
- **적용한 수정**: 설정 크기는 `getConfiguredWidth()/getConfiguredHeight()`로 조회하고, JFrame의 실제 크기 조회는 상속된 메서드를 사용한다. 시작·게임·설정·점수판은 계속 동일한 설정 크기를 사용한다. 근거: [Maincontainer.java:134](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:134>), [Startpanel.java:20](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Startpanel.java:20>), [Gamepanel.java:46](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Gamepanel.java:46>), [Settingpanel.java:48](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:48>), [Scoreboardpanel.java:36](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Scoreboardpanel.java:36>).
- **자동 검증**: 신규 `frame-size` 검사는 수정 전 실패, 수정 후 통과했다. 세 가지 설정 크기에서 실제 AWT 크기와 게임 화면의 설정 크기가 독립적인지 확인한다. 전체 회귀 검사 **17/17 통과**, Java 21 컴파일 및 `git diff --check` 통과. 컴파일의 serial/Unsafe 관련 경고는 남아 있다.
- **검증 한계·보류**: GUI 비교는 macOS/Java 21에서 진단 메뉴로 기존 키 처리 코드를 호출했다. 물리 키 입력·포커스와 Windows는 이 자동 비교의 검증 범위 밖이다. Startpanel을 새 설정 적용 전에 만드는 기존 복귀 순서와 색상 적용 시점은 별도 논의 대상으로 남긴다.
- **근거 보관**: 저장소 밖 `artifacts/pr10-review/white-gap-20261022/`에 GUI 캡처·크기 로그·비교 diff·픽셀 측정 결과, `artifacts/pr10-review/white-gap-fix-20261022/`에 수정 전 실패·수정 후 컴파일·전체 회귀 검사 로그를 저장했다. 로컬 수정이며 원격 push는 하지 않았다.

## 3. 전체 코드 구조에서 확인한 책임과 연결

| 구성 | 현재 맡은 일 | 주요 연결·근거 |
|---|---|---|
| `Tetris` | 실행 진입, 창 생성·표시, 시작 메뉴 진입 | [Tetris.java:7](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/main/Tetris.java:7>) |
| `Maincontainer` | 프레임·화면 전환, 전역 조작키·크기·색상, 내부 Setting의 파일 읽기·검증·저장 | [Maincontainer.java:18](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:18>), [Maincontainer.java:180](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:180>) |
| `Startpanel` | 시작 메뉴 표시·선택·동작 | Maincontainer 화면 전환 호출; 버튼 선택 상태는 static 목록/인덱스. [Startpanel.java:48](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Startpanel.java:48>), [Startpanel.java:132](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Startpanel.java:132>) |
| `Settingpanel` | 크기·색각·초기화 메뉴, 키 변경 화면, 점수 파일 비우기 | Setting 내부 Map에 직접 접근; 설정·키 메뉴에 유사 버튼 구현. [Settingpanel.java:63](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:63>), [Settingpanel.java:187](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:187>) |
| `Gamepanel` | 보드·미리보기·점수 UI, 입력 전달, 일시정지·게임 오버·이름 입력, 점수 저장 | Board 생성 및 Board의 UI 콜백 대상. [Gamepanel.java:42](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Gamepanel.java:42>), [Gamepanel.java:86](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Gamepanel.java:86>), [Gamepanel.java:153](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Gamepanel.java:153>), [Gamepanel.java:221](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Gamepanel.java:221>) |
| `Board` | 격자·현재/다음 블록, 충돌·회전·낙하·줄 삭제·점수·레벨, Timer, 텍스트/ghost 그리기 | 상태 변경과 표시 데이터 갱신이 한 경로에 결합. [Board.java:35](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:35>), [Board.java:133](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:133>), [Board.java:307](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:307>), [Board.java:358](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:358>) |
| `Nextblockpanel` | 다음 블록 텍스트와 색·배율 렌더링 | Gamepanel의 getter를 거쳐 Board의 다음 블록을 조회. [Nextblockpanel.java:96](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Nextblockpanel.java:96>), [Nextblockpanel.java:108](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Nextblockpanel.java:108>) |
| `Scoreboardpanel` | 파일 읽기·파싱·정렬·상위 10개 표시·시작 화면 복귀 | [Scoreboardpanel.java:72](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Scoreboardpanel.java:72>), [Scoreboardpanel.java:87](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Scoreboardpanel.java:87>) |
| `Block` + 7개 구체 블록 | 모양·색·가변 회전·가로/세로 길이 | `IBlock/JBlock/LBlock/OBlock/SBlock/TBlock/ZBlock`; 각 생성자에서 모양과 색 대입. [Block.java:7](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/blocks/Block.java:7>), [OBlock.java:7](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/blocks/OBlock.java:7>) |
| 회귀 테스트 | 설정·생성·이동/회전·점수·정지·기록·화면 크기·복귀·블록 초기값·메뉴의 26개 검사 | [RefactoringRegressionTest.java:55](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/tests/RefactoringRegressionTest.java:55>) |

**확인된 연결**: Gamepanel은 Board를 소유하며 조작 메서드를 호출한다. Board도 Gamepanel을 참조해 점수·미리보기 갱신, 정지·재개·게임 종료 UI를 호출한다. 설정 파일은 Maincontainer 내부에서 처리하지만 점수 파일은 Gamepanel의 쓰기, Scoreboardpanel의 읽기, Settingpanel의 초기화로 나뉜다.

**해석 / 확신 높음**: 단순 중복 삭제가 가능한 부분과, 상태/화면의 경계를 바꿔야 하는 부분이 섞여 있다. 문서의 앞쪽 후보는 작은 정리를, 뒤쪽 후보는 2차 요구사항 때문에 필요한 구조 분리를 다룬다. 클래스 길이만으로 재작성을 결정하지 않는다.

## 4. 기존 개선 메모를 현재 코드와 대조한 결과

기존 메모 원문은 수정하지 않고 아래 판정을 적용한다.

| 기존 항목 | 현재 판정 | 현재 코드 기준으로 바뀌어야 할 설명 |
|---|---|---|
| 새 블록 시작 위치 가운데 계산 | **수정 방향 합의·구현 전** | O블록의 시작 x가 3에서 4로 바뀌는 동작 변경을 포함한다. 초기·일반 고정 후·하드드롭 후 생성과 게임 오버 판정에 같은 계산을 적용하고 검증 |
| Block 생성자의 중복 초기화 제거 | **REF-02 구현·검증 완료(macOS)** | 부모 기본 생성자 8줄 삭제. 7종 하위 생성자 유지, 초기 모양·색 고정 검사 포함 19/19 통과 |
| 세 설정 setter의 입력 검증 | **기존 API 설명 폐기·분석 대상 개정** | 해당 setter는 삭제됨. 현재 `changeSetting()/modifySetting()`, UI와 파일 로드의 검증 책임을 검토 |
| 중복 창 표시 호출 제거 | **REF-01 구현·검증 완료(macOS)** | 진입점의 중복 표시 호출을 제거하고 `applySetting()`의 표시는 유지. 최초 실행과 키 입력 확인 |
| 시작 화면 복귀 중복 제거 | **REF-01 구현·검증 완료(macOS)** | 화면 정리·설정 적용 후 `enterStart()` 재사용. 복귀·첫 입력·크기·색상·이전 패널 잔존 검증 완료 |
| 하드드롭 기본 키 보충 단순화 | **관련 설정 로직 정리 대상으로 합의·세부 범위 미정** | 기존 `addDefaultHardDropKey()`는 이미 삭제됨. 실제 정리할 현재 코드와 예전 5키 파일 지원 여부는 구현 전에 확정. 삭제된 함수를 되살리는 것으로 간주하지 않음 |
| 패널 재생성/재사용 선택 | **현행 유지 합의** | root 재사용과 개별 화면 재생성을 유지. 공통 메서드 재사용과 화면 객체 재사용은 구분하며, 패널 재사용 구조로 전환하지 않음 |

**논의 현황 (2026-10-22)**: 수정 방향 합의, 코드 구현, 검증 완료를 각 행의 상태로 구분한다. 3번 설정 입력 검증의 세부 범위는 아직 논의 중이다. 모든 실제 수정은 관련 회귀 검사와 필요한 GUI 확인을 함께 진행한다.

**4번 논의 메모 — 중복 창 표시 호출 제거 (2026-10-22)**

- **수정 범위**: `Tetris.main()`의 `maincontainer.setVisible(true)` 한 줄만 삭제했다. `new Maincontainer()`가 생성자 → `applySetting()` → `setVisible(true)`를 실행하므로 창 표시는 내부에서 유지된다. `applySetting()`의 표시 호출과 최초 표시 시점은 이번 정리에서 변경하지 않는다.
- **이력과 판단**: 확인한 초기 구현 커밋 `0d94deb`부터 두 호출이 함께 있었고, 이후 내부 표시 코드가 `applySetting()`으로 이동한 뒤에도 중복이 유지됐다. 중복을 의도한 이유는 이력만으로 확정하지 않는다. 창 표시가 유지된다는 코드 분석과 실제 입력·포커스 검증은 구분한다.
- **수정 후 확인할 내용**:
  - 게임을 새로 실행했을 때 창과 START·MENU·SCORE·EXIT 메뉴가 정상적으로 표시되는지 확인한다.
  - 화면을 추가로 클릭하지 않고 첫 ↑·↓ 입력으로 선택이 이동하고, Enter로 선택한 메뉴에 진입하는지 확인한다. 중복 호출을 제거해도 초기 키보드 포커스가 유지되는지가 핵심이다.
  - 설정에서 세 창 크기를 바꾸고 저장·복귀했을 때 시작 화면 표시, 첫 키 입력, 하단 흰 여백 재발 여부를 확인한다.
- **검증 방법**: 실제 게임 창에서 위 항목을 확인한다. 기존 헤드리스 회귀 테스트 통과만으로 최초 창 표시·물리 키 입력·포커스가 검증됐다고 판단하지 않는다.
- **현재 상태**: REF-01에서 해당 한 줄과 복귀 중복 코드를 정리했다. 자동 검사와 실제 조작 결과·미확인 항목은 아래 실행 기록을 따른다.

대조 근거: 시작 위치 [Board.java:51](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:51>), [Board.java:249](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:249>), [Board.java:270](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:270>), [Board.java:341](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:341>); Block 초기화 [Block.java:10](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/blocks/Block.java:10>); 설정 변경 [Settingpanel.java:63](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:63>), [Settingpanel.java:263](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:263>); 6키 검증 [Maincontainer.java:229](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:229>); root 수명 [Maincontainer.java:42](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:42>); 표시 호출 [Maincontainer.java:131](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:131>), [Tetris.java:7](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/main/Tetris.java:7>).

## 5. 개선 후보 우선순위 — 합의된 방향과 추가 논의

PR10 오류 처리 후 아래 순서로 하나씩 상의하는 안이다. 번호는 이 문서의 식별자이며 기존 팀 작업 카드 번호가 아니다.

4절에서 이미 논의한 항목은 아래 후보에 연결해 한 번만 구현한다. REF-01의 창 표시·시작 화면 복귀 정리는 아래 실행 기록에 따라 구현·검증을 마쳤으며, REF-02~REF-06의 구현·자동 검사와 macOS 실제 조작을 마쳤으며 검증 범위는 아래 실행 기록을 따른다. 4절 6번의 설정 처리 중복은 REF-04의 승인 범위에서 정리했다.

| 순서 | 번호 | 개선 후보 | 예상 범위·위험 | 상태 |
|---|---|---|---|---|
| 1 | REF-01 | 중복 창 표시와 시작 화면 복귀 흐름 정리 | 작음~중간; 크기·포커스 순서 주의 | 구현·검증 완료(macOS) |
| 2 | REF-02 | Block 기본 초기화 중복 삭제 | 작음; 초기화 누락 방지 필요 | 구현·검증 완료(macOS) |
| 3 | REF-03 | 메뉴 선택 상태 소유와 유사 코드 정리 | 중간; 생성·복귀 시 선택 상태 주의 | 구현·검증 완료(macOS) |
| 4 | REF-04 | 설정 파싱·검증·변경 경계 정리 | 중간; 파일 호환·현재값 유지 필요 | 구현·검증 완료(macOS) |
| 5 | REF-05 | Board의 중복된 고정·승계·그리기 흐름 정리 | 중간~큼; 호출 순서가 게임 결과에 영향 | 구현·검증 완료(macOS), 로컬 커밋 |
| 6 | REF-06 | 블록 생성 책임 분리 | 중간; 2차 난이도·아이템의 확장 지점 | 구현·검증 완료(macOS), 로컬 커밋 |
| 7 | REF-07 | 게임 규칙과 Swing 표시·시간 처리 경계 분리 | 큼; 필요한 규칙부터 좁게 분리 | 논의 대기 |
| 8 | REF-08 | 점수 파일 읽기·쓰기·초기화 규칙 한곳에서 관리 | 중간; 기존 기록 형식 유지 | 논의 대기 |
| 9 | REF-09 | 미리보기의 간접 의존과 렌더링 중복 검토 | 중간~큼; 큰 공통 렌더러는 후순위 | 논의 대기 |
| 10 | REF-10 | 수정 영역의 잘못된 주석·미사용 데이터·이름 정리 | 작음; 일괄 이름 변경은 제외 | 논의 대기 |

### REF-01. 중복 표시·화면 복귀

PR10-04는 실제 창 크기 조회 재정의를 제거해 해결했다(2.2절). 여기서는 4절 4·5번에서 논의한 중복 표시·복귀 코드 정리를 다루며, 설정 적용과 화면 생성 순서의 영향은 별도로 검증한다.

**실행 계획 (2026-10-22, `refactor/start-screen-flow`)**

1. 기존 회귀 검사와 화면 왕복 시 이전 패널 제거·시작 화면 재생성을 확인하는 검사를 수정 전에 실행한다. 실제 창 표시·포커스·페인트는 GUI에서 별도로 확인한다.
2. `Tetris.main()`의 중복 표시 호출 한 줄을 제거한다. `Maincontainer`의 최초 표시 동작은 유지한다.
3. 게임·점수판 복귀는 각 이전 패널을 정리한 뒤 기존 `enterStart()`를 호출한다. 게임 패널의 null·부모 확인 조건은 보존한다.
4. 설정 복귀는 설정 패널 제거 → 설정 적용 → `enterStart()` 순서로 바꾼다. root 재사용과 개별 화면 재생성을 유지하며, `enterStart()`의 CardLayout 표시 경로를 사용한다. 기존 Z순서 변경과 주석 처리된 카드 표시는 이 공통 경로로 대체한다.
5. 전체 회귀 검사·Java 컴파일·diff 검사를 실행하고, 실제 키 입력으로 첫 실행, 게임·점수판·설정 왕복, 세 크기, 색상 변경, 이전 패널 잔존과 흰 여백을 확인한다. 키 입력·포커스를 진단용 핸들러 호출로 대체해 검증 완료라고 기록하지 않는다.

**범위·검토 구분**: 구현 대상은 `Tetris.java`, `Maincontainer.java`, 관련 회귀 테스트와 이 문서다. REF-02 이후 항목, 설정 파일 호환 정책, 메뉴 상태 구조는 변경하지 않는다. 계획 검토와 구현 후 코드 검토를 별도 단계로 진행한다. 기존 게임 패널 정리 조건은 정상적인 방어 조건으로 유지하며, 오류를 숨기는 새 기본값·예외 무시 경로는 추가하지 않는다.

**실행 기록 (2026-10-22)**

- **브랜치·PR 구분**: 기존 버그 수정은 `refactor/code-cleanup`, 리팩토링은 `refactor/start-screen-flow`에서 관리한다. REF별 커밋을 유지하고 관련 REF 2~3개를 하나의 PR로 묶는다. 첫 묶음은 관련 메뉴 정리를 포함한 REF-01~03으로 관리한다. 이 브랜치는 버그 수정 커밋 `2745dc8`에서 분기했으므로, 버그 PR을 먼저 main에 반영한 뒤 리팩토링 PR의 비교 범위를 확인한다.

- **코드 변경**: `Tetris.main()`의 중복 표시를 제거했다. 게임·점수판은 기존 패널 제거 후 `enterStart()`를 호출한다. 설정 복귀는 기존 root 분리·재부착을 보존한 채 설정 패널 제거 → root 분리 → 설정 적용 → `enterStart()` 순서로 변경했다. 새 Startpanel이 변경된 설정 크기·색을 읽고 공통 CardLayout 경로로 표시된다.
- **회귀 검사**: `start-return` 검사를 추가해 게임·점수판 왕복 3회, 이전 패널 제거, 새 시작 화면 생성, START 선택 초기화를 확인한다. 수정 전·후 전체 **18/18 통과**. Java 21 전체 컴파일과 `git diff --check` 통과. serial/Unsafe 관련 컴파일 경고는 남아 있다.
- **독립 검토**: 계획 검토 후 구현 diff를 별도로 검토했으며 수정 요청은 없었다.
- **실제 키 조작으로 확인한 항목**: Finder에서 일반 실행한 테스트 앱에서 시작 메뉴가 표시된 뒤 추가 화면 클릭 없이 ↓·Enter로 설정 진입. `600X500 → 750X625 → 900X750 → 600X500` 크기 변경과 Protanopia·Deuteranopia·Tritanopia 저장·복귀. 설정·점수판 복귀 후 첫 키 입력. 게임 진입 후 좌우 이동·회전·아래 이동·하드드롭·일시정지. 세 크기에서 복귀 직후 하단 흰 띠 0픽셀, 로그의 root 자식 1개 및 시작 화면 배경색과 적용 팔레트 일치 확인.
- **추가 실사용 확인 완료**: 실제 P 입력으로 일시정지 → 재개(PAUSED 표시 제거) → 다시 일시정지 → Enter로 시작 화면 복귀. 추가 클릭 없이 첫 ↓·Enter로 설정에 진입하고, 저장·복귀 후 첫 Enter로 새 게임 시작을 확인했다. 하드드롭 11회로 게임 오버(203점)를 만들고 이름 `1` 저장 → 시작 화면 복귀 → 첫 ↓·↓·Enter로 점수판 진입 및 `1: 203` 기록 확인. 점수판 복귀 후 첫 Enter로 새 게임 점수 0 초기화, 다시 일시정지·종료 후 START 선택과 root 자식 1개 유지도 확인했다.
- **검증 한계·도구 상태**: macOS의 실제 키 입력으로 위 경로를 검증했으며, 중간에 자동 조작 도구의 포커스·응답 문제가 발생해 사용자가 테스트 창을 활성화한 뒤 이어서 확인했다. 이 도구 문제를 게임 입력 오류로 판단하지 않는다. 진단 메뉴의 핸들러 직접 호출로 실사용 검증을 대체하지 않았다. Windows 실행은 미검증이다.
- **데이터·근거**: 실제 저장소의 `setting.txt`·`tetris_score.txt`는 검증 전후 동일하다. GUI는 임시 디렉터리의 테스트 데이터로 실행했다. 저장소 밖 `artifacts/ref01/`에 수정 전후 로그, 실제 키 입력·포커스 로그, 화면 캡처와 픽셀 측정 결과를 보관한다. 원격 push·PR 생성은 별도 단계로 진행한다.


- **확인 사실 / 확신 높음**: 시작 화면 생성·추가·revalidate/repaint·카드 전환·포커스 요청이 `enterStart()`, 게임 복귀, 점수판 복귀에 반복된다. `setVisible(true)`도 두 곳에 있다. [Maincontainer.java:47](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:47>), [Maincontainer.java:65](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:65>), [Maincontainer.java:106](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:106>), [Maincontainer.java:131](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:131>), [Tetris.java:7](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/main/Tetris.java:7>).
- **제안**: 우선 동일한 복귀 흐름이 `enterStart()`를 재사용할 수 있는지 검토한다. 설정 저장 후 복귀는 별도 전처리가 있으므로 그대로 같은 코드로 대체하지 않는다. 창을 보여주는 소유자를 한 곳으로 정한다.
- **유지 조건**: 새 게임 초기화, 점수판 최신 기록 읽기, 메뉴 선택 초기화, 첫 키 입력, 설정 적용 시점.
- **검증**: 시작↔게임·설정·점수판 왕복, 세 크기, 색각 모드 변경. 현재 자동 검사만으로 GUI 정상 여부를 판정하지 않는다.
- **상의할 사항**: 단순 중복 제거만 먼저 할지, PR10-04에서 확인한 전환 순서 문제까지 별도 수정으로 이어갈지.

### REF-02. Block 중복 기본값

**실행 계획 (2026-10-22, `refactor/start-screen-flow`)**

1. 기존 7종 생성자의 초기 모양 전체와 색을 고정 기대값으로 검증하는 `block-initialization` 검사를 추가하고, 생산 코드 수정 전에 전체 회귀 검사를 실행한다.
2. `Block`의 명시적 기본 생성자만 삭제한다. `shape`·`color` 필드, 공통 회전·조회 메서드, 7개 하위 생성자는 그대로 유지한다. 확인한 모든 하위 생성자가 자기 모양·색을 대입하며, 부모 생성자에서는 다른 동작을 수행하지 않는다.
3. 전체 Java 21 컴파일·회귀 검사·diff 검사와 별도 코드 검토를 진행한다. 실제 게임에서 현재·다음 블록 표시와 이동·회전·하드드롭을 확인한다. 자동 검사의 고정 기대값으로 7종 전체를 확인하고, GUI에서 우연히 나온 블록만으로 전체 검증을 대신하지 않는다.
4. 새 하위 블록도 자기 모양·색을 설정하고 회귀 검사에 기대값을 추가해야 함을 기록한다. 필수 생성자 인수 도입 등 초기화 구조 변경은 이번 범위에 포함하지 않는다.

**범위·중복 분류**: 수정 파일은 `Block.java`, 기존 회귀 테스트, 이 문서다. 부모의 O모양·노란색 기본값은 현재 7종에서 모두 덮어쓰는 중복 초기화다. 이 기본값에 의존하는 추가 하위 클래스·익명 하위 클래스는 저장소에서 확인되지 않았다. 의존성 추가나 새 예외 무시·기본값 경로는 만들지 않는다. 계획 검토와 구현 후 검토는 별도로 수행한다.

**실행 기록 (2026-10-22)**

- **코드 변경**: `Block.java`의 명시적 기본 생성자 8줄만 삭제했다. 모양·색 필드와 공통 조회·회전 메서드, 7종 하위 생성자는 유지했다. 컴파일러가 제공하는 공개 기본 생성자로 기존 생성 호출이 유지된다.
- **수정 전후 검증**: 7종 전체 초기 행렬·정확한 색을 고정하는 `block-initialization`을 추가했다. 생산 코드 변경 전·후 모두 **19/19 통과**. Java 21 `--release 21 -Xlint:all` 전체 컴파일 성공, 기존 serial/Unsafe 등의 경고는 남아 있다. 별도 빌드 도구·lint·LSP 구성은 없으며 컴파일 진단과 diff 검사를 수행했다.
- **추가 근거·독립 검토**: `javap -p` 기준 `Block()` 포함 API 목록이 전후 동일하고 7종 하위 클래스의 `.class` 파일도 전후 바이트가 동일하다. 계획과 구현을 별도로 검토했으며 수정 요청은 없었다. 의존성·새 추상화·예외 무시 경로는 추가하지 않았다.
- **실사용 후속 확인 완료**: 최초 도구 연결 당시에는 입력이 수신되지 않아 미검증으로 기록했으나, 후속 요청에서 현재 코드(REF-03 포함)를 AppleScript로 실제 조작했다. 현재 S블록·다음 O블록 표시, 좌우 이동, 변경한 W키 회전, B키 하드드롭 후 현재 O블록·다음 I블록 승계를 화면과 입력 로그로 확인했다. 네 번 회전한 뒤 보드 캡처의 픽셀이 처음과 같고, 하드드롭 뒤 고정 블록·점수 증가를 확인했다. 이로써 계획한 macOS 실제 조작 확인을 마쳤다. 7종 전체 초기값은 자동 검사로 고정하며, 모든 종류를 실제 화면에서 관찰했다는 의미는 아니다. Windows 실행은 미검증이다.
- **데이터·근거**: 실제 저장소의 `setting.txt`·`tetris_score.txt`는 전후 바이트가 동일하다. 테스트 앱은 별도 임시 파일을 사용했다. 저장소 밖 `artifacts/ref02/`에 수정 전후 컴파일·19개 검사 로그, API 비교, 하위 클래스·데이터 무변경 확인과 GUI 입력 로그를 보관한다. REF-02도 독립 커밋으로 관리하며, 원격 push·PR 생성은 별도로 진행한다.

- **구현 전 확인 사실 / 확신 높음**: 추상 Block의 생성자는 O모양/노란색을 넣고, OBlock을 포함한 7종 생성자는 자기 모양/색을 다시 넣었다. [Block.java:10](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/blocks/Block.java:10>), [OBlock.java:7](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/blocks/OBlock.java:7>), [IBlock.java:7](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/blocks/IBlock.java:7>).
- **제안**: 현재 7종이 반드시 모양·색을 채운다는 조건 아래 사용되지 않는 기본 초기화를 삭제하는 작은 변경을 우선 검토한다. 새 블록의 초기화 누락까지 막을 필요가 생기면 필수 생성자 인수 등 더 강한 계약을 별도 검토한다.
- **위험·검증**: 미래 하위 클래스도 모양·색을 직접 초기화해야 한다. 새 블록을 추가할 때 `block-initialization`에 초기 행렬·색의 기대값을 추가한다. 현재 7종의 초기값은 새 검사로 고정하며, 기존 회전 검사로 네 칸 유지·색 유지·네 번 회전 후 복원을 확인한다.
- **상의할 사항**: 이번에는 중복 삭제만 할지, 초기화 계약 보강까지 묶을지.

### REF-03. 메뉴 상태와 유사 버튼 코드

**실행 계획 (2026-10-22, `refactor/start-screen-flow`)**

1. ↑·↓ 순환·선택 강조, 시작 메뉴의 MENU/SCORE 진입, 키 설정 왕복 후 상위 선택 유지·리스너 1개 복원, 키 입력 대기 중 예약·중복 키 처리를 검사한다. 같은 종류의 화면 두 개를 생성해도 서로의 선택·목록이 바뀌지 않아야 한다는 검사를 추가한다. 기존 동작 검사는 수정 전에 통과시키고, 인스턴스 간섭 검사의 기존 실패는 별도 기록한다.
2. 작은 package-private `MenuButtons`에 목록·선택 번호·등록·순환·강조 처리만 모으고, 시작·설정·키 변경 화면이 각각 별도 객체를 소유하게 한다. 목록 길이로 개수를 계산하며 static 목록·선택 번호·reset 호출을 없앤다. 시작 화면의 간격(2px), 설정 화면의 간격(0px), 기존 등록 상한의 실제 동작(최대 11개)은 유지한다. 상한 정책 변경은 별도 논의 대상으로 둔다.
3. 키 변경 메뉴는 표시 코드가 동일한 기존 `OptionButton`을 재사용한다. Enter의 실행 내용은 각 화면에 유지하며, 키 변경 대상 번호는 해당 키 메뉴의 선택 번호에서 가져온다. 키 입력 대기 동안 방향키가 메뉴 이동으로 처리되지 않도록 현재 리스너 전환을 보존한다.
4. 시작 화면은 재진입 시 START, 설정 화면은 새로 생성하면 첫 항목, 키 변경 창은 열 때 첫 항목을 선택한다. 키 변경 창에서 저장 후 돌아오면 상위 Change Keys 선택은 유지한다. root 재사용·화면 재생성 정책, 키 파싱·검증 정책, 게임 규칙은 변경하지 않는다.
5. 전체 Java 21 컴파일·회귀 검사·diff 검사 및 독립 코드 검토를 진행한다. 세 크기의 시작·설정·키 변경 화면을 수정 전후 같은 조건으로 오프스크린 렌더링해 비교한다. 이 비교와 핸들러 기반 검사를 OS의 실제 포커스·키 조작 검증과 구분한다.

**범위·검토 구분**: `Startpanel.java`, `Settingpanel.java`, 작은 공통 메뉴 클래스 1개, 기존 회귀 테스트와 이 문서만 수정한다. 기존 등록 상한의 조용한 반환은 현재 동작 보존을 위해 그대로 한곳으로 옮기며, 새 기본값·예외 무시 경로를 추가하지 않는다. 계획 검토와 구현 후 검토는 별도로 수행한다.

**실행 기록 (2026-10-22)**

- **변경**: `Startpanel`·`Settingpanel`·`ChangeKeyPanel`의 static 목록·선택·개수 및 reset 코드를 제거했다. 새 `MenuButtons`는 세 화면에서 실제로 공통 사용하며, 키 메뉴는 기존 `OptionButton`을 재사용한다. `MenuButtons` 외 새 생산 클래스나 외부 의존성은 추가하지 않았다.
- **검사 전후**: 메뉴 순환·강조·Enter 실행, 키 변경 3회 왕복, 입력 모드 전환·예약/중복 키·하드드롭 저장, 등록 상한, 세 인스턴스 독립성 등 **7개 검사**를 추가했다. 생산 코드 수정 전 **23/26 통과**이며 실패 3개는 의도한 인스턴스 간섭 재현뿐이었다. 수정 후 **26/26 통과**. 상한 검사의 내부 등록 어댑터만 static 메서드에서 인스턴스 메뉴로 맞췄고 기대값은 유지했다.
- **표시 유지**: 동일 Java 21·Metal L&F·EDT에서 `600X500`, `750X625`, `900X750`의 시작·설정·키 변경 메뉴 8상태씩 총 **24개 PNG**와 bounds·preferredSize·opacity 정보를 비교했다. 이미지 24개와 배치 파일 24개 모두 전후 바이트가 동일하다. OS 창 캡처나 실제 키 입력의 증거로 간주하지 않는다.
- **품질 검사**: Java 21 `--release 21 -Xlint:all` 전체 컴파일·회귀 검사·`git diff --check` 통과. serial/Unsafe/this-escape 및 메뉴 필드의 직렬화 관련 컴파일 경고는 남아 있다. 메뉴 상태의 static 선언·reset·삭제한 버튼 클래스 잔존이 없음을 검색했다. 계획과 구현 diff를 별도로 검토했고 구현 검토는 수정 요청 없이 승인됐다. 검토자가 독립적으로 재실행한 검사도 26/26 통과했다.
- **실제 조작 경로**: 사용자 요청으로 현재 생산 소스를 다시 컴파일하고 실제 `Tetris.main()`을 실행하는 `TetrisRef03Test` 앱을 만들었다. 임시 데이터와 읽기 전용 입력 관찰 로그만 추가했으며 게임 동작을 직접 호출하는 진단 버튼은 없다. 기존 조작 도구는 앱 접근을 승인하지 않았고 사용자에게 허용 요청도 보이지 않았다. AppleScript 대체 방식을 안내한 뒤 사용자의 재검증 지시에 따라 macOS 실제 키 이벤트를 보냈다. 매 입력 전 대상 앱이 전면인지 확인했으며, AWT 입력 수신·화면 상태 로그와 OS 창 캡처로 결과를 검증했다.
- **실제 조작 결과**: **50단계 통과**. 시작·설정·키 변경 메뉴의 위/아래 순환, 예약·중복 키 거부, Rotate→W·Hard Drop→B 변경, 키 메뉴 저장·3회 재진입과 상위 선택 유지·리스너 1개 복원, 설정 저장·점수판 왕복·복귀 직후 첫 입력을 확인했다. 변경한 키로 좌우 이동·회전·하드드롭·일시정지·재개·종료를 실행했다. EXIT로 프로세스를 종료한 뒤 새 JVM에서 키 설정 유지도 확인했다. `600X500 → 750X625 → 900X750 → 600X500` 변경·복귀·첫 입력과 root 자식 1개 유지도 모두 통과했다.
- **네이티브 화면 확인**: 실제 캡처에서 W 한 번 회전 후 보드 픽셀이 달라지고 네 번 후 처음과 일치함을 확인했다. 변경된 키 설정 화면, 게임·미리보기·점수 화면 및 세 크기의 시작 화면을 보관했다. 저장 시 포커스가 옛 패널에서 사라져 Enter의 release 로그가 누락된 경우는 다음 실제 방향키로 복귀한 화면과 선택·리스너 상태를 확인했다. 종료 시 자동화 호출이 지연된 경우도 실제 프로세스 종료와 새 시작 로그로 재실행을 확인했다.
- **데이터·한계**: 실제 `setting.txt`·`tetris_score.txt`는 전후 동일하다. `artifacts/ref03/`의 `native-menu-checks.json`, `native-session-1.log`, `native-session-2.log`, `native-screenshots/`, `native-rotation-comparison.json`에 실제 조작 근거를 보관한다. 자동 검사 26/26, 오프스크린 24상태 비교와 실제 조작 검증을 구분한다. Windows 실행만 미검증이며 push·PR은 별도로 진행한다.

- **현재 구조 / 확신 높음**: 시작·설정·키 변경 화면이 각각 자기 `MenuButtons`를 소유한다. 목록·선택·등록·순환·강조 처리는 공통으로 사용하며, 키 변경 화면의 중복 버튼 클래스를 제거하고 `OptionButton`을 재사용한다. [MenuButtons.java:9](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/MenuButtons.java:9>), [Startpanel.java:17](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Startpanel.java:17>), [Settingpanel.java:42](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:42>), [Settingpanel.java:193](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:193>).
- **기존 문제 재현**: 수정 전 같은 종류의 화면 두 개를 생성하면 첫 화면의 입력이 다른 화면의 버튼 목록·선택 상태에 작용하는 현상을 세 독립 검사에서 재현했다. 일반 게임에서 화면을 하나씩 사용하는 경로 전체의 사용자 오류로 재현했다는 의미는 아니다.
- **적용 범위**: 선택 목록·인덱스를 화면별로 분리하고 동일한 등록·순환·강조 및 키 메뉴 표시 중복을 정리했다. Enter 실행 내용과 키 입력 대기의 예약·중복 검증은 각 화면의 기존 코드를 유지했다.
- **위험·검증**: START로 초기화할지, 이전 선택을 유지할지가 달라질 수 있다. 위/아래 순환, Enter 동작, 여러 번 진입·복귀, 리스너 중복, 서로 다른 인스턴스 간 간섭을 확인한다.
- **후속 논의**: 최대 버튼 수 상한 정책은 2차 메뉴 수가 정해질 때 별도로 검토한다. 이번에는 기존 11개 수용·12번째 미등록 동작을 유지했다.

### REF-04. 설정 경계와 문자열 반복

- **수정 전 확인**: 같은 키 문자열을 `Maincontainer.applySetting`과 `ChangeKeyPanel` 생성자에서 각각 분해하고, 키 메뉴에서 다시 조립했다. UI도 `Setting` 내부 Map에 직접 접근했다. 이번 구현에서 문자열 처리를 `Setting`으로 모으고 Map을 private으로 좁혔다.
- **유지한 규칙**: 파일 전체 키 검증과 UI의 개별 키 입력 검증은 목적이 달라 구분한다. 공통 파서는 여섯 키 이름·순서와 숫자를 읽고, 파일 검증은 양수·중복·PAUSE Enter 금지를 추가 확인한다. UI의 입력 처리와 경고 문구는 유지하며, `updateKeyCodes`는 기존 UI 편집값의 문자열 변환만 담당한다. 크기·색상은 기존 허용 목록으로 순환한다.
- **합의·구현**: 키 파싱·조립 공통화와 의미 있는 설정 조회·변경 함수까지만 진행한다. `Setting`의 별도 파일 분리나 전체 설정의 새 타입 도입은 이번 범위에 포함하지 않는다.
- **위험·검증**: 기본값, 예약 키·중복 키, 정상/손상 파일 처리, 현재 5키 파일 거절 정책, 저장 후 재로드를 보존한다. PR10 세 오류와 창 크기 변경 후 흰 여백 회귀를 다시 확인한다.
- **상태**: 사용자 구현·검증 지시에 따라 아래 계획을 실행했다. REF-01~03의 마지막 커밋을 기준으로 분기한 `refactor/settings-boundary`에서 별도 커밋으로 관리하며 push·PR은 후속 진행한다.

#### REF-04 실행 계획 (2026-10-22)

- **승인 범위**: `Maincontainer.java` 내부 `Setting`에 키 배열 조회·문자열 저장을 모으고, `applySetting`과 `Settingpanel`의 직접 파싱·조립을 제거한다. 창 크기·색상도 의미 있는 조회·순환 함수로 접근한다. `Setting`의 별도 파일 분리는 하지 않는다.
- **순서**: 기존 동작 회귀 검사를 먼저 보강·실행한다 → 키 문자열 중복을 공통화한다 → UI의 내부 Map 접근을 제거한다 → 전체 회귀·컴파일 진단·렌더 비교·macOS 실제 조작을 검증한다 → 독립 검토한다.
- **보존할 경계**: 키 배열은 화면별 편집 복사본이다. 키 서브메뉴 저장은 메모리 설정만 변경하며 상위 메뉴 저장에서 파일을 쓴다. 창 크기·색상은 현재 순서로 순환한다. 키 파일 원문과 여섯 키의 순서, 숫자 허용 규칙, UI의 중복·예약 키 메시지는 유지한다.
- **기존 실패 처리 분류**: 알 수 없는 항목·손상된 값은 무시하고 직전 정상값 또는 기본값을 유지하는 파일 경계의 방어 동작이다. 정상·손상 경로를 회귀 검사로 고정하고 유지한다. 5키 파일은 현재처럼 거절하며 자동 이관 기능을 추가하지 않는다. 파일 I/O 예외의 출력 정책도 유지한다.
- **대상 파일**: `src/seoultech/se/tetris/component/Maincontainer.java`, `src/seoultech/se/tetris/component/Settingpanel.java`, `tests/RefactoringRegressionTest.java`, 이 문서. 의존성·게임 규칙·레이아웃 변경은 없다.
- **진행 상태**: 아래 구현·검증 완료. 독립 사전 검토 CLEAR 후 실행했으며 구현 검토의 발견 이슈는 0개다.

#### REF-04 구현·검증 결과 (2026-10-22)

- **코드 변경**: `Setting.getKeyCodes()`가 공통 파서로 새 편집 배열을 반환하고 `updateKeyCodes()`가 여섯 키의 이름·순서에 따라 문자열을 만든다. 파일 검증도 같은 파서를 사용한다. `applySetting`과 키 화면의 중복 분해·조립을 제거했다. 창 크기·색상은 getter와 next 함수로 조회·순환하며 production의 `currentSettingMap` 접근은 `Setting` 안에만 남는다.
- **수정 전 기준선**: 기존 26검사에 파일 허용·거절, 원문 유지, 편집/메모리/파일 저장 시점, 창 크기·색상 순환, 누락·손상·공백 처리를 보강하여 코드 변경 전에 31/31 통과했다. 5키, 항목 순서/이름, 빈 값, 숫자 변환 실패·범위 초과, 0·음수, 중복, PAUSE Enter 거절과 HARD_DROP Enter·`+0038` 허용도 현재 동작으로 고정했다.
- **수정 후 자동 검사**: 새 API의 반환·전달 배열 소유권 검사를 추가하여 32/32 통과했다. Java 21 `--release 21 -Xlint:all` 컴파일 오류 0개. 기존 serial·Unsafe·this-escape·deprecation 경고 39개는 전후 동일하다. 별도 lint/LSP/보안 분석 구성은 없어 컴파일러 진단과 diff 검사를 사용했다.
- **화면 비교**: 동일 JDK·Metal L&F·EDT에서 창 크기 3종 × 메뉴 상태 8종, 총 24개 PNG와 24개 배치 기록이 수정 전후 48/48 byte-identical이다.
- **macOS 실제 조작**: 별도 빌드에서 실제 `Tetris.main()`을 실행하고 AppleScript의 OS 키 입력·전면 앱 확인, 수동 입력 관찰 로그와 화면 캡처로 40단계 검증을 마쳤다. 창 크기 3종·색상 4종 순환, 키 편집 중 값 분리, 키 메뉴 저장 후 메모리만 반영, 상위 메뉴 저장 후 파일 반영을 확인했다. W 회전·B 하드드롭·일시정지·시작 화면 복귀, 게임 종료 후 새 프로세스에서 키·크기·색상 유지, 기본값 초기화와 저장을 확인했다. 네 번 W 회전 후 보드 픽셀은 처음과 같고, B 하드드롭은 점수 36 증가·다음 블록 승계를 확인했다. 세 크기에서 시작 화면의 첫 입력·패널 1개 유지와 하단 흰 여백 없음도 확인했다.
- **조작 관찰 보정**: 최초 게임 렌더 중 로그 지연은 상태 대기로 보완했다. 화면 제거로 Enter의 KEY_RELEASED 관찰이 누락된 경우는 실제 화면과 다음 OS 방향키 입력으로 전환·포커스를 확인했다. 종료 스크립트 조건과 재실행의 이전 로그 읽기 문제도 검증 도구에서 수정했다. 이 과정에서 제품 코드 변경은 없었다.
- **독립 검토**: 별도 검토자가 컴파일·32검사와 48파일 렌더 비교를 재확인했으며 발견 이슈 0개다. LSP 도구 부재로 형식 판정은 COMMENT, 코드 기준 승인 차단 사유 없음으로 보고했다.
- **데이터·근거·한계**: 실제 저장소의 `setting.txt`·`tetris_score.txt`는 전후 동일하다. 테스트 전용 폴더에서만 값을 저장했고 GUI 빌드의 소스 해시는 현재 production 소스와 모두 일치한다. `artifacts/ref04/`에 컴파일·회귀·렌더 비교, `native-menu-checks.json`, 두 세션 입력 로그, `native-screenshots/`, 회전·창 크기 비교와 `verification-summary.json`을 보관한다. Windows 실행은 미검증이다. 원격 게시는 별도로 진행한다.


### REF-05. Board의 반복된 이동·고정 흐름

- **확인 사실 / 확신 높음**: 좌우 이동·회전은 현재 블록 지우기→판정→다시 놓기를 반복한다. 일반 낙하와 하드드롭에는 줄 삭제→게임 오버 판정→next 승계→새 next 생성→미리보기 갱신→시작 좌표 설정이 반복된다. [Board.java:234](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:234>), [Board.java:256](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:256>), [Board.java:276](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:276>).
- **제안**: 먼저 차이를 표로 확인한 뒤 동일한 '고정 후 다음 블록 진행' 부분만 공통화할 수 있는지 검토한다. 하드드롭과 일반 낙하의 점수·반환값·그리기 차이는 유지한다.
- **위험**: `placeBlock/eraseCurr`는 실제 격자와 표시용 textChunks를 함께 바꾼다. 보기 좋은 공통 함수로 묶으면서 호출 순서를 바꾸면 중첩·ghost·게임 오버 후 추가 배치가 달라질 수 있다. [Board.java:133](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:133>).
- **검증**: 벽/고정 블록 충돌, 막힌 회전, 낙하 점수, 줄 삭제·레벨 경계, 일반 고정과 하드드롭 각각의 next 승계, 게임 오버 시 즉시 중단, ghost 표시.
- **상의할 사항**: 공통화 범위를 고정/승계부터 시작할지. 이동·그리기까지 한꺼번에 묶지 않는 안을 권한다.

#### REF-05 실행 계획 (2026-10-22)

- **승인 범위**: `Board.java`의 일반 낙하 막힘 경로와 하드드롭의 고정 이후 중복만 `private boolean lockAndAdvanceBlock()`으로 모은다. `Gamepanel`, 이동·회전, 점수 규칙, 생성 정책과 그리기 구현은 변경하지 않는다.
- **작업 기준**: REF-04 완료 커밋 `0277962`에서 `refactor/board-lock-flow`로 분기한다. 시작 시 fetch한 원격 main은 아직 `e6f2d5c`여서 이전 PR에 이번 작업을 추가 push하지 않는다.
- **수정 전 동작 고정**: 자동 Timer·직접 낙하·아래 방향키·직접 하드드롭·하드드롭 키 각각의 점수와 고정, next 승계, 줄 삭제/레벨, 게임 오버 중단을 검사한다. 기존 테스트에 빠진 두 낙하 경로의 통합 상태를 먼저 보강·실행한다.
- **순서 유지**: 현재 블록 배치 → 줄 삭제 → 다음 블록 생성 자리의 게임 오버 판정 → curr/next 승계·새 next 생성 → 미리보기 갱신 → 좌표 (3,0) → 새 블록 배치. 실패하면 curr/next·좌표·난수 진행을 변경하지 않는다.
- **호출부 차이 유지**: 이동 가능한 `moveDown`은 기존처럼 true, 막혀 고정되면 새 블록이 나와도 false다. 자동 낙하는 한 칸 1점, 아래 방향키는 한 칸 2점, 하드드롭은 거리×2점이다. 하드드롭 성공 후에만 Board의 마지막 drawBoard를 호출하며 Timer·Gamepanel의 추가 그리기 호출도 그대로 둔다.
- **실패 처리 분류**: 게임 오버 시 타이머 종료·즉시 반환은 충돌 뒤 새 블록 중첩을 막는 검증된 종료 경로로 유지한다. 이번 범위에는 오류를 숨기는 fallback·예외 무시를 추가하지 않는다.
- **검증**: 수정 전후 전체 회귀, 동일 시나리오의 격자·표시 청크·미리보기·난수/점수 상태와 렌더 비교, macOS 실제 조작을 구분해서 기록한다. 별도 검토자의 사전·구현 검토를 받는다. 대상은 Board, 회귀 테스트, 이 문서의 3개 파일이다.
- **상태**: 아래 구현·검증 완료(macOS). 독립 사전·구현 검토를 마쳤으며 후속 사용자 요청으로 로컬 커밋한다. push는 별도로 진행한다.

#### REF-05 구현·검증 결과 (2026-10-22)

- **코드 변경 전후**: `moveDown()`의 막힘 경로와 `hardDrop()`에 각각 있던 고정·줄 삭제·게임 오버 판정·next 승계 코드를 `private boolean lockAndAdvanceBlock()` 한 곳으로 모았다. 점수 계산은 기존 호출부에 남기고, 하드드롭의 마지막 `drawBoard()`는 공통 함수가 성공했을 때만 호출한다. 일반 낙하는 고정 후에도 false를 반환한다. 좌우 이동·회전·생성 정책·Gamepanel은 변경하지 않았다.
- **수정 전후 회귀**: 기존 32검사에 입력별 점수, 바닥/고정 블록 위 고정·승계, 줄 삭제·레벨 경계, 게임 오버 중단, 줄 삭제 후 생성 공간 확보의 5검사를 추가했다. production 수정 전 37/37, 수정 후 37/37 통과했다. 직접 호출·실제 Timer 리스너·Gamepanel 아래키·하드드롭 키 경로에서 격자·좌표·블록 참조·난수 소비·미리보기·ghost·점수·타이머·오버레이를 검사했다. 두 줄 삭제 300점, 누적 10줄의 레벨/속도 변경과 게임 오버 시 추가 생성·배치 중단도 포함한다.
- **게임 화면 비교**: 동일 JDK·Metal L&F·EDT에서 창 크기 3종 × 실제 게임 상태 8종(낙하·일반 고정·하드드롭·두 경로의 줄 삭제/레벨·두 경로의 게임 오버·줄 삭제 후 생성), 총 24개 PNG와 24개 상태 기록이 수정 전후 48/48 byte-identical이다. 메뉴 화면만 비교한 결과가 아니다.
- **macOS 실제 조작**: 현재 production 소스로 별도 앱을 빌드하여 실제 `Tetris.main()`을 실행했다. OS 키 입력과 수동 입력 관찰 로그에서 자동 낙하 한 칸 1점, 아래키 한 칸 2점, 하드드롭 16칸 32점과 다음 I블록 승계를 확인했다. 아래키로 일반 고정 후 J블록 승계, 일시정지 중 아래키·Space 무시, 반복 하드드롭 후 게임 오버 화면과 추가 이동 차단을 확인했다. 테스트 전용 점수 `5:193` 저장 후 시작 화면 복귀와 첫 방향키 반응도 확인했다.
- **조작 도구의 관찰 한계**: Computer Use의 입력이 앱에 도착하지 않아 기존에 허용한 AppleScript OS 입력 경로를 사용하고 매 입력 전 전면 앱을 확인했다. 게임 오버는 Gamepanel의 리스너를 교체하므로 JTextPane 포커스를 기대한 최초 판정은 잘못된 테스트 조건이었다. Enter로 이전 패널이 제거되면 KEY_RELEASED 로그가 빠지는 경우는 다음 실제 방향키와 저장 파일로 전환을 확인했다. 현재 입력기에서 영문 이름 REF는 입력되지 않고 숫자 5만 저장됐으며 이름 입력 자체는 이번 변경 범위가 아니다. 이 관찰 보정으로 제품 코드를 수정하지 않았다.
- **컴파일·독립 검토**: Java 21 `--release 21 -Xlint:all` 컴파일 오류 0개, 기존 경고 39개는 전후 동일하다. Board의 protected/public API와 Gamepanel.class도 전후 동일하다. 별도 검토자가 독립 컴파일·37검사·diff 검사를 통과했고 코드 발견 이슈 0개다. 문서의 진행 상태 갱신 의견은 반영했다. 별도 lint/LSP/보안 분석 구성은 없으며 LSP 도구 부재로 검토의 형식 판정은 COMMENT, 코드 승인 차단 사유 없음이다.
- **근거·한계**: `artifacts/ref05/`에 전후 컴파일·회귀 로그, 렌더 비교, API 비교, OS 입력 로그·화면과 `verification-summary.json`을 보관한다. 실제 저장소의 `setting.txt`·`tetris_score.txt`는 전후 동일하고 GUI 검증은 격리된 폴더에서 실행했다. GUI 빌드의 소스 해시는 현재 production 소스와 일치한다. Windows 실행은 미검증이며 후속 사용자 요청으로 로컬 커밋한다. 원격 게시는 별도로 진행한다.

### REF-06. 블록 생성 책임

- **분리 전 확인 사실 / 확신 높음**: REF-05 완료 시점의 Board가 Random 한 개와 7종 선택 switch를 소유했다. 생성 호출은 최초 curr/next와 고정 후 새 next 경로에 있었다. 현재 구현은 아래 실행 결과처럼 생성 책임을 옮겼다.
- **제안**: 2차 난이도·아이템을 추가할 때 바뀌는 생성 책임만 작게 분리하는 방향을 검토한다. 현재의 7종 균등 선택, 호출 순서, 미리보기 승계는 유지한다. 아이템/가중치 실제 기능은 후속 개발이다.
- **검증**: 실제 생성 경로의 7종 선택, 난수 시드 재현, 새로운 블록 인스턴스 생성, 다음 블록이 실제 curr가 되는 순서. 현재 생성 검사에는 확률 오차 검증이 포함되지 않는다.
- **합의**: 이번 정리에 생성 경계를 포함한다. 7종 선택 규칙과 생성 호출 순서는 유지하며 실제 난이도·아이템 기능은 후속 개발로 둔다.

#### REF-06 실행 계획 (2026-10-22)

- **승인 범위**: Board의 블록 생성 책임만 분리한다. `blocks/BlockGenerator.java`를 추가하고 Board의 난수 필드·7종 생성 switch를 옮긴다. Board는 생성기 한 개를 소유하고 시작 시 두 번, 정상 고정 후 한 번 `nextBlock()`을 직접 호출한다. 점수·이동·고정·렌더·생성 확률·아이템/난이도 기능은 변경하지 않는다. 대상은 Board·새 생성기·기존 회귀 테스트·이 문서의 4개 파일이다.
- **작업 기준**: REF-05 커밋 `77f2229`에서 `refactor/block-generation`으로 분기한다. 이전 PR·브랜치에 추가 push하지 않는다.
- **동작 고정 우선**: production 수정 전에 시드별 index→블록 매핑(0:I, 1:J, 2:L, 3:Z, 4:S, 5:T, 6:O), 매 생성의 새 인스턴스·초기 모양/색, 시작의 정확히 2회 난수 소비, 생성기 간 독립성을 보강한다. 성공 고정의 1회 소비·next 참조 승계와 게임 오버의 0회 소비는 REF-05 검사도 유지한다.
- **최소 구조**: public final 생성 클래스와 `nextBlock()` 하나를 사용한다. 다른 패키지인 Board에서 호출하므로 클래스·메서드는 public이다. 인터페이스·전역 생성기·공유 블록 풀·새 의존성·확률 정책 계층은 추가하지 않는다. 테스트용 시드 설정은 기존 reflection 어댑터만 조정하며 production에 별도 주입 API를 추가하지 않는다.
- **도달 불가능한 기본 반환 분류**: 기존 `nextInt(7)` 뒤의 기본 LBlock 반환은 유효한 Random에서 도달할 수 없는 묵시적 기본값이다. switch expression으로 옮기면서 default는 IllegalStateException으로 명시한다. Java Random의 0~6 경계 안에서 기존 동작은 같고, 난수 오류를 정상 블록으로 숨기는 대체 경로는 두지 않는다.
- **검토·검증 순서**: 독립 사전 검토 CLEAR → 보강 회귀 기준선 → 생성 책임 이동 → 동일 회귀·게임 렌더/상태 비교·컴파일 진단 → 독립 구현 검토·macOS 실제 조작 → 결과 기록. Windows 미검증과 도구 부재는 별도로 기록한다.
- **상태**: 아래 구현·자동 검사·macOS 실제 조작·독립 검토 완료. 후속 사용자 요청으로 로컬 커밋한다. push는 별도로 진행한다.


#### REF-06 구현·검증 결과 (2026-10-22)

- **코드 변경 전후**: Board의 Random 필드와 `getRandomBlock()`을 제거하고 `blocks.BlockGenerator`로 생성 책임을 옮겼다. Board는 생성기 한 개를 소유하며 시작의 curr/next와 정상 고정 후 새 next, 세 호출 위치에서 `nextBlock()`을 직접 사용한다. 생성기는 상태를 가진 작은 final 클래스이며 별도 인터페이스·전역 생성기·블록 재사용 풀·의존성을 추가하지 않았다.
- **동작 유지**: 기존 `nextInt(7)` 한 번과 0:I, 1:J, 2:L, 3:Z, 4:S, 5:T, 6:O 매핑, 매번 새 인스턴스 생성, 시작 2회·정상 고정 1회·게임 오버 0회 난수 소비를 유지했다. next 객체 자체를 curr로 승계하는 순서와 초기 모양·색, 점수·이동·렌더 호출도 같다. 난이도 가중치·아이템 기능은 추가하지 않았다.
- **수정 전후 회귀**: 기존 생성 검사를 4개 시드의 정확한 종류·순서·모양·색·인스턴스 독립성·난수 소비 검사로 보강하고, 시작 시 생성/게임판 간 독립성 검사 하나를 추가했다. production 수정 전 38/38, 수정 후 38/38 통과했다. 이전 생성 블록을 회전해도 이후 블록의 초기 상태가 같고, 한 게임판에서 20회 더 생성해도 다른 게임판의 생성 순서에 영향이 없는지 포함한다. 기존 고정·게임 오버·미리보기 회귀 검사도 모두 유지했다. 통계적인 분포 검사가 아니라 기존 알고리즘·시드 결과의 동일성 검사다.
- **직렬화 특성 보완**: 최초 추출 후 Board의 새 필드에 직렬화 경고가 추가돼 40개가 된 것을 발견했다. 기존 Random 필드의 직렬화 특성을 유지하도록 생성기에 Serializable과 serialVersionUID를 추가했다. transient 처리나 경고 억제는 사용하지 않았다. 보완 후 fresh 전체 컴파일·회귀를 다시 실행하여 경고 39개·38/38 통과를 확인했다. 별도 직렬화 왕복 검사에서도 Random 객체는 독립적이고 이후 210개 블록의 종류·색·모양이 동일했다. Board 전체 직렬화 지원을 새로 보장하는 변경은 아니다.
- **게임 화면·호출부 비교**: 창 크기 3종 × 실제 게임 상태 8종, 24개 PNG와 24개 상태 기록이 최종 수정 전후 48/48 byte-identical이다. Board의 protected/public API와 Gamepanel·Nextblockpanel의 바이트코드도 동일하다. 새 생성 클래스의 public 타입·`nextBlock()`과 Serializable 계약만 추가했다.
- **macOS 실제 조작**: 최종 production 소스로 별도 앱을 빌드하고 실제 `Tetris.main()`에 AppleScript OS 키 입력을 보내 12단계 확인했다. 자동 한 칸 1점·아래키 한 칸 2점, 하드드롭 16칸 32점과 미리보기 O블록 객체의 승계, 일반 고정 후 다음 O블록 객체의 승계를 확인했다. 같은 O종류가 연속 등장해도 서로 다른 객체였다. 일시정지 입력 무시, 게임 오버 시 타이머 종료·추가 입력 차단, 전용 점수 `5:199` 저장과 시작 화면 복귀 후 첫 방향키 반응도 통과했다. 화면 캡처를 확인했으며 원래 저장 파일은 변경하지 않았다.
- **독립 검토·진단**: 사전 계획 CLEAR 후 별도 검토자가 구현·회귀 38/38·최종 fresh 컴파일 경고 39개와 diff 검사를 확인했다. 새 직렬화 경고는 수정했고 최종 코드 발견 이슈 0개다. lint/type 진단은 Java 21 `--release 21 -Xlint:all` 컴파일로 확인했다. 별도 LSP/AST·보안 스캐너 구성이나 도구는 없어 형식 판정은 COMMENT, 코드 기준 승인 차단 사유 없음으로 보고했다.
- **근거·한계**: `artifacts/ref06/`에 전후 컴파일·회귀·렌더/상태 비교, 직렬화 왕복 검사, API 비교, GUI 소스 해시, native 입력 로그·화면과 `verification-summary.json`을 보관한다. GUI 소스 해시는 현재 production 소스와 전부 일치한다. 실제 저장소의 `setting.txt`·`tetris_score.txt`는 전후 동일하다. 테스트 앱은 시작 화면에 두었고 Windows 실행은 미검증이다. 후속 사용자 요청으로 로컬 커밋한다. 원격 게시는 별도로 진행한다.

### REF-07. 게임 규칙과 화면·타이머 경계

- **확인 사실 / 확신 높음**: Board 안에 충돌, 회전, 점수/레벨 계산, Timer, textChunks와 paintComponent가 함께 있으며 Gamepanel과 서로 호출한다. [Board.java:35](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:35>), [Board.java:175](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:175>), [Board.java:307](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:307>), [Board.java:358](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:358>), [Gamepanel.java:86](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Gamepanel.java:86>).
- **제안**: 앞선 작은 중복 정리가 끝난 뒤, 2차 개발에 필요한 계산부터 나눈다. 논의 단위는 ①충돌·회전 판정 ②줄 삭제·점수·레벨 결과 ③입력·Timer·화면 갱신과의 연결 순서다. 거대한 새 구조나 완성형 게임 엔진 재작성부터 결정하지 않는다.
- **위험·검증**: 활성 블록과 고정 블록이 현재 격자에 어떻게 포함되는지 유지해야 한다. 동일한 블록 순서·입력에서 격자·점수·next·게임 종료 결과가 같은지 비교하고, 각 부분을 나눈 뒤 검사한다. Timer는 실제 대기시간에 의존하지 않는 방식과 GUI 확인을 구분한다.
- **상의할 사항**: 이번 리팩토링에서 꼭 필요한 경계가 어디까지인지. 난이도·아이템 규칙까지 구현하는 것으로 범위를 넓히지 않는다.

### REF-08. 점수 저장 규칙

- **확인 사실 / 확신 높음**: 쓰기·읽기/정렬·초기화가 서로 다른 패널에서 파일 경로와 형식을 다룬다. [Gamepanel.java:153](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Gamepanel.java:153>), [Scoreboardpanel.java:87](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Scoreboardpanel.java:87>), [Settingpanel.java:88](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:88>).
- **제안**: 파일 처리/기록 규칙을 한곳에서 다루고 패널은 결과 표시나 사용자 동작 전달을 맡기는 방향을 검토한다. 설정 저장과 점수 저장을 반드시 하나의 거대한 저장 클래스에 합칠 필요는 없다.
- **위험·검증**: 기존 `이름:점수` 파일, 손상 행 뒤 정상 기록, 내림차순·상위 10개, 빈 파일, 초기화 후 재조회. 모드·난이도 필드를 실제 파일에 추가하는 작업은 별도 기능/호환성 변경이다.
- **상의할 사항**: 2차 점수 구분 기능 전에 이 경계를 미리 분리할지.

### REF-09. 미리보기와 렌더링의 유사 구현

- **확인 사실 / 확신 높음**: Nextblockpanel은 Gamepanel을 통해 Board.next를 조회하고, Board와 별도의 TextChunk·폰트 배율 렌더링을 갖는다. [Nextblockpanel.java:35](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Nextblockpanel.java:35>), [Nextblockpanel.java:96](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Nextblockpanel.java:96>), [Board.java:358](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:358>).
- **제안**: 우선 미리보기에 필요한 블록/표시 값을 직접 넘기는 좁은 경계가 유익한지 본다. 비슷해 보이는 렌더링 전체를 바로 하나로 합치는 것은 후순위로 둔다.
- **위험·검증**: 다음 블록의 모양·색·크기·갱신 시점, 화면 배율별 잘림, 아이템 문자 표시 확장. 현재 테스트는 실제 미리보기 페인트를 확인하지 않는다.
- **상의할 사항**: 간접 참조를 줄이는 것만 할지, 렌더링 중복을 남겨둘지.

### REF-10. 주석·미사용 데이터·이름

- **확인 사실 / 확신 높음**: `remove(mainPanel) // GC 발생`이라는 주석이 있지만 같은 필드 객체를 다시 add한다. `resetScore` 설정값은 존재하나 실제 점수 초기화는 파일을 바로 비운다. [Maincontainer.java:87](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:87>), [Maincontainer.java:194](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:194>), [Settingpanel.java:88](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Settingpanel.java:88>).
- **제안**: 수정하는 영역의 사실과 다른 주석·중복 세미콜론·불필요한 지역값부터 정리한다. 파일에 저장되는 설정 이름이나 외부에서 쓰는 API는 단순 미사용처럼 보여도 먼저 영향 범위를 확인한다.
- **위험·검증**: `resetScore` 항목 삭제는 기존 파일/출력 형식 변화가 될 수 있다. 클래스명·패키지명을 일괄 변경하면 기존 테스트 어댑터도 영향을 받으므로 현재 우선순위에서 제외한다.
- **상의할 사항**: 주석·명백한 중복만 정리하고 정책이 걸린 필드는 보류하는 안.

## 6. 순수 정리와 구분해서 결정할 정책

| 번호 | 결정할 사항 | 코드상 확인 내용 | 제안하는 현재 취급 |
|---|---|---|---|
| DEC-01 | 블록 시작 위치 중앙 정렬 | O블록 x=3→4로 바뀜. 게임 오버 판정도 현재 +3 사용 | 기능 변경 후보로 보류. 채택하면 세 생성 경로와 next 생성 가능 판정 함께 설계 |
| DEC-02 | 5키 레거시 설정 지원 | 보충 메서드는 삭제됐고 검증은 6키만 허용 | 과거 사용자 파일 존재 여부 미확인. 지원/미지원 정책부터 논의 |
| DEC-03 | 화면 재생성/재사용 및 선택 상태 | root는 유지, 개별 화면은 재생성. reset은 새 인스턴스 생성 시 수행 | 현재 재생성이 잘못됐다고 전제하지 않음. 바꿀 경우 선택 초기화·데이터 재조회·타이머 정리부터 결정 |
| DEC-04 | 배포 설정 파일과 초기화 기본값 | 코드 기본 크기 600X500, 체크인 setting.txt 크기 750X625 | 저장 파일이 사용자 상태인지 배포 기본값인지 결정. 이번 분석에서 값 변경 없음 |
| DEC-05 | 설정 크기와 실제 프레임 크기 API | Maincontainer.getWidth/getHeight가 설정 값을 반환 | 명시적 설정 크기 API와 프레임의 실제 크기 구분 검토. 흰 여백의 직접 원인으로 단정하지 않음 |

근거: DEC-01 [Board.java:341](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Board.java:341>); DEC-02 [Maincontainer.java:229](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:229>); DEC-03 [Maincontainer.java:42](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:42>), [Startpanel.java:26](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Startpanel.java:26>), [Scoreboardpanel.java:30](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Scoreboardpanel.java:30>); DEC-04 [Maincontainer.java:191](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:191>), [setting.txt:4](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/setting.txt:4>); DEC-05 [Maincontainer.java:134](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/src/seoultech/se/tetris/component/Maincontainer.java:134>).

현재 규칙을 바꾸지 않는 정리와 의도적으로 동작을 바꾸는 수정은 문서와 향후 커밋에서 구분한다. 기능 변화가 있어야 하는 항목은 사용자가 방향을 정하기 전까지 구현 대상으로 간주하지 않는다.

## 7. 테스트 근거와 아직 확인하지 못한 부분

- **파일 확인**: [RefactoringRegressionTest.java:55](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/tests/RefactoringRegressionTest.java:55>)에 현재 26개 검사가 있다(기존 16개 + 창 크기 독립성 + 시작 화면 복귀 + 블록 초기값 + 메뉴 7개). 빌드 도구 설정은 없지만 테스트 코드가 없는 상태는 아니다. 새 라이브러리 설치를 리팩토링의 필수 선행 조건으로 두지 않는다.
- **이전 실행 기록**: 2026-10-21 저장된 [refactoring-tests-run.log:1](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/artifacts/pr10-review/refactoring-tests-run.log:1>)은 수정 브랜치 16/16 통과다. [refactoring-tests-unfixed-main.log:1](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/artifacts/pr10-review/refactoring-tests-unfixed-main.log:1>)에는 당시 미수정 main `e6f2d5c`에서 설정 관련 네 검사가 실패한 기록이 있다.
- **분석 작성 당시**: 문서 작성만 수행했고 테스트를 실행하지 않았다.
- **후속 오류 해결 요청 검증 (2026-10-22)**: 전체 게임 소스 16개와 회귀 테스트를 Java 21 `--release 21 -Xlint:all`로 컴파일해 오류 0, 회귀 검사 16/16 통과. 컴파일 경고는 남아 있으며 새 리팩토링 범위로 확대하지 않았다.
- **새 로그**: [컴파일 로그](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/artifacts/pr10-review/pr10-fix-20261022-compile.log:1>), [테스트 로그](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/artifacts/pr10-review/pr10-fix-20261022-tests.log:1>).
- **검증 범위**: 설정 변경/초기화/독립성, 블록 회전·생성·충돌, 하드드롭·줄 삭제·레벨, 정지/재개·게임 오버, 점수 저장·순위·초기화.
- **범위 밖**: 실제 창의 흰 여백·포커스·페인트·색 구분, 미리보기 표시, Windows 실행, 커버리지 비율. 전체 요구사항 충족을 자동 검사 통과만으로 보장하지 않는다. REF-01의 실제 창 검증은 5절 실행 기록을 따른다.
- **유지할 주의점**: 일부 검사는 결합된 UI를 준비하기 위해 reflection/Unsafe와 내부 이름을 사용한다. 내부 구조·이름을 바꾸면 어댑터를 조정할 수 있지만, 실패를 없애려고 동작 기대값을 임의로 바꾸지 않는다. [RefactoringRegressionTest.java:386](</Users/henrykil/Library/Mobile Documents/com~apple~CloudDocs/1. 대학수업/4학년2학기/SW공학/SE-Codex/SeoulTech-SE-Tetris-Ref/tests/RefactoringRegressionTest.java:386>).

## 8. 앞으로 이 문서로 논의하는 방법

1. **PR10-01~03은 2026-10-22 현재 브랜치에서 해결·재검증 완료**다. 원격 게시·main 반영과는 구분한다.
2. 그다음 PR10-04의 화면 문제를 어디까지 먼저 다룰지 확인한다.
3. 이후 REF-01부터 한 항목씩 현재 코드→불편한 점→선택지→동작 유지 조건 순서로 상의한다.
4. 합의된 결론과 실제 구현/검증 결과는 아래 기록에 구분해서 추가한다. 현재 문서의 제안은 일괄 구현 지시가 아니다.
5. 이후 코드 변경은 사용자가 지시한 범위에서 진행한다. 최초 분석 요청에서는 코드 변경을 하지 않았고, 후속 오류 해결 요청에서도 기존 수정으로 통과해 추가 소스 변경이 필요하지 않았다.

| 날짜 | 논의 항목 | 사용자와 합의한 결론 | 구현·검증 상태 |
|---|---|---|---|
| 2026-10-22 | 문서 작성 범위 | 코드 변경 없이 전체 구조 분석·제안 작성, PR10 오류 처리 우선 | 문서만 작성 |
| 2026-10-22 | PR10-01~03 | 사용자 요청에 따라 조건부 오류 해결 우선 처리 | 기존 `4eabe8d` 적용 확인, Java 21 컴파일 및 회귀 검사 16/16 통과, 로컬 완료 |
| 미정 | PR10-04 | 상의 대기 | 실제 GUI 원인 미확인 |
| 2026-10-22 | REF-01 | 중복 표시 제거·시작 화면 복귀 공통화 | 구현 완료, 회귀 18/18 및 macOS 실제 조작 검증 완료 |
| 2026-10-22 | REF-02 | 부모의 중복 초기화 삭제, 7종 초기 모양·색 검사 보강 | 자동 검사 19/19 및 후속 macOS 실제 조작 검증 완료 |
| 2026-10-22 | REF-03 | 화면별 메뉴 선택 상태 분리·공통 선택 처리·키 메뉴 표시 재사용 | 회귀 26/26·24상태 렌더 동일·실제 조작 50단계 통과(macOS) |
| 2026-10-22 | REF-04 | 설정 문자열 처리 공통화·UI의 내부 Map 직접 접근 제거 | 회귀 32/32·24상태 렌더 동일·macOS 실제 조작 40단계 검증 완료 |
| 2026-10-22 | REF-05 | 일반 낙하·하드드롭의 고정 후 승계 중복만 공통화 | 회귀 37/37·실제 게임 24상태 렌더 동일·macOS 실제 조작 검증 완료, 로컬 커밋 |
| 2026-10-22 | REF-06 | 블록 생성 책임을 독립 클래스로 이동, 기존 생성 순서·확률·인스턴스 유지 | 회귀 38/38·실제 게임 24상태 렌더 동일·macOS 실제 조작 12단계 검증 완료, 로컬 커밋 |
| 미정 | REF-07~10 / DEC-01~05 | 각 항목의 합의·논의 상태는 5·6절 참조 | 후속 리팩토링 구현 전 |


## 9. 조건부 오류 해결 기록 커밋

- 2026-10-22 사용자 요청: `PR 10 오류 3종 해결` 제목으로 현재 결과를 커밋.
- 오류 수정 코드 본체는 기존 `4eabe8d`에 포함되어 있다. 이번 추가 커밋에는 회귀 테스트 파일과 이 검토·해결 기록을 포함한다.
- 2026-10-22 Java 21 컴파일 및 회귀 테스트 16/16 통과 결과를 기준으로 저장하며, 후속 리팩토링 구현과 원격 push·머지는 포함하지 않는다.
