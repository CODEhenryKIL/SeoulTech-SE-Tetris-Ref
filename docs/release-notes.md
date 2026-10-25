# SeoulTech SE Tetris 0.1.0 릴리스 노트

## 제공 기능

- 아이템 후속 변경: 2×2 P의 가로 2행·세로 2열 삭제, 총 5종 각 20% 공급, 여러 10줄 경계의 지급 권리 보존과 C/B 검증을 추가했다. 회전하지 않는 B가 회전 보정으로 밀리던 동작을 막았다. [아이템 규칙과 검증](req2-items.md)을 따른다. 아래 이전 패키지 체크섬은 이 변경을 포함하지 않는다.

- 모드별 기록 후속 변경: Classic/Item 정보 저장, 모드별 상위 10개와 순위 진입 판정, All 목록의 이전 파일 호환 및 새 기록 강조. 정책과 검증은 [R2-14 기록](req2-mode-scores.md)을 따른다. 아래 이전 패키지 체크섬은 이 변경을 포함하지 않는다.

- 20×10 보드, 7종 테트로미노, 회전·이동·하드드롭·다음 블록 표시
- 줄 삭제, 점수·레벨·낙하 속도, 일시정지와 게임 종료 처리
- 창 크기 3종, 조작키 변경, 색각이상 대응 팔레트
- 점수 기록 저장·정렬·상위 10개 표시·초기화
- 설정 메뉴의 Easy·Normal·Hard 선택·저장, 난이도별 블록 확률·속도 증가와 점수판 난이도 표기
- 삭제 행의 짧은 강조 애니메이션과 진행 중 `Esc` 메뉴 복귀
- Java 21 런타임과 앱 아이콘을 포함한 Windows/macOS 실행 패키지

## 패키지 생성

JDK 21의 `jpackage`를 사용하며, 해당 운영체제에서만 그 운영체제의 앱 이미지를 생성한다.

```bash
./gradlew clean build packageRelease
```

Windows PowerShell에서는 다음 명령을 사용한다.

```powershell
.\gradlew.bat clean build packageRelease
```

결과는 `build/distributions/`에 ZIP과 같은 이름의 `.sha256` 파일로 생성된다. `app-image` 형식을 압축하므로 설치 프로그램 권한이나 별도 Java 설치 없이 압축 해제 후 실행할 수 있다. 생성된 런처는 `-Dtetris.packaged=true`를 사용해 설정과 점수를 사용자 홈의 `.seoultech-tetris`에 저장한다.

| 대상 | 산출물 | 상태 |
|---|---|---|
| macOS 26 / Apple Silicon | `seoultech-tetris-0.1.0-macos-aarch64.zip` | 로컬 생성·SHA-256·압축 해제·엄격 서명 구조 검사·번들 런처 기동 확인 |
| Windows Server 2025 / x64 | `seoultech-tetris-0.1.0-windows-x64.zip` | CI 생성·SHA-256·런처/JVM/JAR/아이콘 구조 검증 성공; 실제 Windows 11 설치·조작은 RELEASE-04 기록에서 별도 확인 |

## 무결성과 재현 조건

- 소스 기준: `docs/req1-nondev-tasks` 브랜치의 릴리스 검증 commit
- Gradle Wrapper 9.8.2, Temurin/OpenJDK 21
- 앱 버전 `0.1.0`, 비모듈 JAR 진입점 `seoultech.se.tetris.main.Tetris`
- macOS `CFBundleShortVersionString`은 첫 숫자가 양수여야 하는 jpackage 제약 때문에 `1.0.0`을 사용하고, 배포 ZIP·릴리스 표기는 `0.1.0`으로 유지
- 패키지별 `.sha256` 파일을 함께 보관

## 알려진 제약

- 수업용 후보 빌드로 Windows 코드 서명과 Apple 서명·공증을 적용하지 않았다.
- OS별 패키지는 교차 생성하지 않는다. Windows 패키지는 Windows에서, macOS 패키지는 macOS에서 만든다.
- headless 자동 테스트는 실제 키 반복 입력, 창 표시, 더블클릭 실행을 대체하지 않는다. 사용자 환경 검증 범위는 릴리스 검증 기록에 따로 남긴다.
- 이후 게임 규칙·화면·저장 형식이 바뀌면 동일 절차로 패키지를 다시 생성하고 전체 회귀·실행 검증을 반복해야 한다.

소스 `456d876`의 [양 OS CI](https://github.com/CODEhenryKIL/SeoulTech-SE-Tetris-Ref/actions/runs/40085571922)와 다운로드·체크섬은 [검증 기록](verification.md#실제-github-ci와-배포-산출물-확인)에 연결했다. 실제 Windows 11/깨끗한 사용자 환경 검증은 별도 조건으로 남긴다.

## 최신 main 통합 범위

요구사항 2 PR #15가 머지된 main을 통합하여 다섯 시작 메뉴와 아이템 모드 진입을 유지한다. 요구사항 1의 표준 소스 경로, Java 21 빌드·CI·패키징, 기존 점수 파일 호환성과 일반 행 삭제 효과를 함께 유지한다. 요구사항 2의 신규 테스트 및 아이템 효과·난이도 UI 등의 알려진 오류 수정은 별도 작업이다. 위 이전 패키지 체크섬은 해당 소스 commit에만 적용된다.

## BUG-03 후속 수정

- 아이템 애니메이션 중 정지·재개·메뉴 복귀와 연속 입력을 처리하고, 일반 줄 삭제 강조와 아이템 효과의 Timer 정리를 분리했다.
- 설정 메뉴의 난이도 항목을 연결하고, 속도 증가량에 Easy 0.8·Normal 1.0·Hard 1.2 계수를 적용했다.
- L 표시 좌표를 한 점유 칸에 고정하고, L과 일반 삭제 행의 합집합에 점수·누계·아이템 지급을 한 번씩 반영한다.
- 무게추는 최초 접촉 후 좌우 이동을 막고 회전 위치 보정을 적용하지 않는다.
- 추가 회귀와 전체 빌드·실행 패키지 검증 결과는 [BUG-03 검증 기록](verification.md#bug-03-pr-15-후속-수정-검증-2026-10-24)에 남겼다. 위의 과거 패키지 링크는 각 기록의 소스 버전에만 해당한다.
