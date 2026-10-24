# SeoulTech SE Tetris 사용자 안내서

이 안내서는 `0.1.0` 실행 패키지 기준이다. 패키지에는 Java 21 실행 환경이 들어 있으므로 별도 Java 설치가 필요하지 않다.

## 실행

### Windows 11

1. `seoultech-tetris-0.1.0-windows-x64.zip`을 원하는 폴더에 완전히 푼다.
2. `SeoulTech Tetris` 폴더의 `SeoulTech Tetris.exe`를 더블클릭한다.
3. Windows가 출처를 확인할 수 없다는 알림을 표시하면, 배포자에게 ZIP과 SHA-256 값을 확인한다. 확인되지 않은 파일은 실행하지 않는다.

### macOS 26 이상

1. Mac CPU에 맞는 `seoultech-tetris-0.1.0-macos-<architecture>.zip`을 푼다. Apple Silicon은 `aarch64`, Intel은 `x64` 패키지를 사용한다.
2. `SeoulTech Tetris.app`을 Applications 등 원하는 폴더로 옮긴 뒤 더블클릭한다.
3. 이 개발용 패키지는 Apple 공증·서명이 없다. macOS가 실행을 막으면 시스템 설정의 개인정보 보호 및 보안 화면에서 파일 이름과 배포 출처를 확인한 뒤 한 번만 실행을 허용한다.

패키지의 `.sha256` 파일과 ZIP 파일을 같은 폴더에 두고 아래처럼 무결성을 확인할 수 있다.

```powershell
# Windows PowerShell
Get-FileHash .\seoultech-tetris-0.1.0-windows-x64.zip -Algorithm SHA256
```

```bash
# macOS
shasum -a 256 seoultech-tetris-0.1.0-macos-aarch64.zip
```

## 메뉴와 조작

시작 메뉴와 설정 메뉴에서는 `↑`·`↓`로 항목을 선택하고 `Enter`로 실행한다.
시작 메뉴에서는 `←`·`→`로 Easy, Normal, Hard 난이도를 고른다. Normal은 7종 블록이 같은 확률로 나오며, Easy는 I 블록이 조금 더 자주, Hard는 I 블록이 조금 덜 나온다. 선택한 난이도는 새 점수 기록과 스코어보드에 함께 표시된다.

| 동작 | 기본 키 |
|---|---|
| 시계 방향 회전 | `↑` |
| 한 칸 아래로 이동 | `↓` |
| 왼쪽·오른쪽 이동 | `←`·`→` |
| 일시정지·재개 | `P` |
| 하드드롭 | `Space` |
| 진행 중 게임을 끝내고 시작 메뉴로 복귀 | `Esc` |

설정 화면에서 창 크기, 조작키, 색각이상 대응 팔레트를 변경할 수 있다. `Reset setting`은 설정을 기본값으로 되돌리고, `Reset Scoreboard`는 점수 기록을 지운다. 설정 화면에서 시작 메뉴로 돌아갈 때 변경 내용이 저장된다.

게임오버 뒤 기록을 입력할 때에는 문자와 숫자를 입력하고 `Backspace`로 지운다. 이름을 입력하지 않으면 `PLAYER`로 저장된다. `Enter`를 누르면 기록을 저장하고 점수판으로 이동한다. 스코어보드 화면에서도 `Enter`로 시작 메뉴에 돌아간다.

완성된 행은 격자에서 즉시 삭제되고, 삭제 위치를 약 0.2초 동안 밝게 표시한다. 이 표시는 입력·점수 계산·다음 블록 진행을 막지 않는다.

## 데이터 위치

실행 패키지는 사용자 홈의 `.seoultech-tetris` 폴더에 `setting.txt`와 `tetris_score.txt`를 저장한다. 압축을 다시 풀거나 앱을 다른 폴더로 옮겨도 이 데이터는 유지된다. 기록을 완전히 초기화하려면 게임의 설정 화면에서 초기화 기능을 사용한다.

개발 저장소에서 `./gradlew run`으로 실행하면 기존 호환성을 위해 작업 디렉터리의 두 파일을 사용한다.

## 알려진 제약

- `0.1.0` 패키지는 Windows 코드 서명과 Apple 서명·공증이 적용되지 않은 수업용 빌드다.
- Windows와 macOS 패키지는 각 운영체제에서 따로 만들어야 하며 CPU 아키텍처가 다른 macOS 패키지는 서로 대체할 수 없다.
- 실제 운영체제·CPU에서 확인한 패키지와 SHA-256 값은 릴리스 노트의 검증 표를 기준으로 한다.

순위가 이미 10개 있고 현재 점수가 10위보다 높지 않으면 이름 입력 없이 점수판으로 이동합니다. 진행 중에는 화면의 `MENU` 버튼으로 복귀할 수 있으며 `Esc`를 조작키로 지정했다면 해당 조작이 우선합니다.
