# Courses assets (향후 확장용)

골프장별 그린 좌표 JSON을 이 폴더에 둡니다.

**현재 상태:** 구조만 준비됨. `MainActivity.kt`는 기존 하드코딩 좌표를 사용합니다.

## 파일 형식

```json
{
  "courseId": "k2_daegu",
  "courseName": "K2 대구",
  "holes": [
    { "hole": 1, "lat": 35.902494, "lng": 128.663617 }
  ]
}
```

## 샘플

- `k2_daegu.json` — 예시 1홀 (추후 18홀 데이터 확장 가능)
