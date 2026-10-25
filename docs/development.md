# 개발·빌드·테스트 실행

## 환경과 고정 버전

| 항목 | 설정 |
|---|---|
| 프로젝트 JDK | JDK 21. `JAVA_HOME`을 해당 JDK 경로로 설정 |
| Java 컴파일 | Java toolchain 21, `--release 21`, UTF-8, `-Xlint:all` |
| Gradle | Wrapper 9.8.2, 공식 배포 ZIP SHA-256 검증 |
| JUnit | JUnit 5 BOM 5.14.1, Jupiter와 Platform Launcher |
| 의존성 저장소 | Maven Central |

JDK의 패치 버전·제조사와 OS는 팀원 환경에 따라 다를 수 있다. 실행 기록에는
`java -version`과 `./gradlew --version` 결과를 남긴다. JDK 21이 없으면 먼저 설치한다.
다른 Java 버전만 설치한 상태에서 toolchain을 자동 다운로드하도록 구성하지 않았다.
별도 Gradle 설치는 필요 없다. 첫 실행에는 Gradle·JUnit 다운로드를 위한 인터넷 연결이 필요하다.

macOS에서 JDK 21을 설치한 뒤 설정하는 예:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew --version
```

Windows PowerShell 예(경로는 실제 설치 위치로 변경):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat --version
```

## 명령

저장소 루트에서 실행한다. Windows에서는 `./gradlew` 대신 `.\gradlew.bat`을 사용한다.

| 명령 | 동작 |
|---|---|
| `./gradlew build` | 컴파일·테스트와 application 플러그인의 개발용 배포 아카이브 생성 |
| `./gradlew test` | JUnit 검사와 기존 독립 실행 회귀 검사 실행 |
| `./gradlew clean build` | 빌드 결과를 지우고 다시 빌드·검증 |
| `./gradlew test --rerun-tasks` | 이전 성공 결과를 재사용하지 않고 검사 재실행 |
| `./gradlew run` | Swing 게임 실행. 화면을 사용할 수 있는 데스크톱 환경 필요 |

소스/의존성이 바뀌지 않았다면 `test`가 `UP-TO-DATE`로 표시될 수 있다. 매번 실제
재실행이 필요하면 `--rerun-tasks`를 사용한다. `run`은 루트의 `setting.txt`와
`tetris_score.txt`를 사용하는 기존 동작을 유지하므로, 직접 플레이하면 이 파일이 바뀔 수 있다.
자동 테스트는 별도 임시 폴더에서 실행해 두 파일을 변경하지 않는다.

`build`가 만드는 ZIP/TAR와 실행 스크립트는 개발용 산출물이다. 아이콘·런타임을 포함한
더블클릭 배포 패키지는 `packageRelease`로 만든다. 깨끗한 Windows/macOS 사용자 환경의
물리 키·저장 후 재실행은 TEST-06/RELEASE-04의 수동 인수 조건이다.

## 디렉터리

```text
build.gradle                      빌드·의존성·실행·검사 설정
settings.gradle                   프로젝트 이름
gradlew / gradlew.bat              OS별 Wrapper 진입점
gradle/wrapper/                    Wrapper JAR·배포 버전·체크섬
src/main/java/seoultech/se/tetris/ 게임 소스 (기존 패키지 유지)
src/test/java/seoultech/se/tetris/ 테스트 소스
build/                            생성된 클래스·보고서·개발용 아카이브 (Git 제외)
```

기존 `src/seoultech/...`는 `src/main/java/seoultech/...`로 옮겼다.
`tests/RefactoringRegressionTest.java`는 패키지에 맞게
`src/test/java/seoultech/se/tetris/component/RefactoringRegressionTest.java`로 옮겼다.
기존 문서의 옛 경로는 당시 commit 기준이다. IDE에서는 Gradle 프로젝트로 다시 가져오고,
진행 중인 다른 브랜치를 합칠 때에는 변경된 경로에서 양쪽 기능을 확인한다.

## 55개 회귀 검사와 JUnit

