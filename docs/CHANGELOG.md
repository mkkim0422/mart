# 마트노트 변경 기록 (CHANGELOG)

> 사용자 지시 1건 = 1줄. 최신이 맨 위. 탐색·빌드·실기기 검증은 기록하지 않음.
> 형식: `- YYYY-MM-DD · {한 줄 요약} · {대표 파일들}` (커밋되면 끝에 ` (커밋 abc1234)` 추가)

## 미커밋 (워킹트리)

- 2026-06-19 · Play 출시 자료 일괄 제작: 피처 그래픽 1024×500(`scripts/compose_feature_graphic.py`→`docs/playstore_assets/final/feature_graphic_1024x500.png`), 스토어 등록 복붙 문서(앱이름·짧은/긴 설명·데이터보안·콘텐츠등급·인앱상품 가이드 `docs/playstore_listing.md`), 릴리스 서명 키스토어 생성(`grocery-note-release.jks`, alias grocerynote, gitignore됨) + `keystore.properties` 채움 → **서명된 업로드용 AAB** 빌드(`app/build/outputs/bundle/release/app-release.aab`, 13.8MB, jar verified) · 신규 자료/스크립트

- 2026-06-19 · 리스트 하단 항목이 음성추가/추가 FAB에 가려지던 문제 수정: `ItemList` LazyColumn 하단 contentPadding 8dp→92dp(FAB 56dp+여백)로 마지막 항목이 버튼 위로 스크롤됨. 실기기 확인 완료 · `ui/screens/home/components/ItemList.kt`

- 2026-06-18 · Play스토어 스크린샷 시리즈 v2 재생성(현재 버전·5마트 기준): 실기기(S24+) 캡처 7장(홈/음성인트로/추가시트/위젯피커/완료/이마트홈/다크) → 폰목업+그라데이션+한글카피 합성. 01 마트별 / 02 음성추가 / 03 1초 입력 / 04 위젯 5크기 / 05 완료 / 06 마트색구분 / 07 다크. 광고밴드 자동 제거(y2622–2872) · `scripts/compose_store_v2.py`(신규), `docs/playstore_assets/final/01–07*.png`(교체), `docs/playstore_assets/raw/ss1–7*.png`(교체)

- 2026-06-20 · (세션 중 시도→되돌림) 공유 버튼 상단 ⋮ 이동 / 음성 입력칸 마이크 이동 / 음성 인트로·설정 가이드 추가 등을 검토했으나, 최종적으로 **원래 레이아웃(음성추가 pill + 공유 FAB)** 유지로 결정해 `git checkout HEAD`로 `HomeScreen.kt`·`AddItemSheet.kt` 복원. 리스트 하단 패딩 수정은 유지. versionCode 2→3 · `app/build.gradle.kts`

- 2026-06-16 · 구글 플레이 스토어용 음성추가 스크린샷(07) 제작: 기존 6장과 동일 스타일(그라데이션 배경+폰 목업+캡션)로 음성추가 홈화면을 렌더링, 🎤 "라면 추가" 말풍선 오버레이. 기기 캡처 대신 결정론적 렌더(폰 분리·에뮬 ANR로) · `scripts/compose_voice_shot.py`(신규), `docs/playstore_assets/final/07_voice_add.png`(신규)

- 2026-06-16 · 음성으로 항목 추가: 홈 `추가` 버튼 옆 같은 레벨에 `음성추가` 버튼(마트색 테두리), 시스템 음성인식(RecognizerIntent, ko-KR, RECORD_AUDIO 권한·라이브러리 불필요)으로 현재 마트에 즉시 추가. "라면 추가"→끝 명령어("추가/담아/넣어줘" 등) 제거 후 "라면"만 추가, 인식 성공 시 자동 재실행(계속 듣기). "끝/완료/그만/종료" 등 종료어를 말하면 추가 안 하고 종료(뒤로가기로도 종료) · `ui/screens/home/HomeScreen.kt`

## 커밋됨 (참고용, git log와 동기화 안 됨 — 정확한 이력은 `git log` 참조)

