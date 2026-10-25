# GitHub Issue 등록 대기 본문

상태: **미등록**. 원본 저장소 `has_issues=false`, 공식 API 생성 요청은 `410 Gone`. 현재 계정 `admin=false`로 기능 활성화 불가. 관리자가 원본 저장소 Issues를 켠 뒤 아래 제목·본문으로 등록하고 실제 Issue URL을 R2-07 원장·보드에 연결한다. 등록 완료 전 임의 번호나 링크를 만들지 않는다.

제목: `[R2-07] 요구사항 2 오류 수정·재검증 이력 추적`

## 추적 보완 목적

R2-07 최종 평가 증빙 대조에서 기존 오류가 Project의 Draft 카드와 PR에는 기록돼 있지만 저장소 GitHub Issue에는 연결되지 않은 것을 확인했습니다. 요구사항 2의 Issue→수정→재검증 추적을 위해 이력을 사후 연결합니다. 아래 6건은 이미 PR #17로 수정된 과거 결함이며 새로 발견된 회귀로 보고하는 것이 아닙니다.

## 이미 main에 반영된 BUG-03

근거: [PR #15 조건부 병합 코멘트](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/pull/15#issuecomment-6383409868), [수정 PR #17](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/pull/17).
수정 commit: `11c9b6e3740a5547af59de62a0f614c571db0947`, main merge: `dbb89684cb87f231fb411c513018e64c04e990b6`.

- [x] 행·열 아이템 효과 종료가 정지 상태를 취소하고 메뉴 복귀 후 콜백이 남는 문제 → 효과 Timer/루프와 취소·정지 의도 관리.
- [x] 설정 메뉴 난이도 선택 누락 → Easy/Normal/Hard 옵션 연결.
- [x] L 표식 부착 위치 덮어쓰기 → 점유 칸 하나에 고정하고 회전 좌표 추적.
- [x] L 행의 점수·삭제 누계 누락 → 일반 완성 행과 합집합으로 한 번 계산.
- [x] 난이도별 속도 증가 차이 누락 → Normal 증가량 대비 0.8/1.0/1.2 계수 적용.
- [x] 무게추 최초 접촉 뒤 이동·회전 보정으로 잠금 우회 → 접촉 latch와 위치 보정 제한.

## PR #20에서 수정·검증했고 병합 대기인 결함

- [ ] 회전하지 않는 Bomb가 회전 위치 보정으로 벽/바닥에서 이동하는 결함을 main에 반영.
  - 재현: Bomb를 오른쪽 벽·바닥에 둔 뒤 회전 키 반복 입력.
  - 기대: 회전하지 않는 블록의 위치가 변하지 않음.
  - 수정: `Board.rotationPosition()`에서 Bomb 제외.
  - 회귀: `ColumnBombAcceptanceTest#bombRotationInputDoesNotKickAtWallOrFloor`.
  - 수정 commit: `15e46ec8ff6bfa530eb2da186bdc445517568e15`, [PR #20](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/pull/20).

## 재검증 근거

- [PR #20 원본 저장소 CI](https://github.com/HoroyoiPeach/SeoulTech-SE-Tetris-Ref/actions/runs/40185986802): Windows Server 2025·macOS 26 모두 성공.
- `15e46ec`에서 양 OS 각각 JUnit 95개, 실패·오류·건너뜀 0, 기존 독립 회귀 55/55, 전체 제품 line coverage 1499/1668=89.87%.
- 실제 CI 보고서 ZIP·실행 패키지 ZIP의 SHA-256과 내부 테스트·분포·DesktopSmoke 결과를 R2-07에서 대조했습니다.
- 위 자동 검증은 Windows 11 실기기·물리 키·사람의 색 식별·깨끗한 환경 저장/재실행의 완료 증빙을 대신하지 않습니다. TEST-06/RELEASE-04에서 계속 추적합니다.

## 종료 조건

- [ ] PR #18 → #19 → #20 순서의 검토·병합 후 Bomb 수정의 main 반영을 확인.
- [ ] 최종 main의 해당 회귀 검사·전체 검사 결과를 연결.
- [ ] R2-07 보드·최종 증빙 문서와 이 이슈의 해결 이력이 연결됨을 확인.

[프로젝트 보드](https://github.com/users/HoroyoiPeach/projects/3/views/1). 코드·검사·명세: `docs/requirements-2-traceability.md`, `docs/req2-final-verification.md` (chore/req2-final-verification 작업 중).
