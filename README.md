# SeoulTech SE Course Team Project - Tetris reference code.

## Build, test and run

Install JDK 21 and set `JAVA_HOME` to its installation directory. Use the
checked-in Gradle Wrapper; a separate Gradle installation is not required.

```bash
./gradlew build
./gradlew test
./gradlew run
```

On Windows PowerShell, use `.\gradlew.bat build`, `.\gradlew.bat test`, and
`.\gradlew.bat run`. The first invocation needs internet access to download
Gradle and test dependencies.

The project uses Gradle 9.8.2, Java 21, and JUnit 5.14.1. Production sources
are under `src/main/java`; tests are under `src/test/java`. The `test` task
also runs the 55 standalone regression checks (the original 52 plus 3 boundary checks) through a JUnit adapter.
Open `build/reports/tests/test/index.html` for the results.

See [development instructions](docs/development.md) for setup, test scope,
and troubleshooting.

## 요구사항 2 평가·검증 자료

- [최종 통합 검증 원장](docs/req2-final-verification.md): 검사 버전·CI·배포물 체크섬·미완료 인수 조건
- [요구사항 2 추적표](docs/requirements-2-traceability.md): 요구사항→카드→코드→테스트 연결
- [재사용 평가 체크리스트](docs/evaluation-checklist.md), [우리 팀 평가 결과](docs/evaluations/req2-2026-10-25.md)
- [아이템 형태·효과·재현 명세](docs/req2-items.md), [모드별 기록 정책](docs/req2-mode-scores.md)

전체 자동 검사는 `./gradlew clean build desktopSmoke packageRelease`로 재현한다.
그래픽 세션과 JDK 21이 필요하며 Windows에서는 `gradlew.bat`을 사용한다.
실제 Windows 11·물리 키·사람의 색 식별·깨끗한 환경의 저장/재실행은 원장의 수동 인수 양식으로 확인한다.

## Original reference-code notes

This code is a very basic implementation of Tetris game.   

Use it only for reference of how Java Swing and simple game logics work, if you don't know where to start.   
To implement the actual game, like you've seen in the demo, you need to chagne a lot for implementation.  
Hence, it would be better to start with your own code, and use this code as a hint. 

The code implements the following functionalities.  

- Create game screen with ```JFrame```.
- Implement simple ```KeyListener``` for player's input.
- Add ```Timer``` for dropping blocks.
- Default blocks of Tetris game.
- Basic game logics for block movement.
- Simple examples for changing styles.

Obviously, the current code is deliberately changed into a crude state, to prevent you from using it as your base code.   
For further funcionality implementations, you need to change the design and implementation a lot to make the code in better shape.  

--------------

**주의**

여기 제시된 코드는 Java Swing을 사용하여 어떻게 게임을 구현해야 할지 힌트를 주기 위한 것입니다.  
현재 디자인 및 구현은 앞으로 게임을 위한 다양한 기능을 구현하기에 적절한 상태가 아니니 단순한 참고용으로 사용하시기 바랍니다.  
구현 과정에서 원하는 경우 다른 오픈소스 라이브러리 등을 가져와 사용해도 좋습니다.


Release candidates include a Java 21 runtime and application icons. Run `./gradlew packageRelease`
(on Windows, `gradlew.bat packageRelease`) on each target OS. See [the user guide](docs/user-guide.md)
and [verification scope and results](docs/verification.md). `build` also enforces 70% line coverage;
`desktopSmoke` checks a real JFrame using synthetic input in a graphical session.