- 2026-06-16 · 탭/필터 스와이프 전환을 네이트식 가로 슬라이드로 변경: 탭 줄 고정·본문만 손가락 방향대로 280ms 슬라이드+페이드(FastOutSlowIn), 부트스트랩은 페이드만. (제거→220ms 크로스페이드→0.97 스케일페이드를 거쳐 최종 슬라이드로 확정) · `ui/screens/home/HomeScreen.kt`, `ui/screens/completed/CompletedScreen.kt` (커밋 `c33ade6`)
- 2026-06-09 · 알림 진동·소리 수정: 채널 enableVibration(true)+패턴, 채널 id item_reminders→item_reminders_v2 교체 · `reminder/ReminderScheduler.kt` (커밋 `0deda0e`)
- 2026-06-09 · 알림 진입 UX 변경: 항목 행 종 아이콘으로 진입(테두리=미설정/꽉찬종=설정·마트색), ⋮ 메뉴 알림 항목 제거 · `ui/screens/home/components/{ItemRow,ItemList}.kt`, `ui/screens/home/HomeScreen.kt` (커밋 `f2f0aec`)
- 2026-06-09 · BootReceiver LOCKED_BOOT_COMPLETED 제거 (Direct Boot 시 DB 접근 불가) · `reminder/BootReceiver.kt` (커밋 `0a34ae2`)
- 2026-06-09 · 항목별 일회성 푸시 알림 기능 (v1.0.0→1.1.0/vc2): 날짜·시각 시트 + 🔔 칩 + 로컬 알림(AlarmManager inexact, 재부팅 재등록) + 알림 탭→마트·항목 하이라이트 + POST_NOTIFICATIONS 13+ 권한. Room v1→v2(reminder_at) · `reminder/*`(신규), `di/ReminderEntryPoint.kt`, `data/local/*`, `util/ReminderFormat.kt`, `AndroidManifest.xml` 외 (커밋 `dce5bf1`)
- 2026-05-26 · 위젯 XLarge breakpoint(320×320, 최대 6마트) + 공유 다이얼로그(부탁 문구 200자·15개 회전) + 공유 SmallFAB + 홈 타이틀 "마트노트" + 위젯 클릭 라우팅 개선 등 5/21~5/26 작업 일괄 · `widget/*`, `ui/screens/home/components/ShareRequestDialog.kt` 외 (커밋 `e1e57c4`~`62e5019` 사이)
- 2026-05-19 · 완료 화면 리스트 스와이프 삭제/되돌리기 제거 (실기기 미작동) (커밋 `8475fc4`)
- 2026-05-19 · 온보딩 위젯 페이지를 마지막으로 이동 + 다크모드 이미지 실기기 캡처로 교체 (커밋 `f652afd`)
- 2026-05-19 · 다크모드 온보딩 페이지 추가 + 온보딩/스플래시 구조 개선 + 스샷2 실기기화 (커밋 `e1e57c4`)
- 2026-05-19 · AdMob ID debug/release 완전 분리 + 릴리스 로그·분석 하드닝 (커밋 `7f3f51b`)
- 2026-05-19 · 아이콘 여백 + 스플래시 일치 + 온보딩→메인 전환 매끄럽게 (커밋 `7fb405d`)
- 2026-05-19 · 디자인 원본 자산 + 디자인 인수인계서 보관 (커밋 `be71e8f`)
- 2026-05-19 · Phase 10 실기기 피드백 수정 (시트 마트선택·배너 노출·위젯 핀 크기) (커밋 `62e5019`)
- 2026-05-18 · 온보딩 일러스트·어댑티브 아이콘 + Play Store 스크린샷 6장 (커밋 `9ee412b`)
- 2026-05-18 · 위젯 핀 사례 교훈 인수인계서 추가 (커밋 `0bd96bf`)
- 2026-05-18 · 위젯 핀 성공 후 홈화면 자동 이동 (커밋 `5767e42`)
- 2026-05-18 · Long(2x4) 위젯 추가 + 5종 적응형 리사이즈, 피커/온보딩 카피 정리 (커밋 `f3d2e9b`)
- 2026-05-18 · 위젯 모델 표시 전용 피벗, 완료 애니메이션 V2, 카피·문서 동기화 (커밋 `f6d8177`)
- 2026-05-18 · 앱명을 마트노트로 변경, 4사이즈 위젯, 마트 관리, UX 폴리싱 (커밋 `987a26b`)
- 2026-05-14 · Phase 3-6 일괄: 풀 앱, 위젯, 광고/결제, 릴리스 (커밋 `e4d78cd`)
- 2026-05-14 · Phase 2: Room 데이터 계층 + Hilt + 개발 테스트 화면 (커밋 `43b245a`)
- 2026-05-14 · Phase 1: 프로젝트 부트스트랩 + 토스 디자인 시스템 (커밋 `85f320d`)
