# SeoulTech SE Tetris 0.1.0 릴리스 노트

## 제공 기능

- 20×10 보드, 7종 테트로미노, 회전·이동·하드드롭·다음 블록 표시
- 줄 삭제, 점수·레벨·낙하 속도, 일시정지와 게임 종료 처리
- 창 크기 3종, 조작키 변경, 색각이상 대응 팔레트
- 점수 기록 저장·정렬·상위 10개 표시·초기화
- Easy·Normal·Hard 난이도별 블록 확률과 점수판 난이도 표기
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
