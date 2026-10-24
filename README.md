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
also runs the existing 52 standalone regression checks through a JUnit adapter.
Open `build/reports/tests/test/index.html` for the results.

See [development instructions](docs/development.md) for setup, test scope,
and troubleshooting.

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
