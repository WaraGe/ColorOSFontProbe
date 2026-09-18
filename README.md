# ColorOS Font Probe 0.1

오포/ColorOS에서 루트 없이 사용자 TTF/OTF를 시스템 폰트로 적용할 수 있는 공개/반공개 경로가 남아 있는지 확인하기 위한 실험용 앱입니다.

## 현재 버전에서 하는 일
- TTF/OTF 선택 및 앱 내부 복사
- 선택 폰트 미리보기
- `com.oplus.themestore` 존재 여부 확인
- ColorOS의 알려진 `SET_FONT` 계열 Intent resolve 및 호출 테스트
- `font`, `opluscustomize`, `theme` 계열 Binder 이름 탐색
- Android FontManager 관련 reflection 결과 출력
- 테스트용 ContentProvider로 선택한 폰트를 read-only URI로 노출
- 모든 결과를 앱 화면 로그로 표시/복사

## 중요한 점
이 버전은 **Probe(탐색용)** 입니다. ThemeStore가 삭제된 기기에서 바로 시스템 폰트를 변경한다고 보장하지 않습니다.
성공/실패 로그를 기반으로 ColorOS 전용 Binder/권한/Shizuku 경로를 다음 버전에서 좁히는 목적입니다.

## 빌드
Android Studio에서 프로젝트를 열고 `Build > Build APK(s)`를 실행합니다.
JDK 17+, Android SDK 35 권장.