`LegacyRegressionTest`는 JUnit 검사 1개로 기존 독립 실행 검사 전체를 실행하는 어댑터다.
보고서의 JUnit 검사 수 1은 내부 회귀 검사가 1개라는 뜻이 아니다. 현재 내부 검사 수는 기존 52개와 경계 검사 3개를 합한 55개이며,
기존 실행기가 각 검사를 별도 JVM/임시 폴더에서 수행한다. 점수·신규 난이도 기록 형식의 요구 결함 수정에 따른 기대값 변경은 `test-requirements.md`에 명시했다.

어댑터는 Gradle이 전달한 전체 test runtime classpath와 Java 21로 별도 JVM을 시작한다.
기존 실행기의 `System.exit`이 JUnit 프로세스를 종료하지 않으며, 실패 종료 코드는 JUnit 실패와
Gradle 명령 실패로 전달된다. 전체 실행이 비계측 5분(커버리지 계측 시 10분)을 넘으면 자식 프로세스를 종료하고 실패 처리한다.
JUnit 검사가 발견되지 않는 경우에도 빌드를 실패시킨다.
기존 실행기는 진입 시 표준 출력과 오류 출력을 UTF-8로 명시해 부모/내부 검사 JVM의 로그를
같은 인코딩으로 읽는다. Windows 기본 콘솔 인코딩이 달라도 한글 결과와 실패 원인을 보존한다.

- HTML 보고서: `build/reports/tests/test/index.html`
- XML 결과와 내부 55개 실행 로그: `build/test-results/test/TEST-seoultech.se.tetris.component.LegacyRegressionTest.xml`의 `system-out`
- 새 JUnit 검사는 `src/test/java` 아래의 적절한 패키지에 추가

기존 실행기를 JUnit 없이 직접 실행하는 방법(macOS/Linux, JDK 21):

```bash
test_classes=$(mktemp -d)
"$JAVA_HOME/bin/javac" --release 21 -encoding UTF-8 -d "$test_classes" \
  $(find src/main/java -name '*.java') \
  src/test/java/seoultech/se/tetris/component/RefactoringRegressionTest.java
"$JAVA_HOME/bin/java" -cp "$test_classes" seoultech.se.tetris.component.RefactoringRegressionTest
```

임시 클래스 폴더는 검증 후 삭제할 수 있다. Gradle과 JUnit을 통한 실행이 팀의 기본 경로다.

## 검증 범위와 후속 작업

이 구성은 빌드와 기존 동작 검사를 반복 실행하는 기반이다. 회귀 통과가 요구사항 전체 충족을
뜻하지 않는다. 실제 창·포커스·키 반복·Windows 실행·성능·커버리지 비율은 별도 검증한다.
기존 reflection/Unsafe 등 컴파일 경고도 이번 기반 구성에서 수정하지 않는다.

JaCoCo는 Gradle JUnit worker와 기존 회귀 실행기·내부 검사 JVM까지 agent를 전달해 계측한다.
`LegacyRegressionTest`는 자식 실행 전후 coverage 파일 크기 증가를 확인하고, `check`는
제품 전체 line coverage 70%를 검사한다. HTML/XML 보고서에서 측정 대상과 실제 실행된
제품 코드를 확인할 수 있다. 현재 실측값과 최종 제출 조건은 [R2-07 원장](req2-final-verification.md)에 연결한다.
GitHub push/PR 자동 실행은 아래 CI 구성을 따른다.

## GitHub Actions CI

`.github/workflows/ci.yml`의 `Java CI`가 모든 브랜치의 push와 PR 생성/갱신 시 실행된다.
GitHub의 Actions 탭에서 실행을 선택하거나 PR의 Checks에서 결과를 확인한다.
fork에서 처음 사용하는 경우 해당 저장소의 Actions 활성화가 필요할 수 있다.

| 검사 작업 | 실행 환경 | 명령 |
|---|---|---|
| `Build and test (windows-2025)` | Windows Server 2025, PowerShell, Temurin Java 21 | `./gradlew.bat --no-daemon --console=plain build desktopSmoke packageRelease` |
| `Build and test (macos-26)` | macOS 26, Bash, Temurin Java 21 | `./gradlew --no-daemon --console=plain build desktopSmoke packageRelease` |

