# 마트노트 변경 기록 (CHANGELOG)

> 사용자 지시 1건 = 1줄. 최신이 맨 위. 탐색·빌드·실기기 검증은 기록하지 않음.
> 형식: `- YYYY-MM-DD · {한 줄 요약} · {대표 파일들}` (커밋되면 끝에 ` (커밋 abc1234)` 추가)

## 미커밋 (워킹트리)

- 2026-06-09 · 알림 진동·소리 수정: 채널에 enableVibration(true)+진동패턴+라이트 추가, 채널 설정 불변 특성 때문에 채널 id를 item_reminders→item_reminders_v2로 교체(기존 채널 삭제 후 재생성) · `reminder/ReminderScheduler.kt`
- 2026-06-09 · 알림 진입 UX 변경: 항목 행에 종 아이콘 직접 추가(테두리=미설정/꽉찬종=설정·마트색, 한 번 탭→시트), ⋮ 메뉴의 알림 설정/변경/끄기 항목 제거(끄기는 시트 내 "알림 끄기"로 통합) · `ui/screens/home/components/{ItemRow,ItemList}.kt`, `ui/screens/home/HomeScreen.kt`
- 2026-06-09 · 항목별 일회성 푸시 알림 기능: 항목 ⋮ "알림 설정/변경/끄기" + 날짜·시각 선택 시트 + 이름 아래 🔔 칩 표시, 로컬 알림(AlarmManager inexact, 재부팅 재등록), 알림 탭→해당 마트·항목 하이라이트, POST_NOTIFICATIONS 13+ 런타임 권한. Room v1→v2(reminder_at). 버전 1.0.0→1.1.0(vc2) · `domain/model/Item.kt`, `data/local/{ItemEntity,ItemDao,AppDatabase}.kt`, `data/repository/ItemRepository.kt`, `reminder/{ReminderScheduler,ReminderReceiver,BootReceiver}.kt` (신규), `di/ReminderEntryPoint.kt` (신규), `ui/screens/home/components/{ReminderPickerSheet(신규),ItemRow,ItemList}.kt`, `ui/screens/home/{HomeScreen,HomeViewModel}.kt`, `util/ReminderFormat.kt` (신규), `res/drawable/ic_stat_reminder.xml` (신규), `AndroidManifest.xml`, `app/build.gradle.kts`
- 2026-05-26 · 위젯 어댑티브 사이즈 확장: XLarge breakpoint(320×320) 추가 + xml maxResize 5개 모두 360→480, 최대 6마트(5: 2x2+1, 6: 2x3) XLargeContent 신규. 위젯 클래스/picker는 그대로 (어댑티브 전용) · `widget/WidgetSizes.kt`, `widget/common/WidgetCommon.kt`, `res/xml/grocery_widget_{2x1,small,long,medium,large}_info.xml` (5개)
- 2026-05-26 · 공유 다이얼로그 부탁 문구 15개 무작위 회전: DEFAULT_SHARE_REQUEST_NOTE 단일 상수 → SHARE_REQUEST_PLACEHOLDERS 15개 리스트, 다이얼로그 인스턴스마다 random() 추첨 (remember 로 깜빡임 방지) · `ui/screens/home/components/ShareRequestDialog.kt`
- 2026-05-26 · 공유 다이얼로그 빈 입력 fallback 수정: placeholder("이거 사다 줄래? 🥺")가 시각용에 그치지 않고 빈 채로 보내면 실제 전송 텍스트로 들어가도록. DEFAULT_SHARE_REQUEST_NOTE 상수로 단일화 + 안내 카피 "비워두면 기본 문구로 보내져요" · `ui/screens/home/components/ShareRequestDialog.kt`
- 2026-05-26 · 공유 전 부탁 문구 입력 다이얼로그 추가 (placeholder "이거 사다 줄래? 🥺", 200자 캡, 비워두면 리스트만 공유). buildShareText 함수 분리 + 미래 OS분기 스토어 링크 자리 시그니처로 열어둠 · `ui/screens/home/components/ShareRequestDialog.kt` (신규), `ui/screens/home/HomeScreen.kt`
- 2026-05-26 · 공유 텍스트 포맷 수정: 푸터를 "마트노트에서 보냄"으로 변경, 스토어 다운로드 링크 제거 (심부름 용도 피벗) · `ui/screens/home/HomeScreen.kt`
- 2026-05-26 · 홈 우하단 "추가" FAB 위에 공유 SmallFAB 추가 (현재 마트 미완료 항목 텍스트 공유, 1초 연타 방지, 빈 리스트·앱 없음 Snackbar 처리) · `ui/screens/home/HomeScreen.kt`
- 2026-05-26 · 홈 상단 타이틀 "구매예정" → "마트노트"로 변경 · `ui/screens/home/HomeScreen.kt`
- 2026-05-25 · AI 협업용 종합 인수인계서 작성 (자기완결, 20개 섹션, CLAUDE.md/HANDOVER.md 충돌 시 우선 문서) · `docs/HANDOFF_AI협업_종합.md`
- 2026-05-25 · 위젯 클릭 라우팅 개선: 항목 행/마트 헤더/카운트 행이 각자 `storeId`로 명시적 click 부착 → 어디를 탭하든 그 마트로 정확히 라우팅 · `widget/common/WidgetCommon.kt`, `widget/components/WidgetItemRow.kt`, `widget/components/WidgetStoreCountRow.kt`, `widget/components/WidgetStoreHeader.kt`
- 2026-05-21 · 설정 화면 "인트로 미리보기" 메뉴 숨김 (주석으로 보존, 복구 가능) · `ui/screens/settings/SettingsScreen.kt`

## 커밋됨 (참고용, git log와 동기화 안 됨 — 정확한 이력은 `git log` 참조)

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
