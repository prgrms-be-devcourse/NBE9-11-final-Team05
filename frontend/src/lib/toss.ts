// 토스페이먼츠 클라이언트 키 (테스트키).
// 운영 배포 전, 실제 발급받은 라이브 키로 교체하고 .env로 옮겨주세요.
//   NEXT_PUBLIC_TOSS_CLIENT_KEY=live_ck_...
export const TOSS_CLIENT_KEY =
  process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY ??
  "test_ck_yL0qZ4G1VOdPdvOgZ1GProWb2MQY";