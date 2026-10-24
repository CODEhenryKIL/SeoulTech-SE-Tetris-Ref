package seoultech.se.tetris.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import seoultech.se.tetris.blocks.Block;
import seoultech.se.tetris.blocks.IBlock;
import seoultech.se.tetris.blocks.JBlock;
import seoultech.se.tetris.blocks.LBlock;
import seoultech.se.tetris.blocks.OBlock;
import seoultech.se.tetris.blocks.SBlock;
import seoultech.se.tetris.blocks.TBlock;
import seoultech.se.tetris.blocks.ZBlock;
import seoultech.se.tetris.rules.CollisionRules;

class PerformanceRegressionTest {
    private static final int ITERATIONS = 100_000;
    private static final Duration BUDGET = Duration.ofSeconds(5);

    @Test
    @Timeout(10)
    @DisplayName("핵심 이동·회전 판정 40만 회 성능 예산")
    void coreMovementDecisionsStayWithinBudget() {
        int[][] grid = new int[20][10];
        Block[] blocks = {new IBlock(), new JBlock(), new LBlock(), new OBlock(),
            new SBlock(), new TBlock(), new ZBlock()};

        for (int index = 0; index < 10_000; index++) {
            Block block = blocks[index % blocks.length];
            CollisionRules.canDown(grid, block, 3, 0);
            CollisionRules.rotationPosition(grid, block, 3, 0);
        }

        long started = System.nanoTime();
        int operations = 0;
        int successfulDecisions = 0;
        for (int index = 0; index < ITERATIONS; index++) {
            Block block = blocks[index % blocks.length];
            successfulDecisions += CollisionRules.canDown(grid, block, 3, 0) ? 1 : 0;
            successfulDecisions += CollisionRules.canLeft(grid, block, 3, 0) ? 1 : 0;
            successfulDecisions += CollisionRules.canRight(grid, block, 3, 0) ? 1 : 0;
            successfulDecisions += CollisionRules.rotationPosition(grid, block, 3, 0) != null ? 1 : 0;
            operations += 4;
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - started);

        assertEquals(ITERATIONS * 4, operations, "정해진 판정 작업을 모두 실행해야 합니다.");
        assertEquals(operations, successfulDecisions,
            "빈 보드의 중앙 생성 위치에서는 모든 이동·회전 판정이 성공해야 합니다.");
        assertTrue(elapsed.compareTo(BUDGET) < 0,
            operations + "회 핵심 판정이 성능 예산 " + BUDGET.toMillis()
                + "ms를 초과했습니다: " + elapsed.toMillis() + "ms");
        System.out.printf("PERFORMANCE core-decisions operations=%d elapsedMs=%d budgetMs=%d%n",
            operations, elapsed.toMillis(), BUDGET.toMillis());
    }
}