두 작업은 독립 실행된다. 한 OS에서 실패해도 다른 OS의 검사는 계속 진행한다.
같은 브랜치/PR에 새 변경이 올라오면 이전 실행을 취소하고 최신 변경을 검사한다.
Gradle Wrapper의 공식 JAR 검증 후 빌드하며, 기존 JUnit 어댑터를 통해 55개 회귀 검사도 실행한다.
검사가 실패하면 해당 작업이 실패로 표시된다. 외부 Action은 확인한 릴리스의 전체 commit SHA로 고정한다.

성공·실패 모두 `test-reports-windows-2025`, `test-reports-macos-26` 아티팩트를 7일간 보관한다.
실행 화면의 Artifacts에서 다운로드하면 JUnit XML/내부 회귀 로그, JaCoCo·성능·실제 창 보고서,
요구사항 2 분포 CSV·팔레트 PNG와 Gradle 문제 보고서를 볼 수 있다. 성공한 OS job의
`package-<OS>`에는 실행 ZIP과 SHA-256도 보관한다. 컴파일/환경 준비가 먼저 실패해 보고서가 없으면
아티팩트 미생성 경고와 해당 단계의 로그를 확인한다.

워크플로의 저장소 권한은 `contents: read`이며 checkout 인증 정보는 남기지 않는다.
CI에 개인 토큰이나 배포용 secret은 필요 없다. Windows Server의 headless 검사 결과는
Windows 11의 실제 게임 창·키 입력·설치 검증을 대신하지 않으며, 해당 검증은 TEST-06/RELEASE-04에서 진행한다.

## 공식 근거

- [Gradle 9.8.2 Wrapper](https://docs.gradle.org/9.8.2/userguide/gradle_wrapper.html): Wrapper 파일의 버전 관리와 배포 체크섬
- [Gradle Java toolchains](https://docs.gradle.org/9.8.2/userguide/toolchains.html): Java 21 선택과 release 설정
- [Gradle JVM testing](https://docs.gradle.org/9.8.2/userguide/java_testing.html): JUnit Platform·검사 실패 처리
- [Gradle 공식 체크섬](https://gradle.org/release-checksums/): ZIP 및 Wrapper JAR 검증
- [JUnit 5.14.1 Gradle 설정](https://docs.junit.org/5.14.1/running-tests/build-support.html): BOM·Jupiter·Launcher 구성
- [GitHub 호스팅 실행 환경](https://docs.github.com/en/actions/reference/runners/github-hosted-runners): Windows/macOS 실행 환경
- [Gradle setup-gradle](https://github.com/gradle/actions/blob/v6.4.0/docs/setup-gradle.md): Wrapper 검증과 캐시
- [GitHub 테스트 산출물 보관](https://docs.github.com/en/actions/tutorials/store-and-share-data): 아티팩트 업로드와 보관 기간


## 커버리지·성능·실제 창 검사·배포

- `./gradlew clean build`: 독립 회귀 55개, 추가 JUnit 검사, 전체 제품 코드 line coverage 70% 검증. JaCoCo HTML/XML은 `build/reports/jacoco/test/`.
- `./gradlew desktopSmoke`: 그래픽 세션에서 실제 JFrame을 띄우고 합성 KeyListener 입력으로 메뉴·반복 입력·정지/재개·설정·점수판·순위 진입/미진입을 검사한다. OS 물리 입력 검증을 대체하지 않는다. `build/reports/desktop/`에 결과·화면을 남긴다.
- 성능 검사는 `PerformanceRegressionTest`에서 핵심 이동·회전 판정 400,000회를 5초 예산으로 검증하고 JUnit 로그에 환경·시간을 남긴다.
- `./gradlew packageRelease`: 현재 OS의 Java 21 런타임 포함 앱 이미지 ZIP·SHA-256 생성. Windows에서는 `gradlew.bat` 사용. 배포 방식과 실행법은 [사용자 안내](user-guide.md), 검증 범위·한계는 [검증 기록](verification.md).
- GitHub CI는 Windows Server 2025·macOS 26에서 `build desktopSmoke packageRelease`를 수행하고 테스트·커버리지·실제 창 결과 및 OS별 실행 ZIP을 각각 7일간 보관한다. 실제 Windows 11 설치 확인은 별도다.
